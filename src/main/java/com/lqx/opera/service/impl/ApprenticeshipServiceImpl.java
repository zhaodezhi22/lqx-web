package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuqin.opera.entity.InheritorProfile;
import com.liuqin.opera.entity.SysUser;
import com.liuqin.opera.mapper.InheritorProfileMapper;
import com.liuqin.opera.mapper.SysUserMapper;
import com.lqx.opera.entity.ApprenticeshipApply;
import com.lqx.opera.mapper.ApprenticeshipApplyMapper;
import com.lqx.opera.service.ApprenticeshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprenticeshipServiceImpl extends ServiceImpl<ApprenticeshipApplyMapper, ApprenticeshipApply> implements ApprenticeshipService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private InheritorProfileMapper inheritorProfileMapper;

    @Override
    public void submitApply(Long studentId, Long masterId, String content) {
        // 校验：查询是否已存在审核中的申请
        LambdaQueryWrapper<ApprenticeshipApply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ApprenticeshipApply::getStudentId, studentId)
                    .eq(ApprenticeshipApply::getMasterId, masterId)
                    .eq(ApprenticeshipApply::getStatus, 0); // 0-审核中
        
        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("您已提交过申请，请等待审核");
        }

        // 保存新申请
        ApprenticeshipApply apply = new ApprenticeshipApply();
        apply.setStudentId(studentId);
        apply.setMasterId(masterId);
        apply.setApplyContent(content);
        apply.setStatus(0); // 0-审核中
        this.save(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long applyId, Integer auditStatus, Long currentUserId) {
        // 查询申请记录
        ApprenticeshipApply apply = this.getById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请不存在");
        }

        // 权限校验
        if (!apply.getMasterId().equals(currentUserId)) {
            throw new RuntimeException("无权审核此申请");
        }

        // 更新状态
        apply.setStatus(auditStatus);
        this.updateById(apply);

        // 如果通过 (auditStatus == 1)，联动更新
        if (auditStatus == 1) {
            // 查询师父的真实姓名
            SysUser master = sysUserMapper.selectById(currentUserId);
            if (master == null) {
                throw new RuntimeException("师父信息不存在");
            }

            // 查询徒弟的档案
            LambdaQueryWrapper<InheritorProfile> profileWrapper = new LambdaQueryWrapper<>();
            profileWrapper.eq(InheritorProfile::getUserId, apply.getStudentId());
            InheritorProfile studentProfile = inheritorProfileMapper.selectOne(profileWrapper);

            if (studentProfile != null) {
                // 更新徒弟档案表中的 master_name
                studentProfile.setMasterName(master.getRealName());
                inheritorProfileMapper.updateById(studentProfile);
            } else {
                // 如果没有档案，可能需要创建或者记录日志，这里根据需求暂时忽略或抛出
                // 考虑到徒弟申请拜师，应该是已有传承人身份或者正在申请，这里假设档案存在或不强制报错
            }
        }
    }
}
