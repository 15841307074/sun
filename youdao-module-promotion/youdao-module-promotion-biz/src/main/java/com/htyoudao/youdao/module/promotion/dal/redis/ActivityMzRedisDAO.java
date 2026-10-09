package com.htyoudao.youdao.module.promotion.dal.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import jakarta.annotation.Resource;
import java.util.*;

import static com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants.*;

/**
 * 满赠活动 Redis DAO
 * <p>
 * 库存采用 Hash 结构存储：
 * <ul>
 *   <li>共用库存：key = mz_gift:{activityId}，field = giftCommodityId，value = 剩余库存</li>
 *   <li>独立库存：key = mz_gift:{activityId}:store:{storeId}，field = giftCommodityId，value = 剩余库存</li>
 * </ul>
 */
@Repository
@Slf4j
public class ActivityMzRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Lua 脚本：原子性增减赠品库存
     * <p>
     * KEYS[1] = Hash key (mz_gift:{activityId} 或 mz_gift:{activityId}:store:{storeId})
     * ARGV[1] = Hash field (giftCommodityId)
     * ARGV[2] = 变化量（正数=增加/回滚，负数=扣减）
     * ARGV[3] = 可选的原始配置库存，仅扣减且 field 不存在时使用
     * <p>
     * 返回值：
     * -2 = 缓存未初始化（field不存在）
     * -1 = 不限制库存（无需扣减）
     * 扣减量超过库存时扣完剩余库存，不报库存不足。
     * 返回值编码：有限库存返回实际变更量；-2=未初始化；-1=不限库存。
     */
    private static final DefaultRedisScript<Long> CHANGE_INVENTORY_SCRIPT;

    static {
        CHANGE_INVENTORY_SCRIPT = new DefaultRedisScript<>();
        CHANGE_INVENTORY_SCRIPT.setScriptText(
                "local key = KEYS[1]\n" +
                "local field = ARGV[1]\n" +
                "local change = tonumber(ARGV[2])\n" +
                "local raw = redis.call('HGET', key, field)\n" +
                "-- 仅缺失时使用回源的配置库存；并发请求不得覆盖已经扣减的库存\n" +
                "if raw == false then\n" +
                "    if change >= 0 or ARGV[3] == nil then return -2 end\n" +
                "    raw = ARGV[3]\n" +
                "    redis.call('HSET', key, field, raw)\n" +
                "end\n" +
                "local current = tonumber(raw)\n" +
                "-- -1表示不限制库存，直接返回\n" +
                "if current == -1 then\n" +
                "    return -1\n" +
                "end\n" +
                "-- 扣减超过库存时只扣当前剩余量；满赠库存不足不能阻断正品下单\n" +
                "local actual = math.abs(change)\n" +
                "local newVal\n" +
                "if change < 0 then\n" +
                "    actual = math.min(actual, current)\n" +
                "    newVal = current - actual\n" +
                "else\n" +
                "    newVal = current + change\n" +
                "end\n" +
                "redis.call('HSET', key, field, tostring(newVal))\n" +
                "return actual\n"
        );
        CHANGE_INVENTORY_SCRIPT.setResultType(Long.class);
    }

    // ==================== Key 构建 ====================

    /**
     * 构建共用库存 Hash Key
     */
    public String buildSharedKey(Long activityId) {
        return MZ_GIFT_INVENTORY + activityId;
    }

    /**
     * 构建独立库存 Hash Key（按门店）
     */
    public String buildStoreKey(Long activityId, Long storeId) {
        return String.format(MZ_GIFT_STORE_INVENTORY, activityId, storeId);
    }

    // ==================== Hash 读写 ====================

    /**
     * 批量初始化库存到 Hash
     *
     * @param key       Hash key
     * @param inventory giftCommodityId -> 初始库存 映射
     */
    public void initInventory(String key, Map<String, String> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForHash().putAll(key, inventory);
        log.info("满赠库存缓存初始化 key={}, 赠品数={}", key, inventory.size());
    }

    /**
     * 获取 Hash 中某个赠品的剩余库存
     *
     * @return 剩余库存，缓存不存在时返回 null
     */
    public Integer getGiftInventory(String key, Long giftCommodityId) {
        Object val = stringRedisTemplate.opsForHash().get(key, giftCommodityId.toString());
        if (val == null) {
            return null;
        }
        return Integer.parseInt(val.toString());
    }

    /**
     * 使用HMGET批量读取同一个库存Hash中的多个赠品field。
     *
     * <p>这里只接收一个Redis Key，因此兼容Redis Cluster，不会产生跨Slot操作。
     * Redis返回值顺序与field输入顺序一致，本方法保持该顺序转换为Integer列表；
     * Hash或某个field不存在时，对应位置返回null。</p>
     *
     * @param key 共用库存或某门店独立库存的Hash Key
     * @param giftCommodityIds 作为Hash field的赠品连锁商品ID列表
     * @return 与giftCommodityIds等长、顺序一致的库存列表；-1表示不限库存
     */
    public List<Integer> getGiftInventories(String key, List<Long> giftCommodityIds) {
        if (giftCommodityIds == null || giftCommodityIds.isEmpty()) return Collections.emptyList();
        List<Object> fields = giftCommodityIds.stream().map(String::valueOf).map(v -> (Object) v).toList();
        List<Object> values = stringRedisTemplate.opsForHash().multiGet(key, fields);
        if (values == null) return Collections.nCopies(giftCommodityIds.size(), null);
        List<Integer> result = new ArrayList<>(values.size());
        for (Object value : values) result.add(value == null ? null : Integer.valueOf(value.toString()));
        return result;
    }

    /**
     * 获取 Hash 中所有赠品的剩余库存
     *
     * @return giftCommodityId -> 剩余库存 映射
     */
    public Map<Object, Object> getAllGiftInventory(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除整个库存 Hash
     */
    public void deleteInventory(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 批量删除多个库存 Hash key
     */
    public void deleteInventory(Collection<String> keys) {
        if (keys != null && !keys.isEmpty()) {
            // Redis Cluster 不允许一个命令携带不同 slot 的 Key。逐个删除仅作用于满赠缓存，
            // 避免活动清理时因历史 Key 或不同活动 Key 混入而触发 CROSSSLOT。
            keys.forEach(stringRedisTemplate::delete);
        }
    }

    /**
     * 设置单个赠品的库存（覆盖写）
     */
    public void setGiftInventory(String key, Long giftCommodityId, int inventory) {
        stringRedisTemplate.opsForHash().put(key, giftCommodityId.toString(), String.valueOf(inventory));
    }

    /**
     * 删除 Hash 中指定赠品的库存
     */
    public void deleteGiftInventory(String key, Long giftCommodityId) {
        stringRedisTemplate.opsForHash().delete(key, giftCommodityId.toString());
    }

    // ==================== 原子增减库存 ====================

    /**
     * 原子性增减赠品库存（Lua 脚本）
     *
     * @param key              Hash key
     * @param giftCommodityId  赠送商品ID
     * @param change           变化量（负数=扣减，正数=增加/回滚）
     * @return 实际变更数量；-2=缓存未初始化；-1=不限库存
     */
    public Long changeInventory(String key, Long giftCommodityId, int change) {
        return changeInventory(key, giftCommodityId, change, null);
    }

    /** 缓存缺失时初始化并扣减，已有库存保持原值；初始化和扣减在同一 Lua 中执行。 */
    public Long changeInventory(String key, Long giftCommodityId, int change, Integer initialInventory) {
        List<String> keys = Collections.singletonList(key);
        String[] args = initialInventory == null
                ? new String[]{giftCommodityId.toString(), String.valueOf(change)}
                : new String[]{giftCommodityId.toString(), String.valueOf(change), initialInventory.toString()};
        Long result = stringRedisTemplate.execute(CHANGE_INVENTORY_SCRIPT, keys, args);
        log.debug("满赠库存变更 key={}, giftCommodityId={}, change={}, result={}", key, giftCommodityId, change, result);
        // null表示脚本没有得到确定结果，不能按-1“不限库存”处理。
        return result != null ? result : -2L;
    }

    // ==================== 活动元数据缓存 ====================

    /**
     * 缓存活动元数据（giftInventoryType 等）
     */
    public void setActivityMeta(Long activityId, String json) {
        stringRedisTemplate.opsForValue().set(MZ_ACTIVITY + activityId, json);
    }

    /**
     * 获取活动元数据
     */
    public String getActivityMeta(Long activityId) {
        return stringRedisTemplate.opsForValue().get(MZ_ACTIVITY + activityId);
    }

    /**
     * 删除活动元数据
     */
    public void deleteActivityMeta(Long activityId) {
        stringRedisTemplate.delete(MZ_ACTIVITY + activityId);
    }
}
