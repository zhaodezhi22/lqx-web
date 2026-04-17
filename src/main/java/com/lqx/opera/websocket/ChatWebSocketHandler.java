package com.lqx.opera.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.entity.ImChatMessage;
import com.lqx.opera.service.ImFriendService;
import com.lqx.opera.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 存储在线用户的 Session，Key 为 userId
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 由于 WebSocketHandler 是单例，Service 需要延迟注入或通过 ApplicationContext 获取
    // 这里使用 setter 注入或 ApplicationContext
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("用户 {} 上线，Session ID: {}", userId, session.getId());
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long fromId = getUserIdFromSession(session);
        if (fromId == null) return;

        String payload = message.getPayload();
        // 假设前端发送的消息格式为: { "toId": 123, "content": "hello", "type": 0 }
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
        
        Long toId = Long.valueOf(msgMap.get("toId").toString());
        String content = (String) msgMap.get("content");
        Integer type = msgMap.get("type") != null ? Integer.valueOf(msgMap.get("type").toString()) : 0;

        // 构造消息实体
        ImChatMessage chatMessage = new ImChatMessage();
        chatMessage.setFromId(fromId);
        chatMessage.setToId(toId);
        chatMessage.setContent(content);
        chatMessage.setType(type);
        chatMessage.setIsRecalled(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        // 同步保存消息
        ImChatMessage savedMsg = saveMessageSync(chatMessage);
        String pushMsg = objectMapper.writeValueAsString(savedMsg);

        // 推送给接收方
        WebSocketSession toSession = USER_SESSIONS.get(toId);
        if (toSession != null && toSession.isOpen()) {
            toSession.sendMessage(new TextMessage(pushMsg));
        }

        // 同时也推给发送方（确认发送成功，并回传真实 ID）
        WebSocketSession fromSession = USER_SESSIONS.get(fromId);
        if (fromSession != null && fromSession.isOpen()) {
            fromSession.sendMessage(new TextMessage(pushMsg));
        }
    }

    private ImChatMessage saveMessageSync(ImChatMessage message) {
        ImChatMessageService service = applicationContext.getBean(ImChatMessageService.class);
        service.save(message);
        return message;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} 下线", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        // 从 Query 参数中获取 token: ws://localhost:8080/ws/chat?token=xxxxx
        String query = session.getUri().getQuery();
        if (StringUtils.hasText(query) && query.contains("token=")) {
            String token = query.split("token=")[1];
            try {
                Claims claims = jwtUtils.parseToken(token);
                Object userIdVal = claims.get("userId");
                if (userIdVal instanceof Integer) {
                    return ((Integer) userIdVal).longValue();
                } else if (userIdVal instanceof Long) {
                    return (Long) userIdVal;
                }
            } catch (Exception e) {
                log.error("WebSocket 认证失败", e);
            }
        }
        return null;
    }

    // 异步保存消息到数据库
    // 注意：需要在配置类中开启 @EnableAsync
    public void saveMessageAsync(ImChatMessage message) {
        // 通过 ApplicationContext 获取 Service Bean，避免循环依赖或注入为空
        ImChatMessageService service = applicationContext.getBean(ImChatMessageService.class);
        service.saveMessage(message);
    }
    
    // 供外部调用：发送消息（如撤回通知）
    public static void sendMessage(Long toId, String message) {
        WebSocketSession session = USER_SESSIONS.get(toId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("发送消息失败", e);
            }
        }
    }
}
