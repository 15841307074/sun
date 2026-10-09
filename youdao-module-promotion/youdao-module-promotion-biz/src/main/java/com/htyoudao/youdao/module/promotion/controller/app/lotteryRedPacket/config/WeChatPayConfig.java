package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//@ConfigurationProperties(prefix = "wechat.pay")
public class WeChatPayConfig {

    private String mchId;          // 商户号
    private String mchKey;         // 商户密钥
    private String appId;          // 小程序appId
    // 证书配置
    private String certPath;       // apiclient_cert.pem路径
    private String keyPath;        // apiclient_key.pem路径
    private String notifyUrl;      // 回调地址

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getMchKey() {
        return mchKey;
    }

    public void setMchKey(String mchKey) {
        this.mchKey = mchKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

}
