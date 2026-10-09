package com.htyoudao.youdao.module.promotion.api.enums.activity;

/**
 * 满赠活动参与品类枚举（place_order_type=1 按品类限制时生效）
 */
public enum MzCategoryTypeEnum {

    ALL(1, "全部"),
    SINGLE(2, "仅单品"),
    COMBO(3, "仅套餐");

    private final int code;
    private final String description;

    MzCategoryTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MzCategoryTypeEnum of(int code) {
        for (MzCategoryTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的参与品类类型: " + code);
    }
}
