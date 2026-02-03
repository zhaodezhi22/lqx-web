package com.lqx.opera.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class LineageTreeNodeDto {
    private Long id;
    private String name;
    private String level;
    private Boolean isTarget;
    private List<LineageTreeNodeDto> children;
}
