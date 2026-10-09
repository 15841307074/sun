package com.htyoudao.youdao.framework.websocket.core.sender;

import com.htyoudao.youdao.framework.websocket.core.message.GroupMessage;
import com.htyoudao.youdao.framework.websocket.core.message.PrivateMessage;

/**
 * WebSocket 消息的发送器接口
 *
 * @author 0090
 */
public interface WebSocketMessageSender {

    /**
     * 发送私聊消息
     *
     * @param privateMessage 私信
     */
    <T> void sendPrivateMessage(PrivateMessage<T> privateMessage);

    /**
     * 发送群聊消息
     *
     * @param message 群聊消息
     */
    <T> void sendGroupMessage(GroupMessage<T> message);


}
