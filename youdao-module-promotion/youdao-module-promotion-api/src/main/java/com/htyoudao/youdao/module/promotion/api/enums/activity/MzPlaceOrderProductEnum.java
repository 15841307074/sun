package com.htyoudao.youdao.module.promotion.api.enums.activity;

/**
 * 满赠活动下单商品枚举（place_order_type=2 按商品限制时生效）
 */
public enum MzPlaceOrderProductEnum {

    ALL(1, "全部商品"),
    SPECIFIED(2, "指定商品");

    private final int code;
    private final String description;

    MzPlaceOrderProductEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MzPlaceOrderProductEnum of(int code) {
        for (MzPlaceOrderProductEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的下单商品类型: " + code);
    }
}
