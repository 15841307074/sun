package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryPrizeReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import java.util.List;

/** LotteryScopeService 服务契约；实现位于 impl 包。 */
public interface LotteryScopeService {
    List<Long> tagIds(long activityId);

    List<Long> replace(ActivityDO activity, List<Long> tagIds, List<Long> storeIds);

    List<LotteryPrizeReqVO> template(LotterySettingsDO settings);

    void prepareLegacyPrizes(LotterySettingsDO settings);

    void syncPrizes(LotterySettingsDO settings, List<LotteryPrizeReqVO> template, List<Long> targets,
                           Integer previousPool);

    boolean refreshMembership(long activityId, long storeId);

    boolean matches(long activityId, long storeId);
}
