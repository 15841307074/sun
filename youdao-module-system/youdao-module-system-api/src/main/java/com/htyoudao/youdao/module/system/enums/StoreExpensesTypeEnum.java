package com.htyoudao.youdao.module.system.enums;

import lombok.Getter;

@Getter
public enum StoreExpensesTypeEnum {

    /**
     * 堂食
     */
    CANTEEN_FOOD(0, "堂食/外带打包费"),

    /**
     * 打包
     */
    TAKEAWAY_PACKAGE(1, "外卖打包费"),

    /**
     * 外卖
     */
    TAKEAWAY_DELIVERY(2, "外卖配送费"),

    /**
     * 校园配送
     */
    CAMPUS_DELIVERY(3, "校园配送费用");

    private final int code;

    private final String message;

    StoreExpensesTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMsgByCode(int code) {
        StoreExpensesTypeEnum[] values = StoreExpensesTypeEnum.values();
        for (StoreExpensesTypeEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
