package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.ClaimCouponService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author duht
 * 责任链模式 优惠券领取 第一个被调用的impl
 * 下一个impl claimCouponRest
 * 领取优惠券 判断优惠券是否存在
 */
@Slf4j
@Service("claimCouponEmpty")
public class ClaimCouponEmptyImpl implements ClaimCouponService {


    /**
     * 下一个impl 判断优惠券是否还有剩余
     */
    @Resource
    @Qualifier("claimCouponRest")
    private ClaimCouponService claimCouponService;

    /**
     * 领取优惠券 判断优惠券是否存在
     * @param goodCoupon 优惠券
     * @param wxMember 用户
     */
    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        log.info("进入第一个impl 优惠券领取 判断优惠券是否存在");
        if (ObjectUtil.isEmpty(goodCoupon)) {
            throw exception(ErrorCodeConstants.COUPON_EXPIRED);
        }else {
            claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        }
    }
}
