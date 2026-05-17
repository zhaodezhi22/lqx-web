package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.HeritageResource;
import com.lqx.opera.entity.Product;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.HeritageResourceService;
import com.lqx.opera.service.ProductService;
import com.lqx.opera.service.SysUserService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit-center")
@RequireRole({2, 3})
public class AdminAuditCenterController {

    private final HeritageResourceService heritageResourceService;
    private final ProductService productService;
    private final SysUserService sysUserService;

    public AdminAuditCenterController(HeritageResourceService heritageResourceService,
                                      ProductService productService,
                                      SysUserService sysUserService) {
        this.heritageResourceService = heritageResourceService;
        this.productService = productService;
        this.sysUserService = sysUserService;
    }

    @GetMapping("/items")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false, defaultValue = "ALL") String type,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String applicantName) {
        String normalizedType = type == null ? "ALL" : type.trim().toUpperCase();
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedApplicantName = applicantName == null ? "" : applicantName.trim();

        List<AuditCenterItem> merged = new ArrayList<>();

        if ("ALL".equals(normalizedType) || "RESOURCE".equals(normalizedType)) {
            merged.addAll(buildResourceItems(status, normalizedKeyword, normalizedApplicantName));
        }
        if ("ALL".equals(normalizedType) || "PRODUCT".equals(normalizedType)) {
            merged.addAll(buildProductItems(status, normalizedKeyword, normalizedApplicantName));
        }

        merged.sort(Comparator
                .comparing((AuditCenterItem item) -> item.getStatus() != null && item.getStatus() == 0 ? 0 : 1)
                .thenComparing(AuditCenterItem::getCreatedTime, Comparator.nullsLast(Comparator.reverseOrder())));

        int current = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int fromIndex = Math.min((current - 1) * pageSize, merged.size());
        int toIndex = Math.min(fromIndex + pageSize, merged.size());

        Map<String, Object> data = new HashMap<>();
        data.put("records", merged.subList(fromIndex, toIndex));
        data.put("total", merged.size());
        data.put("page", current);
        data.put("size", pageSize);
        return Result.success(data);
    }

    @PutMapping("/{type}/{id}")
    public Result<Boolean> audit(@PathVariable String type,
                                 @PathVariable Long id,
                                 @RequestBody AuditRequest request) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase();
        if ("RESOURCE".equals(normalizedType)) {
            HeritageResource resource = heritageResourceService.getById(id);
            if (resource == null) {
                return Result.fail(404, "资源不存在");
            }
            resource.setStatus(request.getStatus());
            resource.setAuditRemark(request.getRemark());
            return heritageResourceService.updateById(resource) ? Result.success(true) : Result.fail("资源审核失败");
        }

        if ("PRODUCT".equals(normalizedType)) {
            Product product = productService.getById(id);
            if (product == null) {
                return Result.fail(404, "商品不存在");
            }
            product.setStatus(request.getStatus());
            product.setAuditRemark(request.getRemark());
            return productService.updateById(product) ? Result.success(true) : Result.fail("商品审核失败");
        }

        return Result.fail(400, "不支持的审核类型");
    }

    @PostMapping("/batch")
    public Result<Boolean> batchAudit(@RequestBody BatchAuditRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return Result.fail(400, "请选择需要审核的数据");
        }

        for (BatchAuditItem item : request.getItems()) {
            if (item.getRecordId() == null || item.getBizType() == null || item.getBizType().isBlank()) {
                return Result.fail(400, "存在无效的审核项");
            }
            String normalizedType = item.getBizType().trim().toUpperCase();
            if ("RESOURCE".equals(normalizedType)) {
                HeritageResource resource = heritageResourceService.getById(item.getRecordId());
                if (resource != null) {
                    resource.setStatus(request.getStatus());
                    resource.setAuditRemark(request.getRemark());
                    heritageResourceService.updateById(resource);
                }
            } else if ("PRODUCT".equals(normalizedType)) {
                Product product = productService.getById(item.getRecordId());
                if (product != null) {
                    product.setStatus(request.getStatus());
                    product.setAuditRemark(request.getRemark());
                    productService.updateById(product);
                }
            }
        }

        return Result.success(true);
    }

    @GetMapping("/{type}/{id}")
    public Result<AuditCenterItem> detail(@PathVariable String type, @PathVariable Long id) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase();
        if ("RESOURCE".equals(normalizedType)) {
            HeritageResource item = heritageResourceService.getById(id);
            if (item == null) {
                return Result.fail(404, "资源不存在");
            }
            SysUser user = item.getUploaderId() == null ? null : sysUserService.getById(item.getUploaderId());
            AuditCenterItem dto = new AuditCenterItem();
            dto.setBizType("RESOURCE");
            dto.setRecordId(item.getResourceId());
            dto.setTitle(item.getTitle());
            dto.setSubTitle(item.getCategory());
            dto.setStatus(item.getStatus());
            dto.setCreatedTime(item.getCreatedTime());
            dto.setApplicantId(item.getUploaderId());
            dto.setApplicantName(resolveUserName(user));
            dto.setMeta(item.getType() == null ? "" : "资源类型: " + getResourceTypeLabel(item.getType()));
            dto.setAuditRemark(item.getAuditRemark());
            dto.setDescription(item.getDescription());
            dto.setCoverImage(item.getCoverImg());
            dto.setFileUrl(item.getFileUrl());
            return Result.success(dto);
        }
        if ("PRODUCT".equals(normalizedType)) {
            Product item = productService.getById(id);
            if (item == null) {
                return Result.fail(404, "商品不存在");
            }
            SysUser user = item.getSellerId() == null ? null : sysUserService.getById(item.getSellerId());
            AuditCenterItem dto = new AuditCenterItem();
            dto.setBizType("PRODUCT");
            dto.setRecordId(item.getProductId());
            dto.setTitle(item.getName());
            dto.setSubTitle(item.getSubTitle());
            dto.setStatus(item.getStatus());
            dto.setCreatedTime(item.getCreatedTime());
            dto.setApplicantId(item.getSellerId());
            dto.setApplicantName(resolveUserName(user));
            dto.setMeta(item.getPrice() == null ? "" : "商品价格: ¥" + item.getPrice());
            dto.setAuditRemark(item.getAuditRemark());
            dto.setDescription(item.getDetail());
            dto.setCoverImage(item.getMainImage());
            return Result.success(dto);
        }
        return Result.fail(400, "不支持的审核类型");
    }

    private List<AuditCenterItem> buildResourceItems(Integer status, String keyword, String applicantName) {
        LambdaQueryWrapper<HeritageResource> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(HeritageResource::getStatus, status);
        }
        if (!keyword.isEmpty()) {
            wrapper.like(HeritageResource::getTitle, keyword);
        }
        List<HeritageResource> resources = heritageResourceService.list(wrapper);
        Map<Long, SysUser> userMap = buildUserMap(resources.stream().map(HeritageResource::getUploaderId).collect(Collectors.toSet()));

        return resources.stream().map(item -> {
            AuditCenterItem dto = new AuditCenterItem();
            dto.setBizType("RESOURCE");
            dto.setRecordId(item.getResourceId());
            dto.setTitle(item.getTitle());
            dto.setSubTitle(item.getCategory());
            dto.setStatus(item.getStatus());
            dto.setCreatedTime(item.getCreatedTime());
            dto.setApplicantId(item.getUploaderId());
            dto.setApplicantName(resolveUserName(userMap.get(item.getUploaderId())));
            dto.setMeta(item.getType() == null ? "" : "资源类型: " + getResourceTypeLabel(item.getType()));
            dto.setAuditRemark(item.getAuditRemark());
            return dto;
        }).filter(item -> applicantName.isEmpty() || item.getApplicantName().contains(applicantName))
                .collect(Collectors.toList());
    }

    private List<AuditCenterItem> buildProductItems(Integer status, String keyword, String applicantName) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (!keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        List<Product> products = productService.list(wrapper);
        Map<Long, SysUser> userMap = buildUserMap(products.stream().map(Product::getSellerId).collect(Collectors.toSet()));

        return products.stream().map(item -> {
            AuditCenterItem dto = new AuditCenterItem();
            dto.setBizType("PRODUCT");
            dto.setRecordId(item.getProductId());
            dto.setTitle(item.getName());
            dto.setSubTitle(item.getSubTitle());
            dto.setStatus(item.getStatus());
            dto.setCreatedTime(item.getCreatedTime());
            dto.setApplicantId(item.getSellerId());
            dto.setApplicantName(resolveUserName(userMap.get(item.getSellerId())));
            dto.setMeta(item.getPrice() == null ? "" : "商品价格: ¥" + item.getPrice());
            dto.setAuditRemark(item.getAuditRemark());
            return dto;
        }).filter(item -> applicantName.isEmpty() || item.getApplicantName().contains(applicantName))
                .collect(Collectors.toList());
    }

    private Map<Long, SysUser> buildUserMap(Set<Long> userIds) {
        Set<Long> filtered = userIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (filtered.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserService.listByIds(filtered).stream()
                .collect(Collectors.toMap(SysUser::getUserId, user -> user));
    }

    private String resolveUserName(SysUser user) {
        if (user == null) {
            return "未知用户";
        }
        return user.getRealName() != null && !user.getRealName().isBlank() ? user.getRealName() : user.getUsername();
    }

    private String getResourceTypeLabel(Integer type) {
        return switch (type) {
            case 1 -> "视频";
            case 2 -> "音频";
            case 3 -> "图文";
            case 4 -> "剧本PDF";
            default -> "未知";
        };
    }

    @Data
    public static class AuditRequest {
        private Integer status;
        private String remark;
    }

    @Data
    public static class BatchAuditRequest {
        private Integer status;
        private String remark;
        private List<BatchAuditItem> items;
    }

    @Data
    public static class BatchAuditItem {
        private String bizType;
        private Long recordId;
    }

    @Data
    public static class AuditCenterItem {
        private String bizType;
        private Long recordId;
        private String title;
        private String subTitle;
        private Integer status;
        private LocalDateTime createdTime;
        private Long applicantId;
        private String applicantName;
        private String meta;
        private String auditRemark;
        private String description;
        private String coverImage;
        private String fileUrl;
    }
}
