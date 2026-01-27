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
    private final com.lqx.opera.service.MallOrderService mallOrderService;
    private final com.lqx.opera.service.MallOrderItemService mallOrderItemService;

    public ProductController(ProductService productService,
                             com.lqx.opera.service.MallOrderService mallOrderService,
                             com.lqx.opera.service.MallOrderItemService mallOrderItemService) {
        this.productService = productService;
        this.mallOrderService = mallOrderService;
        this.mallOrderItemService = mallOrderItemService;
    }

    @GetMapping
    public Result<List<Product>> listAll(@RequestParam(required = false) Integer status) {
        if (status != null) {
            return Result.success(productService.list(new LambdaQueryWrapper<Product>().eq(Product::getStatus, status)));
        }
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

    @GetMapping("/my-sales-stats")
    @RequireRole(1)
    public Result<SalesStatsDto> getMySalesStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        // 1. Get my products
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, userId));
        
        SalesStatsDto stats = new SalesStatsDto();
        // Count active products (status = 1)
        long activeCount = products.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count();
        stats.setProductCount((int) activeCount);

        if (products.isEmpty()) {
            stats.setTotalSales(java.math.BigDecimal.ZERO);
            stats.setOrderCount(0);
            return Result.success(stats);
        }

        List<Long> productIds = products.stream().map(Product::getProductId).toList();

        // 2. Get Order Items for these products
        List<com.lqx.opera.entity.MallOrderItem> items = mallOrderItemService.list(
                new LambdaQueryWrapper<com.lqx.opera.entity.MallOrderItem>()
                        .in(com.lqx.opera.entity.MallOrderItem::getProductId, productIds));
        
        if (items.isEmpty()) {
            stats.setTotalSales(java.math.BigDecimal.ZERO);
            stats.setOrderCount(0);
            return Result.success(stats);
        }

        // 3. Get corresponding orders to check status
        List<Long> orderIds = items.stream().map(com.lqx.opera.entity.MallOrderItem::getOrderId).distinct().toList();
        List<com.lqx.opera.entity.MallOrder> orders = mallOrderService.listByIds(orderIds);
        
        // Filter valid orders (Paid=1, Shipped=2, RefundPending=4)
        java.util.Set<Long> validOrderIds = orders.stream()
                .filter(o -> o.getStatus() != null && (o.getStatus() == 1 || o.getStatus() == 2 || o.getStatus() == 4))
                .map(com.lqx.opera.entity.MallOrder::getId)
                .collect(java.util.stream.Collectors.toSet());

        // 4. Calculate stats
        java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
        long validOrderCount = 0;

        // Iterate items to sum price, and count orders
        // Note: validOrderCount is simply the size of validOrderIds intersected with orderIds from my items
        // But an order might have multiple items from me.
        // Requirement: "订单总数" (Order Count) usually means unique orders.
        validOrderCount = validOrderIds.size();

        for (com.lqx.opera.entity.MallOrderItem item : items) {
            if (validOrderIds.contains(item.getOrderId())) {
                java.math.BigDecimal amount = item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()));
                totalSales = totalSales.add(amount);
            }
        }

        stats.setTotalSales(totalSales);
        stats.setOrderCount((int) validOrderCount);

        return Result.success(stats);
    }

    @lombok.Data
    public static class SalesStatsDto {
        private java.math.BigDecimal totalSales;
        private Integer orderCount;
        private Integer productCount;
    }
}
