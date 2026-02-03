package com.lqx.opera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lqx.opera.common.Result;
import com.lqx.opera.dto.UserPublicProfileDTO;
import com.lqx.opera.entity.*;
import com.lqx.opera.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/user")
public class PublicUserController {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private InheritorProfileService inheritorProfileService;
    @Autowired
    private HeritageResourceService resourceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CommunityService postService;
    @Autowired
    private PerformanceEventService eventService;

    @GetMapping("/{id}/profile")
    public Result<UserPublicProfileDTO> getProfile(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) return Result.fail(404, "用户不存在");

        UserPublicProfileDTO dto = new UserPublicProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setNickname(user.getRealName() != null ? user.getRealName() : user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());

        // If inheritor, get profile
        InheritorProfile profile = inheritorProfileService.getOne(
                new LambdaQueryWrapper<InheritorProfile>().eq(InheritorProfile::getUserId, id)
        );
        if (profile != null) {
            dto.setLevel(profile.getLevel());
            dto.setGenre(profile.getGenre());
            dto.setArtisticCareer(profile.getArtisticCareer());
            dto.setAwards(profile.getAwards());
            dto.setVerifyStatus(profile.getVerifyStatus());
        }
        return Result.success(dto);
    }

    @GetMapping("/{id}/resources")
    public Result<List<HeritageResource>> getResources(@PathVariable Long id) {
        return Result.success(resourceService.list(
                new LambdaQueryWrapper<HeritageResource>()
                        .eq(HeritageResource::getUploaderId, id)
                        .eq(HeritageResource::getStatus, 1) // Only approved
                        .orderByDesc(HeritageResource::getCreatedTime)
        ));
    }

    @GetMapping("/{id}/products")
    public Result<List<Product>> getProducts(@PathVariable Long id) {
        return Result.success(productService.list(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getSellerId, id)
                        .eq(Product::getStatus, 1) // Only on shelf
                        .orderByDesc(Product::getCreatedTime)
        ));
    }

    @GetMapping("/{id}/posts")
    public Result<List<CommunityPost>> getPosts(@PathVariable Long id) {
        return Result.success(postService.list(
                new LambdaQueryWrapper<CommunityPost>()
                        .eq(CommunityPost::getUserId, id)
                        .orderByDesc(CommunityPost::getCreatedTime)
        ));
    }

    @GetMapping("/{id}/events")
    public Result<List<PerformanceEvent>> getEvents(@PathVariable Long id) {
        return Result.success(eventService.list(
                new LambdaQueryWrapper<PerformanceEvent>()
                        .eq(PerformanceEvent::getPublisherId, id)
                        .eq(PerformanceEvent::getStatus, 1) // Only active
                        .orderByDesc(PerformanceEvent::getShowTime)
        ));
    }
}
