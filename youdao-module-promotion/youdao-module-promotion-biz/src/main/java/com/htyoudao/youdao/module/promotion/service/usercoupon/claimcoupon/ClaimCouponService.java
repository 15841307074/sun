package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon;


import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;

/**
 * 优惠券领取的一堆判断 责任链
 * 第一个impl claimCouponEmpty
 * @author dht
 */
public interface ClaimCouponService {

    /**
     * 优惠券领取 用户条件判断
     * @param goodCoupon goodCoupon
     * @param wxMember wxMember
     */
    void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember);
}