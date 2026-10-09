package com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppUserCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.CouponCanUseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dht
 * 过期否
 */
@Service("timeCanUse")
@Slf4j
public class TimeCanUseImpl implements CouponCanUseService {

    @Resource
    @Qualifier("couponStoreCanUse")
    private CouponCanUseService couponCanUseService;

    @Override
    public AppUserCouponRespVO isDetermineIfItCanBeUsed(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> result, AppUserCouponRespVO appUserCouponRespVO) {
        log.info("优惠券是否到期{}", settlementReqVO);
        Set<AppCouponCalculateRespVO> nonMatchingList = new HashSet<>();
        Set<AppCouponCalculateRespVO> matchingList = new HashSet<>();
        Date date = new Date();
        for (AppCouponCalculateRespVO appCouponListRespVO : result) {
            Date vaildStartTime = appCouponListRespVO.getVaildStartTime();
            if(date.compareTo(vaildStartTime) < 0){
                appCouponListRespVO.setRemark("优惠券待生效");
                nonMatchingList.add(appCouponListRespVO);
            }else {
                matchingList.add(appCouponListRespVO);
            }
        }
//        appUserCouponRespVO.getCanNotUseCoupons().addAll(nonMatchingList);
//        appUserCouponRespVO.getCanUseCoupons().addAll(matchingList);
//        appUserCouponRespVO.setCanUseCoupons(matchingList);
//        appUserCouponRespVO.setCanNotUseCoupons(nonMatchingList);

        appUserCouponRespVO.setCanUseCoupons(matchingList);
        appUserCouponRespVO.getCanNotUseCoupons().addAll(nonMatchingList);
        if(ObjectUtil.isNotEmpty(couponCanUseService) && CollectionUtils.isNotEmpty(matchingList)){
            return couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO,matchingList,appUserCouponRespVO);
        }else {
            return appUserCouponRespVO;
        }
    }
}
