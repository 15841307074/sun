package com.htyoudao.youdao.module.promotion.service.couponstoreclaimnum;

import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstoreclaimnum.CouponStoreClaimNumDO;

import java.util.List;

/**
 * @author dht
 */
public interface CouponStoreClaimNumService {

    /**
     * 根据优惠券id查询门店领取数量
     * @param id id
     * @return List<CouponStoreClaimNumDO>
     */
    List<CouponStoreClaimNumDO> getByCouponId(Long id);
}
