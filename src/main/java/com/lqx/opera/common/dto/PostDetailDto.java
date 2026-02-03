package com.lqx.opera.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostDetailDto {
    private Long postId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String content;
    private List<String> images;
    private Integer viewCount;
    private Integer likeCount;
    private Integer status; // 0-Pending, 1-Approved, 2-Rejected
    private String createdTime; // Format: yyyy-MM-dd HH:mm
    private Boolean isLiked;
    
    // Comments
    private List<CommentDto> comments;

    @Data
    public static class CommentDto {
        private Long commentId;
        private Long userId;
        private String userName;
        private String userAvatar;
        private String content;
        private String createdTime;
        private List<CommentDto> replies; // 子评论/回复
    }
}
