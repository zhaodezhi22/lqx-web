package com.liuqin.opera.controller;

import com.liuqin.opera.common.Result;
import com.liuqin.opera.common.annotation.RequireRole;
import com.liuqin.opera.common.dto.AuditApplyRequest;
import com.liuqin.opera.common.dto.SubmitApplyRequest;
import com.liuqin.opera.service.ApprenticeshipService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor")
public class MentorController {

    private final ApprenticeshipService apprenticeshipService;

    public MentorController(ApprenticeshipService apprenticeshipService) {
        this.apprenticeshipService = apprenticeshipService;
    }

    /**
     * 提交拜师申请
     */
    @PostMapping("/apply")
    public Result<Boolean> submitApply(@RequestBody SubmitApplyRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        apprenticeshipService.submitApply(userId, req.getMasterId(), req.getContent());
        return Result.success(true);
    }

    /**
     * 师父审核申请
     */
    @PostMapping("/audit")
    @RequireRole({1, 2, 3}) // 传承人或管理员
    public Result<Boolean> auditApply(@RequestBody AuditApplyRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        apprenticeshipService.auditApply(req.getApplyId(), userId, req.getPass());
        return Result.success(true);
    }
}
