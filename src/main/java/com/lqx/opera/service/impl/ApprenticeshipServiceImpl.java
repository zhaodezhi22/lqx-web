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
import com.lqx.opera.common.dto.ApprenticeInfoDTO;
import com.lqx.opera.entity.ApprenticeshipRelation;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApprenticeshipServiceImpl extends ServiceImpl<ApprenticeshipApplyMapper, ApprenticeshipApply> implements ApprenticeshipService {

    private final InheritorProfileService inheritorProfileService;
    private final SysUserService sysUserService;
    private final com.lqx.opera.service.CommunityService communityService;
    private final com.lqx.opera.mapper.ApprenticeshipRelationMapper apprenticeshipRelationMapper;

    public ApprenticeshipServiceImpl(InheritorProfileService inheritorProfileService, 
                                     SysUserService sysUserService,
                                     com.lqx.opera.service.CommunityService communityService,
                                     com.lqx.opera.mapper.ApprenticeshipRelationMapper apprenticeshipRelationMapper) {
        this.inheritorProfileService = inheritorProfileService;
        this.sysUserService = sysUserService;
        this.communityService = communityService;
        this.apprenticeshipRelationMapper = apprenticeshipRelationMapper;
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

        if (studentId.equals(masterId)) {
            throw new RuntimeException("不能拜自己为师");
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

            // 获取师父档案以获取流派信息
            InheritorProfile mentorProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, mentorId));
            
            Long mentorProfileId = mentorProfile != null ? mentorProfile.getId() : null;

            if (studentProfile != null) {
                studentProfile.setMasterName(mentorName);
                if (mentorProfileId != null) {
                    studentProfile.setMasterId(mentorProfileId);
                }
                inheritorProfileService.updateById(studentProfile);
            } else {
                // 如果没有档案，自动创建传承人档案
                studentProfile = new InheritorProfile();
                studentProfile.setUserId(apply.getStudentId());
                studentProfile.setVerifyStatus(1); // 直接认证通过
                if (mentorProfileId != null) {
                    studentProfile.setMasterId(mentorProfileId);
                }
                studentProfile.setMasterName(mentorName);
                if (mentorProfile != null) {
                    studentProfile.setGenre(mentorProfile.getGenre());
                }
                studentProfile.setLevel("县/区级"); // 默认为基础等级
                inheritorProfileService.save(studentProfile);

                // 更新用户角色为传承人
                SysUser studentUser = sysUserService.getById(apply.getStudentId());
                if (studentUser != null) {
                    studentUser.setRole(1);
                    sysUserService.updateById(studentUser);
                }
            }

            // 自动发布社区动态
            SysUser student = sysUserService.getById(apply.getStudentId());
            String studentName = (student != null && student.getRealName() != null) ? student.getRealName() : (student != null ? student.getUsername() : "未知学徒");
            String content = "恭喜 " + studentName + " 正式拜入 " + mentorName + " 大师门下，传承非遗文化！";
            // 假设使用系统账号或师父账号发布？这里使用师父账号发布
            communityService.createPost(mentorId, content, null);

            // Create Apprenticeship Relation
            com.lqx.opera.entity.ApprenticeshipRelation relation = new com.lqx.opera.entity.ApprenticeshipRelation();
            relation.setMasterId(mentorId);
            relation.setStudentId(apply.getStudentId());
            relation.setRelationStatus(1); // 1-Teaching
            relation.setCreateTime(LocalDateTime.now());
            apprenticeshipRelationMapper.insert(relation);

            // Update User Role to Apprentice (3) if they are Ordinary (0)
            if (student != null && (student.getRole() == null || student.getRole() == 0)) {
                student.setRole(3); // 3-Apprentice
                sysUserService.updateById(student);
            }
        } else {
            apply.setStatus(2); // 拒绝
        }
        this.updateById(apply);
    }

    @Override
    public List<ApprenticeInfoDTO> getMyApprentices(Long masterId) {
        // 1. Query Relations
        List<ApprenticeshipRelation> relations = apprenticeshipRelationMapper.selectList(
                new LambdaQueryWrapper<ApprenticeshipRelation>()
                        .eq(ApprenticeshipRelation::getMasterId, masterId)
                        .in(ApprenticeshipRelation::getRelationStatus, 1, 2) // Teaching or Graduated
        );

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Collect Student IDs
        List<Long> studentIds = relations.stream()
                .map(ApprenticeshipRelation::getStudentId)
                .collect(Collectors.toList());

        // 3. Batch Query Users and Profiles
        List<SysUser> users = sysUserService.listByIds(studentIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        List<InheritorProfile> profiles = inheritorProfileService.list(
                new LambdaQueryWrapper<InheritorProfile>()
                        .in(InheritorProfile::getUserId, studentIds)
        );
        Map<Long, InheritorProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(InheritorProfile::getUserId, p -> p));

        // 4. Build DTOs
        return relations.stream().map(relation -> {
            ApprenticeInfoDTO dto = new ApprenticeInfoDTO();
            Long sId = relation.getStudentId();
            SysUser u = userMap.get(sId);
            InheritorProfile p = profileMap.get(sId);

            dto.setStudentId(sId);
            dto.setRelationStatus(relation.getRelationStatus());
            dto.setJoinTime(relation.getCreateTime());

            if (u != null) {
                dto.setUsername(u.getUsername());
                dto.setRealName(u.getRealName());
                dto.setPhone(u.getPhone());
                dto.setAvatar(u.getAvatar());
            }
            if (p != null) {
                dto.setLevel(p.getLevel());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public InheritorProfile getMyMaster(Long studentId) {
        // 查找已通过的拜师申请
        ApprenticeshipApply apply = this.getOne(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getStatus, 1) // 1-已通过
                .orderByDesc(ApprenticeshipApply::getId)
                .last("LIMIT 1"));

        if (apply == null) {
            return null;
        }

        // 获取师父的档案
        return inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, apply.getMasterId())
                .eq(InheritorProfile::getVerifyStatus, 1));
    }
}
