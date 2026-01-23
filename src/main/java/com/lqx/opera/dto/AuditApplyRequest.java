package com.lqx.opera.dto;

import lombok.Data;

@Data
public class AuditApplyRequest {
    private Long applyId;
    private Integer auditStatus; // 1-通过, 2-拒绝
    private Long currentUserId;
}
