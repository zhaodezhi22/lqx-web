package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product implements Serializable {

    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;
    private String name;
    private String subTitle;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String detail;
    private Long sellerId;
    private LocalDateTime createdTime;
}

