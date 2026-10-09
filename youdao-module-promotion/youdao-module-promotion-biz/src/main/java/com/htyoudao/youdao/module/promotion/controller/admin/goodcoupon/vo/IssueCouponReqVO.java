package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import lombok.Data;

/**
 * @author dht
 */
@Data
public class IssueCouponReqVO {

    /**
     * 优惠券id
     */
    private Long couponId;

    /**
     * 发放数量
     */
    private Integer couponNum;

    /**
     * 会员手机号
     */
    private String memberMobile;
}
