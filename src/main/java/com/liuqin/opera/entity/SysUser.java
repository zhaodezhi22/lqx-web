package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
    private LocalDateTime createdTime;
}

