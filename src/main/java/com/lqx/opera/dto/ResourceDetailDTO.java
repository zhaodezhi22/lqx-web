package com.lqx.opera.dto;

import com.lqx.opera.entity.HeritageResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceDetailDTO extends HeritageResource {
    private String uploaderName;
    private String uploaderAvatar;
    private Integer uploaderRole;
}
