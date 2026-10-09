package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "promotion.lottery")
public class LotteryProperties {
    /** 回退时只暂停新请求，已受理的发奖任务继续执行。 */
    private boolean accepting = true;
    private int drawRate = 100;
    private int queryRate = 1000;
    private int resultRate = 300;
    private int taskRate = 200;
    private int activityDrawRate = 60;
    private int memberDrawRate = 1;
    private int drawConcurrency = 8;
    private int grantConcurrency = 4;
    private int connectionBudget = 16;
    private int configTtlSeconds = 1800;
    private int listTtlSeconds = 30;
    private int resultTtlSeconds = 86400;
    private int refillConcurrency = 4;
}
