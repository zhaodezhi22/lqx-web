package com.lqx.opera.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long userId;
    private String address;
    private List<CartItemDto> items;
}
