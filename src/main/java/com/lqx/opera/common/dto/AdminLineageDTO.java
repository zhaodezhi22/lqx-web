package com.lqx.opera.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminLineageDTO {
    private Long id;
    private Long masterId;
    private String masterName;
    private Long apprenticeId;
    private String apprenticeName;
    private String heritageItem; // From master's genre
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    private Integer status; // Relation status
}
