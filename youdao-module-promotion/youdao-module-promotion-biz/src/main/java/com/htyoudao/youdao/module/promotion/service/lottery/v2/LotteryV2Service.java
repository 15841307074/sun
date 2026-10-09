package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;

/** LotteryV2Service 服务契约；实现位于 impl 包。 */
public interface LotteryV2Service {
    boolean enabled(LotterySettingsCacheDataVO cfg);

    LotteryUserLogVO drawAfterResultMiss(LotteryVO req,LotterySettingsCacheDataVO cfg);

    LotteryUserLogVO query(LotteryVO req);

    LotteryUserLogVO result(LotteryLedger.Draw draw);
}
