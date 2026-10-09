package com.htyoudao.youdao.module.errand.framework.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置（提现专用）
 */
@Component
@ConfigurationProperties(prefix = "wechat.pay.withdraw")
@Data
public class WechatPayConfig {
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
