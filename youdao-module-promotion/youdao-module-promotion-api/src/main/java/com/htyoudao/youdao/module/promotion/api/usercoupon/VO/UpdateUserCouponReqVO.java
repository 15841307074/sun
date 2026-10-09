package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateUserCouponReqVO implements Serializable {

    private static final long serialVersionUID = 6966049376329896974L;
    /**
     * 优惠券编码
     */
    private String couponCode;

    /**
     * 使用状态(0-待使用,1-已使用)
     */
    private Integer isUsed;

    /**
     * 优惠券id
     */
    private Long couponId;

    private Long memberId;

    private Integer addNumber;

    private Long id;
}
