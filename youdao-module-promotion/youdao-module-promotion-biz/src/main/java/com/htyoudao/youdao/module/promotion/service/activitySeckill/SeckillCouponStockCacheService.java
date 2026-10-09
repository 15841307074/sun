package com.htyoudao.youdao.module.promotion.service.activitySeckill;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillCouponStockCacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:{%s}:stock:coupon:";

    // 生成库存key
    public String buildStockKey(Long storeId, Long activityId, Long couponId, Integer sessionId) {
        String formatted = STOCK_KEY_PREFIX.formatted(activityId);
        return formatted + couponId + ":" + sessionId;
    }

    // 初始化场次库存
    public boolean initSessionStockBatch(List<Long> storeIds, Long activityId, Long couponId, Integer sessionId,
        Integer stockQuantity) {

        String key = buildStockKey(null, activityId, couponId, sessionId);

        // 计算今天剩余的时间（到今晚23:59:59）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().atTime(23, 59, 59);
        Duration duration = Duration.between(now, midnight);

        // 使用SETNX确保不会覆盖已有库存
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, stockQuantity.toString(), duration);

        return result != null && result;
    }


    // 获取当前库存
    public Integer getCurrentStock(Long storeId, Long activityId, Long couponId, Integer sessionId) {
        String key = buildStockKey(storeId, activityId, couponId, sessionId);
        String stock = stringRedisTemplate.opsForValue().get(key);
        return stock != null ? Integer.parseInt(stock) : null;
    }


    /**
     * 用旧的那个也能删掉
     *
     * @param activityId 活动ID
     */
    public void deleteStockByActivityId(Long activityId) {
        // 1. 构建匹配模式：seckill:stock:{activityId}:*
        deleteKeysByPattern(STOCK_KEY_PREFIX.formatted(activityId) + "*");
    }


    /**
     * 删除指定活动ID和商品ID集合的所有门店/场次缓存
     *
     * @param activityId 活动ID
     * @param couponId  优惠券ID
     */
    public void deleteStockCache(Long activityId, Long couponId) {
        // 构建匹配模式: seckill:stock:{activityId}:{couponId}:*
        String formatted = STOCK_KEY_PREFIX.formatted(activityId);
        String pattern = formatted + "*" + couponId + ":*";
        deleteKeysByPattern(pattern);
    }

    private void deleteKeysByPattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();

        try (var cursor = Objects.requireNonNull(stringRedisTemplate.getConnectionFactory()).getConnection()
            .scan(options)) {

            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                // 逐个删除，避免跨槽位问题
                stringRedisTemplate.unlink(key);
            }
        }
    }
}