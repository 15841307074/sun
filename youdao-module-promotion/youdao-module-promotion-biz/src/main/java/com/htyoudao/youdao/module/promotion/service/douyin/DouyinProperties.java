package com.htyoudao.youdao.module.promotion.service.douyin;


import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "douyin")
public class DouyinProperties {
    private final String clientKey = "CLOUD_SECRET_REQUIRED";
    private final String clientSecret = "CLOUD_SECRET_REQUIRED";
    private final String baseUrl = "https://open.douyin.com";
    private final String accountId = "7485283719483066419";
    private final String transitAccount = "";

}
