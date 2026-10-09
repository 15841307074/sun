package com.htyoudao.youdao.module.promotion.api.enums.activity;

/**
 * 满赠活动下单类型枚举
 */
public enum MzPlaceOrderTypeEnum {

    BY_CATEGORY(1, "按品类限制"),
    BY_PRODUCT(2, "按商品限制");

    private final int code;
    private final String description;

    MzPlaceOrderTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MzPlaceOrderTypeEnum of(int code) {
        for (MzPlaceOrderTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的下单类型: " + code);
    }
}
