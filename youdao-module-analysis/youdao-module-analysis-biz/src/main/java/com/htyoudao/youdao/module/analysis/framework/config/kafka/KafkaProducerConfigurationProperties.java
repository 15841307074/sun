package com.htyoudao.youdao.module.analysis.framework.config.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * kafka生产者配置信息（与 order 模块保持一致，读取 kafka.producer.* 配置）
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

    /**
     * 广告埋点 Kafka 发送开关：false 时 /ad-add 埋点链路直接返回成功不发送消息
     * （压测/本地调试/紧急止损用），通过 @ConfigurationProperties 支持 Nacos 热刷新
     */
    private Boolean adEventEnabled = true;
}
