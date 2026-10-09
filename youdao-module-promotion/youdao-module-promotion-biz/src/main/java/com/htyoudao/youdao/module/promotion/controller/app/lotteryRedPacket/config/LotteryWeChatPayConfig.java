package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wechat.pay.hanbao")
@Data
public class LotteryWeChatPayConfig {
    private String mchId;
    private String appId;
    private String certSerialNo;
    private String privateKeyPath;
    private String wechatPublicKeyId;
    private String wechatPublicKeyPath;
    private String transferSceneId;
    private String notifyUrl;
    private String apiV3Key;
}
