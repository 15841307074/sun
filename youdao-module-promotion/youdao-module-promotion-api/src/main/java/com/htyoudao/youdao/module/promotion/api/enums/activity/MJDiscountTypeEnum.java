package com.htyoudao.youdao.module.promotion.api.enums.activity;

public enum MJDiscountTypeEnum {

    BUY_N_YUAN(1, "满N元"),
    BUY_N_ITEMS(2, "满N件");

    private final int code;
    private final String description;

    MJDiscountTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MJDiscountTypeEnum of(int code) {
        for (MJDiscountTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的优惠类型: " + code);
    }
}