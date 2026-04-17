package com.lqx.opera.common.dto;

import lombok.Data;

@Data
public class ImFriendDto {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String remark;
}
