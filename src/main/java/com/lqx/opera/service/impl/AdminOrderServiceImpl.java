package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.entity.MallOrder;
import com.lqx.opera.entity.MallOrderItem;
import com.lqx.opera.entity.Product;
import com.lqx.opera.service.AdminOrderService;
import com.lqx.opera.service.MallOrderItemService;
import com.lqx.opera.service.MallOrderService;
import com.lqx.opera.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private final MallOrderService mallOrderService;
    private final MallOrderItemService mallOrderItemService;
    private final ProductService productService;

    public AdminOrderServiceImpl(MallOrderService mallOrderService,
                                 MallOrderItemService mallOrderItemService,
                                 ProductService productService) {
        this.mallOrderService = mallOrderService;
        this.mallOrderItemService = mallOrderItemService;
        this.productService = productService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, String deliveryCompany, String deliveryNo) {
        MallOrder order = mallOrderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // 只能对已支付(1)或已发货(2)的订单操作
        if (order.getStatus() == null || (order.getStatus() != 1 && order.getStatus() != 2)) {
            throw new RuntimeException("订单状态不正确，无法操作物流");
        }
        
        order.setDeliveryCompany(deliveryCompany);
        order.setDeliveryNo(deliveryNo);
        if (order.getStatus() == 1) {
            order.setStatus(2); // 2-已发货
        }
        mallOrderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmRefund(Long orderId) {
        MallOrder order = mallOrderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        // 检查 mall_order 状态是否为待发货/已支付 (Status 1) 或 申请退款 (Status 4)
        if (order.getStatus() == null || (order.getStatus() != 1 && order.getStatus() != 4)) {
             throw new RuntimeException("订单状态不正确，只能取消待发货/已支付或申请退款的订单");
        }

        // 模拟调用支付退款接口...
        // PaymentService.refund(order.getPaymentId(), order.getTotalAmount());

        // 1. 更新状态为已取消 (这里使用 3-已取消)
        order.setStatus(3);
        mallOrderService.updateById(order);

        // 2. 库存回滚
        List<MallOrderItem> items = mallOrderItemService.list(new LambdaQueryWrapper<MallOrderItem>()
                .eq(MallOrderItem::getOrderId, orderId));
        
        for (MallOrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productService.updateById(product);
            }
        }
    }
}
