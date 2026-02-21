package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.common.dto.PostDetailDto;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.entity.CommunityLike;
import com.lqx.opera.entity.CommunityPost;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.CommunityLikeMapper;
import com.lqx.opera.mapper.CommunityPostMapper;
import com.lqx.opera.service.CommunityCommentService;
import com.lqx.opera.service.CommunityService;
import com.lqx.opera.service.PointsService;
import com.lqx.opera.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunityServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityService {

    private final CommunityLikeMapper communityLikeMapper;
    private final SysUserService sysUserService;
    private final CommunityCommentService communityCommentService;
    private final PointsService pointsService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public CommunityServiceImpl(CommunityLikeMapper communityLikeMapper, 
                                SysUserService sysUserService, 
                                CommunityCommentService communityCommentService,
                                PointsService pointsService) {
        this.communityLikeMapper = communityLikeMapper;
        this.sysUserService = sysUserService;
        this.communityCommentService = communityCommentService;
        this.pointsService = pointsService;
    }

    @Override
    public boolean createPost(Long userId, String content, List<String> imageUrls) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setContent(content);
        post.setCreatedTime(LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setStatus(1); // Auto-publish (1-Approved) for Post-moderation flow

        try {
            if (imageUrls != null && !imageUrls.isEmpty()) {
                post.setImages(objectMapper.writeValueAsString(imageUrls));
            } else {
                post.setImages("[]");
            }
        } catch (JsonProcessingException e) {
            log.error("JSON serialization failed", e);
            post.setImages("[]");
        }

        boolean saved = this.save(post);
        if (saved) {
            pointsService.recordDailyAction(userId, "社区活跃", 3, 10);
        }
        return saved;
    }

    @Override
    public Page<PostDetailDto> getPostList(Page<CommunityPost> page, Long currentUserId) {
        // 1. Query posts
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<>();
        
        // Show Approved (1) OR My Posts (if logged in)
        // If strict pre-moderation:
        wrapper.and(w -> {
            w.eq(CommunityPost::getStatus, 1);
            if (currentUserId != null) {
                w.or().eq(CommunityPost::getUserId, currentUserId);
            }
        });

        wrapper.orderByDesc(CommunityPost::getCreatedTime);
        Page<CommunityPost> postPage = this.page(page, wrapper);
        
        List<CommunityPost> records = postPage.getRecords();
        if (records.isEmpty()) {
            return new Page<PostDetailDto>(page.getCurrent(), page.getSize(), page.getTotal());
        }

        // 2. Prepare user info map
        Set<Long> userIds = records.stream().map(CommunityPost::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserService.listByIds(userIds);
            if (users != null) {
                userMap = users.stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
            }
        }

        // 3. Prepare liked status map
        Set<Long> likedPostIds = new HashSet<>();
        if (currentUserId != null) {
            List<Long> postIds = records.stream().map(CommunityPost::getPostId).collect(Collectors.toList());
            if (!postIds.isEmpty()) {
                LambdaQueryWrapper<CommunityLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(CommunityLike::getUserId, currentUserId)
                           .eq(CommunityLike::getTargetType, 1) // 1 for post
                           .in(CommunityLike::getTargetId, postIds);
                List<CommunityLike> likes = communityLikeMapper.selectList(likeWrapper);
                likedPostIds = likes.stream().map(CommunityLike::getTargetId).collect(Collectors.toSet());
            }
        }

        // 4. Convert to DTO
        List<PostDetailDto> dtos = new ArrayList<>();
        for (CommunityPost post : records) {
            PostDetailDto dto = new PostDetailDto();
            dto.setPostId(post.getPostId());
            dto.setUserId(post.getUserId());
            dto.setContent(post.getContent());
            dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setStatus(post.getStatus());
        dto.setCreatedTime(post.getCreatedTime() != null ? post.getCreatedTime().format(dateFormatter) : "");
            
            // User Info
            SysUser user = userMap.get(post.getUserId());
            if (user != null) {
                dto.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            } else {
                dto.setUserName("未知用户");
            }

            // Images
            try {
                if (post.getImages() != null && !post.getImages().isEmpty()) {
                    List<String> images = objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {});
                    dto.setImages(images);
                } else {
                    dto.setImages(Collections.emptyList());
                }
            } catch (JsonProcessingException e) {
                dto.setImages(Collections.emptyList());
            }

            // Is Liked
            dto.setIsLiked(likedPostIds.contains(post.getPostId()));
            
            dtos.add(dto);
        }

        Page<PostDetailDto> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(dtos);
        return resultPage;
    }

    @Override
    public PostDetailDto getPostDetail(Long postId, Long currentUserId) {
        // 1. Get Post
        CommunityPost post = this.getById(postId);
        if (post == null) return null;

        // 2. Increment View Count (async or simple update)
        post.setViewCount(post.getViewCount() + 1);
        this.updateById(post);

        PostDetailDto dto = new PostDetailDto();
        dto.setPostId(post.getPostId());
        dto.setUserId(post.getUserId());
        dto.setContent(post.getContent());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setStatus(post.getStatus());
        dto.setCreatedTime(post.getCreatedTime() != null ? post.getCreatedTime().format(dateFormatter) : "");

        // Images
        try {
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                List<String> images = objectMapper.readValue(post.getImages(), new TypeReference<List<String>>() {});
                dto.setImages(images);
            } else {
                dto.setImages(Collections.emptyList());
            }
        } catch (JsonProcessingException e) {
            dto.setImages(Collections.emptyList());
        }

        // 3. Get Author Info
        SysUser author = sysUserService.getById(post.getUserId());
        if (author != null) {
            dto.setUserName(author.getRealName() != null ? author.getRealName() : author.getUsername());
            dto.setUserAvatar(author.getAvatar());
        } else {
            dto.setUserName("未知用户");
        }

        // 4. Check Like Status
        if (currentUserId != null) {
            LambdaQueryWrapper<CommunityLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(CommunityLike::getUserId, currentUserId)
                       .eq(CommunityLike::getTargetType, 1)
                       .eq(CommunityLike::getTargetId, postId);
            dto.setIsLiked(communityLikeMapper.selectCount(likeWrapper) > 0);
        } else {
            dto.setIsLiked(false);
        }

        // 5. Get Comments (target_type=3 for Post Comments, 4 for Replies)
        // 5.1 Get Level 1 Comments
        List<CommunityComment> l1Comments = communityCommentService.list(new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getTargetId, postId)
                .eq(CommunityComment::getTargetType, 3)
                .orderByDesc(CommunityComment::getCreatedTime));

        List<PostDetailDto.CommentDto> commentDtos = new ArrayList<>();
        if (!l1Comments.isEmpty()) {
            Set<Long> l1Ids = l1Comments.stream().map(CommunityComment::getCommentId).collect(Collectors.toSet());
            
            // 5.2 Get Level 2 Comments (Replies)
            List<CommunityComment> l2Comments = Collections.emptyList();
            if (!l1Ids.isEmpty()) {
                l2Comments = communityCommentService.list(new LambdaQueryWrapper<CommunityComment>()
                    .in(CommunityComment::getTargetId, l1Ids)
                    .eq(CommunityComment::getTargetType, 4)
                    .orderByAsc(CommunityComment::getCreatedTime));
            }

            // 5.3 Collect User IDs
            Set<Long> userIds = new HashSet<>();
            l1Comments.forEach(c -> userIds.add(c.getUserId()));
            l2Comments.forEach(c -> userIds.add(c.getUserId()));
            
            Map<Long, SysUser> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserService.listByIds(userIds);
                if (users != null) {
                    userMap = users.stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
                }
            }

            // 5.4 Prepare Helper
            // Helper to convert to DTO
            java.util.function.BiFunction<CommunityComment, Map<Long, SysUser>, PostDetailDto.CommentDto> converter = (c, uMap) -> {
                PostDetailDto.CommentDto cDto = new PostDetailDto.CommentDto();
                cDto.setCommentId(c.getCommentId());
                cDto.setUserId(c.getUserId());
                cDto.setContent(c.getContent());
                cDto.setCreatedTime(c.getCreatedTime() != null ? c.getCreatedTime().format(dateFormatter) : "");
                SysUser u = uMap.get(c.getUserId());
                if (u != null) {
                    cDto.setUserName(u.getRealName() != null ? u.getRealName() : u.getUsername());
                    cDto.setUserAvatar(u.getAvatar());
                } else {
                    cDto.setUserName("匿名用户");
                }
                return cDto;
            };

            // 5.5 Map L2 to L1
            Map<Long, List<PostDetailDto.CommentDto>> repliesMap = new HashMap<>();
            for (CommunityComment c : l2Comments) {
                PostDetailDto.CommentDto replyDto = converter.apply(c, userMap);
                repliesMap.computeIfAbsent(c.getTargetId(), k -> new ArrayList<>()).add(replyDto);
            }

            // 5.6 Assemble L1
            for (CommunityComment c : l1Comments) {
                PostDetailDto.CommentDto commentDto = converter.apply(c, userMap);
                commentDto.setReplies(repliesMap.getOrDefault(c.getCommentId(), new ArrayList<>()));
                commentDtos.add(commentDto);
            }
        }
        dto.setComments(commentDtos);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long userId, Long postId) {
        // 1. Check if already liked
        LambdaQueryWrapper<CommunityLike> query = new LambdaQueryWrapper<>();
        query.eq(CommunityLike::getUserId, userId)
             .eq(CommunityLike::getTargetId, postId)
             .eq(CommunityLike::getTargetType, 1);
        
        CommunityLike existingLike = communityLikeMapper.selectOne(query);
        CommunityPost post = this.getById(postId);
        if (post == null) return false;

        if (existingLike != null) {
            // Cancel like
            communityLikeMapper.deleteById(existingLike.getId());
            if (post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
                this.updateById(post);
            }
            return false; // Liked -> Unliked
        } else {
            // Add like
            CommunityLike newLike = new CommunityLike();
            newLike.setUserId(userId);
            newLike.setTargetId(postId);
            newLike.setTargetType(1);
            communityLikeMapper.insert(newLike);
            
            post.setLikeCount(post.getLikeCount() + 1);
            this.updateById(post);
            return true; // Unliked -> Liked
        }
    }
}
