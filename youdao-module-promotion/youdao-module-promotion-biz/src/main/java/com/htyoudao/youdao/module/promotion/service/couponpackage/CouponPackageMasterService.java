package com.htyoudao.youdao.module.promotion.service.couponpackage;

import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;

import java.util.List;

/**
 * @author dht
 */
public interface CouponPackageMasterService {

    Boolean batchInsert(Long shardingValue,List<UserCouponPackageDO> userCouponPackages);
}
