package com.htyoudao.youdao.module.commodity.job;

import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.module.commodity.service.job.JobService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommodityJob {

    @Resource
    private JobService jobService;

    @XxlJob("updateCommoditySaleTaskHandler")
    public void updateCommoditySaleTaskHandler() {
        log.info("updateCommoditySaleTaskHandler is running.");
        jobService.updateCommoditySaleTaskHandler();
        XxlJobHelper.log("updateCommoditySaleTaskHandler is running.");
    }




    @XxlJob("commodityCacheReloadHandler")
    public void commodityCacheReloadHandler() {
        log.info("commodityCacheReloadHandler is running.");
        String jobParam = XxlJobHelper.getJobParam();
        Long businessId = 10L;
        if (StringUtils.isEmpty(jobParam)){
            businessId = Long.valueOf(jobParam);
        }
        jobService.commodityCacheReloadHandler(List.of(), businessId);
        XxlJobHelper.log("commodityCacheReloadHandler is running.");
    }
}
