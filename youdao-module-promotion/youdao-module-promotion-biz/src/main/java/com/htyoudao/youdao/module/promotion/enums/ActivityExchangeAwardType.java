package com.htyoudao.youdao.module.promotion.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;

/**
 * 集点活动兑换奖品类型枚举
 * @author 33483
 */
public enum ActivityExchangeAwardType implements ArrayValuable<Integer> {

    COUPON(1,"优惠券"),
    COUPON_PACKAGE(2,"优惠券包");

    private final Integer code;
    private final String description;

    ActivityExchangeAwardType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举
     */
    public static ActivityExchangeAwardType getByCode(int code) {
        for (ActivityExchangeAwardType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据description获取枚举
     */
    public static ActivityExchangeAwardType getByDescription(String description) {
        for (ActivityExchangeAwardType type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

}
