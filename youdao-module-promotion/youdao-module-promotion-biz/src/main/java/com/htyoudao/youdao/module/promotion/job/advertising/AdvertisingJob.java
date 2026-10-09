package com.htyoudao.youdao.module.promotion.job.advertising;

import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AdvertisingJob {

    @Resource
    private JobService jobService;

    @XxlJob("advertisementsJobHandler")
    public void screenAdvertisements() {
        System.out.println("advertisementsJobHandler is running.");
        jobService.screenAdvertisements();
        XxlJobHelper.log("advertisementsJobHandler is running.");
    }
}
