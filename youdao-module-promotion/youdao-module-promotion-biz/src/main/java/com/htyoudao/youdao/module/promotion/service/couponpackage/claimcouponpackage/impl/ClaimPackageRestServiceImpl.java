package com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.impl;


import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.ClaimPackageService;
import com.htyoudao.youdao.module.promotion.util.CouponPackageCountUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_PACKAGE_OVER;

/**
 * @author duht
 * 优惠券领取 第二个被调用的impl
 * 判断优惠券是否还有剩余
 */
@Slf4j
@Service("claimPackageRest")
public class ClaimPackageRestServiceImpl implements ClaimPackageService {

    //下一个impl 判断优惠券是否还有剩余
    @Resource
    @Qualifier("claimPackageGround")
    private ClaimPackageService claimPackageService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember) {
        log.info("进入第二个impl 优惠券剩余数量：{}", couponPackage.getPackageNum());
        // 判断优惠券数量
        Integer couponNum = couponPackage.getPackageNum();
        Integer storeLimitNum = couponPackage.getStoreLimitNum();
        if (couponNum < 1) {
            throw exception(COUPON_PACKAGE_OVER);
        }else if(ObjectUtil.isNotEmpty(storeLimitNum) && storeLimitNum > 0){
            // 判断店铺优惠券包数量
            CouponPackageCountUtil.getRemainingQuantity(redisTemplate,couponPackage);
            claimPackageService.claimCouponPackage(couponPackage, wxMember);
        }else {
            claimPackageService.claimCouponPackage(couponPackage, wxMember);
        }
    }
}
