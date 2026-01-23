package com.liuqin.opera.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateMallOrderRequest {
    private List<CreateMallOrderItem> items;
    private String address; // 简化：可选收货地址
}

