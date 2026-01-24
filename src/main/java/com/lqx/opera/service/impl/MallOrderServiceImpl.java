package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.common.dto.CreateMallOrderItem;
import com.lqx.opera.entity.MallOrder;
import com.lqx.opera.entity.MallOrderItem;
import com.lqx.opera.entity.Product;
import com.lqx.opera.mapper.MallOrderMapper;
import com.lqx.opera.service.MallOrderItemService;
import com.lqx.opera.service.MallOrderService;
import com.lqx.opera.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    private final ProductService productService;
    private final MallOrderItemService mallOrderItemService;

    public MallOrderServiceImpl(ProductService productService, MallOrderItemService mallOrderItemService) {
        this.productService = productService;
        this.mallOrderItemService = mallOrderItemService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrder createOrder(Long userId, List<CreateMallOrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        List<MallOrderItem> toSaveItems = new ArrayList<>();

        for (CreateMallOrderItem item : items) {
            // 锁定库存检查（简单的先查询再更新，高并发需用乐观锁或Redis，此处按题目要求先做基本事务）
            Product p = productService.getById(item.getProductId());
            if (p == null || (p.getStatus() != null && p.getStatus() == 0)) {
                throw new RuntimeException("商品不存在或已下架 " + item.getProductId());
            }
            if (p.getStock() < item.getQuantity()) {
                throw new RuntimeException("库存不足: " + p.getName());
            }

            // 扣减库存
            p.setStock(p.getStock() - item.getQuantity());
            boolean updated = productService.updateById(p);
            if (!updated) {
                throw new RuntimeException("扣减库存失败: " + p.getName());
            }

            BigDecimal price = p.getPrice();
            BigDecimal line = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(line);

            MallOrderItem oi = new MallOrderItem();
            oi.setProductId(p.getProductId());
            oi.setProductName(p.getName());
            oi.setPrice(price);
            oi.setQuantity(item.getQuantity());
            toSaveItems.add(oi);
        }

        MallOrder order = new MallOrder();
        order.setOrderNo("MORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6));
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(1); // 模拟支付成功 -> 已支付待发货
        order.setCreateTime(LocalDateTime.now());
        this.save(order);

        Long orderId = order.getId();
        for (MallOrderItem oi : toSaveItems) {
            oi.setOrderId(orderId);
        }
        mallOrderItemService.saveBatch(toSaveItems);
        
        return order;
    }

    @Override
    public boolean applyRefund(Long orderId, Long userId) {
        MallOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("当前状态无法申请退款");
        }
        order.setStatus(4); // 4-Refund Pending
        return this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditRefund(Long orderId, boolean pass) {
        MallOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 4) {
            throw new RuntimeException("订单不是待审核状态");
        }

        if (pass) {
            // 通过：状态改为5-Refunded
            // 恢复库存
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MallOrderItem> query = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            query.eq(MallOrderItem::getOrderId, order.getId());
            List<MallOrderItem> items = mallOrderItemService.list(query);
            for (MallOrderItem item : items) {
                Product p = productService.getById(item.getProductId());
                if (p != null) {
                    p.setStock(p.getStock() + item.getQuantity());
                    productService.updateById(p);
                }
            }
            order.setStatus(5);
        } else {
            // 驳回：状态回退到1-Paid
            order.setStatus(1);
        }
        return this.updateById(order);
    }
}

