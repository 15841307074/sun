package com.htyoudao.youdao.module.promotion.service.usercoupon;

import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;

import java.util.List;

/**
 * @author dht
 * usercoupon的主数据源 业务层
 */
public interface UserCouponMasterService {

    /**
     * 批量新增 user_coupon 手写的
     * @param userCoupons userCoupons
     * @param shardingValue shardingValue
     * @return Boolean
     */
    Boolean insertBatch(Long shardingValue,List<UserCouponDO> userCoupons);
}
