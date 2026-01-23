package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.PerformanceEvent;
import com.lqx.opera.mapper.PerformanceEventMapper;
import com.lqx.opera.service.PerformanceEventService;
import org.springframework.stereotype.Service;

@Service
public class PerformanceEventServiceImpl extends ServiceImpl<PerformanceEventMapper, PerformanceEvent> implements PerformanceEventService {
}
