package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.ApprenticeshipApply;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.ApprenticeshipService;
import com.lqx.opera.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/master/apprentice")
public class ApprenticeshipController {

    private final ApprenticeshipService apprenticeshipService;
    private final SysUserService sysUserService;

    public ApprenticeshipController(ApprenticeshipService apprenticeshipService, SysUserService sysUserService) {
        this.apprenticeshipService = apprenticeshipService;
        this.sysUserService = sysUserService;
    }

    @PostMapping("/apply")
    // Regular users can apply
    public Result<Boolean> submitApply(@RequestBody ApplyRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        try {
            apprenticeshipService.submitApply(userId, req.getMasterId(), req.getContent());
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Data
    public static class ApplyRequest {
        private Long masterId;
        private String content;
    }

    /**
     * 获取我的师父信息 (用于申请传承人时自动回填)
     */
    @GetMapping("/my-master")
    public Result<Map<String, Object>> getMyMaster(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        com.lqx.opera.entity.InheritorProfile masterProfile = apprenticeshipService.getMyMaster(userId);
        if (masterProfile == null) {
            return Result.success(null);
        }

        SysUser masterUser = sysUserService.getById(masterProfile.getUserId());
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", masterProfile.getId());
        map.put("name", masterUser != null && masterUser.getRealName() != null ? masterUser.getRealName() : (masterUser != null ? masterUser.getUsername() : "未知"));
        map.put("level", masterProfile.getLevel());
        
        return Result.success(map);
    }

    /**
     * 获取我的徒弟列表
     */
    @GetMapping("/my-apprentices")
    @RequireRole(1)
    public Result<List<com.lqx.opera.common.dto.ApprenticeInfoDTO>> getMyApprentices(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        return Result.success(apprenticeshipService.getMyApprentices(userId));
    }

    /**
     * 分页查询待审核申请
     */
    @GetMapping("/list")
    @RequireRole(1) // 传承人
    public Result<Page<ApprenticeApplyDTO>> list(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        Page<ApprenticeshipApply> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ApprenticeshipApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprenticeshipApply::getMasterId, userId)
               .orderByDesc(ApprenticeshipApply::getId); // Show all, let frontend filter or backend filter? 
               // User requirement: "分页查询待审核申请" (Page query pending applications)
               // But usually management needs to see history too. I'll return all and let frontend filter or add status param.
               // Re-reading prompt: "实现 GET ... 分页查询待审核申请"
               // Okay, I will filter status=0 by default or allow param.
               // Let's filter status=0 as requested, but maybe useful to see history.
               // Prompt says: "查询待审核申请". I'll default to all but prioritize pending or just return pending?
               // Let's stick to returning all so they can see history, but maybe prompt implies ONLY pending.
               // "ApprenticeshipController...实现...分页查询待审核申请" -> strict reading implies only pending.
               // But "修改状态...当状态变为1时..." implies flow.
               // I'll add a status param, default to null (all) or 0 (pending).
               // Let's support status filtering.

        // Actually, user prompt UI description: "列表页...点击'通过'..."
        // I will return all for better UX (history), but allow filtering.
        
        // Wait, strictly following prompt: "查询待审核申请" (query PENDING applications).
        // I will add `.eq(ApprenticeshipApply::getStatus, 0)` if that's what is strictly asked.
        // But for a management module, seeing history is crucial.
        // I will default to status 0 if not provided, or just return pending if strictly interpreted.
        // Let's return pending only as per prompt instruction "查询待审核申请", but maybe add an option.
        // Prompt: "实现 GET /api/master/apprentice/list 分页查询待审核申请"
        // I will filter by status=0.

        wrapper.eq(ApprenticeshipApply::getStatus, 0);

        Page<ApprenticeshipApply> resultPage = apprenticeshipService.page(pageParam, wrapper);
        
        // Convert to DTO with student info
        List<ApprenticeApplyDTO> dtos = resultPage.getRecords().stream().map(apply -> {
            ApprenticeApplyDTO dto = new ApprenticeApplyDTO();
            dto.setId(apply.getId());
            dto.setStudentId(apply.getStudentId());
            dto.setApplyContent(apply.getApplyContent());
            dto.setStatus(apply.getStatus());
            // Get student info
            SysUser student = sysUserService.getById(apply.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getRealName() != null ? student.getRealName() : student.getUsername());
                dto.setStudentAvatar(student.getAvatar());
            }
            return dto;
        }).collect(Collectors.toList());

        Page<ApprenticeApplyDTO> dtoPage = new Page<>(page, size);
        dtoPage.setTotal(resultPage.getTotal());
        dtoPage.setRecords(dtos);

        return Result.success(dtoPage);
    }

    /**
     * 修改状态
     */
    @PutMapping("/audit")
    @RequireRole(1)
    public Result<Boolean> audit(@RequestBody AuditRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        try {
            // Use existing service method
            // auditApply(Long applyId, Long mentorId, boolean pass)
            // But prompt asks for status 0, 1, 2.
            // Service method takes boolean pass.
            // If status is 1 -> pass=true, 2 -> pass=false.
            // If status is 0, it's pending (no action).
            
            if (req.getStatus() == 1) {
                apprenticeshipService.auditApply(req.getId(), userId, true);
            } else if (req.getStatus() == 2) {
                apprenticeshipService.auditApply(req.getId(), userId, false);
            } else {
                return Result.fail("无效的状态");
            }
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @Data
    public static class ApprenticeApplyDTO {
        private Long id;
        private Long studentId;
        private String studentName;
        private String studentAvatar;
        private String applyContent;
        private Integer status;
    }

    @Data
    public static class AuditRequest {
        private Long id;
        private Integer status; // 1-pass, 2-reject
        private String message; // Optional message
    }
}
