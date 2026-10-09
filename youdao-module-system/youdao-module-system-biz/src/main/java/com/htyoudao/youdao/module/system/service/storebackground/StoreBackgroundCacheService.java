package com.htyoudao.youdao.module.system.service.storebackground;

import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundStoreDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundTemplateDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storebackground.StoreBackgroundStoreMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storebackground.StoreBackgroundTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storebackground.StoreBackgroundTemplateMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 门店背景缓存服务，负责缓存查询、失效处理和异步重建。
 */
@Service
@Slf4j
@DataPermission(enable = false)
public class StoreBackgroundCacheService {

    /** Redis Cluster Hash Tag，确保 Lua 脚本使用的多个 Key 位于同一槽位。 */
    private static final String CACHE_HASH_TAG = "{store:background}";
    public static final String DEFAULT_CACHE_KEY = CACHE_HASH_TAG + ":default";
    public static final String STORE_CACHE_KEY = CACHE_HASH_TAG + ":data";
    public static final String PENDING_CACHE_KEY = CACHE_HASH_TAG + ":rebuild:pending";
    public static final String REVISION_CACHE_KEY = CACHE_HASH_TAG + ":revision";

    private static final int REBUILD_BATCH_SIZE = 500;
    private static final int PUBLISH_STATUS_ENABLED = 1;
    private static final int DEFAULT_FLAG_CUSTOM = 0;
    private static final int DEFAULT_FLAG_SYSTEM = 1;
    private static final int APP_SCOPE_STORE = 1;
    private static final int APP_SCOPE_TAG = 2;
    private static final int STORE_SCOPE_ALL = 1;

    private static final DefaultRedisScript<Long> VERSIONED_BATCH_WRITE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end " +
                    "for i = 2, #ARGV, 2 do redis.call('HSET', KEYS[2], ARGV[i], ARGV[i + 1]) end " +
                    "return 1", Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private StoreBackgroundTemplateMapper templateMapper;
    @Resource
    private StoreBackgroundStoreMapper backgroundStoreMapper;
    @Resource
    private StoreBackgroundTagMapper backgroundTagMapper;
    @Resource
    private SystemStoreInfoMapper storeInfoMapper;
    @Resource
    private SystemStoreTagMapper storeTagMapper;

    private final Executor cacheExecutor;
    private final AtomicBoolean workerRunning = new AtomicBoolean(false);

    /**
     * 注入门店背景缓存专用执行器。
     *
     * @param cacheExecutor 缓存重建执行器
     */
    public StoreBackgroundCacheService(
            @Qualifier("storeBackgroundCacheExecutor") Executor cacheExecutor) {
        this.cacheExecutor = cacheExecutor;
    }

    /**
     * 应用启动后加载系统默认模板，并将全部门店加入异步重建队列。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeCache() {
        try {
            stringRedisTemplate.opsForValue().setIfAbsent(REVISION_CACHE_KEY, "0");
            StoreBackgroundTemplateDO defaultTemplate = getDefaultTemplate();
            writeDefaultCache(defaultTemplate);
            enqueueStoreIds(selectAllStoreIds());
            log.info("门店背景缓存初始化完成，已加载默认模板并提交全量门店重建");
        } catch (Exception ex) {
            // 数据库脚本尚未执行时不能影响系统模块启动。
            log.error("门店背景缓存初始化失败，请检查门店背景表和默认模板数据", ex);
        }
    }

    /**
     * 获取单个门店的背景图片。缓存未命中时立即返回默认图片，并异步重建该门店缓存。
     *
     * @param storeId 门店编号
     * @return 背景图片地址
     */
    public String getBackgroundImage(Long storeId) {
        if (storeId == null) {
            return getDefaultImage();
        }
        Object cached = stringRedisTemplate.opsForHash().get(STORE_CACHE_KEY, storeId.toString());
        if (cached != null) {
            return parseImage(cached.toString());
        }
        log.info("门店背景缓存未命中，立即返回默认模板并异步重建，门店编号={}", storeId);
        enqueueStoreIds(Collections.singleton(storeId));
        return getDefaultImage();
    }

    /**
     * 批量获取门店背景图片，通过一次 Redis 批量读取避免逐门店查询。
     * 缓存未命中的门店先返回默认图片，并加入异步重建队列。
     *
     * @param storeIds 门店编号集合
     * @return 门店编号与背景图片的对应关系
     */
    public Map<Long, String> getBackgroundImages(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> normalizedIds = storeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object> fields = normalizedIds.stream().map(String::valueOf)
                .map(value -> (Object) value).toList();
        List<Object> cachedValues = stringRedisTemplate.opsForHash().multiGet(STORE_CACHE_KEY, fields);
        String defaultImage = getDefaultImage();
        Map<Long, String> result = new HashMap<>(normalizedIds.size());
        List<Long> missingIds = new ArrayList<>();
        for (int i = 0; i < normalizedIds.size(); i++) {
            Object cached = cachedValues.get(i);
            if (cached == null) {
                result.put(normalizedIds.get(i), defaultImage);
                missingIds.add(normalizedIds.get(i));
            } else {
                result.put(normalizedIds.get(i), parseImage(cached.toString()));
            }
        }
        if (!missingIds.isEmpty()) {
            log.info("门店背景缓存批量查询存在未命中，总数={}，命中数={}，未命中数={}，未命中门店样例={}",
                    normalizedIds.size(), normalizedIds.size() - missingIds.size(), missingIds.size(),
                    sampleStoreIds(missingIds));
        } else {
            log.debug("门店背景缓存批量查询全部命中，门店数量={}", normalizedIds.size());
        }
        enqueueStoreIds(missingIds);
        return result;
    }

    /**
     * 根据模板应用范围计算其影响的全部门店。
     *
     * @param template 模板数据
     * @return 受影响门店编号集合
     */
    public Set<Long> resolveAffectedStoreIds(StoreBackgroundTemplateDO template) {
        if (template == null || Objects.equals(template.getDefaultFlag(), DEFAULT_FLAG_SYSTEM)) {
            return new HashSet<>(selectAllStoreIds());
        }
        if (Objects.equals(template.getAppScope(), APP_SCOPE_STORE)) {
            if (Objects.equals(template.getStoreScope(), STORE_SCOPE_ALL)) {
                Set<Long> storeIds = new HashSet<>(selectStoreIdsByBusinessId(template.getBusinessId()));
                log.info("全部门店背景模板计算影响范围完成，模板编号={}，项目编号={}，门店数量={}，门店样例={}",
                        template.getBackgroundId(), template.getBusinessId(), storeIds.size(), sampleStoreIds(storeIds));
                return storeIds;
            }
            return backgroundStoreMapper.selectList(new LambdaQueryWrapperX<StoreBackgroundStoreDO>()
                            .eq(StoreBackgroundStoreDO::getBackgroundId, template.getBackgroundId()))
                    .stream().map(StoreBackgroundStoreDO::getStoreId).filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        if (Objects.equals(template.getAppScope(), APP_SCOPE_TAG)) {
            List<Long> tagIds = backgroundTagMapper.selectList(new LambdaQueryWrapperX<StoreBackgroundTagDO>()
                            .eq(StoreBackgroundTagDO::getBackgroundId, template.getBackgroundId()))
                    .stream().map(StoreBackgroundTagDO::getTagId).filter(Objects::nonNull).distinct().toList();
            if (tagIds.isEmpty()) {
                return Collections.emptySet();
            }
            return storeTagMapper.selectList(new LambdaQueryWrapperX<SystemStoreTagDO>()
                            .in(SystemStoreTagDO::getTagId, tagIds))
                    .stream().map(SystemStoreTagDO::getStoreId).filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * 在当前事务提交后清除并重建指定门店缓存。
     *
     * @param storeIds 门店编号集合
     */
    public void refreshStoresAfterCommit(Collection<Long> storeIds) {
        afterCommit(() -> invalidateAndEnqueue(storeIds));
    }

    /**
     * 在当前事务提交后清除全部门店缓存并触发全量重建。
     */
    public void refreshAllAfterCommit() {
        afterCommit(() -> {
            incrementRevision();
            stringRedisTemplate.delete(STORE_CACHE_KEY);
            enqueueStoreIds(selectAllStoreIds());
        });
    }

    /**
     * 在当前事务提交后更新默认模板缓存，并异步刷新使用默认模板的门店。
     * 不清空门店缓存，避免已命中自定义模板的门店短暂回退到默认模板。
     *
     * @param template 系统默认模板
     */
    public void updateDefaultAfterCommit(StoreBackgroundTemplateDO template) {
        afterCommit(() -> {
            incrementRevision();
            writeDefaultCache(template);
            enqueueStoreIds(selectAllStoreIds());
        });
    }

    /**
     * 在当前事务提交后删除指定门店的背景缓存及待重建记录。
     *
     * @param storeId 门店编号
     */
    public void removeStoreAfterCommit(Long storeId) {
        if (storeId == null) {
            return;
        }
        afterCommit(() -> {
            incrementRevision();
            stringRedisTemplate.opsForHash().delete(STORE_CACHE_KEY, storeId.toString());
            stringRedisTemplate.opsForSet().remove(PENDING_CACHE_KEY, storeId.toString());
        });
    }

    /**
     * 清除指定门店缓存、递增版本号并加入重建队列。
     *
     * @param storeIds 门店编号集合
     */
    private void invalidateAndEnqueue(Collection<Long> storeIds) {
        List<Long> ids = normalizeIds(storeIds);
        if (ids.isEmpty()) {
            return;
        }
        incrementRevision();
        stringRedisTemplate.opsForHash().delete(STORE_CACHE_KEY,
                ids.stream().map(String::valueOf).toArray());
        enqueueStoreIds(ids);
        log.info("门店背景缓存已失效并进入重建队列，门店数量={}", ids.size());
    }

    /**
     * 将门店编号写入 Redis 待重建集合，并尝试启动后台工作线程。
     *
     * @param storeIds 门店编号集合
     */
    private void enqueueStoreIds(Collection<Long> storeIds) {
        List<Long> ids = normalizeIds(storeIds);
        if (!ids.isEmpty()) {
            String[] values = ids.stream().map(String::valueOf).toArray(String[]::new);
            Long addedCount = stringRedisTemplate.opsForSet().add(PENDING_CACHE_KEY, values);
            Long pendingCount = stringRedisTemplate.opsForSet().size(PENDING_CACHE_KEY);
            log.info("门店背景缓存异步重建已入队，请求门店数={}，新增门店数={}，当前待处理数={}，门店样例={}",
                    ids.size(), Objects.requireNonNullElse(addedCount, 0L),
                    Objects.requireNonNullElse(pendingCount, 0L), sampleStoreIds(ids));
        }
        startWorker();
    }

    /**
     * 启动唯一的本地缓存重建工作线程，避免同一实例重复消费队列。
     */
    private void startWorker() {
        if (!workerRunning.compareAndSet(false, true)) {
            log.debug("门店背景缓存重建线程正在运行，本次不重复启动");
            return;
        }
        try {
            cacheExecutor.execute(this::drainPendingStores);
            log.info("门店背景缓存重建线程已提交执行");
        } catch (RejectedExecutionException ex) {
            workerRunning.set(false);
            Long pendingCount = stringRedisTemplate.opsForSet().size(PENDING_CACHE_KEY);
            log.warn("门店背景缓存重建线程池繁忙，待重建门店将由后续请求继续处理，当前待处理数={}",
                    Objects.requireNonNullElse(pendingCount, 0L));
        }
    }

    /**
     * 分批消费 Redis 待重建门店集合，异常时将当前批次重新放回队列。
     */
    private void drainPendingStores() {
        boolean failed = false;
        int processedBatchCount = 0;
        int processedStoreCount = 0;
        log.info("门店背景缓存异步重建线程开始消费任务");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                List<String> members = stringRedisTemplate.opsForSet()
                        .pop(PENDING_CACHE_KEY, REBUILD_BATCH_SIZE);
                if (members == null || members.isEmpty()) {
                    break;
                }
                List<Long> storeIds = members.stream().map(Long::valueOf).distinct().toList();
                processedBatchCount++;
                processedStoreCount += storeIds.size();
                log.info("门店背景缓存开始处理批次，批次门店数={}，门店样例={}",
                        storeIds.size(), sampleStoreIds(storeIds));
                try {
                    DataPermissionUtils.executeIgnore(() -> rebuildBatch(storeIds));
                } catch (Exception ex) {
                    failed = true;
                    stringRedisTemplate.opsForSet().add(PENDING_CACHE_KEY,
                            members.toArray(String[]::new));
                    log.error("门店背景缓存批量重建失败，门店数量={}", storeIds.size(), ex);
                    break;
                }
            }
        } finally {
            workerRunning.set(false);
            Long pendingSize = stringRedisTemplate.opsForSet().size(PENDING_CACHE_KEY);
            log.info("门店背景缓存异步重建线程结束，累计批次数={}，累计处理门店数={}，是否异常={}，是否被中断={}，剩余待处理数={}",
                    processedBatchCount, processedStoreCount, failed, Thread.currentThread().isInterrupted(),
                    Objects.requireNonNullElse(pendingSize, 0L));
            if (!failed && pendingSize != null && pendingSize > 0) {
                log.info("门店背景缓存仍有待处理任务，准备继续启动重建线程，剩余待处理数={}", pendingSize);
                startWorker();
            }
        }
    }

    /**
     * 批量计算门店最终命中的最新模板，没有自定义模板时使用系统默认模板。
     *
     * @param requestedStoreIds 待重建门店编号
     */
    private void rebuildBatch(List<Long> requestedStoreIds) {
        String revision = currentRevision();
        log.info("门店背景缓存批次开始重建，缓存版本={}，请求门店数={}，门店样例={}",
                revision, requestedStoreIds.size(), sampleStoreIds(requestedStoreIds));
        List<SystemStoreInfoDO> validStores = storeInfoMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getBusinessId)
                        .in(SystemStoreInfoDO::getStoreId, requestedStoreIds));
        List<Long> validStoreIds = validStores.stream().map(SystemStoreInfoDO::getStoreId).toList();
        if (validStoreIds.isEmpty()) {
            log.warn("门店背景缓存批次没有查询到有效门店，缓存版本={}，请求门店数={}，门店样例={}",
                    revision, requestedStoreIds.size(), sampleStoreIds(requestedStoreIds));
            return;
        }
        Map<Long, Long> businessIdByStore = new HashMap<>();
        validStores.forEach(store -> businessIdByStore.put(store.getStoreId(), store.getBusinessId()));

        List<StoreBackgroundTemplateDO> templates = templateMapper.selectList(
                new LambdaQueryWrapperX<StoreBackgroundTemplateDO>()
                        .eq(StoreBackgroundTemplateDO::getPublishStatus, PUBLISH_STATUS_ENABLED)
                        .eq(StoreBackgroundTemplateDO::getDefaultFlag, DEFAULT_FLAG_CUSTOM));
        StoreBackgroundTemplateDO defaultTemplate = getDefaultTemplate();

        Set<Long> templateIds = templates.stream().map(StoreBackgroundTemplateDO::getBackgroundId)
                .collect(Collectors.toSet());
        Map<Long, Set<Long>> directTemplateIdsByStore = new HashMap<>();
        Map<Long, Set<Long>> templateIdsByTag = new HashMap<>();
        if (!templateIds.isEmpty()) {
            backgroundStoreMapper.selectList(new LambdaQueryWrapperX<StoreBackgroundStoreDO>()
                            .in(StoreBackgroundStoreDO::getBackgroundId, templateIds)
                            .in(StoreBackgroundStoreDO::getStoreId, validStoreIds))
                    .forEach(item -> directTemplateIdsByStore
                            .computeIfAbsent(item.getStoreId(), key -> new HashSet<>())
                            .add(item.getBackgroundId()));
            backgroundTagMapper.selectList(new LambdaQueryWrapperX<StoreBackgroundTagDO>()
                            .in(StoreBackgroundTagDO::getBackgroundId, templateIds))
                    .forEach(item -> templateIdsByTag
                            .computeIfAbsent(item.getTagId(), key -> new HashSet<>())
                            .add(item.getBackgroundId()));
        }

        Map<Long, Set<Long>> tagIdsByStore = new HashMap<>();
        List<SystemStoreTagDO> storeTagRelations = storeTagMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreTagDO>()
                        .in(SystemStoreTagDO::getStoreId, validStoreIds));
        storeTagRelations.forEach(item -> tagIdsByStore
                .computeIfAbsent(item.getStoreId(), key -> new HashSet<>())
                .add(item.getTagId()));
        log.info("门店背景缓存批次标签关系加载完成，有效门店数={}，门店标签关系数={}，有标签门店数={}",
                validStoreIds.size(), storeTagRelations.size(), tagIdsByStore.size());

        Map<Long, StoreBackgroundCacheValue> values = new LinkedHashMap<>();
        int specificTemplateCount = 0;
        int allStoreTemplateCount = 0;
        int defaultTemplateCount = 0;
        for (Long storeId : validStoreIds) {
            Set<Long> candidateIds = new HashSet<>(directTemplateIdsByStore
                    .getOrDefault(storeId, Collections.emptySet()));
            for (Long tagId : tagIdsByStore.getOrDefault(storeId, Collections.emptySet())) {
                candidateIds.addAll(templateIdsByTag.getOrDefault(tagId, Collections.emptySet()));
            }
            StoreBackgroundTemplateDO selected = selectTemplateForStore(templates, candidateIds,
                    businessIdByStore.get(storeId), defaultTemplate);
            if (Objects.equals(selected.getDefaultFlag(), DEFAULT_FLAG_SYSTEM)) {
                defaultTemplateCount++;
            } else if (isAllStoreTemplate(selected)) {
                allStoreTemplateCount++;
            } else {
                specificTemplateCount++;
            }
            values.put(storeId, toCacheValue(selected));
        }
        writeBatchIfRevisionMatches(revision, values);
        log.info("门店背景缓存重建完成，请求门店数={}，有效门店数={}，指定门店或标签模板命中={}，全部门店模板命中={}，默认模板命中={}",
                requestedStoreIds.size(), validStoreIds.size(), specificTemplateCount,
                allStoreTemplateCount, defaultTemplateCount);
    }

    /**
     * 选择门店最终使用的模板。指定门店和标签模板优先于全部门店模板，
     * 同一优先级再按发布时间和模板编号选择最新记录。
     *
     * @param templates 已发布的自定义模板
     * @param candidateIds 门店通过直接关系或标签关系命中的模板编号
     * @param businessId 门店所属项目编号
     * @param defaultTemplate 系统默认模板
     * @return 门店最终使用的模板
     */
    StoreBackgroundTemplateDO selectTemplateForStore(List<StoreBackgroundTemplateDO> templates,
                                                       Set<Long> candidateIds,
                                                       Long businessId,
                                                       StoreBackgroundTemplateDO defaultTemplate) {
        Comparator<StoreBackgroundTemplateDO> comparator = Comparator
                .comparingInt((StoreBackgroundTemplateDO template) -> isAllStoreTemplate(template) ? 0 : 1)
                .thenComparing(StoreBackgroundTemplateDO::getReleaseTime,
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(StoreBackgroundTemplateDO::getBackgroundId);
        return templates.stream()
                .filter(template -> Objects.equals(template.getBusinessId(), businessId))
                .filter(template -> isAllStoreTemplate(template)
                        || candidateIds.contains(template.getBackgroundId()))
                .max(comparator).orElse(defaultTemplate);
    }

    /**
     * 仅在缓存版本未变化时批量写入门店模板，防止旧任务覆盖新发布结果。
     *
     * @param revision 任务开始时的缓存版本
     * @param values 门店最终模板
     */
    private void writeBatchIfRevisionMatches(String revision,
                                               Map<Long, StoreBackgroundCacheValue> values) {
        List<String> args = new ArrayList<>(values.size() * 2 + 1);
        args.add(revision);
        values.forEach((storeId, value) -> {
            args.add(storeId.toString());
            args.add(JsonUtils.toJsonString(value));
        });
        Long result = stringRedisTemplate.execute(VERSIONED_BATCH_WRITE_SCRIPT,
                Arrays.asList(REVISION_CACHE_KEY, STORE_CACHE_KEY), args.toArray());
        if (!Objects.equals(result, 1L)) {
            String[] storeIds = values.keySet().stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(PENDING_CACHE_KEY, storeIds);
            log.warn("门店背景缓存版本已变化，本批次放弃写入并重新排队，任务版本={}，当前版本={}，门店数量={}，门店样例={}",
                    revision, currentRevision(), values.size(), sampleStoreIds(values.keySet()));
            return;
        }
        log.info("门店背景缓存批次写入 Redis 成功，缓存版本={}，门店数量={}，门店样例={}",
                revision, values.size(), sampleStoreIds(values.keySet()));
    }

    /**
     * 判断模板是否直接适用于全部门店。
     *
     * @param template 模板数据
     * @return 是否为全部门店模板
     */
    private boolean isAllStoreTemplate(StoreBackgroundTemplateDO template) {
        return Objects.equals(template.getAppScope(), APP_SCOPE_STORE)
                && Objects.equals(template.getStoreScope(), STORE_SCOPE_ALL);
    }

    /**
     * 查询系统默认模板，默认模板缺失时终止缓存构建。
     *
     * @return 系统默认模板
     */
    private StoreBackgroundTemplateDO getDefaultTemplate() {
        StoreBackgroundTemplateDO template = templateMapper.selectOne(
                new LambdaQueryWrapperX<StoreBackgroundTemplateDO>()
                        .eq(StoreBackgroundTemplateDO::getDefaultFlag, DEFAULT_FLAG_SYSTEM), false);
        if (template == null) {
            throw new IllegalStateException("系统默认门店背景模板不存在");
        }
        return template;
    }

    /**
     * 将系统默认模板以 JSON 字符串写入 Redis。
     *
     * @param template 系统默认模板
     */
    private void writeDefaultCache(StoreBackgroundTemplateDO template) {
        stringRedisTemplate.opsForValue().set(DEFAULT_CACHE_KEY,
                JsonUtils.toJsonString(toCacheValue(template)));
    }

    /**
     * 将模板数据转换为缓存对象。
     *
     * @param template 模板数据
     * @return 缓存对象
     */
    private StoreBackgroundCacheValue toCacheValue(StoreBackgroundTemplateDO template) {
        return new StoreBackgroundCacheValue(template.getBackgroundId(), template.getBackgroundImage(),
                template.getReleaseTime(), Objects.equals(template.getDefaultFlag(), DEFAULT_FLAG_SYSTEM));
    }

    /**
     * 从 Redis 获取系统默认背景图片。
     *
     * @return 默认背景图片地址
     */
    private String getDefaultImage() {
        return parseImage(stringRedisTemplate.opsForValue().get(DEFAULT_CACHE_KEY));
    }

    /**
     * 解析缓存 JSON 并取得背景图片地址。
     *
     * @param json 缓存 JSON
     * @return 背景图片地址
     */
    private String parseImage(String json) {
        StoreBackgroundCacheValue value = JsonUtils.parseObject(json, StoreBackgroundCacheValue.class);
        return value == null ? null : value.getImageUrl();
    }

    /**
     * 获取当前缓存版本，版本不存在时初始化为零。
     *
     * @return 当前缓存版本
     */
    private String currentRevision() {
        String revision = stringRedisTemplate.opsForValue().get(REVISION_CACHE_KEY);
        if (revision != null) {
            return revision;
        }
        stringRedisTemplate.opsForValue().setIfAbsent(REVISION_CACHE_KEY, "0");
        return Objects.requireNonNullElse(stringRedisTemplate.opsForValue().get(REVISION_CACHE_KEY), "0");
    }

    /**
     * 递增缓存版本，使正在执行的旧重建任务放弃写入。
     */
    private void incrementRevision() {
        stringRedisTemplate.opsForValue().increment(REVISION_CACHE_KEY);
    }

    /**
     * 查询全部有效门店编号，用于系统默认模板或启动时全量缓存重建。
     *
     * @return 门店编号列表
     */
    private List<Long> selectAllStoreIds() {
        return storeInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .select(SystemStoreInfoDO::getStoreId))
                .stream().map(SystemStoreInfoDO::getStoreId).filter(Objects::nonNull).toList();
    }

    /**
     * 查询模板所属项目的全部有效门店，确保“全部门店”不会跨项目生效。
     *
     * @param businessId 模板所属项目编号
     * @return 项目下的门店编号列表
     */
    private List<Long> selectStoreIdsByBusinessId(Long businessId) {
        return storeInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .select(SystemStoreInfoDO::getStoreId)
                        .eq(businessId != null, SystemStoreInfoDO::getBusinessId, businessId))
                .stream().map(SystemStoreInfoDO::getStoreId).filter(Objects::nonNull).toList();
    }

    /**
     * 过滤空门店编号并去重。
     *
     * @param storeIds 门店编号集合
     * @return 规范化后的门店编号列表
     */
    private List<Long> normalizeIds(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return storeIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * 截取少量门店编号用于异步重建日志，避免全量任务输出过多内容。
     *
     * @param storeIds 门店编号集合
     * @return 最多十个门店编号
     */
    private List<Long> sampleStoreIds(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return storeIds.stream().filter(Objects::nonNull).limit(10).toList();
    }

    /**
     * 将缓存操作延迟到事务提交后执行；无事务时直接执行。
     *
     * @param action 待执行缓存操作
     */
    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
