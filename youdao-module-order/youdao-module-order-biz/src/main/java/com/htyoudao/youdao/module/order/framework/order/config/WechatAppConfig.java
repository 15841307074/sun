package com.htyoudao.youdao.module.order.framework.order.config;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 全局MQ配置类
 *
 * @author dongyan
 */
@Component
@Data
@RefreshScope
public class WechatAppConfig {
    private Map<String, Map<String, String>> weChatApps;

    public WechatAppConfig(@Value("${wechat.apps}") String apps) {
        weChatApps = JSON.parseObject(apps, Map.class);
    }

    public Map<String, String> getWeChatApp(String bussinessId) {
        return weChatApps.get(bussinessId);
    }

    public String getAppId(Long bussinessId) {
        return this.getWeChatApp(bussinessId.toString()).get("appId");
    }

    public String getAppSecret(Long bussinessId) {
        return this.getWeChatApp(bussinessId.toString()).get("appSecret");
    }
}


