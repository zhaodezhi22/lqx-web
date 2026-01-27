package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.CreateTicketDTO;
import com.lqx.opera.common.dto.TicketOrderDetailDto;
import com.lqx.opera.entity.TicketOrder;

import java.util.List;

public interface TicketService extends IService<TicketOrder> {
    boolean lockSeat(Long eventId, String seatId, Long userId);
    TicketOrder createTicketOrder(CreateTicketDTO dto, Long userId);
    
    void cancelUnpaidOrders();

    /**
     * 申请退票
     */
    boolean applyRefund(Long orderId, Long userId);

    /**
     * 审核退票
     */
    boolean auditRefund(Long orderId, boolean pass);

    /**
     * 获取所有票务详情（管理员用）
     */
    List<TicketOrderDetailDto> getAllTicketDetails(Integer status);

    /**
     * 获取用户票务详情列表
     */
    List<TicketOrderDetailDto> getUserTicketDetails(Long userId);

    /**
     * 退票
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean refundTicket(Long userId, Long orderId);

    /**
     * 验票核销
     * @param orderNo 订单编号/序列号
     * @return 是否成功
     */
    boolean checkIn(String orderNo);
}
