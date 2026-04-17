package com.lqx.opera.websocket;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqx.opera.entity.ImChatMessage;

public interface ImChatMessageService extends IService<ImChatMessage> {
    
    /**
     * 异步保存消息
     */
    void saveMessage(ImChatMessage message);
    
    /**
     * 撤回消息
     */
    void recallMessage(Long userId, Long messageId);

    /**
     * 获取历史消息
     */
    java.util.List<ImChatMessage> getHistoryMessages(Long userId, Long friendId, Integer size);
}
