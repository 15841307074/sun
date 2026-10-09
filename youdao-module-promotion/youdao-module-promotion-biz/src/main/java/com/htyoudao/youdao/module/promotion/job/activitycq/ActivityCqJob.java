package com.htyoudao.youdao.module.promotion.job.activitycq;

import com.htyoudao.youdao.module.promotion.service.activityCqFree.ActivityCqFreeCodeGrantService;
import com.htyoudao.youdao.module.promotion.service.activityCqDraw.ActivityCqDrawService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ActivityCqJob {

    @Resource
    private ActivityCqDrawService activityCqDrawService;
    @Resource
    private ActivityCqFreeCodeGrantService activityCqFreeCodeGrantService;

    @XxlJob("activityCqDrawJobHandler")
    public void activityCqDrawJobHandler() {
        activityCqDrawService.executeDueDraw();
        XxlJobHelper.log("activityCqDrawJobHandler is running.");
    }

    @XxlJob("activityCqDailyFreeCodeGrantJobHandler")
    public void activityCqDailyFreeCodeGrantJobHandler() {
        activityCqFreeCodeGrantService.executeDailyFreeCodeGrant();
        XxlJobHelper.log("activityCqDailyFreeCodeGrantJobHandler is running.");
    }
}
