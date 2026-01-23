package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("points_log")
public class PointsLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changePoint;
    private String reason;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;
}
