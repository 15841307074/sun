package com.htyoudao.youdao.module.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Package:  com.htyd.config.properties
 * @ClassName: KafkaProducerConfigurationProperties.java
 * @Description: kafka生产者配置信息
 * @Author: wangwei
 * @Date: 2023/3/23 12:59
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.producer")
public class KafkaProducerConfigurationProperties {

    /**
     * 服务器列表
     */
    private String bootstrapServers;

    /**
     * 键序列化器
     */
    private String keySerializer = org.apache.kafka.common.serialization.StringSerializer.class.getName();

    /**
     * 值序列化器
     */
    private String valueSerializer = org.apache.kafka.common.serialization.StringSerializer.class.getName();

    /**
     * 自动确认0，不接收返回结果
     */
    private String acks = "0";

    /**
     * 消息发送失败重试次数
     */
    private Integer retries = 0;

    /**
     * 缓冲区内存
     */
    private Long bufferMemory = 33554432L;

    /**
     * 请求的最长等待时间
     */
    private Integer maxBlockMs = 30000;

    /**
     * 设置客户端内部重试间隔
     */
    private Integer retryBackoffMs = 30000;

    /**
     * 是否开启安全认证
     */
    private Boolean useSecurity = false;

    /**
     * 安全认证协议
     */
    private String securityProtocol = "SASL_PLAINTEXT";

    /**
     * 安全认证机制
     */
    private String saslMechanism = "PLAIN";

    /**
     * SASL认证所需的JAAS配置
     */
    private String saslJaasConfig;
}