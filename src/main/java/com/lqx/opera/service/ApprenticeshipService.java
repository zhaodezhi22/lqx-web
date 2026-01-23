package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.ApprenticeshipApply;

public interface ApprenticeshipService extends IService<ApprenticeshipApply> {
    void submitApply(Long studentId, Long masterId, String content);
    void auditApply(Long applyId, Integer auditStatus, Long currentUserId);
}
