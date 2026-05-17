package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.AdminOperationLog;
import com.lqx.opera.service.AdminOperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@RequireRole({2, 3})
public class AdminOperationLogController {

    private final AdminOperationLogService adminOperationLogService;

    public AdminOperationLogController(AdminOperationLogService adminOperationLogService) {
        this.adminOperationLogService = adminOperationLogService;
    }

    @GetMapping
    public Result<Page<AdminOperationLog>> list(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "20") Integer size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String method,
                                                @RequestParam(required = false) String path) {
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(q -> q.like(AdminOperationLog::getOperatorName, keyword.trim())
                    .or()
                    .like(AdminOperationLog::getActionName, keyword.trim()));
        }
        if (method != null && !method.trim().isEmpty()) {
            wrapper.eq(AdminOperationLog::getRequestMethod, method.trim().toUpperCase());
        }
        if (path != null && !path.trim().isEmpty()) {
            wrapper.like(AdminOperationLog::getRequestPath, path.trim());
        }
        wrapper.orderByDesc(AdminOperationLog::getCreatedTime);
        return Result.success(adminOperationLogService.page(new Page<>(page, size), wrapper));
    }
}
