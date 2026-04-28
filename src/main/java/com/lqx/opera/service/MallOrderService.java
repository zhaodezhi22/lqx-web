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
     * 卖家审核退款
     */
    boolean auditRefundBySeller(Long orderId, boolean pass, Long sellerId);

    /**
     * 自动通过超时未审核的退款申请
     */
    int autoApproveExpiredRefunds();

    /**
     * 确认收货
     */
    void confirmReceipt(Long orderId, Long userId);
}

