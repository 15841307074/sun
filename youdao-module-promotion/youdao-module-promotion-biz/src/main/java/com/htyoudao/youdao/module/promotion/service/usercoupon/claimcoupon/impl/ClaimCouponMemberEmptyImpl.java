package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

import cn.hutool.core.util.ObjectUtil;
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
 * 领取优惠券 第5个被调用的 impl
 * 判断会员是否存在
 */
@Slf4j
@Service("claimCouponMemberEmpty")
public class ClaimCouponMemberEmptyImpl implements ClaimCouponService {


    /**
     * 第6个被调用的 impl 判断会员
     */
    @Resource
    @Qualifier("claimCouponRestrict")
    private ClaimCouponService claimCouponService;

    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        if(ObjectUtil.isEmpty(wxMember)){
            throw exception(ErrorCodeConstants.COUPON_NOT_REGISTER);
        }
        String memberMobile = wxMember.getMemberMobile();
        Integer userIdentity = wxMember.getUserIdentity();
        log.info("进入第五个impl,会员手机号:{}", memberMobile);
        if (ObjectUtil.isEmpty(userIdentity) || userIdentity == 0) {
            throw exception(ErrorCodeConstants.COUPON_NOT_REGISTER);
        }else {
            claimCouponService.claimOneCoupon(goodCoupon, wxMember);
        }
    }
}