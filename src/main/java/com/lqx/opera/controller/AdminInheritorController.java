package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.service.InheritorProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/inheritor")
public class AdminInheritorController {

    private final InheritorProfileService inheritorProfileService;

    public AdminInheritorController(InheritorProfileService inheritorProfileService) {
        this.inheritorProfileService = inheritorProfileService;
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
