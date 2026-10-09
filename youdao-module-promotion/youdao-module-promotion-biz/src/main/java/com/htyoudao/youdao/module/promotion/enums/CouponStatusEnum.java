package com.htyoudao.youdao.module.promotion.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author 33483
 */

@Getter
public enum CouponStatusEnum {
    TO_BE_USED(0,"待使用"),
    USED(1,"已使用"),
    NOT_STARTED(2,"未开始"),
    PROCEEDING(3,"进行中"),
    EXPIRED(4,"已到期")
    ;


    CouponStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public static String getMessageByCode(int code){
        CouponStatusEnum[] values = CouponStatusEnum.values();
            for (CouponStatusEnum value : values) {
                if (Objects.equals(value.getCode(),code)){
                    return value.getMessage();
                }
            }
        return null;
    }


}