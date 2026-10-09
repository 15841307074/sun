package com.htyoudao.youdao.framework.env.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 环境配置
 *
 * @author 0090
 */
@ConfigurationProperties(prefix = "youdao.env")
@Data
public class EnvProperties {

    public static final String TAG_KEY = "youdao.env.tag";

    /**
     * 环境标签
     */
    private String tag;

}
