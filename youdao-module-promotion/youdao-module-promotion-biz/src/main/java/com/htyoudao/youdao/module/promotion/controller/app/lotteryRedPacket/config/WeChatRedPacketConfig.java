package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//@ConfigurationProperties(prefix = "wechat.redpacket")
@Data
public class WeChatRedPacketConfig {
    private String mchId;
    private String mchKey;  // APIv2密钥
    private String wxappid; // 小程序AppID
    private String certPath;
    private String keyPath;
    private String sendName; // 商户名称
    private String clientIp = "127.0.0.1";
}
