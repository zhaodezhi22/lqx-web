package com.liuqin.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuqin.opera.common.annotation.RequireRole;
import com.liuqin.opera.common.Result;
import com.liuqin.opera.entity.HeritageResource;
import com.liuqin.opera.service.HeritageResourceService;
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
        wrapper.orderByDesc(HeritageResource::getCreatedTime);
        return Result.success(heritageResourceService.list(wrapper));
    }

    @GetMapping("/featured")
    public Result<List<HeritageResource>> featured(@RequestParam(required = false, defaultValue = "6") Integer limit) {
        LambdaQueryWrapper<HeritageResource> wrapper = new LambdaQueryWrapper<>();
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
        return Result.success(resource);
    }

    /**
     * 传承人上传资源
     */
    @PostMapping
    @RequireRole(1)
    public Result<Boolean> create(@RequestBody HeritageResource resource, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        resource.setUploaderId(userId);
        boolean ok = heritageResourceService.save(resource);
        return ok ? Result.success(true) : Result.fail("保存失败");
    }

    @PutMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> update(@PathVariable Long id, @RequestBody HeritageResource resource) {
        resource.setResourceId(id);
        boolean ok = heritageResourceService.updateById(resource);
        return ok ? Result.success(true) : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = heritageResourceService.removeById(id);
        return ok ? Result.success(true) : Result.fail("删除失败");
    }
}

