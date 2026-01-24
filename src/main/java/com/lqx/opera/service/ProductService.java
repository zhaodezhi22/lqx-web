package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.ProductDetailDTO;
import com.lqx.opera.entity.Product;

public interface ProductService extends IService<Product> {
    ProductDetailDTO getProductDetail(Long productId);
}

