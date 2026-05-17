package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.AdminOperationLog;
import com.lqx.opera.mapper.AdminOperationLogMapper;
import com.lqx.opera.service.AdminOperationLogService;
import org.springframework.stereotype.Service;

@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog> implements AdminOperationLogService {
}
