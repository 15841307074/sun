package com.htyoudao.youdao.module.member.controller.admin.wecom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wecom")
@RefreshScope
public class WecomConfig {
//    private String corpId = "wwf55f41e781c09b02";//企业的
    private String corpId = "ww33bef5c6db3b679d";//0090应用的

    private String corpSecret = "CLOUD_SECRET_REQUIRED";//0090应用的
//    private String corpSecret = "CLOUD_SECRET_REQUIRED";//企业id

    private String token = "CLOUD_SECRET_REQUIRED";
    private String encodingAESKey = "CLOUD_SECRET_REQUIRED";
}
