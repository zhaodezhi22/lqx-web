package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.HeritageResource;
import com.lqx.opera.entity.ResourceCategory;
import com.lqx.opera.service.HeritageResourceService;
import com.lqx.opera.service.ResourceCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource-categories")
public class ResourceCategoryController {

    private final ResourceCategoryService resourceCategoryService;
    private final HeritageResourceService heritageResourceService;

    public ResourceCategoryController(ResourceCategoryService resourceCategoryService,
                                      HeritageResourceService heritageResourceService) {
        this.resourceCategoryService = resourceCategoryService;
        this.heritageResourceService = heritageResourceService;
    }

    @GetMapping
    public Result<List<ResourceCategory>> list() {
        List<ResourceCategory> list = resourceCategoryService.list(
                new LambdaQueryWrapper<ResourceCategory>()
                        .orderByAsc(ResourceCategory::getSortOrder)
                        .orderByAsc(ResourceCategory::getCategoryId)
        );
        return Result.success(list);
    }

    @PostMapping
    @RequireRole({2, 3})
    public Result<Boolean> add(@RequestBody CategoryRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            return Result.fail(400, "分类名称不能为空");
        }
        long exists = resourceCategoryService.count(
                new LambdaQueryWrapper<ResourceCategory>()
                        .eq(ResourceCategory::getName, name)
        );
        if (exists > 0) {
            return Result.fail(400, "分类已存在");
        }

        ResourceCategory category = new ResourceCategory();
        category.setName(name);
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        return resourceCategoryService.save(category)
                ? Result.success(true)
                : Result.fail("新增分类失败");
    }

    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        ResourceCategory category = resourceCategoryService.getById(id);
        if (category == null) {
            return Result.fail(404, "分类不存在");
        }

        long usedCount = heritageResourceService.count(
                new LambdaQueryWrapper<HeritageResource>()
                        .eq(HeritageResource::getCategory, category.getName())
        );
        if (usedCount > 0) {
            return Result.fail(400, "该分类已被资源使用，无法删除");
        }

        return resourceCategoryService.removeById(id)
                ? Result.success(true)
                : Result.fail("删除分类失败");
    }

    @lombok.Data
    public static class CategoryRequest {
        private String name;
        private Integer sortOrder;
    }
}
