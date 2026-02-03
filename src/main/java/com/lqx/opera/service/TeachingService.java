package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.ApprenticeTask;
import com.lqx.opera.entity.TaskAssignment;

import java.util.List;

public interface TeachingService extends IService<ApprenticeTask> {
    
    /**
     * Publish a task to multiple students
     */
    void publishTask(Long masterId, String title, String description, String videoUrl, List<Long> studentIds);

    /**
     * Get tasks published by master
     */
    List<ApprenticeTask> getMasterTasks(Long masterId);

    /**
     * Get submissions for a specific task
     */
    List<TaskAssignmentDto> getTaskSubmissions(Long taskId);

    /**
     * Review a submission
     */
    void reviewSubmission(Long masterId, Long assignmentId, String comment, Integer score);

    /**
     * Get comments for a submission
     */
    List<CommentDto> getAssignmentComments(Long assignmentId);

    /**
     * Get assignments for a student
     */
    List<StudentAssignmentDto> getStudentAssignments(Long studentId);

    /**
     * Submit a task
     */
    void submitTask(Long studentId, Long assignmentId, String content, String videoUrl);
    
    @lombok.Data
    class CommentDto {
        private Long commentId;
        private Long userId;
        private String userName;
        private String userAvatar;
        private String content;
        private Integer isOfficial;
        private String createdTime;
    }

    // DTO for submission list
    @lombok.Data
    class TaskAssignmentDto {
        private Long id; // assignment id
        private Long studentId;
        private String studentName;
        private String studentAvatar;
        private Integer status;
        private String submissionVideoUrl;
        private String submissionContent;
        private java.time.LocalDateTime submitTime;
        private java.time.LocalDateTime reviewTime;
        
        // Latest review (optional, or list)
        // For simple list, maybe just status. Detail view will fetch comments.
    }

    @lombok.Data
    class StudentAssignmentDto {
        private Long assignmentId;
        private Long taskId;
        private String taskTitle;
        private String taskDescription;
        private String demoVideoUrl;
        private Long masterId;
        private String masterName;
        private Integer status;
        private String submissionContent;
        private String submissionVideoUrl;
        private java.time.LocalDateTime submitTime;
        private java.time.LocalDateTime reviewTime;
    }
}
