package com.lqx.opera.common.dto;

import com.lqx.opera.entity.TicketOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketOrderDetailDto extends TicketOrder {
    private String eventTitle;
    private String eventVenue;
    private LocalDateTime showTime;
    private String eventCover; // Optional, if we want to show image
}
