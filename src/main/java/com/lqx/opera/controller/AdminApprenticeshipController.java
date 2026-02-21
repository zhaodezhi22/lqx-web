package com.lqx.opera.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.AdminLineageDTO;
import com.lqx.opera.entity.ApprenticeshipRelation;
import com.lqx.opera.service.ApprenticeshipService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/apprenticeship")
public class AdminApprenticeshipController {

    private final ApprenticeshipService apprenticeshipService;

    public AdminApprenticeshipController(ApprenticeshipService apprenticeshipService) {
        this.apprenticeshipService = apprenticeshipService;
    }

    /**
     * 获取师承关系列表
     */
    @GetMapping
    @RequireRole({2, 3})
    public Result<Page<AdminLineageDTO>> list(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Page<ApprenticeshipRelation> pageParam = new Page<>(page, size);
        return Result.success(apprenticeshipService.getLineagePage(pageParam));
    }
    
    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        return apprenticeshipService.deleteRelation(id) ? Result.success(true) : Result.fail("删除失败");
    }
}
