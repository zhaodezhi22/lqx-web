package com.lqx.opera.common.dto;

import com.lqx.opera.entity.SysUser;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private SysUser user;
}

