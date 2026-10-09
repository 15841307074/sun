package com.htyoudao.youdao.module.promotion.service.usercoupon.calculatefullminus;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;

/**
 * 满减优惠券计算接口
 * @author dht
 */
public interface CalculateFullMinus {

    /**
     * 满减优惠券计算
     * @param userCoupon userCoupon
     * @param settlementReqVO settlementReqVO
     * @return AppCouponListRespVO
     */
    AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO);
}