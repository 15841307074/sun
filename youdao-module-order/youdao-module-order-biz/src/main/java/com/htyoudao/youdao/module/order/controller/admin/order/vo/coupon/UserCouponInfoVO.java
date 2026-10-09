package com.htyoudao.youdao.module.order.controller.admin.order.vo.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserCouponInfoVO {
    private Long couponId;

    private Long id;

    /**
     * user_id
     */
    private Long userId;

    /**
     * 是否使用(0-待使用,1-已使用,2-不满足使用条件)
     */
    private Integer isUsed;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券类型(0-满减券,1-直减券,2-折扣券)
     */
    private Integer couponType;

    private String couponTypeName;

    /**
     * 优惠券有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date couponStartTime;

    /**
     * 优惠券有效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date couponEndTime;

    /**
     * 满减金额
     */
    private BigDecimal fullReduction;

    /**
     * 减少金额
     */
    private BigDecimal reduceAmount;

    /**
     * 结算金额
     */
    private BigDecimal settlementAmount;

    /**
     * 折扣
     */
    private String discount;

    /**
     * 商品id
     */
    private String singleIds;

    /**
     * 门店id
     */
    private String storeId;

    private Integer couponStatus;

    /**
     * 优惠券编码
     */
    private String couponCode;

    /**
     * 领取方式
     */
    private Long distributionMethod;

    /*
    优惠卷说明
     */
    private String couponExplain;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date useTime;
}
