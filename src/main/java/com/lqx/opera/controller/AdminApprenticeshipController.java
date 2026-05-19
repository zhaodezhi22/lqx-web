package com.lqx.opera.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.dto.AdminLineageDTO;
import com.lqx.opera.entity.ApprenticeshipApply;
import com.lqx.opera.entity.ApprenticeshipRelation;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.ApprenticeshipService;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/apprenticeship")
public class AdminApprenticeshipController {
    private static final String MASTER_CHANGE_PREFIX = "[MASTER_CHANGE]";

    private final ApprenticeshipService apprenticeshipService;
    private final SysUserService sysUserService;
    private final InheritorProfileService inheritorProfileService;

    public AdminApprenticeshipController(ApprenticeshipService apprenticeshipService,
                                         SysUserService sysUserService,
                                         InheritorProfileService inheritorProfileService) {
        this.apprenticeshipService = apprenticeshipService;
        this.sysUserService = sysUserService;
        this.inheritorProfileService = inheritorProfileService;
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

    @GetMapping("/change-apply")
    @RequireRole({2, 3})
    public Result<List<MasterChangeApplyDto>> listChangeApply() {
        List<ApprenticeshipApply> records = apprenticeshipService.lambdaQuery()
                .eq(ApprenticeshipApply::getStatus, 0)
                .likeRight(ApprenticeshipApply::getApplyContent, MASTER_CHANGE_PREFIX)
                .orderByDesc(ApprenticeshipApply::getId)
                .list();

        List<MasterChangeApplyDto> result = records.stream().map(item -> {
            MasterChangeApplyDto dto = new MasterChangeApplyDto();
            dto.setId(item.getId());
            dto.setStudentId(item.getStudentId());
            dto.setTargetMasterUserId(item.getMasterId());
            dto.setReason(stripMasterChangePrefix(item.getApplyContent()));

            SysUser studentUser = sysUserService.getById(item.getStudentId());
            if (studentUser != null) {
                dto.setStudentName(studentUser.getRealName() != null && !studentUser.getRealName().isBlank()
                        ? studentUser.getRealName() : studentUser.getUsername());
            }

            InheritorProfile studentProfile = inheritorProfileService.lambdaQuery()
                    .eq(InheritorProfile::getUserId, item.getStudentId())
                    .last("LIMIT 1")
                    .one();
            if (studentProfile != null && studentProfile.getMasterId() != null) {
                InheritorProfile currentMasterProfile = inheritorProfileService.getById(studentProfile.getMasterId());
                if (currentMasterProfile != null) {
                    SysUser currentMasterUser = sysUserService.getById(currentMasterProfile.getUserId());
                    if (currentMasterUser != null) {
                        dto.setCurrentMasterName(currentMasterUser.getRealName() != null && !currentMasterUser.getRealName().isBlank()
                                ? currentMasterUser.getRealName() : currentMasterUser.getUsername());
                    }
                }
            }

            SysUser targetMasterUser = sysUserService.getById(item.getMasterId());
            if (targetMasterUser != null) {
                dto.setTargetMasterName(targetMasterUser.getRealName() != null && !targetMasterUser.getRealName().isBlank()
                        ? targetMasterUser.getRealName() : targetMasterUser.getUsername());
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @PutMapping("/change-apply/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> auditChangeApply(@PathVariable Long id, @RequestBody ChangeApplyAuditRequest request) {
        try {
            apprenticeshipService.auditMasterChangeApply(id, request != null && Boolean.TRUE.equals(request.getPass()));
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> delete(@PathVariable Long id) {
        return apprenticeshipService.deleteRelation(id) ? Result.success(true) : Result.fail("删除失败");
    }

    private String stripMasterChangePrefix(String content) {
        if (content == null) {
            return "";
        }
        return content.startsWith(MASTER_CHANGE_PREFIX) ? content.substring(MASTER_CHANGE_PREFIX.length()) : content;
    }

    public static class ChangeApplyAuditRequest {
        private Boolean pass;

        public Boolean getPass() { return pass; }
        public void setPass(Boolean pass) { this.pass = pass; }
    }

    public static class MasterChangeApplyDto {
        private Long id;
        private Long studentId;
        private String studentName;
        private String currentMasterName;
        private Long targetMasterUserId;
        private String targetMasterName;
        private String reason;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getCurrentMasterName() { return currentMasterName; }
        public void setCurrentMasterName(String currentMasterName) { this.currentMasterName = currentMasterName; }
        public Long getTargetMasterUserId() { return targetMasterUserId; }
        public void setTargetMasterUserId(Long targetMasterUserId) { this.targetMasterUserId = targetMasterUserId; }
        public String getTargetMasterName() { return targetMasterName; }
        public void setTargetMasterName(String targetMasterName) { this.targetMasterName = targetMasterName; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
