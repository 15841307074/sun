package com.htyoudao.youdao.module.promotion.service.usercoupon;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dht
 * usercoupon的主数据源 业务层
 */
@Service
@DS(DsNameConstants.MASTER)
public class UserCouponMasterServiceImpl implements UserCouponMasterService{

    @Resource
    private UserCouponMapper userCouponMapper;


    @Override
    @DS(DsNameConstants.MASTER)
    public Boolean insertBatch(Long shardingValue,List<UserCouponDO> userCoupons) {
        return userCouponMapper.writtenInsertion(shardingValue, userCoupons) > 0;
    }
}
