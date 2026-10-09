package com.htyoudao.youdao.module.promotion.job.activity;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.StockPreheatService;
import com.htyoudao.youdao.module.system.api.business.BusinnessApi;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ActivityJob {

    @Resource
    private StockPreheatService stockPreheatService;

    @DubboReference
    private BusinnessApi businnessApi;

    @XxlJob("preheatNextTimeStock")
    public void preheatNextTimeStock() {
        Integer time = LocalDateTime.now().plusHours(1).getHour();
        log.info("preheatNextTimeStock:{} is running.", time);

        for (BusinessDTO checkedDatum : businnessApi.listAll().getCheckedData()) {
            BusinessContextHolder.setBusinessId(checkedDatum.getId());
            stockPreheatService.preheatStock(time);
        }
        XxlJobHelper.log("preheatNextTimeStock:{} is running.", time);
    }
}
