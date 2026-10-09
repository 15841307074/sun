package com.htyoudao.youdao.module.promotion.util.redis;

import cn.hutool.core.lang.mutable.MutableByte;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * spring redis 工具类
 *
 *
 **/
@Getter
@SuppressWarnings(value = {"unchecked", "rawtypes"})
@Component
@Slf4j
public class RedisCache {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end", Long.class);

    @Resource
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key   缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> boolean setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            log.info("redis用户token写入成功, key:{},value:{}", key, value);
            return true;
        } catch (Exception e) {
            log.error("redis用户token写入失败, key:{},value:{}", key, value);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 写入热点缓存，不输出完整 value，避免大对象日志影响接口响应。
     */
    public <T> boolean setCacheObjectQuietly(final String key, final T value, final Integer timeout,
                                             final TimeUnit timeUnit) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            return true;
        } catch (Exception e) {
            log.error("redis cache write failed, key:{}", key, e);
            return false;
        }
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取有效时间
     *
     * @param key Redis键
     * @return 有效时间
     */
    public long getExpire(final String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 读取可回源缓存，旧格式数据反序列化失败时删除缓存并返回空。
     */
    public <T> T getCacheObjectOrDelete(final String key) {
        try {
            return getCacheObject(key);
        } catch (SerializationException e) {
            log.warn("Redis 缓存反序列化失败，删除旧缓存并回源，key={}", key);
            try {
                deleteObject(key);
            } catch (Exception deleteException) {
                log.warn("删除异常 Redis 缓存失败，key={}", key, deleteException);
            }
            return null;
        }
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public boolean deleteObject(final Collection collection) {
        return redisTemplate.delete(collection) > 0;
    }

    /**
     * 缓存List数据
     *
     * @param key      缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key     缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }




    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key   Redis键
     * @param hKey  Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey) {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key   Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 删除Hash中的某条数据
     *
     * @param key  Redis键
     * @param hKey Hash键
     * @return 是否成功
     */
    public boolean deleteCacheMapValue(final String key, final String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

    public boolean lock(String key, String value, long expireTime) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
    }

    public void unlock(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        try {
            redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), value);
        } catch (Exception e) {
            // 锁有过期时间，释放失败不能影响已经完成的业务事务。
            log.warn("Redis 分布式锁释放失败，key={}", key, e);
        }
    }

    public Boolean setIsMember(String key, Long value){
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Boolean getBit(String key,int offset) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, offset));
    }

    public Boolean setBit(String key,int offset) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setBit(key, offset, true));
    }

    public void opsForSet(String s, Set<Long> longs) {
        redisTemplate.opsForSet().add(s, longs);
    }

    public void increment(String key) {
        redisTemplate.opsForValue().increment(key,1);
    }

    public Integer getOpsForValue(String key) {
        return (Integer)redisTemplate.opsForValue().get(key);
    }

    public Long bitCount(String key) {
        Object execute = redisTemplate.execute((RedisCallback<Long>) p -> p.stringCommands().bitCount(key.getBytes()));
        return (Long) execute;
    }

    public HashOperations<String, String, Object> getHash(){
        return redisTemplate.opsForHash();
    }

    public Object getHashValue(String key, String name){
        Object o = redisTemplate.opsForHash().get(key, name);
        return o;
    }
}
