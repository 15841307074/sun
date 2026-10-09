package com.htyoudao.youdao.module.commodity.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 删除全部门店商品开关。
 * 单独做成小 Bean，避免大 Service 长任务占住 RefreshScope 读锁后读到旧值。
 */
@Component
@RefreshScope
public class CommodityStoreSpuConfig {

    @Value("${commodity.store.delete-all-spu-enabled:false}")
    private Boolean deleteAllSpuEnabled;

    public boolean isDeleteAllSpuEnabled() {
        return Boolean.TRUE.equals(deleteAllSpuEnabled);
    }
}
