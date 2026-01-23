package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.CreateTicketDTO;
import com.lqx.opera.entity.TicketOrder;

public interface TicketService extends IService<TicketOrder> {
    boolean lockSeat(Long eventId, String seatId, Long userId);
    TicketOrder createTicketOrder(CreateTicketDTO dto, Long userId);
    void cancelUnpaidOrders();
}
