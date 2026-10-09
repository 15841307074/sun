package com.htyoudao.youdao.module.member.framework.app.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * wechat配置类
 *
 * @author dongyan
 */
@Component
@Data
@RefreshScope
public class WechatAppConfig {
    private Map<String, Map<String, String>> weChatApps;
    private String tokenAuth;
    private String tokenUrl;

    public WechatAppConfig(@Value("${wechat.apps}") String apps, @Value("${wechat.token.auth}") String tokenAuth,
                           @Value("${wechat.token.url}") String tokenUrl) {
        weChatApps = JSON.parseObject(apps, new TypeReference<>() {
        });
        this.tokenAuth = tokenAuth;
        this.tokenUrl = tokenUrl;
    }

    public Map<String, String> getWeChatApp(String businessId) {
        return weChatApps.get(businessId);
    }

    public String getAppId(String businessId) {
        return this.getWeChatApp(businessId).get("appId");
    }

    public String getAppSecret(String businessId) {
        return this.getWeChatApp(businessId).get("appSecret");
    }
}


