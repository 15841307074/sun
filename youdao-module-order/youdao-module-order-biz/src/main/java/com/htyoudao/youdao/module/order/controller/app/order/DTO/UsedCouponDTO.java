package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券使用传参dto
 */
@Data
public class UsedCouponDTO {
    /**
     * user_coupon表的id
     */
    private Long userCouponId;

    /**
     * 下单的门店id
     */
    private Long storeId;

    /**
     * 是否使用过，0：未使用，1：已使用
     */
    private Integer isUsed;

    /**
     * 使用时间
     */
    private Date useTime;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 购物车中商品的总数量
     */
    private Integer count;

    /**
     * 优惠额度
     */
    private BigDecimal couponPrice;

    /**
     * 三方支付金额
     */
    private BigDecimal payAmount;
}
