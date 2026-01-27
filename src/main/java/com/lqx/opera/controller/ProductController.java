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

    // Admin Create
    @PostMapping
    @RequireRole({2, 3})
    public Result<Boolean> create(@RequestBody Product product) {
        boolean saved = productService.save(product);
        return saved ? Result.success(true) : Result.fail("保存失败");
    }

    // Admin Update
    @PutMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Product product) {
        product.setProductId(id);
        boolean updated = productService.updateById(product);
        return updated ? Result.success(true) : Result.fail("更新失败");
    }

    // Admin Delete
    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = productService.removeById(id);
        return removed ? Result.success(true) : Result.fail("删除失败");
    }

    // Admin Audit
    @PutMapping("/audit")
    @RequireRole({2, 3})
    public Result<Boolean> audit(@RequestBody AuditRequest req) {
        Product product = productService.getById(req.getId());
        if (product == null) return Result.fail("商品不存在");
        
        product.setStatus(req.getStatus()); // 1-On Shelf, 2-Off/Reject
        return productService.updateById(product) ? Result.success(true) : Result.fail("审核失败");
    }

    @lombok.Data
    public static class AuditRequest {
        private Long id;
        private Integer status;
    }

    // Inheritor Create
    @PostMapping("/create")
    @RequireRole(1)
    public Result<Boolean> createMyProduct(@RequestBody Product product, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        product.setSellerId(userId);
        product.setStatus(0); // 0-Pending Audit
        product.setCreatedTime(LocalDateTime.now());
        return productService.save(product) ? Result.success(true) : Result.fail("发布失败");
    }

    // Inheritor Update
    @PutMapping("/my-product/{id}")
    @RequireRole(1)
    public Result<Boolean> updateMyProduct(@PathVariable Long id, @RequestBody Product product, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Product old = productService.getById(id);
        if (old == null) return Result.fail(404, "商品不存在");
        if (!old.getSellerId().equals(userId)) return Result.fail(403, "无权修改");
        
        product.setProductId(id);
        product.setSellerId(userId); // Ensure ownership
        product.setStatus(0); // Re-submit for audit
        boolean updated = productService.updateById(product);
        return updated ? Result.success(true) : Result.fail("更新失败");
    }

    // Inheritor Delete
    @DeleteMapping("/my-product/{id}")
    @RequireRole(1)
    public Result<Boolean> deleteMyProduct(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Product old = productService.getById(id);
        if (old == null) return Result.fail(404, "商品不存在");
        if (!old.getSellerId().equals(userId)) return Result.fail(403, "无权删除");
        
        boolean removed = productService.removeById(id);
        return removed ? Result.success(true) : Result.fail("删除失败");
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
