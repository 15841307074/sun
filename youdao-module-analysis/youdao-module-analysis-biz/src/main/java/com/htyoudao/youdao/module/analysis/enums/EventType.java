package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
    IN_STORE("进店","in_store"),
    CLICK_PRODUCT("点击商品","click_product"),
    ADD_CART("加入购物车","add_cart"),
    SHARE("分享","share"),

    ACTIVITY("活动","activity"),
    COUPON_SHARE("优惠券分享","coupon_share"),
    COUPON_VIEW("优惠券进页面","coupon_view"),
    COUPON_CLAIM("优惠券领取","coupon_claim"),
    ;
    private final String name;
    private final String code;
}
