package com.lqx.opera.controller;

import com.lqx.opera.common.Result;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.service.CommunityCommentService;
import com.lqx.opera.service.PointsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/community/comment")
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;
    private final PointsService pointsService;

    public CommunityCommentController(CommunityCommentService communityCommentService, PointsService pointsService) {
        this.communityCommentService = communityCommentService;
        this.pointsService = pointsService;
    }

    @PostMapping("/add")
    public Result<Boolean> addComment(@RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }

        CommunityComment comment = new CommunityComment();
        comment.setUserId(userId);
        comment.setTargetId(request.getTargetId());
        comment.setTargetType(request.getTargetType());
        comment.setContent(request.getContent());
        comment.setCreatedTime(LocalDateTime.now());

        boolean success = communityCommentService.save(comment);
        if (success) {
            pointsService.recordDailyAction(userId, "社区活跃", 3, 10);
        }
        return success ? Result.success(true) : Result.fail("评论失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteComment(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        
        CommunityComment comment = communityCommentService.getById(id);
        if (comment == null) {
            return Result.fail(404, "评论不存在");
        }
        
        if (!comment.getUserId().equals(userId)) {
            // Check if user is admin (role 2 or 3) - Optional, but requirement says "user can delete own"
            // We can also allow admin to delete. For now, strict to owner or admin.
            // But we don't have role info in attribute easily unless we decode again or passed in.
            // Usually jwt interceptor sets userId.
            // Let's stick to owner check for now as requested "delete own".
            return Result.fail(403, "无权删除");
        }

        boolean success = communityCommentService.removeById(id);
        return success ? Result.success(true) : Result.fail("删除失败");
    }

    @Data
    public static class CommentRequest {
        private Long targetId;
        private Integer targetType;
        private String content;
    }
}
