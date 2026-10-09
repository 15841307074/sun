package com.htyoudao.youdao.module.infra.websocket;

import com.htyoudao.youdao.framework.websocket.core.listener.RabbitMQMessageListener;
import com.htyoudao.youdao.framework.websocket.core.message.GroupMessage;
import com.htyoudao.youdao.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageSender;
import com.htyoudao.youdao.module.infra.enums.MessageTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 群发消息
 *
 * @author 0090
 */
@Component
@ConditionalOnProperty(prefix = "youdao.websocket", name = "sender-type", havingValue = "rabbitmq")
public class RabbitMQGroupMessageListener<T> implements RabbitMQMessageListener<GroupMessage<T>> {

    @Resource
    private RabbitMQWebSocketMessageSender rabbitMQWebSocketMessageSender;

    @Override
    public void onMessage(GroupMessage<T> message) {
        rabbitMQWebSocketMessageSender.send(message);
    }

    @Override
    public String getType() {
        return MessageTypeEnum.GROUP_MESSAGE.getType();
    }

}
