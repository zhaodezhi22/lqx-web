package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin_operation_log")
public class AdminOperationLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long operatorId;

    private String operatorName;

    private Integer operatorRole;

    private String requestMethod;

    private String requestPath;

    private String requestQuery;

    private String actionName;

    private Integer responseStatus;

    private Integer successFlag;

    private LocalDateTime createdTime;
}
