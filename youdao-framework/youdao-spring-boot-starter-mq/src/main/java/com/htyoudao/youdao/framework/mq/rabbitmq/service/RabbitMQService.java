package com.htyoudao.youdao.framework.mq.rabbitmq.service;

/**
 * <p>
 * MQ
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-14
 */

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQService {

    @Qualifier("rabbitTemplate")
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 发送消息到指定交换机
     *
     * @param exchange   交换机名称
     * @param routingKey 路由键
     * @param message    消息内容（自动序列化为JSON）
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 创建交换机
     *
     * @param exchangeName 交换机名称
     * @return
     */
    public Exchange creatExchange(String exchangeName) {
        Exchange exchange = ExchangeBuilder.fanoutExchange(exchangeName).durable(true).build();
        amqpAdmin.declareExchange(exchange);
        return exchange;
    }

    /**
     * 绑定队列到交换机
     *
     */
    public void bindExchange(Exchange exchange, Queue queue) {
        amqpAdmin.declareBinding(new Binding(queue.getName(), Binding.DestinationType.QUEUE,
                exchange.getName(), queue.getName(), null));
    }

    /**
     * 创建队列
     *
     * @param queueName 队列名称
     * @return
     */
    public Queue createQueue(String queueName) {
        //创建队列
        Queue queue = QueueBuilder.durable(queueName).build();
        amqpAdmin.declareQueue(queue);

        return queue;
    }
}
