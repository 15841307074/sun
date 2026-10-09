package com.htyoudao.youdao.module.promotion.enums;

import lombok.Getter;

@Getter
public enum AdvertisingPositionStateEnum {



    HOME_PAGE_POP_UP_WINDOW(1,"首页弹窗"),
    HOME_PAGE_TOP_CAROUSEL(2,"首页顶部轮播"),
    HOME_PAGE_BOTTOM_CAROUSEL(3,"首页底部轮播"),
    HOME_PAGE_BOTTOM_ACTIVITY(4,"首页底部活动"),
    ORDERING_PAGE_POP_UP_WINDOW(5,"点餐页弹窗"),
    ORDERING_PAGE_TOP_CAROUSEL(6,"点餐页顶部轮播"),
    ORDERING_PAGE_BOTTOM_CAROUSEL(7,"点餐页底部轮播"),
    SETTLEMENT_PAGE_CAROUSEL(8,"结算页轮播"),
    ORDER_DETAILS_PAGE_CAROUSEL(9,"订单详情页轮播"),
    ORDER_LIST_PAGE_CAROUSEL(10,"订单列表轮播"),
    SPLASH_SCREEN_ADVERTISEMENT(11,"开屏广告"),
    FLOATING_WINDOW(12,"浮窗"),
    ;




    AdvertisingPositionStateEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
