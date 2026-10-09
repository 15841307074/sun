package com.htyoudao.youdao.module.demo.framework.rpc.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableDubbo(scanBasePackages = "com.htyoudao.youdao.module.demo.api")
public class RpcConfiguration {
}
