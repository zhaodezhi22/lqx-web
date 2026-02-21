package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("apprenticeship_apply")
public class ApprenticeshipApply implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long masterId;
    private String applyContent;
    private Integer status; // 0-审核中 1-通过 2-拒绝
}
