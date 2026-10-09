package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author dht
 */
@Builder
@Data
public class UsedCouponReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3614138033312134472L;
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

    /**
     * 是否是退款，0：否，1：是
     */
    private int isRefundAction;
}
