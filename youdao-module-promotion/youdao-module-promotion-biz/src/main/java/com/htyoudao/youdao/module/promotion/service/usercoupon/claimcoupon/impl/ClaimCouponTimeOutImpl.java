package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

import cn.hutool.core.date.DateUtil;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.ClaimCouponService;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author duht
 * 领取优惠券 第4个被调用的 impl
 * 判断优惠券是否超时
 */
@Slf4j
@Service("claimCouponTimeOut")
public class ClaimCouponTimeOutImpl implements ClaimCouponService {

    /**
     * 第五个被调用的 impl 判断会员是否存在
     */
    @Resource
    @Qualifier("claimCouponMemberEmpty")
    private ClaimCouponService claimCouponService;

    /**
     * 领取优惠券 过期
     * @param goodCoupon 优惠券
     * @param wxMember 用户
     */
    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        // 校验优惠券领取时间
        CouponTimeUtil.validateCouponTime(goodCoupon);
        // 判断优惠券时间
        Date couponEndTime = goodCoupon.getCouponEndTime();
        Integer useType = goodCoupon.getUseType();
        log.info("进入第四个impl，优惠券过期{}{}", couponEndTime, useType);
        if(useType == 0){
            if (DateUtil.compare(new Date(), couponEndTime) > 0) {
                throw exception(ErrorCodeConstants.COUPON_TIME_OUT);
            }else {
                claimCouponService.claimOneCoupon(goodCoupon, wxMember);
            }
        }else {
            claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        }
    }
}