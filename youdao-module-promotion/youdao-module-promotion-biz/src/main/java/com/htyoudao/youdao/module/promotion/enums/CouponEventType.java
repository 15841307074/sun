package com.htyoudao.youdao.module.promotion.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author dht
 */

@Getter
@AllArgsConstructor
public enum CouponEventType {

    COUPON_SHARE("优惠券分享","coupon_share"),
    COUPON_VIEW("优惠券进页面","coupon_view")

    ;
    private final String name;
    private final String code;
}
