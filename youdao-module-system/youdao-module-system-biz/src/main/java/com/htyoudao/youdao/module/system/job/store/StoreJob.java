package com.htyoudao.youdao.module.system.job.store;

import com.htyoudao.youdao.module.system.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class StoreJob {

    @Resource
    private JobService jobService;

    @XxlJob("storeStatusJobHandler")
    public void storeStatusJob() {
        System.out.println("storeStatusJob is running.");
        jobService.storeStatusJob();
        XxlJobHelper.log("storeStatusJob is running.");
    }
}
