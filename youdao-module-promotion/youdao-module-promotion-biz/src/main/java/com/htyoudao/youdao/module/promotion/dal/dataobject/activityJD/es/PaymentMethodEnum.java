package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es;

import lombok.AllArgsConstructor;
import lombok.Getter;

// PaymentCode 枚举（支付方式）
@Getter
@AllArgsConstructor
public enum PaymentMethodEnum {
    /**
     * 现金
     */
    CASH(0, "现金", "cash"),

    /**
     * 微信支付
     */
    WECHAT_PAYMENT(1, "微信支付", "wechat"),

    /**
     * 支付宝支付
     */
    ALIPAY_PAYMENT(2, "支付宝支付", "aliPay");

    private int code;

    private String message;

    private String engMsg;

}