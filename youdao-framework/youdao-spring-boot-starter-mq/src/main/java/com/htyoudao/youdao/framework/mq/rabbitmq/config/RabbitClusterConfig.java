package com.htyoudao.youdao.framework.mq.rabbitmq.config;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *    MQ配置
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-14
 */
@Configuration
public class RabbitClusterConfig {

    /**
     * JSON 消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制化 RabbitTemplate
     */
    @Bean("rabbitTemplate")
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);

        // 开启强制标志，确保消息路由失败时触发 returns 回调
        rabbitTemplate.setMandatory(true);

        // 心跳检测
        connectionFactory.setRequestedHeartBeat(60);
        // 连接超时
        connectionFactory.setConnectionTimeout(3000);

        // 消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息到达交换机成功");
            } else {
                System.err.println("消息到达交换机失败，原因: " + cause);
            }
        });

        // 消息路由失败回调
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("消息路由到队列失败: "
                    + returned.getMessage()
                    + ", 响应码: " + returned.getReplyCode()
                    + ", 失败原因: " + returned.getReplyText());
        });

        return rabbitTemplate;
    }
}
