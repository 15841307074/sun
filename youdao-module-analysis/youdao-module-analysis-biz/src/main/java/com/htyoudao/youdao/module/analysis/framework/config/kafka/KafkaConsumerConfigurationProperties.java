package com.htyoudao.youdao.module.analysis.framework.config.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * kafka消费者配置信息（读取 kafka.consumer.* 配置中的连接与认证信息）
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kafka.consumer")
public class KafkaConsumerConfigurationProperties {

    /**
     * 服务器列表
     */
    private String bootstrapServers;

    /**
     * 键反序列化器
     */
    private String keyDeserializer = org.apache.kafka.common.serialization.StringDeserializer.class.getName();

    /**
     * 值反序列化器
     */
    private String valueDeserializer = org.apache.kafka.common.serialization.StringDeserializer.class.getName();

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

    /**
     * 消费并发数（需 <= Topic 分区数）
     */
    private Integer concurrency = 1;
}
