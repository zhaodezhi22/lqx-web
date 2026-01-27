package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.Result;
import com.lqx.opera.entity.HeritageResource;
import com.lqx.opera.service.HeritageResourceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class HeritageResourceController {

    private final HeritageResourceService heritageResourceService;

    public HeritageResourceController(HeritageResourceService heritageResourceService) {
        this.heritageResourceService = heritageResourceService;
    }

    @GetMapping
    public Result<List<HeritageResource>> list(@RequestParam(required = false) String category) {
        LambdaQueryWrapper<HeritageResource> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(HeritageResource::getCategory, category);
        }
        // Show all resources (including pending) as per user requirement to see data
        // wrapper.eq(HeritageResource::getStatus, 1);
        wrapper.orderByDesc(HeritageResource::getCreatedTime);
        return Result.success(heritageResourceService.list(wrapper));
    }

    @GetMapping("/featured")
    public Result<List<HeritageResource>> featured(@RequestParam(required = false, defaultValue = "6") Integer limit) {
        LambdaQueryWrapper<HeritageResource> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(HeritageResource::getStatus, 1);
        wrapper.orderByDesc(HeritageResource::getCreatedTime);
        Page<HeritageResource> page = new Page<>(1, limit == null ? 6 : limit);
        return Result.success(heritageResourceService.page(page, wrapper).getRecords());
    }

    @GetMapping("/{id}")
    public Result<HeritageResource> detail(@PathVariable Long id) {
        HeritageResource resource = heritageResourceService.getById(id);
        if (resource == null) {
            return Result.fail(404, "资源不存在");
        }
        // Allow viewing all statuses
        return Result.success(resource);
    }

    /**
     * 传承人上传资源
     */
    @PostMapping
    @RequireRole(1)
    public Result<Boolean> add(@RequestBody HeritageResource resource, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        resource.setUploaderId(userId);
        resource.setStatus(0); // Pending
        boolean ok = heritageResourceService.save(resource);
        return ok ? Result.success(true) : Result.fail("保存失败");
    }

    /**
     * 传承人获取我的资源列表
     */
    @GetMapping("/my-resources")
    @RequireRole(1)
    public Result<List<HeritageResource>> getMyResources(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        List<HeritageResource> list = heritageResourceService.list(new LambdaQueryWrapper<HeritageResource>()
                .eq(HeritageResource::getUploaderId, userId)
                .orderByDesc(HeritageResource::getCreatedTime));
        return Result.success(list);
    }

    /**
     * 传承人更新我的资源
     */
    @PutMapping("/my-resource/{id}")
    @RequireRole(1)
    public Result<Boolean> updateMyResource(@PathVariable Long id, @RequestBody HeritageResource resource, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        HeritageResource old = heritageResourceService.getById(id);
        if (old == null) return Result.fail(404, "资源不存在");
        if (!old.getUploaderId().equals(userId)) return Result.fail(403, "无权修改");

        resource.setResourceId(id);
        resource.setUploaderId(userId);
        resource.setStatus(0); // Reset to pending audit
        
        boolean ok = heritageResourceService.updateById(resource);
        return ok ? Result.success(true) : Result.fail("更新失败");
    }

    /**
     * 传承人删除我的资源
     */
    @DeleteMapping("/my-resource/{id}")
    @RequireRole(1)
    public Result<Boolean> deleteMyResource(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        HeritageResource old = heritageResourceService.getById(id);
        if (old == null) return Result.fail(404, "资源不存在");
        if (!old.getUploaderId().equals(userId)) return Result.fail(403, "无权删除");

        boolean ok = heritageResourceService.removeById(id);
        return ok ? Result.success(true) : Result.fail("删除失败");
    }

    // Admin Update
    @PutMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> update(@PathVariable Long id, @RequestBody HeritageResource resource) {
        resource.setResourceId(id);
        boolean ok = heritageResourceService.updateById(resource);
        return ok ? Result.success(true) : Result.fail("更新失败");
    }

    // Admin Delete
    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = heritageResourceService.removeById(id);
        return ok ? Result.success(true) : Result.fail("删除失败");
    }
}
