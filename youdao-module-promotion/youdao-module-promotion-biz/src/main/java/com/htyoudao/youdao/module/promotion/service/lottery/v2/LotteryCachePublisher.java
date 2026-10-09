package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;

/** LotteryCachePublisher 服务契约；实现位于 impl 包。 */
public interface LotteryCachePublisher {
    void enqueue(LotterySettingsDO settings);

    void assertNoPending(long activityId);

    void assertPrizeNoPending(long activityId, String code);

    void resizeStock(LotterySettingsDO settings, LotteryPrizeDO prize, int total);

    void initializeNewStock(LotterySettingsDO settings, LotteryPrizeDO prize);

    void retry();

    void publish(long b, long a);
}
