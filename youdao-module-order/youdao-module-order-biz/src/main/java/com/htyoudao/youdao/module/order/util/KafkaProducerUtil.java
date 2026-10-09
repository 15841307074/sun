package com.htyoudao.youdao.module.order.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaProducerUtil {

    private final KafkaTemplate<String, String> kafkaTemplate;

//    @Resource
//    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    KafkaProducerUtil(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * @param topic:     消息主题
     * @param key:       消息键（用于分区路由）
     * @param message:   消息json
     * @return
     * @throws
     * @method: sendMessage
     * @Description: 发送消息
     * @author: wangwei
     * @date: 2023/3/20 13:48
     **/
    public void sendMessage(String topic, String key, String message) {
        try {
            kafkaTemplate.send(topic, key, message);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送失败主题[{}]key[{}]的数据消息[{}]", topic, key, message);
        }
    }
}
