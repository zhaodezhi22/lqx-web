package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("mall_order")
public class MallOrder implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("address_snapshot")
    private String addressSnapshot;

    @TableField("delivery_company")
    private String deliveryCompany;

    @TableField("delivery_no")
    private String deliveryNo;

    @TableField("status")
    private Integer status; // 0-待付 1-已付 2-已发货 3-已取消/退款 4-退款中 5-已退款(Old)

    @TableField("create_time")
    private java.time.LocalDateTime createTime;
}
