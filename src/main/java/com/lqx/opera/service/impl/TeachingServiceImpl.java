package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.entity.ApprenticeTask;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.entity.TaskAssignment;
import com.lqx.opera.mapper.ApprenticeTaskMapper;
import com.lqx.opera.mapper.CommunityCommentMapper;
import com.lqx.opera.mapper.TaskAssignmentMapper;
import com.lqx.opera.service.SysUserService;
import com.lqx.opera.service.TeachingService;
import com.lqx.opera.service.TeachingService.TaskAssignmentDto;
import com.lqx.opera.service.TeachingService.StudentAssignmentDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeachingServiceImpl extends ServiceImpl<ApprenticeTaskMapper, ApprenticeTask> implements TeachingService {

    private final TaskAssignmentMapper taskAssignmentMapper;
    private final SysUserService sysUserService;
    private final CommunityCommentMapper communityCommentMapper;

    public TeachingServiceImpl(TaskAssignmentMapper taskAssignmentMapper, 
                               SysUserService sysUserService,
                               CommunityCommentMapper communityCommentMapper) {
        this.taskAssignmentMapper = taskAssignmentMapper;
        this.sysUserService = sysUserService;
        this.communityCommentMapper = communityCommentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishTask(Long masterId, String title, String description, String videoUrl, List<Long> studentIds) {
        // 1. Create Task
        ApprenticeTask task = new ApprenticeTask();
        task.setMasterId(masterId);
        task.setTitle(title);
        task.setDescription(description);
        task.setDemoVideoUrl(videoUrl);
        task.setCreatedTime(LocalDateTime.now());
        this.save(task);

        // 2. Assign to students
        if (studentIds != null && !studentIds.isEmpty()) {
            for (Long studentId : studentIds) {
                TaskAssignment assignment = new TaskAssignment();
                assignment.setTaskId(task.getId());
                assignment.setStudentId(studentId);
                assignment.setStatus(0); // Pending
                taskAssignmentMapper.insert(assignment);
            }
        }
    }

    @Override
    public List<ApprenticeTask> getMasterTasks(Long masterId) {
        return this.list(new LambdaQueryWrapper<ApprenticeTask>()
                .eq(ApprenticeTask::getMasterId, masterId)
                .orderByDesc(ApprenticeTask::getCreatedTime));
    }

    @Override
    public List<TaskAssignmentDto> getTaskSubmissions(Long taskId) {
        // 1. Get assignments
        List<TaskAssignment> assignments = taskAssignmentMapper.selectList(new LambdaQueryWrapper<TaskAssignment>()
                .eq(TaskAssignment::getTaskId, taskId));
        
        if (assignments.isEmpty()) return new ArrayList<>();

        // 2. Get Student Info
        List<Long> studentIds = assignments.stream().map(TaskAssignment::getStudentId).collect(Collectors.toList());
        Map<Long, SysUser> studentMap = sysUserService.listByIds(studentIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        // 3. Convert to DTO
        return assignments.stream().map(a -> {
            TaskAssignmentDto dto = new TaskAssignmentDto();
            dto.setId(a.getId());
            dto.setStudentId(a.getStudentId());
            dto.setStatus(a.getStatus());
            dto.setSubmissionVideoUrl(a.getSubmissionVideoUrl());
            dto.setSubmissionContent(a.getSubmissionContent());
            dto.setSubmitTime(a.getSubmitTime());
            dto.setReviewTime(a.getReviewTime());
            
            SysUser s = studentMap.get(a.getStudentId());
            if (s != null) {
                dto.setStudentName(s.getRealName() != null ? s.getRealName() : s.getUsername());
                dto.setStudentAvatar(s.getAvatar());
            } else {
                dto.setStudentName("未知学生");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewSubmission(Long masterId, Long assignmentId, String commentContent, Integer score) {
        TaskAssignment assignment = taskAssignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("作业不存在");
        }
        
        // Verify task ownership
        ApprenticeTask task = this.getById(assignment.getTaskId());
        if (task == null || !task.getMasterId().equals(masterId)) {
            throw new RuntimeException("无权点评此作业");
        }

        if (commentContent == null || commentContent.trim().isEmpty()) {
            throw new RuntimeException("评语不能为空");
        }
        if (score == null) {
            throw new RuntimeException("必须打分");
        }

        // 1. Save Comment
        CommunityComment comment = new CommunityComment();
        comment.setUserId(masterId);
        comment.setTargetId(assignmentId);
        comment.setTargetType(5); // 5 = Task Assignment
        comment.setContent(commentContent);
        comment.setIsOfficial(1); // Master Review
        comment.setCreatedTime(LocalDateTime.now());
        communityCommentMapper.insert(comment);

        // 2. Update Status and Score
        assignment.setStatus(2); // Reviewed
        assignment.setReviewTime(LocalDateTime.now());
        assignment.setScore(score);
        assignment.setReviewContent(commentContent);
        taskAssignmentMapper.updateById(assignment);
    }

    @Override
    public List<TeachingService.CommentDto> getAssignmentComments(Long assignmentId) {
        List<CommunityComment> comments = communityCommentMapper.selectList(new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getTargetId, assignmentId)
                .eq(CommunityComment::getTargetType, 5)
                .orderByDesc(CommunityComment::getIsOfficial) // Official first
                .orderByDesc(CommunityComment::getCreatedTime));

        if (comments.isEmpty()) return new ArrayList<>();

        List<Long> userIds = comments.stream().map(CommunityComment::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        return comments.stream().map(c -> {
            TeachingService.CommentDto dto = new TeachingService.CommentDto();
            dto.setCommentId(c.getCommentId());
            dto.setUserId(c.getUserId());
            dto.setContent(c.getContent());
            dto.setIsOfficial(c.getIsOfficial());
            dto.setCreatedTime(c.getCreatedTime().toString().replace("T", " ")); // Simple format
            
            SysUser u = userMap.get(c.getUserId());
            if (u != null) {
                dto.setUserName(u.getRealName() != null ? u.getRealName() : u.getUsername());
                dto.setUserAvatar(u.getAvatar());
            } else {
                dto.setUserName("未知用户");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StudentAssignmentDto> getStudentAssignments(Long studentId) {
        List<TaskAssignment> assignments = taskAssignmentMapper.selectList(new LambdaQueryWrapper<TaskAssignment>()
                .eq(TaskAssignment::getStudentId, studentId)
                .orderByDesc(TaskAssignment::getId)); // Newest first

        if (assignments.isEmpty()) return new ArrayList<>();

        List<Long> taskIds = assignments.stream().map(TaskAssignment::getTaskId).collect(Collectors.toList());
        Map<Long, ApprenticeTask> taskMap = this.listByIds(taskIds).stream()
                .collect(Collectors.toMap(ApprenticeTask::getId, t -> t));
        
        // Collect master IDs
        List<Long> masterIds = taskMap.values().stream().map(ApprenticeTask::getMasterId).collect(Collectors.toList());
        Map<Long, SysUser> masterMap = sysUserService.listByIds(masterIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        return assignments.stream().map(a -> {
            StudentAssignmentDto dto = new StudentAssignmentDto();
            dto.setAssignmentId(a.getId());
            dto.setTaskId(a.getTaskId());
            dto.setStatus(a.getStatus());
            dto.setSubmissionContent(a.getSubmissionContent());
            dto.setSubmissionVideoUrl(a.getSubmissionVideoUrl());
            dto.setSubmitTime(a.getSubmitTime());
            dto.setReviewTime(a.getReviewTime());

            ApprenticeTask t = taskMap.get(a.getTaskId());
            if (t != null) {
                dto.setTaskTitle(t.getTitle());
                dto.setTaskDescription(t.getDescription());
                dto.setDemoVideoUrl(t.getDemoVideoUrl());
                dto.setMasterId(t.getMasterId());
                
                SysUser m = masterMap.get(t.getMasterId());
                if (m != null) {
                    dto.setMasterName(m.getRealName() != null ? m.getRealName() : m.getUsername());
                } else {
                    dto.setMasterName("未知师傅");
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTask(Long studentId, Long assignmentId, String content, String videoUrl) {
        TaskAssignment assignment = taskAssignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!assignment.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权提交此任务");
        }

        assignment.setSubmissionContent(content);
        assignment.setSubmissionVideoUrl(videoUrl);
        assignment.setStatus(1); // Submitted
        assignment.setSubmitTime(LocalDateTime.now());
        
        taskAssignmentMapper.updateById(assignment);
    }
}
