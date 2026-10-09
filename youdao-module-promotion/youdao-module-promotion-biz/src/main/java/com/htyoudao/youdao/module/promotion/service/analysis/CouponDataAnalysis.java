package com.htyoudao.youdao.module.promotion.service.analysis;

import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;

public interface CouponDataAnalysis {


    GoodCouponDO getById(Long id);

    GoodCouponDO getBy(Long id, Long storeId);
}
