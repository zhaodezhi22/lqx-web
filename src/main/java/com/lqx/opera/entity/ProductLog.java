package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("product_log")
public class ProductLog implements Serializable {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long productId;
    private String productName; // Store name in case product is deleted
    private Long operatorId;
    private String actionType; // CREATE, UPDATE, DELETE
    private String content; // Details of change
    private LocalDateTime createTime;
}
