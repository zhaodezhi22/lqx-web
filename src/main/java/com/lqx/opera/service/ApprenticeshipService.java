package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.ApprenticeshipApply;

public interface ApprenticeshipService extends IService<ApprenticeshipApply> {
    /**
     * 提交拜师申请
     * @param studentId 学生ID
     * @param masterId 师父ID
     * @param content 申请内容
     * @return 申请记录
     */
    ApprenticeshipApply submitApply(Long studentId, Long masterId, String content);

    /**
     * 提交师承变更申请（由管理员审核）
     * @param studentId 申请人用户ID
     * @param masterId 目标师父用户ID
     * @param content 申请说明
     * @return 申请记录
     */
    ApprenticeshipApply submitMasterChangeApply(Long studentId, Long masterId, String content);

    /**
     * 徒弟提交解除师徒关系申请（由当前师父审核）
     * @param studentId 徒弟用户ID
     * @param content 申请说明
     * @return 申请记录
     */
    ApprenticeshipApply submitUnbindApply(Long studentId, String content);

    /**
     * 审核申请
     * @param applyId 申请ID
     * @param mentorId 审核人ID（师父）
     * @param pass 是否通过
     */
    void auditApply(Long applyId, Long mentorId, boolean pass);

    /**
     * 管理员审核师承变更申请
     * @param applyId 申请ID
     * @param pass 是否通过
     */
    void auditMasterChangeApply(Long applyId, boolean pass);

    /**
     * 师父审核解除师徒关系申请
     * @param applyId 申请ID
     * @param masterId 当前师父用户ID
     * @param pass 是否同意解除
     */
    void auditUnbindApply(Long applyId, Long masterId, boolean pass);

    /**
     * 师父直接解除与徒弟的关系
     * @param masterId 师父用户ID
     * @param studentId 徒弟用户ID
     */
    void terminateRelation(Long masterId, Long studentId);

    /**
     * 获取我的师父信息
     * @param studentId 学生ID
     * @return 师父的传承人档案（如果存在）
     */
    com.lqx.opera.entity.InheritorProfile getMyMaster(Long studentId);

    /**
     * 获取我的徒弟列表
     * @param masterId 师父ID
     * @return 徒弟列表信息
     */
    java.util.List<com.lqx.opera.common.dto.ApprenticeInfoDTO> getMyApprentices(Long masterId);

    /**
     * 分页查询所有师承关系 (管理员用)
     * @param page 分页参数
     * @return 丰富信息的列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.lqx.opera.common.dto.AdminLineageDTO> getLineagePage(com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.lqx.opera.entity.ApprenticeshipRelation> page);

    /**
     * 删除师承关系
     * @param id 关系ID
     * @return 是否成功
     */
    boolean deleteRelation(Long id);

    /**
     * 获取我的待审核师承变更申请
     * @param studentId 申请人用户ID
     * @return 申请记录
     */
    ApprenticeshipApply getPendingMasterChangeApply(Long studentId);

    /**
     * 获取我的待审核解除申请
     * @param studentId 徒弟用户ID
     * @return 申请记录
     */
    ApprenticeshipApply getPendingUnbindApply(Long studentId);
}
