package com.htyoudao.youdao.module.promotion.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lqman
 */
@ConfigurationProperties(prefix = "sms")
@Data
public class SmsProperties {
    /**
     * 访问id
     */
    private String accessKeyId;
    /**
     * 访问密钥
     */
    private String accessKeySecret;
    /**
     * 服务区地址
     */
    private String endpoint;

    /**
     * 自动重试
     */
    private Boolean autoRetry;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;
}
