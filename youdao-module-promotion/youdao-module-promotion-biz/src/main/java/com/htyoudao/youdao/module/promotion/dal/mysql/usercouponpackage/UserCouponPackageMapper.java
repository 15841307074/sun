package com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户优惠券包关系 Mapper
 *
 * @author 13149747939
 */
@Mapper
public interface UserCouponPackageMapper extends BaseMapperX<UserCouponPackageDO> {

    Boolean batchInsert(@Param("shardingValue") Long shardingValue,@Param("list") List<UserCouponPackageDO> userCouponPackages);
}