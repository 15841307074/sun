package com.htyoudao.youdao.module.order.enums;

import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 通用状态枚举
 *
 * @author 0090
 */
@Getter
public enum CouponStatusEnum   {
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

    @Getter
    private int code;
    @Getter
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
