package com.htyoudao.youdao.module.promotion.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;

/**
 * 抽签活动奖品类型枚举。
 */
public enum ActivityCqPrizeTypeEnum implements ArrayValuable<Integer> {

    COUPON(1, "优惠券"),
    POINTS(2, "积分"),
    PHYSICAL(3, "实物"),
    NO_PRIZE(4, "未中奖"),
    RED_PACKET(5, "现金红包"),
    COUPON_PACKAGE(6, "优惠券包"),
    GRAND_PRIZE(7, "大奖");

    private final Integer code;
    private final String description;

    ActivityCqPrizeTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ActivityCqPrizeTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ActivityCqPrizeTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getDescriptionByCode(Integer code) {
        ActivityCqPrizeTypeEnum value = getByCode(code);
        return value == null ? "未知" : value.getDescription();
    }

    @Override
    public Integer[] array() {
        return new Integer[0];
    }
}
