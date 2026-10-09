package com.htyoudao.youdao.module.promotion.dal.mysql.usercouponrecord;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponDataAnalysisBySourceDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord.UserCouponStoreIdDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券使用记录 Mapper
 *
 * @author dht
 */
@Mapper
public interface UserCouponStoreIdMapper extends BaseMapperX<UserCouponStoreIdDO> {
}