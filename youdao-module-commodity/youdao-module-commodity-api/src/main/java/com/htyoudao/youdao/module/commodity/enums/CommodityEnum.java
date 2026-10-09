package com.htyoudao.youdao.module.commodity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品套餐定义枚举
 */
@Getter
@AllArgsConstructor
public enum CommodityEnum {

    SINGLE(1),//单品
    SETMAIL(2);//套餐

    public final int isSingle;

}
