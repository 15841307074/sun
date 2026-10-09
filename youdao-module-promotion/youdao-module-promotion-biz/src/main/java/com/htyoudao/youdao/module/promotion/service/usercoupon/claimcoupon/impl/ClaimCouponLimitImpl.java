package com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.service.usercoupon.claimcoupon.ClaimCouponService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author duht
 * 领取优惠券 第7个被调用的 impl
 * 判断领取数量到没到上限
 */
@Slf4j
@Service("claimCouponLimit")
public class ClaimCouponLimitImpl implements ClaimCouponService {


    @Resource
    private UserCouponMapper userCouponMapper;

    /**
     * 领取优惠券 判断领取数量到没到上限
     * @param goodCoupon 优惠券
     * @param wxMember 用户
     */
    @Override
    public void claimOneCoupon(GoodCouponRespVO goodCoupon, WxMemberDTO wxMember) {
        //领取数量限制
        Integer limitNum = goodCoupon.getLimitNum();
        QueryWrapper<UserCouponDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", wxMember.getMemberId());
        queryWrapper.eq("coupon_id", goodCoupon.getId());
        queryWrapper.in("coupon_source", List.of(CouponSourceType.MINI_PROGRAM_LINK.getCode(),
                                                        CouponSourceType.H5_LINK.getCode(),
                                                        CouponSourceType.MINI_PROGRAM_BANNER.getCode(),
                                                        CouponSourceType.MINI_PROGRAM_POPUP.getCode(),
                                                        CouponSourceType.MINI_PROGRAM_SHARE.getCode()
                ));
        Long receivedNum = userCouponMapper.selectCount(queryWrapper);
        Integer dayLimit = goodCoupon.getDayLimit();
        if (dayLimit == 0){
            queryWrapper.eq("DATE_FORMAT(coupon_create_time, '%Y-%m-%d')", LocalDate.now());
            long l = userCouponMapper.selectCount(queryWrapper);
            if (l >= UserCouponConstants.COUPON_LIMIT_100){
                throw exception(ErrorCodeConstants.COUPON_OVER_LIMIT);
            }
        }else {
            queryWrapper.eq("DATE_FORMAT(coupon_create_time, '%Y-%m-%d')", LocalDate.now());
            long l = userCouponMapper.selectCount(queryWrapper);
            if (l >= dayLimit){
                throw exception(ErrorCodeConstants.COUPON_OVER_LIMIT);
            }
        }
        log.info("进入第七个impl，限制数和领取数：{},{}", receivedNum, limitNum);
        if (limitNum != UserCouponConstants.COUPON_LIMIT_100 && receivedNum >= limitNum) {
            throw exception(ErrorCodeConstants.COUPON_OVER_LIMIT);
        }
    }
}