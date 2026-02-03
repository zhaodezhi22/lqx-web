package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.ProductDetailDTO;
import com.lqx.opera.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mall/product")
public class MallProductController {

    private final ProductService productService;

    public MallProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/detail/{id}")
    public Result<ProductDetailDTO> getDetail(@PathVariable Long id) {
        ProductDetailDTO detail = productService.getProductDetail(id);
        if (detail == null) {
            return Result.fail(404, "商品不存在");
        }
        return Result.success(detail);
    }
}
