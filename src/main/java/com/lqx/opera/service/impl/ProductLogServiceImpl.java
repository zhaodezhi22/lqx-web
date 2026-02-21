package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.ProductLog;
import com.lqx.opera.mapper.ProductLogMapper;
import com.lqx.opera.service.ProductLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductLogServiceImpl extends ServiceImpl<ProductLogMapper, ProductLog> implements ProductLogService {

    @Override
    public void log(Long productId, String productName, Long operatorId, String action, String content) {
        ProductLog log = new ProductLog();
        log.setProductId(productId);
        log.setProductName(productName);
        log.setOperatorId(operatorId);
        log.setActionType(action);
        log.setContent(content);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }
}
