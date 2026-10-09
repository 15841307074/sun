package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedPacketResult {
    private boolean success;
    private String paymentNo;    // 微信付款单号
    private String paymentTime;  // 付款成功时间
    private String errorMsg;     // 错误信息

    public static RedPacketResult success(String paymentNo, String paymentTime) {
        return new RedPacketResult(true, paymentNo, paymentTime, null);
    }

    public static RedPacketResult fail(String errorMsg) {
        return new RedPacketResult(false, null, null, errorMsg);
    }
}
