package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("inheritor_level_apply")
public class InheritorLevelApply implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String currentLevel;

    private String applyLevel;

    private String reason;

    private String proofMaterials; // JSON array of URLs

    private Integer status; // 0-审核中, 1-通过, 2-拒绝

    private String auditRemark;

    private LocalDateTime createTime;

    private LocalDateTime auditTime;
}
