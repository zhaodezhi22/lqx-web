package com.liuqin.opera.common.dto;

import lombok.Data;

@Data
public class AuditApplyRequest {
    private Long applyId;
    private Boolean pass;
}
