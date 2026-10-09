package com.htyoudao.youdao.module.promotion.dal.mysql.couponstore;


import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreAndOrgDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 优惠券门店关系 Mapper
 *
 * @author 13149747939
 */
@Mapper
public interface CouponStoreAndOrgMapper extends BaseMapperX<CouponStoreAndOrgDO> {
    List<CouponStoreAndOrgDO> getStoresByCouponIdAndName(Long couponId, String storeName, Long businessId);
}