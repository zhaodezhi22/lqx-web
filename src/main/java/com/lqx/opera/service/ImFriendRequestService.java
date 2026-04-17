package com.lqx.opera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.common.dto.ImFriendRequestDto;
import com.lqx.opera.entity.ImFriendRequest;

import java.util.List;

public interface ImFriendRequestService extends IService<ImFriendRequest> {

    /**
     * 发送好友申请
     */
    void sendRequest(Long userId, Long friendId, String reason);

    /**
     * 同意好友申请
     */
    void acceptRequest(Long userId, Long requestId);

    /**
     * 拒绝好友申请
     */
    void rejectRequest(Long userId, Long requestId);

    /**
     * 获取待处理的好友申请
     */
    List<ImFriendRequestDto> listPendingRequests(Long userId);
}
