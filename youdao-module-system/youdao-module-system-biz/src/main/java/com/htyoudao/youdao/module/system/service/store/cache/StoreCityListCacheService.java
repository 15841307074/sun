package com.htyoudao.youdao.module.system.service.store.cache;

import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreExpensesDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.wxstore.StoreWecomConfigDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreExpensesMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.wxstore.StoreWecomConfigMapper;
import com.htyoudao.youdao.module.system.enums.StoreStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.event.EventListener;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.util.DigestUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 城市门店列表缓存服务。
 *
 * <p>负责缓存预热、按城市回源、单门店刷新和门店背景缓存读取。缓存值统一使用 JSON 字符串，
 * 避免使用 Object 序列化造成跨版本反序列化问题。</p>
 */
@Slf4j
@Service
@DataPermission(enable = false)
public class StoreCityListCacheService {

    private static final long BUSINESS_ID = 10L;
    private static final int BATCH_SIZE = 500;
    private static final int MAX_READ_RETRY = 3;
    private static final int CITY_CACHE_WAIT_RETRY = 40;
    private static final int STORE_CACHE_WAIT_RETRY = 10;
    private static final int REVISION_LOCK_RETRY = 3;
    private static final String ACTIVE_KEY = "store:list:v1:active";
    private static final String REVISION_KEY = "store:list:v1:revision";
    private static final String PENDING_KEY = "store:list:v1:refresh:pending";
    private static final String RETRY_KEY = "store:list:v1:refresh:retry";
    private static final String PREHEAT_LOCK_KEY = "store:list:v1:lock:preheat";
    private static final String REVISION_LOCK_KEY = "store:list:v1:lock:revision";
    private static final String REFRESH_LOCK_KEY = "store:list:v1:lock:refresh";
    private static final String BACKGROUND_DATA_KEY = "{store:background}:data";
    private static final String BACKGROUND_DEFAULT_KEY = "{store:background}:default";
    private static final String BACKGROUND_PENDING_KEY = "{store:background}:rebuild:pending";
    private static final String BACKGROUND_REVISION_KEY = "{store:background}:revision";
    private static final Duration GENERATION_TTL = Duration.ofDays(2);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    private SystemStoreExpensesMapper systemStoreExpensesMapper;
    @Resource
    private StoreWecomConfigMapper storeWecomConfigMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApplicationContext applicationContext;
    @Resource(name = "storeCityListCacheExecutor")
    private ThreadPoolTaskExecutor cacheExecutor;

    /** 限制同步回源并发，避免 Redis 异常时打满数据库连接池。 */
    private final Semaphore databaseFallbackSemaphore = new Semaphore(2);
    /** 防止同一实例重复提交待刷新集合消费任务。 */
    private final AtomicBoolean refreshWorkerRunning = new AtomicBoolean(false);
    /** 标记 Redis 兜底失败后需要执行一次全量补偿。 */
    private final AtomicBoolean fullRebuildRequested = new AtomicBoolean(false);
    /** 防止同一实例重复提交全量补偿任务。 */
    private final AtomicBoolean fullRebuildWorkerRunning = new AtomicBoolean(false);
    /** 限制自动全量补偿次数，避免故障期间无限重试。 */
    private final AtomicInteger fullRebuildRetryCount = new AtomicInteger(0);
    /** 标识本地全量补偿请求版本，防止构建期间的新请求被旧任务清除。 */
    private final AtomicLong fullRebuildRequestVersion = new AtomicLong(0);

    /**
     * 应用启动后异步预热全部城市门店缓存。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCache() {
        requestFullRebuild("应用启动预热");
    }

    /**
     * 每六小时异步校准一次全量缓存。
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void scheduledRebuildAll() {
        requestFullRebuild("六小时定时全量校准");
    }

    /**
     * 定时消费因线程池繁忙而暂未处理的门店刷新任务。
     */
    @Scheduled(fixedDelay = 5000L, initialDelay = 15000L)
    public void scheduledDrainPendingStores() {
        startRequestedFullRebuild();
        startRefreshWorker();
    }

    /**
     * 按城市读取门店快照。缓存命中时不会访问数据库。
     *
     * @param cityName 完整城市名称
     * @return 城市门店快照
     */
    public List<StoreCityListCacheValue> getStoresByCity(String cityName) {
        if (!StringUtils.hasText(cityName)) {
            return Collections.emptyList();
        }
        String normalizedCity = cityName.trim();
        try {
            String generation = getOrCreateActiveGeneration();
            List<StoreCityListCacheValue> cached = readCity(generation, normalizedCity);
            if (cached != null) {
                return cached;
            }
            boolean rebuildOwner = rebuildCityWithLock(generation, normalizedCity);
            cached = readCity(generation, normalizedCity);
            if (cached != null) {
                return cached;
            }
            if (!rebuildOwner) {
                log.warn("城市门店缓存等待其他实例重建超时，将受限降级查询数据库，城市={}", normalizedCity);
            }
        } catch (Exception ex) {
            log.error("读取城市门店缓存失败，降级查询数据库，城市={}", normalizedCity, ex);
        }
        return queryCityWithPermit(normalizedCity);
    }

    /**
     * 批量读取门店背景图片。未命中时直接返回默认背景，不查询背景配置表。
     *
     * @param storeIds 门店 ID 集合
     * @return 门店 ID 与背景图片映射
     */
    public Map<Long, String> getBackgroundImages(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> normalizedIds = storeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new HashMap<>();
        try {
            List<Object> fields = normalizedIds.stream().map(String::valueOf).collect(Collectors.toList());
            List<Object> values = stringRedisTemplate.opsForHash().multiGet(BACKGROUND_DATA_KEY, fields);
            String defaultImage = parseBackgroundImage(stringRedisTemplate.opsForValue().get(BACKGROUND_DEFAULT_KEY));
            List<String> missingIds = new ArrayList<>();
            for (int i = 0; i < normalizedIds.size(); i++) {
                String image = values == null || i >= values.size() ? null
                        : parseBackgroundImage(Objects.toString(values.get(i), null));
                if (!StringUtils.hasText(image)) {
                    image = defaultImage;
                    missingIds.add(String.valueOf(normalizedIds.get(i)));
                }
                result.put(normalizedIds.get(i), image);
            }
            if (!missingIds.isEmpty()) {
                stringRedisTemplate.opsForSet().add(BACKGROUND_PENDING_KEY, missingIds.toArray(String[]::new));
            }
        } catch (Exception ex) {
            log.error("批量读取门店背景缓存失败，门店数量={}", normalizedIds.size(), ex);
        }
        return result;
    }

    /**
     * 在事务提交后刷新单个门店缓存。
     *
     * @param storeId 门店 ID
     * @param oldCity 修改前城市
     * @param newCity 修改后城市
     * @param removed 是否从列表中移除
     */
    public void refreshStoreAfterCommit(Long storeId, String oldCity, String newCity, boolean removed) {
        if (storeId == null) {
            return;
        }
        afterCommitAsync(() -> invalidateAndEnqueue(storeId, oldCity, newCity, removed),
                Collections.singletonList(storeId), "单门店缓存失效");
    }

    /**
     * 在事务提交后批量刷新门店缓存。
     *
     * @param storeIds 门店 ID 集合
     */
    public void refreshStoresAfterCommit(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return;
        }
        List<Long> normalizedIds = storeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return;
        }
        afterCommitAsync(() -> invalidateAndEnqueueBatch(normalizedIds), normalizedIds, "批量门店缓存失效");
    }

    /**
     * 在事务提交后批量移除已经失效的门店缓存。
     *
     * @param storeCityMap 门店 ID 与修改前城市映射
     */
    public void removeStoresAfterCommit(Map<Long, String> storeCityMap) {
        if (storeCityMap == null || storeCityMap.isEmpty()) {
            return;
        }
        Map<Long, String> normalizedMap = storeCityMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left));
        if (normalizedMap.isEmpty()) {
            return;
        }
        afterCommitAsync(() -> removeStores(normalizedMap), normalizedMap.keySet(), "批量门店缓存移除");
    }

    /**
     * 门店标签变化后通知门店背景缓存重新匹配模板。
     *
     * <p>当前分支未包含背景缓存 Java 源码，因此通过可选 Bean 调用保持分支解耦；
     * 背景模块尚未装配时仅写入它的待重建集合。</p>
     *
     * @param storeIds 门店 ID 集合
     */
    public void refreshBackgroundAfterCommit(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return;
        }
        List<Long> normalizedIds = storeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return;
        }
        try {
            Class<?> serviceClass = findBackgroundCacheServiceClass();
            Object backgroundService = applicationContext.getBean(serviceClass);
            // 背景服务自身负责事务提交回调，此处不能在 afterCompletion 中再次注册回调。
            serviceClass.getMethod("refreshStoresAfterCommit", Collection.class)
                    .invoke(backgroundService, normalizedIds);
        } catch (ClassNotFoundException ex) {
            afterCommit(() -> enqueueBackgroundFallback(normalizedIds));
            log.info("门店背景缓存服务尚未装配，门店将在事务提交后加入背景待重建集合，门店数量={}", normalizedIds.size());
        } catch (Exception ex) {
            afterCommit(() -> enqueueBackgroundFallback(normalizedIds));
            log.error("通知门店背景缓存刷新失败，将使用待重建集合兜底，门店数量={}", normalizedIds.size(), ex);
        }
    }

    /**
     * 门店删除后移除其背景缓存。
     *
     * @param storeId 门店 ID
     */
    public void removeBackgroundAfterCommit(Long storeId) {
        if (storeId == null) {
            return;
        }
        try {
            Class<?> serviceClass = findBackgroundCacheServiceClass();
            Object backgroundService = applicationContext.getBean(serviceClass);
            serviceClass.getMethod("removeStoreAfterCommit", Long.class).invoke(backgroundService, storeId);
        } catch (ClassNotFoundException ex) {
            afterCommit(() -> removeBackgroundFallback(storeId));
        } catch (Exception ex) {
            afterCommit(() -> removeBackgroundFallback(storeId));
            log.error("通知门店背景缓存删除失败，将使用 Redis 删除兜底，门店ID={}", storeId, ex);
        }
    }

    /**
     * 全量重建城市门店缓存，并在版本未变化时切换新缓存代数。
     */
    public void rebuildAll() {
        rebuildAllInternal();
    }

    /**
     * 同步全量刷新城市门店缓存。
     *
     * @return 新缓存代数完整构建并成功切换时返回 {@code true}
     */
    public boolean refreshAllSynchronously() {
        log.info("开始手动同步刷新城市门店缓存");
        boolean success = rebuildAllInternal();
        if (success) {
            log.info("手动同步刷新城市门店缓存成功");
        } else {
            log.warn("手动同步刷新城市门店缓存未完成，请根据前序日志定位原因");
        }
        return success;
    }

    /**
     * 执行城市门店缓存全量重建，并返回本次是否成功切换缓存代数。
     */
    private boolean rebuildAllInternal() {
        long requestVersion = fullRebuildRequestVersion.get();
        RLock lock = redissonClient.getLock(PREHEAT_LOCK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 10, TimeUnit.MINUTES);
            if (!locked) {
                // 其他实例已经承担本轮全量构建，本实例不再排队重复扫描数据库。
                if (requestVersion == fullRebuildRequestVersion.get()) {
                    fullRebuildRequested.set(false);
                    fullRebuildRetryCount.set(0);
                }
                log.info("城市门店缓存全量重建已由其他实例执行，本实例跳过");
                return false;
            }
            String expectedRevision = currentRevision();
            String generation = String.valueOf(System.currentTimeMillis());
            Map<Long, StoreCityListCacheValue> values = executeWithDatabasePermit(
                    () -> loadCacheValues(selectEligibleStores(null, null)), "城市门店缓存全量预热繁忙");
            writeGeneration(generation, values.values());
            // 新代数写完后先释放预热锁，再获取短时版本锁完成切换，避免嵌套 Redis 锁。
            unlock(lock, true);
            locked = false;
            if (!switchGeneration(expectedRevision, generation)) {
                log.warn("城市门店缓存全量重建期间数据发生变化，放弃切换，缓存代数={}", generation);
                return false;
            }
            if (requestVersion == fullRebuildRequestVersion.get()) {
                fullRebuildRequested.set(false);
                fullRebuildRetryCount.set(0);
            }
            log.info("城市门店缓存全量重建完成，缓存代数={}，门店数量={}，城市数量={}",
                    generation, values.size(), values.values().stream()
                            .map(StoreCityListCacheValue::getCityName).filter(StringUtils::hasText).distinct().count());
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存全量重建线程被中断");
            return false;
        } catch (Exception ex) {
            log.error("城市门店缓存全量重建失败", ex);
            return false;
        } finally {
            unlock(lock, locked);
        }
    }

    /**
     * 读取已构建城市缓存；返回 null 表示该城市需要回源。
     */
    private List<StoreCityListCacheValue> readCity(String generation, String cityName) {
        Object ready = stringRedisTemplate.opsForHash().get(readyKey(generation), cityName);
        if (ready == null) {
            return null;
        }
        Set<String> storeIdStrings = stringRedisTemplate.opsForSet().members(cityKey(generation, cityName));
        Long expectedSize = parseLong(ready.toString());
        int actualSize = storeIdStrings == null ? 0 : storeIdStrings.size();
        if (expectedSize == null || expectedSize != actualSize) {
            log.warn("城市门店缓存索引数量不一致，将重新加载，城市={}，标记数量={}，实际数量={}",
                    cityName, expectedSize, actualSize);
            return null;
        }
        if (actualSize == 0) {
            return Collections.emptyList();
        }
        List<String> storeIds = new ArrayList<>(storeIdStrings);
        if (storeIds.stream().anyMatch(id -> parseLong(id) == null)) {
            log.warn("城市门店缓存索引包含非法门店ID，将重新加载，城市={}", cityName);
            return null;
        }
        List<Object> fields = new ArrayList<>(storeIds);
        List<Object> jsonValues = stringRedisTemplate.opsForHash().multiGet(dataKey(generation), fields);
        List<Long> missingIds = new ArrayList<>();
        List<StoreCityListCacheValue> result = parseValues(cityName, storeIds, jsonValues, missingIds);
        if (!missingIds.isEmpty()) {
            rebuildMissingStores(generation, cityName, missingIds);
            // 补齐期间城市索引可能因门店修改或跨城市迁移发生变化，必须重新读取索引和完成标记。
            Object refreshedReady = stringRedisTemplate.opsForHash().get(readyKey(generation), cityName);
            Set<String> refreshedStoreIdStrings = stringRedisTemplate.opsForSet()
                    .members(cityKey(generation, cityName));
            Long refreshedExpectedSize = refreshedReady == null ? null : parseLong(refreshedReady.toString());
            int refreshedActualSize = refreshedStoreIdStrings == null ? 0 : refreshedStoreIdStrings.size();
            if (refreshedExpectedSize == null || refreshedExpectedSize != refreshedActualSize) {
                return null;
            }
            if (refreshedActualSize == 0) {
                return Collections.emptyList();
            }
            storeIds = new ArrayList<>(refreshedStoreIdStrings);
            if (storeIds.stream().anyMatch(id -> parseLong(id) == null)) {
                log.warn("城市门店缓存补齐后索引仍包含非法门店ID，将重新加载，城市={}", cityName);
                return null;
            }
            fields = new ArrayList<>(storeIds);
            jsonValues = stringRedisTemplate.opsForHash().multiGet(dataKey(generation), fields);
            List<Long> remainingMissingIds = new ArrayList<>();
            result = parseValues(cityName, storeIds, jsonValues, remainingMissingIds);
            if (!remainingMissingIds.isEmpty()) {
                return null;
            }
        }
        return result;
    }

    /**
     * 解析 Redis 中的门店快照，并记录缺失或损坏的数据。
     */
    private List<StoreCityListCacheValue> parseValues(String cityName, List<String> storeIds, List<Object> jsonValues,
                                                       List<Long> missingIds) {
        List<StoreCityListCacheValue> result = new ArrayList<>();
        for (int i = 0; i < storeIds.size(); i++) {
            String json = jsonValues == null || i >= jsonValues.size() ? null
                    : Objects.toString(jsonValues.get(i), null);
            if (!StringUtils.hasText(json)) {
                missingIds.add(parseLong(storeIds.get(i)));
                continue;
            }
            try {
                StoreCityListCacheValue value = JsonUtils.parseObject(json, StoreCityListCacheValue.class);
                Long expectedStoreId = parseLong(storeIds.get(i));
                if (value != null && Objects.equals(expectedStoreId, value.getStoreId())
                        && Objects.equals(cityName, value.getCityName())
                        && value.getLongitude() != null && value.getLatitude() != null) {
                    result.add(value);
                } else if (expectedStoreId != null) {
                    missingIds.add(expectedStoreId);
                }
            } catch (Exception ex) {
                missingIds.add(parseLong(storeIds.get(i)));
                log.warn("城市门店缓存 JSON 解析失败，将重新加载，门店ID={}", storeIds.get(i), ex);
            }
        }
        missingIds.removeIf(Objects::isNull);
        return result;
    }

    /**
     * 使用城市级分布式锁重建冷城市，避免并发请求击穿数据库。
     */
    private boolean rebuildCityWithLock(String generation, String cityName) {
        RLock lock = redissonClient.getLock(cityLockKey(cityName));
        boolean locked = false;
        try {
            locked = lock.tryLock(300, 30, TimeUnit.SECONDS);
            if (!locked) {
                waitForCityCache(generation, cityName);
                return false;
            }
            Object ready = stringRedisTemplate.opsForHash().get(readyKey(generation), cityName);
            Long expectedSize = ready == null ? null : parseLong(ready.toString());
            Long actualSize = stringRedisTemplate.opsForSet().size(cityKey(generation, cityName));
            if (expectedSize != null && Objects.equals(expectedSize, actualSize)) {
                return true;
            }
            stringRedisTemplate.opsForHash().delete(readyKey(generation), cityName);
            String expectedRevision = currentRevision();
            List<StoreCityListCacheValue> values = queryCityWithPermit(cityName);
            if (!Objects.equals(expectedRevision, currentRevision())) {
                log.warn("城市门店缓存回源期间数据版本已变化，放弃写入，城市={}", cityName);
                return true;
            }
            writeCity(generation, cityName, values);
            if (!Objects.equals(expectedRevision, currentRevision())) {
                // 修改通知先递增版本；若冲突发生在写入过程中，清除城市完整标记避免返回旧快照。
                stringRedisTemplate.delete(cityKey(generation, cityName));
                stringRedisTemplate.opsForHash().delete(readyKey(generation), cityName);
                log.warn("城市门店缓存写入期间数据版本已变化，已清除局部结果，城市={}", cityName);
                return true;
            }
            log.info("城市门店缓存回源完成，城市={}，门店数量={}", cityName, values.size());
            return true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存回源线程被中断，城市={}", cityName);
            return false;
        } finally {
            unlock(lock, locked);
        }
    }

    /**
     * 只回源补齐 Hash 中缺失的门店，避免重新查询整个城市。
     */
    private void rebuildMissingStores(String generation, String cityName, List<Long> storeIds) {
        RLock lock = redissonClient.getLock(cityLockKey(cityName));
        boolean locked = false;
        try {
            locked = lock.tryLock(300, 30, TimeUnit.SECONDS);
            if (!locked) {
                waitForStoreValues(generation, storeIds);
                return;
            }
            reloadMissingStores(generation, cityName, storeIds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存缺失数据补齐线程被中断，城市={}", cityName);
        } finally {
            unlock(lock, locked);
        }
    }

    /**
     * 查询并回填缺失门店，同时移除已经失效的城市索引。
     */
    private void reloadMissingStores(String generation, String cityName, List<Long> storeIds) {
        String expectedRevision = currentRevision();
        Map<Long, StoreCityListCacheValue> values = executeWithDatabasePermit(
                () -> loadCacheValues(selectEligibleStores(null, storeIds)), "城市门店缓存单店刷新繁忙");
        if (!Objects.equals(expectedRevision, currentRevision())) {
            log.warn("城市门店缓存缺失数据查询期间版本已变化，放弃本次补齐，城市={}，门店数量={}",
                    cityName, storeIds.size());
            return;
        }
        writeValues(generation, values.values());
        if (!Objects.equals(expectedRevision, currentRevision())) {
            stringRedisTemplate.delete(cityKey(generation, cityName));
            stringRedisTemplate.opsForHash().delete(readyKey(generation), cityName);
            log.warn("城市门店缓存缺失数据写入期间版本已变化，已清除局部结果，城市={}，门店数量={}",
                    cityName, storeIds.size());
            return;
        }
        Set<Long> validCityIds = values.values().stream()
                .filter(value -> Objects.equals(cityName, value.getCityName()))
                .map(StoreCityListCacheValue::getStoreId)
                .collect(Collectors.toSet());
        List<String> invalidCityIds = storeIds.stream().filter(id -> !validCityIds.contains(id))
                .map(String::valueOf).toList();
        if (!invalidCityIds.isEmpty()) {
            stringRedisTemplate.opsForSet().remove(cityKey(generation, cityName), invalidCityIds.toArray());
            // 跨城市门店的最新快照已经写入新城市，只删除真正失效或不存在门店的 Hash 数据。
            List<String> invalidCacheIds = storeIds.stream().filter(id -> !values.containsKey(id))
                    .map(String::valueOf).toList();
            if (!invalidCacheIds.isEmpty()) {
                stringRedisTemplate.opsForHash().delete(dataKey(generation), invalidCacheIds.toArray());
            }
            updateReadyCount(generation, cityName);
        }
        values.values().stream().map(StoreCityListCacheValue::getCityName).distinct()
                .forEach(city -> updateReadyCountIfPresent(generation, city));
        log.info("城市门店缓存缺失数据补齐完成，城市={}，请求数量={}，有效数量={}",
                cityName, storeIds.size(), validCityIds.size());
    }

    /**
     * 查询指定城市并合并门店、费用和企微数据。
     */
    private List<StoreCityListCacheValue> queryCityWithPermit(String cityName) {
        return executeWithDatabasePermit(() -> {
            List<SystemStoreInfoDO> stores = selectEligibleStores(cityName, null);
            return new ArrayList<>(loadCacheValues(stores).values());
        }, "城市门店缓存回源任务繁忙");
    }

    /**
     * 在统一信号量保护下执行缓存数据库查询，限制缓存任务的总查询并发数。
     */
    private <T> T executeWithDatabasePermit(Supplier<T> supplier, String busyMessage) {
        boolean acquired = false;
        try {
            acquired = databaseFallbackSemaphore.tryAcquire(2, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException(busyMessage + "，请稍后重试");
            }
            return DataPermissionUtils.executeIgnore((Callable<T>) supplier::get);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("城市门店缓存数据库查询线程被中断", ex);
        } finally {
            if (acquired) {
                databaseFallbackSemaphore.release();
            }
        }
    }

    /**
     * 查询满足小程序门店列表基础条件的门店。
     */
    private List<SystemStoreInfoDO> selectEligibleStores(String cityName, Collection<Long> storeIds) {
        LambdaQueryWrapperX<SystemStoreInfoDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(SystemStoreInfoDO::getBusinessId, BUSINESS_ID);
        wrapper.eq(SystemStoreInfoDO::getStoreSource, 0);
        wrapper.eq(SystemStoreInfoDO::getStoreStatus, StoreStatusEnum.OPEN.getStatus());
        wrapper.in(SystemStoreInfoDO::getOrderType, 0, 2);
        wrapper.eqIfPresent(SystemStoreInfoDO::getStoreCity, cityName);
        wrapper.inIfPresent(SystemStoreInfoDO::getStoreId, storeIds);
        return systemStoreInfoMapper.selectList(wrapper).stream()
                .filter(this::isEligible)
                .toList();
    }

    /**
     * 按门店 ID 查询当前记录，由调用方判断是否仍满足列表条件。
     */
    private List<SystemStoreInfoDO> selectStoresByIds(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return systemStoreInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                .in(SystemStoreInfoDO::getStoreId, storeIds));
    }

    /**
     * 判断门店是否满足城市门店列表的基础条件。
     */
    private boolean isEligible(SystemStoreInfoDO store) {
        return store != null
                && Objects.equals(store.getBusinessId(), BUSINESS_ID)
                && Objects.equals(String.valueOf(store.getStoreSource()), "0")
                && Objects.equals(store.getStoreStatus(), StoreStatusEnum.OPEN.getStatus())
                && (Objects.equals(store.getOrderType(), 0) || Objects.equals(store.getOrderType(), 2))
                && store.getStoreLongitude() != null
                && store.getStoreLatitude() != null
                && StringUtils.hasText(store.getStoreCity());
    }

    /**
     * 批量加载门店费用和企微配置并组装缓存值。
     */
    private Map<Long, StoreCityListCacheValue> loadCacheValues(List<SystemStoreInfoDO> stores) {
        if (stores == null || stores.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, StoreCityListCacheValue> result = new LinkedHashMap<>();
        for (int from = 0; from < stores.size(); from += BATCH_SIZE) {
            List<SystemStoreInfoDO> batch = stores.subList(from, Math.min(from + BATCH_SIZE, stores.size()));
            Set<Long> storeIds = batch.stream().map(SystemStoreInfoDO::getStoreId).collect(Collectors.toSet());
            Map<Integer, Map<Long, SystemStoreExpensesDO>> expenseMap = loadExpenses(storeIds);
            Map<Long, StoreWecomConfigDO> wecomMap = loadWecomConfigs(storeIds);
            for (SystemStoreInfoDO store : batch) {
                result.put(store.getStoreId(), toCacheValue(store, expenseMap, wecomMap.get(store.getStoreId())));
            }
        }
        return result;
    }

    /**
     * 批量查询门店费用并按费用类型和门店分组。
     */
    private Map<Integer, Map<Long, SystemStoreExpensesDO>> loadExpenses(Set<Long> storeIds) {
        List<SystemStoreExpensesDO> expenses = systemStoreExpensesMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreExpensesDO>()
                        .in(SystemStoreExpensesDO::getStoreId, storeIds)
                        .in(SystemStoreExpensesDO::getStoreExpensesType, 0, 1, 2, 3));
        return expenses.stream().collect(Collectors.groupingBy(
                SystemStoreExpensesDO::getStoreExpensesType,
                Collectors.toMap(SystemStoreExpensesDO::getStoreId, Function.identity(), (left, right) -> left)));
    }

    /**
     * 批量查询企微配置，并保持原接口处理重复配置时的选择规则。
     */
    private Map<Long, StoreWecomConfigDO> loadWecomConfigs(Set<Long> storeIds) {
        return storeWecomConfigMapper.selectList(new LambdaQueryWrapperX<StoreWecomConfigDO>()
                        .in(StoreWecomConfigDO::getStoreId, storeIds))
                .stream().collect(Collectors.toMap(StoreWecomConfigDO::getStoreId, Function.identity(),
                        this::selectWecomConfig));
    }

    /**
     * 选择同一门店需要用于列表展示的企微配置。
     */
    private StoreWecomConfigDO selectWecomConfig(StoreWecomConfigDO existing, StoreWecomConfigDO replacement) {
        if (!StringUtils.hasText(replacement.getQrCommunityCode())) {
            return replacement;
        } else if (!StringUtils.hasText(existing.getQrCommunityCode())) {
            return existing;
        }
        if (!StringUtils.hasText(replacement.getQrCode())) {
            return replacement;
        } else if (!StringUtils.hasText(existing.getQrCode())) {
            return existing;
        }
        return replacement;
    }

    /**
     * 将数据库记录转换为静态门店缓存值。
     */
    private StoreCityListCacheValue toCacheValue(SystemStoreInfoDO store,
                                                  Map<Integer, Map<Long, SystemStoreExpensesDO>> expenses,
                                                  StoreWecomConfigDO wecom) {
        StoreCityListCacheValue value = new StoreCityListCacheValue();
        value.setStoreId(store.getStoreId());
        value.setStoreName(store.getStoreName());
        value.setStoreAddress(defaultString(store.getStoreAddress()));
        value.setLongitude(store.getStoreLongitude());
        value.setLatitude(store.getStoreLatitude());
        value.setOpenStatus(store.getOpenStatus());
        value.setStoreTakeaway(store.getStoreTakeaway());
        value.setStoreHours(defaultString(store.getStoreHours()));
        value.setStoreAnnouncement(defaultString(store.getStoreAnnouncement()));
        value.setStorePhone(defaultString(store.getStorePhone()));
        value.setStorePayType(store.getStorePayType());
        value.setStoreWithoutPayment(store.getStoreWithoutPayment());
        value.setCampusDeliveryStatus(store.getCampusDeliveryStatus() == null ? 1 : store.getCampusDeliveryStatus());
        value.setCampusDeliverySubsidy(defaultMoney(store.getCampusDeliverySubsidy()));
        value.setCampusDeliveryFee(BigDecimal.ZERO);
        value.setCampusDeliveryCalculationType(null);
        value.setTiktokId(store.getTiktokId());
        value.setDeliveryTime(defaultString(store.getDeliveryTime()));
        value.setIsPrompt(store.getIsPrompt() == null ? 0 : store.getIsPrompt());
        value.setPromptText(defaultString(store.getPromptText()));
        value.setCityName(defaultString(store.getStoreCity()));

        SystemStoreExpensesDO delivery = getExpense(expenses, 2, store.getStoreId());
        if (delivery != null) {
            value.setAdditionaaCosts(delivery.getAdditionaaCosts());
            value.setMinimumDeliveryFee(delivery.getMinimumDeliveryFee());
        }
        SystemStoreExpensesDO packageExpense = getExpense(expenses, 0, store.getStoreId());
        if (packageExpense != null) {
            value.setPackCosts(packageExpense.getAdditionaaCosts());
            value.setMinimumPackageFee(packageExpense.getMinimumDeliveryFee());
            value.setStoreCalculationType(packageExpense.getStoreCalculationType());
        }
        SystemStoreExpensesDO takeawayPackage = getExpense(expenses, 1, store.getStoreId());
        if (takeawayPackage != null) {
            value.setPackDeliveryCosts(takeawayPackage.getAdditionaaCosts());
            value.setMinimumDeliveryPackFee(takeawayPackage.getMinimumDeliveryFee());
            value.setStoreDeliveryCalculationType(takeawayPackage.getStoreCalculationType());
        }
        SystemStoreExpensesDO campus = getExpense(expenses, 3, store.getStoreId());
        value.setCampusMinimumDeliveryFee(campus == null ? BigDecimal.ZERO
                : defaultMoney(campus.getMinimumDeliveryFee()));
        if (wecom != null) {
            value.setQrCode(wecom.getQrCode());
            value.setQrType(wecom.getQrType());
        }
        return value;
    }

    /**
     * 获取指定门店和类型的费用配置。
     */
    private SystemStoreExpensesDO getExpense(Map<Integer, Map<Long, SystemStoreExpensesDO>> expenses,
                                             int type, Long storeId) {
        return expenses.getOrDefault(type, Collections.emptyMap()).get(storeId);
    }

    /**
     * 将单店缓存标记失效并加入待刷新集合。
     */
    private void invalidateAndEnqueue(Long storeId, String oldCity, String newCity, boolean removed) {
        RLock revisionLock = redissonClient.getLock(REVISION_LOCK_KEY);
        boolean locked = false;
        boolean completed = false;
        try {
            locked = tryAcquireRevisionLock(revisionLock);
            if (!locked) {
                log.warn("城市门店缓存版本锁竞争超时，将门店写入待刷新集合，门店ID={}", storeId);
                enqueuePendingWithoutExecutor(Collections.singletonList(storeId), "单门店版本锁竞争补偿");
                return;
            }
            String generation = stringRedisTemplate.opsForValue().get(ACTIVE_KEY);
            // 先递增版本，使并发中的数据库回源在写入前或写入后能够识别旧结果。
            stringRedisTemplate.opsForValue().increment(REVISION_KEY);
            if (StringUtils.hasText(generation)) {
                stringRedisTemplate.opsForHash().delete(dataKey(generation), String.valueOf(storeId));
                if (removed && StringUtils.hasText(oldCity)) {
                    stringRedisTemplate.opsForSet().remove(cityKey(generation, oldCity), String.valueOf(storeId));
                    updateReadyCountIfPresent(generation, oldCity);
                } else if (StringUtils.hasText(oldCity) && StringUtils.hasText(newCity)
                        && !Objects.equals(oldCity, newCity)) {
                    stringRedisTemplate.opsForSet().remove(cityKey(generation, oldCity), String.valueOf(storeId));
                    updateReadyCountIfPresent(generation, oldCity);
                    stringRedisTemplate.opsForHash().delete(readyKey(generation), newCity);
                } else if (StringUtils.hasText(oldCity) && !StringUtils.hasText(newCity)) {
                    stringRedisTemplate.opsForSet().remove(cityKey(generation, oldCity), String.valueOf(storeId));
                    updateReadyCountIfPresent(generation, oldCity);
                } else if (!StringUtils.hasText(oldCity) && StringUtils.hasText(newCity)) {
                    stringRedisTemplate.opsForHash().delete(readyKey(generation), newCity);
                }
            }
            if (!removed) {
                stringRedisTemplate.opsForHash().delete(RETRY_KEY, String.valueOf(storeId));
                stringRedisTemplate.opsForSet().add(PENDING_KEY, String.valueOf(storeId));
            }
            log.info("城市门店缓存已标记刷新，门店ID={}，原城市={}，新城市={}，是否移除={}",
                    storeId, oldCity, newCity, removed);
            completed = true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存失效处理线程被中断，门店ID={}", storeId);
        } catch (Exception ex) {
            log.error("城市门店缓存失效处理失败，门店ID={}", storeId, ex);
        } finally {
            unlock(revisionLock, locked);
        }
        if (completed && !removed) {
            startRefreshWorker();
        }
        if (!completed) {
            enqueuePendingWithoutExecutor(Collections.singletonList(storeId), "单门店缓存失效异常补偿");
        }
    }

    /**
     * 批量失效静态快照并提交异步刷新，避免逐门店获取版本锁。
     */
    private void invalidateAndEnqueueBatch(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return;
        }
        RLock revisionLock = redissonClient.getLock(REVISION_LOCK_KEY);
        boolean locked = false;
        boolean completed = false;
        try {
            locked = tryAcquireRevisionLock(revisionLock);
            if (!locked) {
                log.warn("城市门店缓存批量失效版本锁竞争超时，将门店写入待刷新集合，门店数量={}", storeIds.size());
                enqueuePendingWithoutExecutor(storeIds, "批量刷新版本锁竞争补偿");
                return;
            }
            String generation = stringRedisTemplate.opsForValue().get(ACTIVE_KEY);
            String[] fields = storeIds.stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForValue().increment(REVISION_KEY);
            if (StringUtils.hasText(generation)) {
                stringRedisTemplate.opsForHash().delete(dataKey(generation), (Object[]) fields);
            }
            stringRedisTemplate.opsForHash().delete(RETRY_KEY, (Object[]) fields);
            stringRedisTemplate.opsForSet().add(PENDING_KEY, fields);
            log.info("城市门店缓存已批量标记刷新，门店数量={}，是否取得版本锁={}", storeIds.size(), locked);
            completed = true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存批量失效线程被中断，门店数量={}", storeIds.size());
        } catch (Exception ex) {
            log.error("城市门店缓存批量失效失败，门店数量={}", storeIds.size(), ex);
        } finally {
            unlock(revisionLock, locked);
        }
        if (completed) {
            startRefreshWorker();
        } else {
            enqueuePendingWithoutExecutor(storeIds, "批量刷新异常补偿");
        }
    }

    /**
     * 批量移除关闭或删除门店，版本锁竞争时将门店保留在待刷新集合。
     */
    private void removeStores(Map<Long, String> storeCityMap) {
        RLock revisionLock = redissonClient.getLock(REVISION_LOCK_KEY);
        boolean locked = false;
        boolean completed = false;
        try {
            locked = tryAcquireRevisionLock(revisionLock);
            if (!locked) {
                log.warn("城市门店缓存批量移除版本锁竞争超时，将门店写入待刷新集合，门店数量={}", storeCityMap.size());
                enqueuePendingWithoutExecutor(storeCityMap.keySet(), "批量移除版本锁竞争补偿");
                return;
            }
            String generation = stringRedisTemplate.opsForValue().get(ACTIVE_KEY);
            String[] fields = storeCityMap.keySet().stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForValue().increment(REVISION_KEY);
            if (StringUtils.hasText(generation)) {
                stringRedisTemplate.opsForHash().delete(dataKey(generation), (Object[]) fields);
                Map<String, List<String>> cityStoreIds = storeCityMap.entrySet().stream()
                        .filter(entry -> StringUtils.hasText(entry.getValue()))
                        .collect(Collectors.groupingBy(Map.Entry::getValue,
                                Collectors.mapping(entry -> String.valueOf(entry.getKey()), Collectors.toList())));
                for (Map.Entry<String, List<String>> entry : cityStoreIds.entrySet()) {
                    stringRedisTemplate.opsForSet().remove(cityKey(generation, entry.getKey()),
                            entry.getValue().toArray());
                    updateReadyCountIfPresent(generation, entry.getKey());
                }
            }
            log.info("城市门店缓存已批量移除，门店数量={}，是否取得版本锁={}", storeCityMap.size(), locked);
            completed = true;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存批量移除线程被中断，门店数量={}", storeCityMap.size());
        } catch (Exception ex) {
            log.error("城市门店缓存批量移除失败，门店数量={}", storeCityMap.size(), ex);
        } finally {
            unlock(revisionLock, locked);
        }
        if (!completed) {
            enqueuePendingWithoutExecutor(storeCityMap.keySet(), "批量移除异常补偿");
        }
    }

    /**
     * 启动待刷新门店消费任务。
     */
    private void startRefreshWorker() {
        if (!refreshWorkerRunning.compareAndSet(false, true)) {
            return;
        }
        boolean submitted = submitSafely(() -> {
            try {
                drainPendingStores();
            } finally {
                refreshWorkerRunning.set(false);
            }
        }, "待刷新门店消费");
        if (!submitted) {
            refreshWorkerRunning.set(false);
        }
    }

    /**
     * 单次最多处理一批待刷新门店，不使用无限循环。
     */
    private void drainPendingStores() {
        RLock refreshLock = redissonClient.getLock(REFRESH_LOCK_KEY);
        boolean locked = false;
        Set<String> ids = Collections.emptySet();
        try {
            locked = refreshLock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }
            // 任务成功前不从 Set 删除，进程异常退出后仍可由下一轮继续处理。
            ids = stringRedisTemplate.opsForSet().distinctRandomMembers(PENDING_KEY, BATCH_SIZE);
            if (ids == null || ids.isEmpty()) {
                return;
            }
            List<Long> storeIds = ids.stream().map(this::parseLong).filter(Objects::nonNull).toList();
            String generation = getOrCreateActiveGeneration();
            String expectedRevision = currentRevision();
            List<SystemStoreInfoDO> currentStores = executeWithDatabasePermit(
                    () -> selectStoresByIds(storeIds), "城市门店缓存异步刷新繁忙");
            List<SystemStoreInfoDO> eligibleStores = currentStores.stream().filter(this::isEligible).toList();
            Map<Long, StoreCityListCacheValue> values = executeWithDatabasePermit(
                    () -> loadCacheValues(eligibleStores), "城市门店缓存异步刷新繁忙");
            if (!Objects.equals(expectedRevision, currentRevision())) {
                log.warn("城市门店缓存异步查询期间版本已变化，本批任务保留等待重试，门店数量={}", storeIds.size());
                return;
            }
            writeValues(generation, values.values());
            if (!Objects.equals(expectedRevision, currentRevision())) {
                cleanupConflictedRefresh(generation, values.values());
                stringRedisTemplate.opsForHash().delete(dataKey(generation),
                        storeIds.stream().map(String::valueOf).toArray());
                log.warn("城市门店缓存异步写入期间版本已变化，已清除局部结果并保留任务，门店数量={}", storeIds.size());
                return;
            }
            values.values().stream().map(StoreCityListCacheValue::getCityName).distinct()
                    .forEach(city -> updateReadyCountIfPresent(generation, city));
            Set<Long> validIds = values.keySet();
            List<String> invalidIds = storeIds.stream().filter(id -> !validIds.contains(id))
                    .map(String::valueOf).toList();
            if (!invalidIds.isEmpty()) {
                stringRedisTemplate.opsForHash().delete(dataKey(generation), invalidIds.toArray());
                Map<String, List<String>> invalidCityStoreIds = currentStores.stream()
                        .filter(store -> !validIds.contains(store.getStoreId()))
                        .filter(store -> StringUtils.hasText(store.getStoreCity()))
                        .collect(Collectors.groupingBy(SystemStoreInfoDO::getStoreCity,
                                Collectors.mapping(store -> String.valueOf(store.getStoreId()), Collectors.toList())));
                for (Map.Entry<String, List<String>> entry : invalidCityStoreIds.entrySet()) {
                    stringRedisTemplate.opsForSet().remove(cityKey(generation, entry.getKey()),
                            entry.getValue().toArray());
                    updateReadyCountIfPresent(generation, entry.getKey());
                }
            }
            stringRedisTemplate.opsForSet().remove(PENDING_KEY, ids.toArray());
            stringRedisTemplate.opsForHash().delete(RETRY_KEY, ids.toArray());
            if (!Objects.equals(expectedRevision, currentRevision())) {
                // 若修改通知恰好发生在确认任务期间，重新入队，避免删除新一轮刷新通知。
                stringRedisTemplate.opsForSet().add(PENDING_KEY, ids.toArray(String[]::new));
                log.warn("城市门店缓存确认任务期间版本已变化，已将本批门店重新入队，门店数量={}", storeIds.size());
                return;
            }
            log.info("城市门店缓存异步刷新完成，请求门店数={}，有效门店数={}", storeIds.size(), values.size());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("城市门店缓存异步刷新线程被中断，任务保留在待刷新集合中");
        } catch (Exception ex) {
            // 单个门店最多重试三次，避免异常数据形成永久循环。
            log.error("城市门店缓存异步刷新失败，任务保留在待刷新集合中", ex);
            boolean retryExhausted = false;
            try {
                if (!ids.isEmpty()) {
                    for (String id : ids) {
                        Long retryCount = stringRedisTemplate.opsForHash().increment(RETRY_KEY, id, 1L);
                        if (retryCount == null || retryCount > 3) {
                            stringRedisTemplate.opsForSet().remove(PENDING_KEY, id);
                            stringRedisTemplate.opsForHash().delete(RETRY_KEY, id);
                            log.error("城市门店缓存刷新连续失败，停止自动重试，门店ID={}", id);
                            retryExhausted = true;
                        }
                    }
                }
            } catch (Exception retryEx) {
                log.error("记录城市门店缓存刷新重试次数失败，任务仍保留在待刷新集合中", retryEx);
            }
            if (retryExhausted) {
                requestFullRebuild("单门店刷新连续失败补偿");
            }
        } finally {
            unlock(refreshLock, locked);
        }
    }

    /**
     * 写入一批门店缓存值及城市索引。
     */
    private void writeValues(String generation, Collection<StoreCityListCacheValue> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        Map<String, String> jsonMap = new HashMap<>();
        Map<String, List<String>> cityStoreIds = new HashMap<>();
        for (StoreCityListCacheValue value : values) {
            if (value == null || value.getStoreId() == null || !StringUtils.hasText(value.getCityName())) {
                log.warn("跳过无效的城市门店缓存值，门店ID={}，城市={}",
                        value == null ? null : value.getStoreId(), value == null ? null : value.getCityName());
                continue;
            }
            jsonMap.put(String.valueOf(value.getStoreId()), JsonUtils.toJsonString(value));
            cityStoreIds.computeIfAbsent(value.getCityName(), key -> new ArrayList<>())
                    .add(String.valueOf(value.getStoreId()));
        }
        if (jsonMap.isEmpty()) {
            return;
        }
        stringRedisTemplate.opsForHash().putAll(dataKey(generation), jsonMap);
        stringRedisTemplate.expire(dataKey(generation), GENERATION_TTL);
        for (Map.Entry<String, List<String>> entry : cityStoreIds.entrySet()) {
            String cityKey = cityKey(generation, entry.getKey());
            stringRedisTemplate.opsForSet().add(cityKey,
                    entry.getValue().toArray(String[]::new));
            stringRedisTemplate.expire(cityKey, GENERATION_TTL);
        }
    }

    /**
     * 更新城市完成标记中的门店数量，正确区分空城市与索引键丢失。
     */
    private void updateReadyCount(String generation, String cityName) {
        Long size = stringRedisTemplate.opsForSet().size(cityKey(generation, cityName));
        stringRedisTemplate.opsForHash().put(readyKey(generation), cityName,
                String.valueOf(size == null ? 0L : size));
        stringRedisTemplate.expire(readyKey(generation), GENERATION_TTL);
    }

    /**
     * 仅在城市已经完成过全量构建时更新数量，避免局部刷新被误标记为完整城市缓存。
     */
    private void updateReadyCountIfPresent(String generation, String cityName) {
        if (StringUtils.hasText(cityName)
                && Boolean.TRUE.equals(stringRedisTemplate.opsForHash().hasKey(readyKey(generation), cityName))) {
            updateReadyCount(generation, cityName);
        }
    }

    /**
     * 清理版本冲突批次写入的城市索引，任务保留后由下一轮重新加载最新数据。
     */
    private void cleanupConflictedRefresh(String generation, Collection<StoreCityListCacheValue> values) {
        Map<String, List<String>> cityStoreIds = values.stream()
                .filter(Objects::nonNull)
                .filter(value -> value.getStoreId() != null && StringUtils.hasText(value.getCityName()))
                .collect(Collectors.groupingBy(StoreCityListCacheValue::getCityName,
                        Collectors.mapping(value -> String.valueOf(value.getStoreId()), Collectors.toList())));
        for (Map.Entry<String, List<String>> entry : cityStoreIds.entrySet()) {
            stringRedisTemplate.opsForSet().remove(cityKey(generation, entry.getKey()), entry.getValue().toArray());
            stringRedisTemplate.opsForHash().delete(readyKey(generation), entry.getKey());
        }
    }

    /**
     * 写入完整城市缓存，空城市也写入完成标记。
     */
    private void writeCity(String generation, String cityName, Collection<StoreCityListCacheValue> values) {
        String cityKey = cityKey(generation, cityName);
        stringRedisTemplate.delete(cityKey);
        writeValues(generation, values);
        stringRedisTemplate.opsForHash().put(readyKey(generation), cityName,
                String.valueOf(values == null ? 0 : values.size()));
        stringRedisTemplate.expire(cityKey, GENERATION_TTL);
        stringRedisTemplate.expire(readyKey(generation), GENERATION_TTL);
        stringRedisTemplate.expire(dataKey(generation), GENERATION_TTL);
    }

    /**
     * 写入一个全新的缓存代数。
     */
    private void writeGeneration(String generation, Collection<StoreCityListCacheValue> values) {
        writeValues(generation, values);
        Map<String, List<StoreCityListCacheValue>> cityMap = values.stream()
                .collect(Collectors.groupingBy(StoreCityListCacheValue::getCityName));
        for (Map.Entry<String, List<StoreCityListCacheValue>> entry : cityMap.entrySet()) {
            stringRedisTemplate.opsForHash().put(readyKey(generation), entry.getKey(),
                    String.valueOf(entry.getValue().size()));
            stringRedisTemplate.expire(cityKey(generation, entry.getKey()), GENERATION_TTL);
        }
        stringRedisTemplate.expire(dataKey(generation), GENERATION_TTL);
        stringRedisTemplate.expire(readyKey(generation), GENERATION_TTL);
    }

    /**
     * 在版本一致时切换当前缓存代数。
     */
    private boolean switchGeneration(String expectedRevision, String generation) throws InterruptedException {
        RLock lock = redissonClient.getLock(REVISION_LOCK_KEY);
        boolean locked = false;
        try {
            locked = lock.tryLock(2, 10, TimeUnit.SECONDS);
            if (!locked || !Objects.equals(expectedRevision, currentRevision())) {
                return false;
            }
            stringRedisTemplate.opsForValue().set(ACTIVE_KEY, generation);
            return true;
        } finally {
            unlock(lock, locked);
        }
    }

    /**
     * 获取当前缓存代数；首次访问时创建启动代数。
     */
    private String getOrCreateActiveGeneration() {
        for (int i = 0; i < MAX_READ_RETRY; i++) {
            String generation = stringRedisTemplate.opsForValue().get(ACTIVE_KEY);
            if (StringUtils.hasText(generation)) {
                return generation;
            }
            String bootstrap = "bootstrap-" + System.currentTimeMillis();
            Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(ACTIVE_KEY, bootstrap);
            if (Boolean.TRUE.equals(created)) {
                return bootstrap;
            }
        }
        throw new IllegalStateException("城市门店缓存当前代数初始化失败");
    }

    /**
     * 短暂等待其他实例完成同一城市的回源，不永久阻塞请求。
     */
    private void waitForCityCache(String generation, String cityName) throws InterruptedException {
        for (int i = 0; i < CITY_CACHE_WAIT_RETRY; i++) {
            if (stringRedisTemplate.opsForHash().hasKey(readyKey(generation), cityName)) {
                return;
            }
            Thread.sleep(50L);
        }
    }

    /**
     * 短暂等待持锁实例补齐指定门店快照，等待次数固定且不会永久阻塞。
     */
    private void waitForStoreValues(String generation, Collection<Long> storeIds) throws InterruptedException {
        List<Object> fields = storeIds.stream().map(String::valueOf).collect(Collectors.toList());
        for (int i = 0; i < STORE_CACHE_WAIT_RETRY; i++) {
            List<Object> values = stringRedisTemplate.opsForHash().multiGet(dataKey(generation), fields);
            if (values != null && values.stream().allMatch(Objects::nonNull)) {
                return;
            }
            Thread.sleep(50L);
        }
    }

    /**
     * 获取当前数据修订版本。
     */
    private String currentRevision() {
        String revision = stringRedisTemplate.opsForValue().get(REVISION_KEY);
        if (revision != null) {
            return revision;
        }
        Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(REVISION_KEY, "0");
        if (Boolean.TRUE.equals(created)) {
            return "0";
        }
        revision = stringRedisTemplate.opsForValue().get(REVISION_KEY);
        if (revision == null) {
            throw new IllegalStateException("城市门店缓存版本初始化失败");
        }
        return revision;
    }

    /**
     * 在事务提交后将城市缓存操作投递到专用线程池，避免占用数据库事务线程。
     */
    private void afterCommitAsync(Runnable action, Collection<Long> fallbackStoreIds, String taskName) {
        Runnable dispatcher = () -> {
            if (!submitSafely(action, taskName)) {
                enqueuePendingWithoutExecutor(fallbackStoreIds, taskName);
            }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            dispatcher.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    dispatcher.run();
                }
            }
        });
    }

    /**
     * 专用线程池拒绝任务时只执行轻量 Redis 标记，不在业务线程查询数据库。
     *
     * <p>先递增版本并写入待刷新集合，再删除旧快照。即使删除旧快照失败，待刷新任务仍会在
     * 后续定时消费时覆盖旧值；Redis 本身异常时再使用有次数上限的全量补偿。</p>
     */
    private void enqueuePendingWithoutExecutor(Collection<Long> storeIds, String taskName) {
        if (storeIds == null || storeIds.isEmpty()) {
            return;
        }
        String[] fields = storeIds.stream().filter(Objects::nonNull).distinct()
                .map(String::valueOf).toArray(String[]::new);
        if (fields.length == 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().increment(REVISION_KEY);
            stringRedisTemplate.opsForSet().add(PENDING_KEY, fields);
            stringRedisTemplate.opsForHash().delete(RETRY_KEY, (Object[]) fields);
            String generation = stringRedisTemplate.opsForValue().get(ACTIVE_KEY);
            if (StringUtils.hasText(generation)) {
                stringRedisTemplate.opsForHash().delete(dataKey(generation), (Object[]) fields);
            }
            log.warn("城市门店缓存异步任务未完成，已写入 Redis 待刷新集合，任务={}，门店数量={}",
                    taskName, fields.length);
        } catch (Exception ex) {
            log.error("城市门店缓存异步任务的 Redis 兜底失败，将请求全量校准，任务={}，门店数量={}",
                    taskName, fields.length, ex);
            requestFullRebuild(taskName + "提交失败补偿");
        }
    }

    /**
     * 请求一次有次数上限的全量补偿校准。
     */
    private void requestFullRebuild(String reason) {
        fullRebuildRequestVersion.incrementAndGet();
        if (fullRebuildRequested.compareAndSet(false, true)) {
            fullRebuildRetryCount.set(0);
            log.warn("城市门店缓存已请求全量补偿校准，原因={}", reason);
        }
        startRequestedFullRebuild();
    }

    /**
     * 启动全量补偿任务，同一实例只允许一个任务运行且最多尝试三次。
     */
    private void startRequestedFullRebuild() {
        if (!fullRebuildRequested.get() || !fullRebuildWorkerRunning.compareAndSet(false, true)) {
            return;
        }
        if (fullRebuildRetryCount.get() >= 3) {
            fullRebuildRequested.set(false);
            fullRebuildWorkerRunning.set(false);
            log.error("城市门店缓存全量补偿连续失败三次，停止自动重试，等待下一次定时校准");
            return;
        }
        boolean submitted = submitSafely(() -> {
            long attemptRequestVersion = fullRebuildRequestVersion.get();
            int attempt = fullRebuildRetryCount.incrementAndGet();
            try {
                log.info("开始执行城市门店缓存全量补偿，第{}次", attempt);
                rebuildAll();
            } finally {
                fullRebuildWorkerRunning.set(false);
                if (fullRebuildRequested.get() && fullRebuildRetryCount.get() >= 3) {
                    if (attemptRequestVersion == fullRebuildRequestVersion.get()) {
                        fullRebuildRequested.set(false);
                        log.error("城市门店缓存全量补偿连续失败三次，停止自动重试，等待下一次定时校准");
                    } else {
                        fullRebuildRetryCount.set(0);
                    }
                }
            }
        }, "全量补偿校准");
        if (!submitted) {
            fullRebuildWorkerRunning.set(false);
        }
    }

    /**
     * 在事务真正提交后执行缓存操作，避免回滚事务污染缓存。
     */
    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    action.run();
                }
            }
        });
    }

    /**
     * 安全提交缓存任务；线程池拒绝时由待刷新集合或下次定时任务兜底。
     */
    private boolean submitSafely(Runnable task, String taskName) {
        try {
            cacheExecutor.execute(task);
            return true;
        } catch (Exception ex) {
            log.error("提交城市门店缓存任务失败，任务={}", taskName, ex);
            return false;
        }
    }

    /**
     * 安全释放当前线程持有的 Redis 锁。
     */
    private void unlock(RLock lock, boolean locked) {
        if (locked && lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
            } catch (Exception ex) {
                log.warn("释放城市门店缓存 Redis 锁失败，锁名称={}", lock.getName(), ex);
            }
        }
    }

    /**
     * 固定次数获取版本锁，缓存线程最多等待六秒，不会形成无限阻塞。
     */
    private boolean tryAcquireRevisionLock(RLock lock) throws InterruptedException {
        for (int i = 0; i < REVISION_LOCK_RETRY; i++) {
            if (lock.tryLock(2, 10, TimeUnit.SECONDS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找可选的门店背景缓存服务类型，保持当前分支与背景模块解耦。
     */
    private Class<?> findBackgroundCacheServiceClass() throws ClassNotFoundException {
        return ClassUtils.forName(
                "com.htyoudao.youdao.module.system.service.storebackground.StoreBackgroundCacheService",
                StoreCityListCacheService.class.getClassLoader());
    }

    /**
     * 背景缓存服务未装配时递增版本并将门店加入待重建集合。
     */
    private void enqueueBackgroundFallback(Collection<Long> storeIds) {
        try {
            String[] fields = storeIds.stream().filter(Objects::nonNull)
                    .map(String::valueOf).toArray(String[]::new);
            if (fields.length == 0) {
                return;
            }
            stringRedisTemplate.opsForValue().increment(BACKGROUND_REVISION_KEY);
            stringRedisTemplate.opsForSet().add(BACKGROUND_PENDING_KEY, fields);
        } catch (Exception ex) {
            log.error("门店背景缓存兜底刷新失败，门店数量={}", storeIds.size(), ex);
        }
    }

    /**
     * 背景缓存服务未装配时直接移除门店背景并递增版本。
     */
    private void removeBackgroundFallback(Long storeId) {
        try {
            stringRedisTemplate.opsForValue().increment(BACKGROUND_REVISION_KEY);
            stringRedisTemplate.opsForHash().delete(BACKGROUND_DATA_KEY, String.valueOf(storeId));
            stringRedisTemplate.opsForSet().remove(BACKGROUND_PENDING_KEY, String.valueOf(storeId));
        } catch (Exception ex) {
            log.error("删除门店背景缓存兜底处理失败，门店ID={}", storeId, ex);
        }
    }

    /**
     * 解析门店背景缓存中的图片地址。
     */
    @SuppressWarnings("unchecked")
    private String parseBackgroundImage(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            Map<String, Object> value = JsonUtils.parseObject(json, Map.class);
            Object image = value == null ? null : value.get("imageUrl");
            return image == null ? null : String.valueOf(image);
        } catch (Exception ex) {
            return json.startsWith("http") ? json : null;
        }
    }

    private String dataKey(String generation) {
        return "store:list:v1:{" + generation + "}:data";
    }

    private String readyKey(String generation) {
        return "store:list:v1:{" + generation + "}:city-ready";
    }

    private String cityKey(String generation, String cityName) {
        return "store:list:v1:{" + generation + "}:city:" + cityHash(cityName);
    }

    private String cityLockKey(String cityName) {
        return "store:list:v1:lock:city:" + cityHash(cityName);
    }

    private String cityHash(String cityName) {
        return DigestUtils.md5DigestAsHex(cityName.getBytes(StandardCharsets.UTF_8));
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
