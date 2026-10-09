package com.htyoudao.youdao.framework.sentinel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * sentinel自定义配置类
 *
 * @author lqman
 */
@Data
@ConfigurationProperties(prefix = "spring.cloud.sentinel.transport")
public class SentinelCustomProperties {

    private String serverName;

}
