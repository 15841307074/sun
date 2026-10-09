package com.htyoudao.youdao.module.promotion.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dht
 */
public class UserCouponCommodityContext {

    private static final InheritableThreadLocal<Map<Long, Collection<Long>>> USER_CAN_USE_COMMODITY = new InheritableThreadLocal<>();

    /**
     * 初始化或获取当前线程的 "可用商品" Map
     */
    private static Map<Long, Collection<Long>> getOrInitCanUseMap() {
        Map<Long, Collection<Long>> map = USER_CAN_USE_COMMODITY.get();
        if (map == null) {
            map = new HashMap<>(8);
            USER_CAN_USE_COMMODITY.set(map);
        }
        return map;
    }

    /**
     * 获取指定优惠券的 "可用商品" 列表
     * @param couponId 优惠券ID
     * @return 商品ID列表（可能为 null）
     */
    public static Collection<Long> getCanUseCommoditiesByCouponId(Long couponId) {
        Map<Long, Collection<Long>> map = USER_CAN_USE_COMMODITY.get();
        return map != null ? map.get(couponId) : null;
    }

    /**
     * 向 "可用商品" Map 中添加数据
     * @param couponId  优惠券ID
     * @param commodityIds 商品ID列表
     */
    public static void addCanUseCommodities(Long couponId, Collection<Long> commodityIds) {
        Map<Long, Collection<Long>> map = getOrInitCanUseMap();
        map.put(couponId, commodityIds);
    }

    /**
     * 获取当前线程的 "可用商品" Map
     */
    public static Map<Long, Collection<Long>> getCanUseCommodities() {
        return USER_CAN_USE_COMMODITY.get();
    }

    /**
     * 清除当前线程的 ThreadLocal 数据（防止内存泄漏）
     */
    public static void clear() {
        USER_CAN_USE_COMMODITY.remove();
    }
}
