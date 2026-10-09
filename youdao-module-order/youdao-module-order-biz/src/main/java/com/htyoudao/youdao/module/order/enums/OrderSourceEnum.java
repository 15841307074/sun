package com.htyoudao.youdao.module.order.enums;

import lombok.Getter;

/**
 * <p>
 * 订单来源 1 小程序付款单 2 点餐机付款单 3 小程序现金单 4 点餐机现金单 5 外卖单 6 拼单 7 秒杀 8 代取单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-24
 */
@Getter
public enum OrderSourceEnum {

    /**
     * 小程序付款单
     */
    X_PAYMENT_ORDER(1, "小程序付款单"),

    /**
     * 点餐机付款单
     */
    K_PAYMENT_ORDER(2, "点餐机付款单"),

    /**
     * 小程序现金单
     */
    X_CASH_ORDER(3, "小程序现金单"),

    /**
     * 点餐机现金单
     */
    K_CASH_ORDER(4, "点餐机现金单"),

    /**
     * 外卖单
     */
    TAKE_OUT_ORDER(5, "外卖单"),

    /**
     * 拼单
     */
    SPLICING_ORDER(6, "拼单"),

    /**
     * 秒杀
     */
    SECKILL_ORDER(7, "秒杀"),

    /**
     * 代取单
     */
    ERRAND_ORDER(8, "代取单");

    private final int code;

    private final String message;

    OrderSourceEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static OrderSourceEnum getOrderSourceEnumByCode(int code) {
        OrderSourceEnum[] values = OrderSourceEnum.values();
        for (OrderSourceEnum value : values) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return null;
    }
}
