package com.htyoudao.youdao.framework.websocket.core.listener;

/**
 * RabbitMQ 消息监听器接口
 * 目的：消费队列消息，处理对应 {@link #getType()} 类型的消息
 *
 * @param <T> 泛型，消息类型
 */
public interface RabbitMQMessageListener<T> {

    /**
     * 处理消息
     *
     * @param message 消息
     */
    void onMessage(T message);

    /**
     * 获得消息类型
     *
     * @return 消息类型
     */
    String getType();

}
