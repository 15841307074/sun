package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

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
 * 领取优惠券 第三个被调用的 impl
 * 判断优惠券是否下架
 */
@Slf4j
@Service("claimCouponGround")
public class ClaimCouponGroundImpl implements ClaimCouponService {

    /**
     * 第四个被调用的 impl 判断优惠券是否过期
     */
    @Resource
    @Qualifier("claimCouponTimeOut")
    private ClaimCouponService claimCouponService;


    /**
     * 领取优惠券 判断优惠券是否下架
     * @param goodCoupon 优惠券
     * @param wxMember 用户
     */
    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        log.info("进入第三个impl,优惠券是否下架{}", goodCoupon.getIsGround());
        Integer isGround = goodCoupon.getIsGround();
        if (isGround == 0) {
            throw exception(ErrorCodeConstants.COUPON_NOT_ON_SHELF);
        }else {
            claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        }
    }
}
