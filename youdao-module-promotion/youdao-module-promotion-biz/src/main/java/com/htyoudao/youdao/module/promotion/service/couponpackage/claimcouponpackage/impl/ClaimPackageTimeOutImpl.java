package com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.impl;

import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.ClaimPackageService;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author duht
 * 优惠券领取 最后被调用的impl
 * 判断优惠券包的领取时间
 */
@Slf4j
@Service("claimPackageTimeOut")
public class ClaimPackageTimeOutImpl implements ClaimPackageService {
    @Override
    public void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember) {
        // 校验优惠券包领取时间
        CouponTimeUtil.validateCouponTime(couponPackage);
    }
}
