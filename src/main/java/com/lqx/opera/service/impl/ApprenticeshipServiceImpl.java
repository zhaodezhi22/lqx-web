package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.common.dto.AdminLineageDTO;
import com.lqx.opera.common.dto.ApprenticeInfoDTO;
import com.lqx.opera.entity.ApprenticeshipApply;
import com.lqx.opera.entity.ApprenticeshipRelation;
import com.lqx.opera.entity.InheritorProfile;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.ApprenticeshipApplyMapper;
import com.lqx.opera.service.ApprenticeshipService;
import com.lqx.opera.service.InheritorProfileService;
import com.lqx.opera.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApprenticeshipServiceImpl extends ServiceImpl<ApprenticeshipApplyMapper, ApprenticeshipApply> implements ApprenticeshipService {
    private static final String MASTER_CHANGE_PREFIX = "[MASTER_CHANGE]";
    private static final String UNBIND_PREFIX = "[UNBIND]";

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
        ApprenticeshipRelation currentRelation = getCurrentActiveRelationByStudent(studentId);
        if (currentRelation != null) {
            if (currentRelation.getMasterId().equals(masterId)) {
                throw new RuntimeException("当前已关联该师父，无需重复申请");
            }
            throw new RuntimeException("您当前已有师父，如需更换请先提交师承变更或解除关系申请");
        }

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
    public ApprenticeshipApply submitUnbindApply(Long studentId, String content) {
        ApprenticeshipRelation currentRelation = getCurrentActiveRelationByStudent(studentId);
        if (currentRelation == null) {
            throw new RuntimeException("当前暂无可解除的师徒关系");
        }

        Long count = this.count(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getMasterId, currentRelation.getMasterId())
                .eq(ApprenticeshipApply::getStatus, 0)
                .likeRight(ApprenticeshipApply::getApplyContent, UNBIND_PREFIX));
        if (count > 0) {
            throw new RuntimeException("您已有待审核的解除申请，请勿重复提交");
        }

        ApprenticeshipApply apply = new ApprenticeshipApply();
        apply.setStudentId(studentId);
        apply.setMasterId(currentRelation.getMasterId());
        apply.setApplyContent(buildUnbindContent(content));
        apply.setStatus(0);
        this.save(apply);
        return apply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprenticeshipApply submitMasterChangeApply(Long studentId, Long masterId, String content) {
        ApprenticeshipRelation currentRelation = getCurrentActiveRelationByStudent(studentId);
        if (currentRelation == null) {
            throw new RuntimeException("当前暂无师父，请直接提交拜师申请");
        }

        Long count = this.count(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getStatus, 0)
                .likeRight(ApprenticeshipApply::getApplyContent, MASTER_CHANGE_PREFIX));
        if (count > 0) {
            throw new RuntimeException("您已有待审核的师承变更申请，请勿重复提交");
        }
        if (studentId.equals(masterId)) {
            throw new RuntimeException("不能将自己设置为师父");
        }

        InheritorProfile studentProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, studentId)
                .eq(InheritorProfile::getVerifyStatus, 1)
                .last("LIMIT 1"));
        if (studentProfile == null) {
            throw new RuntimeException("仅已认证传承人可申请修改师承");
        }

        InheritorProfile masterProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, masterId)
                .eq(InheritorProfile::getVerifyStatus, 1)
                .last("LIMIT 1"));
        if (masterProfile == null) {
            throw new RuntimeException("目标师父不存在或未认证");
        }
        if (masterProfile.getId().equals(studentProfile.getMasterId())) {
            throw new RuntimeException("当前已关联该师父，无需重复申请");
        }

        ApprenticeshipApply apply = new ApprenticeshipApply();
        apply.setStudentId(studentId);
        apply.setMasterId(masterId);
        apply.setApplyContent(buildMasterChangeContent(content));
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
        if (isMasterChangeApply(apply)) {
            throw new RuntimeException("该申请需由管理员审核");
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
            }


            // 自动发布社区动态
            SysUser student = sysUserService.getById(apply.getStudentId());
            String studentName = (student != null && student.getRealName() != null) ? student.getRealName() : (student != null ? student.getUsername() : "未知学徒");
            String content = "恭喜 " + studentName + " 正式拜入 " + mentorName + " 大师门下，传承非遗文化！";
            communityService.createPost(mentorId, content, null);

            // Create Apprenticeship Relation
            ApprenticeshipRelation existingRelation = apprenticeshipRelationMapper.selectOne(
                    new LambdaQueryWrapper<ApprenticeshipRelation>()
                            .eq(ApprenticeshipRelation::getStudentId, apply.getStudentId())
                            .eq(ApprenticeshipRelation::getMasterId, mentorId)
                            .orderByDesc(ApprenticeshipRelation::getId)
                            .last("LIMIT 1"));
            if (existingRelation != null) {
                existingRelation.setRelationStatus(1);
                existingRelation.setCreateTime(LocalDateTime.now());
                apprenticeshipRelationMapper.updateById(existingRelation);
            } else {
                ApprenticeshipRelation relation = new ApprenticeshipRelation();
                relation.setMasterId(mentorId);
                relation.setStudentId(apply.getStudentId());
                relation.setRelationStatus(1); // 1-Teaching
                relation.setCreateTime(LocalDateTime.now());
                apprenticeshipRelationMapper.insert(relation);
            }

        } else {
            apply.setStatus(2); // 拒绝
        }
        this.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditMasterChangeApply(Long applyId, boolean pass) {
        ApprenticeshipApply apply = this.getById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!isMasterChangeApply(apply)) {
            throw new RuntimeException("该记录不是师承变更申请");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }

        if (pass) {
            InheritorProfile studentProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, apply.getStudentId())
                    .eq(InheritorProfile::getVerifyStatus, 1)
                    .last("LIMIT 1"));
            if (studentProfile == null) {
                throw new RuntimeException("申请人档案不存在");
            }

            InheritorProfile targetMasterProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, apply.getMasterId())
                    .eq(InheritorProfile::getVerifyStatus, 1)
                    .last("LIMIT 1"));
            if (targetMasterProfile == null) {
                throw new RuntimeException("目标师父不存在或未认证");
            }

            SysUser masterUser = sysUserService.getById(apply.getMasterId());
            String masterName = resolveUserName(masterUser);

            studentProfile.setMasterId(targetMasterProfile.getId());
            studentProfile.setMasterName(masterName);
            inheritorProfileService.updateById(studentProfile);

            List<ApprenticeshipRelation> activeRelations = apprenticeshipRelationMapper.selectList(
                    new LambdaQueryWrapper<ApprenticeshipRelation>()
                            .eq(ApprenticeshipRelation::getStudentId, apply.getStudentId())
                            .in(ApprenticeshipRelation::getRelationStatus, 1, 2));
            for (ApprenticeshipRelation relation : activeRelations) {
                relation.setRelationStatus(3);
                apprenticeshipRelationMapper.updateById(relation);
            }

            ApprenticeshipRelation existingRelation = apprenticeshipRelationMapper.selectOne(
                    new LambdaQueryWrapper<ApprenticeshipRelation>()
                            .eq(ApprenticeshipRelation::getStudentId, apply.getStudentId())
                            .eq(ApprenticeshipRelation::getMasterId, apply.getMasterId())
                            .last("LIMIT 1"));
            if (existingRelation != null) {
                existingRelation.setRelationStatus(1);
                existingRelation.setCreateTime(LocalDateTime.now());
                apprenticeshipRelationMapper.updateById(existingRelation);
            } else {
                ApprenticeshipRelation relation = new ApprenticeshipRelation();
                relation.setMasterId(apply.getMasterId());
                relation.setStudentId(apply.getStudentId());
                relation.setRelationStatus(1);
                relation.setCreateTime(LocalDateTime.now());
                apprenticeshipRelationMapper.insert(relation);
            }

            apply.setStatus(1);
        } else {
            apply.setStatus(2);
        }

        this.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditUnbindApply(Long applyId, Long masterId, boolean pass) {
        ApprenticeshipApply apply = this.getById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!isUnbindApply(apply)) {
            throw new RuntimeException("该记录不是解除申请");
        }
        if (!apply.getMasterId().equals(masterId)) {
            throw new RuntimeException("您无权审核此申请");
        }
        if (apply.getStatus() != 0) {
            throw new RuntimeException("该申请已处理");
        }

        if (pass) {
            terminateRelationInternal(masterId, apply.getStudentId());
            apply.setStatus(1);
        } else {
            apply.setStatus(2);
        }
        this.updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateRelation(Long masterId, Long studentId) {
        terminateRelationInternal(masterId, studentId);

        List<ApprenticeshipApply> pendingUnbindApplies = this.list(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getMasterId, masterId)
                .eq(ApprenticeshipApply::getStatus, 0)
                .likeRight(ApprenticeshipApply::getApplyContent, UNBIND_PREFIX));
        for (ApprenticeshipApply apply : pendingUnbindApplies) {
            apply.setStatus(1);
            this.updateById(apply);
        }
    }

    @Override
    public List<ApprenticeInfoDTO> getMyApprentices(Long masterId) {
        List<ApprenticeshipRelation> relations = apprenticeshipRelationMapper.selectList(
                new LambdaQueryWrapper<ApprenticeshipRelation>()
                        .eq(ApprenticeshipRelation::getMasterId, masterId)
                        .in(ApprenticeshipRelation::getRelationStatus, 1, 2)
        );

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> studentIds = relations.stream()
                .map(ApprenticeshipRelation::getStudentId)
                .collect(Collectors.toList());

        List<SysUser> users = sysUserService.listByIds(studentIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        List<InheritorProfile> profiles = inheritorProfileService.list(
                new LambdaQueryWrapper<InheritorProfile>()
                        .in(InheritorProfile::getUserId, studentIds)
        );
        Map<Long, InheritorProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(InheritorProfile::getUserId, p -> p));

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
    public Page<AdminLineageDTO> getLineagePage(Page<ApprenticeshipRelation> page) {
        // 1. Query Relation Page
        Page<ApprenticeshipRelation> relationPage = apprenticeshipRelationMapper.selectPage(page, 
            new LambdaQueryWrapper<ApprenticeshipRelation>().orderByDesc(ApprenticeshipRelation::getCreateTime));
        
        List<ApprenticeshipRelation> records = relationPage.getRecords();
        if (records.isEmpty()) {
            Page<AdminLineageDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), 0);
            return dtoPage;
        }

        // 2. Collect IDs
        List<Long> masterIds = records.stream().map(ApprenticeshipRelation::getMasterId).distinct().collect(Collectors.toList());
        List<Long> studentIds = records.stream().map(ApprenticeshipRelation::getStudentId).distinct().collect(Collectors.toList());
        
        List<Long> allUserIds = new ArrayList<>(masterIds);
        allUserIds.addAll(studentIds);

        // 3. Batch Query Users and Profiles
        Map<Long, SysUser> userMap = sysUserService.listByIds(allUserIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));
        
        Map<Long, InheritorProfile> masterProfileMap = inheritorProfileService.list(
                new LambdaQueryWrapper<InheritorProfile>().in(InheritorProfile::getUserId, masterIds)
        ).stream().collect(Collectors.toMap(InheritorProfile::getUserId, p -> p));

        // 4. Build DTOs
        List<AdminLineageDTO> dtos = records.stream().map(r -> {
            AdminLineageDTO dto = new AdminLineageDTO();
            dto.setId(r.getId());
            dto.setMasterId(r.getMasterId());
            dto.setApprenticeId(r.getStudentId());
            dto.setStartDate(r.getCreateTime());
            dto.setStatus(r.getRelationStatus());

            SysUser master = userMap.get(r.getMasterId());
            if (master != null) {
                dto.setMasterName(master.getRealName() != null ? master.getRealName() : master.getUsername());
            }

            SysUser student = userMap.get(r.getStudentId());
            if (student != null) {
                dto.setApprenticeName(student.getRealName() != null ? student.getRealName() : student.getUsername());
            }

            InheritorProfile mp = masterProfileMap.get(r.getMasterId());
            if (mp != null) {
                dto.setHeritageItem(mp.getGenre());
            }

            return dto;
        }).collect(Collectors.toList());

        Page<AdminLineageDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), relationPage.getTotal());
        dtoPage.setRecords(dtos);
        
        return dtoPage;
    }

    @Override
    public boolean deleteRelation(Long id) {
        return apprenticeshipRelationMapper.deleteById(id) > 0;
    }

    @Override
    public InheritorProfile getMyMaster(Long studentId) {
        ApprenticeshipRelation currentRelation = getCurrentActiveRelationByStudent(studentId);
        if (currentRelation == null) {
            return null;
        }

        return inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, currentRelation.getMasterId())
                .eq(InheritorProfile::getVerifyStatus, 1)
                .last("LIMIT 1"));
    }

    @Override
    public ApprenticeshipApply getPendingMasterChangeApply(Long studentId) {
        return this.getOne(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getStatus, 0)
                .likeRight(ApprenticeshipApply::getApplyContent, MASTER_CHANGE_PREFIX)
                .orderByDesc(ApprenticeshipApply::getId)
                .last("LIMIT 1"));
    }

    @Override
    public ApprenticeshipApply getPendingUnbindApply(Long studentId) {
        return this.getOne(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getStudentId, studentId)
                .eq(ApprenticeshipApply::getStatus, 0)
                .likeRight(ApprenticeshipApply::getApplyContent, UNBIND_PREFIX)
                .orderByDesc(ApprenticeshipApply::getId)
                .last("LIMIT 1"));
    }

    private boolean isMasterChangeApply(ApprenticeshipApply apply) {
        return apply != null
                && apply.getApplyContent() != null
                && apply.getApplyContent().startsWith(MASTER_CHANGE_PREFIX);
    }

    private boolean isUnbindApply(ApprenticeshipApply apply) {
        return apply != null
                && apply.getApplyContent() != null
                && apply.getApplyContent().startsWith(UNBIND_PREFIX);
    }

    private String buildMasterChangeContent(String content) {
        String normalized = content == null ? "" : content.trim();
        return MASTER_CHANGE_PREFIX + normalized;
    }

    private String buildUnbindContent(String content) {
        String normalized = content == null ? "" : content.trim();
        return UNBIND_PREFIX + normalized;
    }

    private ApprenticeshipRelation getCurrentActiveRelationByStudent(Long studentId) {
        return apprenticeshipRelationMapper.selectOne(new LambdaQueryWrapper<ApprenticeshipRelation>()
                .eq(ApprenticeshipRelation::getStudentId, studentId)
                .in(ApprenticeshipRelation::getRelationStatus, 1, 2)
                .orderByDesc(ApprenticeshipRelation::getCreateTime)
                .orderByDesc(ApprenticeshipRelation::getId)
                .last("LIMIT 1"));
    }

    private ApprenticeshipRelation getCurrentActiveRelation(Long masterId, Long studentId) {
        return apprenticeshipRelationMapper.selectOne(new LambdaQueryWrapper<ApprenticeshipRelation>()
                .eq(ApprenticeshipRelation::getMasterId, masterId)
                .eq(ApprenticeshipRelation::getStudentId, studentId)
                .in(ApprenticeshipRelation::getRelationStatus, 1, 2)
                .orderByDesc(ApprenticeshipRelation::getCreateTime)
                .orderByDesc(ApprenticeshipRelation::getId)
                .last("LIMIT 1"));
    }

    private void terminateRelationInternal(Long masterId, Long studentId) {
        ApprenticeshipRelation relation = getCurrentActiveRelation(masterId, studentId);
        if (relation == null) {
            throw new RuntimeException("当前师徒关系不存在或已解除");
        }

        relation.setRelationStatus(3);
        apprenticeshipRelationMapper.updateById(relation);

        InheritorProfile studentProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                .eq(InheritorProfile::getUserId, studentId)
                .last("LIMIT 1"));
        if (studentProfile != null) {
            InheritorProfile masterProfile = inheritorProfileService.getOne(new LambdaQueryWrapper<InheritorProfile>()
                    .eq(InheritorProfile::getUserId, masterId)
                    .last("LIMIT 1"));
            if (masterProfile == null || masterProfile.getId().equals(studentProfile.getMasterId())) {
                studentProfile.setMasterId(null);
                studentProfile.setMasterName(null);
                inheritorProfileService.updateById(studentProfile);
            }
        }
    }

    private String resolveUserName(SysUser user) {
        if (user == null) {
            return "未知师父";
        }
        return user.getRealName() != null && !user.getRealName().isBlank() ? user.getRealName() : user.getUsername();
    }
}
