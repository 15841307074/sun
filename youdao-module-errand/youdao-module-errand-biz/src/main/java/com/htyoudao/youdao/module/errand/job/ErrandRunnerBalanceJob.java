package com.htyoudao.youdao.module.errand.job;

import com.htyoudao.youdao.module.errand.service.errandRunner.ErrandRunnerService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ErrandRunnerBalanceJob {

    @Resource
    private ErrandRunnerService errandRunnerService;

    @XxlJob("unfreezeExpiredRewards")
    public void unfreezeExpiredRewards() {
        log.info("unfreezeExpiredRewards is running.");
        int count = errandRunnerService.unfreezeExpiredRewards();
        if (count > 0) {
            log.info("跑腿赏金解冻完成，count: {}", count);
        }
        XxlJobHelper.log("unfreezeExpiredRewards is running.");
    }
}
