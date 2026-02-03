package com.lqx.opera.dto;

import com.lqx.opera.entity.PerformanceEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventDetailDTO extends PerformanceEvent {
    private String publisherName;
    private String publisherAvatar;
    private Integer publisherRole;
}
