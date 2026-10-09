package com.htyoudao.youdao.module.order.enums;

import com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil;
import com.htyoudao.youdao.module.system.enums.ErrorCodeConstants;

import java.util.Arrays;

public enum NjnzDiscountTypeEnum {

    SECOND_HALF_PRICE(1, "第二件半价"),
    BUY_ONE_GET_ONE(2, "买一送一"),
    CUSTOM(3, "自定义优惠"),
    MJ(5, "满减满折");
    private final int code;
    private final String description;

    NjnzDiscountTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NjnzDiscountTypeEnum of(int code) {
        for (NjnzDiscountTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的优惠类型: " + code);
    }
}