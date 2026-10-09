package com.htyoudao.youdao.module.analysis.framework.config.kafka;

import jakarta.annotation.Resource;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka生产者配置（与 order 模块保持一致，支持 SASL 安全认证）
 */
@Configuration
public class KafkaProducerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Resource
    private KafkaProducerConfigurationProperties properties;

    /**
     * 生产者配置信息组装
     */
    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>(16);
        //节点ip:端口
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        //键序列化器
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, properties.getKeySerializer());
        //值序列化器
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, properties.getValueSerializer());
        //消息发送失败重试次数
        props.put(ProducerConfig.RETRIES_CONFIG, properties.getRetries());
        //缓冲区内存
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, properties.getBufferMemory());
        //请求的最长等待时间
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, properties.getMaxBlockMs());
        //设置客户端内部重试间隔
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, properties.getRetryBackoffMs());
        //生产者对于发送消息请求的响应的处理方式
        props.put(ProducerConfig.ACKS_CONFIG, properties.getAcks());
        // 是否进行认证
        if (properties.getUseSecurity()) {
            //安全认证协议
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, properties.getSecurityProtocol());
            //安全认证机制
            props.put(SaslConfigs.SASL_MECHANISM, properties.getSaslMechanism());
            //SASL认证所需的JAAS配置
            props.put(SaslConfigs.SASL_JAAS_CONFIG, properties.getSaslJaasConfig());
        }
        return props;
    }

    /**
     * 生产者监听器，异步发送监听
     */
    public ProducerListener<String, String> producerListener() {
        return new LoggingProducerListener<String, String>() {
            @Override
            public void onSuccess(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata) {
                super.onSuccess(producerRecord, recordMetadata);
            }

            @Override
            public void onError(ProducerRecord<String, String> producerRecord, RecordMetadata recordMetadata, Exception exception) {
                super.onError(producerRecord, recordMetadata, exception);
                log.error("kafka生产者回调-producer:{},recordMetadata：{},发送失败:{}",
                        producerRecord, recordMetadata, exception.getMessage());
            }
        };
    }

    @Bean("kafkaTemplate")
    public KafkaTemplate<String, String> kafkaTemplate() {
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(this.producerConfig());
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setProducerListener(this.producerListener());
        return kafkaTemplate;
    }
}
