package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryReissueReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryReissueRespVO;

/** LotteryReissueService 服务契约；实现位于 impl 包。 */
public interface LotteryReissueService {
    LotteryReissueRespVO reissue(LotteryReissueReqVO reqVO);
}
