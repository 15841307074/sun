package com.htyoudao.youdao.module.order.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1.固定搭配套餐，2.分组可选套餐, 3.单品
 */
@Getter
@AllArgsConstructor
public enum SetmealTypeEnum {

    /**
     * 固定搭配套餐
     */
    FIXED(1, "固定搭配套餐"),
    /**
     * 分组可选套餐
     */
    GROUP(2, "分组可选套餐"),
    /**
     * 单品
     */
    SINGLE(3, "单品"),
    ;

    private final int code;

    private final String message;
}
