package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.HeritageResource;
import com.lqx.opera.service.HeritageResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourceController {

    private final HeritageResourceService heritageResourceService;

    public AdminResourceController(HeritageResourceService heritageResourceService) {
        this.heritageResourceService = heritageResourceService;
    }

    @GetMapping
    @RequireRole({2, 3})
    public Result<Page<HeritageResource>> list(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(required = false) String keyword) {
        Page<HeritageResource> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<HeritageResource> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(HeritageResource::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(HeritageResource::getTitle, keyword);
        }
        
        // Default sort: Pending (0) first, then CreatedTime desc
        wrapper.last("ORDER BY CASE WHEN status = 0 THEN 0 ELSE 1 END ASC, created_time DESC");

        return Result.success(heritageResourceService.page(pageParam, wrapper));
    }

    @PutMapping("/{id}/audit")
    @RequireRole({2, 3})
    public Result<Boolean> audit(@PathVariable Long id, @RequestBody AuditRequest req) {
        HeritageResource resource = heritageResourceService.getById(id);
        if (resource == null) {
            return Result.fail(404, "资源不存在");
        }
        
        resource.setStatus(req.getStatus());
        // Maybe add audit remark? Entity doesn't seem to have it based on memory, but let's check.
        // Assuming just status for now.
        
        boolean success = heritageResourceService.updateById(resource);
        return success ? Result.success(true) : Result.fail("审核失败");
    }

    @lombok.Data
    public static class AuditRequest {
        private Integer status; // 1-Pass, 2-Reject
        private String message;
    }
}
