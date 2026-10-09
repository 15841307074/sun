package com.htyoudao.youdao.module.promotion.api.enums.activity;

/**
 * 满赠活动门店奖励库存枚举
 */
public enum MzGiftInventoryTypeEnum {

    SHARED(1, "共用库存"),
    INDEPENDENT(2, "独立库存");

    private final int code;
    private final String description;

    MzGiftInventoryTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MzGiftInventoryTypeEnum of(int code) {
        for (MzGiftInventoryTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的库存类型: " + code);
    }
}
