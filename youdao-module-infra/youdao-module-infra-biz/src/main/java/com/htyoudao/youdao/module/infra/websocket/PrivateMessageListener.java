package com.htyoudao.youdao.module.infra.websocket;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.websocket.core.listener.WebSocketMessageListener;
import com.htyoudao.youdao.framework.websocket.core.message.PrivateMessage;
import com.htyoudao.youdao.framework.websocket.core.message.UserInfo;
import com.htyoudao.youdao.framework.websocket.core.sender.WebSocketMessageSender;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import com.htyoudao.youdao.module.infra.enums.MessageTypeEnum;
import com.htyoudao.youdao.module.infra.websocket.message.WebsocketSendMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

/**
 * WebSocket 单发消息
 *
 * @author 0090
 */
@Component
public class PrivateMessageListener implements WebSocketMessageListener<WebsocketSendMessage> {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public void onMessage(WebSocketSession session, WebsocketSendMessage message) {
        Long fromUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        Integer terminalType = WebSocketFrameworkUtils.getTerminalType(session);
        List<Long> receiverIds = new ArrayList<>(message.getReceiverIds());
        if (CollUtil.isNotEmpty(receiverIds)) {
            PrivateMessage<String> privateMessage = new PrivateMessage<>();
            privateMessage.setSender(new UserInfo().setId(fromUserId).setTerminal(terminalType));
            privateMessage.setReceiverId(receiverIds.get(0));
            privateMessage.setData(message.getText());
            webSocketMessageSender.sendPrivateMessage(privateMessage);
        }
    }

    @Override
    public String getType() {
        return MessageTypeEnum.PRIVATE_MESSAGE.getType();
    }

}
