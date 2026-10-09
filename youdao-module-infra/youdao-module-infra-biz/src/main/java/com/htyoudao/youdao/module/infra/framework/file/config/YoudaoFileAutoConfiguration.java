package com.htyoudao.youdao.module.infra.framework.file.config;

import com.htyoudao.youdao.module.infra.framework.file.core.client.FileClientFactory;
import com.htyoudao.youdao.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author 0090
 */
@Configuration(proxyBeanMethods = false)
public class YoudaoFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
