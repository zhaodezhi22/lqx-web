package com.lqx.opera.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lqx.opera.common.Result;
import com.lqx.opera.common.dto.PostDetailDto;
import com.lqx.opera.entity.CommunityPost;
import com.lqx.opera.service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/post")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping("/add")
    public Result<Boolean> createPost(@RequestBody PostRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        boolean success = communityService.createPost(userId, request.getContent(), request.getImageUrls());
        return success ? Result.success(true) : Result.fail("发布失败");
    }

    @GetMapping("/list")
    public Result<Page<PostDetailDto>> getPostList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId"); // Can be null if not logged in, but usually interceptor handles it. 
        // If endpoint is public, we might need to handle null userId in service (which we did).
        // Assuming /list might be public, but usually 'userId' attribute is set only if token is valid.
        // If token is missing, userId might be null.
        
        Page<CommunityPost> pageParam = new Page<>(page, size);
        return Result.success(communityService.getPostList(pageParam, userId));
    }

    @GetMapping("/{id}")
    public Result<PostDetailDto> getPostDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PostDetailDto detail = communityService.getPostDetail(id, userId);
        if (detail == null) {
            return Result.fail(404, "帖子不存在");
        }
        return Result.success(detail);
    }

    @PostMapping("/like/{postId}")
    public Result<Boolean> toggleLike(@PathVariable Long postId, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        boolean isLiked = communityService.toggleLike(userId, postId);
        return Result.success(isLiked);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        
        CommunityPost post = communityService.getById(id);
        if (post == null) {
            return Result.fail(404, "帖子不存在");
        }
        
        if (!post.getUserId().equals(userId)) {
            return Result.fail(403, "无权删除");
        }
        
        boolean success = communityService.removeById(id);
        // TODO: Optionally delete related comments and likes
        return success ? Result.success(true) : Result.fail("删除失败");
    }

    @lombok.Data
    public static class PostRequest {
        private String content;
        // Map "images" from JSON to "imageUrls" field, or rename field to "images"
        // Since frontend sends "images", we should either rename this field or add JsonProperty
        // Let's rename for simplicity and consistency with frontend
        private List<String> images;

        public List<String> getImageUrls() {
            return images;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.images = imageUrls;
        }
    }
}
