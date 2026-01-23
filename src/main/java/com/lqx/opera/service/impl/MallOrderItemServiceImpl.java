package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.MallOrderItem;
import com.lqx.opera.mapper.MallOrderItemMapper;
import com.lqx.opera.service.MallOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class MallOrderItemServiceImpl extends ServiceImpl<MallOrderItemMapper, MallOrderItem> implements MallOrderItemService {
}

