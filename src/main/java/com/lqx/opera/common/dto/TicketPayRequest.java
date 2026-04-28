package com.lqx.opera.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class TicketPayRequest {
    private List<Long> orderIds;
    private Integer usedPoints;
}
