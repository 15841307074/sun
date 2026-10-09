package com.htyoudao.youdao.module.promotion.service.usercoupon;

import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;

import java.util.List;

/**
 * @author dht
 */
public interface UserCouponShardService {

    /**
     * 查询用户优惠券的数量
     * @param goodCoupon goodCoupon
     * @param couponId couponId
     * @param wxMember wxMember
     * @param num num
     */
    void userCouponCount(GoodCouponRespVO goodCoupon, Long couponId, WxMemberDTO wxMember, Integer num);

    /**
     * 批量新增 user_coupon
     * @param userCoupons userCoupons
     * @return Boolean
     */
    Boolean insertBatch(List<UserCouponDO> userCoupons);
}
