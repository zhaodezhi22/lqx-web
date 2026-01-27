package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.ApprenticeshipApply;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.ApprenticeshipApplyMapper;
import com.lqx.opera.service.ApprenticeshipService;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApprenticeshipServiceImpl extends ServiceImpl<ApprenticeshipApplyMapper, ApprenticeshipApply> implements ApprenticeshipService {

    private final InheritorProfileService inheritorProfileService;
    private final SysUserService sysUserService;
    private final com.lqx.opera.service.CommunityService communityService;

    public ApprenticeshipServiceImpl(InheritorProfileService inheritorProfileService, 
                                     SysUserService sysUserService,
                                     com.lqx.opera.service.CommunityService communityService) {
        this.inheritorProfileService = inheritorProfileService;
        this.sysUserService = sysUserService;
        this.communityService = communityService;
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
            }

            // 自动发布社区动态
            SysUser student = sysUserService.getById(apply.getStudentId());
            String studentName = (student != null && student.getRealName() != null) ? student.getRealName() : (student != null ? student.getUsername() : "未知学徒");
            String content = "恭喜 " + studentName + " 正式拜入 " + mentorName + " 大师门下，传承非遗文化！";
            // 假设使用系统账号或师父账号发布？这里使用师父账号发布
            communityService.createPost(mentorId, content, null);
        } else {
            apply.setStatus(2); // 拒绝
        }
        this.updateById(apply);
    }
}
