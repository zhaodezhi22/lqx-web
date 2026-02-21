package com.lqx.opera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("task_assignment")
public class TaskAssignment implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long studentId;
    
    // 0-Assigned (Pending), 1-Submitted, 2-Reviewed
    private Integer status;
    
    private String submissionVideoUrl;
    private String submissionContent;
    
    private LocalDateTime submitTime;
    
    private Integer score;
    private String reviewContent;

    private LocalDateTime reviewTime;
    
    // Helper fields (not in DB usually, but for joining? No, let's keep entity simple)
}
