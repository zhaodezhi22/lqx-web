package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.ApprenticeTask;
import com.lqx.opera.entity.ApprenticeshipApply;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.ApprenticeshipService;
import com.lqx.opera.service.CommunityCommentService;
import com.lqx.opera.service.SysUserService;
import com.lqx.opera.service.TeachingService;
import com.lqx.opera.service.TeachingService.TaskAssignmentDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teaching")
public class TeachingController {

    private final TeachingService teachingService;
    private final ApprenticeshipService apprenticeshipService;
    private final SysUserService sysUserService;
    private final CommunityCommentService communityCommentService;

    public TeachingController(TeachingService teachingService,
                              ApprenticeshipService apprenticeshipService,
                              SysUserService sysUserService,
                              CommunityCommentService communityCommentService) {
        this.teachingService = teachingService;
        this.apprenticeshipService = apprenticeshipService;
        this.sysUserService = sysUserService;
        this.communityCommentService = communityCommentService;
    }

    /**
     * 获取师父发布的任务列表
     */
    @GetMapping("/tasks")
    @RequireRole(1)
    public Result<List<ApprenticeTask>> getTasks(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        return Result.success(teachingService.getMasterTasks(userId));
    }

    /**
     * 获取我的徒弟列表 (用于发布任务)
     */
    @GetMapping("/my-apprentices")
    @RequireRole(1)
    public Result<List<ApprenticeDTO>> getMyApprentices(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        // 查询状态为1 (已通过) 的申请记录
        List<ApprenticeshipApply> list = apprenticeshipService.list(new LambdaQueryWrapper<ApprenticeshipApply>()
                .eq(ApprenticeshipApply::getMasterId, userId)
                .eq(ApprenticeshipApply::getStatus, 1));

        List<ApprenticeDTO> dtos = list.stream().map(apply -> {
            ApprenticeDTO dto = new ApprenticeDTO();
            dto.setStudentId(apply.getStudentId());
            SysUser student = sysUserService.getById(apply.getStudentId());
            if (student != null) {
                dto.setName(student.getRealName() != null ? student.getRealName() : student.getUsername());
                dto.setAvatar(student.getAvatar());
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.success(dtos);
    }

    /**
     * 发布新任务
     */
    @PostMapping("/task")
    @RequireRole(1)
    public Result<Boolean> publishTask(@RequestBody PublishTaskRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        try {
            teachingService.publishTask(userId, req.getTitle(), req.getDescription(), req.getVideoUrl(), req.getStudentIds());
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取任务的提交情况
     */
    @GetMapping("/task/{id}/submissions")
    @RequireRole(1)
    public Result<List<TaskAssignmentDto>> getSubmissions(@PathVariable Long id) {
        return Result.success(teachingService.getTaskSubmissions(id));
    }

    /**
     * 点评作业
     */
    @PostMapping("/review")
    @RequireRole(1)
    public Result<Boolean> review(@RequestBody ReviewRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");

        try {
            teachingService.reviewSubmission(userId, req.getAssignmentId(), req.getComment(), req.getScore());
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 获取作业的评论列表
     */
    @GetMapping("/assignment/{id}/comments")
    public Result<List<CommunityComment>> getComments(@PathVariable Long id) {
        List<CommunityComment> comments = communityCommentService.list(new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getTargetId, id)
                .eq(CommunityComment::getTargetType, 5) // 5 = Task Assignment
                .orderByDesc(CommunityComment::getCreatedTime));
        return Result.success(comments);
    }

    /**
     * 获取我的作业列表 (学生视角)
     */
    @GetMapping("/my-assignments")
    public Result<List<TeachingService.StudentAssignmentDto>> getMyAssignments(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        return Result.success(teachingService.getStudentAssignments(userId));
    }

    /**
     * 提交作业
     */
    @PostMapping("/submit")
    public Result<Boolean> submitTask(@RequestBody SubmitTaskRequest req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.fail(401, "未登录");
        
        try {
            teachingService.submitTask(userId, req.getAssignmentId(), req.getContent(), req.getVideoUrl());
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // DTOs
    @Data
    public static class ApprenticeDTO {
        private Long studentId;
        private String name;
        private String avatar;
    }

    @Data
    public static class PublishTaskRequest {
        private String title;
        private String description;
        private String videoUrl;
        private List<Long> studentIds;
    }

    @Data
    public static class ReviewRequest {
        private Long assignmentId;
        private String comment;
        private Integer score;
    }

    @Data
    public static class SubmitTaskRequest {
        private Long assignmentId;
        private String content;
        private String videoUrl;
    }
}
