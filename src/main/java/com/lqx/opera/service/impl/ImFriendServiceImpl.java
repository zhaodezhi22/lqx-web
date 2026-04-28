package com.lqx.opera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqx.opera.common.dto.ImFriendDto;
import com.lqx.opera.entity.ImChatMessage;
import com.lqx.opera.entity.ImFriend;
import com.lqx.opera.entity.SysUser;
import com.lqx.opera.mapper.ImChatMessageMapper;
import com.lqx.opera.mapper.ImFriendMapper;
import com.lqx.opera.service.ImFriendService;
import com.lqx.opera.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImFriendServiceImpl extends ServiceImpl<ImFriendMapper, ImFriend> implements ImFriendService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ImChatMessageMapper imChatMessageMapper;

    @Override
    public List<ImFriendDto> listFriends(Long userId) {
        // 1. 查询好友关系
        List<ImFriend> friendRelations = this.list(new LambdaQueryWrapper<ImFriend>()
                .eq(ImFriend::getUserId, userId));

        if (friendRelations.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取好友ID列表
        List<Long> friendIds = friendRelations.stream()
                .map(ImFriend::getFriendId)
                .collect(Collectors.toList());

        // 3. 查询好友用户详情
        List<SysUser> users = sysUserService.listByIds(friendIds);
        Map<Long, SysUser> userMap = users.stream()
                .collect(Collectors.toMap(SysUser::getUserId, u -> u));

        // 4. 组装DTO
        return friendRelations.stream().map(relation -> {
            SysUser friendUser = userMap.get(relation.getFriendId());
            ImFriendDto dto = new ImFriendDto();
            dto.setUserId(relation.getFriendId());
            dto.setRemark(relation.getRemark());
            if (friendUser != null) {
                dto.setUsername(friendUser.getUsername());
                dto.setRealName(friendUser.getRealName());
                dto.setAvatar(friendUser.getAvatar());
            }
            ImChatMessage lastMessage = imChatMessageMapper.selectOne(new LambdaQueryWrapper<ImChatMessage>()
                    .and(w -> w
                            .eq(ImChatMessage::getFromId, userId).eq(ImChatMessage::getToId, relation.getFriendId())
                            .or()
                            .eq(ImChatMessage::getFromId, relation.getFriendId()).eq(ImChatMessage::getToId, userId)
                    )
                    .orderByDesc(ImChatMessage::getCreateTime)
                    .last("LIMIT 1"));
            if (lastMessage != null) {
                dto.setLastMessage(lastMessage.getContent());
                dto.setLastMessageType(lastMessage.getType());
                dto.setLastMessageIsRecalled(lastMessage.getIsRecalled());
                dto.setLastMessageTime(lastMessage.getCreateTime());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long userId, Long friendId) {
        // 双向删除
        this.remove(new LambdaQueryWrapper<ImFriend>()
                .eq(ImFriend::getUserId, userId)
                .eq(ImFriend::getFriendId, friendId));
        
        this.remove(new LambdaQueryWrapper<ImFriend>()
                .eq(ImFriend::getUserId, friendId)
                .eq(ImFriend::getFriendId, userId));
    }
}
