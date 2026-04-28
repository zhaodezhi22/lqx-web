package com.lqx.opera.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqx.opera.entity.ImChatMessage;
import com.lqx.opera.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    private static final String SESSION_USER_ID_KEY = "wsUserId";

    // 存储在线用户的 Session，Key 为 userId
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    
    @Autowired
    private ObjectMapper objectMapper;
    
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
            session.getAttributes().put(SESSION_USER_ID_KEY, userId);
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
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);

        Long toId = Long.valueOf(msgMap.get("toId").toString());
        String content = (String) msgMap.get("content");
        Integer type = msgMap.get("type") != null ? Integer.valueOf(msgMap.get("type").toString()) : 0;
        String clientMsgId = msgMap.get("clientMsgId") == null ? null : msgMap.get("clientMsgId").toString();

        ImChatMessage chatMessage = new ImChatMessage();
        chatMessage.setFromId(fromId);
        chatMessage.setToId(toId);
        chatMessage.setContent(content);
        chatMessage.setType(type);
        chatMessage.setIsRecalled(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        ImChatMessage savedMsg = saveMessageSync(chatMessage);

        ImChatMessage receiverMsg = buildSocketMessage(savedMsg, "message", null);
        String receiverPayload = objectMapper.writeValueAsString(receiverMsg);
        WebSocketSession toSession = USER_SESSIONS.get(toId);
        if (toSession != null && toSession.isOpen()) {
            toSession.sendMessage(new TextMessage(receiverPayload));
        }

        ImChatMessage ackMsg = buildSocketMessage(savedMsg, "ack", clientMsgId);
        String ackPayload = objectMapper.writeValueAsString(ackMsg);
        WebSocketSession fromSession = USER_SESSIONS.get(fromId);
        if (fromSession != null && fromSession.isOpen()) {
            fromSession.sendMessage(new TextMessage(ackPayload));
        }
    }

    private ImChatMessage saveMessageSync(ImChatMessage message) {
        ImChatMessageService service = applicationContext.getBean(ImChatMessageService.class);
        service.save(message);
        return message;
    }

    private ImChatMessage buildSocketMessage(ImChatMessage source, String event, String clientMsgId) {
        ImChatMessage socketMessage = new ImChatMessage();
        socketMessage.setId(source.getId());
        socketMessage.setFromId(source.getFromId());
        socketMessage.setToId(source.getToId());
        socketMessage.setContent(source.getContent());
        socketMessage.setType(source.getType());
        socketMessage.setIsRecalled(source.getIsRecalled());
        socketMessage.setCreateTime(source.getCreateTime());
        socketMessage.setEvent(event);
        socketMessage.setClientMsgId(clientMsgId);
        return socketMessage;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getCachedUserId(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} 下线", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Long cachedUserId = getCachedUserId(session);
        if (cachedUserId != null) {
            return cachedUserId;
        }
        // 从 Query 参数中获取 token: ws://localhost:8080/ws/chat?token=xxxxx
        if (session.getUri() == null) {
            return null;
        }
        String query = session.getUri().getQuery();
        if (StringUtils.hasText(query) && query.contains("token=")) {
            String token = extractToken(query);
            if (!StringUtils.hasText(token)) {
                return null;
            }
            try {
                Claims claims = jwtUtils.parseToken(token);
                Object userIdVal = claims.get("userId");
                if (userIdVal instanceof Integer) {
                    Long userId = ((Integer) userIdVal).longValue();
                    session.getAttributes().put(SESSION_USER_ID_KEY, userId);
                    return userId;
                } else if (userIdVal instanceof Long) {
                    Long userId = (Long) userIdVal;
                    session.getAttributes().put(SESSION_USER_ID_KEY, userId);
                    return userId;
                }
            } catch (ExpiredJwtException e) {
                log.warn("WebSocket token 已过期，Session ID: {}", session.getId());
            } catch (Exception e) {
                log.error("WebSocket 认证失败", e);
            }
        }
        return null;
    }

    private Long getCachedUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get(SESSION_USER_ID_KEY);
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return null;
    }

    private String extractToken(String query) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair.startsWith("token=")) {
                return pair.substring("token=".length());
            }
        }
        return null;
    }

    public void saveMessageAsync(ImChatMessage message) {
        ImChatMessageService service = applicationContext.getBean(ImChatMessageService.class);
        service.saveMessage(message);
    }

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
