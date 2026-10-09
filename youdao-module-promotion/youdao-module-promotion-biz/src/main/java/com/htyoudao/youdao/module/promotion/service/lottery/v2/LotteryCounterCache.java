package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import java.util.Map;

/** LotteryCounterCache 服务契约；实现位于 impl 包。 */
public interface LotteryCounterCache {
    void changed(long b,long a,long member);

    Map<String,long[]> read(long b,long a,long m,String period);

    void retry();
}
