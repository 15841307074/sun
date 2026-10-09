package com.htyoudao.youdao.framework.websocket.core.sender.rabbitmq;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * RabbitMQ 广播 WebSocket 的消息
 *
 * @author 0090
 */
@Data
public class RabbitMQWebSocketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = -8163991275959887224L;

    /**
     * 消息类型
     */
    private String messageType;
    /**
     * 消息内容
     */
    private String messageContent;

}
