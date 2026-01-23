package com.liuqin.opera.controller;

import com.liuqin.opera.common.annotation.RequireRole;
import com.liuqin.opera.common.Result;
import com.liuqin.opera.entity.SysUser;
import com.liuqin.opera.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequireRole(3) // 仅管理员可访问
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public Result<List<SysUser>> listAll() {
        return Result.success(sysUserService.list());
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody SysUser sysUser) {
        boolean saved = sysUserService.save(sysUser);
        return saved ? Result.success(true) : Result.fail("保存失败");
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SysUser sysUser) {
        sysUser.setUserId(id);
        boolean updated = sysUserService.updateById(sysUser);
        return updated ? Result.success(true) : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = sysUserService.removeById(id);
        return removed ? Result.success(true) : Result.fail("删除失败");
    }
}

