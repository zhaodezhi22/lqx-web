package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String orderNo;
    private Long userId;
    private Long eventId;
    private String seatInfo;
    private BigDecimal price;
    private Integer status;
    private String qrCode;
    private LocalDateTime payTime;
    private LocalDateTime createdTime;
}

