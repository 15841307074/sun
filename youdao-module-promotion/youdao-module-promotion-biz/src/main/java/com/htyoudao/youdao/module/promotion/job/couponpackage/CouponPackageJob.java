package com.htyoudao.youdao.module.promotion.job.couponpackage;

import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author dht
 */
@Component
@Slf4j
public class CouponPackageJob {


    @Resource
    private JobService jobService;


    @XxlJob("automaticDistributionOnMemberDaysJobHandler")
    public void automaticDistributionOnMemberDaysJobHandler() {
        log.info("automaticDistributionOnMemberDaysJobHandler is running.");
        jobService.automaticDistributionOnMemberDaysJobHandler();
        XxlJobHelper.log("automaticDistributionOnMemberDaysJobHandler is running.");
    }

}
