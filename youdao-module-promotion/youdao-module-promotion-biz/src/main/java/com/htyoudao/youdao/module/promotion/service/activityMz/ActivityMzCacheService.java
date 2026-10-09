package com.htyoudao.youdao.module.promotion.service.activityMz;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryQuery;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryResult;
import com.htyoudao.youdao.module.promotion.dal.redis.ActivityMzRedisDAO;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.service.activityMzGift.ActivityMzGiftService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 满赠活动缓存 Service
 * <p>
 * 负责管理满赠活动的 Redis 缓存，包括：
 * <ul>
 *   <li>活动创建/修改/删除时联动初始化/刷新/清理缓存</li>
 *   <li>赠品库存增减（原子操作，Lua 脚本）</li>
 *   <li>库存查询（共用/独立门店）</li>
 * </ul>
 */
@Service
@Slf4j
public class ActivityMzCacheService {

    @Resource
    private ActivityMzRedisDAO activityMzRedisDAO;

    @Resource
    private ActivityMzGiftMapper activityMzGiftMapper;

    @Resource
    private ActivityMzGiftService activityMzGiftService;

    @Resource
    private ActivityStoreService activityStoreService;

    // ==================== 缓存生命周期 ====================

    /**
     * 获取活动元数据（公共方法，供 ServiceImpl 在更新前获取旧元数据）
     *
     * @param activityId 活动ID
     * @return 活动元数据，缓存不存在时返回 null
     */
    public ActivityMzMeta getActivityMetaPublic(Long activityId) {
        return getActivityMeta(activityId);
    }

    /**
     * 活动创建后初始化库存缓存
     *
     * @param activityId      活动ID
     * @param giftInventoryType 库存类型（1共用 2独立）
     * @param userLimitType   用户参与限制类型（0不限制 1每天限制 2活动期间限制）
     * @param userLimitValue  用户参与限制次数
     * @param giftList        赠品列表
     * @param storeIds        门店ID列表（独立库存时使用）
     */
    public void initCacheOnCreate(Long activityId, Integer giftInventoryType,
                                  Integer userLimitType, Integer userLimitValue,
                                  List<ActivityMzGiftDO> giftList, List<Long> storeIds) {
        // 活动元数据不依赖赠品列表，即使赠品为空也要写入，供参与次数等业务读取。
        cacheActivityMeta(activityId, giftInventoryType, userLimitType, userLimitValue);

        if (ObjectUtil.isEmpty(giftList)) return;

        // 构建 giftCommodityId -> 初始库存 映射
        Map<String, String> inventoryMap = buildInventoryMap(giftList);

        if (Integer.valueOf(2).equals(giftInventoryType) && ObjectUtil.isNotEmpty(storeIds)) {
            // 独立库存：每个门店一个 Hash
            for (Long storeId : storeIds) {
                String key = activityMzRedisDAO.buildStoreKey(activityId, storeId);
                activityMzRedisDAO.initInventory(key, inventoryMap);
            }
            log.info("满赠活动[{}]独立库存缓存初始化完成，门店数={}", activityId, storeIds.size());
        } else {
            // 共用库存：一个 Hash
            String key = activityMzRedisDAO.buildSharedKey(activityId);
            activityMzRedisDAO.initInventory(key, inventoryMap);
            log.info("满赠活动[{}]共用库存缓存初始化完成", activityId);
        }
    }

    /**
     * 活动修改后刷新库存缓存
     * <p>
     * 策略：
     * 1. 检测库存模式切换，清理旧模式缓存（Bug 6）
     * 2. 共用库存：重置为初始库存（赠品已删除重建）
     * 3. 独立库存：保留门店保留剩余库存，新增门店初始化初始库存（Bug 2）
     * 4. 清理被移除门店的孤立缓存 key
     *
     * @param activityId           活动ID
     * @param giftInventoryType    新库存类型（1共用 2独立）
     * @param userLimitType        新用户参与限制类型
     * @param userLimitValue       新用户参与限制次数
     * @param giftList             新赠品配置列表
     * @param newStoreIds          变更后的门店ID列表
     * @param oldStoreIds         变更前的旧门店ID列表
     * @param oldGiftInventoryType 变更前的库存类型（用于检测模式切换）
     */
    public void refreshCacheOnUpdate(Long activityId, Integer giftInventoryType,
                                     Integer userLimitType, Integer userLimitValue,
                                     List<ActivityMzGiftDO> giftList, List<Long> newStoreIds,
                                     List<Long> oldStoreIds, Integer oldGiftInventoryType) {
        boolean isIndependent = Integer.valueOf(2).equals(giftInventoryType);
        boolean wasIndependent = Integer.valueOf(2).equals(oldGiftInventoryType);

        // 1. 检测库存模式切换，清理旧模式缓存（Bug 6）
        if (wasIndependent && !isIndependent) {
            // 独立→共用：清理所有旧门店的独立库存 key
            List<String> oldStoreKeys = new ArrayList<>();
            if (ObjectUtil.isNotEmpty(oldStoreIds)) {
                for (Long storeId : oldStoreIds) {
                    oldStoreKeys.add(activityMzRedisDAO.buildStoreKey(activityId, storeId));
                }
            }
            if (!oldStoreKeys.isEmpty()) {
                activityMzRedisDAO.deleteInventory(oldStoreKeys);
                log.info("满赠活动[{}]库存模式切换(独立→共用)，清理{}个旧门店缓存", activityId, oldStoreKeys.size());
            }
        } else if (!wasIndependent && isIndependent) {
            // 共用→独立：清理旧的共用库存 key
            activityMzRedisDAO.deleteInventory(activityMzRedisDAO.buildSharedKey(activityId));
            log.info("满赠活动[{}]库存模式切换(共用→独立)，清理共用缓存", activityId);
        }

        // 2. 初始化新缓存
        if (isIndependent && ObjectUtil.isNotEmpty(newStoreIds)) {
            // 独立库存模式
            Set<Long> oldStoreSet = new HashSet<>(ObjectUtil.isNotEmpty(oldStoreIds) ? oldStoreIds : Collections.emptyList());
            Map<String, String> initialInventoryMap = buildInventoryMap(giftList);

            for (Long storeId : newStoreIds) {
                String key = activityMzRedisDAO.buildStoreKey(activityId, storeId);
                if (wasIndependent && oldStoreSet.contains(storeId)) {
                    // 保留门店：保留剩余库存，仅补充新增赠品的初始库存
                    Map<String, String> mergedMap = mergeInventoryForRetainedStore(key, initialInventoryMap);
                    activityMzRedisDAO.initInventory(key, mergedMap);
                } else {
                    // 新增门店：初始化初始库存
                    activityMzRedisDAO.initInventory(key, initialInventoryMap);
                }
            }
            log.info("满赠活动[{}]独立库存缓存刷新完成，门店数={}", activityId, newStoreIds.size());
        } else if (!isIndependent) {
            // 共用库存模式：赠品已删除重建，重置为初始库存
            if (ObjectUtil.isNotEmpty(giftList)) {
                String key = activityMzRedisDAO.buildSharedKey(activityId);
                activityMzRedisDAO.initInventory(key, buildInventoryMap(giftList));
                log.info("满赠活动[{}]共用库存缓存刷新完成", activityId);
            }
        }

        // 3. 清理被移除门店的孤立缓存 key（旧门店 - 新门店）
        if (ObjectUtil.isNotEmpty(oldStoreIds)) {
            Set<Long> newSet = new HashSet<>(ObjectUtil.isNotEmpty(newStoreIds) ? newStoreIds : Collections.emptyList());
            List<String> orphanedKeys = new ArrayList<>();
            for (Long oldStoreId : oldStoreIds) {
                if (!newSet.contains(oldStoreId)) {
                    orphanedKeys.add(activityMzRedisDAO.buildStoreKey(activityId, oldStoreId));
                }
            }
            if (!orphanedKeys.isEmpty()) {
                activityMzRedisDAO.deleteInventory(orphanedKeys);
                log.info("满赠活动[{}]清理{}个被移除门店的孤立缓存", activityId, orphanedKeys.size());
            }
        }

        // 4. 刷新完整活动元数据
        cacheActivityMeta(activityId, giftInventoryType, userLimitType, userLimitValue);
    }

    /**
     * 写入完整的满赠活动元数据。
     *
     * <p>除活动创建、修改外，也用于旧版本缓存缺少新增字段时的懒加载回填。</p>
     */
    public void cacheActivityMeta(Long activityId, Integer giftInventoryType,
                                  Integer userLimitType, Integer userLimitValue) {
        ActivityMzMeta meta = new ActivityMzMeta();
        meta.setActivityId(activityId);
        meta.setGiftInventoryType(giftInventoryType);
        // 历史数据可能为 null，统一按“不限制”缓存，避免每次参与校验重复回源数据库。
        meta.setUserLimitType(userLimitType == null ? 0 : userLimitType);
        meta.setUserLimitValue(userLimitValue);
        activityMzRedisDAO.setActivityMeta(activityId, JSON.toJSONString(meta));
    }

    /**
     * 活动删除后清理所有缓存
     * <p>
     * 注意：storeIds 必须在调用方删除 activity_store 记录之前捕获并传入，
     * 否则独立库存模式下无法查到门店ID，导致门店库存 key 遗留（Bug 5）。
     *
     * @param activityId 活动ID
     * @param storeIds   活动关联的门店ID列表（由调用方在DB删除前捕获）
     */
    public void clearCacheOnDelete(Long activityId, List<Long> storeIds) {
        clearAllCache(activityId, storeIds);
        log.info("满赠活动[{}]缓存已全部清理", activityId);
    }

    // ==================== 库存增减 ====================

    /**
     * 原子性增减赠品库存
     *
     * @param activityId      活动ID
     * @param storeId         门店ID（独立库存时必传，共用库存时传null）
     * @param giftCommodityId 赠送商品ID
     * @param change          变化量（负数=扣减，正数=增加/回滚）
     * @return 操作结果
     */
    public InventoryChangeResult changeGiftInventory(Long activityId, Long storeId,
                                                     Long giftCommodityId, int change) {
        return changeGiftInventory(activityId, storeId, giftCommodityId, change, getActivityMeta(activityId));
    }

    /** 锁库及本次补偿复用同一份元数据，避免重复读取 Redis 和库存 Key 路由变化。 */
    InventoryChangeResult changeGiftInventory(Long activityId, Long storeId,
                                             Long giftCommodityId, int change, ActivityMzMeta meta) {
        if (meta == null) {
            log.error("满赠活动库存变更失败, activityId={}, giftCommodityId={}, change={}, 原因: 活动缓存不存在", activityId, giftCommodityId, change);
            return new InventoryChangeResult(0, "活动缓存不存在，请先初始化缓存", false);
        }

        String key;
        if (Integer.valueOf(2).equals(meta.getGiftInventoryType())) {
            if (storeId == null) {
                log.error("满赠活动库存变更失败, activityId={}, giftCommodityId={}, change={}, 原因: 独立库存模式必须指定门店ID", activityId, giftCommodityId, change);
                return new InventoryChangeResult(0, "独立库存模式必须指定门店ID", false);
            }
            key = activityMzRedisDAO.buildStoreKey(activityId, storeId);
        } else {
            key = activityMzRedisDAO.buildSharedKey(activityId);
        }

        Long result = activityMzRedisDAO.changeInventory(key, giftCommodityId, change);

        if (result == -2L && change < 0) {
            // 仅首次缺少库存时回源配置。独立库存可复用其他门店的原始配置，
            // 不查询标签/门店关联，也不创建当前门店的 activity_mz_gift 记录。
            LambdaQueryWrapper<ActivityMzGiftDO> query = new LambdaQueryWrapper<ActivityMzGiftDO>()
                    .eq(ActivityMzGiftDO::getActivityId, activityId)
                    .eq(ActivityMzGiftDO::getGiftCommodityId, giftCommodityId);
            if (!Integer.valueOf(2).equals(meta.getGiftInventoryType())) {
                query.isNull(ActivityMzGiftDO::getStoreId);
            }
            ActivityMzGiftDO config = activityMzGiftMapper.selectOne(query
                    .orderByAsc(ActivityMzGiftDO::getId).last("LIMIT 1"));
            if (config != null) {
                int initialInventory = config.getActivityInventory() == null ? -1 : config.getActivityInventory();
                result = activityMzRedisDAO.changeInventory(key, giftCommodityId, change, initialInventory);
            }
        }

        int absChange = Math.abs(change);

        if (result == -2L) {
            log.error("满赠活动库存变更失败, activityId={}, giftCommodityId={}, change={}, 原因: 库存缓存未初始化", activityId, giftCommodityId, change);
            return new InventoryChangeResult(0, "库存缓存未初始化", false);
        } else if (result == -1L) {
            // 不限制库存，变更成功
            return new InventoryChangeResult(absChange, "不限制库存，操作成功", true);
        } else {
            // 有限库存由 Lua 返回实际变更数量（库存不足时自动截断）
            return new InventoryChangeResult(result.intValue(), "操作成功", true);
        }
        
    }

    // ==================== 库存查询 ====================

    /**
     * 查询共用库存模式下某赠品的剩余库存
     */
    public Integer querySharedInventory(Long activityId, Long giftCommodityId) {
        String key = activityMzRedisDAO.buildSharedKey(activityId);
        return activityMzRedisDAO.getGiftInventory(key, giftCommodityId);
    }

    /**
     * 查询独立库存模式下某门店某赠品的剩余库存
     */
    public Integer queryStoreInventory(Long activityId, Long storeId, Long giftCommodityId) {
        String key = activityMzRedisDAO.buildStoreKey(activityId, storeId);
        return activityMzRedisDAO.getGiftInventory(key, giftCommodityId);
    }

    /**
     * 批量查询满赠赠品的实时可用库存，供订单结算阶段判断赠品是否可展示及可展示数量。
     *
     * <p>该方法只读取库存，不锁定、不扣减库存。结算结果仅供展示，订单提交时仍需调用锁库接口，
     * 最终赠送数量以提交时实际锁定数量为准。</p>
     *
     * <p>处理流程：</p>
     * <ol>
     *     <li>按照请求顺序预先创建等长结果列表，保证返回结果与入参位置一一对应；</li>
     *     <li>同一批请求中的活动元数据按activityId复用，避免同一活动重复读取库存模式；</li>
     *     <li>根据活动库存模式选择实际Redis Hash Key：
     *         共用库存使用 {@code mz_gift:{activityId}}，
     *         门店独立库存使用 {@code mz_gift:{activityId}:store:{storeId}}；</li>
     *     <li>按实际Hash Key分组，同一个Hash中的多个赠品字段通过一次HMGET读取；</li>
     *     <li>将HMGET结果按照原始索引回填，避免不同活动、门店或相同赠品商品之间发生错位。</li>
     * </ol>
     *
     * <p>Redis Cluster兼容性：HMGET操作的是一个Hash Key下的多个field，并非对多个Redis Key执行MGET；
     * 每个分组只发送单Key命令，因此不会产生CROSSSLOT。不同Hash Key分别执行并由客户端路由到对应节点。</p>
     *
     * <p>异常/缺失口径：</p>
     * <ul>
     *     <li>入参为null或空集合：返回空列表；</li>
     *     <li>单条请求为空、缺少activityId或giftCommodityId：保留对应结果，inventory为null；</li>
     *     <li>活动元数据不存在：无法确定库存模式，inventory返回null；</li>
     *     <li>门店独立库存未传storeId：inventory返回null；</li>
     *     <li>Redis Hash或field不存在：inventory返回null；</li>
     *     <li>Redis值为-1：表示不限库存；0表示无可用库存；正数表示实时可用数量。</li>
     * </ul>
     *
     * @param queries 批量查询条件；每项包含活动ID、门店ID和赠品连锁商品ID
     * @return 与queries数量及顺序一致的库存结果；无法查询的项库存值为null
     */
    public List<MzGiftInventoryResult> queryGiftInventories(List<MzGiftInventoryQuery> queries) {
        // 空批次不访问Redis，直接返回不可变空列表。
        if (queries == null || queries.isEmpty()) return Collections.emptyList();

        // results按请求长度预分配并逐项占位，后续通过原始索引精确回填库存。
        List<MzGiftInventoryResult> results = new ArrayList<>(queries.size());
        // 同一活动可能配置多个赠品，只读取一次活动元数据并在本次方法调用内复用。
        Map<Long, ActivityMzMeta> metaCache = new HashMap<>();
        // Redis Hash Key -> queries原始索引；一个Key最终只执行一次HMGET。
        Map<String, List<Integer>> indexesByKey = new LinkedHashMap<>();
        for (int i = 0; i < queries.size(); i++) {
            MzGiftInventoryQuery query = queries.get(i);
            // 无论查询条件是否完整都生成对应结果，保证调用方可以按位置或三元组稳定匹配。
            MzGiftInventoryResult result = new MzGiftInventoryResult();
            if (query != null) {
                result.setActivityId(query.getActivityId());
                result.setStoreId(query.getStoreId());
                result.setGiftCommodityId(query.getGiftCommodityId());
            }
            results.add(result);
            // 非法单项不影响批次中的其他查询，该项以inventory=null返回。
            if (query == null || query.getActivityId() == null || query.getGiftCommodityId() == null) continue;

            // 元数据决定库存使用全局共享Key还是门店独立Key；不存在时无法安全猜测库存模式。
            ActivityMzMeta meta = metaCache.computeIfAbsent(query.getActivityId(), this::getActivityMeta);
            if (meta == null) continue;
            String key;
            if (Integer.valueOf(2).equals(meta.getGiftInventoryType())) {
                // 独立库存必须有门店维度；缺少storeId时保留null结果，不回退查询共用库存。
                if (query.getStoreId() == null) continue;
                key = activityMzRedisDAO.buildStoreKey(query.getActivityId(), query.getStoreId());
            } else {
                key = activityMzRedisDAO.buildSharedKey(query.getActivityId());
            }
            indexesByKey.computeIfAbsent(key, ignored -> new ArrayList<>()).add(i);
        }

        // 每个Hash Key独立执行HMGET；不同Key无需位于同一个Redis Cluster slot。
        indexesByKey.forEach((key, indexes) -> {
            // field顺序与indexes顺序一致，Redis HMGET返回值也按field输入顺序排列。
            List<Long> giftIds = indexes.stream().map(i -> queries.get(i).getGiftCommodityId()).toList();
            List<Integer> inventories = activityMzRedisDAO.getGiftInventories(key, giftIds);
            // 按原始请求索引回填，最终results顺序不会受到Hash分组顺序影响。
            for (int i = 0; i < indexes.size(); i++) results.get(indexes.get(i)).setInventory(inventories.get(i));
        });
        return results;
    }

    /**
     * 查询共用库存模式下所有赠品的剩余库存
     *
     * @return giftCommodityId -> 剩余库存
     */
    public Map<String, Integer> queryAllSharedInventory(Long activityId) {
        String key = activityMzRedisDAO.buildSharedKey(activityId);
        return parseInventoryMap(activityMzRedisDAO.getAllGiftInventory(key));
    }

    /**
     * 查询独立库存模式下某门店所有赠品的剩余库存
     *
     * @return giftCommodityId -> 剩余库存
     */
    public Map<String, Integer> queryAllStoreInventory(Long activityId, Long storeId) {
        String key = activityMzRedisDAO.buildStoreKey(activityId, storeId);
        return parseInventoryMap(activityMzRedisDAO.getAllGiftInventory(key));
    }

    /**
     * 查询独立库存模式下所有门店的库存
     *
     * @return storeId -> (giftCommodityId -> 剩余库存)
     */
    public Map<Long, Map<String, Integer>> queryAllStoreInventoryBatch(Long activityId) {
        // 从DB获取门店列表
        List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(activityId);
        if (ObjectUtil.isEmpty(storeIds)) {
            return Collections.emptyMap();
        }

        Map<Long, Map<String, Integer>> result = new HashMap<>();
        for (Long storeId : storeIds) {
            String key = activityMzRedisDAO.buildStoreKey(activityId, storeId);
            result.put(storeId, parseInventoryMap(activityMzRedisDAO.getAllGiftInventory(key)));
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 清理活动相关的所有缓存（共用Hash + 所有门店Hash + 元数据）
     *
     * @param storeIds 活动关联的门店ID列表（由调用方提供，避免从DB查询时数据已删除）
     */
    private void clearAllCache(Long activityId, List<Long> storeIds) {
        List<String> keysToDelete = new ArrayList<>();

        // 共用库存 key
        keysToDelete.add(activityMzRedisDAO.buildSharedKey(activityId));

        // 独立库存 keys（使用调用方传入的门店列表）
        if (ObjectUtil.isNotEmpty(storeIds)) {
            for (Long storeId : storeIds) {
                keysToDelete.add(activityMzRedisDAO.buildStoreKey(activityId, storeId));
            }
        }

        // 元数据 key
        keysToDelete.add(RedisKeyConstants.MZ_ACTIVITY + activityId);

        activityMzRedisDAO.deleteInventory(keysToDelete);
    }

    /**
     * 构建 giftCommodityId -> 初始库存 映射
     */
    private Map<String, String> buildInventoryMap(List<ActivityMzGiftDO> giftList) {
        Map<String, String> map = new LinkedHashMap<>();
        for (ActivityMzGiftDO gift : giftList) {
            // null = 不限制（存-1），0 = 无库存，N = 正常库存
            int inventory = gift.getActivityInventory() != null ? gift.getActivityInventory() : -1;
            map.put(gift.getGiftCommodityId().toString(), String.valueOf(inventory));
        }
        return map;
    }

    /**
     * 为保留门店合并库存：保留已有赠品的剩余库存，新增赠品使用初始库存
     * <p>
     * 场景：修改活动时，独立库存模式下的保留门店不应被重置库存，
     * 但如果新增了赠品，需要为新赠品初始化初始库存。
     *
     * @param storeKey            门店的 Redis Hash key
     * @param initialInventoryMap 新赠品配置的 giftCommodityId -> 初始库存
     * @return 合并后的 giftCommodityId -> 库存值映射
     */
    private Map<String, String> mergeInventoryForRetainedStore(String storeKey, Map<String, String> initialInventoryMap) {
        Map<Object, Object> currentRaw = activityMzRedisDAO.getAllGiftInventory(storeKey);
        Map<String, String> merged = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : initialInventoryMap.entrySet()) {
            String giftCommodityId = entry.getKey();
            String initialValue = entry.getValue();

            Object currentValue = currentRaw.get(giftCommodityId);
            if (currentValue != null) {
                // 已有赠品：保留当前剩余库存
                merged.put(giftCommodityId, currentValue.toString());
            } else {
                // 新增赠品：使用初始库存
                merged.put(giftCommodityId, initialValue);
            }
        }
        return merged;
    }

    /**
     * 解析 Redis Hash 返回的库存映射
     */
    private Map<String, Integer> parseInventoryMap(Map<Object, Object> rawMap) {
        if (rawMap == null || rawMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new HashMap<>();
        rawMap.forEach((k, v) -> {
            try {
                result.put(k.toString(), Integer.parseInt(v.toString()));
            } catch (NumberFormatException e) {
                log.warn("满赠库存缓存值解析失败, giftCommodityId={}, value={}", k, v);
            }
        });
        return result;
    }

    /**
     * 获取活动元数据
     */
    private ActivityMzMeta getActivityMeta(Long activityId) {
        String json = activityMzRedisDAO.getActivityMeta(activityId);
        if (json == null) {
            return null;
        }
        try {
            return JSON.parseObject(json, ActivityMzMeta.class);
        } catch (Exception e) {
            log.warn("满赠活动元数据解析失败, activityId={}", activityId, e);
            return null;
        }
    }

    // ==================== 内部数据结构 ====================

    /**
     * 活动元数据缓存 VO
     */
    @Data
    public static class ActivityMzMeta {
        private Long activityId;
        private Integer giftInventoryType;
        /** 用户参与限制：0不限制，1每人每天限制，2活动期间总次数限制。 */
        private Integer userLimitType;
        /** 用户允许参与的最大次数；null或非正数表示不限制次数。 */
        private Integer userLimitValue;
    }

    /**
     * 库存变更结果
     */
    @Data
    public static class InventoryChangeResult {
        /** 实际变更数量（成功=变更绝对值，失败=0） */
        private int remainingInventory;
        /** 操作消息 */
        private String message;
        /** 操作是否成功 */
        private boolean success;

        public InventoryChangeResult(int remainingInventory, String message, boolean success) {
            this.remainingInventory = remainingInventory;
            this.message = message;
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
