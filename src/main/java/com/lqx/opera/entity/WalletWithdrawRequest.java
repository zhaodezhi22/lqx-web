package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_withdraw_request")
public class WalletWithdrawRequest implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("user_role")
    private Integer userRole;

    @TableField("source_type")
    private String sourceType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("account_name")
    private String accountName;

    @TableField("account_no")
    private String accountNo;

    @TableField("remark")
    private String remark;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("processed_time")
    private LocalDateTime processedTime;
}
