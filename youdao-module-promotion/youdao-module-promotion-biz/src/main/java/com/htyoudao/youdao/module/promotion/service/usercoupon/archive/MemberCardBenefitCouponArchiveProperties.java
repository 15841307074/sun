package com.htyoudao.youdao.module.promotion.service.usercoupon.archive;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 一级会员卡用户券归档动态配置。
 *
 * <p>配置来自 promotion-server 对应的 Nacos 配置文件，并通过 {@link RefreshScope}
 * 支持运行时刷新，无需重启 Pod。</p>
 */
@Component
@RefreshScope
public class MemberCardBenefitCouponArchiveProperties {

    /**
     * 是否启用每日 05:30 停止限制。
     *
     * <p>true：达到 05:30 后停止本次归档；false：忽略停止时间，可用于测试环境随时执行。
     * Nacos 未配置该属性时默认使用 true。</p>
     */
    @Value("${promotion.member-card-coupon-archive.daily-stop-enabled:true}")
    private boolean dailyStopEnabled;

    public boolean isDailyStopEnabled() {
        return dailyStopEnabled;
    }
}
