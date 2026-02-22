package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.InheritorLevelApplyService;
import com.lqx.opera.service.InheritorLevelApplyService.LevelApplyDto;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/inheritor")
public class AdminInheritorController {

    private final InheritorProfileService inheritorProfileService;
    private final InheritorLevelApplyService inheritorLevelApplyService;

    public AdminInheritorController(InheritorProfileService inheritorProfileService, InheritorLevelApplyService inheritorLevelApplyService) {
        this.inheritorProfileService = inheritorProfileService;
        this.inheritorLevelApplyService = inheritorLevelApplyService;
    }

    /**
     * 获取等级变更申请列表
     * GET /api/admin/inheritor/level/list
     */
    @GetMapping("/level/list")
    @RequireRole({2, 3})
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<LevelApplyDto>> levelApplyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.lqx.opera.entity.InheritorLevelApply> p = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        return Result.success(inheritorLevelApplyService.getAuditDtoPage(p, status));
    }

    /**
     * 审核等级变更申请
     * PUT /api/admin/inheritor/level/audit
     */
    @PutMapping("/level/audit")
    @RequireRole({2, 3})
    public Result<Boolean> auditLevel(@RequestBody Map<String, Object> body) {
        try {
            Long id = Long.valueOf(body.get("id").toString());
            Integer status = Integer.valueOf(body.get("status").toString());
            String remark = (String) body.get("remark");

            if (status != 1 && status != 2) {
                return Result.fail("Invalid status");
            }
            if (status == 2 && (remark == null || remark.trim().isEmpty())) {
                return Result.fail("拒绝时必须填写审核意见");
            }

            inheritorLevelApplyService.auditApply(id, status, remark);
            return Result.success(true);
        } catch (NumberFormatException e) {
            return Result.fail("参数格式错误");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取待审核列表
     * GET /api/admin/inheritor/pending
     */
    @GetMapping("/pending")
    @RequireRole({2, 3})
    public Result<java.util.List<InheritorProfile>> pendingList() {
        return Result.success(inheritorProfileService.list(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getVerifyStatus, 0)));
    }

    /**
     * 审核传承人申请
     * PUT /api/admin/inheritor/audit
     * Body: { "id": 1, "status": 1, "remark": "Passed" }
     */
    @PutMapping("/audit")
    @RequireRole({2, 3})
    public Result<Boolean> audit(@RequestBody Map<String, Object> body) {
        try {
            Long id = Long.valueOf(body.get("id").toString());
            Integer status = Integer.valueOf(body.get("status").toString());
            String remark = (String) body.get("remark");

            if (status != 1 && status != 2) {
                return Result.fail("Invalid status");
            }
            if (status == 2 && (remark == null || remark.trim().isEmpty())) {
                return Result.fail("拒绝时必须填写审核意见");
            }

            inheritorProfileService.auditInheritor(id, status, remark);
            return Result.success(true);
        } catch (NumberFormatException e) {
            return Result.fail("参数格式错误");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
}
