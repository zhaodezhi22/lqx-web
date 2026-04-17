package com.lqx.opera.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImFriendRequestDto {
    private Long id;
    private Long fromId;
    private String fromName; // realName or username
    private String fromAvatar;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
}
