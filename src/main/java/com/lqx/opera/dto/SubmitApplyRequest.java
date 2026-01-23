package com.lqx.opera.dto;

import lombok.Data;

@Data
public class SubmitApplyRequest {
    private Long studentId;
    private Long masterId;
    private String content;
}
