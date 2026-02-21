package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("community_comment")
public class CommunityComment implements Serializable {

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;
    private Long userId;
    private Long targetId;
    private Integer targetType;
    private String content;
    private Integer isOfficial; // 0-Normal, 1-Official/Master
    private Integer status; // 0-Normal, 1-Reported, 2-Hidden
    private LocalDateTime createdTime;
}

