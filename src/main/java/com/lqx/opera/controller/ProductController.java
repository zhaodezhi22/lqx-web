package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.Product;
import com.lqx.opera.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Result<List<Product>> listAll() {
        return Result.success(productService.list());
    }

    @GetMapping("/hot")
    public Result<List<Product>> hot(@RequestParam(required = false, defaultValue = "5") Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1).orderByDesc(Product::getCreatedTime);
        Page<Product> page = new Page<>(1, limit == null ? 5 : limit);
        return Result.success(productService.page(page, wrapper).getRecords());
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.fail(404, "商品不存在");
        }
        return Result.success(product);
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody Product product) {
        boolean saved = productService.save(product);
        return saved ? Result.success(true) : Result.fail("保存失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Product product) {
        product.setProductId(id);
        boolean updated = productService.updateById(product);
        return updated ? Result.success(true) : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = productService.removeById(id);
        return removed ? Result.success(true) : Result.fail("删除失败");
    }

    @PostMapping("/create")
    @RequireRole(1)
    public Result<Boolean> createMyProduct(@RequestBody Product product, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        product.setSellerId(userId);
        product.setStatus(1);
        product.setCreatedTime(LocalDateTime.now());
        return productService.save(product) ? Result.success(true) : Result.fail("发布失败");
    }

    @GetMapping("/my-products")
    @RequireRole(1)
    public Result<List<Product>> myProducts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        return Result.success(productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, userId)
                .orderByDesc(Product::getCreatedTime)));
    }
}

