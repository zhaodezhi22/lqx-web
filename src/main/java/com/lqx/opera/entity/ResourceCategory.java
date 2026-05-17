package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("resource_category")
public class ResourceCategory implements Serializable {

    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    private String name;

    private Integer sortOrder;

    private LocalDateTime createdTime;
}
