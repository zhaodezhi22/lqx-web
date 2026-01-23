package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("performance_event")
public class PerformanceEvent implements Serializable {

    @TableId(value = "event_id", type = IdType.AUTO)
    private Long eventId;

    @TableField("title")
    private String title;

    @TableField("venue")
    private String venue;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("show_time")
    private LocalDateTime showTime;
    
    @TableField("ticket_price")
    private BigDecimal ticketPrice;

    @TableField("total_seats")
    private Integer totalSeats;

    @TableField("seat_layout_json")
    private String seatLayoutJson;

    @TableField("status")
    private Integer status;

    @TableField("publisher_id")
    private Long publisherId;
}

