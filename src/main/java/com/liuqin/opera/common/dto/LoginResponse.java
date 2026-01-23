package com.liuqin.opera.common.dto;

import com.liuqin.opera.entity.SysUser;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private SysUser user;
}

