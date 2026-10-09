package com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.ClaimPackageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_EXPIRED;

/**
 * @author duht
 * 责任链模式 优惠券领取 第一个被调用的impl
 * 下一个impl claimCouponRest
 * 领取优惠券 判断优惠券是否存在
 */
@Slf4j
@Service("claimPackageEmpty")
public class ClaimPackageEmptyServiceImpl implements ClaimPackageService {

    //下一个impl 判断优惠券是否还有剩余
    @Resource
    @Qualifier("claimPackageRest")
    private ClaimPackageService claimPackageService;


    @Override
    public void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember) {
        log.info("进入第一个impl 优惠券领取 判断优惠券是否存在");
        if (ObjectUtil.isEmpty(couponPackage)) {
            throw exception(COUPON_EXPIRED);
        }else {
            claimPackageService.claimCouponPackage(couponPackage, wxMember);
        }
    }
}
