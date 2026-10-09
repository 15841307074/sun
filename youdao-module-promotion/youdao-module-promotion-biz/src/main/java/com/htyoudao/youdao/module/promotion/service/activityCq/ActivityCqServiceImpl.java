package com.htyoudao.youdao.module.promotion.service.activityCq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqDetailRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqLogEventReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqLogExportReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqLogExportRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqLogPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqLogRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqPrizeReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqSaveOrUpdateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqSpreadRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqSpreadSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqStateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqStatisticsRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqUpdateExpressReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqWinningExportRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityHelpDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityTaskDO;
import com.htyoudao.youdao.module.promotion.dal.redis.CQKeyConstants;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCommodity.ActivityCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCq.ActivityCqMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityHelp.ActivityHelpMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLog.ActivityCqLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityPrize.ActivityPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityTask.ActivityTaskMapper;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.ActivityCqPrizeTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import org.apache.dubbo.config.annotation.DubboReference;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_ERROR;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_OUT_ERROR;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.MJ_IS_NOT_ID;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class ActivityCqServiceImpl extends ServiceImpl<ActivityCqMapper, ActivityCqDO> implements ActivityCqService {

    /**
     * 时区配置
     */
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final int CQ_SETTING_CACHE_SECONDS = 300;
    private static final int CQ_PRIZE_CACHE_SECONDS = 300;

    /**
     * 抽签活动子表 mapper
     */
    @Resource
    private ActivityCqMapper activityCqMapper;
    /**
     * 抽签奖品 mapper
     */
    @Resource
    private ActivityPrizeMapper activityPrizeMapper;
    /**
     * 抽签活动商品 mapper
     */
    @Resource
    private ActivityCommodityMapper activityCommodityMapper;
    /**
     * 抽签任务 mapper
     */
    @Resource
    private ActivityTaskMapper activityTaskMapper;
    /**
     * 抽签助力 mapper
     */
    @Resource
    private ActivityHelpMapper activityHelpMapper;
    /**
     * 抽签记录 mapper
     */
    @Resource
    private ActivityCqLogMapper activityCqLogMapper;
    @Resource
    private ActivityCqLogQueryService activityCqLogQueryService;
    /**
     * 主活动服务
     */
    @Resource
    private ActivityService activityService;
    /**
     * 活动门店服务
     */
    @Resource
    private ActivityStoreService activityStoreService;
    /**
     * 活动渠道服务
     */
    @Resource
    private ActivityChannelService activityChannelService;
    /**
     * 商品服务
     */
    @DubboReference
    private CommodityApi commodityApi;
    /**
     * Excel 导出服务
     */
    @Resource
    private ExcelActionService<ActivityCqLogExportReqVO> excelActionService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private IEventService eventService;
    // 分享图片
    @Value("${activity.cq.shareImageUrl}")
    private String shareImageUrl;
    // 分享标题
    @Value("${activity.cq.shareTitle}")
    private String shareTitle;
    // 分享描述
    @Value("${activity.cq.shareDescription}")
    private String shareDescription;
    /**
     * 创建抽签活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> createActivityCq(ActivityCqSaveOrUpdateReqVO reqVO) {
        // 先落主活动，再落抽签子配置和关联信息，保证后台保存时数据结构完整。
        ActivityDO activityDO = buildActivityDO(reqVO);
        activityDO.setActivityType(ActivityTypeEnum.CQ.getCode());
        Long activityId = activityService.createActivity(activityDO);

        ActivityCqDO activityCqDO = buildActivityCqDO(reqVO);
        activityCqDO.setActivityId(activityId);
        activityCqDO.setDrawStatus(0);
        activityCqDO.setShareNote(shareDescription);
        activityCqDO.setShareTitle(shareTitle);
        activityCqDO.setShareImgUrl(shareImageUrl);
        activityCqMapper.insert(activityCqDO);

        syncStores(activityId, reqVO.getStoreIds());
        syncCommodities(activityId, reqVO.getCommodityIds());
        syncPrizes(activityId, Collections.emptyList(), reqVO.getPrizeReqVOList());
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.CQ.getCode());
        refreshCqCaches(activityId);
        return success(1);
    }

    /**
     * 修改抽签活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> updateActivityCq(ActivityCqSaveOrUpdateReqVO reqVO) {
        // 修改时兼容两种传法：优先 activityId，其次根据子表 id 反查主活动 id。
        Long activityId = reqVO.getActivityId();
        if (activityId == null && reqVO.getId() != null) {
            ActivityCqDO current = activityCqMapper.selectById(reqVO.getId());
            if (current != null) {
                activityId = current.getActivityId();
            }
        }
        if (activityId == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        reqVO.setActivityId(activityId);

        ActivityDO activityDO = buildActivityDO(reqVO);
        activityDO.setId(activityId);
        activityDO.setActivityType(ActivityTypeEnum.CQ.getCode());
        activityService.updateActivity(activityDO);

        ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getActivityId, activityId)
                .last("limit 1"));
        ActivityCqDO updateDO = buildActivityCqDO(reqVO);
        updateDO.setActivityId(activityId);
        if (activityCqDO == null) {
            updateDO.setDrawStatus(0);
            activityCqMapper.insert(updateDO);
        } else {
            updateDO.setId(activityCqDO.getId());
            if (updateDO.getDrawStatus() == null) {
                updateDO.setDrawStatus(activityCqDO.getDrawStatus());
            }
            activityCqMapper.updateById(updateDO);
        }

        syncStores(activityId, reqVO.getStoreIds());
        syncCommodities(activityId, reqVO.getCommodityIds());
        List<ActivityPrizeDO> existingPrizeList = activityPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityPrizeDO>()
                .eq(ActivityPrizeDO::getActivityId, activityId));
        syncPrizes(activityId, existingPrizeList, reqVO.getPrizeReqVOList());
        refreshCqCaches(activityId);
        return success(1);
    }

    /**
     * 删除抽签活动及其关联数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> deleteActivityCq(Long id) {
        activityService.deleteActivity(id);
        activityCqMapper.delete(new LambdaQueryWrapperX<ActivityCqDO>().eq(ActivityCqDO::getActivityId, id));
        activityPrizeMapper.delete(new LambdaQueryWrapperX<ActivityPrizeDO>().eq(ActivityPrizeDO::getActivityId, id));
        activityCommodityMapper.delete(new LambdaQueryWrapperX<ActivityCommodityDO>().eq(ActivityCommodityDO::getActivityId, id));
        activityTaskMapper.delete(new LambdaQueryWrapperX<ActivityTaskDO>().eq(ActivityTaskDO::getActivityId, id));
        activityHelpMapper.delete(new LambdaQueryWrapperX<ActivityHelpDO>().eq(ActivityHelpDO::getActivityId, id));
        activityStoreService.deleteByActivityId(id);
        activityChannelService.deleteByActivityId(id);
        redisCache.deleteObject(CQKeyConstants.CQ_SETTING + id);
        redisCache.deleteObject(CQKeyConstants.CQ_PRIZE + id);
        return success(1);
    }

    /**
     * 查询抽签活动详情
     */
    @Override
    public ActivityCqDetailRespVO selectDetail(Long id) {
        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO == null) {
            return null;
        }
        ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getActivityId, id)
                .last("limit 1"));
        if (activityCqDO == null) {
            return null;
        }

        ActivityCqDetailRespVO respVO = new ActivityCqDetailRespVO();
        BeanUtils.copyProperties(activityDO, respVO);
        BeanUtils.copyProperties(activityCqDO, respVO);
        respVO.setId(activityCqDO.getId());
        respVO.setActivityId(id);
        respVO.setStartDate(resolveCqStartDate(activityDO, activityCqDO));
        respVO.setEndDate(resolveCqEndDate(activityDO, activityCqDO));
        respVO.setResultPublishTime(toDate(activityCqDO.getResultPublishTime()));
        respVO.setIsDrawn(Objects.equals(activityCqDO.getDrawStatus(), 2));
        respVO.setDayNumberList(parseIntegerList(activityDO.getDayNumbers()));
        respVO.setWeekNumberList(parseIntegerList(activityDO.getWeekNumbers()));
        respVO.setTimeRangeList(parseStringList(activityDO.getTimeRange()));
        respVO.setStoreIds(activityStoreService.selectStoreIdsByActivityId(id));
        List<ActivityCommodityDO> commodityDOList = activityCommodityMapper.selectList(new LambdaQueryWrapperX<ActivityCommodityDO>()
                .eq(ActivityCommodityDO::getActivityId, id));
        List<Long> commodityIds = commodityDOList.stream().map(ActivityCommodityDO::getCommodityId).collect(Collectors.toList());
        respVO.setCommodityIds(commodityIds);
        respVO.setCommodityDTOList(getCommodityDTOList(commodityIds));
        respVO.setPrizeReqVOList(activityPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityPrizeDO>()
                        .eq(ActivityPrizeDO::getActivityId, id)
                        .orderByAsc(ActivityPrizeDO::getId))
                .stream().map(this::convertPrizeReqVO).collect(Collectors.toList()));
        //设置门店信息
        if (respVO.getActivityStore().equals(0)) {
            respVO.setStoreInfoDTOList(activityStoreService.storesByActivityId(id));
        } else {
            respVO.setStoreInfoDTOList(new ArrayList<>());
        }
        return respVO;
    }

    /**
     * 复制抽签活动配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> copyActivityCq(ActivityCqSaveOrUpdateReqVO reqVO) {
        ActivityCqSaveOrUpdateReqVO copyReqVO = BeanUtils.toBean(reqVO, ActivityCqSaveOrUpdateReqVO.class);
        copyReqVO.setId(null);
        copyReqVO.setActivityId(null);
        if (!CollectionUtils.isEmpty(copyReqVO.getPrizeReqVOList())) {
            copyReqVO.getPrizeReqVOList().forEach(item -> item.setId(null));
        }
        return createActivityCq(copyReqVO);
    }

    /**
     * 获取抽签活动概览数据
     */
    @Override
    public Map<String, Object> getActivityCqAnalysis(String id) {
        Long activityId = Long.valueOf(id);
        ActivityDO activityDO = activityService.selectById(activityId);
        Map<String, Object> result = new HashMap<>();
        if (activityDO == null) {
            result.put("sum", 0L);
            result.put("distinctSum", 0L);
            result.put("couponCount", 0L);
            result.put("pointsCount", BigDecimal.ZERO);
            result.put("totalCash", BigDecimal.ZERO);
            result.put("pv", 0L);
            result.put("uv", 0L);
            result.put("shareCount", 0L);
            return result;
        }

        ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getActivityId, activityId)
                .last("limit 1"));
        LocalDateTime start = toLocalDateTime(resolveCqStartDate(activityDO, activityCqDO));
        LocalDateTime end = toLocalDateTime(resolveCqEndDate(activityDO, activityCqDO));
        end = normalizeAnalysisEndTime(end);

        QueryWrapper<ActivityCqLogDO> wrapper = new QueryWrapper<ActivityCqLogDO>();
        wrapper.lambda().eq(ActivityCqLogDO::getActivityId, activityId)
                .eq(ActivityCqLogDO::getDeleted, 0)
                .eq(ActivityCqLogDO::getResultStatus, 2)
                .isNotNull(ActivityCqLogDO::getMemberMobile)
                .ne(ActivityCqLogDO::getMemberMobile, "");
        if (start != null) {
            wrapper.lambda().ge(ActivityCqLogDO::getDrawTime, start);
        }
        if (end != null) {
            wrapper.lambda().le(ActivityCqLogDO::getDrawTime, end);
        }

        List<ActivityCqLogDO> logList = activityCqLogMapper.selectList(wrapper);
        List<ActivityCqLogDO> winningLogList = logList.stream()
                .filter(item -> Objects.equals(item.getResultStatus(), 2))
                .collect(Collectors.toList());

        BigDecimal pointsCount = BigDecimal.ZERO;
        BigDecimal totalCash = BigDecimal.ZERO;
        long couponCount = 0L;
        if (!CollectionUtils.isEmpty(winningLogList)) {
            Set<Long> prizeIdSet = winningLogList.stream()
                    .map(ActivityCqLogDO::getPrizeId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Map<Long, ActivityPrizeDO> prizeMap = CollectionUtils.isEmpty(prizeIdSet)
                    ? Collections.emptyMap()
                    : activityPrizeMapper.selectBatchIds(prizeIdSet).stream()
                    .collect(Collectors.toMap(ActivityPrizeDO::getId, item -> item, (left, right) -> left));

            for (ActivityCqLogDO logDO : winningLogList) {
                ActivityPrizeDO prizeDO = prizeMap.get(logDO.getPrizeId());
                if (prizeDO == null) {
                    continue;
                }
                if (Objects.equals(prizeDO.getPrizeType(), 2) && prizeDO.getPrizeValue() != null) {
                    pointsCount = pointsCount.add(prizeDO.getPrizeValue());
                }
                if ((Objects.equals(prizeDO.getPrizeType(), 1) || Objects.equals(prizeDO.getPrizeType(), 6))
                        && !Objects.equals(logDO.getPrizeState(), 9)) {
                    couponCount++;
                }
                if (Objects.equals(prizeDO.getPrizeType(), 5)
                        && Objects.equals(logDO.getClaimStatus(), 2)
                        && prizeDO.getPrizeValue() != null) {
                    totalCash = totalCash.add(prizeDO.getPrizeValue());
                }
            }
        }

        long sum = logList.size();
        long distinctSum = logList.stream().map(ActivityCqLogDO::getMemberId).filter(Objects::nonNull).distinct().count();
        long winningCount = winningLogList.size();
        long winningParticipation = winningLogList.stream()
                .map(ActivityCqLogDO::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long shareCount = logList.stream()
                .filter(item -> Objects.equals(item.getObtainType(), 3))
                .map(ActivityCqLogDO::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        result.put("sum", sum);
        result.put("distinctSum", distinctSum);
        result.put("couponCount", couponCount);
        result.put("pointsCount", pointsCount);
        result.put("totalCash", totalCash);
        Date startDate = resolveCqStartDate(activityDO, activityCqDO);
        Date endDate = resolveCqEndDate(activityDO, activityCqDO);
        buildDrawLotsResult(result, id, startDate, endDate);
        buildShareResult(result, id, startDate, endDate, shareCount);
        return result;
    }

    /**
     * 活动结束时间通常由前端按分钟传入；零点结束按整天统计，其余按当前分钟最后一秒统计。
     */
    private LocalDateTime normalizeAnalysisEndTime(LocalDateTime end) {
        if (end == null) {
            return null;
        }
        if (end.toLocalTime().equals(java.time.LocalTime.MIN)) {
            return end.withHour(23).withMinute(59).withSecond(59).withNano(0);
        }
        return end.withSecond(59).withNano(0);
    }

    /**
     * 统计抽签活动浏览量和访客量。
     */
    private void buildDrawLotsResult(Map<String, Object> result, String id, Date startTime, Date endTime) {
        result.put("pv", 0L);
        result.put("uv", 0L);
        if (eventService == null) {
            return;
        }
        LocalDateTime start = toLocalDateTime(startTime);
        LocalDateTime end = toLocalDateTime(endTime);
        if (end != null) {
            end = end.withHour(23).withMinute(59).withSecond(59).withNano(0);
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        Map<String, Long> pvUvMap = eventService.statPvUv(EventType.DRAW_LOTS, id, start, end, businessId);
        if (pvUvMap == null || pvUvMap.isEmpty()) {
            return;
        }
        result.put("pv", pvUvMap.getOrDefault("pv", 0L));
        result.put("uv", pvUvMap.getOrDefault("uv", 0L));
    }

    /**
     * 统计抽签活动分享人数。
     */
    private void buildShareResult(Map<String, Object> result, String id, Date startTime, Date endTime, Long defaultShareCount) {
        result.put("shareCount", defaultShareCount == null ? 0L : defaultShareCount);
        if (eventService == null) {
            return;
        }
        LocalDateTime start = toLocalDateTime(startTime);
        LocalDateTime end = toLocalDateTime(endTime);
        if (end != null) {
            end = end.withHour(23).withMinute(59).withSecond(59).withNano(0);
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        Long shareCount = eventService.statUv(EventType.SHARE, id, start, end, businessId);
        if (shareCount != null) {
            result.put("shareCount", shareCount);
        }
    }

    /**
     * 获取抽签活动按天统计数据
     */
    @Override
    public Map<String, Object> getActivityCqDailyAnalysis(ActivityCqLogEventReqVO reqVO) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        LocalDateTime startTime = reqVO.getStartTime();
        LocalDateTime endTime = reqVO.getEndTime();
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0);
            startTime = endTime.minusDays(29)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
        }
        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime)) {
                throw exception(LOTTERY_ANALYSIS_TIME_ERROR);
            }
            long daysDiff = ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate()) + 1;
            if (daysDiff > 180) {
                throw exception(LOTTERY_ANALYSIS_TIME_OUT_ERROR);
            }
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        buildCqDailyResult(result, reqVO.getId(), startTime, endTime, businessId);
        return convertStatsMapToArrays(result);
    }

    private void buildCqDailyResult(Map<String, Map<String, Long>> result, Long activityId,
                                    LocalDateTime startTime, LocalDateTime endTime, Long businessId) {
        if (activityId == null || startTime == null || endTime == null) {
            return;
        }
        if (eventService != null) {
            Map<String, Map<String, Long>> dailyPvUvMap = eventService.statDailyPvUv(
                    EventType.DRAW_LOTS, activityId.toString(), startTime, endTime, businessId);
            if (!CollectionUtils.isEmpty(dailyPvUvMap)) {
                result.putAll(dailyPvUvMap);
                return;
            }
        }
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        for (LocalDate current = startDate; !current.isAfter(endDate); current = current.plusDays(1)) {
            Map<String, Long> dayStat = new HashMap<>(2);
            dayStat.put("pv", 0L);
            dayStat.put("uv", 0L);
            result.put(current.toString(), dayStat);
        }
        List<ActivityCqLogDO> logList = activityCqLogMapper.selectList(new LambdaQueryWrapperX<ActivityCqLogDO>()
                .eq(ActivityCqLogDO::getActivityId, activityId)
                .geIfPresent(ActivityCqLogDO::getDrawTime, startTime)
                .leIfPresent(ActivityCqLogDO::getDrawTime, endTime)
                .orderByAsc(ActivityCqLogDO::getDrawTime));
        Map<String, List<ActivityCqLogDO>> dayLogMap = logList.stream()
                .filter(item -> item.getDrawTime() != null)
                .collect(Collectors.groupingBy(item -> item.getDrawTime().toLocalDate().toString()));
        for (Map.Entry<String, List<ActivityCqLogDO>> entry : dayLogMap.entrySet()) {
            Map<String, Long> dayStat = result.computeIfAbsent(entry.getKey(), key -> new HashMap<>(2));
            dayStat.put("pv", (long) entry.getValue().size());
            dayStat.put("uv", entry.getValue().stream()
                    .map(ActivityCqLogDO::getMemberId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count());
        }
    }

    private Map<String, Object> convertStatsMapToArrays(Map<String, Map<String, Long>> statsMap) {
        if (statsMap == null || statsMap.isEmpty()) {
            Map<String, Object> emptyResult = new HashMap<>(3);
            emptyResult.put("times", new String[0]);
            emptyResult.put("pvValues", new Long[0]);
            emptyResult.put("uvValues", new Long[0]);
            return emptyResult;
        }
        List<String> dateList = new ArrayList<>(statsMap.keySet());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateList.sort((dateStr1, dateStr2) -> {
            try {
                LocalDate date1 = LocalDate.parse(dateStr1, dateFormatter);
                LocalDate date2 = LocalDate.parse(dateStr2, dateFormatter);
                return date1.compareTo(date2);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("invalid date format: " + dateStr1 + " / " + dateStr2, e);
            }
        });
        int dataSize = dateList.size();
        String[] dateArray = new String[dataSize];
        Long[] uvArray = new Long[dataSize];
        Long[] pvArray = new Long[dataSize];
        for (int i = 0; i < dataSize; i++) {
            String currentDate = dateList.get(i);
            Map<String, Long> innerMap = statsMap.get(currentDate);
            dateArray[i] = currentDate;
            uvArray[i] = innerMap == null ? 0L : innerMap.getOrDefault("uv", 0L);
            pvArray[i] = innerMap == null ? 0L : innerMap.getOrDefault("pv", 0L);
        }
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("times", dateArray);
        resultMap.put("pvValues", pvArray);
        resultMap.put("uvValues", uvArray);
        return resultMap;
    }

    /**
     * 分页查询抽签记录
     */
    @Override
    public PageResult<ActivityCqLogRespVO> getActivityCqLogList(ActivityCqLogPageReqVO reqVO) {
        return pageLogList(reqVO, false);
    }

    /**
     * 统计抽签参与人数和参与次数
     */
    @Override
    public ActivityCqStatisticsRespVO getActivityCqLogCount(ActivityCqLogPageReqVO reqVO) {
        return buildStatistics(reqVO, false);
    }

    /**
     * 分页查询中签记录
     */
    @Override
    public PageResult<ActivityCqLogRespVO> getWinningLogList(ActivityCqLogPageReqVO reqVO) {
        return pageLogList(reqVO, true);
    }

    /**
     * 统计中签人数和中签次数
     */
    @Override
    public ActivityCqStatisticsRespVO getWinningCount(ActivityCqLogPageReqVO reqVO) {
        return buildStatistics(reqVO, true);
    }

    /**
     * 导出抽签记录
     */
    @Override
    public void exportActivityCqLog(ActivityCqLogExportReqVO reqVO, HttpServletRequest request, HttpServletResponse response) {
        QueryWrapper<ActivityCqLogDO> queryWrapper = buildExportLogQuery(reqVO, false);
        int pageSize = 10000;
        Page<ActivityCqLogExportRespVO> pageParam = new Page<>(1, pageSize);
        excelActionService.exportAsyncExcel(
                ActivityCqLogExportRespVO.class,
                pageParam,
                param -> getExportLogData(param, queryWrapper),
                buildLogExportFileName(reqVO, false)
        );
    }

    /**
     * 导出中签记录
     */
    @Override
    public void exportWinningLog(ActivityCqLogExportReqVO reqVO, HttpServletRequest request, HttpServletResponse response) {
        QueryWrapper<ActivityCqLogDO> queryWrapper = buildExportLogQuery(reqVO, true);
        int pageSize = 10000;
        Page<ActivityCqWinningExportRespVO> pageParam = new Page<>(1, pageSize);
        excelActionService.exportAsyncExcel(
                ActivityCqWinningExportRespVO.class,
                pageParam,
                param -> getWinningExportData(param, queryWrapper),
                buildLogExportFileName(reqVO, true)
        );
    }

    /**
     * 更新中签记录的发货信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateExpress(ActivityCqUpdateExpressReqVO reqVO) {
        // 1. 参数校验
        if (reqVO == null || reqVO.getId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        // 2. 检查记录是否存在
        ActivityCqLogDO existingLog = activityCqLogMapper.selectById(reqVO.getId());
        if (existingLog == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        // 3. 构建更新条件（只有真正有值的字段才更新）
        LambdaUpdateWrapper<ActivityCqLogDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityCqLogDO::getId, reqVO.getId());

        // 字符串字段：使用 hasText 过滤空值和空格
        updateWrapper.set(StringUtils.hasText(reqVO.getReceiveUser()),
                ActivityCqLogDO::getReceiveUser, reqVO.getReceiveUser());
        updateWrapper.set(StringUtils.hasText(reqVO.getReceiveMobile()),
                ActivityCqLogDO::getReceiveMobile, reqVO.getReceiveMobile());
        updateWrapper.set(StringUtils.hasText(reqVO.getReceiveAddress()),
                ActivityCqLogDO::getReceiveAddress, reqVO.getReceiveAddress());
        updateWrapper.set(StringUtils.hasText(reqVO.getTrackingNumber()),
                ActivityCqLogDO::getTrackingNumber, reqVO.getTrackingNumber());
        updateWrapper.set(StringUtils.hasText(reqVO.getExpressCompany()),
                ActivityCqLogDO::getExpressCompany, reqVO.getExpressCompany());

        // 枚举/状态字段：判断非 null
        updateWrapper.set(reqVO.getPrizeState() != null,
                ActivityCqLogDO::getPrizeState, reqVO.getPrizeState());

        // 4. 执行更新
        int updateCount = activityCqLogMapper.update(null, updateWrapper);

        // 5. 可选：记录操作日志
        log.info("更新快递信息成功, id: {}, 更新条数: {}", reqVO.getId(), updateCount);

        return updateCount;
    }

    /**
     * 查询分享配置
     */
    @Override
    public ActivityCqSpreadRespVO selectSpread(Long id) {
        ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getActivityId, id)
                .last("limit 1"));
        if (activityCqDO == null) {
            return null;
        }
        ActivityCqSpreadRespVO respVO = new ActivityCqSpreadRespVO();
        respVO.setId(id);
        respVO.setShareTitle(activityCqDO.getShareTitle());
        respVO.setShareNote(activityCqDO.getShareNote());
        respVO.setShareImgUrl(activityCqDO.getShareImgUrl());
        return respVO;
    }

    /**
     * 修改分享配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpread(ActivityCqSpreadSaveReqVO reqVO) {
        LambdaUpdateWrapper<ActivityCqDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityCqDO::getActivityId, reqVO.getId());
        updateWrapper.set(ActivityCqDO::getShareTitle, reqVO.getShareTitle());
        updateWrapper.set(ActivityCqDO::getShareNote, reqVO.getShareNote());
        updateWrapper.set(ActivityCqDO::getShareImgUrl, reqVO.getShareImgUrl());
        activityCqMapper.update(null, updateWrapper);
        refreshCqSettingCache(reqVO.getId());
    }

    /**
     * 获取抽签活动列表
     */
    @Override
    public List<ActivityCqReqVO> getActivityCqList() {
        List<ActivityCqDO> activityCqDOList = activityCqMapper.selectList(new LambdaQueryWrapperX<ActivityCqDO>()
                .orderByDesc(ActivityCqDO::getCreateTime));
        if (CollectionUtils.isEmpty(activityCqDOList)) {
            return Collections.emptyList();
        }
        return activityCqDOList.stream().map(item -> {
            ActivityCqReqVO respVO = new ActivityCqReqVO();
            respVO.setId(item.getId());
            respVO.setActivityId(item.getActivityId());
            respVO.setActivityCoverImage(item.getActivityCoverImage());
            respVO.setResultPublishTime(toDate(item.getResultPublishTime()));
            respVO.setPublicButton(item.getPublicButton());
            respVO.setDrawStatus(item.getDrawStatus());
            ActivityDO activityDO = activityService.selectById(item.getActivityId());
            if (activityDO != null) {
                respVO.setActivityName(activityDO.getActivityName());
                respVO.setIsEnabled(activityDO.getIsEnabled());
            }
            return respVO;
        }).collect(Collectors.toList());
    }

    /**
     * 更新抽签活动启用状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateState(ActivityCqStateReqVO reqVO) {
        activityService.updateStatus(reqVO.getId(), reqVO.getIsEnabled());
        refreshCqSettingCache(reqVO.getId());
        return 1;
    }

    /**
     * 后台保存成功后主动刷新抽签缓存，保证后台写库后 C 端马上读到最新配置。
     */
    private void refreshCqCaches(Long activityId) {
        refreshCqSettingCache(activityId);
        refreshCqPrizeCache(activityId);
    }

    private void refreshCqSettingCache(Long activityId) {
        if (activityId == null) {
            return;
        }
        ActivityDO activityDO = activityService.selectById(activityId);
        ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getActivityId, activityId)
                .last("limit 1"));
        if (activityDO == null || activityCqDO == null) {
            redisCache.deleteObject(CQKeyConstants.CQ_SETTING + activityId);
            return;
        }

        ActivityCqSaveOrUpdateReqVO cacheVO = new ActivityCqSaveOrUpdateReqVO();
        BeanUtils.copyProperties(activityDO, cacheVO);
        BeanUtils.copyProperties(activityCqDO, cacheVO);
        cacheVO.setId(activityCqDO.getId());
        cacheVO.setActivityId(activityId);
        cacheVO.setActivityName(activityDO.getActivityName());
        cacheVO.setActivityRemark(activityDO.getActivityRemark());
        cacheVO.setActivityRules(activityDO.getActivityRules());
        cacheVO.setStartDate(resolveCqStartDate(activityDO, activityCqDO));
        cacheVO.setEndDate(resolveCqEndDate(activityDO, activityCqDO));
        cacheVO.setDayNumberList(parseIntegerList(activityDO.getDayNumbers()));
        cacheVO.setWeekNumberList(parseIntegerList(activityDO.getWeekNumbers()));
        cacheVO.setTimeRangeList(parseStringList(activityDO.getTimeRange()));
        redisCache.setCacheObject(CQKeyConstants.CQ_SETTING + activityId, cacheVO, CQ_SETTING_CACHE_SECONDS, TimeUnit.SECONDS);
    }

    private void refreshCqPrizeCache(Long activityId) {
        if (activityId == null) {
            return;
        }
        List<ActivityPrizeDO> prizeList = activityPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityPrizeDO>()
                .eq(ActivityPrizeDO::getActivityId, activityId)
                .orderByAsc(ActivityPrizeDO::getId));
        redisCache.setCacheObject(CQKeyConstants.CQ_PRIZE + activityId, prizeList, CQ_PRIZE_CACHE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 通用分页查询记录
     */
    private PageResult<ActivityCqLogRespVO> pageLogList(ActivityCqLogPageReqVO reqVO, boolean winningOnly) {
        // 抽签记录和中签记录共用同一套分页逻辑，通过 winningOnly 区分口径。
        PageResult<ActivityCqLogDO> pageResult = activityCqLogMapper.selectPage(reqVO, buildLogQuery(reqVO, winningOnly));
        List<ActivityCqLogRespVO> respVOList = CollectionUtils.isEmpty(pageResult.getList())
                ? Collections.emptyList()
                : pageResult.getList().stream().map(this::convertLogRespVO).collect(Collectors.toList());
        return new PageResult<>(respVOList, pageResult.getTotal());
    }

    /**
     * 分页读取导出的抽签记录数据
     */
    private List<ActivityCqLogExportRespVO> getExportLogData(Page<ActivityCqLogExportRespVO> pageParam,
                                                             QueryWrapper<ActivityCqLogDO> queryWrapper) {
        Page<ActivityCqLogDO> currentPageParam = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        Page<ActivityCqLogDO> resultPage = activityCqLogQueryService.selectPage(currentPageParam, queryWrapper);
        List<ActivityCqLogDO> logList = resultPage.getRecords();
        List<ActivityCqLogExportRespVO> respList = new ArrayList<>(logList.size());
        for (ActivityCqLogDO logDO : logList) {
            ActivityCqLogExportRespVO respVO = new ActivityCqLogExportRespVO();
            respVO.setMemberName(logDO.getMemberName());
            respVO.setMemberMobile(logDO.getMemberMobile());
            respVO.setSignCode(logDO.getSignCode());
            respVO.setObtainTypeName(buildObtainTypeName(logDO.getObtainType()));
            respVO.setDrawTime(logDO.getDrawTime());
            respVO.setStoreName(logDO.getStoreName());
            respVO.setResultStatusName(buildResultStatusName(logDO.getResultStatus()));
            respList.add(respVO);
        }
        return respList;
    }

    /**
     * 分页读取导出的中签记录数据
     */
    private List<ActivityCqWinningExportRespVO> getWinningExportData(Page<ActivityCqWinningExportRespVO> pageParam,
                                                                     QueryWrapper<ActivityCqLogDO> queryWrapper) {
        Page<ActivityCqLogDO> currentPageParam = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        Page<ActivityCqLogDO> resultPage = activityCqLogQueryService.selectPage(currentPageParam, queryWrapper);
        List<ActivityCqLogDO> logList = resultPage.getRecords();
        List<ActivityCqWinningExportRespVO> respList = new ArrayList<>(logList.size());
        for (ActivityCqLogDO logDO : logList) {
            ActivityCqWinningExportRespVO respVO = new ActivityCqWinningExportRespVO();
            respVO.setMemberName(logDO.getMemberName());
            respVO.setMemberMobile(logDO.getMemberMobile());
            respVO.setPrizeTypeName(buildPrizeTypeName(logDO.getPrizeType()));
            respVO.setPrizeContent(logDO.getPrizeContent());
            respVO.setPrizeImgUrl(logDO.getPrizeImgUrl());
            respVO.setRedPacketStatusName(buildRedPacketStatusName(logDO.getClaimStatus()));
            respVO.setReceiveUser(logDO.getReceiveUser());
            respVO.setReceiveMobile(logDO.getReceiveMobile());
            respVO.setReceiveAddress(logDO.getReceiveAddress());
            respVO.setTrackingNumber(logDO.getTrackingNumber());
            respVO.setExpressCompany(logDO.getExpressCompany());
            respVO.setDrawTime(logDO.getDrawTime());
            respVO.setStoreName(logDO.getStoreName());
            respList.add(respVO);
        }
        return respList;
    }

    /**
     * 构建统计结果对象
     */
    private ActivityCqStatisticsRespVO buildStatistics(ActivityCqLogPageReqVO reqVO, boolean winningOnly) {
        LocalDateTime startTime = resolveStartTime(reqVO.getStartTime(), reqVO.getActivityTime());
        LocalDateTime endTime = resolveEndTime(reqVO.getEndTime(), reqVO.getActivityTime());
        if (startTime == null && endTime == null) {
            LocalDateTime[] activityTimeRange = resolveActivityTimeRange(reqVO.getId());
            startTime = activityTimeRange[0];
            endTime = activityTimeRange[1];
            if (endTime != null) {
                endTime = endTime.withSecond(59).withNano(0);
            }
        }
        ActivityCqStatisticsRespVO respVO = new ActivityCqStatisticsRespVO();
        respVO.setCount(countLog(reqVO.getId(), winningOnly, startTime, endTime, reqVO));
        respVO.setParticipation(countDistinctMember(reqVO.getId(), winningOnly, startTime, endTime, reqVO));
        respVO.setWinningParticipation(winningOnly ? respVO.getParticipation() : 0L);
        respVO.setPointsCount(BigDecimal.ZERO);
        respVO.setCouponCount(0L);
        respVO.setTotalCash(BigDecimal.ZERO);
        if (winningOnly) {
            fillWinningPrizeStatistics(respVO, reqVO, startTime, endTime);
        }
        return respVO;
    }

    private LocalDateTime[] resolveActivityTimeRange(Long activityId) {
        if (activityId == null) {
            return new LocalDateTime[]{null, null};
        }
        ActivityDO activityDO = activityService.selectById(activityId);
        ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getActivityId, activityId)
                .last("limit 1"));
        return new LocalDateTime[]{
                toLocalDateTime(resolveCqStartDate(activityDO, activityCqDO)),
                toLocalDateTime(resolveCqEndDate(activityDO, activityCqDO))
        };
    }

    /**
     * 填充中签奖品发放统计。
     */
    private void fillWinningPrizeStatistics(ActivityCqStatisticsRespVO respVO, ActivityCqLogPageReqVO reqVO,
                                            LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<ActivityCqLogDO> wrapper = new QueryWrapper<>();
        applyWinningPrizeStatisticsConditions(wrapper, reqVO, startTime, endTime);
        List<ActivityCqLogDO> winningLogList = activityCqLogMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(winningLogList)) {
            return;
        }

        Set<Long> prizeIdSet = winningLogList.stream()
                .map(ActivityCqLogDO::getPrizeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, ActivityPrizeDO> prizeMap = CollectionUtils.isEmpty(prizeIdSet)
                ? Collections.emptyMap()
                : activityPrizeMapper.selectBatchIds(prizeIdSet).stream()
                .collect(Collectors.toMap(ActivityPrizeDO::getId, item -> item, (left, right) -> left));

        BigDecimal pointsCount = BigDecimal.ZERO;
        BigDecimal totalCash = BigDecimal.ZERO;
        long couponCount = 0L;
        long winningParticipation = winningLogList.stream()
                .map(ActivityCqLogDO::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        for (ActivityCqLogDO logDO : winningLogList) {
            ActivityPrizeDO prizeDO = prizeMap.get(logDO.getPrizeId());
            Integer prizeType = prizeDO != null ? prizeDO.getPrizeType() : logDO.getPrizeType();
            BigDecimal prizeValue = prizeDO != null ? prizeDO.getPrizeValue() : null;

            if (Objects.equals(prizeType, 2) && prizeValue != null) {
                pointsCount = pointsCount.add(prizeValue);
            }
            if ((Objects.equals(prizeType, 1) || Objects.equals(prizeType, 6))
                    && !Objects.equals(logDO.getPrizeState(), 9)) {
                couponCount++;
            }
            if (Objects.equals(prizeType, 5)
                    && Objects.equals(logDO.getClaimStatus(), 2)
                    && prizeValue != null) {
                totalCash = totalCash.add(prizeValue);
            }
        }

        respVO.setWinningParticipation(winningParticipation);
        respVO.setPointsCount(pointsCount);
        respVO.setCouponCount(couponCount);
        respVO.setTotalCash(totalCash);
    }

    /**
     * 构建记录查询条件
     */
    private void applyWinningPrizeStatisticsConditions(QueryWrapper<ActivityCqLogDO> wrapper, ActivityCqLogPageReqVO reqVO,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        if (reqVO.getId() != null) {
            wrapper.lambda().eq(ActivityCqLogDO::getActivityId, reqVO.getId());
        }
        wrapper.lambda().eq(ActivityCqLogDO::getResultStatus, 2);
        if (startTime != null) {
            wrapper.lambda().ge(ActivityCqLogDO::getDrawTime, startTime);
        }
        if (endTime != null) {
            wrapper.lambda().le(ActivityCqLogDO::getDrawTime, endTime);
        }
        if (StringUtils.hasText(reqVO.getMemberName())) {
            wrapper.and(item -> item.like("member_name", reqVO.getMemberName())
                    .or().like("member_mobile", reqVO.getMemberName()));
        }
        wrapper.lambda()
                .in(!CollectionUtils.isEmpty(reqVO.getObtainTypeList()), ActivityCqLogDO::getObtainType, reqVO.getObtainTypeList())
                .eq(reqVO.getPrizeType() != null, ActivityCqLogDO::getPrizeType, reqVO.getPrizeType())
                .in(!CollectionUtils.isEmpty(reqVO.getPrizeTypeList()), ActivityCqLogDO::getPrizeType, reqVO.getPrizeTypeList())
                .in(!CollectionUtils.isEmpty(reqVO.getResultStatusList()), ActivityCqLogDO::getResultStatus, reqVO.getResultStatusList())
                .in(!CollectionUtils.isEmpty(reqVO.getRedPacketStatusList()), ActivityCqLogDO::getClaimStatus, reqVO.getRedPacketStatusList())
                .in(!CollectionUtils.isEmpty(reqVO.getPrizeStateList()), ActivityCqLogDO::getPrizeState, reqVO.getPrizeStateList())
                .eq(reqVO.getStoreId() != null, ActivityCqLogDO::getStoreId, reqVO.getStoreId())
                .eq(reqVO.getClaimStatus() != null, ActivityCqLogDO::getClaimStatus, reqVO.getClaimStatus())
                .in(!CollectionUtils.isEmpty(reqVO.getStoreIdList()), ActivityCqLogDO::getStoreId, reqVO.getStoreIdList());
        applyReceiveAddressCondition(wrapper, reqVO.getReceiveAddressType());
        applyTrackingNumberCondition(wrapper, reqVO.getTrackingType());
    }

    private QueryWrapper<ActivityCqLogDO> buildLogQuery(ActivityCqLogPageReqVO reqVO, boolean winningOnly) {
        QueryWrapper<ActivityCqLogDO> wrapper = new QueryWrapper<>();
        applyCountConditions(wrapper, reqVO.getId(), winningOnly,
                resolveStartTime(reqVO.getStartTime(), reqVO.getActivityTime()),
                resolveEndTime(reqVO.getEndTime(), reqVO.getActivityTime()), reqVO);
        wrapper.lambda().orderByDesc(ActivityCqLogDO::getDrawTime, ActivityCqLogDO::getId);
        return wrapper;
    }

    /**
     * 构建导出查询条件
     */
    private QueryWrapper<ActivityCqLogDO> buildExportLogQuery(ActivityCqLogExportReqVO reqVO, boolean winningOnly) {
        QueryWrapper<ActivityCqLogDO> wrapper = new QueryWrapper<>();
        applyExportConditions(wrapper, reqVO, winningOnly);
        wrapper.lambda().orderByDesc(ActivityCqLogDO::getDrawTime, ActivityCqLogDO::getId);
        return wrapper;
    }

    /**
     * 统计记录总数
     */
    private Long countLog(Long activityId, boolean winningOnly, LocalDateTime startTime, LocalDateTime endTime) {
        return countLog(activityId, winningOnly, startTime, endTime, null);
    }

    /**
     * 按条件统计记录总数
     */
    private Long countLog(Long activityId, boolean winningOnly, LocalDateTime startTime, LocalDateTime endTime,
                          ActivityCqLogPageReqVO reqVO) {
        QueryWrapper<ActivityCqLogDO> wrapper = new QueryWrapper<>();
        applyCountConditions(wrapper, activityId, winningOnly, startTime, endTime, reqVO);
        return activityCqLogMapper.selectCount(wrapper);
    }

    /**
     * 统计去重后的参与人数
     */
    private Long countDistinctMember(Long activityId, boolean winningOnly, LocalDateTime startTime, LocalDateTime endTime) {
        return countDistinctMember(activityId, winningOnly, startTime, endTime, null);
    }

    /**
     * 按条件统计去重后的参与人数
     */
    private Long countDistinctMember(Long activityId, boolean winningOnly, LocalDateTime startTime, LocalDateTime endTime,
                                     ActivityCqLogPageReqVO reqVO) {
        QueryWrapper<ActivityCqLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT member_id) AS cnt");
        applyCountConditions(wrapper, activityId, winningOnly, startTime, endTime, reqVO);
        List<Map<String, Object>> resultList = activityCqLogMapper.selectMaps(wrapper);
        if (CollectionUtils.isEmpty(resultList) || CollectionUtils.isEmpty(resultList.get(0))) {
            return 0L;
        }
        Object countObj = resultList.get(0).values().iterator().next();
        if (countObj == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(countObj));
    }

    /**
     * 统一拼装记录查询条件
     */
    private void applyCountConditions(QueryWrapper<ActivityCqLogDO> wrapper, Long activityId, boolean winningOnly,
                                      LocalDateTime startTime, LocalDateTime endTime, ActivityCqLogPageReqVO reqVO) {
        if (activityId != null) {
            wrapper.lambda().eq(ActivityCqLogDO::getActivityId, activityId)
                    .isNotNull(ActivityCqLogDO::getMemberMobile)
                    .ne(ActivityCqLogDO::getMemberMobile, "");
        }
        if (winningOnly) {
            wrapper.lambda().eq(ActivityCqLogDO::getResultStatus, 2);
        }
        if (startTime != null) {
            wrapper.lambda().ge(ActivityCqLogDO::getDrawTime, startTime);
        }
        if (endTime != null) {
            wrapper.lambda().le(ActivityCqLogDO::getDrawTime, endTime);
        }

        if (reqVO == null) {
            return;
        }
        if (StringUtils.hasText(reqVO.getMemberName())) {
            wrapper.eq("member_mobile", reqVO.getMemberName());
        }
        wrapper.lambda()
                .in(!CollectionUtils.isEmpty(reqVO.getObtainTypeList()), ActivityCqLogDO::getObtainType, reqVO.getObtainTypeList())
                .eq(reqVO.getPrizeType() != null, ActivityCqLogDO::getPrizeType, reqVO.getPrizeType())
                .in(!CollectionUtils.isEmpty(reqVO.getPrizeTypeList()), ActivityCqLogDO::getPrizeType, reqVO.getPrizeTypeList())
                .in(!CollectionUtils.isEmpty(reqVO.getResultStatusList()), ActivityCqLogDO::getResultStatus, reqVO.getResultStatusList())
                .in(!CollectionUtils.isEmpty(reqVO.getRedPacketStatusList()), ActivityCqLogDO::getClaimStatus, reqVO.getRedPacketStatusList())
                .in(!CollectionUtils.isEmpty(reqVO.getPrizeStateList()), ActivityCqLogDO::getPrizeState, reqVO.getPrizeStateList())
                .eq(reqVO.getStoreId() != null, ActivityCqLogDO::getStoreId, reqVO.getStoreId())
                .eq(reqVO.getClaimStatus() != null, ActivityCqLogDO::getClaimStatus, reqVO.getClaimStatus())
                .in(!CollectionUtils.isEmpty(reqVO.getStoreIdList()), ActivityCqLogDO::getStoreId, reqVO.getStoreIdList());
        applyReceiveAddressCondition(wrapper, reqVO.getReceiveAddressType());
        applyTrackingNumberCondition(wrapper, reqVO.getTrackingType());
    }

    /**
     * 统一拼装导出查询条件
     */
    private void applyExportConditions(QueryWrapper<ActivityCqLogDO> wrapper, ActivityCqLogExportReqVO reqVO, boolean winningOnly) {
        if (reqVO.getId() != null) {
            wrapper.lambda().eq(ActivityCqLogDO::getActivityId, reqVO.getId());
        }
        if (winningOnly) {
            wrapper.lambda().eq(ActivityCqLogDO::getResultStatus, 2)
                    .isNotNull(ActivityCqLogDO::getMemberMobile)
                    .ne(ActivityCqLogDO::getMemberMobile, "");
        }
        LocalDateTime startTime = resolveStartTime(reqVO.getStartTime(), reqVO.getActivityTime());
        LocalDateTime endTime = resolveEndTime(reqVO.getEndTime(), reqVO.getActivityTime());
        if (startTime != null) {
            wrapper.lambda().ge(ActivityCqLogDO::getDrawTime, startTime);
        }
        if (endTime != null) {
            wrapper.lambda().le(ActivityCqLogDO::getDrawTime, endTime);
        }
        if (StringUtils.hasText(reqVO.getMemberName())) {
            wrapper.and(item -> item.like("member_name", reqVO.getMemberName())
                    .or().like("member_mobile", reqVO.getMemberName()));
        }
        wrapper.lambda()
                .in(!CollectionUtils.isEmpty(reqVO.getObtainTypeList()), ActivityCqLogDO::getObtainType, reqVO.getObtainTypeList())
                .eq(reqVO.getPrizeType() != null, ActivityCqLogDO::getPrizeType, reqVO.getPrizeType())
                .in(!CollectionUtils.isEmpty(reqVO.getPrizeTypeList()), ActivityCqLogDO::getPrizeType, reqVO.getPrizeTypeList())
                .in(!CollectionUtils.isEmpty(reqVO.getResultStatusList()), ActivityCqLogDO::getResultStatus, reqVO.getResultStatusList())
                .in(!CollectionUtils.isEmpty(reqVO.getRedPacketStatusList()), ActivityCqLogDO::getClaimStatus, reqVO.getRedPacketStatusList())
                .in(!CollectionUtils.isEmpty(reqVO.getPrizeStateList()), ActivityCqLogDO::getPrizeState, reqVO.getPrizeStateList())
                .eq(reqVO.getStoreId() != null, ActivityCqLogDO::getStoreId, reqVO.getStoreId())
                .in(!CollectionUtils.isEmpty(reqVO.getStoreIdList()), ActivityCqLogDO::getStoreId, reqVO.getStoreIdList());
        applyReceiveAddressCondition(wrapper, reqVO.getReceiveAddressType());
        applyTrackingNumberCondition(wrapper, reqVO.getTrackingType());
    }

    /**
     * 解析开始时间，优先使用显式开始时间，其次兼容时间范围数组。
     */
    private LocalDateTime resolveStartTime(LocalDateTime startTime, List<LocalDateTime> activityTime) {
        if (startTime != null) {
            return startTime;
        }
        if (!CollectionUtils.isEmpty(activityTime)) {
            return activityTime.get(0);
        }
        return null;
    }

    /**
     * 解析结束时间，优先使用显式结束时间，其次兼容时间范围数组。
     */
    private LocalDateTime resolveEndTime(LocalDateTime endTime, List<LocalDateTime> activityTime) {
        if (endTime != null) {
            return endTime;
        }
        if (!CollectionUtils.isEmpty(activityTime) && activityTime.size() > 1) {
            return activityTime.get(1);
        }
        return null;
    }

    /**
     * 收货地址筛选。
     * 1 未填写
     * 2 已填写
     */
    private void applyReceiveAddressCondition(QueryWrapper<ActivityCqLogDO> wrapper, Integer receiveAddressType) {
        if (receiveAddressType == null || receiveAddressType == 0) {
            return;
        }
        if (Objects.equals(receiveAddressType, 1)) {
            wrapper.and(item -> item.isNull("receive_address").or().eq("receive_address", ""));
            return;
        }
        if (Objects.equals(receiveAddressType, 2)) {
            wrapper.and(item -> item.isNotNull("receive_address").ne("receive_address", ""));
        }
    }

    /**
     * 物流单号筛选。
     * 1 未填写
     * 2 已填写
     */
    private void applyTrackingNumberCondition(QueryWrapper<ActivityCqLogDO> wrapper, Integer trackingType) {
        if (trackingType == null || trackingType == 0) {
            return;
        }
        if (Objects.equals(trackingType, 1)) {
            wrapper.and(item -> item.isNull("tracking_number").or().eq("tracking_number", ""));
            return;
        }
        if (Objects.equals(trackingType, 2)) {
            wrapper.and(item -> item.isNotNull("tracking_number").ne("tracking_number", ""));
        }
    }

    /**
     * 转换记录返回对象
     */
    private ActivityCqLogRespVO convertLogRespVO(ActivityCqLogDO logDO) {
        ActivityCqLogRespVO respVO = new ActivityCqLogRespVO();
        BeanUtils.copyProperties(logDO, respVO);
        respVO.setObtainTypeName(buildObtainTypeName(logDO.getObtainType()));
        respVO.setResultStatusName(buildResultStatusName(logDO.getResultStatus()));
        respVO.setPrizeTypeName(buildPrizeTypeName(logDO.getPrizeType()));
        respVO.setRedPacketStatus(logDO.getClaimStatus());
        respVO.setRedPacketStatusName(buildRedPacketStatusName(logDO.getClaimStatus()));
        return respVO;
    }

    /**
     * 构建主活动数据对象
     */
    private ActivityDO buildActivityDO(ActivityCqSaveOrUpdateReqVO reqVO) {
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(reqVO.getActivityName());
        activityDO.setActivityType(reqVO.getActivityType());
        activityDO.setActivityRemark(reqVO.getActivityRemark());
        activityDO.setActivityRules(reqVO.getActivityRules());
        activityDO.setActivityStore(reqVO.getActivityStore());
        activityDO.setStartDate(reqVO.getStartDate() == null ? null : new Date(reqVO.getStartDate().getTime()));
        activityDO.setEndDate(reqVO.getEndDate() == null ? null : new Date(reqVO.getEndDate().getTime()));
        activityDO.setDayNumbers(joinIntegerList(reqVO.getDayNumberList()));
        activityDO.setWeekNumbers(joinIntegerList(reqVO.getWeekNumberList()));
        activityDO.setTimeRange(joinStringList(reqVO.getTimeRangeList()));
        activityDO.setCommunityFlag(reqVO.getCommunityFlag());
        activityDO.setGuideImage(reqVO.getGuideImage());
        activityDO.setIsEnabled(reqVO.getIsEnabled());
        return activityDO;
    }

    /**
     * 构建抽签活动子表数据对象
     */
    private ActivityCqDO buildActivityCqDO(ActivityCqSaveOrUpdateReqVO reqVO) {
        ActivityCqDO activityCqDO = new ActivityCqDO();
        activityCqDO.setActivityCoverImage(reqVO.getActivityCoverImage());
        activityCqDO.setActivityBackgroundImage(reqVO.getActivityBackgroundImage());
        activityCqDO.setActivityDetailsImage(reqVO.getActivityDetailsImage());
        activityCqDO.setButtonBackgroundColor(reqVO.getButtonBackgroundColor());
        activityCqDO.setShareTitle(reqVO.getShareTitle());
        activityCqDO.setShareNote(reqVO.getShareNote());
        activityCqDO.setShareImgUrl(reqVO.getShareImgUrl());
        activityCqDO.setDailyAttendance(reqVO.getDailyAttendance());
        activityCqDO.setDailyAttenCount(reqVO.getDailyAttenCount());
        activityCqDO.setFreeEvent(reqVO.getFreeEvent());
        activityCqDO.setFreeCount(reqVO.getFreeCount());
        activityCqDO.setPlaceOrderStatus(reqVO.getPlaceOrderStatus());
        activityCqDO.setPlaceOrderType(reqVO.getPlaceOrderType());
        activityCqDO.setCategoryType(reqVO.getCategoryType());
        activityCqDO.setPlaceOrderProduct(reqVO.getPlaceOrderProduct());
        activityCqDO.setPaymentThreshold(reqVO.getPaymentThreshold());
        activityCqDO.setPaymentCount(reqVO.getPaymentCount());
        activityCqDO.setPlaceOrderCode(reqVO.getPlaceOrderCode());
        activityCqDO.setPlaceOrderCodeNumber(reqVO.getPlaceOrderCodeNumber());
        activityCqDO.setShareEvent(reqVO.getShareEvent());
        activityCqDO.setShareCount(reqVO.getShareCount());
        activityCqDO.setBrowseHomeEvent(reqVO.getBrowseHomeEvent());
        activityCqDO.setBrowseHomeCount(reqVO.getBrowseHomeCount());
        activityCqDO.setStartDateTime(toLocalDateTime(reqVO.getStartDate()));
        activityCqDO.setEndDateTime(toLocalDateTime(reqVO.getEndDate()));
        activityCqDO.setResultPublishTime(toLocalDateTime(reqVO.getResultPublishTime()));
        activityCqDO.setDrawStatus(reqVO.getDrawStatus());
        activityCqDO.setPublicButton(reqVO.getPublicButton());
        activityCqDO.setShareType(reqVO.getShareType());
        return activityCqDO;
    }

    private Date resolveCqStartDate(ActivityDO activityDO, ActivityCqDO activityCqDO) {
        if (activityCqDO != null && activityCqDO.getStartDateTime() != null) {
            return toDate(activityCqDO.getStartDateTime());
        }
        return activityDO == null ? null : activityDO.getStartDate();
    }

    private Date resolveCqEndDate(ActivityDO activityDO, ActivityCqDO activityCqDO) {
        if (activityCqDO != null && activityCqDO.getEndDateTime() != null) {
            return toDate(activityCqDO.getEndDateTime());
        }
        return activityDO == null ? null : activityDO.getEndDate();
    }

    /**
     * 同步活动门店范围
     */
    private void syncStores(Long activityId, List<Long> storeIds) {
        // 门店关系直接按当前提交结果全量覆盖，避免后台反复编辑后残留旧绑定关系。
        activityStoreService.deleteByActivityId(activityId);
        if (CollectionUtils.isEmpty(storeIds)) {
            return;
        }
        activityStoreService.createBatch(new ArrayList<>(new LinkedHashSet<>(storeIds)), activityId);
    }

    /**
     * 同步活动商品范围
     */
    private void syncCommodities(Long activityId, List<Long> commodityIds) {
        // 商品范围同样走全量重建，逻辑简单，也更适合后台配置场景。
        activityCommodityMapper.delete(new LambdaQueryWrapperX<ActivityCommodityDO>().eq(ActivityCommodityDO::getActivityId, activityId));
        if (CollectionUtils.isEmpty(commodityIds)) {
            return;
        }
        List<Long> distinctCommodityIds = new ArrayList<>(new LinkedHashSet<>(commodityIds));
        for (Long commodityId : distinctCommodityIds) {
            ActivityCommodityDO commodityDO = new ActivityCommodityDO();
            commodityDO.setActivityId(activityId);
            commodityDO.setCommodityId(commodityId);
            activityCommodityMapper.insert(commodityDO);
        }
    }

    /**
     * 同步奖品配置
     */
    private void syncPrizes(Long activityId, List<ActivityPrizeDO> existingPrizeList, List<ActivityCqPrizeReqVO> prizeReqVOList) {
        // 奖品同步分三步：删掉前端已移除的、更新已有的、新增本次新配的。
        List<ActivityCqPrizeReqVO> safePrizeList = CollectionUtils.isEmpty(prizeReqVOList) ? Collections.emptyList() : prizeReqVOList;
        Map<Long, ActivityPrizeDO> existingPrizeMap = CollectionUtils.isEmpty(existingPrizeList)
                ? Collections.emptyMap()
                : existingPrizeList.stream().filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ActivityPrizeDO::getId, item -> item));
        Set<Long> currentIds = safePrizeList.stream().map(ActivityCqPrizeReqVO::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(existingPrizeList)) {
            existingPrizeList.stream()
                    .map(ActivityPrizeDO::getId)
                    .filter(Objects::nonNull)
                    .filter(id -> !currentIds.contains(id))
                    .forEach(activityPrizeMapper::deleteById);
        }
        for (ActivityCqPrizeReqVO prizeReqVO : safePrizeList) {
            ActivityPrizeDO prizeDO = new ActivityPrizeDO();
            prizeDO.setActivityId(activityId);
            prizeDO.setPrizeType(prizeReqVO.getPrizeType());
            prizeDO.setPrizeName(prizeReqVO.getPrizeName());
            prizeDO.setPrizeNum(prizeReqVO.getPrizeNum());
            prizeDO.setPrizeValue(prizeReqVO.getPrizeValue());
            prizeDO.setProbability(prizeReqVO.getProbability());
            prizeDO.setPrizeImgUrl(prizeReqVO.getPrizeImgUrl());
            prizeDO.setAwardId(prizeReqVO.getAwardId());
            prizeDO.setRemainNum(prizeReqVO.getRemainNum() == null ? 0 : prizeReqVO.getRemainNum());
            prizeDO.setWinningCitys(convertListToString(prizeReqVO.getWinningCitys()));
            prizeDO.setCouponName(prizeReqVO.getCouponName());
            prizeDO.setIsGuarantees(prizeReqVO.getIsGuarantees());
            prizeDO.setMinimumNumber(prizeReqVO.getMinimumNumber());
            if (prizeReqVO.getId() != null && existingPrizeMap.containsKey(prizeReqVO.getId())) {
                prizeDO.setId(prizeReqVO.getId());
                activityPrizeMapper.updateById(prizeDO);
            } else {
                prizeDO.setId(null);
                activityPrizeMapper.insert(prizeDO);
            }
        }
    }

    /**
     * 将奖品 DO 转为前端配置对象
     */
    private ActivityCqPrizeReqVO convertPrizeReqVO(ActivityPrizeDO prizeDO) {
        ActivityCqPrizeReqVO reqVO = new ActivityCqPrizeReqVO();
        BeanUtils.copyProperties(prizeDO, reqVO);
        return reqVO;
    }

    /**
     * 解析逗号分隔的整数列表
     */
    private List<Integer> parseIntegerList(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(value.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 解析逗号分隔的字符串列表
     */
    private List<String> parseStringList(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(value.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 将整数列表拼成逗号分隔字符串
     */
    private String joinIntegerList(List<Integer> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 将字符串列表拼成逗号分隔字符串
     */
    private String joinStringList(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.joining(","));
    }

    /**
     * 查询活动商品详情列表
     */
    private List<CommodityDTO> getCommodityDTOList(List<Long> commodityIds) {
        if (CollectionUtils.isEmpty(commodityIds)) {
            return new ArrayList<>();
        }
        CommonResult<List<CommodityDTO>> commodityResult = commodityApi.getCommodityList(commodityIds);
        if (commodityResult == null || CollectionUtils.isEmpty(commodityResult.getData())) {
            return new ArrayList<>();
        }
        return commodityResult.getData();
    }

    /**
     * Date 转 LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZONE_ID);
    }

    /**
     * LocalDateTime 转 Date
     */
    private Date toDate(LocalDateTime dateTime) {
        return dateTime == null ? null : Date.from(dateTime.atZone(ZONE_ID).toInstant());
    }

    /**
     * 构建获取方式名称
     */
    private String buildObtainTypeName(Integer obtainType) {
        if (obtainType == null) {
            return "";
        }
        return switch (obtainType) {
            case 1 -> "每日签到";
            case 2 -> "下单获得";
            case 3 -> "分享活动";
            case 4 -> "免费集签";
            case 5 -> "浏览首页";
            default -> "未知";
        };
    }

    /**
     * 构建结果状态名称
     */
    private String buildResultStatusName(Integer resultStatus) {
        if (resultStatus == null) {
            return "";
        }
        return switch (resultStatus) {
            case 0 -> "待开奖";
            case 1 -> "未中签";
            case 2 -> "已中签";
            default -> "未知";
        };
    }

    /**
     * 构建奖品类型名称
     */
    private String buildPrizeTypeName(Integer prizeType) {
        if (prizeType == null) {
            return "";
        }
        return ActivityCqPrizeTypeEnum.getDescriptionByCode(prizeType);
    }

    /**
     * 构建红包状态名称
     */
    private String buildRedPacketStatusName(Integer redPacketStatus) {
        if (redPacketStatus == null) {
            return "";
        }
        return switch (redPacketStatus) {
            case 1 -> "未领取";
            case 2 -> "已领取";
            case 3 -> "已失效";
            default -> "未知";
        };
    }

    /**
     * 构建导出文件名
     */
    private String buildLogExportFileName(ActivityCqLogExportReqVO reqVO, boolean winningOnly) {
        StringBuilder fileName = new StringBuilder(winningOnly ? "中签记录" : "抽签记录");
        if (reqVO.getId() != null) {
            fileName.append("_活动").append(reqVO.getId());
        }
        fileName.append("_").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        return fileName.toString();
    }
    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
