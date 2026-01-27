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
     * 审核申请
     * @param applyId 申请ID
     * @param mentorId 审核人ID（师父）
     * @param pass 是否通过
     */
    void auditApply(Long applyId, Long mentorId, boolean pass);

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
}
