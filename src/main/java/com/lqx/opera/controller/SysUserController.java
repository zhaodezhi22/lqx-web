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

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
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

