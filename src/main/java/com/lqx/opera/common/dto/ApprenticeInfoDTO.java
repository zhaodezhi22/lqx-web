package com.lqx.opera.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApprenticeInfoDTO {
    private Long studentId;
    private String realName;
    private String username;
    private String phone;
    private String avatar;
    private String level; // 传承等级
    private Integer relationStatus; // 1-Teaching, 2-Graduated
    private LocalDateTime joinTime;
}
