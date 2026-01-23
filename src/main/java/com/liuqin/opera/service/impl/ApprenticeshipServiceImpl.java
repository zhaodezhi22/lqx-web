package com.liuqin.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.ApprenticeshipApply;
import com.liuqin.opera.entity.InheritorProfile;
import com.liuqin.opera.entity.SysUser;
import com.liuqin.opera.mapper.ApprenticeshipApplyMapper;
import com.liuqin.opera.service.ApprenticeshipService;
import com.liuqin.opera.service.InheritorProfileService;
import com.liuqin.opera.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApprenticeshipServiceImpl extends ServiceImpl<ApprenticeshipApplyMapper, ApprenticeshipApply> implements ApprenticeshipService {

    private final InheritorProfileService inheritorProfileService;
    private final SysUserService sysUserService;

    public ApprenticeshipServiceImpl(InheritorProfileService inheritorProfileService, SysUserService sysUserService) {
        this.inheritorProfileService = inheritorProfileService;
        this.sysUserService = sysUserService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprenticeshipApply submitApply(Long studentId, Long masterId, String content) {
        // 1. 检查是否已经有待审核的申请
        Long count = this.count(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getStatus, 0)); // 0-审核中
        if (count > 0) {
            throw new RuntimeException("您已有待审核的申请，请勿重复提交");
        }

        // 2. 检查师父是否存在
        InheritorProfile masterProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, masterId)
                .eq(InheritorProfile::getVerifyStatus, 1)); // 必须是已认证的传承人
        if (masterProfile == null) {
            throw new RuntimeException("该师父不存在或未认证");
        }

        // 3. 创建申请
        ApprenticeshipApply apply = new ApprenticeshipApply();
        apply.setStudentId(studentId);
        apply.setMasterId(masterId);
        apply.setApplyContent(content);
        apply.setStatus(0);
        
        this.save(apply);
        return apply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long applyId, Long mentorId, boolean pass) {
        ApprenticeshipApply apply = this.getById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!apply.getMasterId().equals(mentorId)) {
            throw new RuntimeException("您无权审核此申请");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }

        if (pass) {
            apply.setStatus(1); // 通过
            // 更新学徒的档案
            InheritorProfile studentProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, apply.getStudentId()));
            
            // 获取师父名字
            SysUser mentor = sysUserService.getById(mentorId);
            String mentorName = (mentor != null && mentor.getRealName() != null) ? mentor.getRealName() : "未知师父";

            if (studentProfile != null) {
                studentProfile.setMasterName(mentorName);
                inheritorProfileService.updateById(studentProfile);
            } else {
                // 如果学徒没有档案，可能需要创建？或者抛出异常？
                // 假设学徒应该先注册成为普通传承人用户或者系统自动创建
                // 这里暂时只更新已存在的档案，若不存在则可能需要业务确认。
                // 既然是拜师，通常已经是圈内人。
            }
        } else {
            apply.setStatus(2); // 拒绝
        }
        this.updateById(apply);
    }
}
