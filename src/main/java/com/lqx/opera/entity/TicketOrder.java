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
@TableName("ticket_order")
public class TicketOrder implements Serializable {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("event_id")
    private Long eventId;

    @TableField("seat_info")
    private String seatInfo;

    @TableField("price")
    private BigDecimal price;

    @TableField("status")
    private Integer status; // 0-待支付 1-已支付
    @TableField("created_time")
    private LocalDateTime createdTime;
}
