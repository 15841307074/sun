package com.htyoudao.youdao.module.promotion.service.activityVote;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteOptionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteOptionDeleteLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardDeleteLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteOptionDeleteLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteOptionMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteRewardDeleteLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteRewardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteRewardMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.redis.VoteKeyConstants;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@DS(DsNameConstants.SHARDING)
@RefreshScope
public class ActivityVoteServiceImpl extends ServiceImpl<ActivityVoteMapper, ActivityVoteDO> implements ActivityVoteService {

    private static final int VOTE_SETTING_CACHE_SECONDS = 300;
    /** pipeline批量操作大小（每批1000条） */
    private static final int BATCH_SIZE = 1000;

    @Resource
    private ActivityVoteMapper activityVoteMapper;
    @Resource
    private ActivityVoteOptionMapper activityVoteOptionMapper;
    @Resource
    private ActivityVoteRewardMapper activityVoteRewardMapper;
    @Resource
    private ActivityVoteRewardLogMapper activityVoteRewardLogMapper;
    @Resource
    private GoodCouponPackageMapper goodCouponPackageMapper;
    @Resource
    private ActivityVoteLogMapper activityVoteLogMapper;
    @Resource
    private ActivityVoteOptionDeleteLogMapper activityVoteOptionDeleteLogMapper;
    @Resource
    private ActivityVoteRewardDeleteLogMapper activityVoteRewardDeleteLogMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityService activityService;
    @Resource
    private ActivityStoreService activityStoreService;
    @Resource
    private ActivityChannelService activityChannelService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @DubboReference
    private StoreApi storeApi;
    @DubboReference
    private WxMemberApi wxMemberApi;
    @Resource
    private IEventService eventService;
    @Resource
    private ExcelActionService<ActivityVoteLogExportRespVO> voteLogExcelActionService;
    @Resource
    private ExcelActionService<ActivityVoteRewardLogExportRespVO> voteRewardLogExcelActionService;

    // 分享默认配置（yml兜底，PC未配时使用）
    @Value("${activity.vote.shareImageUrl:}")
    private String defaultShareImageUrl;
    @Value("${activity.vote.shareTitle:}")
    private String defaultShareTitle;
    @Value("${activity.vote.shareDescription:}")
    private String defaultShareDescription;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> createActivityVote(ActivityVoteSaveOrUpdateReqVO reqVO) {
        // 0. 参数校验（create与copy均经过此入口，两处同时生效）
        validateSaveOrUpdateReqVO(reqVO);

        // 1. 插入基础活动表（内部已校验名称唯一）
        ActivityDO activityDO = buildActivityDO(reqVO);
        activityDO.setActivityType(ActivityTypeEnum.VOTE.getCode());
        Long activityId = activityService.createActivity(activityDO);

        // 3. 插入投票扩展表
        ActivityVoteDO voteDO = buildActivityVoteDO(reqVO);
        voteDO.setActivityId(activityId);
        activityVoteMapper.insert(voteDO);

        // 4. 同步门店
        syncStores(activityId, reqVO.getActivityStore(), reqVO.getStoreIds());

        // 5. 同步投票选项
        syncOptions(activityId, Collections.emptyList(), reqVO.getOptionList());

        // 6. 同步奖励
        syncRewards(activityId, Collections.emptyList(), reqVO.getRewardList());

        // 7. 创建推广渠道
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.VOTE.getCode());

        return success(1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> updateActivityVote(ActivityVoteSaveOrUpdateReqVO reqVO) {
        // 0. 参数校验（与create/copy保持一致）
        validateSaveOrUpdateReqVO(reqVO);

        // 1. 解析activityId
        Long activityId = reqVO.getActivityId();
        if (activityId == null && reqVO.getId() != null) {
            ActivityVoteDO current = activityVoteMapper.selectById(reqVO.getId());
            if (current != null) {
                activityId = current.getActivityId();
            }
        }
        if (activityId == null) {
            throw exception(VOTE_IS_NOT_ID);
        }

        // 2. 校验活动状态（启用中不允许修改）
        ActivityDO existActivity = activityService.selectById(activityId);
        if (existActivity == null) {
            throw exception(VOTE_NOT_EXISTS);
        }
        if (Objects.equals(existActivity.getIsEnabled(), 1)) {
            throw exception(VOTE_IS_ENABLED);
        }

        // 3. 更新基础活动表（内部已校验名称唯一）
        ActivityDO activityDO = buildActivityDO(reqVO);
        activityDO.setId(activityId);
        activityDO.setIsEnabled(0);
        activityService.updateActivity(activityDO);

        // 5. 更新投票扩展表
        ActivityVoteDO existVote = activityVoteMapper.selectOne(
                new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, activityId));
        ActivityVoteDO updateVoteDO = buildActivityVoteDO(reqVO);
        if (existVote == null) {
            updateVoteDO.setActivityId(activityId);
            activityVoteMapper.insert(updateVoteDO);
        } else {
            updateVoteDO.setId(existVote.getId());
            updateVoteDO.setActivityId(activityId);
            activityVoteMapper.updateById(updateVoteDO);
        }

        // 6. 同步门店
        syncStores(activityId, reqVO.getActivityStore(), reqVO.getStoreIds());

        // 7. 同步选项（全量替换）
        List<ActivityVoteOptionDO> existOptions = activityVoteOptionMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, activityId));
        syncOptions(activityId, existOptions, reqVO.getOptionList());

        // 8. 同步奖励（全量替换）
        List<ActivityVoteRewardDO> existRewards = activityVoteRewardMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, activityId));
        syncRewards(activityId, existRewards, reqVO.getRewardList());

        // 9. 清除缓存（活动已禁用，会员级key已在禁用时清理，此处只删固定key）
        clearFixedVoteCaches(activityId);

        return success(1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> deleteActivityVote(Long id) {
        // 1. 校验活动状态
        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO == null) {
            throw exception(VOTE_NOT_EXISTS);
        }
        if (Objects.equals(activityDO.getIsEnabled(), 1)) {
            throw exception(VOTE_IS_USE);
        }

        // 2. 删除基础活动表
        activityService.deleteActivity(id);

        // 3. 删除扩展表
        activityVoteMapper.delete(new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, id));
        activityVoteOptionMapper.delete(new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, id));
        activityVoteRewardMapper.delete(new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, id));
        activityVoteRewardLogMapper.delete(new LambdaQueryWrapper<ActivityVoteRewardLogDO>().eq(ActivityVoteRewardLogDO::getActivityId, id));
        activityVoteLogMapper.delete(new LambdaQueryWrapper<ActivityVoteLogDO>().eq(ActivityVoteLogDO::getActivityId, id));

        // 4. 删除门店关联
        activityStoreService.deleteByActivityId(id);

        // 5. 删除推广渠道
        activityChannelService.deleteByActivityId(id);

        // 6. 清除缓存（活动已禁用，会员级key已在禁用时清理，此处只删固定key）
        clearFixedVoteCaches(id);

        return success(1);
    }

    @Override
    public ActivityVoteDetailRespVO selectDetail(Long id) {
        ActivityDO activityDO = activityService.selectById(id);
        if (activityDO == null) {
            throw exception(VOTE_NOT_EXISTS);
        }
        ActivityVoteDO voteDO = activityVoteMapper.selectOne(
                new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, id));

        ActivityVoteDetailRespVO respVO = new ActivityVoteDetailRespVO();
        // 基础活动信息
        respVO.setActivityId(id);
        respVO.setActivityName(activityDO.getActivityName());
        respVO.setActivityType(activityDO.getActivityType());
        respVO.setIsEnabled(activityDO.getIsEnabled());
        respVO.setStartDate(toLdt(activityDO.getStartDate()));
        respVO.setEndDate(toLdt(activityDO.getEndDate()));
        respVO.setActivityRemark(activityDO.getActivityRemark());
        respVO.setActivityRules(activityDO.getActivityRules());
        respVO.setActivityStore(activityDO.getActivityStore());
        respVO.setActivityStatus(getActivityStatus(activityDO.getIsEnabled(), activityDO.getStartDate(), activityDO.getEndDate()));

        // 投票扩展信息
        if (voteDO != null) {
            respVO.setId(voteDO.getId());
            respVO.setActivityImgUrl(voteDO.getActivityImgUrl());
            respVO.setActivityBackground(voteDO.getActivityBackground());
            respVO.setVoteBackground(voteDO.getVoteBackground());
            respVO.setRankingBackground(voteDO.getRankingBackground());
            respVO.setBackgroundColor(voteDO.getBackgroundColor());
            respVO.setVoteCountFlag(voteDO.getVoteCountFlag());
            respVO.setVoteCount(voteDO.getVoteCount());
            respVO.setVoteBtnTitle(voteDO.getVoteBtnTitle());
            respVO.setShareTitle(voteDO.getShareTitle());
            respVO.setShareNote(voteDO.getShareNote());
            respVO.setShareImgUrl(voteDO.getShareImgUrl());
            respVO.setShareType(voteDO.getShareType());
            respVO.setPublicButton(voteDO.getPublicButton());
            respVO.setCommunityFlag(voteDO.getCommunityFlag());
            respVO.setGuideImage(voteDO.getGuideImage());
        }

        // 门店
        List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(id);
        respVO.setStoreIds(storeIds);
        if (CollUtil.isNotEmpty(storeIds)) {
            List<StoreSimpleResDto> storeList = storeApi.getStoreSimpleResDtoList(storeIds).getCheckedData();
            if (CollUtil.isNotEmpty(storeList)) {
                respVO.setStoreInfoDTOS(storeList);
            }
        }

        // 选项（含票数）
        List<ActivityVoteOptionDO> options = activityVoteOptionMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, id));
        List<ActivityVoteDetailRespVO.OptionDetail> optionDetails = new ArrayList<>();
        for (ActivityVoteOptionDO option : options) {
            ActivityVoteDetailRespVO.OptionDetail detail = new ActivityVoteDetailRespVO.OptionDetail();
            detail.setId(option.getId());
            detail.setOptionName(option.getOptionName());
            detail.setOptionUrl(option.getOptionUrl());
            detail.setOptionDetailUrl(option.getOptionDetailUrl());
            detail.setVoteDetail(option.getVoteDetail());
            // 统计票数：从Redis计数器读取（活动未启用时计数器不存在，默认0）
            Object countObj = redisCache.getCacheObject(VoteKeyConstants.VOTE_OPTION_COUNT + id + ":" + option.getId());
            long voteNum = countObj != null ? Long.parseLong(countObj.toString()) : 0L;
            detail.setVoteNum(voteNum);
            optionDetails.add(detail);
        }
        respVO.setOptionList(optionDetails);

        // 奖励
        List<ActivityVoteRewardDO> rewards = activityVoteRewardMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, id));
        List<ActivityVoteDetailRespVO.RewardDetail> rewardDetails = rewards.stream().map(r -> {
            ActivityVoteDetailRespVO.RewardDetail rd = new ActivityVoteDetailRespVO.RewardDetail();
            BeanUtil.copyProperties(r, rd);
            return rd;
        }).collect(Collectors.toList());
        respVO.setRewardList(rewardDetails);

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> copyActivityVote(ActivityVoteSaveOrUpdateReqVO reqVO) {
        // 复制时清除id，作为新活动创建
        reqVO.setId(null);
        reqVO.setActivityId(null);
        reqVO.setIsEnabled(0);
        if (reqVO.getOptionList() != null) {
            reqVO.getOptionList().forEach(o -> o.setId(null));
        }
        if (reqVO.getRewardList() != null) {
            reqVO.getRewardList().forEach(r -> r.setId(null));
        }
        return createActivityVote(reqVO);
    }

    @Override
    public ActivityVoteAnalysisRespVO getActivityVoteAnalysis(String id) {
        Long activityId = Long.parseLong(id);
        ActivityVoteAnalysisRespVO respVO = new ActivityVoteAnalysisRespVO();

        // PV/UV/分享（ES事件日志，与抽签模块一致）
        respVO.setTotalPv(0L);
        respVO.setTotalUv(0L);
        respVO.setShareCount(0L);
        if (eventService != null) {
            ActivityDO activityDO = activityService.selectById(activityId);
            LocalDateTime start = activityDO != null ? toLdt(activityDO.getStartDate()) : null;
            LocalDateTime end = activityDO != null ? toLdt(activityDO.getEndDate()) : null;
            if (end != null) {
                end = end.withHour(23).withMinute(59).withSecond(59).withNano(0);
            }
            Long businessId = BusinessContextHolder.getBusinessId();
            Map<String, Long> pvUvMap = eventService.statPvUv(EventType.VOTE, id, start, end, businessId);
            if (pvUvMap != null && !pvUvMap.isEmpty()) {
                respVO.setTotalPv(pvUvMap.getOrDefault("pv", 0L));
                respVO.setTotalUv(pvUvMap.getOrDefault("uv", 0L));
            }
            Long shareCount = eventService.statUv(EventType.VOTE_SHARE, id, start, end, businessId);
            respVO.setShareCount(shareCount != null ? shareCount : 0L);
        }

        // 总投票次数（DB）
        Long totalVotes = activityVoteLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteLogDO>().eq(ActivityVoteLogDO::getActivityId, activityId));
        respVO.setTotalVotes(totalVotes);

        // 参与人数（DB侧 COUNT(DISTINCT member_id) 聚合下推，避免分片表明细全拉回内存仅取行数）
        QueryWrapper<ActivityVoteLogDO> participantWrapper = new QueryWrapper<>();
        participantWrapper.select("COUNT(DISTINCT member_id) AS cnt")
                .eq("activity_id", activityId);
        respVO.setParticipants(parseAggLong(activityVoteLogMapper.selectMaps(participantWrapper), "cnt"));

        // 今日投票（DB）
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayVotes = activityVoteLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteLogDO>()
                        .eq(ActivityVoteLogDO::getActivityId, activityId)
                        .ge(ActivityVoteLogDO::getVoteTime, todayStart));
        respVO.setTodayVotes(todayVotes);

        // 奖励发放统计（DB，按prizeType分类）
        Long rewardCount = activityVoteRewardLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteRewardLogDO>().eq(ActivityVoteRewardLogDO::getActivityId, activityId));
        respVO.setRewardCount(rewardCount);

        // 发放积分（prizeType=1，DB侧 SUM 聚合下推）
        respVO.setTotalPoints(sumRewardPrizeValue(activityId, 1));

        // 发放优惠券张数（prizeType=2优惠券按发放次数计 + prizeType=3优惠券包按包内张数×发放次数展开）
        Long couponCount = activityVoteRewardLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                        .eq(ActivityVoteRewardLogDO::getActivityId, activityId)
                        .eq(ActivityVoteRewardLogDO::getPrizeType, 2));
        respVO.setCouponCount(couponCount + statCouponPackageGrantCount(activityId));

        // 发放红包金额（prizeType=5，DB侧 SUM 聚合下推）
        respVO.setTotalRedPacket(sumRewardPrizeValue(activityId, 5));

        return respVO;
    }

    @Override
    public Map<String, Object> getActivityVoteDailyAnalysis(ActivityVoteLogEventReqVO reqVO) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        LocalDateTime startTime = reqVO.getStartTime();
        LocalDateTime endTime = reqVO.getEndTime();
        // 未传时间默认最近30天
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now()
                    .withHour(23).withMinute(59).withSecond(59).withNano(0);
            startTime = endTime.minusDays(29)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
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
        buildVoteDailyResult(result, reqVO.getId(), startTime, endTime, businessId);
        return convertVoteStatsMapToArrays(result);
    }

    /**
     * 构建每日PV/UV数据：优先ES事件日志，ES无数据时回退vote_log记录
     */
    private void buildVoteDailyResult(Map<String, Map<String, Long>> result, Long activityId,
                                      LocalDateTime startTime, LocalDateTime endTime, Long businessId) {
        if (activityId == null || startTime == null || endTime == null) {
            return;
        }
        // 优先从ES获取每日PV/UV
        if (eventService != null) {
            Map<String, Map<String, Long>> dailyPvUvMap = eventService.statDailyPvUv(
                    EventType.VOTE, activityId.toString(), startTime, endTime, businessId);
            if (!CollectionUtils.isEmpty(dailyPvUvMap)) {
                result.putAll(dailyPvUvMap);
                return;
            }
        }
        // ES无数据，回退到vote_log记录统计：聚合下推到DB，不再全量拉明细回内存分组
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        for (LocalDate current = startDate; !current.isAfter(endDate); current = current.plusDays(1)) {
            Map<String, Long> dayStat = new HashMap<>(2);
            dayStat.put("pv", 0L);
            dayStat.put("uv", 0L);
            result.put(current.toString(), dayStat);
        }
        // 每日PV：GROUP BY DATE(vote_time) + COUNT(*)（与本类 initMemberTotalCounts 对同表的 GROUP BY 下推写法一致）
        QueryWrapper<ActivityVoteLogDO> pvWrapper = new QueryWrapper<>();
        pvWrapper.select("DATE(vote_time) AS day", "COUNT(*) AS cnt")
                .eq("activity_id", activityId)
                .ge("vote_time", startTime)
                .le("vote_time", endTime)
                .groupBy("DATE(vote_time)");
        for (Map<String, Object> row : activityVoteLogMapper.selectMaps(pvWrapper)) {
            Map<String, Long> dayStat = result.computeIfAbsent(String.valueOf(row.get("day")), key -> new HashMap<>(2));
            dayStat.put("pv", Long.parseLong(String.valueOf(row.get("cnt"))));
        }
        // 每日UV：DB侧聚合出 (天, member_id) 组合后按天计行数（会员按 member_mobile 尾号分片，
        // 同一会员仅落在一个分表，跨分片结果无重复，等价于 COUNT(DISTINCT member_id)）
        QueryWrapper<ActivityVoteLogDO> uvWrapper = new QueryWrapper<>();
        uvWrapper.select("DATE(vote_time) AS day", "member_id")
                .eq("activity_id", activityId)
                .ge("vote_time", startTime)
                .le("vote_time", endTime)
                .groupBy("DATE(vote_time)", "member_id");
        for (Map<String, Object> row : activityVoteLogMapper.selectMaps(uvWrapper)) {
            if (row.get("member_id") == null) {
                continue;
            }
            Map<String, Long> dayStat = result.computeIfAbsent(String.valueOf(row.get("day")), key -> new HashMap<>(2));
            dayStat.merge("uv", 1L, Long::sum);
        }
    }

    /**
     * 将每日统计Map转换为前端图表所需的排序数组格式
     */
    private Map<String, Object> convertVoteStatsMapToArrays(Map<String, Map<String, Long>> statsMap) {
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

    @Override
    public List<ActivityVoteOptionAnalysisVO> getActivityVoteOptionAnalysis(ActivityVoteLogEventReqVO reqVO) {
        Long activityId = reqVO.getId();
        LocalDateTime startTime = reqVO.getStartTime();
        LocalDateTime endTime = reqVO.getEndTime();
        // 时间必传：activity_vote_log 为分片表，无时间范围的全量扫描代价过大，不再提供默认30天兜底
        if (startTime == null || endTime == null) {
            throw invalidParamException("开始时间与结束时间必须同时传入");
        }
        if (startTime.isAfter(endTime)) {
            throw exception(LOTTERY_ANALYSIS_TIME_ERROR);
        }
        // 聚合下推：GROUP BY option_id + COUNT(*)，避免明细全拉回内存 groupingBy；
        // 选项名取 MAX(option_name)，仍以 vote_log 快照为准，保持"已删除选项仍出数"语义；
        // 若同 optionId 历史快照改过名，取字典序最大值（与原"首行明细快照"存在可接受差异）
        QueryWrapper<ActivityVoteLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("option_id AS optionId", "MAX(option_name) AS optionName", "COUNT(*) AS voteCount")
                .eq("activity_id", activityId)
                .ge("vote_time", startTime)
                .le("vote_time", endTime)
                .groupBy("option_id");
        List<Map<String, Object>> rows = activityVoteLogMapper.selectMaps(wrapper);
        long total = 0L;
        List<ActivityVoteOptionAnalysisVO> optionStats = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object optionIdObj = row.get("optionId");
            if (optionIdObj == null) {
                continue;
            }
            long voteCount = Long.parseLong(String.valueOf(row.get("voteCount")));
            total += voteCount;
            ActivityVoteOptionAnalysisVO optionVO = new ActivityVoteOptionAnalysisVO();
            optionVO.setOptionId(((Number) optionIdObj).longValue());
            optionVO.setOptionName(row.get("optionName") != null ? String.valueOf(row.get("optionName")) : null);
            optionVO.setVoteCount(voteCount);
            optionStats.add(optionVO);
        }
        // 占比基于聚合后总票数，保留两位小数（与原逻辑一致）
        long finalTotal = total;
        optionStats.forEach(optionVO -> optionVO.setPercentage(finalTotal > 0
                ? BigDecimal.valueOf(optionVO.getVoteCount() * 100.0 / finalTotal).setScale(2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO));
        // 按票数降序
        optionStats.sort((a, b) -> Long.compare(b.getVoteCount(), a.getVoteCount()));
        return optionStats;
    }

    @Override
    public PageResult<ActivityVoteLogRespVO> getVoteLogList(ActivityVoteLogPageReqVO reqVO) {
        LambdaQueryWrapper<ActivityVoteLogDO> wrapper = new LambdaQueryWrapper<ActivityVoteLogDO>()
                .eq(ActivityVoteLogDO::getActivityId, reqVO.getActivityId())
                .eq(reqVO.getOptionId() != null, ActivityVoteLogDO::getOptionId, reqVO.getOptionId())
                .eq(reqVO.getMemberMobile() != null, ActivityVoteLogDO::getMemberMobile, reqVO.getMemberMobile())
                .eq(reqVO.getStoreId() != null, ActivityVoteLogDO::getStoreId, reqVO.getStoreId())
                .ge(reqVO.getStartTime() != null, ActivityVoteLogDO::getVoteTime,
                        reqVO.getStartTime() != null ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .le(reqVO.getEndTime() != null, ActivityVoteLogDO::getVoteTime,
                        reqVO.getEndTime() != null ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .orderByDesc(ActivityVoteLogDO::getVoteTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActivityVoteLogDO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActivityVoteLogDO> resultPage =
                activityVoteLogMapper.selectPage(page, wrapper);

        // 统计每个会员在该活动的累计投票次数
        List<ActivityVoteLogDO> records = resultPage.getRecords();
        Map<Long, Integer> memberVoteCountMap = new HashMap<>();
        Map<Long, String> memberNickNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(records)) {
            Set<Long> memberIds = records.stream().map(ActivityVoteLogDO::getMemberId).collect(Collectors.toSet());
            // 关联 wx_member 查询会员昵称
            List<WxMemberDTO> members = wxMemberApi.getMemberByIds(new ArrayList<>(memberIds));
            if (CollUtil.isNotEmpty(members)) {
                for (WxMemberDTO member : members) {
                    memberNickNameMap.put(member.getMemberId(), member.getMemberNickName());
                }
            }
            for (Long memberId : memberIds) {
                Long count = activityVoteLogMapper.selectCount(
                        new LambdaQueryWrapper<ActivityVoteLogDO>()
                                .eq(ActivityVoteLogDO::getActivityId, reqVO.getActivityId())
                                .eq(ActivityVoteLogDO::getMemberId, memberId));
                memberVoteCountMap.put(memberId, count.intValue());
            }
        }

        List<ActivityVoteLogRespVO> voList = records.stream().map(logDO -> {
            ActivityVoteLogRespVO vo = new ActivityVoteLogRespVO();
            BeanUtil.copyProperties(logDO, vo);
            vo.setTotalVoteCount(memberVoteCountMap.getOrDefault(logDO.getMemberId(), 0));
            vo.setMemberName(memberNickNameMap.getOrDefault(logDO.getMemberId(), logDO.getMemberName()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal());
    }

    @Override
    public ActivityVoteStatisticsRespVO getVoteLogCount(ActivityVoteLogPageReqVO reqVO) {
        ActivityVoteStatisticsRespVO respVO = new ActivityVoteStatisticsRespVO();
        LocalDateTime startTime = reqVO.getStartTime() != null
                ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime endTime = reqVO.getEndTime() != null
                ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;

        // 总投票次数（条件与 getVoteLogList 一致）
        Long totalVotes = activityVoteLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteLogDO>()
                        .eq(ActivityVoteLogDO::getActivityId, reqVO.getActivityId())
                        .eq(reqVO.getOptionId() != null, ActivityVoteLogDO::getOptionId, reqVO.getOptionId())
                        .eq(reqVO.getMemberMobile() != null, ActivityVoteLogDO::getMemberMobile, reqVO.getMemberMobile())
                        .eq(reqVO.getStoreId() != null, ActivityVoteLogDO::getStoreId, reqVO.getStoreId())
                        .ge(startTime != null, ActivityVoteLogDO::getVoteTime, startTime)
                        .le(endTime != null, ActivityVoteLogDO::getVoteTime, endTime));
        respVO.setTotalVoteCount(totalVotes);

        // 参与人数（DB侧 COUNT(DISTINCT member_id) 聚合下推，条件与 getVoteLogList 一致）
        QueryWrapper<ActivityVoteLogDO> distinctWrapper = new QueryWrapper<>();
        distinctWrapper.select("COUNT(DISTINCT member_id) AS cnt")
                .eq("activity_id", reqVO.getActivityId())
                .eq(reqVO.getOptionId() != null, "option_id", reqVO.getOptionId())
                .eq(reqVO.getMemberMobile() != null, "member_mobile", reqVO.getMemberMobile())
                .eq(reqVO.getStoreId() != null, "store_id", reqVO.getStoreId())
                .ge(startTime != null, "vote_time", startTime)
                .le(endTime != null, "vote_time", endTime);
        respVO.setParticipantCount(parseAggLong(activityVoteLogMapper.selectMaps(distinctWrapper), "cnt"));

        return respVO;
    }

    @Override
    public PageResult<ActivityVoteRewardLogRespVO> getRewardLogList(ActivityVoteRewardLogPageReqVO reqVO) {
        LambdaQueryWrapper<ActivityVoteRewardLogDO> wrapper = new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                .eq(ActivityVoteRewardLogDO::getActivityId, reqVO.getActivityId())
                .eq(reqVO.getMemberMobile() != null, ActivityVoteRewardLogDO::getMemberMobile, reqVO.getMemberMobile())
                .eq(reqVO.getPrizeType() != null, ActivityVoteRewardLogDO::getPrizeType, reqVO.getPrizeType())
                .eq(reqVO.getPrizeState() != null, ActivityVoteRewardLogDO::getPrizeState, reqVO.getPrizeState())
                .in(CollUtil.isNotEmpty(reqVO.getClaimStatus()), ActivityVoteRewardLogDO::getClaimStatus, reqVO.getClaimStatus())
                .ge(reqVO.getStartTime() != null, ActivityVoteRewardLogDO::getGrantTime,
                        reqVO.getStartTime() != null ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .le(reqVO.getEndTime() != null, ActivityVoteRewardLogDO::getGrantTime,
                        reqVO.getEndTime() != null ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .orderByDesc(ActivityVoteRewardLogDO::getCreateTime);

        // 收货地址 1未填写 2已填写
        if (reqVO.getReceiveAddress() != null) {
            if (reqVO.getReceiveAddress() == 1) {
                wrapper.and(w -> w.isNull(ActivityVoteRewardLogDO::getReceiveAddress).or().eq(ActivityVoteRewardLogDO::getReceiveAddress, ""));
            } else if (reqVO.getReceiveAddress() == 2) {
                wrapper.and(w -> w.isNotNull(ActivityVoteRewardLogDO::getReceiveAddress).ne(ActivityVoteRewardLogDO::getReceiveAddress, ""));
            }
        }
        // 物流单号 1未填写 2已填写
        if (reqVO.getTrackingNumber() != null) {
            if (reqVO.getTrackingNumber() == 1) {
                wrapper.and(w -> w.isNull(ActivityVoteRewardLogDO::getTrackingNumber).or().eq(ActivityVoteRewardLogDO::getTrackingNumber, ""));
            } else if (reqVO.getTrackingNumber() == 2) {
                wrapper.and(w -> w.isNotNull(ActivityVoteRewardLogDO::getTrackingNumber).ne(ActivityVoteRewardLogDO::getTrackingNumber, ""));
            }
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActivityVoteRewardLogDO> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ActivityVoteRewardLogDO> resultPage =
                activityVoteRewardLogMapper.selectPage(page, wrapper);

        List<ActivityVoteRewardLogDO> records = resultPage.getRecords();
        // 关联 wx_member 查询会员昵称（实时覆盖快照）
        Map<Long, String> memberNickNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(records)) {
            Set<Long> memberIds = records.stream().map(ActivityVoteRewardLogDO::getMemberId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(memberIds)) {
                List<WxMemberDTO> members = wxMemberApi.getMemberByIds(new ArrayList<>(memberIds));
                if (CollUtil.isNotEmpty(members)) {
                    for (WxMemberDTO member : members) {
                        memberNickNameMap.put(member.getMemberId(), member.getMemberNickName());
                    }
                }
            }
        }

        List<ActivityVoteRewardLogRespVO> voList = records.stream().map(logDO -> {
            ActivityVoteRewardLogRespVO vo = new ActivityVoteRewardLogRespVO();
            BeanUtil.copyProperties(logDO, vo);
            vo.setMemberName(memberNickNameMap.getOrDefault(logDO.getMemberId(), logDO.getMemberName()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, resultPage.getTotal());
    }

    @Override
    public ActivityVoteRewardStatisticsRespVO getRewardLogCount(ActivityVoteRewardLogPageReqVO reqVO) {
        ActivityVoteRewardStatisticsRespVO respVO = new ActivityVoteRewardStatisticsRespVO();

        // 发放次数（条件与 getRewardLogList 一致）
        LambdaQueryWrapper<ActivityVoteRewardLogDO> countWrapper = new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                .eq(ActivityVoteRewardLogDO::getActivityId, reqVO.getActivityId())
                .eq(reqVO.getMemberMobile() != null, ActivityVoteRewardLogDO::getMemberMobile, reqVO.getMemberMobile())
                .eq(reqVO.getPrizeType() != null, ActivityVoteRewardLogDO::getPrizeType, reqVO.getPrizeType())
                .eq(reqVO.getPrizeState() != null, ActivityVoteRewardLogDO::getPrizeState, reqVO.getPrizeState())
                .in(CollUtil.isNotEmpty(reqVO.getClaimStatus()), ActivityVoteRewardLogDO::getClaimStatus, reqVO.getClaimStatus())
                .ge(reqVO.getStartTime() != null, ActivityVoteRewardLogDO::getGrantTime,
                        reqVO.getStartTime() != null ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .le(reqVO.getEndTime() != null, ActivityVoteRewardLogDO::getGrantTime,
                        reqVO.getEndTime() != null ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        applyRewardLogFilterConditions(countWrapper, reqVO);
        Long totalCount = activityVoteRewardLogMapper.selectCount(countWrapper);
        respVO.setGrantTotalCount(totalCount);

        // 发放人数（DB侧 COUNT(DISTINCT member_id) 聚合下推，条件与 getRewardLogList 一致）
        LocalDateTime rewardStartTime = reqVO.getStartTime() != null
                ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime rewardEndTime = reqVO.getEndTime() != null
                ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        QueryWrapper<ActivityVoteRewardLogDO> distinctWrapper = new QueryWrapper<>();
        distinctWrapper.select("COUNT(DISTINCT member_id) AS cnt")
                .eq("activity_id", reqVO.getActivityId())
                .eq(reqVO.getMemberMobile() != null, "member_mobile", reqVO.getMemberMobile())
                .eq(reqVO.getPrizeType() != null, "prize_type", reqVO.getPrizeType())
                .eq(reqVO.getPrizeState() != null, "prize_state", reqVO.getPrizeState())
                .in(CollUtil.isNotEmpty(reqVO.getClaimStatus()), "claim_status", reqVO.getClaimStatus())
                .ge(rewardStartTime != null, "grant_time", rewardStartTime)
                .le(rewardEndTime != null, "grant_time", rewardEndTime);
        if (reqVO.getReceiveAddress() != null) {
            if (reqVO.getReceiveAddress() == 1) {
                distinctWrapper.and(w -> w.isNull("receive_address").or().eq("receive_address", ""));
            } else if (reqVO.getReceiveAddress() == 2) {
                distinctWrapper.and(w -> w.isNotNull("receive_address").ne("receive_address", ""));
            }
        }
        if (reqVO.getTrackingNumber() != null) {
            if (reqVO.getTrackingNumber() == 1) {
                distinctWrapper.and(w -> w.isNull("tracking_number").or().eq("tracking_number", ""));
            } else if (reqVO.getTrackingNumber() == 2) {
                distinctWrapper.and(w -> w.isNotNull("tracking_number").ne("tracking_number", ""));
            }
        }
        respVO.setGrantPersonCount(parseAggLong(activityVoteRewardLogMapper.selectMaps(distinctWrapper), "cnt"));

        return respVO;
    }

    /** 给 reward log 查询 wrapper 追加收货地址/物流单号的"未填写/已填写"条件（与 getRewardLogList 一致）。 */
    private void applyRewardLogFilterConditions(LambdaQueryWrapper<ActivityVoteRewardLogDO> wrapper, ActivityVoteRewardLogPageReqVO reqVO) {
        if (reqVO.getReceiveAddress() != null) {
            if (reqVO.getReceiveAddress() == 1) {
                wrapper.and(w -> w.isNull(ActivityVoteRewardLogDO::getReceiveAddress).or().eq(ActivityVoteRewardLogDO::getReceiveAddress, ""));
            } else if (reqVO.getReceiveAddress() == 2) {
                wrapper.and(w -> w.isNotNull(ActivityVoteRewardLogDO::getReceiveAddress).ne(ActivityVoteRewardLogDO::getReceiveAddress, ""));
            }
        }
        if (reqVO.getTrackingNumber() != null) {
            if (reqVO.getTrackingNumber() == 1) {
                wrapper.and(w -> w.isNull(ActivityVoteRewardLogDO::getTrackingNumber).or().eq(ActivityVoteRewardLogDO::getTrackingNumber, ""));
            } else if (reqVO.getTrackingNumber() == 2) {
                wrapper.and(w -> w.isNotNull(ActivityVoteRewardLogDO::getTrackingNumber).ne(ActivityVoteRewardLogDO::getTrackingNumber, ""));
            }
        }
    }

    @Override
    public void exportVoteLog(ActivityVoteLogPageReqVO reqVO) {
        // 统计总量，PC端超过30万拒绝导出
        // 主线程走 @DS(SHARDING) ShardingSphere 数据源，只认逻辑表名，用 selectCount 自动路由全部分表汇总
        // （异步取数回调才走默认数据源按物理分表名查询）
        LocalDateTime startTime = reqVO.getStartTime() != null
                ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        LocalDateTime endTime = reqVO.getEndTime() != null
                ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        Long total = activityVoteLogMapper.selectCount(new LambdaQueryWrapper<ActivityVoteLogDO>()
                .eq(ActivityVoteLogDO::getActivityId, reqVO.getActivityId())
                .eq(reqVO.getOptionId() != null, ActivityVoteLogDO::getOptionId, reqVO.getOptionId())
                .eq(reqVO.getMemberMobile() != null, ActivityVoteLogDO::getMemberMobile, reqVO.getMemberMobile())
                .eq(reqVO.getStoreId() != null, ActivityVoteLogDO::getStoreId, reqVO.getStoreId())
                .ge(startTime != null, ActivityVoteLogDO::getVoteTime, startTime)
                .le(endTime != null, ActivityVoteLogDO::getVoteTime, endTime));
        if (!Objects.equals(reqVO.getExportSource(), 999) && total != null && total > 300000) {
            throw new ServerException(500, "导出数据量超过30万条限制");
        }

        Page<ActivityVoteLogExportRespVO> page = new Page<>(1, 10000);
        voteLogExcelActionService.exportAsyncExcel(
                ActivityVoteLogExportRespVO.class,
                page,
                param -> getVoteLogExportData(param, reqVO),
                "投票记录_" + reqVO.getActivityId());
    }

    @Override
    public void exportRewardLog(ActivityVoteRewardLogPageReqVO reqVO) {
        // 统计总量，PC端超过30万拒绝导出
        // 主线程走 @DS(SHARDING) ShardingSphere 数据源，只认逻辑表名，用 selectCount 自动路由全部分表汇总
        // 条件与 getRewardLogCount 一致
        LambdaQueryWrapper<ActivityVoteRewardLogDO> countWrapper = new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                .eq(ActivityVoteRewardLogDO::getActivityId, reqVO.getActivityId())
                .eq(reqVO.getMemberMobile() != null, ActivityVoteRewardLogDO::getMemberMobile, reqVO.getMemberMobile())
                .eq(reqVO.getPrizeType() != null, ActivityVoteRewardLogDO::getPrizeType, reqVO.getPrizeType())
                .eq(reqVO.getPrizeState() != null, ActivityVoteRewardLogDO::getPrizeState, reqVO.getPrizeState())
                .in(CollUtil.isNotEmpty(reqVO.getClaimStatus()), ActivityVoteRewardLogDO::getClaimStatus, reqVO.getClaimStatus())
                .ge(reqVO.getStartTime() != null, ActivityVoteRewardLogDO::getGrantTime,
                        reqVO.getStartTime() != null ? reqVO.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .le(reqVO.getEndTime() != null, ActivityVoteRewardLogDO::getGrantTime,
                        reqVO.getEndTime() != null ? reqVO.getEndTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null);
        applyRewardLogFilterConditions(countWrapper, reqVO);
        Long total = activityVoteRewardLogMapper.selectCount(countWrapper);
        if (!Objects.equals(reqVO.getExportSource(), 999) && total != null && total > 300000) {
            throw new ServerException(500, "导出数据量超过30万条限制");
        }

        Page<ActivityVoteRewardLogExportRespVO> page = new Page<>(1, 10000);
        voteRewardLogExcelActionService.exportAsyncExcel(
                ActivityVoteRewardLogExportRespVO.class,
                page,
                param -> getRewardLogExportData(param, reqVO),
                "发放记录_" + reqVO.getActivityId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateExpress(ActivityVoteUpdateExpressReqVO reqVO) {
        LambdaUpdateWrapper<ActivityVoteRewardLogDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityVoteRewardLogDO::getId, reqVO.getId());
        updateWrapper.set(ActivityVoteRewardLogDO::getExpressCompany, reqVO.getExpressCompany());
        updateWrapper.set(ActivityVoteRewardLogDO::getTrackingNumber, reqVO.getTrackingNumber());
        updateWrapper.set(ActivityVoteRewardLogDO::getPrizeState, 3); // 已发货
        return activityVoteRewardLogMapper.update(null, updateWrapper);
    }

    @Override
    public ActivityVoteSpreadRespVO selectSpread(Long id) {
        ActivityVoteDO voteDO = activityVoteMapper.selectOne(
                new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, id));
        ActivityVoteSpreadRespVO respVO = new ActivityVoteSpreadRespVO();
        respVO.setId(id);
        if (voteDO != null) {
            respVO.setShareTitle(voteDO.getShareTitle());
            respVO.setShareNote(voteDO.getShareNote());
            respVO.setShareImgUrl(voteDO.getShareImgUrl());
            respVO.setShareType(voteDO.getShareType());
        }
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpread(ActivityVoteSpreadSaveReqVO reqVO) {
        LambdaUpdateWrapper<ActivityVoteDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityVoteDO::getActivityId, reqVO.getId());
        updateWrapper.set(ActivityVoteDO::getShareTitle, reqVO.getShareTitle());
        updateWrapper.set(ActivityVoteDO::getShareNote, reqVO.getShareNote());
        updateWrapper.set(ActivityVoteDO::getShareImgUrl, reqVO.getShareImgUrl());
        updateWrapper.set(reqVO.getShareType() != null, ActivityVoteDO::getShareType, reqVO.getShareType());
        activityVoteMapper.update(null, updateWrapper);
        // 仅刷新活动配置缓存，分享字段存在于 vote_setting，直接重新 set 覆盖写，其他缓存一律不动
        refreshVoteSettingCache(reqVO.getId());
    }

    @Override
    public List<ActivityVoteDetailRespVO> getActivityVoteList() {
        // 查询所有投票类型活动（简要信息）
        List<ActivityDO> activityList = activityMapper.selectList(
                new LambdaQueryWrapper<ActivityDO>()
                        .eq(ActivityDO::getActivityType, ActivityTypeEnum.VOTE.getCode())
                        .orderByDesc(ActivityDO::getCreateTime));
        if (CollUtil.isEmpty(activityList)) {
            return Collections.emptyList();
        }
        return activityList.stream().map(a -> {
            ActivityVoteDetailRespVO vo = new ActivityVoteDetailRespVO();
            vo.setActivityId(a.getId());
            vo.setActivityName(a.getActivityName());
            vo.setIsEnabled(a.getIsEnabled());
            vo.setActivityStatus(getActivityStatus(a.getIsEnabled(), a.getStartDate(), a.getEndDate()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateState(ActivityVoteStateReqVO reqVO) {
        activityService.updateStatus(reqVO.getId(), reqVO.getIsEnabled());
        if (Objects.equals(reqVO.getIsEnabled(), 1)) {
            // 启用：初始化所有缓存
            initAllVoteCaches(reqVO.getId());
        } else {
            // 禁用：清除所有缓存
            clearAllVoteCaches(reqVO.getId());
        }
        return 1;
    }

    // ==================== 私有方法 ====================

    /**
     * 投票活动参数校验（create、copy与update共用）：
     * 1. 配置了投票次数时不能超过 9999；
     * 2. 开始时间必须早于结束时间；
     * 3. 配置了奖励时奖品类型不能为空
     */
    private void validateSaveOrUpdateReqVO(ActivityVoteSaveOrUpdateReqVO reqVO) {
        // 1. 投票次数上限：仅当 voteCountFlag 为 1（每天可投票）或 2（最多可投票）时生效，
        //    voteCountFlag 为 0（不限制）时 voteCount 可为 null，不做上限校验
        Integer voteCountFlag = reqVO.getVoteCountFlag();
        if (voteCountFlag != null && (voteCountFlag == 1 || voteCountFlag == 2)
                && reqVO.getVoteCount() != null && reqVO.getVoteCount() > 9999) {
            throw exception(VOTE_COUNT_OVER_LIMIT);
        }
        // 2. 起止时间：有结束时间时必须同时有开始时间，且开始时间早于结束时间
        if (reqVO.getEndDate() != null) {
            if (reqVO.getStartDate() == null || !reqVO.getStartDate().isBefore(reqVO.getEndDate())) {
                throw exception(VOTE_TIME_ERROR);
            }
        }
        // 3. 奖励奖品类型：配置了奖励时每项奖品类型不能为空
        if (CollUtil.isNotEmpty(reqVO.getRewardList())
                && reqVO.getRewardList().stream().anyMatch(r -> r.getPrizeType() == null)) {
            throw exception(VOTE_PRIZE_TYPE_NOT_EXISTS);
        }
    }

    private ActivityDO buildActivityDO(ActivityVoteSaveOrUpdateReqVO reqVO) {
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityName(reqVO.getActivityName());
        activityDO.setActivityType(ActivityTypeEnum.VOTE.getCode());
        activityDO.setActivityRemark(reqVO.getActivityRemark());
        activityDO.setActivityRules(reqVO.getActivityRules());
        activityDO.setActivityStore(reqVO.getActivityStore());
        activityDO.setStartDate(toDate(reqVO.getStartDate()));
        activityDO.setEndDate(toDate(reqVO.getEndDate()));
        activityDO.setIsEnabled(reqVO.getIsEnabled() != null ? reqVO.getIsEnabled() : 0);
        activityDO.setCommunityFlag(reqVO.getCommunityFlag());
        activityDO.setGuideImage(reqVO.getGuideImage());
        return activityDO;
    }

    private ActivityVoteDO buildActivityVoteDO(ActivityVoteSaveOrUpdateReqVO reqVO) {
        ActivityVoteDO voteDO = new ActivityVoteDO();
        voteDO.setActivityStore(reqVO.getActivityStore());
        voteDO.setActivityImgUrl(reqVO.getActivityImgUrl());
        voteDO.setActivityBackground(reqVO.getActivityBackground());
        voteDO.setVoteBackground(reqVO.getVoteBackground());
        voteDO.setRankingBackground(reqVO.getRankingBackground());
        voteDO.setBackgroundColor(reqVO.getBackgroundColor());
        voteDO.setVoteCountFlag(reqVO.getVoteCountFlag());
        voteDO.setVoteCount(reqVO.getVoteCount());
        voteDO.setVoteBtnTitle(reqVO.getVoteBtnTitle());
        voteDO.setShareTitle(cn.hutool.core.util.StrUtil.blankToDefault(reqVO.getShareTitle(), defaultShareTitle));
        voteDO.setShareNote(cn.hutool.core.util.StrUtil.blankToDefault(reqVO.getShareNote(), defaultShareDescription));
        voteDO.setShareImgUrl(cn.hutool.core.util.StrUtil.blankToDefault(reqVO.getShareImgUrl(), defaultShareImageUrl));
        voteDO.setShareType(reqVO.getShareType());
        voteDO.setPublicButton(reqVO.getPublicButton());
        voteDO.setCommunityFlag(reqVO.getCommunityFlag());
        voteDO.setGuideImage(reqVO.getGuideImage());
        return voteDO;
    }

    private void syncStores(Long activityId, Integer activityStore, List<Long> storeIds) {
        activityStoreService.deleteByActivityId(activityId);
        if (Objects.equals(activityStore, 0) && CollUtil.isNotEmpty(storeIds)) {
            activityStoreService.createBatch(storeIds, activityId);
        }
    }

    private void syncOptions(Long activityId, List<ActivityVoteOptionDO> existOptions, List<ActivityVoteOptionReqVO> optionReqList) {
        if (optionReqList == null) {
            optionReqList = Collections.emptyList();
        }
        // 物理删除前记录删除日志
        if (CollUtil.isNotEmpty(existOptions)) {
            List<ActivityVoteOptionDeleteLogDO> deleteLogs = existOptions.stream().map(o ->
                    ActivityVoteOptionDeleteLogDO.builder()
                            .optionId(o.getId())
                            .activityId(activityId)
                            .optionName(o.getOptionName())
                            .optionUrl(o.getOptionUrl())
                            .optionDetailUrl(o.getOptionDetailUrl())
                            .voteDetail(o.getVoteDetail())
                            .deleteTime(LocalDateTime.now())
                            .businessId(o.getBusinessId())
                            .build()
            ).collect(Collectors.toList());
            activityVoteOptionDeleteLogMapper.insertBatch(deleteLogs);
            // 物理删除旧选项（逻辑删除会导致INSERT同id主键冲突）
            activityVoteOptionMapper.physicalDeleteByActivityId(activityId);
        }
        // 统一批量INSERT：有id的保留原id（与vote_log/Redis key对应），无id的雪花算法自动生成
        if (CollUtil.isNotEmpty(optionReqList)) {
            List<ActivityVoteOptionDO> insertList = new ArrayList<>();
            for (ActivityVoteOptionReqVO optionReq : optionReqList) {
                ActivityVoteOptionDO optionDO = new ActivityVoteOptionDO();
                if (optionReq.getId() != null) {
                    optionDO.setId(optionReq.getId());
                }
                optionDO.setActivityId(activityId);
                optionDO.setOptionName(optionReq.getOptionName());
                optionDO.setOptionUrl(optionReq.getOptionUrl());
                optionDO.setOptionDetailUrl(optionReq.getOptionDetailUrl());
                optionDO.setVoteDetail(optionReq.getVoteDetail());
                insertList.add(optionDO);
            }
            activityVoteOptionMapper.insertBatch(insertList);
        }
    }

    private void syncRewards(Long activityId, List<ActivityVoteRewardDO> existRewards, List<ActivityVoteRewardReqVO> rewardReqList) {
        if (rewardReqList == null) {
            rewardReqList = Collections.emptyList();
        }
        // 构建已有奖励map（用于保留usedNum）
        Map<Long, ActivityVoteRewardDO> existMap = CollUtil.isNotEmpty(existRewards)
                ? existRewards.stream().collect(Collectors.toMap(ActivityVoteRewardDO::getId, r -> r))
                : Collections.emptyMap();

        // 物理删除前记录删除日志
        if (CollUtil.isNotEmpty(existRewards)) {
            List<ActivityVoteRewardDeleteLogDO> deleteLogs = existRewards.stream().map(r ->
                    ActivityVoteRewardDeleteLogDO.builder()
                            .rewardId(r.getId())
                            .activityId(activityId)
                            .prizeType(r.getPrizeType())
                            .prizeId(r.getPrizeId())
                            .couponName(r.getCouponName())
                            .prizeName(r.getPrizeName())
                            .prizeImgUrl(r.getPrizeImgUrl())
                            .prizeValue(r.getPrizeValue())
                            .totalNum(r.getTotalNum())
                            .usedNum(r.getUsedNum())
                            .deleteTime(LocalDateTime.now())
                            .businessId(r.getBusinessId())
                            .build()
            ).collect(Collectors.toList());
            activityVoteRewardDeleteLogMapper.insertBatch(deleteLogs);
            // 物理删除旧奖励（逻辑删除会导致INSERT同id主键冲突）
            activityVoteRewardMapper.physicalDeleteByActivityId(activityId);
        }
        // 统一批量INSERT：有id的保留原id（与Redis stock key对应），无id的雪花算法自动生成
        if (CollUtil.isNotEmpty(rewardReqList)) {
            List<ActivityVoteRewardDO> insertList = new ArrayList<>();
            for (ActivityVoteRewardReqVO rewardReq : rewardReqList) {
                ActivityVoteRewardDO rewardDO = new ActivityVoteRewardDO();
                if (rewardReq.getId() != null) {
                    rewardDO.setId(rewardReq.getId());
                    ActivityVoteRewardDO exist = existMap.get(rewardReq.getId());
                    rewardDO.setUsedNum(exist != null ? exist.getUsedNum() : 0);
                } else {
                    rewardDO.setUsedNum(0);
                }
                rewardDO.setActivityId(activityId);
                rewardDO.setPrizeType(rewardReq.getPrizeType());
                rewardDO.setPrizeId(rewardReq.getPrizeId());
                rewardDO.setCouponName(rewardReq.getCouponName());
                rewardDO.setPrizeName(rewardReq.getPrizeName());
                rewardDO.setPrizeImgUrl(rewardReq.getPrizeImgUrl());
                rewardDO.setPrizeValue(rewardReq.getPrizeValue());
                rewardDO.setTotalNum(rewardReq.getTotalNum() != null ? rewardReq.getTotalNum() : 0);
                insertList.add(rewardDO);
            }
            activityVoteRewardMapper.insertBatch(insertList);
        }
    }

    private Integer getActivityStatus(Integer isEnabled, Date startDate, Date endDate) {
        if (isEnabled == null || isEnabled == 0) {
            return 0; // 未启用
        }
        if (startDate == null || endDate == null) {
            return 1;
        }
        Date now = new Date();
        if (now.before(startDate)) {
            return 1; // 未开始
        } else if (now.after(endDate)) {
            return 3; // 已结束
        } else {
            return 2; // 进行中
        }
    }

    /**
     * 启用时初始化所有Redis缓存（活动配置、选项列表、奖励配置、门店、计数器）
     */
    private void initAllVoteCaches(Long activityId) {
        if (activityId == null) {
            return;
        }
        ActivityDO activityDO = activityService.selectById(activityId);
        ActivityVoteDO voteDO = activityVoteMapper.selectOne(
                new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, activityId).last("limit 1"));
        if (activityDO == null || voteDO == null) {
            return;
        }
        int ttlSeconds = calcCacheTtl(activityDO.getEndDate());

        // 1. 活动配置缓存（TTL = endDate + 7天）
        ActivityVoteSaveOrUpdateReqVO cacheVO = buildVoteSettingCacheVO(activityId, activityDO, voteDO);
        redisCache.setCacheObject(VoteKeyConstants.VOTE_SETTING + activityId, cacheVO, ttlSeconds, TimeUnit.SECONDS);

        // 2. 选项列表缓存（TTL = endDate + 7天）
        List<ActivityVoteOptionDO> options = activityVoteOptionMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, activityId));
        redisCache.setCacheObject(VoteKeyConstants.VOTE_OPTIONS + activityId, options, ttlSeconds, TimeUnit.SECONDS);

        // 3. 奖励配置缓存（不过期）
        List<ActivityVoteRewardDO> rewards = activityVoteRewardMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, activityId));
        redisCache.setCacheObject(VoteKeyConstants.VOTE_REWARD_CONFIG + activityId, rewards);

        // 4. 门店Set（TTL = endDate + 7天）
        if (Objects.equals(activityDO.getActivityStore(), 0)) {
            refreshVoteStoreSet(activityId, ttlSeconds);
        }

        // 5. 奖励已下发数量计数器
        initRewardStockCounters(activityId);

        // 6. 选项投票数计数器（首次启用为0，再次启用从DB恢复历史票数）
        initOptionVoteCounts(activityId);

        // 7. 恢复用户奖励已领取标记（从DB reward_log读取已领取用户，TTL=endDate+7天）
        initRewardClaimedFlags(activityId, ttlSeconds);

        // 8. 恢复用户总投票次数（voteCountFlag=2限制用）
        initMemberTotalCounts(activityId, ttlSeconds);

        // 9. 恢复用户当日投票次数（voteCountFlag=1限制用）
        initMemberDailyCounts(activityId);
    }

    /**
     * 从DB恢复用户总投票次数（GROUP BY COUNT + 分页 + pipeline批量SET）
     */
    private void initMemberTotalCounts(Long activityId, long ttlSeconds) {
        int pageNum = 1;
        while (true) {
            Page<Map<String, Object>> page = new Page<>(pageNum, BATCH_SIZE);
            QueryWrapper<ActivityVoteLogDO> wrapper = new QueryWrapper<>();
            wrapper.select("member_mobile, COUNT(*) as cnt")
                    .eq("activity_id", activityId)
                    .groupBy("member_mobile");
            Page<Map<String, Object>> result = activityVoteLogMapper.selectMapsPage(page, wrapper);
            List<Map<String, Object>> records = result.getRecords();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            // pipeline批量SETEX（TTL=活动结束+7天，防止会员级key永久残留）
            redisCache.getRedisTemplate().executePipelined((RedisCallback<Void>) connection -> {
                for (Map<String, Object> row : records) {
                    String mobile = String.valueOf(row.get("member_mobile"));
                    String cnt = String.valueOf(row.get("cnt"));
                    String key = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + mobile;
                    connection.stringCommands().setEx(key.getBytes(), ttlSeconds, cnt.getBytes());
                }
                return null;
            });
            if (records.size() < BATCH_SIZE) {
                break;
            }
            pageNum++;
        }
    }

    /**
     * 从DB恢复用户当日投票次数（GROUP BY COUNT + 分页 + pipeline批量SET，TTL=1天）
     */
    private void initMemberDailyCounts(Long activityId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int pageNum = 1;
        while (true) {
            Page<Map<String, Object>> page = new Page<>(pageNum, BATCH_SIZE);
            QueryWrapper<ActivityVoteLogDO> wrapper = new QueryWrapper<>();
            wrapper.select("member_mobile, COUNT(*) as cnt")
                    .eq("activity_id", activityId)
                    .ge("vote_time", todayStart)
                    .groupBy("member_mobile");
            Page<Map<String, Object>> result = activityVoteLogMapper.selectMapsPage(page, wrapper);
            List<Map<String, Object>> records = result.getRecords();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            // pipeline批量SET + EXPIRE 1天
            redisCache.getRedisTemplate().executePipelined((RedisCallback<Void>) connection -> {
                for (Map<String, Object> row : records) {
                    String mobile = String.valueOf(row.get("member_mobile"));
                    String cnt = String.valueOf(row.get("cnt"));
                    String key = VoteKeyConstants.VOTE_MEMBER_DAILY + activityId + ":" + mobile + ":" + today;
                    connection.stringCommands().setEx(key.getBytes(), 86400, cnt.getBytes());
                }
                return null;
            });
            if (records.size() < BATCH_SIZE) {
                break;
            }
            pageNum++;
        }
    }

    /**
     * 从DB恢复用户奖励已领取标记（分页查询 + pipeline批量SETEX，TTL=endDate+7天，防止禁用再启用后重复领奖）
     */
    private void initRewardClaimedFlags(Long activityId, long ttlSeconds) {
        int pageNum = 1;
        while (true) {
            Page<ActivityVoteRewardLogDO> page = new Page<>(pageNum, BATCH_SIZE);
            LambdaQueryWrapper<ActivityVoteRewardLogDO> wrapper = new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                    .select(ActivityVoteRewardLogDO::getMemberMobile)
                    .eq(ActivityVoteRewardLogDO::getActivityId, activityId)
                    .groupBy(ActivityVoteRewardLogDO::getMemberMobile);
            Page<ActivityVoteRewardLogDO> result = activityVoteRewardLogMapper.selectPage(page, wrapper);
            List<ActivityVoteRewardLogDO> records = result.getRecords();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            // pipeline批量SETEX（值固定"1"，覆盖写无副作用；TTL=endDate+7天，活动结束自动清理）
            redisCache.getRedisTemplate().executePipelined((RedisCallback<Void>) connection -> {
                for (ActivityVoteRewardLogDO log : records) {
                    String claimedKey = VoteKeyConstants.VOTE_REWARD_CLAIMED + activityId + ":" + log.getMemberMobile();
                    connection.stringCommands().setEx(claimedKey.getBytes(), ttlSeconds, "1".getBytes());
                }
                return null;
            });
            if (records.size() < BATCH_SIZE) {
                break;
            }
            pageNum++;
        }
    }

    /**
     * 初始化选项投票数Redis计数器（从DB activity_vote_log统计）
     */
    private void initOptionVoteCounts(Long activityId) {
        List<ActivityVoteOptionDO> options = activityVoteOptionMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, activityId));
        if (CollUtil.isNotEmpty(options)) {
            for (ActivityVoteOptionDO option : options) {
                String optionKey = VoteKeyConstants.VOTE_OPTION_COUNT + activityId + ":" + option.getId();
                Long voteNum = activityVoteLogMapper.selectCount(
                        new LambdaQueryWrapper<ActivityVoteLogDO>()
                                .eq(ActivityVoteLogDO::getActivityId, activityId)
                                .eq(ActivityVoteLogDO::getOptionId, option.getId()));
                stringRedisTemplate.opsForValue().set(optionKey, String.valueOf(voteNum));
            }
        }
    }

    /**
     * 初始化奖励已下发数量Redis计数器（从DB used_num同步）
     */
    private void initRewardStockCounters(Long activityId) {
        List<ActivityVoteRewardDO> rewards = activityVoteRewardMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, activityId));
        if (CollUtil.isNotEmpty(rewards)) {
            for (ActivityVoteRewardDO reward : rewards) {
                if (reward.getTotalNum() != null && reward.getTotalNum() > 0) {
                    String stockKey = VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId();
                    int usedNum = reward.getUsedNum() != null ? reward.getUsedNum() : 0;
                    stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(usedNum));
                }
            }
        }
    }

    /**
     * 加载活动门店到Redis Set
     */
    private void refreshVoteStoreSet(Long activityId, int ttlSeconds) {
        String storeKey = VoteKeyConstants.VOTE_STORE + activityId;
        redisCache.deleteObject(storeKey);
        List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(activityId);
        if (CollUtil.isNotEmpty(storeIds)) {
            String[] members = storeIds.stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(storeKey, members);
            stringRedisTemplate.expire(storeKey, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 组装活动配置缓存对象（含分享字段），供初始化与分享配置更新后复用
     */
    private ActivityVoteSaveOrUpdateReqVO buildVoteSettingCacheVO(Long activityId, ActivityDO activityDO, ActivityVoteDO voteDO) {
        ActivityVoteSaveOrUpdateReqVO cacheVO = new ActivityVoteSaveOrUpdateReqVO();
        cacheVO.setActivityId(activityId);
        cacheVO.setActivityName(activityDO.getActivityName());
        cacheVO.setActivityType(activityDO.getActivityType());
        cacheVO.setIsEnabled(activityDO.getIsEnabled());
        // 活动主表起止时间只精确到日期（时分秒为00:00:00），缓存时归一化：开始日00:00:00、结束日23:59:59
        LocalDateTime startDate = toLdt(activityDO.getStartDate());
        if (startDate != null) {
            startDate = startDate.toLocalDate().atStartOfDay();
        }
        cacheVO.setStartDate(startDate);
        LocalDateTime endDate = toLdt(activityDO.getEndDate());
        if (endDate != null) {
            endDate = endDate.toLocalDate().atTime(23, 59, 59);
        }
        cacheVO.setEndDate(endDate);
        cacheVO.setActivityRemark(activityDO.getActivityRemark());
        cacheVO.setActivityRules(activityDO.getActivityRules());
        cacheVO.setActivityStore(activityDO.getActivityStore());
        cacheVO.setId(voteDO.getId());
        cacheVO.setActivityImgUrl(voteDO.getActivityImgUrl());
        cacheVO.setActivityBackground(voteDO.getActivityBackground());
        cacheVO.setVoteBackground(voteDO.getVoteBackground());
        cacheVO.setRankingBackground(voteDO.getRankingBackground());
        cacheVO.setBackgroundColor(voteDO.getBackgroundColor());
        cacheVO.setVoteCountFlag(voteDO.getVoteCountFlag());
        cacheVO.setVoteCount(voteDO.getVoteCount());
        cacheVO.setVoteBtnTitle(voteDO.getVoteBtnTitle());
        cacheVO.setShareTitle(voteDO.getShareTitle());
        cacheVO.setShareNote(voteDO.getShareNote());
        cacheVO.setShareImgUrl(voteDO.getShareImgUrl());
        cacheVO.setShareType(voteDO.getShareType());
        cacheVO.setPublicButton(voteDO.getPublicButton());
        cacheVO.setCommunityFlag(voteDO.getCommunityFlag());
        cacheVO.setGuideImage(voteDO.getGuideImage());
        return cacheVO;
    }
    
    /**
     * 刷新投票活动配置缓存（vote_setting）：分享配置更新后直接重新 set 覆盖写，复刻签到模块做法，
     * 避免删除缓存后依赖回源重建带来的并发压力；缓存失败不影响主流程。
     * 活动不存在/禁用/扩展配置缺失时缓存本不该存在，降级为删除该 key，保持语义安全。
     */
    private void refreshVoteSettingCache(Long activityId) {
        try {
            if (activityId == null) {
                return;
            }
            ActivityDO activityDO = activityService.selectById(activityId);
            if (activityDO == null || !Objects.equals(activityDO.getIsEnabled(), 1)) {
                redisCache.deleteObject(VoteKeyConstants.VOTE_SETTING + activityId);
                return;
            }
            ActivityVoteDO voteDO = activityVoteMapper.selectOne(
                    new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, activityId).last("limit 1"));
            if (voteDO == null) {
                redisCache.deleteObject(VoteKeyConstants.VOTE_SETTING + activityId);
                return;
            }
            // 与 initAllVoteCaches 复用同一组装逻辑（含 shareTitle/shareNote/shareImgUrl/shareType 分享字段）
            ActivityVoteSaveOrUpdateReqVO cacheVO = buildVoteSettingCacheVO(activityId, activityDO, voteDO);
            int ttlSeconds = calcCacheTtl(activityDO.getEndDate());
            redisCache.setCacheObject(VoteKeyConstants.VOTE_SETTING + activityId, cacheVO, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("刷新投票活动配置缓存失败 activityId={}", activityId, e);
        }
    }
    
    /**
     * 计算缓存TTL：活动结束时间往后推迟一周
     */
    private int calcCacheTtl(Date endDate) {
        if (endDate == null) {
            return VOTE_SETTING_CACHE_SECONDS;
        }
        long expireMillis = endDate.getTime() + 7 * 24 * 3600 * 1000L;
        long ttlSeconds = (expireMillis - System.currentTimeMillis()) / 1000;
        return (int) Math.max(ttlSeconds, 60);
    }

    /**
     * DB侧按 prizeType 聚合 prize_value 总和（SUM 下推，避免明细全拉回内存求和）；无记录或全为 NULL 时返回 ZERO
     */
    private BigDecimal sumRewardPrizeValue(Long activityId, Integer prizeType) {
        QueryWrapper<ActivityVoteRewardLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("SUM(prize_value) AS totalValue")
                .eq("activity_id", activityId)
                .eq("prize_type", prizeType);
        List<Map<String, Object>> rows = activityVoteRewardLogMapper.selectMaps(wrapper);
        // 无匹配记录时 ShardingSphere 聚合可能返回首行为 null 的单元素列表，需同时覆盖空列表与首行 null
        if (CollUtil.isEmpty(rows) || rows.get(0) == null) {
            return BigDecimal.ZERO;
        }
        Object value = rows.get(0).get("totalValue");
        return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value));
    }

    /** 从单行聚合查询结果中取 Long 值（COUNT 等），无结果或 NULL 时返回 0 */
    private long parseAggLong(List<Map<String, Object>> rows, String column) {
        if (CollUtil.isEmpty(rows) || rows.get(0) == null) {
            return 0L;
        }
        Object value = rows.get(0).get(column);
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    /**
     * 统计优惠券包发放的券张数（prizeType=3）：每个券包的发放次数 × 包内券张数。
     * <p>发放次数在发放记录表上按 prize_id GROUP BY 聚合下推（每包仅返回一行，与集卡 activityJk 展开口径一致，
     * 但避免其 selectList 全量明细拉回内存的反模式）；包内张数取券包配置表 good_coupon_package（小表）的
     * SUM(num)，即实际发放时按 couponNumMap 逐券发放的总张数。</p>
     */
    private long statCouponPackageGrantCount(Long activityId) {
        QueryWrapper<ActivityVoteRewardLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("prize_id AS prizeId", "COUNT(*) AS cnt")
                .eq("activity_id", activityId)
                .eq("prize_type", 3)
                .groupBy("prize_id");
        List<Map<String, Object>> rows = activityVoteRewardLogMapper.selectMaps(wrapper);
        if (CollUtil.isEmpty(rows)) {
            return 0L;
        }
        Map<Long, Long> grantCountByPackage = new HashMap<>();
        for (Map<String, Object> row : rows) {
            // 分片表聚合结果行可能为 null，逐行防御
            if (row == null) {
                continue;
            }
            Object prizeId = row.get("prizeId");
            Object cnt = row.get("cnt");
            if (prizeId == null || cnt == null) {
                continue;
            }
            grantCountByPackage.merge(Long.parseLong(String.valueOf(prizeId)),
                    Long.parseLong(String.valueOf(cnt)), Long::sum);
        }
        if (grantCountByPackage.isEmpty()) {
            return 0L;
        }
        // 包内券张数：good_coupon_package 为小配置表，GROUP BY package_id 聚合 SUM(num)
        QueryWrapper<GoodCouponPackageDO> packageWrapper = new QueryWrapper<>();
        packageWrapper.select("package_id AS packageId", "SUM(num) AS totalNum")
                .in("package_id", grantCountByPackage.keySet())
                .groupBy("package_id");
        List<Map<String, Object>> packageRows = goodCouponPackageMapper.selectMaps(packageWrapper);
        Map<Long, Long> numByPackage = new HashMap<>();
        if (CollUtil.isNotEmpty(packageRows)) {
            for (Map<String, Object> row : packageRows) {
                if (row == null) {
                    continue;
                }
                Object packageId = row.get("packageId");
                Object totalNum = row.get("totalNum");
                if (packageId == null || totalNum == null) {
                    continue;
                }
                numByPackage.put(Long.parseLong(String.valueOf(packageId)),
                        Long.parseLong(String.valueOf(totalNum)));
            }
        }
        long total = 0L;
        for (Map.Entry<Long, Long> entry : grantCountByPackage.entrySet()) {
            Long numInPackage = numByPackage.get(entry.getKey());
            if (numInPackage != null) {
                total += numInPackage * entry.getValue();
            }
        }
        return total;
    }

    /** LocalDateTime → Date（写入ActivityDO） */
    private Date toDate(LocalDateTime t) {
        return t == null ? null : Date.from(t.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Date → LocalDateTime（从ActivityDO读出到VO） */
    private LocalDateTime toLdt(Date d) {
        return d == null ? null : LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }

    /**
     * 禁用时清除投票活动相关的所有Redis缓存（含会员级key）
     * 通过查表获取optionId/member_mobile组合key，pipeline+unlink批量删除（支持300w会员级别）
     */
    private void clearAllVoteCaches(Long activityId) {
        // 活动配置缓存、选项列表缓存、奖励配置缓存、门店Set（固定key直接删）
        redisCache.deleteObject(VoteKeyConstants.VOTE_SETTING + activityId);
        redisCache.deleteObject(VoteKeyConstants.VOTE_OPTIONS + activityId);
        redisCache.deleteObject(VoteKeyConstants.VOTE_REWARD_CONFIG + activityId);
        redisCache.deleteObject(VoteKeyConstants.VOTE_STORE + activityId);

        // 选项票数计数器：查选项表拿optionId，拼key删除
        List<ActivityVoteOptionDO> options = activityVoteOptionMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, activityId));
        if (CollUtil.isNotEmpty(options)) {
            List<String> optionCountKeys = options.stream()
                    .map(o -> VoteKeyConstants.VOTE_OPTION_COUNT + activityId + ":" + o.getId())
                    .collect(Collectors.toList());
            pipelineUnlink(optionCountKeys);
        }

        // 会员总投票次数：分页查vote_log的member_mobile，拼key批量删除
        clearMemberKeysByLog(activityId, VoteKeyConstants.VOTE_MEMBER_TOTAL);

        // 会员奖励已领取标记：分页查reward_log的member_mobile，拼key批量删除
        clearMemberKeysByRewardLog(activityId);

        // 奖励已下发数量计数器（按奖励id，数量少直接pipeline删）
        deleteRewardStockKeys(activityId);
    }

    /**
     * 编辑/删除时清除固定Redis缓存（不查会员级key）
     * 因为编辑和删除都要求活动已禁用，禁用时已通过clearAllVoteCaches完成会员级key清理
     */
    private void clearFixedVoteCaches(Long activityId) {
        redisCache.deleteObject(VoteKeyConstants.VOTE_SETTING + activityId);
        redisCache.deleteObject(VoteKeyConstants.VOTE_OPTIONS + activityId);
        redisCache.deleteObject(VoteKeyConstants.VOTE_REWARD_CONFIG + activityId);
        redisCache.deleteObject(VoteKeyConstants.VOTE_STORE + activityId);
        deleteRewardStockKeys(activityId);
    }

    /**
     * 分页查vote_log的member_mobile，拼key批量unlink（每批1000）
     */
    private void clearMemberKeysByLog(Long activityId, String keyPrefix) {
        int pageNum = 1;
        while (true) {
            Page<Map<String, Object>> page = new Page<>(pageNum, BATCH_SIZE);
            QueryWrapper<ActivityVoteLogDO> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT member_mobile")
                    .eq("activity_id", activityId);
            Page<Map<String, Object>> result = activityVoteLogMapper.selectMapsPage(page, wrapper);
            List<Map<String, Object>> records = result.getRecords();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            List<String> keys = records.stream()
                    .map(row -> keyPrefix + activityId + ":" + row.get("member_mobile"))
                    .collect(Collectors.toList());
            pipelineUnlink(keys);
            if (records.size() < BATCH_SIZE) {
                break;
            }
            pageNum++;
        }
    }

    /**
     * 分页查reward_log的member_mobile，拼key批量unlink（每批1000）
     */
    private void clearMemberKeysByRewardLog(Long activityId) {
        int pageNum = 1;
        while (true) {
            Page<Map<String, Object>> page = new Page<>(pageNum, BATCH_SIZE);
            QueryWrapper<ActivityVoteRewardLogDO> wrapper = new QueryWrapper<>();
            wrapper.select("DISTINCT member_mobile")
                    .eq("activity_id", activityId);
            Page<Map<String, Object>> result = activityVoteRewardLogMapper.selectMapsPage(page, wrapper);
            List<Map<String, Object>> records = result.getRecords();
            if (CollUtil.isEmpty(records)) {
                break;
            }
            List<String> keys = records.stream()
                    .map(row -> VoteKeyConstants.VOTE_REWARD_CLAIMED + activityId + ":" + row.get("member_mobile"))
                    .collect(Collectors.toList());
            pipelineUnlink(keys);
            if (records.size() < BATCH_SIZE) {
                break;
            }
            pageNum++;
        }
    }

    /**
     * 删除奖励库存计数器（按奖励id，数量少直接pipeline删）
     */
    private void deleteRewardStockKeys(Long activityId) {
        List<ActivityVoteRewardDO> rewards = activityVoteRewardMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, activityId));
        if (CollUtil.isNotEmpty(rewards)) {
            List<String> stockKeys = rewards.stream()
                    .map(r -> VoteKeyConstants.VOTE_REWARD_STOCK + r.getId())
                    .collect(Collectors.toList());
            pipelineUnlink(stockKeys);
        }
    }

    /**
     * 投票记录导出数据获取（ExcelActionService回调）
     */
    private List<ActivityVoteLogExportRespVO> getVoteLogExportData(Page<ActivityVoteLogExportRespVO> page, ActivityVoteLogPageReqVO reqVO) {
        // 分表查询：手动遍历 activity_vote_log_0..9，按 vote_time DESC 跨表分页拼接。
        // 异步导出线程不走 @DS(SHARDING) 代理，必须显式按物理分表名查询，否则走默认数据源查不到数据。
        int pageNo = Math.max((int) page.getCurrent(), 1);
        int pageSize = Math.max((int) page.getSize(), 10000);
        long globalOffset = (long) (pageNo - 1) * pageSize;
        int remain = pageSize;
        List<ActivityVoteLogDO> records = new ArrayList<>();
        for (int i = 0; i < 10 && remain > 0; i++) {
            String table = voteLogTable(i);
            long tableCount = activityVoteLogMapper.countPage(table, reqVO);
            if (tableCount <= globalOffset) {
                globalOffset -= tableCount;
                continue;
            }
            List<ActivityVoteLogDO> rows = activityVoteLogMapper.selectPage(table, reqVO, (int) globalOffset, remain);
            records.addAll(rows);
            remain = pageSize - records.size();
            globalOffset = 0;
        }
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }

        // 统计每个会员累计投票次数（遍历分表 group by member_id 累加，不依赖 @DS）
        Map<Long, Integer> memberVoteCountMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> groupList = activityVoteLogMapper.countGroupByMember(voteLogTable(i), reqVO.getActivityId());
            if (CollUtil.isNotEmpty(groupList)) {
                for (Map<String, Object> row : groupList) {
                    Object mid = row.get("memberId");
                    if (mid == null) continue;
                    Long memberId = ((Number) mid).longValue();
                    long cnt = row.get("cnt") == null ? 0 : ((Number) row.get("cnt")).longValue();
                    memberVoteCountMap.merge(memberId, (int) cnt, Integer::sum);
                }
            }
        }

        // 关联 wx_member 查询会员昵称（与列表 getVoteLogList 一致）
        Set<Long> memberIds = records.stream().map(ActivityVoteLogDO::getMemberId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> memberNickNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(memberIds)) {
            List<WxMemberDTO> members = wxMemberApi.getMemberByIds(new ArrayList<>(memberIds));
            if (CollUtil.isNotEmpty(members)) {
                for (WxMemberDTO member : members) {
                    memberNickNameMap.put(member.getMemberId(), member.getMemberNickName());
                }
            }
        }

        // 转 VO（memberNickName 用 wx_member 实时查询，DO 快照兜底）
        return records.stream().map(logDO -> {
            ActivityVoteLogExportRespVO vo = new ActivityVoteLogExportRespVO();
            vo.setMemberName(memberNickNameMap.getOrDefault(logDO.getMemberId(), logDO.getMemberName() != null ? logDO.getMemberName() : ""));
            vo.setMemberMobile(logDO.getMemberMobile() != null ? String.valueOf(logDO.getMemberMobile()) : "");
            vo.setId(String.valueOf(logDO.getId()));
            vo.setOptionName(logDO.getOptionName());
            vo.setVoteTime(logDO.getVoteTime());
            vo.setStoreName(logDO.getStoreName());
            vo.setTotalVoteCount(memberVoteCountMap.getOrDefault(logDO.getMemberId(), 0));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 投票记录分表名：activity_vote_log_ + 手机号尾号(0-9)。 */
    private String voteLogTable(int shard) {
        return "activity_vote_log_" + shard;
    }

    /**
     * 发放记录导出数据获取（ExcelActionService回调）
     */
    private List<ActivityVoteRewardLogExportRespVO> getRewardLogExportData(Page<ActivityVoteRewardLogExportRespVO> page, ActivityVoteRewardLogPageReqVO reqVO) {
        // 分表查询：手动遍历 activity_vote_reward_log_0..9，按 create_time DESC 跨表分页拼接。
        // 异步导出线程不走 @DS(SHARDING) 代理，必须显式按物理分表名查询，否则走默认数据源查不到数据。
        int pageNo = Math.max((int) page.getCurrent(), 1);
        int pageSize = Math.max((int) page.getSize(), 10000);
        long globalOffset = (long) (pageNo - 1) * pageSize;
        int remain = pageSize;
        List<ActivityVoteRewardLogDO> records = new ArrayList<>();
        for (int i = 0; i < 10 && remain > 0; i++) {
            String table = rewardLogTable(i);
            long tableCount = activityVoteRewardLogMapper.countPage(table, reqVO);
            if (tableCount <= globalOffset) {
                globalOffset -= tableCount;
                continue;
            }
            List<ActivityVoteRewardLogDO> rows = activityVoteRewardLogMapper.selectPage(table, reqVO, (int) globalOffset, remain);
            records.addAll(rows);
            remain = pageSize - records.size();
            globalOffset = 0;
        }
        if (CollUtil.isEmpty(records)) {
            return Collections.emptyList();
        }
        // 关联 wx_member 查询会员昵称（与列表 getRewardLogList 一致）
        Set<Long> memberIds = records.stream().map(ActivityVoteRewardLogDO::getMemberId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> memberNickNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(memberIds)) {
            List<WxMemberDTO> members = wxMemberApi.getMemberByIds(new ArrayList<>(memberIds));
            if (CollUtil.isNotEmpty(members)) {
                for (WxMemberDTO member : members) {
                    memberNickNameMap.put(member.getMemberId(), member.getMemberNickName());
                }
            }
        }
        return records.stream().map(logDO -> toRewardLogExportResp(logDO, memberNickNameMap)).collect(Collectors.toList());
    }

    /** 投票奖励发放记录分表名：activity_vote_reward_log_ + 手机号尾号(0-9)。 */
    private String rewardLogTable(int shard) {
        return "activity_vote_reward_log_" + shard;
    }

    /** 导出 VO 转换：memberNickName 用 wx_member 实时查询（Feign），DO 快照兜底。 */
    private ActivityVoteRewardLogExportRespVO toRewardLogExportResp(ActivityVoteRewardLogDO logDO, Map<Long, String> memberNickNameMap) {
        ActivityVoteRewardLogExportRespVO vo = new ActivityVoteRewardLogExportRespVO();
        vo.setMemberName(memberNickNameMap.getOrDefault(logDO.getMemberId(), logDO.getMemberName() != null ? logDO.getMemberName() : ""));
        vo.setMemberMobile(logDO.getMemberMobile() != null ? String.valueOf(logDO.getMemberMobile()) : "");
        vo.setId(String.valueOf(logDO.getId()));
        vo.setPrizeTypeName(getPrizeTypeName(logDO.getPrizeType()));
        vo.setPrizeName(logDO.getPrizeName());
        vo.setPrizeValue(logDO.getPrizeValue());
        vo.setGrantTime(logDO.getGrantTime());
        vo.setClaimStatusName(getClaimStatusName(logDO.getClaimStatus()));
        vo.setReceiveUser(logDO.getReceiveUser());
        vo.setReceiveAddress(logDO.getReceiveAddress());
        vo.setExpressCompany(logDO.getExpressCompany());
        vo.setTrackingNumber(logDO.getTrackingNumber());
        return vo;
    }

    private String getPrizeTypeName(Integer prizeType) {
        if (prizeType == null) return "";
        return switch (prizeType) {
            case 1 -> "积分";
            case 2 -> "优惠券";
            case 3 -> "优惠券包";
            case 4 -> "实物奖品";
            case 5 -> "现金红包";
            default -> "";
        };
    }

    private String getClaimStatusName(Integer claimStatus) {
        if (claimStatus == null) return "";
        return switch (claimStatus) {
            case 1 -> "未领取";
            case 2 -> "已领取";
            case 3 -> "已失效";
            default -> "";
        };
    }

    /**
     * pipeline + unlink批量删除（非阻塞删除，性能优于DEL）
     */
    private void pipelineUnlink(List<String> keys) {
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        redisCache.getRedisTemplate().executePipelined((RedisCallback<Void>) connection -> {
            for (String key : keys) {
                connection.keyCommands().unlink(key.getBytes());
            }
            return null;
        });
    }
}
