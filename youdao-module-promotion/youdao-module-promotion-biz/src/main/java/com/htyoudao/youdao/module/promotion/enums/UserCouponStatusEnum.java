package com.htyoudao.youdao.module.promotion.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;

/**
 * @author dht
 */
public enum UserCouponStatusEnum implements ArrayValuable<Integer> {

    USED(1, "已使用"),
    UNUSED(0, "未使用"),
    EXPIRED(2, "已过期");

    private final Integer code;
    private final String description;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    UserCouponStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer[] array() {
        return new Integer[0];
    }

    public static String getByCode(int code) {
        for (UserCouponStatusEnum type : values()) {
            if (type.code == code) {
                return type.getDescription();
            }
        }
        return null;
    }
}
