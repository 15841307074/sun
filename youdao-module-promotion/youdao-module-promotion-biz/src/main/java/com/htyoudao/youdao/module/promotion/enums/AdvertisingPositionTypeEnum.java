package com.htyoudao.youdao.module.promotion.enums;

import lombok.Getter;

@Getter
public enum AdvertisingPositionTypeEnum {



    HOME_PAGE(1,"首页"),
    ORDERING_PAGE(2,"点餐页页"),
    ORDER_SETTLEMENT_PAGE(3,"订餐及结算页"),
    OTHER(4,"其他"),
    ;




    AdvertisingPositionTypeEnum(Integer type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 状态值
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

}
