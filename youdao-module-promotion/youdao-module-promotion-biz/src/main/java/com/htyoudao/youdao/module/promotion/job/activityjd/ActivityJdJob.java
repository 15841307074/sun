package com.htyoudao.youdao.module.promotion.job.activityjd;

import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ActivityJdJob {

    @Resource
    private JobService jobService;

    @XxlJob("activityJdJobHandler")
    public void resetPoint() {
        System.out.println("activityJdJobHandler is running.");
        jobService.resetPoint();
        XxlJobHelper.log("activityJdJobHandler is running.");
    }
}
