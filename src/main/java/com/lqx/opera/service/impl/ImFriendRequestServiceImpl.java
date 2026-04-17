package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.common.dto.ImFriendRequestDto;
import com.lqx.opera.entity.ImFriend;
import com.lqx.opera.entity.ImFriendRequest;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.ImFriendRequestMapper;
import com.lqx.opera.service.ImFriendRequestService;
import com.lqx.opera.service.ImFriendService;
import com.lqx.opera.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImFriendRequestServiceImpl extends ServiceImpl<ImFriendRequestMapper, ImFriendRequest> implements ImFriendRequestService {

    @Autowired
    private ImFriendService imFriendService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<ImFriendRequestDto> listPendingRequests(Long userId) {
        List<ImFriendRequest> requests = this.list(new LambdaQueryWrapper<ImFriendRequest>()
                .eq(ImFriendRequest::getToId, userId)
                .eq(ImFriendRequest::getStatus, 0)
                .orderByDesc(ImFriendRequest::getCreateTime));

        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> fromIds = requests.stream().map(ImFriendRequest::getFromId).collect(Collectors.toList());
        List<SysUser> fromUsers = sysUserService.listByIds(fromIds);
        Map<Long, SysUser> userMap = fromUsers.stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));

        return requests.stream().map(req -> {
            ImFriendRequestDto dto = new ImFriendRequestDto();
            dto.setId(req.getId());
            dto.setFromId(req.getFromId());
            dto.setReason(req.getReason());
            dto.setStatus(req.getStatus());
            dto.setCreateTime(req.getCreateTime());
            
            SysUser u = userMap.get(req.getFromId());
            if (u != null) {
                dto.setFromName(u.getRealName() != null ? u.getRealName() : u.getUsername());
                dto.setFromAvatar(u.getAvatar());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public void sendRequest(Long userId, Long friendId, String reason) {
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 1. 检查是否已经是好友
        long count = imFriendService.count(new LambdaQueryWrapper<ImFriend>()
                .eq(ImFriend::getUserId, userId)
                .eq(ImFriend::getFriendId, friendId));
        if (count > 0) {
            throw new RuntimeException("你们已经是好友了");
        }

        // 2. 检查是否有待处理的申请
        long pendingCount = this.count(new LambdaQueryWrapper<ImFriendRequest>()
                .eq(ImFriendRequest::getFromId, userId)
                .eq(ImFriendRequest::getToId, friendId)
                .eq(ImFriendRequest::getStatus, 0)); // 0: 待处理
        if (pendingCount > 0) {
            throw new RuntimeException("已发送过申请，请等待对方处理");
        }

        // 3. 创建申请
        ImFriendRequest request = new ImFriendRequest();
        request.setFromId(userId);
        request.setToId(friendId);
        request.setReason(reason);
        request.setStatus(0);
        request.setCreateTime(LocalDateTime.now());
        this.save(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptRequest(Long userId, Long requestId) {
        // 1. 获取申请
        ImFriendRequest request = this.getById(requestId);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        // 2. 校验权限（只有接收者可以处理）
        if (!request.getToId().equals(userId)) {
            throw new RuntimeException("无权处理此申请");
        }

        // 3. 校验状态
        if (request.getStatus() != 0) {
            throw new RuntimeException("该申请已被处理");
        }

        // 4. 更新申请状态
        request.setStatus(1); // 1: 已通过
        this.updateById(request);

        // 5. 建立双向好友关系
        // 检查是否已存在关系（防止重复插入报错）
        boolean exists1 = imFriendService.count(new LambdaQueryWrapper<ImFriend>()
                .eq(ImFriend::getUserId, request.getFromId())
                .eq(ImFriend::getFriendId, request.getToId())) > 0;
        
        if (!exists1) {
            ImFriend f1 = new ImFriend();
            f1.setUserId(request.getFromId());
            f1.setFriendId(request.getToId());
            f1.setCreateTime(LocalDateTime.now());
            imFriendService.save(f1);
        }

        boolean exists2 = imFriendService.count(new LambdaQueryWrapper<ImFriend>()
                .eq(ImFriend::getUserId, request.getToId())
                .eq(ImFriend::getFriendId, request.getFromId())) > 0;

        if (!exists2) {
            ImFriend f2 = new ImFriend();
            f2.setUserId(request.getToId());
            f2.setFriendId(request.getFromId());
            f2.setCreateTime(LocalDateTime.now());
            imFriendService.save(f2);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRequest(Long userId, Long requestId) {
        ImFriendRequest request = this.getById(requestId);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!request.getToId().equals(userId)) {
            throw new RuntimeException("无权处理此申请");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("该申请已被处理");
        }
        request.setStatus(2); // 2: 已拒绝
        this.updateById(request);
    }
}
