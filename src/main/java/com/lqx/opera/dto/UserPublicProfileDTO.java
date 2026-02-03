package com.lqx.opera.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserPublicProfileDTO {
    private Long userId;
    private String nickname; // realName or username
    private String avatar;
    private Integer role;
    
    // Inheritor specific
    private String level;
    private String genre;
    private String artisticCareer;
    private String awards;
    private Integer verifyStatus;
}
