package com.lqx.opera.websocket.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.entity.ImChatMessage;
import com.lqx.opera.mapper.ImChatMessageMapper;
import com.lqx.opera.websocket.ChatWebSocketHandler;
import com.lqx.opera.websocket.ImChatMessageService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImChatMessageServiceImpl extends ServiceImpl<ImChatMessageMapper, ImChatMessage> implements ImChatMessageService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Async
    public void saveMessage(ImChatMessage message) {
        this.save(message);
    }

    @Override
    public List<ImChatMessage> getHistoryMessages(Long userId, Long friendId, Integer size) {
        return this.list(new LambdaQueryWrapper<ImChatMessage>()
                .and(w -> w
                        .eq(ImChatMessage::getFromId, userId).eq(ImChatMessage::getToId, friendId)
                        .or()
                        .eq(ImChatMessage::getFromId, friendId).eq(ImChatMessage::getToId, userId)
                )
                .orderByDesc(ImChatMessage::getCreateTime)
                .last("LIMIT " + size)
        ).stream()
        .sorted((m1, m2) -> m1.getCreateTime().compareTo(m2.getCreateTime()))
        .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(Long userId, Long messageId) {
        ImChatMessage message = this.getById(messageId);
        
        // 兼容处理：如果按 ID 查不到，可能是因为前端传的是 timestamp ID (乐观更新时生成的)
        // 尝试通过 createTime 和 fromId 查找最近的一条消息
        if (message == null) {
            // 假设前端传的 messageId 是时间戳，且误差在 1 秒内
            // 或者直接告诉前端需要刷新列表获取真实 ID
            // 这里我们尝试用更宽的条件查找：最近 2 分钟内，该用户发送的，且内容匹配的消息？
            // 还是严格一点：必须要有真实 ID。
            // 前端在发送成功后，应该更新本地消息的 ID 为数据库真实 ID。
            // 但目前前端实现是：socket.send -> addMessageToHistory (localMsg with Date.now() id)
            // 后端异步保存，并没有通过 socket 返回真实 ID 给前端。
            // 所以前端持有的 ID 是假的，发回给后端当然查不到。
            
            // 解决方案：
            // 1. 后端保存消息后，通过 Socket 推送一条 "ACK" 消息，包含 clientMsgId 和 serverMsgId
            // 2. 前端收到 ACK 后，更新本地消息 ID
            // 3. 或者：前端撤回时，如果 ID 是时间戳（很大），则提示“正在发送中或刷新后重试”
            // 
            // 鉴于不改动太大的原则，我们可以在后端做一个模糊匹配（不推荐，容易误删）
            // 或者：修改 ChatWebSocketHandler，保存后推送 ACK。
            
            // 让我们采用方案 1 的变体：修改 ChatWebSocketHandler，在保存后，推送一条 type=10 (System/Ack) 的消息给发送者
            throw new RuntimeException("消息未同步或不存在，请刷新页面后重试");
        }

        if (!message.getFromId().equals(userId)) {
            throw new RuntimeException("只能撤回自己发送的消息");
        }

        if (message.getIsRecalled() == 1) {
            throw new RuntimeException("消息已撤回");
        }

        // 检查时间是否超过 2 分钟
        Duration duration = Duration.between(message.getCreateTime(), LocalDateTime.now());
        if (duration.getSeconds() > 120) {
            throw new RuntimeException("超过2分钟无法撤回");
        }

        // 修改消息状态和内容
        message.setIsRecalled(1);
        message.setContent("该消息已撤回");
        this.updateById(message);

        // 推送撤回通知给接收方
        try {
            Map<String, Object> recallMsg = Map.of(
                "type", 2, // 2 表示撤回提示
                "content", "对方撤回了一条消息",
                "messageId", messageId,
                "fromId", userId
            );
            String jsonMsg = objectMapper.writeValueAsString(recallMsg);
            
            // 推送给接收者
            ChatWebSocketHandler.sendMessage(message.getToId(), jsonMsg);
            
            // 也可以推送给发送者自己，确认撤回成功（如果前端没有自动处理）
            ChatWebSocketHandler.sendMessage(userId, jsonMsg);
            
        } catch (Exception e) {
            log.error("推送撤回消息失败", e);
        }
    }
}
