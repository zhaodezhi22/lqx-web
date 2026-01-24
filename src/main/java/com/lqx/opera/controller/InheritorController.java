package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.GraphResultDto;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inheritor")
public class InheritorController {

    private final InheritorProfileService inheritorProfileService;
    private final SysUserService sysUserService;

    public InheritorController(InheritorProfileService inheritorProfileService, SysUserService sysUserService) {
        this.inheritorProfileService = inheritorProfileService;
        this.sysUserService = sysUserService;
    }

    @GetMapping("/featured")
    public Result<java.util.List<SpotlightItem>> featured(@RequestParam(required = false, defaultValue = "4") Integer limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InheritorProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(InheritorProfile::getVerifyStatus, 1)
                .orderByDesc(InheritorProfile::getId);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<InheritorProfile> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, limit == null ? 4 : limit);
        java.util.List<InheritorProfile> profiles = inheritorProfileService.page(page, wrapper).getRecords();
        java.util.List<SpotlightItem> items = new java.util.ArrayList<>();
        for (InheritorProfile p : profiles) {
            SysUser user = sysUserService.getById(p.getUserId());
            if (user == null) continue;
            SpotlightItem item = new SpotlightItem();
            item.setUserId(user.getUserId());
            item.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            item.setAvatar(user.getAvatar());
            item.setLevel(p.getLevel());
            items.add(item);
        }
        return Result.success(items);
    }

    @GetMapping("/spotlight")
    public Result<java.util.List<SpotlightItem>> spotlight(@RequestParam(required = false, defaultValue = "8") Integer limit) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InheritorProfile> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(InheritorProfile::getVerifyStatus, 1)
                .orderByDesc(InheritorProfile::getId);
        java.util.List<InheritorProfile> profiles = inheritorProfileService.list(wrapper);
        java.util.List<SpotlightItem> items = new java.util.ArrayList<>();
        for (InheritorProfile p : profiles) {
            SysUser user = sysUserService.getById(p.getUserId());
            if (user == null) continue;
            SpotlightItem item = new SpotlightItem();
            item.setUserId(user.getUserId());
            item.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            item.setAvatar(user.getAvatar());
            item.setLevel(p.getLevel());
            items.add(item);
            if (items.size() >= (limit == null ? 8 : limit)) break;
        }
        return Result.success(items);
    }

    @GetMapping("/graph/all")
    public Result<GraphResultDto> getGraphAll() {
        return Result.success(inheritorProfileService.getLineageGraph(null));
    }

    @GetMapping("/graph/{id}")
    public Result<GraphResultDto> getGraphById(@PathVariable Long id) {
        return Result.success(inheritorProfileService.getLineageGraph(id));
    }

    public static class SpotlightItem {
        private Long userId;
        private String name;
        private String avatar;
        private String level;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
    /**
     * 普通用户提交传承人申请
     */
    @PostMapping("/apply")
    public Result<Boolean> apply(@RequestBody InheritorProfile profile, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        InheritorProfile exist = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, userId));
        if (exist != null) {
            profile.setId(exist.getId());
        }
        profile.setUserId(userId);
        profile.setVerifyStatus(0);
        boolean ok = inheritorProfileService.saveOrUpdate(profile);
        return ok ? Result.success(true) : Result.fail("提交失败");
    }

    /**
     * 管理�?审核员查看待审核列表
     */
    @GetMapping("/pending")
    @RequireRole({2, 3})
    public Result<List<InheritorProfile>> pendingList(HttpServletRequest request) {
        List<InheritorProfile> list = inheritorProfileService.list(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getVerifyStatus, 0));
        return Result.success(list);
    }

    /**
     * 审核通过
     */
    @PostMapping("/approve/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> approve(@PathVariable Long id, HttpServletRequest request) {
        InheritorProfile profile = inheritorProfileService.getById(id);
        if (profile == null) {
            return Result.fail(404, "记录不存在");
        }
        profile.setVerifyStatus(1);
        inheritorProfileService.updateById(profile);
        SysUser user = sysUserService.getById(profile.getUserId());
        if (user != null) {
            user.setRole(1);
            sysUserService.updateById(user);
        }
        return Result.success(true);
    }

    /**
     * 审核驳回
     */
    @PostMapping("/reject/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> reject(@PathVariable Long id, HttpServletRequest request) {
        boolean removed = inheritorProfileService.removeById(id);
        return removed ? Result.success(true) : Result.fail("操作失败");
    }

    @GetMapping("/profile")
    @RequireRole(1)
    public Result<InheritorProfile> getMyProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        InheritorProfile profile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, userId));
        return Result.success(profile);
    }

    @PutMapping("/profile")
    @RequireRole(1)
    public Result<Boolean> updateMyProfile(@RequestBody InheritorProfile profile, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        InheritorProfile exist = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, userId));
        if (exist == null) return Result.fail(404, "档案不存在");
        
        profile.setId(exist.getId());
        profile.setUserId(userId);
        profile.setVerifyStatus(exist.getVerifyStatus());
        
        return inheritorProfileService.updateById(profile) ? Result.success(true) : Result.fail("更新失败");
    }
}

