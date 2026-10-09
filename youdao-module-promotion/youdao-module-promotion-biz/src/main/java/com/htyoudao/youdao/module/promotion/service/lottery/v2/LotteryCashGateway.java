package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import jakarta.servlet.http.HttpServletRequest;

/** LotteryCashGateway 服务契约；实现位于 impl 包。 */
public interface LotteryCashGateway {
    TransferToUser.TransferToUserResponse reconcileOrSend(LotteryLedger.Draw draw);

    TransferToUser.TransferToUserResponse queryForReissue(LotteryLedger.Draw draw);

    boolean verify(HttpServletRequest req,String body);
}
