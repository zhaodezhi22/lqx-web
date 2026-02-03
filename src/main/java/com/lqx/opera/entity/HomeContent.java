package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("home_content")
public class HomeContent {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String type; // HOME_CAROUSEL, NEWS_CAROUSEL, NOTICE, HIGHLIGHT
    private String title;
    private String subtitle;
    private String content;
    
    @TableField("image_url")
    private String imageUrl;
    
    @TableField("link_url")
    private String linkUrl;
    
    @TableField("sort_order")
    private Integer sortOrder;
    
    private Integer status; // 1=Active, 0=Disabled
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
