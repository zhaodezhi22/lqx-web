package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.common.dto.ProductDetailDTO;
import com.lqx.opera.entity.CommunityComment;
import com.lqx.opera.entity.Product;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.ProductMapper;
import com.lqx.opera.service.CommunityCommentService;
import com.lqx.opera.service.ProductService;
import com.lqx.opera.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final SysUserService sysUserService;
    private final CommunityCommentService communityCommentService;

    public ProductServiceImpl(SysUserService sysUserService, CommunityCommentService communityCommentService) {
        this.sysUserService = sysUserService;
        this.communityCommentService = communityCommentService;
    }

    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        // 1. Get Product
        Product product = this.getById(productId);
        if (product == null) {
            return null;
        }

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setProduct(product);

        // 2. Get Seller Info
        if (product.getSellerId() != null) {
            SysUser seller = sysUserService.getById(product.getSellerId());
            if (seller != null) {
                ProductDetailDTO.SellerInfo sellerInfo = new ProductDetailDTO.SellerInfo();
                sellerInfo.setUserId(seller.getUserId());
                sellerInfo.setNickname(seller.getRealName() != null ? seller.getRealName() : seller.getUsername());
                sellerInfo.setAvatar(seller.getAvatar());
                dto.setSeller(sellerInfo);
            }
        }

        // 3. Get Comments (target_type = 2 for Product)
        List<CommunityComment> comments = communityCommentService.list(new LambdaQueryWrapper<CommunityComment>()
                .eq(CommunityComment::getTargetId, productId)
                .eq(CommunityComment::getTargetType, 2)
                .orderByDesc(CommunityComment::getCreatedTime));

        List<ProductDetailDTO.CommentInfo> commentInfos = new ArrayList<>();
        for (CommunityComment c : comments) {
            ProductDetailDTO.CommentInfo ci = new ProductDetailDTO.CommentInfo();
            ci.setCommentId(c.getCommentId());
            ci.setContent(c.getContent());
            ci.setCreatedTime(c.getCreatedTime());

            // Get Commenter Info
            SysUser commenter = sysUserService.getById(c.getUserId());
            if (commenter != null) {
                ProductDetailDTO.UserInfo ui = new ProductDetailDTO.UserInfo();
                ui.setUserId(commenter.getUserId());
                ui.setNickname(commenter.getUsername());
                ui.setAvatar(commenter.getAvatar());
                ci.setUser(ui);
            }
            commentInfos.add(ci);
        }
        dto.setComments(commentInfos);

        return dto;
    }
}
