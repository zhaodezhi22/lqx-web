package com.liuqin.opera.common.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    /**
     * 0-普通用户, 1-传承人, 2-管理员
     */
    private Integer role = 0;
    private String realName;
    private String phone;
    private String email;
}

