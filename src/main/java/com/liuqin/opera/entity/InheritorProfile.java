package com.liuqin.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("inheritor_profile")
public class InheritorProfile implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String level;
    private String genre;
    private String masterName;
    private String artisticCareer;
    private String awards;
    private Integer verifyStatus;
}

