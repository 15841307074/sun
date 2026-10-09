package com.htyoudao.youdao.module.promotion.job.lottery;

import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LotteryJob {

    @Resource
    private JobService jobService;

    @XxlJob("lotteryReturnRealJobHandler")
    public void lotteryReturnReal() {
        System.out.println("lotteryReturnRealJobHandler is running.");
        jobService.lotteryReturnReal();
        XxlJobHelper.log("lotteryReturnRealJobHandler is running.");
    }
    @XxlJob("lotteryPoolResetJobHandler")
    public void lotteryPoolResetJobHandler() {
        System.out.println("lotteryPoolResetJobHandler is running.");
        jobService.lotteryPoolReset();
        XxlJobHelper.log("lotteryPoolResetJobHandler is running.");
    }
}
