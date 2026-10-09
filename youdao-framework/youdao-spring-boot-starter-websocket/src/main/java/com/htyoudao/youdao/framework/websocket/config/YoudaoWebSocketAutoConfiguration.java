package com.htyoudao.youdao.framework.websocket.config;

import com.htyoudao.youdao.framework.websocket.core.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.websocket.core.handler.JsonWebSocketMessageHandler;
import com.htyoudao.youdao.framework.websocket.core.listener.RabbitMQMessageListener;
import com.htyoudao.youdao.framework.websocket.core.listener.WebSocketMessageListener;
import com.htyoudao.youdao.framework.websocket.core.security.LoginUserHandshakeInterceptor;
import com.htyoudao.youdao.framework.websocket.core.security.WebSocketAuthorizeRequestsCustomizer;
import com.htyoudao.youdao.framework.websocket.core.sender.local.LocalWebSocketMessageSender;
import com.htyoudao.youdao.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageConsumer;
import com.htyoudao.youdao.framework.websocket.core.sender.rabbitmq.RabbitMQWebSocketMessageSender;
import com.htyoudao.youdao.framework.websocket.core.session.WebSocketSessionHandlerDecorator;
import com.htyoudao.youdao.framework.websocket.core.session.WebSocketSessionManager;
import com.htyoudao.youdao.framework.websocket.core.session.WebSocketSessionManagerImpl;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;

/**
 * WebSocket 自动配置
 *
 * @author xingyu4j
 */
@AutoConfiguration
@EnableWebSocket
@ConditionalOnProperty(prefix = "youdao.websocket", value = "enable", matchIfMissing = true)
@EnableConfigurationProperties(WebSocketProperties.class)
public class YoudaoWebSocketAutoConfiguration {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // ==================== ServerId 相关 ====================

    @Bean
    public Long serverId() {
        return stringRedisTemplate.opsForValue().increment(RedisKeyConstants.IM_MAX_SERVER_ID, 1);
    }

    // ==================== 核心配置 ====================

    @Bean
    public WebSocketConfigurer webSocketConfigurer(HandshakeInterceptor[] handshakeInterceptors,
                                                   WebSocketHandler webSocketHandler,
                                                   WebSocketProperties webSocketProperties) {
        return registry -> registry
                // 添加 WebSocketHandler
                .addHandler(webSocketHandler, webSocketProperties.getPath())
                .addInterceptors(handshakeInterceptors)
                // 允许跨域，否则前端连接会直接断开
                .setAllowedOriginPatterns("*");
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new LoginUserHandshakeInterceptor();
    }

    @Bean
    public WebSocketHandler webSocketHandler(WebSocketSessionManager sessionManager,
                                             List<? extends WebSocketMessageListener<?>> messageListeners,
                                             Long serverId) {
        // 1. 创建 JsonWebSocketMessageHandler 对象，处理消息
        JsonWebSocketMessageHandler messageHandler = new JsonWebSocketMessageHandler(messageListeners);
        // 2. 创建 WebSocketSessionHandlerDecorator 对象，处理连接
        return new WebSocketSessionHandlerDecorator(messageHandler, sessionManager, stringRedisTemplate, serverId);
    }

    @Bean
    public WebSocketSessionManager webSocketSessionManager() {
        return new WebSocketSessionManagerImpl();
    }

    @Bean
    public WebSocketAuthorizeRequestsCustomizer webSocketAuthorizeRequestsCustomizer(WebSocketProperties webSocketProperties) {
        return new WebSocketAuthorizeRequestsCustomizer(webSocketProperties);
    }

    // ==================== Sender 相关 ====================

    @Configuration
    @ConditionalOnProperty(prefix = "youdao.websocket", name = "sender-type", havingValue = "local")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class LocalWebSocketMessageSenderConfiguration {

        @Bean
        public LocalWebSocketMessageSender localWebSocketMessageSender(WebSocketSessionManager sessionManager) {
            return new LocalWebSocketMessageSender(sessionManager);
        }

    }

    @Configuration
    @ConditionalOnProperty(prefix = "youdao.websocket", name = "sender-type", havingValue = "rabbitmq")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public static class RabbitMQWebSocketMessageSenderConfiguration {

        @Resource
        private StringRedisTemplate stringRedisTemplate;

        @Bean
        public RabbitMQWebSocketMessageSender rabbitMQWebSocketMessageSender(
                WebSocketSessionManager sessionManager, RabbitTemplate rabbitTemplate,
                DirectExchange websocketDirectExchange,
                @Value("${youdao.websocket.sender-rabbitmq.routing-key-prefix}") String routingKeyPrefix) {
            return new RabbitMQWebSocketMessageSender(sessionManager, stringRedisTemplate, rabbitTemplate,
                    websocketDirectExchange, routingKeyPrefix + ".");
        }

        @Bean
        public <T> RabbitMQWebSocketMessageConsumer<T> rabbitMQWebSocketMessageConsumer(
                List<? extends RabbitMQMessageListener<T>> messageListeners) {
            return new RabbitMQWebSocketMessageConsumer<>(messageListeners);
        }

        /**
         * 创建 Direct Exchange
         */
        @Bean
        public DirectExchange websocketDirectExchange(@Value("${youdao.websocket.sender-rabbitmq.exchange}") String exchange) {
            return new DirectExchange(exchange,
                    true,  // durable: 是否持久化
                    false);
        }

        // 配置 Jackson 消息转换器：自动序列化/反序列化 JSON 格式
        @Bean
        public MessageConverter messageConverter() {
            return new Jackson2JsonMessageConverter();
        }
    }

}
