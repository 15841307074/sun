package com.htyoudao.youdao.module.order.core.calc.context;

import com.htyoudao.youdao.module.order.dal.dataobject.collection.NotNullHashSet;
import lombok.Data;

/**
 * <p>
 * ID集合存储
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
public class IdCollections {

    /**
     * skuId
     */
    public NotNullHashSet<Long> storeSkuIds = new NotNullHashSet<>();

    /**
     * 连锁库商品ID
     */
    public NotNullHashSet<Long> commodityIds = new NotNullHashSet<>();

    /**
     * spuId
     */
    public NotNullHashSet<Long> afterIds = new NotNullHashSet<>();

    /**
     * 套餐子项Id
     */
    public NotNullHashSet<Long> storeSingleIds = new NotNullHashSet<>();

    /**
     * 用户优惠券Id
     */
    @Deprecated
    public Long userCouponId;

    /**
     * 门店Id
     */
    public Long storeId;

    /**
     * 秒杀场次ID
     */
    public Integer sessionId;

    /**
     * 秒杀活动ID
     */
    public Long seckillActivityId;
}
