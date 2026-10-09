package com.htyoudao.youdao.module.promotion.api.goodcoupon.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class GoodCouponCardVO implements Serializable {


    /**
     * 优惠卷编号
     */
    private String couponCode;

    /**
     * 会员等级
     */
    private Integer memberLevel;

}
