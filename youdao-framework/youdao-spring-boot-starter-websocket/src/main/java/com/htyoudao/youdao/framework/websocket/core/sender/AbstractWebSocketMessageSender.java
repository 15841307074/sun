package com.htyoudao.youdao.framework.websocket.core.sender;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.websocket.core.message.GroupMessage;
import com.htyoudao.youdao.framework.websocket.core.message.JsonWebSocketMessage;
import com.htyoudao.youdao.framework.websocket.core.message.PrivateMessage;
import com.htyoudao.youdao.framework.websocket.core.session.WebSocketSessionManager;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import com.htyoudao.youdao.module.infra.enums.MessageTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * WebSocketMessageSender 实现类
 *
 * @author 0090
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebSocketMessageSender implements WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;

    @Override
    public <T> void sendPrivateMessage(PrivateMessage<T> message) {
        send(message);
    }

    @Override
    public <T> void sendGroupMessage(GroupMessage<T> message) {
        send(message);
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    public <T> void send(PrivateMessage<T> message) {
        // 先发送给接收者
        send(message.getReceiverId(), null,
                MessageTypeEnum.PRIVATE_MESSAGE.getType(), JsonUtils.toJsonString(message.getData()));
        // 再发送给自己的其他端
        send(message.getSender().getId(), message.getSender().getTerminal(),
                MessageTypeEnum.PRIVATE_MESSAGE.getType(), JsonUtils.toJsonString(message.getData()));
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    public <T> void send(GroupMessage<T> message) {
        // 先发送给接收者
        message.getReceiverIds().forEach(
                id -> send(id, null, MessageTypeEnum.GROUP_MESSAGE.getType(),
                        JsonUtils.toJsonString(message.getData())));
        // 再发送给自己的其他端
        send(message.getSender().getId(), message.getSender().getTerminal(),
                MessageTypeEnum.GROUP_MESSAGE.getType(), JsonUtils.toJsonString(message.getData()));
    }

    /**
     * 发送消息
     *
     * @param receiverId      消息接收用户id
     * @param excludeTerminal 排除终端
     * @param messageType     消息类型
     * @param messageContent  消息内容
     */
    public void send(Long receiverId, Integer excludeTerminal, String messageType, String messageContent) {
        // 1. 获得 Session 列表
        List<WebSocketSession> sessions = Collections.emptyList();
        if (receiverId != null) {
            sessions = (List<WebSocketSession>) sessionManager.getSessionList(receiverId);
            if (CollUtil.isEmpty(sessions)) {
                if (log.isDebugEnabled()) {
                    log.debug("[send][receiverId({}) messageType({}) messageContent({}) 未匹配到会话]", receiverId, messageType, messageContent);
                }
            }
            // 移除排除的终端
            if (excludeTerminal != null) {
                sessions.removeIf(session -> excludeTerminal.equals(WebSocketFrameworkUtils.getTerminalType(session)));
            }
        }
        // 2. 执行发送
        doSend(sessions, messageType, messageContent);
    }

    /**
     * 发送消息的具体实现
     *
     * @param sessions       Session 列表
     * @param messageType    消息类型
     * @param messageContent 消息内容
     */
    public void doSend(Collection<WebSocketSession> sessions, String messageType, String messageContent) {
        JsonWebSocketMessage message = new JsonWebSocketMessage().setType(messageType).setContent(messageContent);
        String payload = JsonUtils.toJsonString(message); // 关键，使用 JSON 序列化
        sessions.forEach(session -> {
            // 1. 各种校验，保证 Session 可以被发送
            if (session == null) {
                log.error("[doSend][session 为空, message({})]", message);
                return;
            }
            if (!session.isOpen()) {
                log.error("[doSend][session({}) 已关闭, message({})]", session.getId(), message);
                return;
            }
            // 2. 执行发送
            try {
                session.sendMessage(new TextMessage(payload));
                log.info("[doSend][session({}) 发送消息成功，message({})]", session.getId(), message);
            } catch (IOException ex) {
                log.error("[doSend][session({}) 发送消息失败，message({})]", session.getId(), message, ex);
            }
        });
    }
}
