package com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;

/**
 * 优惠券类型计算
 * @author dht
 */
public interface CouponTypeCalculate {

    /**
     * 计算优惠券优惠金额
     * @param userCoupon userCoupon
     * @param settlementReqVO settlementReqVO
     * @return AppCouponListRespVO
     */
    AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO);
}