package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.CreateMallOrderItem;
import com.lqx.opera.entity.MallOrder;

import java.util.List;

public interface MallOrderService extends IService<MallOrder> {
    MallOrder createOrder(Long userId, List<CreateMallOrderItem> items);
}

