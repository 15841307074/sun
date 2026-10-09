package com.htyoudao.youdao.module.order.enums;

import lombok.Getter;

@Getter
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

    PaymentMethodEnum(int code, String message, String engMsg) {
        this.code = code;
        this.message = message;
        this.engMsg = engMsg;
    }

    private final int code;

    private final String message;

    private final String engMsg;


    public static PaymentMethodEnum getEnumByCode(int code) {
        PaymentMethodEnum[] values = PaymentMethodEnum.values();
        for (PaymentMethodEnum value : values) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return null;
    }

    public static String getMsgByCode(int code) {
        PaymentMethodEnum[] values = PaymentMethodEnum.values();
        for (PaymentMethodEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }

    public static String getEngMsgByCode(int code) {
        PaymentMethodEnum[] values = PaymentMethodEnum.values();
        for (PaymentMethodEnum value : values) {
            if (code == value.getCode()) {
                return value.getEngMsg();
            }
        }
        return null;
    }

}
