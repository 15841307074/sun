package com.htyoudao.youdao.module.member.job.point;

import com.htyoudao.youdao.module.member.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PointJob {

    @Resource
    private JobService jobService;

    @XxlJob("updateMemberPointJobHandler")
//    @TenantJob
    public void updateMemberPointJobHandler() {
        log.info("updateMemberPointJobHandler is running.");
        jobService.updateMemberPointTask(0,0,0, 0);
        XxlJobHelper.log("updateMemberPointJobHandler is running.");
    }

    @XxlJob("clearExpiredPointsJobHandler")
//    @TenantJob
    public void clearExpiredPoints(Long businessId) {
        log.info("clearExpiredPointsJobHandler is running.");
        jobService.clearExpiredPoints( businessId);
        XxlJobHelper.log("clearExpiredPointsJobHandler is running.");
    }

    @XxlJob("nocHistoryOrderDataJobHandler")
//    @TenantJob
    public void nocHistoryOrderData() {
        log.info("nocHistoryOrderData is running.");
        jobService.updateHistoryOrderData();
        XxlJobHelper.log("nocHistoryOrderData is running.");
    }

    @XxlJob("nocHistoryOrderNumJobHandler")
//    @TenantJob
    public void nocHistoryOrderNum() {
        log.info("nocHistoryOrderNum is running.");
        jobService.nocHistoryOrderNum();
        XxlJobHelper.log("nocHistoryOrderNum is running.");
    }

    @XxlJob("updateMemberLabelJobHandler")
//    @TenantJob
    public void updateMemberLabel() {
        log.info("updateMemberLabel is running.");
        jobService.updateMemberLabel();
        XxlJobHelper.log("updateMemberLabel is running.");
    }

    @XxlJob("updateFirstOrderStoreIdJobHandler")
//    @TenantJob
    public void updateFirstOrderStoreId() {
        log.info("updateFirstOrderStoreId is running.");
        jobService.updateFirstOrderStoreId();
        XxlJobHelper.log("updateFirstOrderStoreId is running.");
    }


    @XxlJob("createMemberCrowdJobHandler")
//    @TenantJob
    public void createMemberCrowdJobHandler() throws IOException {
        log.info("createMemberCrowdJobHandler is running.");
        try{
            jobService.createMemberCrowdTask(0);
        }catch (Exception e){
            log.error("createMemberCrowdJobJob is error", e);
        }


        log.info("createMemberCrowdJobHandler is end.");
        XxlJobHelper.log("createMemberCrowdJobHandler is running.");
    }

    @XxlJob("updateMemberGroup")
    public void updateMemberGroup() {
        log.info("updateMemberGroup is running.");
        try{
            jobService.updateMemberGroup();
        }catch (Exception e){
            log.error("updateMemberGroup is error", e);
        }
        log.info("updateMemberGroup is end.");
        XxlJobHelper.log("updateMemberGroup is running.");
    }

}
