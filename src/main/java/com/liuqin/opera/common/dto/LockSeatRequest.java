package com.liuqin.opera.common.dto;

import lombok.Data;

@Data
public class LockSeatRequest {
    private Long eventId;
    private String seatId;
}
