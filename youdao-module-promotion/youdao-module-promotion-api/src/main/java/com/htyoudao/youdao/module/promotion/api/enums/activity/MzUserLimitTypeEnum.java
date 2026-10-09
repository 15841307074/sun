package com.htyoudao.youdao.module.promotion.api.enums.activity;

/**
 * 满赠活动用户参与限制枚举
 */
public enum MzUserLimitTypeEnum {

    NO_LIMIT(0, "不限制用户参与活动次数"),
    PER_DAY(1, "活动期间内每人每天可参与"),
    PER_TOTAL(2, "活动期间内每人最多可参与");

    private final int code;
    private final String description;

    MzUserLimitTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MzUserLimitTypeEnum of(int code) {
        for (MzUserLimitTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的用户参与限制类型: " + code);
    }
}
