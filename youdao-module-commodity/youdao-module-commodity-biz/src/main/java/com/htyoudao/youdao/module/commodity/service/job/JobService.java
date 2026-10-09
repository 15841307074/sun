package com.htyoudao.youdao.module.commodity.service.job;


import java.util.List;

public interface JobService {

    void updateCommoditySaleTaskHandler();

    void commodityCacheReloadHandler(List<Long> storeIds, Long businessId);
}
