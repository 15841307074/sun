package com.htyoudao.youdao.module.infra.framework.file.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AliyunGreenProperties.class)
public class AliyunGreenConfiguration {
}
