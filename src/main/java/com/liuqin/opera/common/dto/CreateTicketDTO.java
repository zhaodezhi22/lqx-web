package com.liuqin.opera.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateTicketDTO {
    private Long eventId;
    private String seatInfo; // changed from seatId to match requirement
    private BigDecimal price;
}
