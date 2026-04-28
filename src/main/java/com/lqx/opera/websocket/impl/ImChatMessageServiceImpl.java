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

    private final ObjectMapper objectMapper;

    public ImChatMessageServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
        if (message == null) {
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
                "event", "recall",
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
