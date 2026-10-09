package com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.impl;


import com.htyoudao.youdao.module.promotion.context.DiscountCalculateContext;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.CouponTypeCalculate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 根据优惠券折扣类型计算优惠金额
 * @author dht
 */
@Slf4j
@Service("discount")
public class DiscountImpl implements CouponTypeCalculate {

    @Resource
    private DiscountCalculateContext discountCalculateContext;

    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("根据优惠券折扣类型计算优惠金额 discount");
        return discountCalculateContext.calculateDiscount(userCoupon, settlementReqVO);
    }
}
