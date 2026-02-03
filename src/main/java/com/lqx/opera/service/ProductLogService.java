package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.ProductLog;

public interface ProductLogService extends IService<ProductLog> {
    void log(Long productId, String productName, Long operatorId, String action, String content);
}
