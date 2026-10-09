package com.htyoudao.youdao.module.promotion.enums;

import lombok.Getter;

import java.util.Objects;

public enum CouponTypeEnum {
    FULL_DISCOUNT_COUPON(0,"满减券"),
    DIRECT_DISCOUNT_COUPON(1,"折扣券"),
    COUPON(2,"兑换券")
    ;

    @Getter
    private int code;
    @Getter
    private String message;

    CouponTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessageByCode(int code){
        CouponTypeEnum[] values = CouponTypeEnum.values();
        for (CouponTypeEnum value : values) {
            if (Objects.equals(value.getCode(),code)){
                return value.getMessage();
            }
        }
        return null;
    }
}