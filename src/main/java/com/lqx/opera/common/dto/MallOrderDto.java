package com.lqx.opera.common.dto;

import com.lqx.opera.entity.MallOrderItem;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MallOrderDto {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String addressSnapshot;
    private Integer status;
    private List<MallOrderItem> items;
    private String payTime;
    private String shipTime;
    private String createTime;
}
