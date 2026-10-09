package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;

/** LotteryRuntimeLifecycle 服务契约；实现位于 impl 包。 */
public interface LotteryRuntimeLifecycle {
    boolean changeState(long activityId, Integer state);

    void initializeLegacy(LotterySettingsDO cfg);

    void reset(long a,String nextSession);

    void rejectLegacyWrite(Long id);
}
