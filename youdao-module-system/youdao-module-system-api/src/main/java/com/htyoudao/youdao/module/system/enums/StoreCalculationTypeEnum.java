package com.htyoudao.youdao.module.system.enums;

import lombok.Getter;

@Getter
public enum StoreCalculationTypeEnum {

    /**
     * 按商品
     */
    COMMODITY(0, "按商品"),

    /**
     * 按订单
     */
    ORDER(1, "按订单");

    private final int code;

    private final String message;

    StoreCalculationTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMsgByCode(int code) {
        StoreCalculationTypeEnum[] values = StoreCalculationTypeEnum.values();
        for (StoreCalculationTypeEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
