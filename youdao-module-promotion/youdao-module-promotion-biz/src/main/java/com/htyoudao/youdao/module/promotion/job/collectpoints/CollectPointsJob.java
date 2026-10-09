package com.htyoudao.youdao.module.promotion.job.collectpoints;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.service.job.JobService;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author dht
 */
@Component
@Slf4j
public class CollectPointsJob {

    @Resource
    private JobService jobService;

    @XxlJob("automaticPointsGoodsJobHandler")
    public void automaticPointsGoodsJobHandler() {
        Integer time = LocalDateTime.now().plusHours(1).getHour();
        log.info("automaticPointsGoodsJobHandler:{} is running.", time);
        jobService.automaticPointsGoodsJobHandler();
        XxlJobHelper.log("automaticPointsGoodsJobHandler:{} is running.", time);
    }
}
