package com.htyoudao.youdao.module.system.service.version.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "app-version")
public class AppVersionProperties {

    private VersionNode defaults = new VersionNode();

    private Map<Long, VersionNode> business = new HashMap<>();

    @Data
    public static class VersionNode {
        private String applet;
        private String orderMachine;
    }
}
