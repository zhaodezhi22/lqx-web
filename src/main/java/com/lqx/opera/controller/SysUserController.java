package com.lqx.opera.controller;

import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.Result;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class SysUserController {

    private final SysUserService sysUserService;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 批量封禁/解封用户
     * @param body { "userIds": [...], "status": 0/1 }
     */
    @PostMapping("/batch-status")
    @RequireRole(3)
    public Result<Boolean> batchStatus(@RequestBody java.util.Map<String, Object> body) {
        List<Integer> userIds = (List<Integer>) body.get("userIds");
        Integer status = (Integer) body.get("status");

        if (userIds == null || userIds.isEmpty() || status == null) {
            return Result.fail(400, "参数错误");
        }

        // Convert Integer IDs to Long
        List<Long> longIds = new java.util.ArrayList<>();
        for (Object id : userIds) {
            if (id instanceof Integer) {
                longIds.add(((Integer) id).longValue());
            } else if (id instanceof Long) {
                longIds.add((Long) id);
            }
        }

        if (longIds.isEmpty()) return Result.fail(400, "用户列表为空");

        // Update status for all users
        SysUser update = new SysUser();
        update.setStatus(status);
        
        boolean success = sysUserService.update(update, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysUser>()
                .in(SysUser::getUserId, longIds));
                
        return success ? Result.success(true) : Result.fail("操作失败");
    }

    /**
     * 管理员重置用户密码
     * @param id 用户ID
     * @param body { "password": "newPassword" }
     */
    @PutMapping("/{id}/password")
    @RequireRole(3)
    public Result<Boolean> resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.fail(400, "密码不能为空");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        boolean updated = sysUserService.updateById(user);
        return updated ? Result.success(true) : Result.fail("重置失败");
    }

    /**
     * 设置用户角色（如派发审核员账号）
     * @param id 用户ID
     * @param body { "role": 2 }
     */
    @PutMapping("/{id}/role")
    @RequireRole(3)
    public Result<Boolean> updateRole(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        Integer role = body.get("role");
        if (role == null) {
            return Result.fail(400, "角色不能为空");
        }

        // Validate role range if needed (0-3)
        if (role < 0 || role > 3) {
            return Result.fail(400, "无效的角色代码");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        
        user.setRole(role);
        boolean ok = sysUserService.updateById(user);
        return ok ? Result.success(true) : Result.fail("更新失败");
    }

    /**
     * 用户修改密码 (需要验证旧密码)
     * @param id 用户ID
     * @param body { "oldPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/{id}/password/change")
    public Result<Boolean> changePassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.fail(400, "参数缺失");
        }

        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        // Check old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.fail(400, "旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        boolean updated = sysUserService.updateById(user);
        return updated ? Result.success(true) : Result.fail("修改失败");
    }

    @GetMapping
    @RequireRole(3)
    public Result<List<SysUser>> listAll() {
        return Result.success(sysUserService.list());
    }

    @GetMapping("/{id}")
    @RequireRole(3)
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping
    @RequireRole(3)
    public Result<Boolean> create(@RequestBody SysUser sysUser) {
        boolean saved = sysUserService.save(sysUser);
        return saved ? Result.success(true) : Result.fail("保存失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SysUser sysUser, jakarta.servlet.http.HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Integer currentRole = (Integer) request.getAttribute("role");

        if (currentUserId == null) {
             return Result.fail(401, "未登录");
        }
        boolean isAdmin = currentRole != null && currentRole == 3;
        boolean isSelf = currentUserId.equals(id);

        if (!isAdmin && !isSelf) {
            return Result.fail(403, "无权修改他人信息");
        }

        if (isAdmin) {
            sysUser.setUserId(id);
            boolean updated = sysUserService.updateById(sysUser);
            return updated ? Result.success(true) : Result.fail("更新失败");
        } else {
            // 普通用户只能修改基本资料
            SysUser existing = sysUserService.getById(id);
            if (existing == null) {
                return Result.fail(404, "用户不存在");
            }
            
            if (sysUser.getRealName() != null) existing.setRealName(sysUser.getRealName());
            if (sysUser.getPhone() != null) existing.setPhone(sysUser.getPhone());
            if (sysUser.getEmail() != null) existing.setEmail(sysUser.getEmail());
            if (sysUser.getAvatar() != null) existing.setAvatar(sysUser.getAvatar());
            
            boolean updated = sysUserService.updateById(existing);
            return updated ? Result.success(true) : Result.fail("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole(3)
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = sysUserService.removeById(id);
        return removed ? Result.success(true) : Result.fail("删除失败");
    }
}

