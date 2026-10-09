package com.htyoudao.youdao.module.promotion.context;

import java.util.List;
import java.util.Map;

/**
 * 优惠券可用商品山下文
 * @author dht
 */
public class CommodityIdsContext {

    private static final InheritableThreadLocal<Map<Long,List<Long>>> COMMODITY_ID_HOLDER1 = new InheritableThreadLocal<>();

    private static final InheritableThreadLocal<Map<Long,List<Long>>> COMMODITY_ID_HOLDER2 = new InheritableThreadLocal<>();

    private static final InheritableThreadLocal<Map<Long,List<Long>>> COMMODITY_ID_HOLDER3 = new InheritableThreadLocal<>();

    public static void setCommodityId1(Map<Long,List<Long>> ids) {
        COMMODITY_ID_HOLDER1.set(ids);
    }

    public static Map<Long,List<Long>> getCommodityId1() {
        return COMMODITY_ID_HOLDER1.get();
    }

    public static void clearCommodityId1() {
        COMMODITY_ID_HOLDER1.remove();
    }

    public static void setCommodityId2(Map<Long,List<Long>> ids) {
        COMMODITY_ID_HOLDER2.set(ids);
    }

    public static Map<Long,List<Long>> getCommodityId2() {
        return COMMODITY_ID_HOLDER2.get();
    }

    public static void clearCommodityId2() {
        COMMODITY_ID_HOLDER2.remove();
    }

    public static void setCommodityId3(Map<Long,List<Long>> ids) {
        COMMODITY_ID_HOLDER3.set(ids);
    }

    public static Map<Long,List<Long>> getCommodityId3() {
        return COMMODITY_ID_HOLDER3.get();
    }

    public static void clearCommodityId3() {
        COMMODITY_ID_HOLDER3.remove();
    }
}