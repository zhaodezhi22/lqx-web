package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("im_friend_request")
public class ImFriendRequest implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long fromId;

    private Long toId;

    private String reason;

    private Integer status; // 0待处理, 1已通过, 2已拒绝

    private LocalDateTime createTime;
}
