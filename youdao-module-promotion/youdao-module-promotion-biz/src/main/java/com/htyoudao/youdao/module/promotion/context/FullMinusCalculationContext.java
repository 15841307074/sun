package com.htyoudao.youdao.module.promotion.context;


import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.calculatefullminus.CalculateFullMinus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 满减优惠金额计算上下文
 * @author dht
 */
@Component
@Configuration
public class FullMinusCalculationContext {

    @Resource
    @Qualifier("fullMinusCommon")
    private CalculateFullMinus fullMinusCommon;

    @Resource
    @Qualifier("fullMinusNotCommon")
    private  CalculateFullMinus fullMinusNotCommon;


    @Resource
    @Qualifier("fullMinusPartCommon")
    private  CalculateFullMinus fullMinusPartCommon;


    private Map<Integer, CalculateFullMinus> fullMinusMap;


    @PostConstruct
    public void buildFullMinusMap() {
        fullMinusMap = new HashMap<>(8);
        // 通过Spring的自动装配机制获取具体策略类实例并放入Map中
        fullMinusMap.put(1, fullMinusCommon);
        fullMinusMap.put(2, fullMinusPartCommon);
        fullMinusMap.put(3, fullMinusNotCommon);
    }



    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        CalculateFullMinus strategy = fullMinusMap.get(userCoupon.getIsCommonStore());
        if (strategy == null) {
            return userCoupon;
        }
        return strategy.calculateDiscount(userCoupon, settlementReqVO);
    }

}
