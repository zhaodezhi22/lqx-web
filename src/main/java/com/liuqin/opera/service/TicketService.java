package com.liuqin.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liuqin.opera.common.dto.CreateTicketDTO;
import com.liuqin.opera.entity.TicketOrder;

public interface TicketService extends IService<TicketOrder> {
    boolean lockSeat(Long eventId, String seatId, Long userId);
    TicketOrder createTicketOrder(CreateTicketDTO dto, Long userId);
}
