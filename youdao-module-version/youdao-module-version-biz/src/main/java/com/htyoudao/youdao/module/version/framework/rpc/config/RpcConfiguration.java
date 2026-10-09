package com.htyoudao.youdao.module.version.framework.rpc.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableDubbo(scanBasePackages = "com.htyoudao.youdao.module.version.api")
public class RpcConfiguration {
}
