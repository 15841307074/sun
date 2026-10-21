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
    @Resource private com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryIdentityBackfillService identityBackfill;

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
    /** 手动补历史映射：{"businessId":10,"afterId":0,"maxRows":10000}。 */
    @XxlJob("lotteryMemberIdentityBackfillJobHandler")
    public void backfillMemberIdentities() throws com.fasterxml.jackson.core.JsonProcessingException {
        var parameters=new com.fasterxml.jackson.databind.ObjectMapper().readTree(XxlJobHelper.getJobParam());
        long business=parameters.path("businessId").asLong();
        if(business<=0)throw new IllegalArgumentException("businessId必须为正整数");
        Long previous=com.htyoudao.youdao.framework.context.BusinessContextHolder.getBusinessId();
        try {
            com.htyoudao.youdao.framework.context.BusinessContextHolder.setBusinessId(business);
            var progress=identityBackfill.run(parameters.path("afterId").asLong(0),parameters.path("maxRows").asInt(10000));
            XxlJobHelper.log("历史抽奖映射补齐 businessId={}, lastId={}, scanned={}, mapped={}, more={}",business,progress.lastId(),progress.scanned(),progress.mapped(),progress.more());
        } finally {com.htyoudao.youdao.framework.context.BusinessContextHolder.setBusinessId(previous);}
    }
}
