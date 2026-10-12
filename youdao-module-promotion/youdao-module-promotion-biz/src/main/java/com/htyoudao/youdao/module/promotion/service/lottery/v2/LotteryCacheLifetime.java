package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/** 活动展示与结果缓存到活动结束后七天过期，不随每次访问续期。 */
public final class LotteryCacheLifetime {
    private LotteryCacheLifetime() {}

    public static Duration remaining(LotterySettingsCacheDataVO settings) {
        return remaining(settings, Instant.now());
    }

    static Duration remaining(LotterySettingsCacheDataVO settings, Instant now) {
        Date end = settings == null ? null : settings.getLotteryEndTime();
        if (end == null && settings != null) end = settings.getEndDate();
        // 历史快照缺少结束时间时短期缓存，避免永久保留或重复回源。
        if (end == null) return Duration.ofMinutes(30);
        Duration remaining = Duration.between(now, Instant.ofEpochMilli(end.getTime()).plus(Duration.ofDays(7)));
        return remaining.isNegative() || remaining.isZero() ? Duration.ZERO : remaining;
    }
}
