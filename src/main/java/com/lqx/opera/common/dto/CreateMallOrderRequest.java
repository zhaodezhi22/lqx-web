package com.lqx.opera.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateMallOrderRequest {
    private List<CreateMallOrderItem> items;
    private Long addressId;
    private Integer usedPoints;
}

