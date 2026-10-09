package com.htyoudao.youdao.module.promotion.dal.mysql.couponpackage;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponpackage.CouponPackageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 优惠券包 Mapper
 *
 * @author dht
 */
@Mapper
public interface CouponPackageMapper extends BaseMapperX<CouponPackageDO> {


    /**
     * 优惠券包领取数 同步
     * @param packageId packageId
     * @param storeId 门店id
     * @param claimNum 领取数量
     */
    void upsertPackageStoreClaim(@Param("packageId") long packageId,
                                 @Param("storeId") long storeId,
                                 @Param("claimNum") int claimNum);
}