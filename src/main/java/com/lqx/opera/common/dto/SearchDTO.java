package com.lqx.opera.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SearchDTO {
    private String id;
    private String title;
    private String type; // "product", "event", "resource"
    private String cover;
    private String subTitle;
    private BigDecimal price;
    private Long originalId; // To keep the original Long ID if needed
}
