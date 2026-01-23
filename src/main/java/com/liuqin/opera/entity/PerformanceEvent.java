package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private String title;
    private String venue;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime showTime;
    
    private BigDecimal ticketPrice;
    private Integer totalSeats;
    private String seatLayoutJson;
    private Integer status;
    private Long publisherId;
}

