package com.lqx.opera.common.dto;

import com.lqx.opera.entity.Product;
import com.lqx.opera.entity.SysUser;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
public class ProductDetailDTO {
    // Product Info
    private Product product;
    
    // Seller Info
    private SellerInfo seller;
    
    // Comments
    private List<CommentInfo> comments;

    @Data
    public static class SellerInfo {
        private Long userId;
        private String nickname;
        private String avatar;
    }

    @Data
    public static class CommentInfo {
        private Long commentId;
        private String content;
        private LocalDateTime createdTime;
        private UserInfo user;
    }

    @Data
    public static class UserInfo {
        private Long userId;
        private String nickname;
        private String avatar;
    }
}
