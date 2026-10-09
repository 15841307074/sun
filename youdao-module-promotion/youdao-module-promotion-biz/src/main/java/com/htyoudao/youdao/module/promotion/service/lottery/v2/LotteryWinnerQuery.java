package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import java.util.List;

/** LotteryWinnerQuery 服务契约；实现位于 impl 包。 */
public interface LotteryWinnerQuery {
    List<LotteryLogDO> latest(long businessId, long activityId, long settingsId);
}
