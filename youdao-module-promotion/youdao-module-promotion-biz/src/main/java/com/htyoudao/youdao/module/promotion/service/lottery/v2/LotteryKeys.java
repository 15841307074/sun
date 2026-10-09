package com.htyoudao.youdao.module.promotion.service.lottery.v2;

/** 每个活动统一标识；同一次原子操作的键落在同一 Redis 分片。 */
public final class LotteryKeys {
    private LotteryKeys() {}
    public static String prefix(long businessId, long activityId) {
        return "lottery:v2:{b" + businessId + ":a" + activityId + "}:";
    }
    public static String key(long businessId, long activityId, String type, Object dimension) {
        return prefix(businessId, activityId) + type + ":" + dimension;
    }
    public static String stock(long businessId, long activityId, long epoch, long storeId, String code) {
        return key(businessId, activityId, "stock", epoch + ":" + storeId + ":" + code);
    }
    public static String mutableRegistry(long businessId, long activityId) {
        return key(businessId, activityId, "mutable-keys", 0);
    }
}
