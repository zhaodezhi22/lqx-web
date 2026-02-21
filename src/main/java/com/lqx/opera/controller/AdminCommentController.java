package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.CommunityCommentMapper;
import com.lqx.opera.service.SysUserService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final CommunityCommentMapper commentMapper;
    private final SysUserService sysUserService;

    public AdminCommentController(CommunityCommentMapper commentMapper, SysUserService sysUserService) {
        this.commentMapper = commentMapper;
        this.sysUserService = sysUserService;
    }

    /**
     * 评论审核列表
     * GET /api/admin/comments?status=pending (or reported)
     */
    @GetMapping
    @RequireRole({2, 3})
    public Result<List<CommentAuditDto>> list(@RequestParam(required = false, defaultValue = "pending") String status) {
        // Map status string to integer
        // pending -> 0? Or maybe we define 0 as Normal (Approved)?
        // Let's say: 0=Normal, 1=Reported, 3=Pending Audit (if pre-audit).
        // User context implies "Comment Audit" page.
        // If comments are auto-published (status 0), then audit page usually shows Reported ones.
        // But prompt says "status=pending". Maybe we assume comments need audit?
        // Let's assume: 0 = Normal/Pending Audit (if configured), 1 = Reported, 2 = Hidden/Blocked.
        // For this task, I'll return:
        // if status='pending' -> status=0 (Assuming we want to audit all new comments or just assume 0 is pending)
        // Actually, usually 0 is published.
        // Let's assume:
        // 0: Published (Normal)
        // 1: Reported (Needs Audit)
        // 2: Hidden (Blocked)
        // 3: Pending (If pre-moderation)
        
        // Given the UI has "Pending" and "Reported":
        // I will map 'pending' to 0 (Just checking latest comments) or maybe 3.
        // Let's stick to: 'pending' -> query status=0 (Normal/Published, but maybe we want to review them).
        // 'reported' -> query status=1.
        
        Integer statusObj = "reported".equals(status) ? 1 : 0;
        
        LambdaQueryWrapper<CommunityComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityComment::getStatus, statusObj);
        wrapper.orderByDesc(CommunityComment::getCreatedTime);
        wrapper.last("LIMIT 100"); 

        List<CommunityComment> list = commentMapper.selectList(wrapper);
        
        // Enrich with User info
        List<Long> userIds = list.stream().map(CommunityComment::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        List<CommentAuditDto> dtos = list.stream().map(c -> {
            CommentAuditDto dto = new CommentAuditDto();
            dto.setId(c.getCommentId());
            dto.setContent(c.getContent());
            SysUser u = userMap.get(c.getUserId());
            dto.setUser(u != null ? (u.getRealName() != null ? u.getRealName() : u.getUsername()) : "Unknown");
            dto.setTime(c.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            return dto;
        }).collect(Collectors.toList());

        return Result.success(dtos);
    }

    /**
     * 审核/操作评论
     * PUT /api/admin/comments/{id}/audit
     * Body: { "action": "pass" | "block" }
     */
    @PutMapping("/{id}/audit")
    @RequireRole({2, 3})
    public Result<Boolean> audit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.get("action");
        CommunityComment comment = commentMapper.selectById(id);
        if (comment == null) return Result.fail("评论不存在");

        if ("pass".equals(action)) {
            comment.setStatus(0); // Normal
        } else if ("block".equals(action) || "reject".equals(action)) {
            comment.setStatus(2); // Hidden
        } else {
            return Result.fail("无效操作");
        }
        commentMapper.updateById(comment);
        return Result.success(true);
    }

    @Data
    public static class CommentAuditDto {
        private Long id;
        private String content;
        private String user;
        private String time;
    }
}
