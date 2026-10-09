package com.htyoudao.youdao.module.promotion.service.couponpackage;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@DS(DsNameConstants.MASTER)
public class CouponPackageMasterServiceImpl implements CouponPackageMasterService{

    @Resource
    private UserCouponPackageMapper userCouponPackageMapper;

    @Override
    @DS(DsNameConstants.MASTER)
    public Boolean batchInsert(Long shardingValue,List<UserCouponPackageDO> userCouponPackages) {
        return userCouponPackageMapper.batchInsert(shardingValue,userCouponPackages);
    }
}
