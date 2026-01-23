package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("heritage_resource")
public class HeritageResource implements Serializable {

    @TableId(value = "resource_id", type = IdType.AUTO)
    private Long resourceId;
    private String title;
    private Integer type;
    private String category;
    private String coverImg;
    private String fileUrl;
    private Long uploaderId;
    private String description;
    private Integer viewCount;
    private LocalDateTime createdTime;
}

