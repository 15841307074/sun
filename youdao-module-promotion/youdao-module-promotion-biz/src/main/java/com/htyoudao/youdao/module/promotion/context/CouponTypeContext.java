package com.htyoudao.youdao.module.promotion.context;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.CouponTypeCalculate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 优惠券分类上下文
 * @author dht
 */
@Slf4j
@Component
public class CouponTypeContext {

    @Resource
    @Qualifier("discount")
    private CouponTypeCalculate discount;

    @Resource
    @Qualifier("fullMinus")
    private CouponTypeCalculate fullMinus;

    @Resource
    @Qualifier("exchange")
    private CouponTypeCalculate exchange;

    private Map<Integer, CouponTypeCalculate> couponTypeMap;

    @PostConstruct
    public void initCouponTypeMap() {
        couponTypeMap = new HashMap<>(8);
        couponTypeMap.put(0, fullMinus);
        couponTypeMap.put(1, discount);
        couponTypeMap.put(2, exchange);
    }


    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("优惠券分类上下文");
        Integer couponType = userCoupon.getCouponType();
        CouponTypeCalculate strategy = couponTypeMap.get(couponType);
        if (strategy == null) {
            return userCoupon;
        }
        return strategy.calculateDiscount(userCoupon, settlementReqVO);
    }
}