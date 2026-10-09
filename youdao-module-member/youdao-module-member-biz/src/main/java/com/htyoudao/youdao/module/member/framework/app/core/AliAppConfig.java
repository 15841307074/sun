package com.htyoudao.youdao.module.member.framework.app.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 支付宝app配置
 *
 * @author lqman
 */
@Component
@Data
@RefreshScope
public class AliAppConfig {

    private Map<String, Map<String, String>> aliApps;

    private List<String> syncUrls;


    public AliAppConfig(@Value("${zfb.apps}") String apps, @Value("${zfb.syncUrl}") String syncUrl) {
        aliApps = JSON.parseObject(apps, new TypeReference<>() {});
        syncUrls = Arrays.asList(syncUrl.split(","));
    }

    public Map<String, String> getAliApps(String projectOwnerShip) {
        return aliApps.get(projectOwnerShip);
    }

    public String getPrivateKey(String projectOwnerShip) {
        return this.getAliApps(projectOwnerShip).get("privateKey");
    }

    public String getAlipayPublicKey(String projectOwnerShip) {
        return this.getAliApps(projectOwnerShip).get("alipayPublicKey");
    }

    public String getSignVeriKey(String projectOwnerShip) {
        return this.getAliApps(projectOwnerShip).get("signVeriKey");
    }

    public String getDecryptKey(String projectOwnerShip) {
        return this.getAliApps(projectOwnerShip).get("decryptKey");
    }

    public String getAppId(String projectOwnerShip) {
        return this.getAliApps(projectOwnerShip).get("appId");
    }
}
