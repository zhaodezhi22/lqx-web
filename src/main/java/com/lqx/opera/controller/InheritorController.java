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
    private static final String MASTER_CHANGE_PREFIX = "[MASTER_CHANGE]";

    private final InheritorProfileService inheritorProfileService;
    private final SysUserService sysUserService;
    private final com.lqx.opera.service.InheritorLevelApplyService inheritorLevelApplyService;
    private final com.lqx.opera.service.ApprenticeshipService apprenticeshipService;

    public InheritorController(InheritorProfileService inheritorProfileService,
                               SysUserService sysUserService,
                               com.lqx.opera.service.InheritorLevelApplyService inheritorLevelApplyService,
                               com.lqx.opera.service.ApprenticeshipService apprenticeshipService) {
        this.inheritorProfileService = inheritorProfileService;
        this.sysUserService = sysUserService;
        this.inheritorLevelApplyService = inheritorLevelApplyService;
        this.apprenticeshipService = apprenticeshipService;
    }

    @GetMapping("/featured")
    public Result<java.util.List<SpotlightItem>> featured(@RequestParam(required = false, defaultValue = "4") Integer limit) {
        java.util.List<InheritorProfile> profiles = inheritorProfileService.getRecentList(limit);
        
        java.util.List<SpotlightItem> items = new java.util.ArrayList<>();
        for (InheritorProfile p : profiles) {
            SysUser user = sysUserService.getById(p.getUserId());
            if (user == null) continue;
            SpotlightItem item = new SpotlightItem();
            item.setInheritorId(p.getId());
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
        java.util.List<InheritorProfile> all = inheritorProfileService.getListSortedByLevel();
        int max = limit == null ? 8 : limit;
        if (max > all.size()) max = all.size();
        java.util.List<InheritorProfile> profiles = all.subList(0, max);

        java.util.List<SpotlightItem> items = new java.util.ArrayList<>();
        for (InheritorProfile p : profiles) {
            SysUser user = sysUserService.getById(p.getUserId());
            if (user == null) continue;
            SpotlightItem item = new SpotlightItem();
            item.setInheritorId(p.getId());
            item.setUserId(user.getUserId());
            item.setName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            item.setAvatar(user.getAvatar());
            item.setLevel(p.getLevel());
            items.add(item);
        }
        return Result.success(items);
    }

    @GetMapping("/list")
    public Result<java.util.List<SpotlightItem>> list(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String level) {
        java.util.List<InheritorProfile> profiles = inheritorProfileService.getListSortedByLevel();
        java.util.List<SpotlightItem> items = new java.util.ArrayList<>();
        for (InheritorProfile p : profiles) {
            if (level != null && !level.isBlank() && !level.equals(p.getLevel())) {
                continue;
            }
            SysUser user = sysUserService.getById(p.getUserId());
            if (user == null) continue;
            String displayName = user.getRealName() != null ? user.getRealName() : user.getUsername();
            if (keyword != null && !keyword.isBlank() && !displayName.contains(keyword.trim())) {
                continue;
            }
            SpotlightItem item = new SpotlightItem();
            item.setInheritorId(p.getId());
            item.setUserId(user.getUserId());
            item.setName(displayName);
            item.setAvatar(user.getAvatar());
            item.setLevel(p.getLevel());
            items.add(item);
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

    @GetMapping("/lineage/{id}")
    public Result<com.lqx.opera.common.dto.LineageTreeNodeDto> getLineageTree(@PathVariable Long id) {
        return Result.success(inheritorProfileService.getLineageTree(id));
    }

    @GetMapping("/masters")
    public Result<List<java.util.Map<String, Object>>> searchMasters(@RequestParam String query) {
        List<InheritorProfile> profiles = inheritorProfileService.searchVerifiedMasters(query);
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (InheritorProfile p : profiles) {
            SysUser u = sysUserService.getById(p.getUserId());
            if (u != null) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", p.getId());
                map.put("userId", p.getUserId());
                map.put("name", u.getRealName() != null ? u.getRealName() : u.getUsername());
                map.put("level", p.getLevel());
                result.add(map);
            }
        }
        return Result.success(result);
    }

    /**
     * 获取当前用户的申请状态
     */
    @GetMapping("/my-status")
    public Result<InheritorProfile> getMyStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        InheritorProfile profile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, userId)
                .last("LIMIT 1"));
        
        populateMasterName(profile);
        
        return Result.success(profile);
    }

    public static class SpotlightItem {
        private Long inheritorId;
        private Long userId;
        private String name;
        private String avatar;
        private String level;
        public Long getInheritorId() { return inheritorId; }
        public void setInheritorId(Long inheritorId) { this.inheritorId = inheritorId; }
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
            if (exist.getVerifyStatus() != null && exist.getVerifyStatus() == 1) {
                return Result.fail("您已是认证传承人，无需重复申请");
            }
            profile.setId(exist.getId());
        }
        profile.setUserId(userId);
        profile.setVerifyStatus(0); // Reset to pending (if rejected or updating pending)
        boolean ok = inheritorProfileService.saveOrUpdate(profile);
        return ok ? Result.success(true) : Result.fail("提交失败");
    }

    /**
     * 管理?审核员查看待审核列表
     */
    @GetMapping("/pending")
    @RequireRole({2, 3})
    public Result<List<InheritorProfile>> pendingList(HttpServletRequest request) {
        List<InheritorProfile> list = inheritorProfileService.list(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getVerifyStatus, 0));
        if (list != null) {
            list.forEach(this::populateMasterName);
        }
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
        
        populateMasterName(profile);
        
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
        
        // Prevent level modification directly
        profile.setLevel(exist.getLevel());
        // Prevent direct master modification; master changes must go through admin audit.
        profile.setMasterId(exist.getMasterId());
        profile.setMasterName(exist.getMasterName());
        
        profile.setId(exist.getId());
        profile.setUserId(userId);
        profile.setVerifyStatus(exist.getVerifyStatus());
        
        boolean ok = inheritorProfileService.updateById(profile);
        return ok ? Result.success(true) : Result.fail("更新失败");
    }

    @GetMapping("/master-change/my-apply")
    @RequireRole(1)
    public Result<MasterChangeApplyDto> getMyMasterChangeApply(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        com.lqx.opera.entity.ApprenticeshipApply apply = apprenticeshipService.getPendingMasterChangeApply(userId);
        if (apply == null) {
            return Result.success(null);
        }

        InheritorProfile profile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, userId)
                .last("LIMIT 1"));
        populateMasterName(profile);

        MasterChangeApplyDto dto = new MasterChangeApplyDto();
        dto.setId(apply.getId());
        dto.setStatus(apply.getStatus());
        dto.setTargetMasterUserId(apply.getMasterId());
        dto.setReason(stripMasterChangePrefix(apply.getApplyContent()));
        dto.setCurrentMasterName(profile != null ? profile.getMasterName() : null);

        SysUser targetMaster = sysUserService.getById(apply.getMasterId());
        if (targetMaster != null) {
            dto.setTargetMasterName(targetMaster.getRealName() != null && !targetMaster.getRealName().isEmpty()
                    ? targetMaster.getRealName() : targetMaster.getUsername());
        }
        return Result.success(dto);
    }

    @PostMapping("/master-change/apply")
    @RequireRole(1)
    public Result<Boolean> applyMasterChange(@RequestBody MasterChangeRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        if (req == null || req.getTargetMasterUserId() == null) {
            return Result.fail(400, "请选择新的师父");
        }

        try {
            apprenticeshipService.submitMasterChangeApply(userId, req.getTargetMasterUserId(), req.getReason());
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
    
    /**
     * 提交等级变更申请
     */
    @PostMapping("/level/apply")
    @RequireRole(1)
    public Result<Boolean> applyLevelChange(@RequestBody com.lqx.opera.entity.InheritorLevelApply apply, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        try {
            inheritorLevelApplyService.submitApply(userId, apply.getCurrentLevel(), apply.getApplyLevel(), apply.getReason(), apply.getProofMaterials());
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取我的最新等级变更申请
     */
    @GetMapping("/level/my-apply")
    @RequireRole(1)
    public Result<com.lqx.opera.entity.InheritorLevelApply> getMyLevelApply(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        return Result.success(inheritorLevelApplyService.getLatestApply(userId));
    }

    private void populateMasterName(InheritorProfile profile) {
        if (profile != null && profile.getMasterId() != null) {
            InheritorProfile masterProfile = inheritorProfileService.getById(profile.getMasterId());
            if (masterProfile != null) {
                SysUser masterUser = sysUserService.getById(masterProfile.getUserId());
                if (masterUser != null) {
                    String realName = masterUser.getRealName();
                    profile.setMasterName((realName != null && !realName.isEmpty()) ? realName : masterUser.getUsername());
                }
            }
        }
    }

    private String stripMasterChangePrefix(String content) {
        if (content == null) {
            return "";
        }
        return content.startsWith(MASTER_CHANGE_PREFIX) ? content.substring(MASTER_CHANGE_PREFIX.length()) : content;
    }

    public static class MasterChangeRequest {
        private Long targetMasterUserId;
        private String reason;

        public Long getTargetMasterUserId() { return targetMasterUserId; }
        public void setTargetMasterUserId(Long targetMasterUserId) { this.targetMasterUserId = targetMasterUserId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class MasterChangeApplyDto {
        private Long id;
        private Integer status;
        private Long targetMasterUserId;
        private String currentMasterName;
        private String targetMasterName;
        private String reason;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public Long getTargetMasterUserId() { return targetMasterUserId; }
        public void setTargetMasterUserId(Long targetMasterUserId) { this.targetMasterUserId = targetMasterUserId; }
        public String getCurrentMasterName() { return currentMasterName; }
        public void setCurrentMasterName(String currentMasterName) { this.currentMasterName = currentMasterName; }
        public String getTargetMasterName() { return targetMasterName; }
        public void setTargetMasterName(String targetMasterName) { this.targetMasterName = targetMasterName; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
