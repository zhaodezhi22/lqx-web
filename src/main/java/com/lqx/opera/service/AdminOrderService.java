package com.lqx.opera.service;

public interface AdminOrderService {
    /**
     * 审核员发货
     * @param orderId 订单ID
     * @param deliveryCompany 物流公司
     * @param deliveryNo 快递单号
     */
    void shipOrder(Long orderId, String deliveryCompany, String deliveryNo);

    /**
     * 审核员确认退款/取消订单
     * @param orderId 订单ID
     */
    void confirmRefund(Long orderId);
}
