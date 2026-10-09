package com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.impl;

import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.ClaimPackageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_PACKAGE_EXPIRED;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_PACKAGE_NOT_ON_SHELF;

/**
 * @author duht
 * 领取优惠券 第三个被调用的 impl
 * 判断优惠券是否下架
 */
@Slf4j
@Service("claimPackageGround")
public class ClaimPackageGroundServiceImpl implements ClaimPackageService {

    // 第四个被调用的 impl 判断优惠券数量
    @Resource
    @Qualifier("claimPackageMemberEmpty")
    private ClaimPackageService claimPackageService;


    /**
     * 领取优惠券 判断优惠券是否下架
     * @param couponPackage 优惠券
     * @param wxMember 用户
     */
    @Override
    public void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember) {
        log.info("进入第三个impl,优惠券是否下架{}", couponPackage.getIsGround());
        Integer isGround = couponPackage.getIsGround();
        if (isGround == 0) {
            throw exception(COUPON_PACKAGE_NOT_ON_SHELF);
        }else {
            claimPackageService.claimCouponPackage(couponPackage, wxMember);
        }
    }
}
