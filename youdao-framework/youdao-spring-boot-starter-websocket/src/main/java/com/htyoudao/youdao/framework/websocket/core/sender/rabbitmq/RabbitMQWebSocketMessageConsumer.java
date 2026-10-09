package com.htyoudao.youdao.framework.websocket.core.sender.rabbitmq;

import cn.hutool.core.util.TypeUtil;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.websocket.core.listener.RabbitMQMessageListener;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link RabbitMQWebSocketMessage} 广播消息的消费者，真正把消息发送出去
 *
 * @author 0090
 */
@RabbitListener(
        bindings = @QueueBinding(
                value = @Queue(
                        // 在 Queue 的名字上，使用 serverId 生成其后缀。这样，启动的 Consumer 的 Queue 不同，以达到广播消费的目的
                        name = "${youdao.websocket.sender-rabbitmq.queue}" + "-" + "#{@serverId}",
                        // Consumer 关闭时，该队列就可以被自动删除了
                        autoDelete = "true"
                ),
                exchange = @Exchange(
                        name = "${youdao.websocket.sender-rabbitmq.exchange}"
                ),
                // 显式指定 RoutingKey = 队列名称（通过 SpEL 引用队列的 name 属性）
                key = "${youdao.websocket.sender-rabbitmq.routing-key-prefix}" + "." + "#{@serverId}"
        )
)
@Slf4j
public class RabbitMQWebSocketMessageConsumer<T> {

    private final Map<String, RabbitMQMessageListener<T>> listeners = new HashMap<>();

    public RabbitMQWebSocketMessageConsumer(List<? extends RabbitMQMessageListener<T>> listenersList) {
        listenersList.forEach((Consumer<RabbitMQMessageListener<T>>) listener ->
                listeners.put(listener.getType(), listener));
    }


    @RabbitHandler
    public void onMessage(RabbitMQWebSocketMessage message, Channel channel, Message amqpMessage) {
        long deliveryTag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            RabbitMQMessageListener<T> messageListener = listeners.get(message.getMessageType());
            if (messageListener == null) {
                log.error("[handleRabbitMQMessage][ message({}) 监听器为空]", message);
                return;
            }
            Type type = TypeUtil.getTypeArgument(messageListener.getClass(), 0);
            messageListener.onMessage(JsonUtils.parseObject(message.getMessageContent(),type));
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process RabbitMQ message, deliveryTag: {}", deliveryTag, e);
        }
    }

}
