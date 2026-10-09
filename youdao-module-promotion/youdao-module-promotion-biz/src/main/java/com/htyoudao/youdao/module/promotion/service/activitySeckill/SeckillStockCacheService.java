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
public class SeckillStockCacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:{%s}:stock:";

    // 生成库存key
    private String buildStockKey(Long storeId, Long activityId, Long productId, Integer sessionId) {
        String formatted = STOCK_KEY_PREFIX.formatted(activityId);
        return formatted + storeId + ":" + productId + ":" + sessionId;
    }

    // 初始化场次库存
    public boolean initSessionStockBatch(List<Long> storeIds, Long activityId, Long productId, Integer sessionId,
        Integer stockQuantity) {
        for (Long storeId : storeIds) {
            initSessionStock(storeId, activityId, productId, sessionId, stockQuantity);
        }
        return true;
    }

    // 初始化场次库存
    public boolean initSessionStock(Long storeId, Long activityId, Long productId, Integer sessionId,
        Integer stockQuantity) {
        String key = buildStockKey(storeId, activityId, productId, sessionId);

        // 计算今天剩余的时间（到今晚23:59:59）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().atTime(23, 59, 59);
        Duration duration = Duration.between(now, midnight);

        // 使用SETNX确保不会覆盖已有库存
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, stockQuantity.toString(), duration);

        return result != null && result;
    }

    // 获取当前库存
    public Integer getCurrentStock(Long storeId, Long activityId, Long productId, Integer sessionId) {
        String key = buildStockKey(storeId, activityId, productId, sessionId);
        String stock = stringRedisTemplate.opsForValue().get(key);
        return stock != null ? Integer.parseInt(stock) : null;
    }


    /**
     * 根据活动ID删除所有关联的库存信息
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
     * @param productId  商品ID
     */
    public void deleteStockCache(Long activityId, Long productId) {
        // 构建匹配模式: seckill:stock:{activityId}:{productId}:*
        String formatted = STOCK_KEY_PREFIX.formatted(activityId);
        String pattern = formatted + "*" + productId + ":*";
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