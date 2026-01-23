package com.liuqin.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liuqin.opera.entity.ApprenticeshipApply;

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
}
