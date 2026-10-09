package com.htyoudao.youdao.module.promotion.job.usercoupon;

import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class UserCouponJob {

    @Resource
    private JobService jobService;

    @XxlJob("memberDayCouponJobHandler")
    public void memberDayCouponJobHandler() throws Exception {
        // 任务执行逻辑
        System.out.println("memberDayCouponJobHandler is running.");
        jobService.memberDayCoupon();
        XxlJobHelper.log("memberDayCouponJobHandler is running.");
    }

    @XxlJob("cancelDouyinCouponJobHandler")
    public void cancelDouyinCouponJobHandler() throws Exception {
        // 任务执行逻辑
        System.out.println("cancelDouyinCouponJobHandler is running.");
        jobService.cancelDouyinCoupon();
        XxlJobHelper.log("cancelDouyinCouponJobHandler is running.");
    }

}
