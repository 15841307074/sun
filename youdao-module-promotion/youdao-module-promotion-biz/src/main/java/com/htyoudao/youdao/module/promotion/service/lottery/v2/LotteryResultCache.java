package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;

/** LotteryResultCache 服务契约；实现位于 impl 包。 */
public interface LotteryResultCache {
    /** 内部缓存元数据与前端响应分开，退奖结果不能被迟到的发奖结果覆盖。 */
    public record Entry(long storeId, LotteryUserLogVO result, String lifecycleStatus, long reissueSequence) {
        public Entry(long storeId,LotteryUserLogVO result,String lifecycleStatus) {this(storeId,result,lifecycleStatus,0);}
    }

    Entry get(long b,LotteryVO request);

    void publish(LotteryLedger.Draw draw,LotteryUserLogVO result);

    void invalidateForReissue(LotteryLedger.Draw draw);

    void invalidateForReissue(LotteryLedger.Draw draw,long sequence);
}
