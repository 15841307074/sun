package com.htyoudao.youdao.module.promotion.api.activity;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillRespDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityStoreTagUpdateDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryQuery;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryResult;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.SettlementActivitiesDTO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelNameDataRespVO;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStore.ActivityStoreMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzGiftInventoryTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityChannel.ActivityChannelMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activity.ApplicableActivityQueryService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannelName.ActivityChannelNameService;
import com.htyoudao.youdao.module.promotion.service.activityMj.ActivityMJAppService;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMZAppService;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMzCacheService;
import com.htyoudao.youdao.module.promotion.service.activityMz.ActivityMzOrderService;
import com.htyoudao.youdao.module.promotion.service.activityNjnz.ActivityNjnzAppService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillApiService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryScopeService;
import com.htyoudao.youdao.module.promotion.util.ImageValueParseUtil;
import jakarta.annotation.Resource;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@DubboService
@Validated
@Slf4j
public class ActivityApiImpl implements ActivityApi {
    @Resource
    private LotteryScopeService lotteryScopeService;

    private static final Cache<SettlementActivitiesCacheKey, SettlementActivitiesDTO> SETTLEMENT_ACTIVITIES_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(1, TimeUnit.MINUTES)
                    .build();
    private static final Cache<ActivityQueryCacheKey, Map<Long, List<ActivityNjnzDTO>>> NJNZ_ACTIVITY_CACHE =
            buildActivityQueryCache();
    private static final Cache<ActivityQueryCacheKey, Map<Long, List<ActivityMJDTO>>> MJ_ACTIVITY_CACHE =
            buildActivityQueryCache();
    private static final Cache<ActivityQueryCacheKey, Map<Long, List<ActivityMzDTO>>> MZ_ACTIVITY_CACHE =
            buildActivityQueryCache();

    private static <V> Cache<ActivityQueryCacheKey, V> buildActivityQueryCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Resource
    private ActivityNjnzAppService activityNjnzAppService;

    @Resource
    private SeckillApiService seckillApiService;

    @Resource
    private ActivityMJAppService activityMJAppService;

    @Resource
    private ActivityMZAppService activityMZAppService;

    @Resource
    private ActivityMzCacheService activityMzCacheService;
    @Resource
    private ActivityMzOrderService activityMzOrderService;

    @Resource
    private ActivityChannelMapper activityChannelMapper;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityAppService activityAppService;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ApplicableActivityQueryService applicableActivityQueryService;

    @Resource
    private ActivityChannelNameService activityChannelNameService;

    @SentinelResource(value = "selectSettlementActivities", fallback = "selectSettlementActivitiesFallback",
            blockHandler = "selectSettlementActivitiesExceptionHandler")
    @Override
    public CommonResult<SettlementActivitiesDTO> selectSettlementActivities(Long storeId,
                                                                              Collection<Long> commodityIds) {
        if (CollectionUtils.isEmpty(commodityIds)) {
            return CommonResult.success(emptySettlementActivities());
        }
        List<Long> sortedCommodityIds = commodityIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        if (sortedCommodityIds.isEmpty()) {
            return CommonResult.success(emptySettlementActivities());
        }
        SettlementActivitiesCacheKey cacheKey = new SettlementActivitiesCacheKey(
                BusinessContextHolder.getRequiredBusinessId(), storeId, sortedCommodityIds);
        try {
            SettlementActivitiesDTO result = SETTLEMENT_ACTIVITIES_CACHE.get(cacheKey,
                    () -> loadSettlementActivities(storeId, new LinkedHashSet<>(sortedCommodityIds)));
            return CommonResult.success(result);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("结算活动缓存加载失败", cause);
        }
    }

    private SettlementActivitiesDTO loadSettlementActivities(Long storeId, Set<Long> commodityIdSet) {
        List<ActivityDO> activities = applicableActivityQueryService.selectApplicable(storeId, List.of(
                com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum.NJ_NZ.getCode(),
                com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum.MJ.getCode(),
                com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum.MZ.getCode()));
        Map<Integer, List<ActivityDO>> byType = activities.stream()
                .collect(Collectors.groupingBy(ActivityDO::getActivityType));

        SettlementActivitiesDTO result = new SettlementActivitiesDTO();
        try {
            result.setNjnzActivities(activityNjnzAppService.selectNjnzActivity(commodityIdSet,
                    byType.getOrDefault(com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum.NJ_NZ.getCode(), List.of())));
        } catch (Exception ex) {
            log.warn("批量查询N件N折活动失败，单独降级 storeId={} commodityIdsSize={}",
                    storeId, commodityIdSet.size(), ex);
            result.setNjnzActivities(Map.of());
        }
        try {
            result.setMjActivities(activityMJAppService.selectMJActivity(commodityIdSet,
                    byType.getOrDefault(com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum.MJ.getCode(), List.of())));
        } catch (Exception ex) {
            log.warn("批量查询满减满折活动失败，单独降级 storeId={} commodityIdsSize={}",
                    storeId, commodityIdSet.size(), ex);
            result.setMjActivities(Map.of());
        }
        try {
            result.setMzActivities(activityMZAppService.selectMZActivity(storeId, commodityIdSet,
                    byType.getOrDefault(com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum.MZ.getCode(), List.of())));
        } catch (Exception ex) {
            log.warn("批量查询满赠活动失败，单独降级 storeId={} commodityIdsSize={}",
                    storeId, commodityIdSet.size(), ex);
            result.setMzActivities(Map.of());
        }
        return result;
    }

    private record SettlementActivitiesCacheKey(Long businessId, Long storeId, List<Long> commodityIds) {}

    private record ActivityQueryCacheKey(Long businessId, Long storeId, List<Long> commodityIds) {}

    private SettlementActivitiesDTO emptySettlementActivities() {
        SettlementActivitiesDTO result = new SettlementActivitiesDTO();
        result.setNjnzActivities(Map.of());
        result.setMjActivities(Map.of());
        result.setMzActivities(Map.of());
        return result;
    }


    @SentinelResource(value = "selectNjnzActivity", fallback = "selectNjnzActivityFallback", blockHandler = "selectNjnzActivityExceptionHandler")
    @Override
    public CommonResult<Map<Long, List<ActivityNjnzDTO>>> selectNjnzActivity(Long storeId,
                                                                             Collection<Long> commodityIds) {
        ActivityQueryCacheKey cacheKey = buildActivityQueryCacheKey(storeId, commodityIds);
        if (cacheKey == null) {
            return CommonResult.success(Map.of());
        }
        Map<Long, List<ActivityNjnzDTO>> result = getCachedActivity("NJNZ", NJNZ_ACTIVITY_CACHE, cacheKey,
                () -> activityNjnzAppService.selectNjnzActivity(storeId, cacheKey.commodityIds()));
        return CommonResult.success(result);
    }

    @SentinelResource(value = "selectMJActivity", fallback = "selectMJActivityFallback", blockHandler = "selectMJActivityExceptionHandler")
    @Override
    public CommonResult<Map<Long, List<ActivityMJDTO>>> selectMJActivity(Long storeId,
                                                                         Collection<Long> commodityIds) {
        ActivityQueryCacheKey cacheKey = buildActivityQueryCacheKey(storeId, commodityIds);
        if (cacheKey == null) {
            return CommonResult.success(Map.of());
        }
        Map<Long, List<ActivityMJDTO>> result = getCachedActivity("MJ", MJ_ACTIVITY_CACHE, cacheKey,
                () -> activityMJAppService.selectMJActivity(storeId, cacheKey.commodityIds()));
        return CommonResult.success(result);
    }

    @SentinelResource(value = "selectMZActivity", fallback = "selectMZActivityFallback", blockHandler = "selectMZActivityExceptionHandler")
    @Override
    public CommonResult<Map<Long, List<ActivityMzDTO>>> selectMZActivity(Long storeId,
                                                                         Collection<Long> commodityIds) {
        ActivityQueryCacheKey cacheKey = buildActivityQueryCacheKey(storeId, commodityIds);
        if (cacheKey == null) {
            return CommonResult.success(Map.of());
        }
        Map<Long, List<ActivityMzDTO>> result = getCachedActivity("MZ", MZ_ACTIVITY_CACHE, cacheKey,
                () -> activityMZAppService.selectMZActivity(storeId, cacheKey.commodityIds()));
        return CommonResult.success(result);
    }

    /**
     * 使用 Cache.get 加载，缓存命中时只读不写，避免 asMap().computeIfAbsent 在 Guava 中通过
     * compute 更新条目写入时间，导致高频访问不断续期、expireAfterWrite 无法按期失效。
     */
    private <V> Map<Long, List<V>> getCachedActivity(String activityType,
                                                      Cache<ActivityQueryCacheKey, Map<Long, List<V>>> cache,
                                                      ActivityQueryCacheKey cacheKey,
                                                      Supplier<Map<Long, List<V>>> loader) {
        try {
            return cache.get(cacheKey, loader::get);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("活动查询缓存加载失败 type=" + activityType, cause);
        }
    }

    private ActivityQueryCacheKey buildActivityQueryCacheKey(Long storeId, Collection<Long> commodityIds) {
        if (CollectionUtils.isEmpty(commodityIds)) {
            return null;
        }
        List<Long> sortedCommodityIds = commodityIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        if (sortedCommodityIds.isEmpty()) {
            return null;
        }
        return new ActivityQueryCacheKey(BusinessContextHolder.getRequiredBusinessId(), storeId, sortedCommodityIds);
    }

    @Override
    public CommonResult<Integer> queryMzGiftInventory(Long activityId, Long storeId, Long giftCommodityId) {
        ActivityMzCacheService.ActivityMzMeta meta = activityMzCacheService.getActivityMetaPublic(activityId);
        if (meta == null) {
            return CommonResult.success(null);
        }
        Integer inventory = Integer.valueOf(MzGiftInventoryTypeEnum.INDEPENDENT.getCode()).equals(meta.getGiftInventoryType())
                ? activityMzCacheService.queryStoreInventory(activityId, storeId, giftCommodityId)
                : activityMzCacheService.querySharedInventory(activityId, giftCommodityId);
        return CommonResult.success(inventory);
    }

    @Override
    public CommonResult<List<MzGiftInventoryResult>> queryMzGiftInventories(List<MzGiftInventoryQuery> queries) {
        return CommonResult.success(activityMzCacheService.queryGiftInventories(queries));
    }

    @Override
    public CommonResult<Boolean> checkMzCanParticipate(Long activityId, Long memberId) {
        return CommonResult.success(activityMzOrderService.canParticipate(activityId, memberId));
    }

    @Override
    public CommonResult<Integer> lockMzGiftInventory(Long activityId, Long storeId, Long memberId, String orderSn,
                                                     Long giftCommodityId, Long giftSkuId, Integer quantity, Boolean checkUserLimit) {
        return CommonResult.success(activityMzOrderService.lock(activityId, storeId, memberId, orderSn,
                giftCommodityId, giftSkuId, quantity, Boolean.TRUE.equals(checkUserLimit)));
    }

    @Override
    public CommonResult<Boolean> confirmMzGiftInventory(String orderSn) {
        return CommonResult.success(activityMzOrderService.confirm(orderSn));
    }

    @Override
    public CommonResult<Boolean> releaseMzGiftInventory(String orderSn) {
        return CommonResult.success(activityMzOrderService.release(orderSn));
    }

    @Override
    public CommonResult<Boolean> refundMzGiftInventory(String orderSn) {
        return CommonResult.success(activityMzOrderService.refund(orderSn));
    }

    @Override
    public CommonResult<ActivitySeckillRespDTO> selectSeckillActivity(Long activityId) {
        return CommonResult.success(seckillApiService.getActivity(activityId));
    }

    @Override
    public CommonResult<Map<Long, Integer>> seckillStock(Long storeId, Long activityId, Integer time,
                                                         Collection<Long> commodityIds) {
        return CommonResult.success(seckillApiService.seckillStock(storeId, activityId, time, commodityIds));
    }


    @Override
    public CommonResult<Boolean> checkCanJoin(Long activityId) {
        return CommonResult.success(activityAppService.checkCanJoin(activityId));
    }

    @Override
    public CommonResult<String> selectChannelName(Integer channelId) {
        if (channelId == null || channelId == 0) {
            return CommonResult.success("");
        }
        ActivityChannelDO channel = activityChannelMapper.selectOne(ActivityChannelDO::getId, channelId);
        if (channel != null) {
            return CommonResult.success(activityChannelMapper.selectOne(ActivityChannelDO::getId, channelId).getChannelName());
        } else {
            return CommonResult.success("");
        }
    }

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;

    public List<String> getGuideImage(Long activityId) {
        LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectById(activityId);
        if (lotterySettingsDO != null) {
            activityId = lotterySettingsDO.getActivityId();
        }

        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (activityDO == null || activityDO.getGuideImage() == null || activityDO.getGuideImage().isBlank()) {
            return List.of();
        }

        //{"communityManagerImg":"https://stage-files.htyoudao.com/2026/03/15/xt5ce5bp8n1773553814995.png","communityStoreImg":"https://stage-files.htyoudao.com/2026/03/15/pp6pxogywe1773553817771.png"}
        return ImageValueParseUtil.parseImageValues(activityDO.getGuideImage());
    }

    public CommonResult<Map<Long, String>> selectActivityByIds(Set<Long> activityIds) {
        return CommonResult.success(activityService.selectActivityByIds(activityIds));
    }

    @Override
    public CommonResult<List<ActivityChannelNameDataRespVO>> getChannelList() {
        return CommonResult.success(activityChannelNameService.getChannelList());
    }

    @Resource
    private ActivityStoreMapper activityStoreMapper;

    @Resource
    private ActivityStoreTagMapper activityStoreTagMapper;

    @DubboReference
    private StoreApi storeApi;

    /** 门店标签变更统一从这里更新活动范围；重试时按当前标签判断。 */
    @Override
    public void updateActivityStoreByTagIdAndStoreId(List<ActivityStoreTagUpdateDTO> updates, CouponStoreTagSqlTypeEnum sqlType) {
        if (CollectionUtils.isEmpty(updates) || sqlType == null) {
            return;
        }
        for (ActivityStoreTagUpdateDTO update : updates) {
            if (update == null || update.getStoreId() == null || CollectionUtils.isEmpty(update.getTagIds())) {
                continue;
            }
            if (sqlType == CouponStoreTagSqlTypeEnum.UPDATE) {
                addActivityStoreByTagIds(update.getStoreId(), update.getTagIds());
            } else {
                removeActivityStoreByTagIds(update.getStoreId(), update.getTagIds());
            }
        }
    }

    /**
     * 门店新增标签后，为引用了这些标签且应用范围为标签（app_scope=1）的活动补充 activity_store 绑定，已有绑定则跳过
     */
    private void addActivityStoreByTagIds(Long storeId, List<Long> tagIds) {
        List<Long> activityIds = selectTagScopeActivityIds(tagIds);
        for (Long activityId : activityIds) {
            if (lotteryScopeService.refreshMembership(activityId, storeId)) continue;
            // 新增通知可能晚于删除通知到达，先核对门店当前是否仍命中活动标签。
            if (!matchesCurrentActivityTags(activityId, storeId)) continue;
            Long boundCount = activityStoreMapper.selectCount(new LambdaQueryWrapperX<ActivityStoreDO>()
                    .eq(ActivityStoreDO::getActivityId, activityId)
                    .eq(ActivityStoreDO::getStoreId, storeId));
            if (boundCount != null && boundCount > 0) {
                continue;
            }
            ActivityStoreDO activityStoreDO = new ActivityStoreDO();
            activityStoreDO.setActivityId(activityId);
            activityStoreDO.setStoreId(storeId);
            activityStoreMapper.insert(activityStoreDO);
        }
    }

    /**
     * 门店移除标签后，若活动配置的标签集合（activity_store_tag）均不再命中该门店（system 侧缓存判断），
     * 删除该活动与门店的 activity_store 绑定；仍命中其他标签则保留绑定
     */
    private void removeActivityStoreByTagIds(Long storeId, List<Long> tagIds) {
        List<Long> activityIds = selectTagScopeActivityIds(tagIds);
        if (CollectionUtils.isEmpty(activityIds)) {
            return;
        }
        for (Long activityId : activityIds) {
            if (lotteryScopeService.refreshMembership(activityId, storeId)) continue;
            if (!matchesCurrentActivityTags(activityId, storeId)) {
                activityStoreMapper.delete(new LambdaQueryWrapperX<ActivityStoreDO>()
                        .eq(ActivityStoreDO::getActivityId, activityId)
                        .eq(ActivityStoreDO::getStoreId, storeId));
            }
        }
    }

    /** 新增和移除通知都以当前标签为准，避免乱序重试写回过期范围。 */
    private boolean matchesCurrentActivityTags(Long activityId, Long storeId) {
        List<Long> activityTagIds = activityStoreTagMapper.selectList(new LambdaQueryWrapperX<ActivityStoreTagDO>()
                        .eq(ActivityStoreTagDO::getActivityId, activityId))
                .stream().map(ActivityStoreTagDO::getTagId).filter(Objects::nonNull).distinct().toList();
        return !activityTagIds.isEmpty() && Boolean.TRUE.equals(storeApi.matchStoreTagCache(
                BusinessContextHolder.getRequiredBusinessId(), storeId, activityTagIds));
    }

    /**
     * 查询引用了入参标签且应用范围为标签（app_scope=1）的活动；门店范围（app_scope=0）的活动不处理
     */
    private List<Long> selectTagScopeActivityIds(List<Long> tagIds) {
        List<Long> activityIds = activityStoreTagMapper.selectList(new LambdaQueryWrapperX<ActivityStoreTagDO>()
                        .in(ActivityStoreTagDO::getTagId, tagIds))
                .stream()
                .map(ActivityStoreTagDO::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(activityIds)) {
            return List.of();
        }
        return activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                        .in(ActivityDO::getId, activityIds)
                        .eq(ActivityDO::getAppScope, 1))
                .stream()
                .map(ActivityDO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }


    public CommonResult<Map<Long, List<ActivityNjnzDTO>>> selectNjnzActivityFallback(Long storeId, Collection<Long> commodityIds, Throwable ex) {
        log.error("ActivityApi.selectNjnzActivityFallback", ex);
        throw exception(PROMOTION_FALLBACK_ERROR);
    }

    public CommonResult<SettlementActivitiesDTO> selectSettlementActivitiesFallback(
            Long storeId, Collection<Long> commodityIds, Throwable ex) {
        log.error("ActivityApi.selectSettlementActivitiesFallback", ex);
        throw exception(PROMOTION_FALLBACK_ERROR);
    }

    public CommonResult<SettlementActivitiesDTO> selectSettlementActivitiesExceptionHandler(
            Long storeId, Collection<Long> commodityIds, BlockException ex) {
        log.error("ActivityApi.selectSettlementActivitiesExceptionHandler", ex);
        throw exception(PROMOTION_BLOCK_ERROR);
    }

    public CommonResult<Map<Long, List<ActivityNjnzDTO>>> selectNjnzActivityExceptionHandler(Long storeId, Collection<Long> commodityIds, BlockException ex) {
        log.error("ActivityApi.selectNjnzActivityExceptionHandler", ex);
        throw exception(PROMOTION_BLOCK_ERROR);

    }

    public CommonResult<Map<Long, List<ActivityNjnzDTO>>> selectMJActivityFallback(Long storeId, Collection<Long> commodityIds, Throwable ex) {
        log.error("ActivityApi.selectMJActivityFallback", ex);
        throw exception(PROMOTION_FALLBACK_ERROR);
    }

    public CommonResult<Map<Long, List<ActivityNjnzDTO>>> selectMJActivityExceptionHandler(Long storeId, Collection<Long> commodityIds, BlockException ex) {
        log.error("ActivityApi.selectMJActivityExceptionHandler", ex);
        throw exception(PROMOTION_BLOCK_ERROR);

    }

    public CommonResult<Map<Long, List<ActivityMzDTO>>> selectMZActivityFallback(Long storeId, Collection<Long> commodityIds, Throwable ex) {
        log.error("ActivityApi.selectMZActivityFallback", ex);
        throw exception(PROMOTION_FALLBACK_ERROR);
    }

    public CommonResult<Map<Long, List<ActivityMzDTO>>> selectMZActivityExceptionHandler(Long storeId, Collection<Long> commodityIds, BlockException ex) {
        log.error("ActivityApi.selectMZActivityExceptionHandler", ex);
        throw exception(PROMOTION_BLOCK_ERROR);

    }
}
