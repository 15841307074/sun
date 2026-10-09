package com.htyoudao.youdao.framework.websocket.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.websocket.core.listener.WebSocketMessageListener;
import com.htyoudao.youdao.framework.websocket.core.message.JsonWebSocketMessage;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JSON 格式 {@link WebSocketHandler} 实现类
 * 基于 {@link JsonWebSocketMessage#getType()} 消息类型，调度到对应的 {@link WebSocketMessageListener} 监听器。
 *
 * @author 0090
 */
@Slf4j
public class JsonWebSocketMessageHandler extends TextWebSocketHandler {

    /**
     * type 与 WebSocketMessageListener 的映射
     */
    private final Map<String, WebSocketMessageListener<Object>> listeners = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonWebSocketMessageHandler(List<? extends WebSocketMessageListener> listenersList) {
        listenersList.forEach((Consumer<WebSocketMessageListener>) listener ->
                listeners.put(listener.getType(), listener));
    }

    @Override
    protected void handleTextMessage(@Nonnull WebSocketSession session, TextMessage message) {
        // 1.1 空消息，跳过
        if (message.getPayloadLength() == 0) {
            return;
        }

        // 2.1 解析消息
        try {
            JsonWebSocketMessage jsonMessage = JsonUtils.parseObject(message.getPayload(), JsonWebSocketMessage.class);
            if (jsonMessage == null) {
                log.error("[handleTextMessage][session({}) message({}) 解析为空]", session.getId(), message.getPayload());
                return;
            }
            if (StrUtil.isEmpty(jsonMessage.getType())) {
                log.error("[handleTextMessage][session({}) message({}) 类型为空]", session.getId(), message.getPayload());
                return;
            }
            // 2.2 获得对应的 WebSocketMessageListener
            WebSocketMessageListener<Object> messageListener = listeners.get(jsonMessage.getType());
            if (messageListener == null) {
                log.error("[handleTextMessage][session({}) message({}) 监听器为空]", session.getId(), message.getPayload());
                return;
            }
            // 2.3 处理消息
            Type type = TypeUtil.getTypeArgument(messageListener.getClass(), 0);
            Object messageObj;
            if (type == String.class) {
                messageObj = jsonMessage.getContent();
            } else {
                messageObj = JsonUtils.parseObject(jsonMessage.getContent(), type);
            }
            messageListener.onMessage(session, messageObj);
        } catch (Throwable ex) {
            log.error("[handleTextMessage][session({}) message({}) 处理异常]", session.getId(), message.getPayload());
        }
    }

}
