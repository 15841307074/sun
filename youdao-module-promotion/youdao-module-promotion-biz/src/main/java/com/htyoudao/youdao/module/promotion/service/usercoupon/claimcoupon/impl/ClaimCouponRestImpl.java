package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.ClaimCouponService;
import com.htyoudao.youdao.module.promotion.util.CouponCountUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author duht
 * 优惠券领取 第二个被调用的impl
 * 判断优惠券是否还有剩余
 */
@Slf4j
@Service("claimCouponRest")
public class ClaimCouponRestImpl implements ClaimCouponService {

    /**
     * 优惠券领取下一个impl 判断是否下架
     */
    @Resource
    @Qualifier("claimCouponGround")
    private ClaimCouponService claimCouponService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 优惠券领取
     * @param goodCoupon 优惠券
     * @param wxMember 用户
     */
    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        log.info("进入第二个impl 优惠券剩余数量：{}", goodCoupon.getCouponNum());
        // 判断优惠券数量
        Integer couponNum = goodCoupon.getCouponNum();
        Integer storeLimitNum = goodCoupon.getStoreLimitNum();
        if (couponNum < 1) {
            throw exception(ErrorCodeConstants.COUPON_OVER);
        }else if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
            // 判断店铺优惠券包数量
            CouponCountUtil.getRemainingQuantity(redisTemplate,goodCoupon);
            claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        }else {
            claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        }
    }
}