package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.MallOrderDto;
import com.lqx.opera.common.dto.MallOrderItemDto;
import com.lqx.opera.entity.MallOrder;
import com.lqx.opera.entity.MallOrderItem;
import com.lqx.opera.entity.Product;
import com.lqx.opera.service.MallOrderItemService;
import com.lqx.opera.service.MallOrderService;
import com.lqx.opera.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final MallOrderService mallOrderService;
    private final MallOrderItemService mallOrderItemService;
    private final ProductService productService;

    public SellerOrderController(MallOrderService mallOrderService,
                                 MallOrderItemService mallOrderItemService,
                                 ProductService productService) {
        this.mallOrderService = mallOrderService;
        this.mallOrderItemService = mallOrderItemService;
        this.productService = productService;
    }

    @GetMapping
    @RequireRole({1}) // Inheritor
    public Result<List<MallOrderDto>> listSellerOrders(HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        
        // 1. Find products sold by this seller
        List<Product> myProducts = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, sellerId));
        
        if (myProducts.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        
        Set<Long> myProductIds = myProducts.stream().map(Product::getProductId).collect(Collectors.toSet());
        
        // 2. Find order items containing these products
        List<MallOrderItem> relatedItems = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                .in(MallOrderItem::getProductId, myProductIds));
        
        if (relatedItems.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        
        Set<Long> orderIds = relatedItems.stream().map(MallOrderItem::getOrderId).collect(Collectors.toSet());
        
        // 3. Find orders
        List<MallOrder> orders = mallOrderService.listByIds(orderIds);
        
        // 4. Build DTOs
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<MallOrderDto> dtos = orders.stream()
                .sorted(Comparator.comparing(MallOrder::getId).reversed())
                .map(order -> {
            MallOrderDto dto = new MallOrderDto();
            BeanUtils.copyProperties(order, dto);
            
            // Format time
            LocalDateTime createTime = order.getCreateTime();
            // Handle timestamp-based orderNo if createTime is null (legacy logic)
            if (createTime == null && order.getOrderNo() != null && order.getOrderNo().length() > 17) {
                try {
                    String tsStr = order.getOrderNo().substring(4, 17);
                    long ts = Long.parseLong(tsStr);
                    createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
                } catch (Exception ignore) {}
            }
            if (createTime != null) {
                dto.setCreateTime(dtf.format(createTime));
                if (order.getStatus() != null && order.getStatus() >= 1) {
                    dto.setPayTime(dtf.format(createTime)); // Approx
                }
            }

            // 5. Filter items: Only show items belonging to this seller
            List<MallOrderItem> orderAllItems = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                    .eq(MallOrderItem::getOrderId, order.getId()));
            
            List<MallOrderItemDto> myItemDtos = orderAllItems.stream()
                    .filter(item -> myProductIds.contains(item.getProductId()))
                    .map(item -> {
                        MallOrderItemDto itemDto = new MallOrderItemDto();
                        BeanUtils.copyProperties(item, itemDto);
                        // Find product image
                        myProducts.stream()
                                .filter(p -> p.getProductId().equals(item.getProductId()))
                                .findFirst()
                                .ifPresent(p -> itemDto.setProductImage(p.getMainImage()));
                        return itemDto;
                    })
                    .collect(Collectors.toList());
            
            dto.setItems(myItemDtos);
            return dto;
        }).collect(Collectors.toList());

        return Result.success(dtos);
    }

    @PostMapping("/ship/{orderId}")
    @RequireRole({1})
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> shipOrder(@PathVariable Long orderId, 
                                     @RequestParam(required = false) String deliveryCompany, 
                                     @RequestParam String deliveryNo,
                                     HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        
        MallOrder order = mallOrderService.getById(orderId);
        if (order == null) return Result.fail("订单不存在");
        
        if (order.getStatus() != 1) return Result.fail("订单状态不正确，无法发货");

        // Verify ownership: Order must contain at least one product from this seller
        // (Simplified logic: Assuming trusting seller or single-seller orders mostly. 
        //  Ideally we check if ALL items are from this seller, but for now we allow if ANY.)
        boolean isMyOrder = checkOrderOwnership(orderId, sellerId);
        if (!isMyOrder) return Result.fail("无权操作此订单");

        order.setDeliveryCompany(deliveryCompany);
        order.setDeliveryNo(deliveryNo);
        order.setStatus(2); // Shipped
        mallOrderService.updateById(order);
        
        return Result.success(true);
    }

    @PostMapping("/refund/{orderId}")
    @RequireRole({1})
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> refundOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        
        MallOrder order = mallOrderService.getById(orderId);
        if (order == null) return Result.fail("订单不存在");

        // Allow refund if Paid(1) or RefundRequested(4)
        if (order.getStatus() != 1 && order.getStatus() != 4) {
            return Result.fail("订单状态不正确");
        }

        boolean isMyOrder = checkOrderOwnership(orderId, sellerId);
        if (!isMyOrder) return Result.fail("无权操作此订单");

        // Execute Refund Logic (Simplified: Status 3 + Stock Rollback)
        order.setStatus(3); // Cancelled/Refunded
        mallOrderService.updateById(order);

        // Rollback Stock for ALL items (Since we cancel the whole order)
        // Note: This affects other sellers if mixed. Accepting this limitation per current architecture.
        List<MallOrderItem> items = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, orderId));
        
        for (MallOrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productService.updateById(product);
            }
        }

        return Result.success(true);
    }

    private boolean checkOrderOwnership(Long orderId, Long sellerId) {
        List<MallOrderItem> items = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, orderId));
        Set<Long> productIds = items.stream().map(MallOrderItem::getProductId).collect(Collectors.toSet());
        if (productIds.isEmpty()) return false;

        long count = productService.count(new LambdaQueryWrapper<Product>()
                .in(Product::getProductId, productIds)
                .eq(Product::getSellerId, sellerId));
        return count > 0;
    }
}
