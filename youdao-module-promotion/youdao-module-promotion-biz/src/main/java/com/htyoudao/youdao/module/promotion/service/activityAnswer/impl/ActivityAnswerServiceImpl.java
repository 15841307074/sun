package com.htyoudao.youdao.module.promotion.service.activityAnswer.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import com.htyoudao.youdao.module.promotion.api.couponpackage.CouponPackageApi;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivityChannelSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityChannel.ActivityChannelDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.*;
import com.htyoudao.youdao.module.promotion.enums.channel.ActivityChannelTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.service.activityAnswer.ActivityAnswerService;
import com.htyoudao.youdao.module.promotion.service.activityAnswer.ActivityAnswerExportQueryService;
import com.htyoudao.youdao.module.promotion.service.activityAnswerCommodity.ActivityAnswerCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityChannel.ActivityChannelService;
import com.htyoudao.youdao.module.promotion.service.activityLotteryCommodity.ActivityLotteryCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Slf4j
@RefreshScope
@DS(DsNameConstants.SHARDING)
public class ActivityAnswerServiceImpl implements ActivityAnswerService {

    private static final int DEFAULT_CACHE_SECONDS = 60;
    private static final int EXPORT_PAGE_SIZE = 10000;

    @Resource
    private ActivityAnswerMapper activityAnswerMapper;
    @Resource
    private ActivityAnswerQuestionMapper questionMapper;
    @Resource
    private ActivityAnswerRewardMapper rewardMapper;
    @Resource
    private ActivityAnswerRecordMapper recordMapper;
    @Resource
    private ActivityAnswerRecordDetailMapper recordDetailMapper;
    @Resource
    private ActivityAnswerRewardLogMapper rewardLogMapper;
    @Resource
    private ActivityStoreService activityStoreService;
    @Resource
    private ActivityService activityService;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private RedisCache redisCache;
    @Resource
    private ExcelActionService<List<Object>> activityAnswerDynamicExcelActionService;
    @Resource
    private ExcelActionService<ActivityAnswerRewardLogExportRespVO> activityAnswerRewardExcelActionService;
    @Resource
    private ActivityAnswerExportQueryService activityAnswerExportQueryService;
    @Resource
    private IEventService eventService;

    @DubboReference
    private StoreApi storeApi;
    @DubboReference
    private GoodCouponApi goodCouponApi;
    @DubboReference
    private CouponPackageApi couponPackageApi;
    /**
     * 活动渠道服务。
     */
    @Resource
    private ActivityChannelService activityChannelService;
    // 分享图片
    @Value("${activity.dt.shareImageUrl}")
    private String shareImageUrl;
    // 分享标题
    @Value("${activity.dt.shareTitle}")
    private String shareTitle;
    // 分享描述
    @Value("${activity.dt.shareDescription}")
    private String shareDescription;

    @Resource
    private ActivityAnswerCommodityService activityAnswerCommodityService;
    /**
     * 创建有奖问答活动，保存配置、题目、奖励、门店范围后同步 Redis 缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> createActivityAnswer(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        CommonResult<Integer> validResult = validateSaveReq(reqVO, true);
        if (validResult.isError()) {
            return validResult;
        }

        Long activityId = createMainActivity(reqVO);
        ActivityAnswerDO answer = BeanUtils.toBean(reqVO, ActivityAnswerDO.class);
        answer.setId(activityId);
        answer.setActivityId(activityId);
        answer.setPrizePoolRules(Objects.requireNonNullElse(answer.getPrizePoolRules(), 1));
        answer.setCalculationRules(Objects.requireNonNullElse(answer.getCalculationRules(), 1));
        answer.setPlaceOrderProduct(reqVO.getPlaceOrderProduct());
        answer.setCreateUserName(SecurityFrameworkUtils.getLoginUsername());
        answer.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
        answer.setShareNote(shareDescription);
        answer.setShareTitle(shareTitle);
        answer.setShareImgUrl(shareImageUrl);
        activityAnswerMapper.insert(answer);

        saveStoreScope(activityId, reqVO);
        saveQuestions(activityId, reqVO);
        saveRewards(activityId, reqVO);
        saveCommodities(activityId, reqVO);
        syncRewardRecordAndCache(activityId);
        activityChannelService.createChannelDO(activityId, ActivityChannelTypeEnum.DT.getCode());
        return success(1);
    }

    /**
     * 修改有奖问答活动，奖励按 ID 增删改，已领取数量按发奖记录重新同步。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> updateActivityAnswer(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw exception(ANSWER_ACTIVITY_ID_NOT_NULL);
        }
        CommonResult<Integer> validResult = validateSaveReq(reqVO, false);
        if (validResult.isError()) {
            return validResult;
        }
        ActivityAnswerDO old = activityAnswerMapper.selectById(reqVO.getId());
        if (old == null) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND);
        }

        Long activityId = resolveActivityId(old);
        ActivityDO activityDO = activityService.selectById(activityId);
        if (activityDO != null && Objects.equals(activityDO.getIsEnabled(), 1)) {
            throw exception(ANSWER_ACTIVITY_ENABLED_NOT_UPDATE);
        }
        updateMainActivity(activityId, reqVO);
        ActivityAnswerDO answer = BeanUtils.toBean(reqVO, ActivityAnswerDO.class);
        answer.setId(old.getId());
        answer.setActivityId(activityId);
        answer.setPrizePoolRules(Objects.requireNonNullElse(answer.getPrizePoolRules(), 1));
        answer.setCalculationRules(Objects.requireNonNullElse(answer.getCalculationRules(), 1));
        answer.setPlaceOrderProduct(reqVO.getPlaceOrderProduct());
        answer.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
        activityAnswerMapper.updateById(answer);

        clearAnswerCache(activityId);
        saveStoreScope(activityId, reqVO);
        questionMapper.delete(ActivityAnswerQuestionDO::getActivityId, activityId);
        // 删除活动关联商品
        activityAnswerCommodityService.deleteByActivityId(activityId);
        saveQuestions(activityId, reqVO);
        saveRewards(activityId, reqVO);
        saveCommodities(activityId, reqVO);
        syncRewardRecordAndCache(activityId);
        return success(1);
    }

    /**
     * 删除有奖问答活动，启用中的活动不允许删除，同时清理子表和缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> deleteActivityAnswer(Long id) {
        ActivityAnswerDO answer = activityAnswerMapper.selectById(id);
        if (answer == null) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND);
        }
        ActivityDO activityDO = activityService.selectById(resolveActivityId(answer));
        if (activityDO != null && Objects.equals(activityDO.getIsEnabled(), 1)) {
            throw exception(ANSWER_ACTIVITY_ENABLED_NOT_DELETE);
        }
        Long activityId = resolveActivityId(answer);
        clearAnswerCache(activityId);
        removeChildren(activityId);
        activityStoreService.deleteByActivityId(activityId);
        activityMapper.deleteById(activityId);
        activityAnswerMapper.deleteById(id);
        return success(1);
    }

    /**
     * 复制有奖问答活动，复制后默认禁用。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> copyActivityAnswer(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        reqVO.setId(null);
        reqVO.setActivityId(null);
        reqVO.setIsEnabled(0);
        return createActivityAnswer(reqVO);
    }


    /**
     * 查询有奖问答详情，并带出题目、奖励档位、下单商品范围和门店范围。
     */
    @Override
    public ActivityAnswerDetailRespVO selectDetail(Long id) {
        ActivityAnswerDO answer = activityAnswerMapper.selectById(id);
        if (answer == null) {
            return null;
        }
        Long activityId = resolveActivityId(answer);
        ActivityAnswerDetailRespVO respVO = BeanUtils.toBean(answer, ActivityAnswerDetailRespVO.class);
        fillMainActivity(respVO, activityId);
        respVO.setQuestions(BeanUtils.toBean(questionMapper.selectList(ActivityAnswerQuestionDO::getActivityId, activityId),
                ActivityAnswerQuestionReqVO.class));
        List<ActivityAnswerRewardDO> rewardList = rewardMapper.selectList(
                ActivityAnswerRewardDO::getActivityId, activityId);
        syncCommonRewardUsedNum(activityId, rewardList);
        respVO.setRewards(BeanUtils.toBean(rewardList, ActivityAnswerRewardReqVO.class));
        List<Long> commodityIds = selectCommodityIds(activityId);
        respVO.setCommodityIds(commodityIds);
        if (Objects.equals(answer.getPlaceOrderProduct(), 2)) {
            respVO.setCommodityDTOList(activityAnswerCommodityService.listByActivityId(activityId));
        } else {
            respVO.setCommodityDTOList(new ArrayList<>());
        }

        List<Long> storeIds = Objects.equals(respVO.getActivityStore(), 1) ? List.of()
                : activityStoreService.selectStoreIdsByActivityId(activityId);
        respVO.setStoreIds(storeIds);
        respVO.setStoreList(buildStoreList(storeIds));
        fillRewardDisplayNames(respVO.getRewards());
        return respVO;
    }

    /**
     * 分页查询有奖问答活动列表。
     */
    @Override
    public PageResult<ActivityAnswerDO> getActivityAnswerPage(ActivityAnswerPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerDO> wrapper = new LambdaQueryWrapperX<ActivityAnswerDO>()
                .orderByDesc(ActivityAnswerDO::getCreateTime)
                .orderByDesc(ActivityAnswerDO::getId);
        List<Long> activityIds = selectActivityIds(reqVO);
        if (activityIds.isEmpty()) {
            return PageResult.empty();
        }
        wrapper.in(ActivityAnswerDO::getActivityId, activityIds);
        PageResult<ActivityAnswerDO> pageResult = activityAnswerMapper.selectPage(reqVO, wrapper);
        fillMainActivityForPage(pageResult.getList());
        return pageResult;
    }

    /**
     * 更新有奖问答启用状态，并同步缓存和库存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateState(ActivityAnswerStateReqVO reqVO) {
        ActivityAnswerDO old = activityAnswerMapper.selectById(reqVO.getId());
        if (old == null) {
            return 0;
        }
        Long activityId = resolveActivityId(old);
        activityService.updateStatus(activityId, reqVO.getIsEnabled());
        syncRewardRecordAndCache(activityId);
        return 1;
    }

    /**
     * 分页查询用户答题记录。
     */
    @Override
    public PageResult<ActivityAnswerRecordRespVO> getRecordPage(ActivityAnswerRecordPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerRecordDO> wrapper = buildRecordWrapper(reqVO)
                .orderByDesc(ActivityAnswerRecordDO::getCreateTime)
                .orderByDesc(ActivityAnswerRecordDO::getId);
        PageResult<ActivityAnswerRecordRespVO> pageResult = BeanUtils.toBean(
                recordMapper.selectPage(reqVO, wrapper), ActivityAnswerRecordRespVO.class);
        pageResult.getList().forEach(item -> {
            item.setParticipationTime(item.getStartTime());
            item.setActivityTime(item.getStartTime());
        });
        return pageResult;
    }

    /**
     * 统计答题次数、提交次数和参与手机号数量。
     */
    @Override
    public ActivityAnswerStatisticsRespVO getRecordCount(ActivityAnswerRecordPageReqVO reqVO) {
        Map<String, Object> stat = recordMapper.selectRecordStatistics(reqVO.getActivityId(), reqVO.getMemberMobile(),
                reqVO.getStoreId(), reqVO.getQueryStatuses(), reqVO.getStartTime(), reqVO.getEndTime());
        Long answerCount = toLong(stat == null ? null : stat.get("answerCount"));
        Long submitCount = toLong(stat == null ? null : stat.get("submitCount"));
        Long memberCount = toLong(stat == null ? null : stat.get("memberCount"));
        return new ActivityAnswerStatisticsRespVO(answerCount, submitCount, memberCount);
    }

    /**
     * 导出答题记录，题目列按答题明细动态生成。
     */
    @Override
    public void exportRecord(ActivityAnswerRecordPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerRecordDO> exportWrapper = buildRecordWrapper(reqVO)
                .orderByDesc(ActivityAnswerRecordDO::getCreateTime)
                .orderByDesc(ActivityAnswerRecordDO::getId);
        List<ActivityAnswerRecordDO> allRecords = recordMapper.selectList(buildRecordWrapper(reqVO));
        List<ActivityAnswerRecordDetailDO> allDetails = selectRecordExportDetails(allRecords);
        List<ActivityAnswerRecordDetailDO> questions = buildExportQuestionColumns(allDetails);
        List<List<String>> head = buildRecordExportHead(questions);
        Page<List<Object>> page = new Page<>(1, EXPORT_PAGE_SIZE);
        activityAnswerDynamicExcelActionService.exportAsyncExcel(page,
                pageParam -> buildRecordExportPage(exportWrapper, questions, pageParam),
                buildExportFileName("有奖问答答题记录"),
                head,
                Collections.emptyList());
    }

    /**
     * 分页查询奖励发放记录。
     */
    @Override
    public PageResult<ActivityAnswerRewardLogRespVO> getRewardLogPage(ActivityAnswerRewardLogPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerRewardLogDO> wrapper = buildRewardLogWrapper(reqVO)
                .orderByDesc(ActivityAnswerRewardLogDO::getCreateTime)
                .orderByDesc(ActivityAnswerRewardLogDO::getId);
        PageResult<ActivityAnswerRewardLogRespVO> pageResult = BeanUtils.toBean(
                rewardLogMapper.selectPage(reqVO, wrapper), ActivityAnswerRewardLogRespVO.class);
        fillRewardLogEffectiveStatus(pageResult.getList());
        return pageResult;
    }

    /**
     * 统计奖励发放次数和按手机号去重的发放人数。
     */
    @Override
    public ActivityAnswerRewardStatisticsRespVO getRewardLogCount(ActivityAnswerRewardLogPageReqVO reqVO) {
        Map<String, Object> stat = rewardLogMapper.selectRewardLogStatistics(reqVO.getActivityId(), reqVO.getMemberMobile(),
                reqVO.getQueryPrizeType(), reqVO.getPrizeState(), reqVO.getClaimStatus(), reqVO.getQueryAddressStatus(),
                reqVO.getQueryExpressStatus(), reqVO.getStartTime(), reqVO.getEndTime(), LocalDateTime.now().minusHours(24));
        Long rewardCount = toLong(stat == null ? null : stat.get("rewardCount"));
        Long memberCount = toLong(stat == null ? null : stat.get("memberCount"));
        return new ActivityAnswerRewardStatisticsRespVO(rewardCount, memberCount);
    }

    /**
     * 导出奖励发放记录。
     */
    @Override
    public void exportRewardLog(ActivityAnswerRewardLogPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerRewardLogDO> exportWrapper = buildRewardLogWrapper(reqVO)
                .orderByDesc(ActivityAnswerRewardLogDO::getCreateTime)
                .orderByDesc(ActivityAnswerRewardLogDO::getId);
        Page<ActivityAnswerRewardLogExportRespVO> page = new Page<>(1, EXPORT_PAGE_SIZE);
        activityAnswerRewardExcelActionService.exportAsyncExcel(
                ActivityAnswerRewardLogExportRespVO.class,
                page,
                pageParam -> buildRewardLogExportPage(exportWrapper, pageParam),
                buildExportFileName("有奖问答发放记录"));
    }

    /**
     * 分页构建答题记录导出数据。
     */
    private List<List<Object>> buildRecordExportPage(LambdaQueryWrapperX<ActivityAnswerRecordDO> wrapper,
                                                     List<ActivityAnswerRecordDetailDO> questions,
                                                     Page<List<Object>> pageParam) {
        Page<ActivityAnswerRecordDO> queryPage = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        List<ActivityAnswerRecordDO> records = activityAnswerExportQueryService.selectRecordPage(queryPage, wrapper).getRecords();
        List<ActivityAnswerRecordDetailDO> details = selectRecordExportDetails(records);
        Map<Long, Map<String, ActivityAnswerRecordDetailDO>> detailMap = buildRecordQuestionResultMap(details);
        return records.stream()
                .map(record -> toObjectRow(buildRecordExportRow(record, questions, detailMap.get(record.getId()))))
                .toList();
    }

    /**
     * 分页构建奖励发放记录导出数据。
     */
    private List<ActivityAnswerRewardLogExportRespVO> buildRewardLogExportPage(
            LambdaQueryWrapperX<ActivityAnswerRewardLogDO> wrapper,
            Page<ActivityAnswerRewardLogExportRespVO> pageParam) {
        Page<ActivityAnswerRewardLogDO> queryPage = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        List<ActivityAnswerRewardLogRespVO> logs = BeanUtils.toBean(
                activityAnswerExportQueryService.selectRewardLogPage(queryPage, wrapper).getRecords(),
                ActivityAnswerRewardLogRespVO.class);
        fillRewardLogEffectiveStatus(logs);
        return logs.stream().map(log -> {
            ActivityAnswerRewardLogExportRespVO exportVO = new ActivityAnswerRewardLogExportRespVO();
            exportVO.setMemberName(nullToEmpty(log.getMemberName()));
            exportVO.setMemberMobile(nullToEmpty(log.getMemberMobile()));
            exportVO.setAnswerNo(nullToEmpty(log.getAnswerNo()));
            exportVO.setPrizeTypeName(prizeTypeText(log.getPrizeType()));
            exportVO.setPrizeName(nullToEmpty(log.getPrizeName()));
            exportVO.setPrizeImgUrl(nullToEmpty(log.getPrizeImgUrl()));
            exportVO.setGrantTime(formatTime(log.getGrantTime()));
            exportVO.setClaimStatusName(claimStatusText(
                    effectiveClaimStatus(log.getPrizeType(), log.getClaimStatus(), log.getGrantTime())));
            exportVO.setReceiveUser(nullToEmpty(log.getReceiveUser()));
            exportVO.setReceiveMobile(nullToEmpty(log.getReceiveMobile()));
            exportVO.setReceiveAddress(nullToEmpty(log.getReceiveAddress()));
            exportVO.setExpressCompany(nullToEmpty(log.getExpressCompany()));
            exportVO.setTrackingNumber(nullToEmpty(log.getTrackingNumber()));
            return exportVO;
        }).toList();
    }

    /**
     * 转换为动态导出需要的 Object 行。
     */
    private List<Object> toObjectRow(List<String> row) {
        return new ArrayList<>(row);
    }

    /**
     * 构建导出文件名。
     */
    private String buildExportFileName(String prefix) {
        return prefix + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * 更新实物奖品快递信息。
     */
    @Override
    public Integer updateExpress(ActivityAnswerUpdateExpressReqVO reqVO) {
        ActivityAnswerRewardLogDO rewardLog = rewardLogMapper.selectOne(
                new LambdaQueryWrapperX<ActivityAnswerRewardLogDO>()
                        .eq(ActivityAnswerRewardLogDO::getId, reqVO.getId())
                        .last("LIMIT 1"));
        if (rewardLog == null) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "奖励记录不存在");
        }
        if (!Objects.equals(rewardLog.getPrizeType(), 4)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "非实物奖品无需填写物流单号");
        }
        if (Objects.equals(rewardLog.getPrizeState(), 9)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "奖品已退回，不能填写物流单号");
        }
        return rewardLogMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getId, rewardLog.getId())
                .eq(ActivityAnswerRewardLogDO::getMemberMobile, rewardLog.getMemberMobile())
                .set(ActivityAnswerRewardLogDO::getExpressCompany, reqVO.getExpressCompany())
                .set(ActivityAnswerRewardLogDO::getTrackingNumber, reqVO.getTrackingNumber())
                .set(ActivityAnswerRewardLogDO::getPrizeState, 3));
    }

    /**
     * 查询有奖问答活动推广配置。
     */
    @Override
    public ActivityAnswerSpreadRespVO selectSpread(Long id) {
        ActivityAnswerDO answer = selectAnswerByIdOrActivityId(id);
        if (answer == null) {
            return null;
        }
        Long activityId = resolveActivityId(answer);
        ActivityAnswerSpreadRespVO respVO = new ActivityAnswerSpreadRespVO();
        respVO.setId(activityId);
        respVO.setShareTitle(answer.getShareTitle());
        respVO.setShareNote(answer.getShareNote());
        respVO.setShareImgUrl(answer.getShareImgUrl());
        List<ActivityChannelDO> channels = activityChannelService.selectByActivityId(activityId);
        if (!CollectionUtils.isEmpty(channels)) {
            respVO.setActivityChannelRespVOS(channels.stream()
                    .map(channel -> BeanUtils.toBean(channel, ActivityChannelRespVO.class))
                    .collect(Collectors.toList()));
        }
        return respVO;
    }

    /**
     * 修改有奖问答活动推广配置，并刷新推广渠道。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpread(ActivityAnswerSpreadSaveReqVO reqVO) {
        ActivityAnswerDO answer = selectAnswerByIdOrActivityId(reqVO.getId());
        if (answer == null) {
            return;
        }
        Long activityId = resolveActivityId(answer);
        ActivityAnswerDO updateDO = new ActivityAnswerDO();
        updateDO.setId(answer.getId());
        updateDO.setShareTitle(reqVO.getShareTitle());
        updateDO.setShareNote(reqVO.getShareNote());
        updateDO.setShareImgUrl(reqVO.getShareImgUrl());
        updateDO.setUpdateUserName(SecurityFrameworkUtils.getLoginUsername());
        activityAnswerMapper.updateById(updateDO);

        List<Long> channelIds = CollectionUtils.isEmpty(reqVO.getActivityChannelList()) ? List.of()
                : reqVO.getActivityChannelList().stream()
                .map(ActivityChannelSaveReqVO::getChannelId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        activityChannelService.refreshByActivityId(activityId, channelIds, ActivityChannelTypeEnum.DT.getCode());
        syncRewardRecordAndCache(activityId);
    }

    /**
     * 获取活动概览数据，统计口径以答题记录和奖励记录为准。
     */
    @Override
    public Map<String, Object> getActivityAnswerAnalysis(Long id) {
        ActivityAnswerDO answer = selectAnswerByIdOrActivityId(id);
        Long activityId = answer == null ? id : resolveActivityId(answer);
        ActivityDO activity = activityMapper.selectById(activityId);
        Map<String, Object> result = new HashMap<>();
        long answerCount = recordMapper.selectCount(ActivityAnswerRecordDO::getActivityId, activityId);
        long submitCount = recordMapper.selectCount(new LambdaQueryWrapper<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getActivityId, activityId)
                .eq(ActivityAnswerRecordDO::getStatus, 1));
        Map<String, Object> recordStat = recordMapper.selectRecordStatistics(activityId, null, null, null, null, null);
        long participantCount = toLong(recordStat == null ? null : recordStat.get("memberCount"));
        List<ActivityAnswerRewardLogDO> rewardLogs = rewardLogMapper.selectList(ActivityAnswerRewardLogDO::getActivityId, activityId);
        BigDecimal pointsCount = sumPrizeValue(rewardLogs, 1);
        long couponCount = rewardLogs.stream()
                .filter(log -> Objects.equals(log.getPrizeType(), 2) || Objects.equals(log.getPrizeType(), 3))
                .count();
        BigDecimal redPacketAmount = sumPrizeValue(rewardLogs, 5);

        Map<String, Long> eventStats = getAnswerEventStats(id, answer, activity);
        long pvCount = eventStats.getOrDefault("pv", 0L);
        long uvCount = eventStats.getOrDefault("uv", 0L);
        result.put("pvCount", pvCount);
        result.put("uvCount", uvCount);
        result.put("pv", pvCount);
        result.put("uv", uvCount);
        result.put("participantCount", participantCount);
        // 兼容现有营销活动统计页面的参与人数字段命名。
        result.put("participationCount", participantCount);
        result.put("participation", participantCount);
        result.put("shareCount", eventStats.getOrDefault("shareCount", 0L));
        result.put("answerCount", answerCount);
        result.put("submitCount", submitCount);
        result.put("completeRate", calculateRate(submitCount, answerCount));
        result.put("pointsCount", pointsCount);
        result.put("couponCount", couponCount);
        result.put("redPacketAmount", redPacketAmount);
        result.put("rewardCount", (long) rewardLogs.size());
        result.put("questionCount", questionMapper.selectCount(ActivityAnswerQuestionDO::getActivityId, activityId));
        return result;
    }

    /**
     * 查询有奖问答每日趋势，PV/UV 暂无事件埋点时返回 0。
     */
    @Override
    public Map<String, Object> getActivityAnswerDailyAnalysis(ActivityAnswerAnalysisReqVO reqVO) {
        ActivityAnswerDO answer = selectAnswerByIdOrActivityId(reqVO.getActivityId());
        Long activityId = answer == null ? reqVO.getActivityId() : resolveActivityId(answer);
        LocalDateTime startTime = reqVO.getStartTime();
        LocalDateTime endTime = reqVO.getEndTime();
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);
            startTime = endTime.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        } else if (startTime == null) {
            startTime = endTime.minusDays(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        } else if (endTime == null) {
            endTime = startTime.plusDays(29).withHour(23).withMinute(59).withSecond(59).withNano(0);
        }
        startTime = normalizeQueryStartTime(startTime);
        endTime = normalizeQueryEndTime(endTime);

        List<Map<String, Object>> rows = recordMapper.selectDailyAnswerStats(
                activityId, startTime, endTime);
        Map<String, Map<String, Long>> eventDailyStats = getAnswerDailyEventStats(
                reqVO.getActivityId(), startTime, endTime);
        Map<String, Map<String, Long>> statMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String statDate = String.valueOf(row.get("statDate"));
            Map<String, Long> dayStat = new HashMap<>();
            dayStat.put("answerCount", toLong(row.get("answerCount")));
            dayStat.put("submitCount", toLong(row.get("submitCount")));
            statMap.put(statDate, dayStat);
        }

        List<String> dateList = new ArrayList<>();
        List<Long> pvValues = new ArrayList<>();
        List<Long> uvValues = new ArrayList<>();
        List<Long> answerValues = new ArrayList<>();
        List<Long> submitValues = new ArrayList<>();

        for (LocalDate current = startTime.toLocalDate(); !current.isAfter(endTime.toLocalDate()); current = current.plusDays(1)) {
            String statDate = current.toString();
            Map<String, Long> dayStat = statMap.getOrDefault(statDate, Collections.emptyMap());
            dateList.add(statDate);
            Map<String, Long> eventDayStat = eventDailyStats.getOrDefault(statDate, Collections.emptyMap());
            pvValues.add(eventDayStat.getOrDefault("pv", 0L));
            uvValues.add(eventDayStat.getOrDefault("uv", 0L));
            answerValues.add(dayStat.getOrDefault("answerCount", 0L));
            submitValues.add(dayStat.getOrDefault("submitCount", 0L));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("dateList", dateList);
        result.put("pvValues", pvValues);
        result.put("uvValues", uvValues);
        result.put("answerValues", answerValues);
        result.put("submitValues", submitValues);
        return result;
    }

    /**
     * 查询题目作答分析。
     */
    @Override
    public List<ActivityAnswerQuestionAnalysisRespVO> getQuestionAnalysis(ActivityAnswerAnalysisReqVO reqVO) {
        LocalDateTime startTime = normalizeQueryStartTime(reqVO.getStartTime());
        LocalDateTime endTime = normalizeQueryEndTime(reqVO.getEndTime());
        return recordDetailMapper.selectQuestionAnalysis(reqVO.getActivityId(), startTime, endTime)
                .stream().map(stat -> {
            long answerCount = toLong(stat.get("answerCount"));
            long correctCount = toLong(stat.get("correctCount"));
            long wrongCount = toLong(stat.get("wrongCount"));
            ActivityAnswerQuestionAnalysisRespVO respVO = new ActivityAnswerQuestionAnalysisRespVO();
            respVO.setQuestionId(toLong(stat.get("questionId")));
            respVO.setQuestionTitle(Objects.toString(stat.get("questionTitle"), ""));
            respVO.setAnswerCount(answerCount);
            respVO.setCorrectCount(correctCount);
            respVO.setWrongCount(wrongCount);
            respVO.setAccuracy(calculateRate(correctCount, answerCount));
            return respVO;
        }).toList();
    }

    /**
     * 扣减共用奖励库存，供小程序提交答题发奖流程复用。
     */
    public boolean increaseRewardUsedNum(Long rewardId) {
        return rewardMapper.increaseUsedNum(rewardId) > 0;
    }

    /**
     * 扣减门店独立奖励库存，供小程序提交答题发奖流程复用。
     */
    public boolean increaseStoreRewardUsedNum(Long activityId, Long rewardId, Long storeId) {
        String stockKey = buildStoreStockKey(activityId, storeId, rewardId);
        String lockKey = stockKey + ":lock";
        String lockValue = UUID.randomUUID().toString();
        if (!redisCache.lock(lockKey, lockValue, 5)) {
            return false;
        }
        try {
            ActivityAnswerRewardDO reward = rewardMapper.selectById(rewardId);
            if (reward == null) {
                return false;
            }
            int totalNum = Objects.requireNonNullElse(reward.getTotalNum(), 0);
            int usedNum = toLong(redisCache.getCacheObject(stockKey)).intValue();
            if (totalNum > 0 && usedNum >= totalNum) {
                return false;
            }
            redisCache.increment(stockKey);
            return true;
        } finally {
            redisCache.unlock(lockKey, lockValue);
        }
    }

    /**
     * 校验有奖问答新增或修改入参。
     */
    private CommonResult<Integer> validateSaveReq(ActivityAnswerSaveOrUpdateReqVO reqVO, boolean create) {
        if (create && reqVO.getId() != null) {
            throw exception(ANSWER_CREATE_ID_NOT_ALLOWED);
        }
        Date startDate = resolveStartDate(reqVO);
        Date endDate = resolveEndDate(reqVO);
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw exception(ANSWER_TIME_RANGE_ERROR);
        }
        if (CollectionUtils.isEmpty(reqVO.getQuestions())) {
            throw exception(ANSWER_QUESTION_NOT_EMPTY);
        }
        CommonResult<Integer> questionResult = validateQuestions(reqVO.getQuestions());
        if (questionResult.isError()) {
            return questionResult;
        }
        CommonResult<Integer> rewardResult = validateRewards(reqVO.getRewards(), reqVO.getQuestions().size());
        if (rewardResult.isError()) {
            return rewardResult;
        }
        if (Objects.equals(reqVO.getPrizePoolRules(), 2) && CollectionUtils.isEmpty(resolveStoreIds(reqVO))) {
            throw exception(ANSWER_STORE_REQUIRED_FOR_INDEPENDENT_STOCK);
        }
        if (Objects.equals(reqVO.getPlaceOrderProduct(), 2) && CollectionUtils.isEmpty(reqVO.getCommodityIds())) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "请选择下单任务指定商品");
        }
        return success(1);
    }

    /**
     * 校验题目选项和正确答案。
     */
    private CommonResult<Integer> validateQuestions(List<ActivityAnswerQuestionReqVO> questions) {
        for (ActivityAnswerQuestionReqVO question : questions) {
            if (!Objects.equals(Objects.requireNonNullElse(question.getQuestionType(), 1), 1)) {
                throw exception(ANSWER_QUESTION_TYPE_ONLY_CHOICE);
            }
            String correctAnswer = question.getCorrectAnswer();
            if (!StringUtils.hasText(correctAnswer) || correctAnswer.contains(",")
                    || correctAnswer.trim().startsWith("[") || correctAnswer.trim().startsWith("{")) {
                throw exception(ANSWER_QUESTION_ONE_CORRECT);
            }
            Set<String> optionCodes = parseOptionCodes(question.getOptionsJson());
            if (optionCodes.size() < 2) {
                throw exception(ANSWER_QUESTION_OPTIONS_MIN);
            }
            if (!optionCodes.contains(correctAnswer.trim())) {
                throw exception(ANSWER_QUESTION_CORRECT_IN_OPTIONS);
            }
        }
        return success(1);
    }

    /**
     * 从选项 JSON 中解析可选答案编码。
     */
    private Set<String> parseOptionCodes(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            return Collections.emptySet();
        }
        JSONArray options = JSON.parseArray(optionsJson);
        Set<String> codes = new LinkedHashSet<>();
        for (Object option : options) {
            if (option instanceof String str && StringUtils.hasText(str)) {
                codes.add(str.trim());
            } else if (option instanceof JSONObject object) {
                String code = firstText(object, "code", "key", "value");
                if (StringUtils.hasText(code)) {
                    codes.add(code.trim());
                }
            }
        }
        return codes;
    }

    /**
     * 按候选 key 获取第一个非空文本。
     */
    private String firstText(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.getString(key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 校验奖励档位和答对题数唯一性。
     */
    private CommonResult<Integer> validateRewards(List<ActivityAnswerRewardReqVO> rewards, int questionCount) {
        if (CollectionUtils.isEmpty(rewards)) {
            return success(1);
        }
        Set<Integer> correctCounts = new HashSet<>();
        for (ActivityAnswerRewardReqVO reward : rewards) {
            Integer correctCount = reward.getCorrectCount();
            if (correctCount == null || correctCount < 0 || correctCount > questionCount) {
                throw exception(ANSWER_REWARD_CORRECT_COUNT_RANGE);
            }
            if (!correctCounts.add(correctCount)) {
                throw exception(ANSWER_REWARD_CORRECT_COUNT_DUPLICATE);
            }
        }
        return success(1);
    }

    /**
     * 保存活动参与门店范围。
     */
    private void saveStoreScope(Long activityId, ActivityAnswerSaveOrUpdateReqVO reqVO) {
        activityStoreService.deleteByActivityId(activityId);
        if (Objects.equals(reqVO.getActivityStore(), 1)) {
            return;
        }
        List<Long> storeIds = resolveStoreIds(reqVO);
        if (!CollectionUtils.isEmpty(storeIds)) {
            activityStoreService.createBatch(storeIds, activityId);
        }
    }

    /**
     * 创建活动主表，时间规则字段参考抽奖 saveLottery 保存到 activity。
     */
    private Long createMainActivity(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        if (reqVO.getActivityId() != null) {
            return reqVO.getActivityId();
        }
        ActivityDO activityDO = buildMainActivity(null, reqVO);
        activityDO.setActivityType(ActivityTypeEnum.ANSWER.getCode());
        activityDO.setIsEnabled(Objects.requireNonNullElse(reqVO.getIsEnabled(), 0));
        return activityService.createActivity(activityDO);
    }

    /**
     * 修改活动主表，保持和抽奖一样同步日期、周几、场次时间段。
     */
    private void updateMainActivity(Long activityId, ActivityAnswerSaveOrUpdateReqVO reqVO) {
        ActivityDO activityDO = buildMainActivity(activityId, reqVO);
        activityDO.setId(activityId);
        activityDO.setActivityType(ActivityTypeEnum.ANSWER.getCode());
        activityDO.setIsEnabled(Objects.requireNonNullElse(reqVO.getIsEnabled(), 0));
        activityService.updateActivity(activityDO);
    }

    /**
     * 构建活动主表保存对象。
     */
    private ActivityDO buildMainActivity(Long activityId, ActivityAnswerSaveOrUpdateReqVO reqVO) {
        ActivityDO activityDO = new ActivityDO();
        activityDO.setId(activityId);
        activityDO.setActivityName(reqVO.getActivityName());
        activityDO.setActivityRules(reqVO.getActivityRules());
        activityDO.setActivityStore(Objects.requireNonNullElse(reqVO.getActivityStore(), 0));
        activityDO.setStartDate(resolveStartDate(reqVO));
        activityDO.setEndDate(resolveEndDate(reqVO));
        activityDO.setDayNumbers(convertListToString(reqVO.getDayNumberList(), null));
        activityDO.setWeekNumbers(convertListToString(reqVO.getWeekNumberList(), null));
        activityDO.setTimeRange(convertListToString(reqVO.getTimeRangeList(), null));
        activityDO.setCommunityFlag(reqVO.getCommunityFlag());
        activityDO.setGuideImage(reqVO.getGuideImage());
        activityDO.setActivityRemark(reqVO.getActivityRemark());
        return activityDO;
    }

    /**
     * 详情回填 activity 主表里的时间规则，前端可继续按抽奖字段渲染。
     */
    private void fillMainActivity(ActivityAnswerDetailRespVO respVO, Long activityId) {
        ActivityDO activityDO = activityService.selectById(activityId);
        if (activityDO == null) {
            return;
        }
        respVO.setStartDate(activityDO.getStartDate());
        respVO.setActivityRemark(activityDO.getActivityRemark());
        respVO.setEndDate(activityDO.getEndDate());
        respVO.setAnswerStartTime(toLocalDate(activityDO.getStartDate()));
        respVO.setAnswerEndTime(toLocalDate(activityDO.getEndDate()));
        respVO.setDayNumbers(activityDO.getDayNumbers());
        respVO.setWeekNumbers(activityDO.getWeekNumbers());
        respVO.setTimeRange(activityDO.getTimeRange());
        respVO.setDayNumberList(parseIntegerList(activityDO.getDayNumbers()));
        respVO.setWeekNumberList(parseIntegerList(activityDO.getWeekNumbers()));
        respVO.setTimeRangeList(parseStringList(activityDO.getTimeRange()));
        respVO.setActivityName(activityDO.getActivityName());
        respVO.setActivityRules(activityDO.getActivityRules());
        respVO.setIsEnabled(activityDO.getIsEnabled());
        respVO.setActivityStore(activityDO.getActivityStore());
    }

    /**
     * 分页结果批量回填活动主表标题、规则和状态。
     */
    private void fillMainActivityForPage(List<ActivityAnswerDO> answerList) {
        if (CollectionUtils.isEmpty(answerList)) {
            return;
        }
        List<Long> activityIds = answerList.stream()
                .map(this::resolveActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(activityIds)) {
            return;
        }
        Map<Long, ActivityDO> activityMap = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                        .in(ActivityDO::getId, activityIds))
                .stream()
                .collect(Collectors.toMap(ActivityDO::getId, item -> item, (left, right) -> left));
        for (ActivityAnswerDO answer : answerList) {
            ActivityDO activityDO = activityMap.get(resolveActivityId(answer));
            if (activityDO == null) {
                continue;
            }
            answer.setAnswerTitle(activityDO.getActivityName());
            answer.setAnswerRule(activityDO.getActivityRules());
            answer.setState(activityDO.getIsEnabled());
            answer.setActivityStore(activityDO.getActivityStore());
        }
    }

    /**
     * 解析活动开始日期，优先使用活动主表字段。
     */
    private Date resolveStartDate(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        if (reqVO.getStartDate() != null) {
            return reqVO.getStartDate();
        }
        return null;
    }

    /**
     * 解析活动结束日期，优先使用活动主表字段。
     */
    private Date resolveEndDate(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        if (reqVO.getEndDate() != null) {
            return reqVO.getEndDate();
        }
        return null;
    }

    /**
     * LocalDate 转 Date。
     */
    private Date toDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date 转 LocalDate。
     */
    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 查询活动结束日期用于计算缓存过期时间。
     */
    private LocalDate resolveActivityEndTime(Long activityId) {
        ActivityDO activityDO = activityService.selectById(activityId);
        return activityDO == null ? null : toLocalDate(activityDO.getEndDate());
    }

    /**
     * 按活动主表日期筛选有奖问答活动 ID。
     */
    private List<Long> selectActivityIds(ActivityAnswerPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityDO> wrapper = new LambdaQueryWrapperX<ActivityDO>()
                .eq(ActivityDO::getActivityType, ActivityTypeEnum.ANSWER.getCode())
                .likeIfPresent(ActivityDO::getActivityName, reqVO.getAnswerTitle())
                .eqIfPresent(ActivityDO::getIsEnabled, reqVO.getState())
                .geIfPresent(ActivityDO::getStartDate, toDate(reqVO.getStartTime()))
                .leIfPresent(ActivityDO::getEndDate, toDate(reqVO.getEndTime()));
        return activityMapper.selectList(wrapper).stream()
                .map(ActivityDO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 将前端数组参数转换为逗号分隔字符串。
     */
    private String convertListToString(List<?> list, String fallback) {
        if (!CollectionUtils.isEmpty(list)) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
        return fallback;
    }

    /**
     * 解析逗号分隔的数字列表。
     */
    private List<Integer> parseIntegerList(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 解析逗号分隔的文本列表。
     */
    private List<String> parseStringList(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 根据门店范围配置解析参与门店 ID。
     */
    private List<Long> resolveStoreIds(ActivityAnswerSaveOrUpdateReqVO reqVO) {
        if (Objects.equals(reqVO.getActivityStore(), 1)) {
            CommonResult<List<StoreInfoDTO>> result = storeApi.getAllStoreList();
            if (result == null || CollectionUtils.isEmpty(result.getData())) {
                return List.of();
            }
            return result.getData().stream().map(StoreInfoDTO::getStoreId).filter(Objects::nonNull).distinct().toList();
        }
        if (CollectionUtils.isEmpty(reqVO.getStoreIds())) {
            return List.of();
        }
        return reqVO.getStoreIds().stream().filter(Objects::nonNull).distinct().toList();
    }

    /**
     * 保存有奖问答题目列表。
     */
    /**
     * 解析独立奖池需要写入缓存的门店 ID；全部门店不落关系表，但缓存仍要覆盖所有门店。
     */
    private List<Long> resolveCacheStoreIds(Long activityId, ActivityDO activityDO) {
        if (activityDO != null && Objects.equals(activityDO.getActivityStore(), 1)) {
            CommonResult<List<StoreInfoDTO>> result = storeApi.getAllStoreList();
            if (result == null || CollectionUtils.isEmpty(result.getData())) {
                return List.of();
            }
            return result.getData().stream()
                    .map(StoreInfoDTO::getStoreId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        return activityStoreService.selectStoreIdsByActivityId(activityId);
    }

    private void saveQuestions(Long activityId, ActivityAnswerSaveOrUpdateReqVO reqVO) {
        List<ActivityAnswerQuestionDO> questions = reqVO.getQuestions().stream()
                .map(item -> {
                    ActivityAnswerQuestionDO question = BeanUtils.toBean(item, ActivityAnswerQuestionDO.class);
                    question.setId(null);
                    question.setActivityId(activityId);
                    question.setQuestionType(1);
                    question.setAnswerTime(Objects.requireNonNullElse(question.getAnswerTime(), 0));
                    return question;
                }).toList();
        questionMapper.insertBatch(questions);
    }

    /**
     * 保存奖励档位并保留已有领取数量。
     */
    private void saveRewards(Long activityId, ActivityAnswerSaveOrUpdateReqVO reqVO) {
        List<ActivityAnswerRewardReqVO> rewards = reqVO.getRewards();
        if (CollectionUtils.isEmpty(rewards)) {
            rewardMapper.deleteNotInCorrectCounts(activityId, Collections.emptyList());
            return;
        }

        List<Integer> submitCorrectCounts = rewards.stream()
                .map(ActivityAnswerRewardReqVO::getCorrectCount)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<ActivityAnswerRewardDO> oldRewards = rewardMapper.selectList(ActivityAnswerRewardDO::getActivityId, activityId);
        Map<Integer, ActivityAnswerRewardDO> oldRewardMapForKeep = oldRewards.stream()
                .filter(item -> item.getCorrectCount() != null)
                .collect(Collectors.toMap(ActivityAnswerRewardDO::getCorrectCount, Function.identity(), (left, right) -> left));
        Map<Long, ActivityAnswerRewardDO> oldRewardIdMapForKeep = oldRewards.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ActivityAnswerRewardDO::getId, Function.identity(), (left, right) -> left));

        List<Long> keepIds = rewards.stream()
                .map(item -> {
                    ActivityAnswerRewardDO reward = oldRewardMapForKeep.get(item.getCorrectCount());
                    if (reward != null) {
                        return reward.getId();
                    }
                    reward = oldRewardIdMapForKeep.get(item.getId());
                    return reward == null ? null : reward.getId();
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        rewardMapper.deleteNotInCorrectCounts(activityId, submitCorrectCounts);
        rewardMapper.deleteConflictCorrectCounts(activityId, submitCorrectCounts, keepIds);

        oldRewards = rewardMapper.selectList(ActivityAnswerRewardDO::getActivityId, activityId);
        Map<Integer, ActivityAnswerRewardDO> oldRewardMap = oldRewards.stream()
                .filter(item -> item.getCorrectCount() != null)
                .collect(Collectors.toMap(ActivityAnswerRewardDO::getCorrectCount, Function.identity(), (left, right) -> left));
        Map<Long, ActivityAnswerRewardDO> oldRewardIdMap = oldRewards.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ActivityAnswerRewardDO::getId, Function.identity(), (left, right) -> left));

        for (ActivityAnswerRewardReqVO item : rewards) {
            ActivityAnswerRewardDO reward = BeanUtils.toBean(item, ActivityAnswerRewardDO.class);
            reward.setActivityId(activityId);
            reward.setTotalNum(Objects.requireNonNullElse(reward.getTotalNum(), 0));
            ActivityAnswerRewardDO oldReward = oldRewardMap.get(item.getCorrectCount());
            if (oldReward == null && item.getId() != null) {
                oldReward = oldRewardIdMap.get(item.getId());
            }
            if (oldReward == null) {
                reward.setId(null);
                reward.setUsedNum(0);
                rewardMapper.insert(reward);
                item.setId(reward.getId());
            } else {
                reward.setId(oldReward.getId());
                reward.setUsedNum(null);
                rewardMapper.updateById(reward);
                item.setId(oldReward.getId());
            }
        }
    }

    /**
     * 保存下单任务商品或品类范围。
     */
    private void saveCommodities(Long activityId, ActivityAnswerSaveOrUpdateReqVO reqVO) {
        activityAnswerCommodityService.deleteByActivityId(activityId);
        if (Objects.equals(reqVO.getPlaceOrderProduct(), 2) && ObjectUtil.isNotEmpty(reqVO.getCommodityIds())) {
            List<Long> commodityIds = reqVO.getCommodityIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            activityAnswerCommodityService.createBatch(commodityIds, activityId);
        }
    }

    /**
     * 查询有奖问答指定商品范围。
     */
    private List<Long> selectCommodityIds(Long activityId) {
        List<ActivityAnswerCommodityDO> commodities = activityAnswerCommodityService.selectByActivityId(activityId);
        if (CollectionUtils.isEmpty(commodities)) {
            return Collections.emptyList();
        }
        return commodities.stream()
                .map(ActivityAnswerCommodityDO::getCommodityId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    /**
     * 同步发奖记录库存并重建 Redis 缓存。
     */
    private void syncRewardRecordAndCache(Long activityId) {
        ActivityAnswerDO answer = activityAnswerMapper.selectOne(ActivityAnswerDO::getActivityId, activityId);
        if (answer == null) {
            answer = activityAnswerMapper.selectById(activityId);
        }
        if (answer == null) {
            return;
        }
        clearAnswerCache(activityId);
        List<ActivityAnswerRewardDO> rewards = rewardMapper.selectList(ActivityAnswerRewardDO::getActivityId, activityId);
        syncCommonRewardUsedNum(activityId, rewards);
        writeAnswerCache(answer, rewards);
    }

    /**
     * 按发奖记录同步共用库存已用数量。
     */
    private void syncCommonRewardUsedNum(Long activityId, List<ActivityAnswerRewardDO> rewards) {
        Map<Long, Integer> usedCountMap = buildRewardUsedCountMap(
                rewardLogMapper.selectRewardUsedCountGroupByRewardId(activityId));
        for (ActivityAnswerRewardDO reward : rewards) {
            Integer usedCount = usedCountMap.getOrDefault(reward.getId(), 0);
            ActivityAnswerRewardDO updateDO = new ActivityAnswerRewardDO();
            updateDO.setId(reward.getId());
            updateDO.setUsedNum(usedCount);
            rewardMapper.updateById(updateDO);
            reward.setUsedNum(usedCount);
        }
    }

    /**
     * 按参与门店同步独立库存已用数量。
     */
    /**
     * 转换奖励已用数量统计结果。
     */
    private Map<Long, Integer> buildRewardUsedCountMap(List<Map<String, Object>> countRows) {
        if (CollectionUtils.isEmpty(countRows)) {
            return Collections.emptyMap();
        }
        return countRows.stream()
                .filter(row -> row.get("rewardId") != null && row.get("usedCount") != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("rewardId")).longValue(),
                        row -> ((Number) row.get("usedCount")).intValue(),
                        (oldValue, newValue) -> newValue
                ));
    }

    /**
     * 转换门店维度奖励已用数量统计结果。
     */
    private Map<String, Integer> buildRewardStoreUsedCountMap(List<Map<String, Object>> countRows) {
        if (CollectionUtils.isEmpty(countRows)) {
            return Collections.emptyMap();
        }
        return countRows.stream()
                .filter(row -> row.get("rewardId") != null && row.get("storeId") != null && row.get("usedCount") != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("rewardId")).longValue() + ":" + ((Number) row.get("storeId")).longValue(),
                        row -> ((Number) row.get("usedCount")).intValue(),
                        (oldValue, newValue) -> newValue
                ));
    }

    /**
     * 写入活动配置、题目、奖励和库存缓存。
     */
    private void writeAnswerCache(ActivityAnswerDO answer, List<ActivityAnswerRewardDO> rewards) {
        Long activityId = resolveActivityId(answer);
        ActivityDO activityDO = activityService.selectById(activityId);
        if (activityDO != null) {
            answer.setAnswerTitle(activityDO.getActivityName());
            answer.setAnswerRule(activityDO.getActivityRules());
            answer.setState(activityDO.getIsEnabled());
            answer.setActivityStore(activityDO.getActivityStore());
        }
        int expireSeconds = calculateActivityExpireSeconds(resolveActivityEndTime(activityId));
        List<ActivityAnswerQuestionDO> questions = questionMapper.selectList(ActivityAnswerQuestionDO::getActivityId, activityId);
        List<Long> storeIds = resolveCacheStoreIds(activityId, activityDO);
        List<Long> commodityIds = selectCommodityIds(activityId);
        ActivityAnswerSettingsCacheDataVO cacheData = new ActivityAnswerSettingsCacheDataVO();
        cacheData.setActivityId(activityId);
        cacheData.setAnswer(answer);
        cacheData.setActivity(activityDO);
        cacheData.setStoreIds(storeIds);
        cacheData.setCommodityIds(commodityIds);
        cacheData.setQuestions(questions == null ? Collections.emptyList() : questions);
        cacheData.setRewards(rewards == null ? Collections.emptyList() : rewards);
        redisCache.setCacheObject(RedisKeyConstants.ANSWER_CACHE + activityId, cacheData, expireSeconds, TimeUnit.SECONDS);
        redisCache.setCacheObject(RedisKeyConstants.ANSWER_SETTING + activityId, answer, expireSeconds, TimeUnit.SECONDS);
        redisCache.setCacheObject(RedisKeyConstants.ANSWER_QUESTION + activityId, questions, expireSeconds, TimeUnit.SECONDS);

        if (Objects.equals(answer.getPrizePoolRules(), 2)) {
            Map<String, Integer> usedCountMap = buildRewardStoreUsedCountMap(
                    rewardLogMapper.selectRewardUsedCountGroupByRewardIdAndStoreId(activityId));
            for (Long storeId : storeIds) {
                List<ActivityAnswerRewardDO> storeRewards = buildStoreRewardCache(rewards, storeId, usedCountMap);
                redisCache.setCacheObject(RedisKeyConstants.ANSWER_REWARD + activityId + ":store:" + storeId,
                        storeRewards, expireSeconds, TimeUnit.SECONDS);
                for (ActivityAnswerRewardDO reward : storeRewards) {
                    redisCache.setCacheObject(buildStoreStockKey(activityId, storeId, reward.getId()),
                            Objects.requireNonNullElse(reward.getUsedNum(), 0), expireSeconds, TimeUnit.SECONDS);
                }
            }
            return;
        }

        redisCache.setCacheObject(RedisKeyConstants.ANSWER_REWARD + activityId, rewards, expireSeconds, TimeUnit.SECONDS);
        for (ActivityAnswerRewardDO reward : rewards) {
            redisCache.setCacheObject(buildCommonStockKey(activityId, reward.getId()),
                    Objects.requireNonNullElse(reward.getUsedNum(), 0), expireSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 构建门店独立库存奖励缓存对象。
     */
    private List<ActivityAnswerRewardDO> buildStoreRewardCache(List<ActivityAnswerRewardDO> rewards, Long storeId,
                                                               Map<String, Integer> usedCountMap) {
        return rewards.stream().map(reward -> {
            ActivityAnswerRewardDO copy = BeanUtils.toBean(reward, ActivityAnswerRewardDO.class);
            copy.setUsedNum(usedCountMap.getOrDefault(reward.getId() + ":" + storeId, 0));
            return copy;
        }).toList();
    }

    /**
     * 清理指定活动的有奖问答缓存。
     */
    /**
     * 生成复制活动名称，避免和原活动重名。
     */
    private String buildCopyActivityName(String activityName) {
        String baseName = StringUtils.hasText(activityName) ? activityName : "有奖问答";
        return baseName + "-复制" + System.currentTimeMillis();
    }

    /**
     * 根据门店 ID 回填门店名称。
     */
    private List<ActivityAnswerStoreRespVO> buildStoreList(List<Long> storeIds) {
        if (CollectionUtils.isEmpty(storeIds)) {
            return List.of();
        }
        CommonResult<List<StoreInfoDTO>> result = storeApi.getStoresByStoreIds(storeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        if (result == null || CollectionUtils.isEmpty(result.getData())) {
            return List.of();
        }
        return result.getData().stream().map(store -> {
            ActivityAnswerStoreRespVO respVO = new ActivityAnswerStoreRespVO();
            respVO.setStoreId(store.getStoreId());
            respVO.setStoreName(store.getStoreName());
            return respVO;
        }).toList();
    }

    /**
     * 奖品为优惠券或优惠券包时，补充奖品展示名称。
     */
    private void fillRewardDisplayNames(List<ActivityAnswerRewardReqVO> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            return;
        }
        List<Long> couponIds = rewards.stream()
                .filter(item -> Objects.equals(item.getPrizeType(), 2))
                .map(ActivityAnswerRewardReqVO::getAwardId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Long> packageIds = rewards.stream()
                .filter(item -> Objects.equals(item.getPrizeType(), 3))
                .map(ActivityAnswerRewardReqVO::getAwardId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> couponNameMap = CollectionUtils.isEmpty(couponIds)
                ? Collections.emptyMap() : goodCouponApi.getCouponNameMap(couponIds);
        Map<Long, String> packageNameMap = CollectionUtils.isEmpty(packageIds)
                ? Collections.emptyMap() : couponPackageApi.getPackageNameMap(packageIds);

        for (ActivityAnswerRewardReqVO reward : rewards) {
            if (Objects.equals(reward.getPrizeType(), 2)) {
                String couponName = couponNameMap.get(reward.getAwardId());
                reward.setCouponName(couponName);
                if (StringUtils.hasText(couponName)) {
                    reward.setPrizeName(couponName);
                }
            } else if (Objects.equals(reward.getPrizeType(), 3)) {
                String packageName = packageNameMap.get(reward.getAwardId());
                reward.setPackageName(packageName);
                if (StringUtils.hasText(packageName)) {
                    reward.setPrizeName(packageName);
                }
            }
        }
    }

    /**
     * 按奖品类型汇总发放金额或积分。
     */
    private BigDecimal sumPrizeValue(List<ActivityAnswerRewardLogDO> rewardLogs, Integer prizeType) {
        if (CollectionUtils.isEmpty(rewardLogs)) {
            return BigDecimal.ZERO;
        }
        return rewardLogs.stream()
                .filter(log -> Objects.equals(log.getPrizeType(), prizeType))
                .map(ActivityAnswerRewardLogDO::getPrizeValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算百分比，返回保留两位小数的数值。
     */
    private BigDecimal calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    /**
     * 兼容不同 JDBC 类型转 Long。
     */
    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private void clearAnswerCache(Long activityId) {
        deleteByPattern(RedisKeyConstants.ANSWER_CACHE + activityId + "*");
        deleteByPattern(RedisKeyConstants.ANSWER_SETTING + activityId + "*");
        deleteByPattern(RedisKeyConstants.ANSWER_QUESTION + activityId + "*");
        deleteByPattern(RedisKeyConstants.ANSWER_REWARD + activityId + "*");
        deleteByPattern(RedisKeyConstants.ANSWER_REWARD_STOCK + activityId + "*");
        deleteByPattern(RedisKeyConstants.ANSWER_NUM + activityId + "*");
    }

    /**
     * 按 Redis key 模式批量删除缓存。
     */
    private void deleteByPattern(String pattern) {
        Collection<String> keys = redisCache.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisCache.deleteObject(keys);
        }
    }

    /**
     * 计算活动配置类缓存过期秒数。
     */
    private int calculateActivityExpireSeconds(LocalDate answerEndTime) {
        if (answerEndTime == null) {
            return DEFAULT_CACHE_SECONDS;
        }
        long seconds = Duration.between(LocalDateTime.now(), LocalDateTime.of(answerEndTime, LocalTime.MAX)).getSeconds();
        return (int) Math.max(seconds, DEFAULT_CACHE_SECONDS);
    }

    /**
     * 构建共用库存缓存 key。
     */
    private String buildCommonStockKey(Long activityId, Long rewardId) {
        return RedisKeyConstants.ANSWER_REWARD_STOCK + activityId + ":reward:" + rewardId;
    }

    /**
     * 构建门店独立库存缓存 key。
     */
    private String buildStoreStockKey(Long activityId, Long storeId, Long rewardId) {
        return RedisKeyConstants.ANSWER_REWARD_STOCK + activityId + ":store:" + storeId + ":reward:" + rewardId;
    }

    /**
     * 删除活动关联的题目、奖励、库存和商品范围。
     */
    private void removeChildren(Long activityId) {
        questionMapper.delete(ActivityAnswerQuestionDO::getActivityId, activityId);
        rewardMapper.delete(ActivityAnswerRewardDO::getActivityId, activityId);
        activityAnswerCommodityService.deleteByActivityId(activityId);
    }

    /**
     * 解析活动主表 ID，兼容历史 id 即 activityId 的数据。
     */
    private Long resolveActivityId(ActivityAnswerDO answer) {
        return answer.getActivityId() == null ? answer.getId() : answer.getActivityId();
    }

    /**
     * 根据有奖问答配置 ID 或活动主表 ID 查询配置。
     */
    private ActivityAnswerDO selectAnswerByIdOrActivityId(Long id) {
        if (id == null) {
            return null;
        }
        ActivityAnswerDO answer = activityAnswerMapper.selectById(id);
        if (answer != null) {
            return answer;
        }
        return activityAnswerMapper.selectOne(ActivityAnswerDO::getActivityId, id);
    }

    /**
     * 统计有奖问答活动的 PV、UV 和分享人数。
     */
    private Map<String, Long> getAnswerEventStats(Long requestId, ActivityAnswerDO answer, ActivityDO activity) {
        Map<String, Long> result = new HashMap<>();
        result.put("pv", 0L);
        result.put("uv", 0L);
        result.put("shareCount", 0L);
        if (eventService == null) {
            return result;
        }
        LocalDateTime startTime = activity == null || activity.getStartDate() == null
                ? null : activity.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault())
                .toLocalDate().atStartOfDay();
        LocalDateTime endTime = activity == null || activity.getEndDate() == null
                ? null : activity.getEndDate().toInstant().atZone(java.time.ZoneId.systemDefault())
                .toLocalDate().atTime(23, 59, 59);
        Long businessId = resolveEventBusinessId(activity);
        for (String eventId : buildAnswerEventIds(requestId, answer)) {
            Map<String, Long> pvUv = eventService.statPvUv(
                    EventType.ANSWER, eventId, startTime, endTime, businessId);
            if (pvUv != null) {
                result.merge("pv", Objects.requireNonNullElse(pvUv.get("pv"), 0L), Long::sum);
                result.merge("uv", Objects.requireNonNullElse(pvUv.get("uv"), 0L), Long::sum);
            }
            Long answerShareCount = eventService.statUv(
                    EventType.ANSWER_SHARE, eventId, startTime, endTime, businessId);
            Long legacyShareCount = eventService.statUv(
                    EventType.POINT_SHARE, eventId, startTime, endTime, businessId);
            result.merge("shareCount", Objects.requireNonNullElse(answerShareCount, 0L)
                    + Objects.requireNonNullElse(legacyShareCount, 0L), Long::sum);
        }
        return result;
    }

    /**
     * 按天统计有奖问答活动的 PV、UV。
     */
    private Map<String, Map<String, Long>> getAnswerDailyEventStats(Long requestId,
                                                                    LocalDateTime startTime,
                                                                    LocalDateTime endTime) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        if (eventService == null) {
            return result;
        }
        ActivityAnswerDO answer = selectAnswerByIdOrActivityId(requestId);
        ActivityDO activity = answer == null ? null : activityMapper.selectById(resolveActivityId(answer));
        Long businessId = resolveEventBusinessId(activity);
        for (String eventId : buildAnswerEventIds(requestId, answer)) {
            Map<String, Map<String, Long>> eventStats = eventService.statDailyPvUv(
                    EventType.ANSWER, eventId, startTime, endTime, businessId);
            if (eventStats == null) {
                continue;
            }
            eventStats.forEach((date, values) -> {
                Map<String, Long> dayResult = result.computeIfAbsent(date, key -> new HashMap<>());
                if (values != null) {
                    dayResult.merge("pv", Objects.requireNonNullElse(values.get("pv"), 0L), Long::sum);
                    dayResult.merge("uv", Objects.requireNonNullElse(values.get("uv"), 0L), Long::sum);
                }
            });
        }
        return result;
    }

    /**
     * 获取埋点统计使用的业务 ID，优先使用活动归属，避免后台登录上下文与 ES 数据不一致。
     */
    private Long resolveEventBusinessId(ActivityDO activity) {
        return activity != null && activity.getBusinessId() != null
                ? activity.getBusinessId() : BusinessContextHolder.getBusinessId();
    }

    /**
     * 构建埋点查询 ID，避免配置 ID 与活动 ID 相同时重复统计。
     */
    private Set<String> buildAnswerEventIds(Long requestId, ActivityAnswerDO answer) {
        Set<String> eventIds = new LinkedHashSet<>();
        if (requestId != null) {
            eventIds.add(requestId.toString());
        }
        if (answer != null && answer.getId() != null) {
            eventIds.add(answer.getId().toString());
        }
        if (answer != null && resolveActivityId(answer) != null) {
            eventIds.add(resolveActivityId(answer).toString());
        }
        return eventIds;
    }

    /**
     * 构建答题记录分页查询条件。
     */
    /**
     * 规范化查询开始时间，去掉纳秒，避免边界比较不稳定。
     */
    private LocalDateTime normalizeQueryStartTime(LocalDateTime startTime) {
        return startTime == null ? null : startTime.withNano(0);
    }

    /**
     * 规范化查询结束时间；如果是日期零点，按整天查询补到 23:59:59。
     */
    private LocalDateTime normalizeQueryEndTime(LocalDateTime endTime) {
        if (endTime == null) {
            return null;
        }
        LocalDateTime normalized = endTime.withNano(0);
        if (normalized.toLocalTime().equals(LocalTime.MIN)) {
            return normalized.withHour(23).withMinute(59).withSecond(59);
        }
        return normalized;
    }

    private LambdaQueryWrapperX<ActivityAnswerRecordDO> buildRecordWrapper(ActivityAnswerRecordPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerRecordDO> wrapper = new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .eqIfPresent(ActivityAnswerRecordDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(ActivityAnswerRecordDO::getStoreId, reqVO.getStoreId())
                .inIfPresent(ActivityAnswerRecordDO::getStatus, reqVO.getQueryStatuses())
                .geIfPresent(ActivityAnswerRecordDO::getStartTime, reqVO.getStartTime())
                .leIfPresent(ActivityAnswerRecordDO::getStartTime, reqVO.getEndTime());
        appendMemberMobileLike(wrapper, reqVO.getMemberMobile());
        return wrapper;
    }

    /**
     * 构建发放记录查询条件，分页、统计、导出共用同一个口径。
     */
    private LambdaQueryWrapperX<ActivityAnswerRewardLogDO> buildRewardLogWrapper(ActivityAnswerRewardLogPageReqVO reqVO) {
        LambdaQueryWrapperX<ActivityAnswerRewardLogDO> wrapper = new LambdaQueryWrapperX<ActivityAnswerRewardLogDO>()
                .eqIfPresent(ActivityAnswerRewardLogDO::getActivityId, reqVO.getActivityId())
                .eqIfPresent(ActivityAnswerRewardLogDO::getPrizeType, reqVO.getQueryPrizeType())
                .eqIfPresent(ActivityAnswerRewardLogDO::getPrizeState, reqVO.getPrizeState())
                .geIfPresent(ActivityAnswerRewardLogDO::getGrantTime, reqVO.getStartTime())
                .leIfPresent(ActivityAnswerRewardLogDO::getGrantTime, reqVO.getEndTime());
        appendMemberMobileLike(wrapper, reqVO.getMemberMobile());
        appendRedPacketClaimStatusCondition(wrapper, reqVO.getClaimStatus());
        if (reqVO.getQueryAddressStatus() != null || reqVO.getQueryExpressStatus() != null) {
            wrapper.eq(ActivityAnswerRewardLogDO::getPrizeType, 4);
        }
        if (Objects.equals(reqVO.getQueryAddressStatus(), 0)) {
            wrapper.and(item -> item.isNull(ActivityAnswerRewardLogDO::getReceiveAddress)
                    .or().eq(ActivityAnswerRewardLogDO::getReceiveAddress, ""));
        } else if (Objects.equals(reqVO.getQueryAddressStatus(), 1)) {
            wrapper.isNotNull(ActivityAnswerRewardLogDO::getReceiveAddress)
                    .ne(ActivityAnswerRewardLogDO::getReceiveAddress, "");
        }
        if (Objects.equals(reqVO.getQueryExpressStatus(), 0)) {
            wrapper.and(item -> item.isNull(ActivityAnswerRewardLogDO::getTrackingNumber)
                    .or().eq(ActivityAnswerRewardLogDO::getTrackingNumber, ""));
        } else if (Objects.equals(reqVO.getQueryExpressStatus(), 1)) {
            wrapper.isNotNull(ActivityAnswerRewardLogDO::getTrackingNumber)
                    .ne(ActivityAnswerRewardLogDO::getTrackingNumber, "");
        }
        return wrapper;
    }

    /**
     * 手机号字段为数字类型，后台联系方式筛选仍按文本做模糊查询。
     */
    private <T> void appendMemberMobileLike(LambdaQueryWrapperX<T> wrapper, Long memberMobile) {
        String mobileStr = Optional.ofNullable(memberMobile)
                .map(String::valueOf)
                .map(String::trim)
                .orElse("");
        if (org.springframework.util.StringUtils.hasText(mobileStr)) {
            // {0} 会自动参数绑定，防止 SQL 注入。
            wrapper.apply("CAST(member_mobile AS CHAR) LIKE CONCAT('%', {0}, '%')", mobileStr);
        }
    }
    /**
     * 红包状态筛选：未领取排除超过 24 小时数据，已失效查询超过 24 小时仍未领取的数据。
     */
    private void appendRedPacketClaimStatusCondition(LambdaQueryWrapperX<ActivityAnswerRewardLogDO> wrapper, Integer claimStatus) {
        if (claimStatus == null) {
            return;
        }
        LocalDateTime expireTime = LocalDateTime.now().minusHours(24);
        wrapper.eq(ActivityAnswerRewardLogDO::getPrizeType, 5);
        if (Objects.equals(claimStatus, 1)) {
            wrapper.eq(ActivityAnswerRewardLogDO::getClaimStatus, 1)
                    .gt(ActivityAnswerRewardLogDO::getGrantTime, expireTime);
        } else if (Objects.equals(claimStatus, 2)) {
            wrapper.eq(ActivityAnswerRewardLogDO::getClaimStatus, 2);
        } else if (Objects.equals(claimStatus, 3)) {
            wrapper.eq(ActivityAnswerRewardLogDO::getClaimStatus, 1)
                    .le(ActivityAnswerRewardLogDO::getGrantTime, expireTime);
        }
    }

    /**
     * 构建答题记录动态导出表头，题目列来自答题明细中的当次题目。
     */
    private List<List<String>> buildRecordExportHead(List<ActivityAnswerRecordDetailDO> questions) {
        List<List<String>> head = new ArrayList<>();
        List.of("会员昵称", "联系方式", "答题编号", "答题数量", "正确数量", "错误数量", "正确率", "答题状态", "参与时间", "参与门店")
                .forEach(title -> head.add(List.of(title)));
        for (ActivityAnswerRecordDetailDO question : questions) {
            head.add(List.of(StringUtils.hasText(question.getQuestionTitle()) ? question.getQuestionTitle() : "题目"));
        }
        return head;
    }

    /**
     * 构建答题记录导出行。
     */
    private List<String> buildRecordExportRow(ActivityAnswerRecordDO record, List<ActivityAnswerRecordDetailDO> questions,
                                              Map<String, ActivityAnswerRecordDetailDO> questionResultMap) {
        List<String> row = new ArrayList<>();
        row.add(nullToEmpty(record.getMemberName()));
        row.add(nullToEmpty(record.getMemberMobile()));
        row.add(nullToEmpty(record.getAnswerNo()));
        row.add(String.valueOf(Objects.requireNonNullElse(record.getQuestionCount(), 0)));
        row.add(String.valueOf(Objects.requireNonNullElse(record.getCorrectCount(), 0)));
        row.add(String.valueOf(Objects.requireNonNullElse(record.getWrongCount(), 0)));
        row.add(formatRate(record.getAccuracy()));
        row.add(recordStatusText(record.getStatus()));
        row.add(formatTime(record.getStartTime()));
        row.add(nullToEmpty(record.getStoreName()));
        Map<String, ActivityAnswerRecordDetailDO> resultMap = questionResultMap == null ? Collections.emptyMap() : questionResultMap;
        for (ActivityAnswerRecordDetailDO question : questions) {
            row.add(answerResultText(resultMap.get(buildQuestionColumnKey(question))));
        }
        return row;
    }

    /**
     * 查询导出范围内的答题明细。
     */
    private List<ActivityAnswerRecordDetailDO> selectRecordExportDetails(List<ActivityAnswerRecordDO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<Long> recordIds = records.stream().map(ActivityAnswerRecordDO::getId).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(recordIds)) {
            return Collections.emptyList();
        }
        return activityAnswerExportQueryService.selectRecordDetails(recordIds);
    }

    /**
     * 动态题目列按题目ID和题目名称去重，同ID但题目变化会拆成新列。
     */
    private List<ActivityAnswerRecordDetailDO> buildExportQuestionColumns(List<ActivityAnswerRecordDetailDO> details) {
        Map<String, ActivityAnswerRecordDetailDO> questionMap = new LinkedHashMap<>();
        for (ActivityAnswerRecordDetailDO detail : details) {
            questionMap.putIfAbsent(buildQuestionColumnKey(detail), detail);
        }
        return new ArrayList<>(questionMap.values());
    }

    /**
     * 查询答题明细并按记录ID、题目列key组织。
     */
    private Map<Long, Map<String, ActivityAnswerRecordDetailDO>> buildRecordQuestionResultMap(List<ActivityAnswerRecordDetailDO> details) {
        if (CollectionUtils.isEmpty(details)) {
            return Collections.emptyMap();
        }
        return details.stream().collect(Collectors.groupingBy(ActivityAnswerRecordDetailDO::getRecordId,
                Collectors.toMap(this::buildQuestionColumnKey, Function.identity(), (oldValue, newValue) -> newValue)));
    }

    /**
     * 题目动态列key：同ID同标题合并，同ID不同标题拆分。
     */
    private String buildQuestionColumnKey(ActivityAnswerRecordDetailDO detail) {
        if (detail == null) {
            return "";
        }
        return Objects.toString(detail.getQuestionId(), "") + "#" + Objects.toString(detail.getQuestionTitle(), "");
    }

    /**
     * 发放记录回填会员昵称。
     */
    /**
     * 回填红包实际展示状态，超过 24 小时未领取的红包按已失效展示。
     */
    private void fillRewardLogEffectiveStatus(List<ActivityAnswerRewardLogRespVO> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        for (ActivityAnswerRewardLogRespVO log : logs) {
            log.setClaimStatus(effectiveClaimStatus(log.getPrizeType(), log.getClaimStatus(), log.getGrantTime()));
        }
    }

    /**
     * 计算红包实际状态，非红包保持原状态。
     */
    private Integer effectiveClaimStatus(Integer prizeType, Integer claimStatus, LocalDateTime grantTime) {
        if (!Objects.equals(prizeType, 5) || !Objects.equals(claimStatus, 1) || grantTime == null) {
            return claimStatus;
        }
        return !grantTime.plusHours(24).isAfter(LocalDateTime.now()) ? 3 : claimStatus;
    }

    private String recordStatusText(Integer status) {
        if (Objects.equals(status, 1)) {
            return "已完成";
        }
        return "未完成";
    }

    private String answerResultText(Integer result) {
        if (Objects.equals(result, 1)) {
            return "正确";
        }
        if (Objects.equals(result, 0)) {
            return "错误";
        }
        return "未答";
    }

    private String answerResultText(ActivityAnswerRecordDetailDO detail) {
        if (detail == null || !Objects.equals(detail.getIsAnswered(), 1)) {
            return "未作答";
        }
        if (Objects.equals(detail.getAnswerResult(), 1)) {
            return "正确";
        }
        if (Objects.equals(detail.getAnswerResult(), 0)) {
            return "错误";
        }
        return "未作答";
    }

    private String prizeTypeText(Integer prizeType) {
        if (Objects.equals(prizeType, 1)) {
            return "积分";
        }
        if (Objects.equals(prizeType, 2)) {
            return "优惠券";
        }
        if (Objects.equals(prizeType, 3)) {
            return "优惠券包";
        }
        if (Objects.equals(prizeType, 4)) {
            return "实物奖品";
        }
        if (Objects.equals(prizeType, 5)) {
            return "现金红包";
        }
        return "";
    }

    private String claimStatusText(Integer claimStatus) {
        if (Objects.equals(claimStatus, 1)) {
            return "未领取";
        }
        if (Objects.equals(claimStatus, 2)) {
            return "已领取";
        }
        if (Objects.equals(claimStatus, 3)) {
            return "已失效";
        }
        return "";
    }

    private String formatRate(BigDecimal value) {
        if (value == null) {
            return "0%";
        }
        return value.stripTrailingZeros().toPlainString() + "%";
    }

    private String formatTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
