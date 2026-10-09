package com.htyoudao.youdao.framework.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cache 配置项
 *
 * @author Wanwan
 */
@ConfigurationProperties("youdao.cache")
@Data
@Validated
public class YoudaoCacheProperties {

    /**
     * {@link #redisScanBatchSize} 默认值
     */
    private static final Integer REDIS_SCAN_BATCH_SIZE_DEFAULT = 1000;

    /**
     * redis scan 一次返回数量
     */
    private Integer redisScanBatchSize = REDIS_SCAN_BATCH_SIZE_DEFAULT;

}
