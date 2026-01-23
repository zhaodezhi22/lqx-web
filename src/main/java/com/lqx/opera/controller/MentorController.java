package com.lqx.opera.controller;

import com.liuqin.opera.common.Result;
import com.lqx.opera.dto.AuditApplyRequest;
import com.lqx.opera.dto.SubmitApplyRequest;
import com.lqx.opera.service.ApprenticeshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MentorController {

    @Autowired
    private ApprenticeshipService apprenticeshipService;

    @PostMapping("/api/mentor/apply")
    public Result submitApply(@RequestBody SubmitApplyRequest request) {
        apprenticeshipService.submitApply(request.getStudentId(), request.getMasterId(), request.getContent());
        return Result.success("申请提交成功");
    }

    @PostMapping("/api/mentor/audit")
    public Result auditApply(@RequestBody AuditApplyRequest request) {
        apprenticeshipService.auditApply(request.getApplyId(), request.getAuditStatus(), request.getCurrentUserId());
        return Result.success("审核完成");
    }
}
