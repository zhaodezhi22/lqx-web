package com.lqx.opera.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphResultDto {
    private List<GraphNodeDto> nodes;
    private List<GraphLinkDto> links;
}
