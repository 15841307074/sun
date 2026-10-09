package com.htyoudao.youdao.module.promotion.api.enums.activity;

public enum ActivityTypeEnum {

    NJ_NZ(1, "n件n折"),
    SEC_KILL(2, "秒杀"),
    LOTTERY(3, "抽奖"),
    JD(4, "集点"),
    MJ(5, "满减满折"),
    JK(6, "集卡"),
    CQ(7, "抽签"),
    SIGN(8, "签到活动"),
    ANSWER(9, "有奖问答"),
    VOTE(10,"投票"),
    MZ(11, "满赠");

    private final int code;
    private final String description;

    ActivityTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ActivityTypeEnum of(int code) {
        for (ActivityTypeEnum scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        throw new IllegalArgumentException("未知的活动适用类型: " + code);
    }

    public static String getMessageByCode(int code) {
        ActivityTypeEnum[] values = ActivityTypeEnum.values();
        for (ActivityTypeEnum value : values) {
            if (code == value.getCode()) {
                return value.getDescription();
            }
        }
        return null;
    }

    public static ActivityTypeEnum getEnumByCode(int code) {
        ActivityTypeEnum[] values = ActivityTypeEnum.values();
        for (ActivityTypeEnum value : values) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return null;
    }
}
