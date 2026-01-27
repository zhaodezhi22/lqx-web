package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.CreateMallOrderRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mall")
public class MallOrderController {

    private final MallOrderService mallOrderService;
    private final MallOrderItemService mallOrderItemService;
    private final ProductService productService;
    private final com.lqx.opera.service.AdminOrderService adminOrderService;

    public MallOrderController(MallOrderService mallOrderService,
                               MallOrderItemService mallOrderItemService,
                               ProductService productService,
                               com.lqx.opera.service.AdminOrderService adminOrderService) {
        this.mallOrderService = mallOrderService;
        this.mallOrderItemService = mallOrderItemService;
        this.productService = productService;
        this.adminOrderService = adminOrderService;
    }

    @PostMapping("/refund/apply/{id}")
    @RequireRole({0, 1, 2, 3})
    public Result<Boolean> applyRefund(@PathVariable Long id, HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        Long userId;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else {
            userId = Long.parseLong(userIdObj.toString());
        }

        try {
            boolean ok = mallOrderService.applyRefund(id, userId);
            return ok ? Result.success(true) : Result.fail("申请失败");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/refund/audit/{id}")
    @RequireRole({2, 3}) // Admin/Reviewer
    public Result<Boolean> auditRefund(@PathVariable Long id, @RequestParam Boolean pass) {
        try {
            boolean ok = mallOrderService.auditRefund(id, pass);
            return ok ? Result.success(true) : Result.fail("审核失败");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核员发货
     */
    @PostMapping("/ship/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> shipOrder(@PathVariable Long id, 
                                     @RequestParam(required = false) String deliveryCompany, 
                                     @RequestParam String deliveryNo) {
        try {
            adminOrderService.shipOrder(id, deliveryCompany, deliveryNo);
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核员确认退款/强制取消
     */
    @PostMapping("/refund/confirm/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> confirmRefund(@PathVariable Long id) {
        try {
            adminOrderService.confirmRefund(id);
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/orders")
    @RequireRole({2, 3}) // Admin/Reviewer
    public Result<List<MallOrder>> getOrders(@RequestParam(required = false) Integer status) {
        try {
            LambdaQueryWrapper<MallOrder> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(MallOrder::getStatus, status);
            }
            wrapper.orderByDesc(MallOrder::getId); // Using new ID field
            return Result.success(mallOrderService.list(wrapper));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping(value = {"/orders", "/order/create"})
    @RequireRole({0, 1, 2, 3})
    public Result<String> createOrder(@RequestBody CreateMallOrderRequest createReq, HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
        }
        Long userId;
        if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        } else {
            userId = Long.parseLong(userIdObj.toString());
        }

        try {
            if (createReq.getItems() == null || createReq.getItems().isEmpty()) {
                return Result.fail(400, "订单商品不能为空");
            }
            MallOrder order = mallOrderService.createOrder(userId, createReq.getItems());
            
            // 如果 CreateMallOrderRequest 有 address，更新它
            if (createReq.getAddress() != null && !createReq.getAddress().isEmpty()) {
                order.setAddressSnapshot(createReq.getAddress());
                mallOrderService.updateById(order);
            }
            return Result.success(order.getOrderNo());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "下单失败: " + e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    @RequireRole({0, 1, 2, 3})
    public Result<List<MallOrderDto>> getMyOrders(HttpServletRequest request) {
        try {
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return Result.fail(HttpStatus.UNAUTHORIZED.value(), "未登录");
            }
            Long userId;
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                userId = Long.parseLong((String) userIdObj);
            } else {
                throw new IllegalArgumentException("Invalid userId type: " + userIdObj.getClass());
            }

            // 1. Query Orders
            LambdaQueryWrapper<MallOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(MallOrder::getUserId, userId)
                        .orderByDesc(MallOrder::getId); // Using new ID field
            List<MallOrder> orders = mallOrderService.list(orderWrapper);

            if (orders.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            // 2. Collect Order IDs
            List<Long> orderIds = orders.stream().map(MallOrder::getId).collect(Collectors.toList());

            // 3. Batch Query Items
            LambdaQueryWrapper<MallOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(MallOrderItem::getOrderId, orderIds);
            List<MallOrderItem> allItems = mallOrderItemService.list(itemWrapper);

            // Collect all product IDs to fetch images
            Set<Long> productIds = allItems.stream()
                .map(MallOrderItem::getProductId)
                .collect(Collectors.toSet());
            
            Map<Long, String> productImageMap = new HashMap<>();
            if (!productIds.isEmpty()) {
                List<Product> products = productService.listByIds(productIds);
                for (Product p : products) {
                    productImageMap.put(p.getProductId(), p.getMainImage());
                }
            }

            // 4. Map to DTO
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            List<MallOrderDto> dtos = orders.stream().map(order -> {
                MallOrderDto dto = new MallOrderDto();
                BeanUtils.copyProperties(order, dto);
                // dto.setId(null); // 不要隐藏订单 ID，前端操作需要使用

                // 优先使用数据库存储的 createTime
                LocalDateTime createTime = order.getCreateTime();
                
                // 如果数据库没有 createTime (旧数据)，尝试从订单号解析
                if (createTime == null && order.getOrderNo() != null && order.getOrderNo().length() > 17) {
                    try {
                        String tsStr = order.getOrderNo().substring(4, 17);
                        long ts = Long.parseLong(tsStr);
                        createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
                    } catch (Exception ignore) {}
                }

                if (createTime != null) {
                    // 设置下单时间
                    dto.setCreateTime(dtf.format(createTime));

                    // 模拟支付时间 = 下单时间
                    if (order.getStatus() != null && order.getStatus() >= 1) {
                        dto.setPayTime(dtf.format(createTime));
                    }

                    // 模拟发货时间 = 下单时间 + 48小时 (如果待发货)
                    LocalDateTime deadline = createTime.plusHours(48);
                    if (order.getStatus() != null && order.getStatus() == 1) {
                         long minutes = java.time.temporal.ChronoUnit.MINUTES.between(LocalDateTime.now(), deadline);
                         long hours = (long) Math.ceil(minutes / 60.0);
                         if (hours <= 0) hours = 1;
                         dto.setShipTime(hours + "小时内");
                    } else if (order.getStatus() != null && order.getStatus() >= 2) {
                         // 已发货则显示下单+48h作为模拟发货时间
                         dto.setShipTime(dtf.format(deadline));
                    }
                }
                
                // Filter items for this order
                List<MallOrderItemDto> myItems = allItems.stream()
                        .filter(item -> item.getOrderId() != null && item.getOrderId().equals(order.getId()))
                        .map(item -> {
                            MallOrderItemDto itemDto = new MallOrderItemDto();
                            BeanUtils.copyProperties(item, itemDto);
                            itemDto.setProductImage(productImageMap.get(item.getProductId()));
                            return itemDto;
                        })
                        .collect(Collectors.toList());
                dto.setItems(myItems);
                
                return dto;
            }).collect(Collectors.toList());

            return Result.success(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "加载订单失败 [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
    }
}
