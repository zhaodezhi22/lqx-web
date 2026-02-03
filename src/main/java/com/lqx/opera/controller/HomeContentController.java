package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.HomeContent;
import com.lqx.opera.service.HomeContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HomeContentController {

    @Autowired
    private HomeContentService homeContentService;

    // Public endpoint for Home Page
    @GetMapping("/home/content")
    public Result<List<HomeContent>> getActiveContent(@RequestParam String type) {
        return Result.success(homeContentService.getActiveContentByType(type));
    }

    @GetMapping("/home/content/{id}")
    public Result<HomeContent> getContentById(@PathVariable Long id) {
        HomeContent content = homeContentService.getById(id);
        if (content == null || content.getStatus() == 0) {
            return Result.fail("内容不存在或已下架 (ID: " + id + ")");
        }
        return Result.success(content);
    }

    // Admin/Auditor endpoints
    @GetMapping("/admin/home/content/list")
    @RequireRole({2, 3})
    public Result<List<HomeContent>> getAllContent(@RequestParam(required = false) String type) {
        LambdaQueryWrapper<HomeContent> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(HomeContent::getType, type);
        }
        wrapper.orderByAsc(HomeContent::getType)
               .orderByAsc(HomeContent::getSortOrder)
               .orderByDesc(HomeContent::getCreateTime);
        return Result.success(homeContentService.list(wrapper));
    }

    @PostMapping("/admin/home/content")
    @RequireRole({2, 3})
    public Result<HomeContent> createContent(@RequestBody HomeContent content) {
        content.setCreateTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        if (content.getStatus() == null) {
            content.setStatus(1);
        }
        if (content.getSortOrder() == null) {
            content.setSortOrder(0);
        }
        homeContentService.save(content);
        return Result.success(content);
    }

    @PutMapping("/admin/home/content")
    @RequireRole({2, 3})
    public Result<HomeContent> updateContent(@RequestBody HomeContent content) {
        content.setUpdateTime(LocalDateTime.now());
        homeContentService.updateById(content);
        return Result.success(content);
    }

    @DeleteMapping("/admin/home/content/{id}")
    @RequireRole({2, 3})
    public Result<String> deleteContent(@PathVariable Long id) {
        homeContentService.removeById(id);
        return Result.success("Deleted successfully");
    }
}
