package com.lqx.opera.common.dto;

import com.lqx.opera.entity.MallOrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MallOrderItemDto extends MallOrderItem {
    private String productImage;
}
