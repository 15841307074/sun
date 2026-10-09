package com.htyoudao.youdao.module.promotion.service.usercoupon;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.COUPON_OVER_LIMIT_0;

/**
 * @author dht
 */
@Service
@DS(DsNameConstants.SHARDING)
public class UserCouponShardServiceImpl implements UserCouponShardService{

    @Resource
    private UserCouponMapper userCouponMapper;

    @Override
    public void userCouponCount(GoodCouponRespVO goodCoupon, Long couponId, WxMemberDTO wxMember, Integer num) {
        if (goodCoupon.getLimitNum() != 100) {
            QueryWrapper<UserCouponDO> qw = new QueryWrapper<>();
            qw.eq("coupon_id", couponId);
            qw.eq("user_id", wxMember.getMemberId());
            long l = userCouponMapper.selectCount(qw);
            if (l >= goodCoupon.getLimitNum() + num) {
                throw exception(COUPON_OVER_LIMIT_0);
            }
        }
    }

    @Override
    public Boolean insertBatch(List<UserCouponDO> userCoupons) {
        return userCouponMapper.insertBatchSomeColumn(userCoupons,500) > 0;
    }
}
