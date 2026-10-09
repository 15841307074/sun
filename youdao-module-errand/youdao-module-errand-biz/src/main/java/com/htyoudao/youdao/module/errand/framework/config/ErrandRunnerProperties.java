package com.htyoudao.youdao.module.errand.framework.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 跑腿员业务配置。
 */
@Component
@ConfigurationProperties(prefix = "errand.runner")
@RefreshScope
@Validated
@Data
public class ErrandRunnerProperties {

    /**
     * 赏金冻结时长，单位：分钟。默认 36 小时。
     */
    @Min(1)
    private Integer rewardFreezeMinutes = 36 * 60;
}
