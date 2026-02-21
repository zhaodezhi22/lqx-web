package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("apprentice_task")
public class ApprenticeTask implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long masterId;
    private String title;
    private String description;
    private String demoVideoUrl;
    private LocalDateTime createdTime;
}
