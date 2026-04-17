package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.ImFriendDto;
import com.lqx.opera.entity.ImFriend;

import java.util.List;

public interface ImFriendService extends IService<ImFriend> {

    /**
     * 查询当前用户的所有好友
     */
    List<ImFriendDto> listFriends(Long userId);

    /**
     * 双向删除好友关系
     */
    void deleteFriend(Long userId, Long friendId);
}
