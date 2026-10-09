package com.htyoudao.youdao.module.errand.framework.rpc.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableDubbo(scanBasePackages = "com.htyoudao.youdao.module.errand.api")
public class RpcConfiguration {
}
