package com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppUserCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.CouponCanUseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author dht
 * 堂食打包外卖
 */
@Service("habitCanUse")
@Slf4j
public class HabitCanUseImpl implements CouponCanUseService {

    @Resource
    @Qualifier("timeCanUse")
    private CouponCanUseService couponCanUseService;


    @Override
    public AppUserCouponRespVO isDetermineIfItCanBeUsed(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> result, AppUserCouponRespVO appUserCouponRespVO) {
        log.info("优惠券是否堂食打包外卖{}", settlementReqVO);

        Integer habit = settlementReqVO.getHabit();
        List<Integer> habits = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(habit)) {
            if (ObjectUtil.equals(habit, UserCouponConstants.HABIT_2)) {
                habits = List.of(UserCouponConstants.HABIT_0, UserCouponConstants.HABIT_2);
                for (AppCouponCalculateRespVO appCouponListRespVO : result) {
                    Integer habit1 = appCouponListRespVO.getHabit();
                    if(habits.contains(habit1)){
                        appUserCouponRespVO.getCanUseCoupons().add(appCouponListRespVO);
                    }else {
                        appCouponListRespVO.setRemark("外卖不可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }
                // 0堂食 1打包 2外卖
            } else {
                habits = List.of(UserCouponConstants.HABIT_0, UserCouponConstants.HABIT_1);
                for (AppCouponCalculateRespVO appCouponListRespVO : result) {
                    Integer habit1 = appCouponListRespVO.getHabit();
                    if(habits.contains(habit1)){
                        appUserCouponRespVO.getCanUseCoupons().add(appCouponListRespVO);
                    }else {
                        appCouponListRespVO.setRemark("仅外卖可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }
            }
        }

        if(ObjectUtil.isNotEmpty(couponCanUseService) && CollectionUtils.isNotEmpty(appUserCouponRespVO.getCanUseCoupons())){
            return couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO,appUserCouponRespVO.getCanUseCoupons(),appUserCouponRespVO);
        }else {
            return appUserCouponRespVO;
        }
    }
}
