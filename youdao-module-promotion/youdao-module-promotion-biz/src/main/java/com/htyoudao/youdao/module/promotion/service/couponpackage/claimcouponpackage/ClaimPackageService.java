package com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage;

import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;

/**
 * @author dht
 */
public interface ClaimPackageService {

    /**
     * 领取优惠券包 条件判断
     * @param couponPackage couponPackage
     * @param wxMember wxMember
     */
    void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember);
}
