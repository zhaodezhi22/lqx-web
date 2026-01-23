package com.liuqin.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.PerformanceEvent;
import com.liuqin.opera.mapper.PerformanceEventMapper;
import com.liuqin.opera.service.PerformanceEventService;
import org.springframework.stereotype.Service;

@Service
public class PerformanceEventServiceImpl extends ServiceImpl<PerformanceEventMapper, PerformanceEvent> implements PerformanceEventService {
}
