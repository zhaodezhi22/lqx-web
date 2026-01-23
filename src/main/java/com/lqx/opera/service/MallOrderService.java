package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.dto.CartItemDto;
import com.lqx.opera.entity.MallOrder;

import java.util.List;

public interface MallOrderService extends IService<MallOrder> {
    void createOrder(Long userId, String address, List<CartItemDto> items);
}
