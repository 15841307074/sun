package com.htyoudao.youdao.module.promotion.service.goodcouponpackage;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackageSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.GoodCouponPackagePageReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage.UserCouponPackageDO;
import jakarta.validation.*;

import java.util.List;

/**
 * 优惠券包关系 Service 接口
 *
 * @author 13149747939
 */
public interface GoodCouponPackageService {


    List<GoodCouponPackageDO> selectListByPackageId(Long id);

    /**
     * 批量插入
     * @param goodCouponPackages goodCouponPackages
     * @return Boolean
     */
    Boolean insertBatch(List<GoodCouponPackageDO> goodCouponPackages);

    List<GoodCouponPackageDO> selectListByCouponId(Long couponId);
}