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
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_NOT_REGISTER;

/**
 * @author duht
 * 领取优惠券包 第4个被调用的 impl
 * 判断会员是否存在
 */
@Slf4j
@Service("claimPackageMemberEmpty")
public class ClaimPackageMemberEmptyImpl implements ClaimPackageService {

    // 第5个被调用的 impl 判断领取人条件
    @Resource
    @Qualifier("claimPackageRestrict")
    private ClaimPackageService claimPackageService;


    @Override
    public void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember) {
        String memberMobile = wxMember.getMemberMobile();
        log.info("进入第五个impl,会员手机号:{}", memberMobile);
        if (ObjectUtil.isEmpty(memberMobile)) {
            throw exception(COUPON_NOT_REGISTER);
        }else {
            claimPackageService.claimCouponPackage(couponPackage, wxMember);
        }
    }
}
