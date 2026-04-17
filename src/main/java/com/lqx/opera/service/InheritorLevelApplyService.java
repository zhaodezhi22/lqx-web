package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.InheritorLevelApply;

import java.util.List;

public interface InheritorLevelApplyService extends IService<InheritorLevelApply> {

    /**
     * 提交等级变更申请
     */
    void submitApply(Long userId, String currentLevel, String applyLevel, String reason, String proofMaterials);

    /**
     * 获取用户最新的申请记录
     */
    InheritorLevelApply getLatestApply(Long userId);

    /**
     * 审核申请
     */
    void auditApply(Long applyId, Integer status, String remark);

    /**
     * 分页查询申请列表 (Admin)
     */
    Page<InheritorLevelApply> getAuditPage(Page<InheritorLevelApply> page, Integer status);
    
    @lombok.Data
    class LevelApplyDto {
        private Long id;
        private Long userId;
        private String userName;
        private String realName;
        private String currentLevel;
        private String applyLevel;
        private String reason;
        private String proofMaterials;
        private Integer status;
        private String auditRemark;
        private String createTime;
    }
    
    Page<LevelApplyDto> getAuditDtoPage(Page<InheritorLevelApply> page, Integer status);
}
