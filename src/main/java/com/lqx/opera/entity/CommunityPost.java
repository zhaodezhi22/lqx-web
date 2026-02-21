package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_post")
public class CommunityPost {
    @TableId(type = IdType.AUTO)
    private Long postId;
    private Long userId;
    private String title;
    private String content;
    private String images; // JSON string
    private Integer viewCount;
    private Integer likeCount;
    private Integer status; // 0-Pending, 1-Approved, 2-Rejected
    private LocalDateTime createdTime;
}
