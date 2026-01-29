package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.CreateMallOrderItem;
import com.lqx.opera.entity.MallOrder;

import java.util.List;

public interface MallOrderService extends IService<MallOrder> {
    /**
     * 创建订单
     */
    MallOrder createOrder(Long userId, List<CreateMallOrderItem> items, Integer usedPoints, Long addressId);

    /**
     * 申请退款
     */
    boolean applyRefund(Long orderId, Long userId);

    /**
     * 审核退款
     */
    boolean auditRefund(Long orderId, boolean pass);

    /**
     * 确认收货
     */
    void confirmReceipt(Long orderId, Long userId);
}

