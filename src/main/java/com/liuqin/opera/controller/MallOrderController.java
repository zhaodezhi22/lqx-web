package com.liuqin.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuqin.opera.common.Result;
import com.liuqin.opera.common.annotation.RequireRole;
import com.liuqin.opera.common.dto.CreateMallOrderItem;
import com.liuqin.opera.common.dto.CreateMallOrderRequest;
import com.liuqin.opera.common.dto.ShipRequest;
import com.liuqin.opera.entity.MallOrder;
import com.liuqin.opera.entity.MallOrderItem;
import com.liuqin.opera.entity.Product;
import com.liuqin.opera.service.MallOrderItemService;
import com.liuqin.opera.service.MallOrderService;
import com.liuqin.opera.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mall")
public class MallOrderController {

    private final MallOrderService mallOrderService;
    private final MallOrderItemService mallOrderItemService;
    private final ProductService productService;

    public MallOrderController(MallOrderService mallOrderService,
                               MallOrderItemService mallOrderItemService,
                               ProductService productService) {
        this.mallOrderService = mallOrderService;
        this.mallOrderItemService = mallOrderItemService;
        this.productService = productService;
    }

    @PostMapping("/orders")
    public Result<MallOrder> createOrder(@RequestBody CreateMallOrderRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return Result.fail(400, "订单项不能为空");
        }
        try {
            MallOrder order = mallOrderService.createOrder(userId, req.getItems());
            return Result.success(order);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            return Result.fail(500, "下单失败: " + e.getMessage());
        }
    }

    @GetMapping("/orders")
    @RequireRole({2, 3})
    public Result<List<MallOrder>> listOrders(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<MallOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MallOrder::getStatus, status);
        }
        wrapper.orderByDesc(MallOrder::getCreatedTime);
        return Result.success(mallOrderService.list(wrapper));
    }

    @GetMapping("/orders/{orderId}/items")
    @RequireRole({2, 3})
    public Result<List<MallOrderItem>> listOrderItems(@PathVariable Long orderId) {
        LambdaQueryWrapper<MallOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallOrderItem::getOrderId, orderId);
        return Result.success(mallOrderItemService.list(wrapper));
    }

    @PutMapping("/orders/{orderId}/ship")
    @RequireRole({2, 3})
    public Result<Boolean> shipOrder(@PathVariable Long orderId, @RequestBody(required = false) ShipRequest shipRequest) {
        MallOrder order = mallOrderService.getById(orderId);
        if (order == null) {
            return Result.fail(404, "订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            return Result.fail(400, "订单状态不可发货");
        }
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        if (shipRequest != null) {
            order.setLogisticsNo(shipRequest.getLogisticsNo());
        }
        boolean ok = mallOrderService.updateById(order);
        return ok ? Result.success(true) : Result.fail("发货失败");
    }

    @GetMapping("/my-orders")
    @RequireRole({0, 1, 2, 3})
    public Result<List<MallOrder>> myOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        LambdaQueryWrapper<MallOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MallOrder::getUserId, userId)
                .orderByDesc(MallOrder::getCreatedTime);
        List<MallOrder> orders = mallOrderService.list(wrapper);
        for (MallOrder order : orders) {
            LambdaQueryWrapper<MallOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(MallOrderItem::getOrderId, order.getOrderId());
            order.setItems(mallOrderItemService.list(itemWrapper));
        }
        return Result.success(orders);
    }

    @GetMapping("/sales-stats")
    @RequireRole(1)
    public Result<Object> salesStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        List<Product> myProducts = productService.list(new LambdaQueryWrapper<Product>().eq(Product::getSellerId, userId));
        if (myProducts.isEmpty()) {
            return Result.success(java.util.Map.of("totalSales", 0, "orderCount", 0, "productCount", 0));
        }
        List<Long> productIds = myProducts.stream().map(Product::getProductId).collect(java.util.stream.Collectors.toList());
        
        List<MallOrderItem> items = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                .in(MallOrderItem::getProductId, productIds));
                
        java.math.BigDecimal totalSales = items.stream()
            .map(item -> item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            
        long orderCount = items.stream().map(MallOrderItem::getOrderId).distinct().count();
        
        return Result.success(java.util.Map.of(
            "totalSales", totalSales,
            "orderCount", orderCount,
            "productCount", myProducts.size()
        ));
    }
}
