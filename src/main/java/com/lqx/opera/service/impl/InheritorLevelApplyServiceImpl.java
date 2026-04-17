package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.InheritorLevelApply;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.InheritorLevelApplyMapper;
import com.lqx.opera.service.InheritorLevelApplyService;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InheritorLevelApplyServiceImpl extends ServiceImpl<InheritorLevelApplyMapper, InheritorLevelApply> implements InheritorLevelApplyService {

    private final InheritorProfileService inheritorProfileService;
    private final SysUserService sysUserService;

    public InheritorLevelApplyServiceImpl(InheritorProfileService inheritorProfileService, SysUserService sysUserService) {
        this.inheritorProfileService = inheritorProfileService;
        this.sysUserService = sysUserService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApply(Long userId, String currentLevel, String applyLevel, String reason, String proofMaterials) {
        // 1. Check if there is a pending application
        Long count = this.count(new LambdaQueryWrapper<InheritorLevelApply>()
                .eq(InheritorLevelApply::getUserId, userId)
                .eq(InheritorLevelApply::getStatus, 0));
        if (count > 0) {
            throw new RuntimeException("您已有待审核的申请，请勿重复提交");
        }

        InheritorLevelApply apply = new InheritorLevelApply();
        apply.setUserId(userId);
        apply.setCurrentLevel(currentLevel);
        apply.setApplyLevel(applyLevel);
        apply.setReason(reason);
        apply.setProofMaterials(proofMaterials);
        apply.setStatus(0);
        apply.setCreateTime(LocalDateTime.now());
        
        this.save(apply);
    }

    @Override
    public InheritorLevelApply getLatestApply(Long userId) {
        return this.getOne(new LambdaQueryWrapper<InheritorLevelApply>()
                .eq(InheritorLevelApply::getUserId, userId)
                .orderByDesc(InheritorLevelApply::getId)
                .last("LIMIT 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long applyId, Integer status, String remark) {
        InheritorLevelApply apply = this.getById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请不存在");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }

        apply.setStatus(status);
        apply.setAuditRemark(remark);
        apply.setAuditTime(LocalDateTime.now());
        this.updateById(apply);

        if (status == 1) { // Approved
            InheritorProfile profile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, apply.getUserId()));
            if (profile != null) {
                profile.setLevel(apply.getApplyLevel());
                inheritorProfileService.updateById(profile);
            }
        }
    }

    @Override
    public Page<InheritorLevelApply> getAuditPage(Page<InheritorLevelApply> page, Integer status) {
        LambdaQueryWrapper<InheritorLevelApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(InheritorLevelApply::getStatus, status);
        }
        wrapper.orderByDesc(InheritorLevelApply::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Page<LevelApplyDto> getAuditDtoPage(Page<InheritorLevelApply> page, Integer status) {
        Page<InheritorLevelApply> p = getAuditPage(page, status);
        List<InheritorLevelApply> records = p.getRecords();
        
        if (records.isEmpty()) {
            return new Page<>(page.getCurrent(), page.getSize(), p.getTotal());
        }

        List<Long> userIds = records.stream().map(InheritorLevelApply::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        List<LevelApplyDto> dtos = records.stream().map(r -> {
            LevelApplyDto dto = new LevelApplyDto();
            dto.setId(r.getId());
            dto.setUserId(r.getUserId());
            dto.setCurrentLevel(r.getCurrentLevel());
            dto.setApplyLevel(r.getApplyLevel());
            dto.setReason(r.getReason());
            dto.setProofMaterials(r.getProofMaterials());
            dto.setStatus(r.getStatus());
            dto.setAuditRemark(r.getAuditRemark());
            dto.setCreateTime(r.getCreateTime().toString().replace("T", " "));
            
            SysUser u = userMap.get(r.getUserId());
            if (u != null) {
                dto.setUserName(u.getUsername());
                dto.setRealName(u.getRealName());
            }
            return dto;
        }).collect(Collectors.toList());

        Page<LevelApplyDto> dtoPage = new Page<>(page.getCurrent(), page.getSize(), p.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }
}
