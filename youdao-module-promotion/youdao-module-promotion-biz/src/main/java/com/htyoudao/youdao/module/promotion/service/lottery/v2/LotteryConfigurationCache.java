package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import java.util.List;

/** LotteryConfigurationCache 服务契约；实现位于 impl 包。 */
public interface LotteryConfigurationCache {
    LotterySettingsCacheDataVO settings(long id);

    List<LotteryPrizeDO> prizes(LotterySettingsCacheDataVO cfg,long storeId);

    List<LotteryPrizeDO> displayPrizes(LotterySettingsCacheDataVO cfg,long storeId);

    List<Long> storeSettings(long storeId);
}
