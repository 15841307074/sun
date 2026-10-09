package com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon;



import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户优惠券Mapper接口
 * 
 * @author dht
 * @date 2024-02-15
 */
@Mapper
@DS(DsNameConstants.SHARDING)
public interface UserCouponMapper extends BaseMapperX<UserCouponDO> {

    /**
     * 查询用户优惠券数量
     * @param userId userId
     * @param isUsed isUsed
     * @return Long
     */
    Long getCountNum(@Param("userId") Long userId ,@Param("isUsed") Integer isUsed);

    /**
     * 批量插入
     * @param shardingValue shardingValue
     * @param list list
     * @return Integer
     */
    @DS(DsNameConstants.MASTER)
    Integer writtenInsertion(@Param("shardingValue") Long shardingValue, @Param("list") List<UserCouponDO> list);
}
