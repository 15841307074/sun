package com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppUserCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;

import java.util.List;
import java.util.Set;

/**
 * 优惠券能否领取
 * @author dht
 */
public interface CouponCanUseService {

    /**
     * 判断优惠券能否使用
     * @param settlementReqVO settlementReqVO
     * @param result result
     * @param appUserCouponRespVO appUserCouponRespVO
     * @return UserCouponDO
     */
    AppUserCouponRespVO isDetermineIfItCanBeUsed(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> result, AppUserCouponRespVO appUserCouponRespVO);
}
