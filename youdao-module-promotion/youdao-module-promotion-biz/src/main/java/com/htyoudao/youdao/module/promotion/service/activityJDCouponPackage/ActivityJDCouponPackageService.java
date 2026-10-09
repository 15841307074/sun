package com.htyoudao.youdao.module.promotion.service.activityJDCouponPackage;

import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponPackageDO;

import java.util.List;

public interface ActivityJDCouponPackageService {
    void createBatch(List<ActivityJDCouponPackageDO> activityJDCommodityDOS);

    void updateBatch(List<ActivityJDCouponPackageDO> activityJDCommodityDOS);

    List<ActivityJDCouponPackageDO> selectByActivityId(Long activityId);

    void deleteByActivityId(Long id);

    List<ActivityJDCouponPackageDO> selectActivityByCouponId(Long id);
}
