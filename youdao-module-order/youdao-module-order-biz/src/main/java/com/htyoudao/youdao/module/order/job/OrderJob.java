package com.htyoudao.youdao.module.order.job;

import com.htyoudao.youdao.module.order.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderJob {

    @Resource
    private JobService jobService;

    @XxlJob("updateCancelOrderTaskHandler")
//    @TenantJob
    public void updateCancelOrderTask() {
        log.info("updateCancelOrderTaskHandler is running.");
        jobService.updateCancelOrderTask();
        XxlJobHelper.log("updateCancelOrderTaskHandler is running.");
    }

    /**
     * 每天 23 点兜底取消当天已支付但未接单的代取订单。
     */
    public void cancelWaitingAcceptErrandOrdersScheduledTask() {
        this.doCancelWaitingAcceptErrandOrdersTask();
        XxlJobHelper.log("cancelWaitingAcceptErrandOrdersScheduledTask is running.");
    }

    @XxlJob("cancelWaitingAcceptErrandOrdersTaskHandler")
    public void cancelWaitingAcceptErrandOrdersXxlTask() {
        this.doCancelWaitingAcceptErrandOrdersTask();
        XxlJobHelper.log("cancelWaitingAcceptErrandOrdersTaskHandler is running.");
    }

    private void doCancelWaitingAcceptErrandOrdersTask() {
        log.info("cancelWaitingAcceptErrandOrdersTaskHandler is running.");
        jobService.cancelTodayWaitingAcceptErrandOrders();
    }

    /**
     * 兜底完成当天已接单或配送中但未确认收货的代取订单。
     */
    public void completeAcceptedErrandOrdersScheduledTask() {
        this.doCompleteAcceptedErrandOrdersTask();
        XxlJobHelper.log("completeAcceptedErrandOrdersScheduledTask is running.");
    }

    @XxlJob("completeAcceptedErrandOrdersTaskHandler")
    public void completeAcceptedErrandOrdersXxlTask() {
        this.doCompleteAcceptedErrandOrdersTask();
        XxlJobHelper.log("completeAcceptedErrandOrdersTaskHandler is running.");
    }

    private void doCompleteAcceptedErrandOrdersTask() {
        log.info("completeAcceptedErrandOrdersTaskHandler is running.");
        jobService.completeTodayAcceptedErrandOrders();
    }

    /**
     * 周报
     */
    @XxlJob("reportWeekTaskHandler")
    public void reportWeekTask() {
        log.info("reportWeekTaskHandler is running.");
        jobService.reportTotalWeek();
        jobService.reportCommodityWeek();
        jobService.reportWarehouseWeek();
        jobService.reportStoreSalesWeek();
        jobService.reportStasticsWeek();
        jobService.reportAppSalesnumWeek();
        XxlJobHelper.log("reportWeekTaskHandler is running.");
    }

    /**
     * 月报
     */
    @XxlJob("reportMonthTaskHandler")
    public void reportMonthTask() {
        log.info("reportMonthTaskHandler is running.");
        jobService.reportTotalMonth();
        jobService.reportWarehouseMonth();
        jobService.reportAppSalesnumMonth();
        jobService.reportCommodityMonth();
        jobService.reportStoreSalesMonth();
        jobService.reportStasticsMonth();
        jobService.reportBuyProportionMonth();
        jobService.reportBuyAmountMonth();
        XxlJobHelper.log("reportMonthTaskHandler is running.");
    }
}
