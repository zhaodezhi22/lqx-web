package com.lqx.opera.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long userId;
    private String address;
    private List<CartItemDto> items;
}
