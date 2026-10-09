package com.htyoudao.youdao.module.promotion.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class PromotionExportProperties {

    @Value("${userCoupon.exportLimit:300000}")
    private Long userCouponExportLimit;

    public Long getUserCouponExportLimit() {
        return userCouponExportLimit;
    }
}
