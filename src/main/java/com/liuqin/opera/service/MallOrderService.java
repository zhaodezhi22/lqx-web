package com.liuqin.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liuqin.opera.common.dto.CreateMallOrderItem;
import com.liuqin.opera.entity.MallOrder;

import java.util.List;

public interface MallOrderService extends IService<MallOrder> {
    MallOrder createOrder(Long userId, List<CreateMallOrderItem> items);
}

