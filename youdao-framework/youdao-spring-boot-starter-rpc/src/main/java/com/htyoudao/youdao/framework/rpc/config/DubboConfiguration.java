package com.htyoudao.youdao.framework.rpc.config;

import com.htyoudao.youdao.framework.rpc.core.properties.DubboCustomProperties;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * dubbo 配置类
 * @author lqman
 */
@AutoConfiguration
@EnableConfigurationProperties(DubboCustomProperties.class)
public class DubboConfiguration {

    /**
     * dubbo自定义IP注入(避免IP不正确问题)
     */
    @Bean
    public BeanFactoryPostProcessor customBeanFactoryPostProcessor() {
        return new CustomBeanFactoryPostProcessor();
    }

}
