package com.htyoudao.youdao.module.system.service.store;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** 门店标签以系统库为准；空标签明确缓存，Redis 异常时不放行。 */
@Service
@DS("master")
public class StoreActivityTagCache {
    @Resource
    private SystemStoreTagMapper tags;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private RedissonClient redisson;

    /** 同时从数据库回填缓存的请求上限。 */
    private static final int MAX_CONCURRENT_REFILLS = 4;
    /** 门店标签缓存保留时间（秒）。 */
    private static final int CACHE_TTL_SECONDS = 1800;
    /** 每个门店缓存刷新最多等待锁的秒数。 */
    private static final int REFILL_LOCK_WAIT_SECONDS = 2;

    private final AtomicInteger refills = new AtomicInteger();

    /** 按项目和门店隔离的标签缓存键。 */
    public static String key(long businessId, long storeId) {
        return String.format("activity_tag%d:%d", businessId, storeId);
    }

    private static String stateKey(String key) {
        return key + ":ready";
    }

    /** 缓存不存在或未就绪时，受控地从门店标签表回填。 */
    public void ensure(long businessId, long storeId) {
        if ("READY".equals(redis.opsForValue().get(stateKey(key(businessId, storeId))))) {
            return;
        }
        load(businessId, storeId, false);
    }

    /** 门店标签提交后强制刷新；供活动同步读取最新标签。 */
    public void refresh(long businessId, long storeId) {
        load(businessId, storeId, true);
    }

    /** 标记缓存未就绪，后续匹配请求需等待回填。 */
    public void invalidate(long businessId, long storeId) {
        String key = key(businessId, storeId);
        RLock lock = redisson.getLock(key + ":refill");
        lockOrThrow(lock);
        try {
            markPending(key);
        } finally {
            lock.unlock();
        }
    }

    private void markPending(String key) {
        redis.opsForValue().set(stateKey(key), "PENDING", Duration.ofSeconds(CACHE_TTL_SECONDS));
        redis.delete(key);
    }

    /** 同一门店的失效与回填串行执行，旧快照不会覆盖新标签。 */
    private void load(long businessId, long storeId, boolean force) {
        String key = key(businessId, storeId);
        if (refills.incrementAndGet() > MAX_CONCURRENT_REFILLS) {
            refills.decrementAndGet();
            throw new IllegalStateException("门店标签缓存恢复中，请重试");
        }
        RLock lock = redisson.getLock(key + ":refill");
        boolean locked = false;
        try {
            lockOrThrow(lock);
            locked = true;
            if (!force && "READY".equals(redis.opsForValue().get(stateKey(key)))) {
                return;
            }
            markPending(key);
            List<String> ids = tags.selectList(new LambdaQueryWrapper<SystemStoreTagDO>()
                            .eq(SystemStoreTagDO::getBusinessId, businessId)
                            .eq(SystemStoreTagDO::getStoreId, storeId))
                    .stream().map(SystemStoreTagDO::getTagId).filter(id -> id != null && id > 0)
                    .distinct().map(String::valueOf).toList();
            if (!ids.isEmpty()) {
                redis.opsForSet().add(key, ids.toArray(String[]::new));
                redis.expire(key, Duration.ofSeconds(CACHE_TTL_SECONDS));
            }
            redis.opsForValue().set(stateKey(key), "READY", Duration.ofSeconds(CACHE_TTL_SECONDS - 10));
        } finally {
            refills.decrementAndGet();
            if (locked) {
                lock.unlock();
            }
        }
    }

    private static void lockOrThrow(RLock lock) {
        try {
            if (!lock.tryLock(REFILL_LOCK_WAIT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("门店标签缓存恢复中，请重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("门店标签缓存恢复中断", e);
        }
    }
}
