package com.htyoudao.youdao.module.infra.websocket;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.websocket.core.listener.WebSocketMessageListener;
import com.htyoudao.youdao.framework.websocket.core.message.GroupMessage;
import com.htyoudao.youdao.framework.websocket.core.message.UserInfo;
import com.htyoudao.youdao.framework.websocket.core.sender.WebSocketMessageSender;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import com.htyoudao.youdao.module.infra.enums.MessageTypeEnum;
import com.htyoudao.youdao.module.infra.websocket.message.WebsocketSendMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

/**
 * WebSocket 群发消息
 *
 * @author 0090
 */
@Component
public class GroupMessageListener implements WebSocketMessageListener<WebsocketSendMessage> {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public void onMessage(WebSocketSession session, WebsocketSendMessage message) {
        Long fromUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        Integer terminalType = WebSocketFrameworkUtils.getTerminalType(session);
        Set<Long> receiverIds = message.getReceiverIds();
        if (CollUtil.isNotEmpty(receiverIds)) {
            GroupMessage<String> groupMessage = new GroupMessage<>();
            groupMessage.setSender(new UserInfo().setId(fromUserId).setTerminal(terminalType));
            groupMessage.setReceiverIds(receiverIds);
            groupMessage.setData(message.getText());
            receiverIds.forEach(toUserId -> webSocketMessageSender.sendGroupMessage(groupMessage));
        }
    }

    @Override
    public String getType() {
        return MessageTypeEnum.GROUP_MESSAGE.getType();
    }

}
