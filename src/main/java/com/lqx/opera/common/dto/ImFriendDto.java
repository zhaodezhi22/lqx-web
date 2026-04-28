package com.lqx.opera.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImFriendDto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String remark;
    private String lastMessage;
    private Integer lastMessageType;
    private Integer lastMessageIsRecalled;
    private LocalDateTime lastMessageTime;
}
