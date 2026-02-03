package com.lqx.opera.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphNodeDto {
    private String id;
    private String name;
    private String symbol;
    private String category;
    private String genre;
    private String intro; // artisticCareer
    private Boolean isTarget;
    private Long userId;
}
