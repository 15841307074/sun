package com.htyoudao.youdao.module.promotion.context;


import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.calculatediscount.CalculateDiscount;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 满减优惠金额计算上下文
 */
@Component
@Configuration
public class DiscountCalculateContext {

    @Resource
    @Qualifier("discountCommon")
    private CalculateDiscount discountCommon;

    @Resource
    @Qualifier("discountNotCommon")
    private  CalculateDiscount discountNotCommon;


    @Resource
    @Qualifier("discountPartCommon")
    private  CalculateDiscount discountPartCommon;


    private Map<Integer, CalculateDiscount> fullMinusMap;

    @PostConstruct
    public void buildFullMinusMap() {
        fullMinusMap = new HashMap<>(8);
        fullMinusMap.put(1, discountCommon);
        fullMinusMap.put(2, discountPartCommon);
        fullMinusMap.put(3, discountNotCommon);
    }



    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        CalculateDiscount strategy = fullMinusMap.get(userCoupon.getIsCommonStore());
        if (strategy == null) {
            return userCoupon;
        }
        return strategy.calculateDiscount(userCoupon, settlementReqVO);
    }
}