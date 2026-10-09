package com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.impl;

import com.htyoudao.youdao.module.promotion.context.FullMinusCalculationContext;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.CouponTypeCalculate;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 策略模式 根据满减计算优惠券
 * @author dht
 */
@Slf4j
@Service("fullMinus")
public class FullMinusImpl implements CouponTypeCalculate {

    @Resource
    private FullMinusCalculationContext fullMinusCalculationContext;

    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("进入满减计算实现类");
        return fullMinusCalculationContext.calculateDiscount(userCoupon, settlementReqVO);
    }
}