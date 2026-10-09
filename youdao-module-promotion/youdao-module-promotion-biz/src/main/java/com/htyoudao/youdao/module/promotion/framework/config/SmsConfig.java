package com.htyoudao.youdao.module.promotion.framework.config;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.htyoudao.youdao.module.promotion.framework.config.properties.SmsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * details
 *
 * @author liuzhaowang
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfig {

    @Bean
    public Client smsClient(SmsProperties smsProperties) throws Exception {
        // 配置短信client
        return new Client(new Config()
                .setEndpoint(smsProperties.getEndpoint())
                .setAccessKeyId(smsProperties.getAccessKeyId())
                .setAccessKeySecret(smsProperties.getAccessKeySecret()));
    }

    @Bean
    public RuntimeOptions smsRuntimeOptions(SmsProperties smsProperties) {
        return new RuntimeOptions()
                .setAutoretry(smsProperties.getAutoRetry())
                .setMaxAttempts(smsProperties.getMaxRetryTimes());
    }
}
