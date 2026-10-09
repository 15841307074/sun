package com.htyoudao.youdao.module.bpm.framework.rpc.config;

import com.htyoudao.youdao.module.bpm.api.event.CrmContractStatusListener;
import com.htyoudao.youdao.module.bpm.api.event.CrmReceivableStatusListener;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "bpmRpcConfiguration", proxyBeanMethods = false)
@EnableDubbo(scanBasePackages = "com.htyoudao.youdao.module.bpm.api")
public class RpcConfiguration {

    // ========== 特殊：解决微 yudao-cloud 微服务场景下，跨服务（进程）无法 Listener 的问题 ==========

    @Bean
    @ConditionalOnMissingBean(name = "crmReceivableStatusListener")
    public CrmReceivableStatusListener crmReceivableStatusListener() {
        return new CrmReceivableStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "crmContractStatusListener")
    public CrmContractStatusListener crmContractStatusListener() {
        return new CrmContractStatusListener();
    }

}
