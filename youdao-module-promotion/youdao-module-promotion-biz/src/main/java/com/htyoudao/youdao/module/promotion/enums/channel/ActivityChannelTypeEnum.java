package com.htyoudao.youdao.module.promotion.enums.channel;

public enum ActivityChannelTypeEnum {

    NJ_NZ(1, "n件n折"),
    SEC_KILL(2, "秒杀"),
    TURNTABLE(3, "转盘"),
    FORTUNE(4, "福袋"),
    COLLECT(5, "集点"),
    COUPON(6, "优惠券"),
    COUPON_PACKAGE(7, "优惠券包"),
    MJMZ(8, "满减满折"),
    CARD(9, "集卡"),
    CQ(10, "抽签"),
    SIGN(11, "签到活动"),
    WJ(12, "问卷"),
    DT(13, "有奖答题"),
    VOTE(14, "投票");

    private final int code;
    private final String description;

    ActivityChannelTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ActivityChannelTypeEnum of(int code) {
        for (ActivityChannelTypeEnum scope : values()) {
            if (scope.code == code) {
                return scope;
            }
        }
        throw new IllegalArgumentException("未知的活动适用类型: " + code);
    }
}