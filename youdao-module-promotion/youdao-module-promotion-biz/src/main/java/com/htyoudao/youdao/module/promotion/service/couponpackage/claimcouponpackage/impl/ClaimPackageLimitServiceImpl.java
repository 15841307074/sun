package com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.service.couponpackage.claimcouponpackage.ClaimPackageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;


@Service("claimPackageLimit")
@Slf4j
@DS(DsNameConstants.SHARDING)
public class ClaimPackageLimitServiceImpl implements ClaimPackageService {

    // 第6个被调用的 impl 判断领取人条件

    @Resource
    private UserCouponPackageMapper userCouponPackageMapper;

    @Resource
    @Qualifier("claimPackageTimeOut")
    private ClaimPackageService claimPackageService;


    @Override
    public void claimCouponPackage(CouponPackageDO couponPackage, WxMemberDTO wxMember) {
        //领取数量限制
        Integer limitNum = couponPackage.getLimitNum();
        QueryWrapper<UserCouponPackageDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", wxMember.getMemberId());
        queryWrapper.eq("package_id", couponPackage.getId());
        queryWrapper.eq("package_source", UserCouponConstants.COUPON_SOURCE_0);
        Long receivedNum = userCouponPackageMapper.selectCount(queryWrapper);
        if(receivedNum >= limitNum){
            throw exception(COUPON_OVER_LIMIT);
        }

        Integer dayLimit = couponPackage.getDayLimit();
        if (dayLimit == 0){
            queryWrapper.eq("DATE_FORMAT(create_time, '%Y-%m-%d')", LocalDate.now());
            long l = userCouponPackageMapper.selectCount(queryWrapper);
            if (l >= UserCouponConstants.COUPON_LIMIT_100){
                throw exception(COUPON_OVER_LIMIT);
            }
        }else {
            queryWrapper.eq("DATE_FORMAT(create_time, '%Y-%m-%d')", LocalDate.now());
            long l = userCouponPackageMapper.selectCount(queryWrapper);
            if (l >= dayLimit){
                throw exception(COUPON_OVER_LIMIT);
            }
        }
        log.info("进入第七个impl，限制数和领取数：{},{}", receivedNum, limitNum);
        if (limitNum != UserCouponConstants.COUPON_LIMIT_100 && receivedNum >= limitNum) {
            throw exception(COUPON_OVER_LIMIT);
        }

        claimPackageService.claimCouponPackage(couponPackage, wxMember);
    }
}
