package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mall_order")
public class MallOrder implements Serializable {
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status; // 0-待支付, 1-已支付待发货, 2-已发货, 3-已完成, 4-已取消
    private String logisticsNo;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime createdTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<MallOrderItem> items;
}

