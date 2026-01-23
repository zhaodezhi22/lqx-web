package com.liuqin.opera.common.dto;

import lombok.Data;

@Data
public class CreateTicketDTO {
    private Long eventId;
    private String seatId;
}
