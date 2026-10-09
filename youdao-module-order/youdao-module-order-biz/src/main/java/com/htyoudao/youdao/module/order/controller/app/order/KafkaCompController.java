package com.htyoudao.youdao.module.order.controller.app.order;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.module.order.controller.app.order.DTO.NotifyOrderDTO;
import com.htyoudao.youdao.module.order.util.KafkaProducerUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "KAFKA补偿", description = "KAFKA补偿")
@RestController
@RequestMapping("/order/kafkaComp")
public class KafkaCompController {

    @Resource
    private KafkaProducerUtil kafkaProducerUtil;

    @Value("${kafka.producer.orderNotifyTopic}")
    private String orderNotifyTopic;

    @PermitAll
    @PostMapping("/sendMessage")
    @Operation(summary = "sendMessage")
    public void sendMessage(@RequestParam(value = "orderSn") String orderSn,@RequestParam(value = "type") String type) {
        NotifyOrderDTO notifyOrderDTO = NotifyOrderDTO.builder()
                .orderSn(orderSn).type(type).build();
        kafkaProducerUtil.sendMessage(orderNotifyTopic, orderSn, JSON.toJSONString(notifyOrderDTO));
        log.info("==> 【手动】kafka发消息 {}", notifyOrderDTO);
    }
}
