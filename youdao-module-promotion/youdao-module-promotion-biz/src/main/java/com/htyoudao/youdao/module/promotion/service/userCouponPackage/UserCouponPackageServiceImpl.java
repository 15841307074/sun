package com.htyoudao.youdao.module.promotion.service.userCouponPackage;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercouponpackage.UserCouponPackageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class UserCouponPackageServiceImpl implements UserCouponPackageService {

    @Resource
    private UserCouponPackageMapper userCouponPackageMapper;

    @Override
    public void insert(UserCouponPackageDO userCouponPackage) {
        userCouponPackageMapper.insert(userCouponPackage);
    }
}
