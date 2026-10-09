package com.htyoudao.youdao.module.order.controller.admin.order.vo.coupon;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 优惠券对象
 *
 * @author zhangjihe
 */
@Data
public class GoodCouponVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5603357144630474525L;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券编码
     */
    private String couponCode;

    /**
     * 优惠券类型(0-满减券,1-直减券,2-折扣券)
     */
    private Integer couponType;
}
