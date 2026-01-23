package com.liuqin.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.MallOrderItem;
import com.liuqin.opera.mapper.MallOrderItemMapper;
import com.liuqin.opera.service.MallOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class MallOrderItemServiceImpl extends ServiceImpl<MallOrderItemMapper, MallOrderItem> implements MallOrderItemService {
}

