package com.soyorim.acaj.module.system.websocket;

import cn.hutool.json.JSONUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/notification/{userId}")
public class NotificationWebSocket {

    private static final Map<String, Session> ONLINE_SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        ONLINE_SESSIONS.put(userId, session);
        log.info("WebSocket 连接建立: userId={}", userId);
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        ONLINE_SESSIONS.remove(userId);
        log.info("WebSocket 连接关闭: userId={}", userId);
    }

    @OnError
    public void onError(Throwable error) {
        log.error("WebSocket 错误", error);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        log.info("收到消息: userId={}, msg={}", userId, message);
    }

    public static void sendToUser(String userId, Object message) {
        Session session = ONLINE_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(JSONUtil.toJsonStr(message));
            } catch (IOException e) {
                log.error("WebSocket 推送失败: userId={}", userId, e);
            }
        }
    }

    public static void broadcast(Object message) {
        ONLINE_SESSIONS.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(JSONUtil.toJsonStr(message));
                } catch (IOException e) {
                    log.error("WebSocket 广播失败: userId={}", userId, e);
                }
            }
        });
    }
}
