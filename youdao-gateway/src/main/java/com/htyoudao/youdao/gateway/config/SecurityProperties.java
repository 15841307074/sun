package com.htyoudao.youdao.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置属性
 *
 * @author lqman
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * 是否启用安全过滤器（总开关）
     */
    private boolean enabled = true;

    /**
     * 是否校验businessId
     */
    private boolean checkBusiness = true;

    /**
     * 签名配置
     */
    private final Sign sign = new Sign();

    /**
     * 防重放配置
     */
    private final Replay replay = new Replay();

    /**
     * 白名单路径，直接跳过所有安全检查
     */
    private final List<String> whiteList = new ArrayList<>();

    /**
     * 签名配置类
     */
    @Data
    public static class Sign {
        /**
         * 是否启用签名验证
         */
        private boolean enabled = true;

        /**
         * 签名密钥
         */
        private String secret = "CLOUD_SECRET_REQUIRED";

        /**
         * 签名过期时间（秒）
         */
        private long expireSeconds = 300;
    }

    /**
     * 防重放配置类
     */
    @Data
    public static class Replay {
        /**
         * 是否启用防重放检查
         */
        private boolean enabled = true;

        /**
         * 防重放窗口时间（秒）
         */
        private int windowSeconds = 60;
    }
}
