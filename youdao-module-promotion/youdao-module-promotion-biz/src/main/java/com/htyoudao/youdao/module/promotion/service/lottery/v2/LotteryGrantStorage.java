package com.htyoudao.youdao.module.promotion.service.lottery.v2;


/** LotteryGrantStorage 服务契约；实现位于 impl 包。 */
public interface LotteryGrantStorage {
    boolean claimPhysicalReturn(LotteryLedger.Draw draw);

    void coupon(LotteryLedger.Draw draw);

    void log(LotteryLedger.Draw draw);
}
