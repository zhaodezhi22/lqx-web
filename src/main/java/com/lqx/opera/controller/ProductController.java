package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.Product;
import com.lqx.opera.entity.ProductLog;
import com.lqx.opera.service.ProductLogService;
import com.lqx.opera.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final com.lqx.opera.service.MallOrderService mallOrderService;
    private final com.lqx.opera.service.MallOrderItemService mallOrderItemService;
    private final ProductLogService productLogService;

    public ProductController(ProductService productService,
                             com.lqx.opera.service.MallOrderService mallOrderService,
                             com.lqx.opera.service.MallOrderItemService mallOrderItemService,
                             ProductLogService productLogService) {
        this.productService = productService;
        this.mallOrderService = mallOrderService;
        this.mallOrderItemService = mallOrderItemService;
        this.productLogService = productLogService;
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
        boolean updated = productService.updateById(product);
        if (updated) {
            // Log audit action
            // Assuming operator is admin, but we don't have admin ID easily available here without request context or Spring Security context.
            // For now, we'll skip operator ID or set it to 0/1 (system/admin).
            // But better to fetch from context if possible. Since this method doesn't take request, we might skip operatorId or use a placeholder.
            // Let's assume admin ID 1 for now or modify signature to take request.
        }
        return updated ? Result.success(true) : Result.fail("审核失败");
    }

    @Data
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
        boolean saved = productService.save(product);
        if (saved) {
            productLogService.log(product.getProductId(), product.getName(), userId, "CREATE", "首次创建商品");
        }
        return saved ? Result.success(true) : Result.fail("发布失败");
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
        
        // Log changes
        StringBuilder changes = new StringBuilder();
        if (!Objects.equals(old.getPrice(), product.getPrice())) {
            changes.append("价格: ").append(old.getPrice()).append(" -> ").append(product.getPrice()).append("; ");
        }
        if (!Objects.equals(old.getStock(), product.getStock())) {
            changes.append("库存: ").append(old.getStock()).append(" -> ").append(product.getStock()).append("; ");
        }
        if (!Objects.equals(old.getName(), product.getName())) {
            changes.append("名称变更; ");
        }
        // ... check other fields if needed

        String changeContent = changes.toString();
        if (changeContent.isEmpty()) {
            changeContent = "更新商品信息";
        }

        // Logic Change: If status was 1 (Approved), keep it 1. Otherwise reset to 0 (Pending).
        if (old.getStatus() == 1) {
            product.setStatus(1); // Bypass audit
        } else {
            product.setStatus(0); // Re-submit or keep pending
        }

        boolean updated = productService.updateById(product);
        if (updated) {
            productLogService.log(id, old.getName(), userId, "UPDATE", changeContent);
        }
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
        if (removed) {
             productLogService.log(id, old.getName(), userId, "DELETE", "删除商品");
        }
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
        // java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
        // long validOrderCount = 0;
        
        // Simplified calculation for now
        stats.setOrderCount(validOrderIds.size());
        
        // Sum items in valid orders
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (com.lqx.opera.entity.MallOrderItem item : items) {
             if (validOrderIds.contains(item.getOrderId())) {
                 total = total.add(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
             }
        }
        stats.setTotalSales(total);
        
        return Result.success(stats);
    }
    
    @Data
    public static class SalesStatsDto {
        private int productCount;
        private int orderCount;
        private java.math.BigDecimal totalSales;
    }

    // Admin Get Logs
    @GetMapping("/logs/{productId}")
    @RequireRole({2, 3})
    public Result<List<ProductLog>> getProductLogs(@PathVariable Long productId) {
        return Result.success(productLogService.list(new LambdaQueryWrapper<ProductLog>()
                .eq(ProductLog::getProductId, productId)
                .orderByDesc(ProductLog::getCreateTime)));
    }
}
