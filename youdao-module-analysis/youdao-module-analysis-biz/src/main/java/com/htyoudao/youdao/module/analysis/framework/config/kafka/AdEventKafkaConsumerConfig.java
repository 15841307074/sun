package com.htyoudao.youdao.module.analysis.framework.config.kafka;

import jakarta.annotation.Resource;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 广告埋点 Kafka 消费者容器工厂
 * 复用 kafka.consumer.* 的连接与 SASL 认证配置，批量消费模式，批量写入 ES
 */
@Configuration
public class AdEventKafkaConsumerConfig {

    @Resource
    private KafkaConsumerConfigurationProperties properties;

    /**
     * 埋点专用容器工厂：batch 模式 + 手动提交（批处理完成后按批次提交，兼顾吞吐与不丢数据）
     */
    @Bean("adEventKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> adEventKafkaListenerContainerFactory() {
        Map<String, Object> props = new HashMap<>(16);
        //节点ip:端口
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        //键反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, properties.getKeyDeserializer());
        //值反序列化器
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, properties.getValueDeserializer());
        //单次拉取上限，即每批写入 ES 的条数
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        //批量消费完成后手动提交 offset，避免消费失败丢数据
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // 是否进行认证
        if (properties.getUseSecurity()) {
            //安全认证协议
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, properties.getSecurityProtocol());
            //安全认证机制
            props.put(SaslConfigs.SASL_MECHANISM, properties.getSaslMechanism());
            //SASL认证所需的JAAS配置
            props.put(SaslConfigs.SASL_JAAS_CONFIG, properties.getSaslJaasConfig());
        }

        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        //批量监听模式，@KafkaListener 参数为 List
        factory.setBatchListener(true);
        //消费并发数（需 <= Topic 分区数）
        factory.setConcurrency(properties.getConcurrency() == null ? 1 : properties.getConcurrency());
        //批量处理完成后提交 offset
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        return factory;
    }
}
