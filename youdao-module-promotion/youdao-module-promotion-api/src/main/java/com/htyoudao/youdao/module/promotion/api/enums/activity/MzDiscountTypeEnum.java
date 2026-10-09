package com.htyoudao.youdao.module.promotion.api.enums.activity;

/**
 * 满赠活动优惠类型枚举
 */
public enum MzDiscountTypeEnum {

    BUY_N_YUAN_GIFT(1, "满N元赠商品"),
    BUY_N_ITEMS_GIFT(2, "满N件赠商品");

    private final int code;
    private final String description;

    MzDiscountTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MzDiscountTypeEnum of(int code) {
        for (MzDiscountTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的满赠优惠类型: " + code);
    }
}
