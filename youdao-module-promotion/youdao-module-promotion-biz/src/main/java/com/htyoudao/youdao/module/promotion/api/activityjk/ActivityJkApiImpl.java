package com.htyoudao.youdao.module.promotion.api.activityjk;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.api.activitycq.ActivityCqApi;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkTaskDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJk.ActivityJkMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJkCommodity.ActivityJkCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJkTask.ActivityJkTaskMapper;
import com.htyoudao.youdao.module.promotion.enums.ActivityJkTaskTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activityAnswerApp.ActivityAnswerAppService;
import com.htyoudao.youdao.module.promotion.service.activityJkApp.ActivityJkAppService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryMobileService;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;

/**
 * 集卡活动 RPC 实现。
 * 这里提供下单链路使用的集卡活动查询，并在命中活动后同步完成一次下单任务。
 */
@DubboService
@Validated
@DS(DsNameConstants.SHARDING)
@Slf4j
public class ActivityJkApiImpl implements ActivityJkApi {

    /**
     * 下单任务处理锁前缀。
     * 用于避免同一会员在高并发下重复累计同一个活动的下单任务。
     */
    private static final String JK_ORDER_TASK_LOCK_PREFIX = "lock:jk:order:";

    @Resource
    private ActivityJkMapper activityJkMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivityJkCommodityMapper activityJkCommodityMapper;

    @Resource
    private ActivityJkTaskMapper activityJkTaskMapper;

    @Resource
    private ActivityAppService activityAppService;

    @Resource
    private ActivityJkAppService activityJkAppService;
    @Resource
    private ActivityCqApi activityCqApi;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private LotteryMobileService lotteryMobileService;
    @Resource
    private ActivityAnswerAppService activityAnswerAppService;

    @Override
    public void getActivityJkList(ActivityJkOrderReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.getStoreId() == null || reqDTO.getMemberId() == null) {
            return ;
        }

        // 先查集卡子表，只保留下单任务开启的活动配置。
        List<ActivityJkDO> activityJkList = activityJkMapper.selectList(new LambdaQueryWrapperX<ActivityJkDO>()
                .eq(ActivityJkDO::getPlaceOrderStatus, 1));
        if (activityJkList.isEmpty()) {
            handleLotteryOrderTaskQuietly(reqDTO);
            return ;
        }

        List<Long> activityIds = activityJkList.stream()
                .map(ActivityJkDO::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (activityIds.isEmpty()) {
            handleLotteryOrderTaskQuietly(reqDTO);
            return ;
        }

        Map<Long, ActivityJkDO> activityJkMap = activityJkList.stream()
                .filter(item -> item.getActivityId() != null)
                .collect(Collectors.toMap(ActivityJkDO::getActivityId, item -> item, (left, right) -> left));

        // 主表负责活动启用状态、业务归属和时间条件。
        List<ActivityDO> activityList = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                .in(ActivityDO::getId, activityIds)
                .eq(ActivityDO::getActivityType, ActivityTypeEnum.JK.getCode())
                .eq(ActivityDO::getIsEnabled, 1));
        if (activityList.isEmpty()) {
            handleLotteryOrderTaskQuietly(reqDTO);
            return ;
        }

        // 只有指定门店参与的活动才需要查 activity_store 关系表。
        List<Long> limitedStoreActivityIds = activityList.stream()
                .filter(activity -> Objects.equals(activity.getActivityStore(), 0))
                .map(ActivityDO::getId)
                .collect(Collectors.toList());
        Map<Long, List<Long>> activityStoreMap = limitedStoreActivityIds.isEmpty()
                ? Collections.emptyMap()
                : activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>()
                                .in(ActivityStoreDO::getActivityId, limitedStoreActivityIds))
                        .stream()
                        .collect(Collectors.groupingBy(
                                ActivityStoreDO::getActivityId,
                                Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())
                        ));

        // 先按活动时间和门店过滤，减少后续商品关系和任务处理的无效查询。
        List<ActivityDO> candidateActivityList = activityList.stream()
                .filter(activity -> TimeValidationUtil.isTimeValid(
                        activity.getStartDate(),
                        activity.getEndDate(),
                        activity.getDayNumbers(),
                        activity.getWeekNumbers(),
                        activity.getTimeRange()
                ))
                .filter(activity -> isStoreMatched(activity, activityStoreMap.get(activity.getId()), reqDTO.getStoreId()))
                .collect(Collectors.toList());
        if (candidateActivityList.isEmpty()) {
            handleLotteryOrderTaskQuietly(reqDTO);
            return ;
        }

        Set<Long> candidateActivityIds = candidateActivityList.stream()
                .map(ActivityDO::getId)
                .collect(Collectors.toSet());

        // 只对“指定商品”的候选活动批量查关联商品，避免循环查库。
        Set<Long> specifiedCommodityActivityIds = activityJkList.stream()
                .filter(item -> item.getActivityId() != null && candidateActivityIds.contains(item.getActivityId()))
                .filter(item -> Objects.equals(item.getPlaceOrderProduct(), 2))
                .map(ActivityJkDO::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, List<Long>> commodityMap = buildCommodityMap(specifiedCommodityActivityIds);

        for (ActivityDO activityDO : candidateActivityList) {
            ActivityJkDO activityJkDO = activityJkMap.get(activityDO.getId());
            if (activityJkDO == null) {
                continue;
            }
            if (!isOrderMatched(activityJkDO, commodityMap.getOrDefault(activityDO.getId(), Collections.emptyList()), reqDTO)) {
                continue;
            }
            if (!completeOrderTask(activityDO.getId(), reqDTO.getMemberId(), activityJkDO)) {
                continue;
            }

        }
        handleLotteryOrderTaskQuietly(reqDTO);
        return ;
    }

    private void handleLotteryOrderTaskQuietly(ActivityJkOrderReqDTO reqDTO) {
        try {
            lotteryMobileService.handleOrderTask(reqDTO);
        } catch (Exception e) {
            // 不影响集卡原有下单任务链路。
        }
        try {
            activityCqApi.getActivityCqList(reqDTO);
        } catch (Exception e) {
            // 不影响集卡原有下单任务链路。
        }
        try {
            activityAnswerAppService.handleOrderTask(reqDTO);
        } catch (Exception e) {
            log.warn("处理有奖问答下单任务失败，storeId={}，memberId={}，commodityIds={}，paymentAmount={}",
                    reqDTO.getStoreId(), reqDTO.getMemberId(), reqDTO.getCommodityIds(), reqDTO.getPaymentAmount(), e);
        }
    }

    /**
     * 校验订单是否满足活动配置：
     * 1. 下单类型匹配
     * 2. 指定商品配置匹配
     * 3. 支付门槛匹配
     */
    private boolean isOrderMatched(ActivityJkDO activityJkDO, List<Long> configuredCommodityIds, ActivityJkOrderReqDTO reqDTO) {
        if (activityJkDO == null || reqDTO == null) {
            return false;
        }
        boolean categoryRestricted = Objects.equals(activityJkDO.getPlaceOrderType(), 1);
        if (categoryRestricted && !isCategoryMatched(activityJkDO.getCategoryType(), reqDTO.getOrderProductType())) {
            return false;
        }
        if (!categoryRestricted && Objects.equals(activityJkDO.getPlaceOrderProduct(), 2)) {
            if (configuredCommodityIds.isEmpty() || reqDTO.getCommodityIds() == null || reqDTO.getCommodityIds().isEmpty()) {
                return false;
            }
            boolean matched = reqDTO.getCommodityIds().stream().anyMatch(configuredCommodityIds::contains);
            if (!matched) {
                return false;
            }
        }
        if (Objects.equals(activityJkDO.getPaymentThreshold(), 1)) {
            BigDecimal paymentAmount = reqDTO.getPaymentAmount();
            BigDecimal thresholdAmount = activityJkDO.getPaymentCount();
            if (paymentAmount == null || thresholdAmount == null || paymentAmount.compareTo(thresholdAmount) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 按活动配置的参与品类判断订单品类是否匹配。
     * 1 全部不做限制，2 仅单品要求订单包含单品，3 仅套餐要求订单包含套餐。
     */
    private boolean isCategoryMatched(Integer categoryType, Integer orderProductType) {
        if (categoryType == null || Objects.equals(categoryType, 1)) {
            return true;
        }
        if (orderProductType == null) {
            return false;
        }
        if (Objects.equals(categoryType, 2)) {
            return Objects.equals(orderProductType, 1) || Objects.equals(orderProductType, 3);
        }
        if (Objects.equals(categoryType, 3)) {
            return Objects.equals(orderProductType, 2) || Objects.equals(orderProductType, 3);
        }
        return true;
    }

    /**
     * 完成一次下单任务。
     * 次数类型为“每人每天”时，只统计当天记录；次数类型为“不限制”时统计累计记录。
     * 如果已经达到次数上限，则返回 false，不再给本次订单发放任务次数。
     */
    private boolean completeOrderTask(Long activityId, Long memberId, ActivityJkDO activityJkDO) {
        RLock lock = redissonClient.getLock(JK_ORDER_TASK_LOCK_PREFIX + activityId + ":" + memberId);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            Integer taskLimit = defaultZero(activityJkDO.getPlaceOrderCardNumber());
            ActivityJkTaskDO taskDO = getOrderTaskRecord(activityId, memberId, activityJkDO);
            int currentFinishCount = taskDO == null ? 0 : defaultZero(taskDO.getFinishCount());
            if (!isUnlimitedOrderTask(activityJkDO) && currentFinishCount >= taskLimit) {
                return false;
            }

            if (taskDO == null) {
                ActivityJkTaskDO latestTaskDO = getLatestOrderTaskRecord(activityId, memberId);
                if (latestTaskDO == null) {
                    if (!insertInitOrderTaskSafely(activityId, memberId)) {
                        return retryCompleteOrderTaskAfterInsertRace(activityId, memberId, activityJkDO, taskLimit);
                    }
                    activityJkAppService.refreshDrawChanceCache(activityId, memberId);
                    return true;
                }

                if (isUnlimitedOrderTask(activityJkDO)) {
                    latestTaskDO.setFinishCount(defaultZero(latestTaskDO.getFinishCount()) + 1);
                    latestTaskDO.setGainCount(defaultZero(latestTaskDO.getGainCount()) + 1);
                    latestTaskDO.setConsumeCount(defaultZero(latestTaskDO.getConsumeCount()));
                } else {
                    latestTaskDO.setFinishCount(1);
                    latestTaskDO.setGainCount(1);
                    latestTaskDO.setConsumeCount(0);
                }
                latestTaskDO.setUpdateTime(LocalDateTime.now());
                updateTaskRecord(latestTaskDO);
                activityJkAppService.refreshDrawChanceCache(activityId, memberId);
                return true;
            }

            taskDO.setFinishCount(currentFinishCount + 1);
            taskDO.setGainCount(defaultZero(taskDO.getGainCount()) + 1);
            taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()));
            updateTaskRecord(taskDO);
            activityJkAppService.refreshDrawChanceCache(activityId, memberId);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 首条任务记录插入存在并发窗口，撞到唯一键时交给调用方回查并走更新逻辑。
     */
    private boolean insertInitOrderTaskSafely(Long activityId, Long memberId) {
        try {
            activityJkTaskMapper.insert(buildInitOrderTask(activityId, memberId));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    /**
     * 插入并发冲突后，基于库里的现有记录补做本次下单任务累计。
     */
    private boolean retryCompleteOrderTaskAfterInsertRace(Long activityId, Long memberId,
                                                           ActivityJkDO activityJkDO, Integer taskLimit) {
        ActivityJkTaskDO taskDO = getOrderTaskRecord(activityId, memberId, activityJkDO);
        if (taskDO == null) {
            taskDO = getLatestOrderTaskRecord(activityId, memberId);
        }
        if (taskDO == null) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }

        int currentFinishCount = defaultZero(taskDO.getFinishCount());
        if (!isUnlimitedOrderTask(activityJkDO) && currentFinishCount >= taskLimit) {
            return false;
        }
        taskDO.setFinishCount(currentFinishCount + 1);
        taskDO.setGainCount(defaultZero(taskDO.getGainCount()) + 1);
        taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()));
        taskDO.setUpdateTime(LocalDateTime.now());
        updateTaskRecord(taskDO);
        activityJkAppService.refreshDrawChanceCache(activityId, memberId);
        return true;
    }

    /**
     * 查询下单任务记录。
     * 配置为“每人每天”时只看当天，配置为“不限制”时看累计记录。
     */
    private ActivityJkTaskDO getOrderTaskRecord(Long activityId, Long memberId, ActivityJkDO activityJkDO) {
        LambdaQueryWrapperX<ActivityJkTaskDO> queryWrapper = new LambdaQueryWrapperX<ActivityJkTaskDO>()
                .eq(ActivityJkTaskDO::getActivityId, activityId)
                .eq(ActivityJkTaskDO::getMemberId, memberId)
                .eq(ActivityJkTaskDO::getTaskType, ActivityJkTaskTypeEnum.ORDER.getCode())
                .orderByDesc(ActivityJkTaskDO::getUpdateTime)
                .orderByDesc(ActivityJkTaskDO::getCreateTime)
                .orderByDesc(ActivityJkTaskDO::getId)
                .last("limit 1");
        if (!isUnlimitedOrderTask(activityJkDO)) {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime tomorrowStart = todayStart.plusDays(1);
            queryWrapper.ge(ActivityJkTaskDO::getUpdateTime, todayStart)
                    .lt(ActivityJkTaskDO::getUpdateTime, tomorrowStart);
        }
        return activityJkTaskMapper.selectOne(queryWrapper);
    }

    /**
     * 查询最新一条下单任务记录，用于每天限制场景跨天复用历史记录。
     */
    private ActivityJkTaskDO getLatestOrderTaskRecord(Long activityId, Long memberId) {
        return activityJkTaskMapper.selectOne(new LambdaQueryWrapperX<ActivityJkTaskDO>()
                .eq(ActivityJkTaskDO::getActivityId, activityId)
                .eq(ActivityJkTaskDO::getMemberId, memberId)
                .eq(ActivityJkTaskDO::getTaskType, ActivityJkTaskTypeEnum.ORDER.getCode())
                .orderByDesc(ActivityJkTaskDO::getUpdateTime)
                .orderByDesc(ActivityJkTaskDO::getCreateTime)
                .orderByDesc(ActivityJkTaskDO::getId)
                .last("limit 1"));
    }

    /**
     * 构建初始化的下单任务记录。
     */
    private ActivityJkTaskDO buildInitOrderTask(Long activityId, Long memberId) {
        ActivityJkTaskDO initTaskDO = new ActivityJkTaskDO();
        initTaskDO.setActivityId(activityId);
        initTaskDO.setMemberId(memberId);
        initTaskDO.setTaskType(ActivityJkTaskTypeEnum.ORDER.getCode());
        initTaskDO.setFinishCount(1);
        initTaskDO.setGainCount(1);
        initTaskDO.setConsumeCount(0);
        return initTaskDO;
    }

    /**
     * 按主键和分片键更新任务记录，避免 updateById 在分表场景下更新失败。
     */
    private void updateTaskRecord(ActivityJkTaskDO taskDO) {
        UpdateWrapper<ActivityJkTaskDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", taskDO.getId())
                .eq("member_id", taskDO.getMemberId())
                .set("finish_count", taskDO.getFinishCount())
                .set("gain_count", taskDO.getGainCount())
                .set("consume_count", taskDO.getConsumeCount())
                .set("update_time", taskDO.getUpdateTime() == null ? LocalDateTime.now() : taskDO.getUpdateTime());
        if (activityJkTaskMapper.update(null, updateWrapper) <= 0) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
    }

    /**
     * 下单任务次数类型为 1 时表示不限制，否则按每天限制处理。
     */
    private boolean isUnlimitedOrderTask(ActivityJkDO activityJkDO) {
        if (activityJkDO == null) {
            return true;
        }
        return Objects.equals(activityJkDO.getPlaceOrderCard(), 1)
                || activityJkDO.getPlaceOrderCardNumber() == null
                || activityJkDO.getPlaceOrderCardNumber() == 0;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 门店关系为空时，表示该活动对所有门店生效。
     */
    private boolean isStoreMatched(ActivityDO activityDO, List<Long> relatedStores, Long storeId) {
        if (activityDO != null && Objects.equals(activityDO.getActivityStore(), 1)) {
            return true;
        }
        return relatedStores == null || relatedStores.isEmpty() || relatedStores.contains(storeId);
    }

    /**
     * 批量构建活动与指定商品 ID 列表的映射。
     * 如果活动没有限制商品，或者没有维护商品关系，则返回空列表给下游。
     */
    private Map<Long, List<Long>> buildCommodityMap(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return activityJkCommodityMapper.selectByActivityIds(new ArrayList<>(activityIds)).stream()
                .filter(item -> item.getActivityId() != null && item.getCommodityId() != null)
                .collect(Collectors.groupingBy(
                        ActivityJkCommodityDO::getActivityId,
                        Collectors.mapping(ActivityJkCommodityDO::getCommodityId, Collectors.toList())
                ));
    }
}
