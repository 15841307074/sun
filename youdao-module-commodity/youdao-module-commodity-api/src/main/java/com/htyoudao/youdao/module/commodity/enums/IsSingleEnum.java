package com.htyoudao.youdao.module.commodity.enums;

import lombok.Getter;

@Getter
public enum IsSingleEnum {

    /**
     * 单品
     */
    SINGLE(1, "单品"),

    /**
     * 套餐
     */
    PACKAGE(2, "套餐");

    private final int code;

    private final String message;

    IsSingleEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMsgByCode(int code) {
        IsSingleEnum[] values = IsSingleEnum.values();
        for (IsSingleEnum value : values) {
            if (code == value.getCode()) {
                return value.getMessage();
            }
        }
        return null;
    }
}
