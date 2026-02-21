package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.annotation.RequireRole;
import com.lqx.opera.entity.CommunityPost;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.service.CommunityService;
import com.lqx.opera.service.SysUserService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/post")
public class AdminPostController {

    private final CommunityService communityService;
    private final SysUserService sysUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminPostController(CommunityService communityService, SysUserService sysUserService) {
        this.communityService = communityService;
        this.sysUserService = sysUserService;
    }

    @GetMapping("/audit/list")
    @RequireRole({2, 3})
    public Result<Page<PostAuditDto>> getAuditList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        
        Page<CommunityPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CommunityPost::getStatus, status);
        }
        wrapper.orderByDesc(CommunityPost::getCreatedTime);
        
        Page<CommunityPost> result = communityService.page(pageParam, wrapper);
        
        if (result.getRecords().isEmpty()) {
            Page<PostAuditDto> emptyPage = new Page<>(page, size, 0);
            emptyPage.setRecords(Collections.emptyList());
            return Result.success(emptyPage);
        }

        // Convert to DTO
        List<Long> userIds = result.getRecords().stream().map(CommunityPost::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> userMap;
        if (!userIds.isEmpty()) {
             userMap = sysUserService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(SysUser::getUserId, u -> u));
        } else {
            userMap = Collections.emptyMap();
        }

        List<PostAuditDto> dtos = result.getRecords().stream().map(p -> {
            PostAuditDto dto = new PostAuditDto();
            dto.setPostId(p.getPostId());
            dto.setTitle(p.getTitle());
            dto.setContent(p.getContent());
            dto.setStatus(p.getStatus());
            dto.setCreatedTime(p.getCreatedTime() != null ? p.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
            
            try {
                if (p.getImages() != null && !p.getImages().isEmpty()) {
                    dto.setImages(objectMapper.readValue(p.getImages(), new TypeReference<List<String>>() {}));
                } else {
                    dto.setImages(Collections.emptyList());
                }
            } catch (Exception e) {
                dto.setImages(Collections.emptyList());
            }

            SysUser u = userMap.get(p.getUserId());
            dto.setUserName(u != null ? (u.getRealName() != null ? u.getRealName() : u.getUsername()) : "Unknown");
            
            return dto;
        }).collect(Collectors.toList());

        Page<PostAuditDto> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(dtos);
        
        return Result.success(dtoPage);
    }

    @PutMapping("/audit/{id}")
    @RequireRole({2, 3})
    public Result<Boolean> auditPost(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            return Result.fail("Invalid status");
        }
        
        CommunityPost post = communityService.getById(id);
        if (post == null) return Result.fail("Post not found");
        
        post.setStatus(status);
        return Result.success(communityService.updateById(post));
    }

    @Data
    public static class PostAuditDto {
        private Long postId;
        private String title;
        private String content;
        private List<String> images;
        private String userName;
        private String createdTime;
        private Integer status;
    }
}
