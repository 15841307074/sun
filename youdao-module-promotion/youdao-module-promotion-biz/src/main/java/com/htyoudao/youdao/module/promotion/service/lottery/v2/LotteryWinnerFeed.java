package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryRecentWinnerRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import java.util.List;

/** LotteryWinnerFeed 服务契约；实现位于 impl 包。 */
public interface LotteryWinnerFeed {
    List<LotteryRecentWinnerRespVO> latest(Long lotteryId);

    void afterLogSaved(long activityId, LotteryLogDO row);
}
