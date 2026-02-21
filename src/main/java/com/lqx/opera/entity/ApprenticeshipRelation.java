package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("apprenticeship_relation")
public class ApprenticeshipRelation implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long masterId;
    private Long studentId;
    private Integer relationStatus; // 1-Teaching, 2-Graduated, 3-Terminated
    private LocalDateTime createTime;
}
