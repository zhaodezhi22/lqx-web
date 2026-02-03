package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.PostDetailDto;
import com.lqx.opera.entity.CommunityPost;

import java.util.List;

public interface CommunityService extends IService<CommunityPost> {
    /**
     * 发布帖子
     */
    boolean createPost(Long userId, String content, List<String> imageUrls);

    /**
     * 获取帖子列表
     */
    Page<PostDetailDto> getPostList(Page<CommunityPost> page, Long currentUserId);

    /**
     * 点赞/取消点赞
     */
    boolean toggleLike(Long userId, Long postId);

    /**
     * 获取帖子详情
     */
    PostDetailDto getPostDetail(Long postId, Long currentUserId);
}
