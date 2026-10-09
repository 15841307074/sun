package com.htyoudao.youdao.module.promotion.service.activityAnswerApp;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo.ActivityAnswerSettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerQuestionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRecordDetailDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerTaskDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerQuestionMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRecordDetailMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRewardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRewardMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerTaskMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

/**
 * 有奖问答小程序服务实现。
 */
@Service
@Slf4j
@RefreshScope
@DS(DsNameConstants.SHARDING)
public class ActivityAnswerAppServiceImpl implements ActivityAnswerAppService {

    private static final int ACTION_DISABLED = 0;
    private static final int ACTION_START = 1;
    private static final int ACTION_CONTINUE = 2;

    private static final int STATUS_NOT_STARTED = 0;
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_ENDED = 2;
    private static final int STATUS_NOT_IN_SESSION = 3;

    private static final int TASK_ORDER = 3;
    private static final int TASK_SHARE = 4;
    private static final int TASK_BROWSE = 5;
    private static final int TASK_SIGN = 6;
    private static final int TASK_REWARD_COUNT = 1;

    @Resource
    private ActivityAnswerMapper activityAnswerMapper;

    @Resource
    private ActivityAnswerQuestionMapper questionMapper;

    @Resource
    private ActivityAnswerRecordMapper recordMapper;

    @Resource
    private ActivityAnswerRecordDetailMapper recordDetailMapper;

    @Resource
    private ActivityAnswerTaskMapper taskMapper;

    @Resource
    private ActivityAnswerRewardMapper rewardMapper;

    @Resource
    private ActivityAnswerRewardLogMapper rewardLogMapper;

    @Resource
    private ActivityAnswerCommodityMapper commodityMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivityAppService activityAppService;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private RedisCache redisCache;

    @Resource
    private LotteryAddLogService lotteryAddLogService;

    @Resource
    private LotteryRedPacketService lotteryRedPacketService;

    @Resource
    private CouponPackageService couponPackageService;

    @Resource
    private ActivityAnswerRewardStockService rewardStockService;

    @Resource
    private IdentifierGenerator identifierGenerator;

    @DubboReference
    private WxMemberApi wxMemberApi;

    @DubboReference
    private StoreApi storeApi;

    /**
     * 查询有奖问答活动详情。
     */
    @Override
    public ActivityAnswerAppDetailRespVO getDetail(Long activityId, Long storeId, Long memberId) {
        Long memberMobile = getMemberMobile(memberId);
        String detailCacheKey = buildAppDetailCacheKey(activityId, storeId, memberMobile);
        ActivityAnswerAppDetailRespVO cachedDetail = detailCacheKey == null
                ? null : redisCache.getCacheObjectOrDelete(detailCacheKey);
        if (cachedDetail != null) {
            return cachedDetail;
        }
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        ActivityAnswerDO answer = cacheData != null && cacheData.getAnswer() != null
                ? cacheData.getAnswer() : loadAnswerSetting(activityId);
        ActivityDO activity = cacheData == null ? null : cacheData.getActivity();
        if (activity == null) {
            activity = activityMapper.selectById(activityId);
        }
        mergeActivitySnapshot(answer, activity);
        ActivityAnswerAppDetailRespVO respVO = buildBaseDetail(answer);
        List<ActivityAnswerQuestionDO> questions = cacheData != null && !CollectionUtils.isEmpty(cacheData.getQuestions())
                ? cacheData.getQuestions() : loadQuestions(activityId, activity);
        respVO.setQuestions(buildQuestionRespList(questions));
        respVO.setQuestionCount(respVO.getQuestions() == null ? 0 : respVO.getQuestions().size());
        respVO.setTaskConfig(buildTaskConfig(answer));

        SessionInfo sessionInfo = buildCurrentSessionInfo(activity, answer.getCalculationRules());
        respVO.setActivityStatus(sessionInfo.activityStatus);
        respVO.setStatusText(sessionInfo.statusText);
        respVO.setCurrentPeriodKey(sessionInfo.periodKey);

        Map<Integer, ActivityAnswerTaskDO> taskMap = loadTaskRecordMap(answer.getActivityId(), memberMobile,
                sessionInfo.chancePeriodKey);
        respVO.setTaskList(buildTaskList(answer, taskMap));
        if (memberMobile == null) {
            respVO.setChanceCount(0);
            respVO.setAnswerDetails(Collections.emptyList());
            disable(respVO, ANSWER_MEMBER_MOBILE_REQUIRED.getMsg());
            cacheAppDetail(detailCacheKey, respVO);
            return respVO;
        }

        int chanceCount = calculateChanceCount(answer, memberMobile, sessionInfo, true, null,
                countTaskGainChance(taskMap));
        respVO.setChanceCount(chanceCount);
        ActivityAnswerRecordDO currentRecord = selectCurrentPeriodRecordCached(
                answer.getActivityId(), memberMobile, sessionInfo.periodKey);
        fillAnswerAction(respVO, activity, cacheData == null ? null : cacheData.getStoreIds(), answer,
                storeId, sessionInfo, chanceCount, currentRecord);
        fillCurrentAnswerRecord(respVO, currentRecord);
        cacheAppDetail(detailCacheKey, respVO);
        return respVO;
    }

    /**
     * 查询当前可用答题次数。
     */
    @Override
    public ActivityAnswerChanceCountRespVO getChanceCount(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerDO answer = loadAnswerSetting(reqVO.getActivityId());
        ActivityDO activity = loadActivity(reqVO.getActivityId());
        validateCommunityIfNecessary(activity);
        mergeActivitySnapshot(answer, activity);
        SessionInfo sessionInfo = buildCurrentSessionInfo(activity, answer.getCalculationRules());
        Long memberMobile = getMemberMobile(reqVO.getMemberId());

        ActivityAnswerChanceCountRespVO respVO = new ActivityAnswerChanceCountRespVO();
        respVO.setPeriodKey(sessionInfo.chancePeriodKey);
        respVO.setTotalLimit(defaultZero(answer.getAnswerTotalNumber()));
        int usedCount = countUsedRecords(answer, memberMobile, sessionInfo);
        respVO.setUsedCount(usedCount);
        respVO.setChanceCount(calculateChanceCount(answer, memberMobile, sessionInfo, true, usedCount));
        return respVO;
    }

    /**
     * 开始或继续答题。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityAnswerJoinRespVO join(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerContext context = validateAnswerContext(reqVO.getActivityId(), reqVO.getStoreId(), reqVO.getMemberId());
        String lockValue = UUID.randomUUID().toString();
        String lockKey = RedisKeyConstants.ANSWER_TASK_LOCK + context.answer.getActivityId() + ":" + context.memberMobile
                + ":" + context.sessionInfo.chancePeriodKey;
        if (!redisCache.lock(lockKey, lockValue, 15)) {
            throw exception(ANSWER_SYSTEM_BUSY);
        }
        boolean unlockAfterTransaction = registerUnlockAfterTransaction(lockKey, lockValue);
        try {
            ActivityAnswerRecordDO currentRecord = selectCurrentPeriodRecordCached(context.answer.getActivityId(),
                    context.memberMobile, context.sessionInfo.periodKey);
            if (currentRecord != null && Objects.equals(currentRecord.getStatus(), 0)) {
                List<ActivityAnswerRecordDetailDO> details = ensureRecordQuestionDetails(context, currentRecord);
                return buildJoinResp(context, currentRecord, 2, details,
                        calculateChanceCount(context.answer, context.memberMobile, context.sessionInfo, true));
            }
            int chanceBefore = calculateChanceCount(context.answer, context.memberMobile, context.sessionInfo, true);
            if (chanceBefore <= 0) {
                throw exception(ANSWER_SYSTEM_BUSY.getCode(), "答题次数已用完");
            }
            ActivityAnswerRecordDO record = buildAnswerRecord(context);
            recordMapper.insert(record);
            List<ActivityAnswerRecordDetailDO> details = initRecordQuestionDetails(context, record);
            clearCurrentRecordCache(context.answer.getActivityId(), context.memberMobile,
                    context.sessionInfo.periodKey);
            clearAppDetailCache(context.answer.getActivityId(), context.storeId, context.memberMobile);
            consumeTaskChance(context.answer, context.memberMobile, context.sessionInfo.chancePeriodKey);
            int chanceAfter = decreaseChanceAfterJoin(context.answer, context.memberMobile,
                    context.sessionInfo, chanceBefore);
            registerJoinCacheAfterCommit(context, record, details);
            return buildJoinResp(context, record, 1, details, chanceAfter);
        } finally {
            if (!unlockAfterTransaction) {
                redisCache.unlock(lockKey, lockValue);
            }
        }
    }

    /**
     * 提交答题结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityAnswerSubmitRespVO submit(ActivityAnswerSubmitReqVO reqVO) {
        ActivityAnswerContext context = validateSubmitContext(
                reqVO.getActivityId(), reqVO.getStoreId(), reqVO.getMemberId());
        String lockValue = UUID.randomUUID().toString();
        String lockKey = RedisKeyConstants.ANSWER_SUBMIT_LOCK + reqVO.getRecordId();
        if (!redisCache.lock(lockKey, lockValue, 60)) {
            throw exception(ANSWER_SYSTEM_BUSY);
        }
        try {
            ActivityAnswerRecordDO record = selectAnswerRecord(reqVO.getRecordId(), reqVO.getActivityId(), context.memberMobile);
            if (record != null && Objects.equals(record.getStatus(), 1)) {
                return buildCompletedSubmitResp(context, record, reqVO.getQuestionId());
            }
            validateSubmitRecord(record, context);
            ActivityAnswerRecordDetailDO detail = selectRecordDetail(reqVO.getRecordId(), reqVO.getQuestionId(), context.memberMobile);
            if (detail == null) {
                loadContextQuestions(context);
                ensureRecordQuestionDetails(context, record);
                detail = selectRecordDetail(reqVO.getRecordId(), reqVO.getQuestionId(), context.memberMobile);
            }
            if (detail == null) {
                throw exception(ANSWER_QUESTION_NOT_EMPTY.getCode(), "题目不存在");
            }
            if (Objects.equals(detail.getIsAnswered(), 1)) {
                return buildSubmitResp(context, record, detail, true);
            }
            detail.setMemberId(context.memberId);
            detail.setSelectedAnswer(reqVO.getSelectedAnswer());
            detail.setAnswerResult(Objects.equals(detail.getCorrectAnswer(), reqVO.getSelectedAnswer()) ? 1 : 0);
            detail.setIsAnswered(1);
            if (!updateRecordDetailAnswer(detail)) {
                detail = selectRecordDetail(reqVO.getRecordId(), reqVO.getQuestionId(), context.memberMobile);
            }
            redisCache.deleteObject(buildRecordDetailCacheKey(reqVO.getRecordId(), context.memberMobile));
            clearCurrentRecordCache(context.answer.getActivityId(), context.memberMobile,
                    context.sessionInfo.periodKey);
            clearAppDetailCache(context.answer.getActivityId(), context.storeId, context.memberMobile);
            return buildSubmitResp(context, record, detail, true);
        } finally {
            redisCache.unlock(lockKey, lockValue);
        }
    }

    /**
     * 取消本次答题，保留已作答明细并按未完成记录统计。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(ActivityAnswerCancelReqVO reqVO) {
        ActivityAnswerContext context = validateAnswerContext(
                reqVO.getActivityId(), reqVO.getStoreId(), reqVO.getMemberId());
        String lockValue = UUID.randomUUID().toString();
        String lockKey = RedisKeyConstants.ANSWER_SUBMIT_LOCK + reqVO.getRecordId();
        if (!redisCache.lock(lockKey, lockValue, 30)) {
            throw exception(ANSWER_SYSTEM_BUSY);
        }
        try {
            ActivityAnswerRecordDO record = selectAnswerRecord(
                    reqVO.getRecordId(), reqVO.getActivityId(), context.memberMobile);
            if (record == null) {
                throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "答题记录不存在");
            }
            if (Objects.equals(record.getCancelStatus(), 1)) {
                return true;
            }
            validateSubmitRecord(record, context);
            List<ActivityAnswerRecordDetailDO> details = selectRecordQuestionDetails(
                    record.getId(), context.memberMobile);
            int correctCount = (int) details.stream()
                    .filter(item -> Objects.equals(item.getIsAnswered(), 1))
                    .filter(item -> Objects.equals(item.getAnswerResult(), 1)).count();
            int wrongCount = (int) details.stream()
                    .filter(item -> Objects.equals(item.getIsAnswered(), 1))
                    .filter(item -> Objects.equals(item.getAnswerResult(), 0)).count();
            int answeredCount = correctCount + wrongCount;
            BigDecimal accuracy = answeredCount == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(correctCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(answeredCount), 2, RoundingMode.HALF_UP);
            int updated = recordMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerRecordDO>()
                    .eq(ActivityAnswerRecordDO::getId, record.getId())
                    .eq(ActivityAnswerRecordDO::getMemberMobile, context.memberMobile)
                    .eq(ActivityAnswerRecordDO::getStatus, 0)
                    .and(item -> item.isNull(ActivityAnswerRecordDO::getCancelStatus)
                            .or().eq(ActivityAnswerRecordDO::getCancelStatus, 0))
                    .set(ActivityAnswerRecordDO::getMemberId, context.memberId)
                    .set(ActivityAnswerRecordDO::getCorrectCount, correctCount)
                    .set(ActivityAnswerRecordDO::getWrongCount, wrongCount)
                    .set(ActivityAnswerRecordDO::getAccuracy, accuracy)
                    .set(ActivityAnswerRecordDO::getCancelStatus, 1));
            if (updated == 0) {
                throw exception(ANSWER_SYSTEM_BUSY);
            }
            clearCurrentRecordCache(context.answer.getActivityId(), context.memberMobile,
                    context.sessionInfo.periodKey);
            clearAppDetailCache(context.answer.getActivityId(), context.storeId, context.memberMobile);
            return true;
        } finally {
            redisCache.unlock(lockKey, lockValue);
        }
    }

    /**
     * 查询当前手机号的答题记录。
     */
    private ActivityAnswerRecordDO selectAnswerRecord(Long recordId, Long activityId, Long memberMobile) {
        return recordMapper.selectOne(new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getId, recordId)
                .eq(ActivityAnswerRecordDO::getActivityId, activityId)
                .eq(ActivityAnswerRecordDO::getMemberMobile, memberMobile)
                .last("LIMIT 1"));
    }

    /**
     * 重复提交已完成记录时返回原答题和奖励结果。
     */
    private ActivityAnswerSubmitRespVO buildCompletedSubmitResp(ActivityAnswerContext context,
                                                                ActivityAnswerRecordDO record, Long questionId) {
        ActivityAnswerRecordDetailDO detail = selectRecordDetail(record.getId(), questionId, context.memberMobile);
        if (detail == null) {
            throw exception(ANSWER_QUESTION_NOT_EMPTY.getCode(), "题目不存在");
        }
        ActivityAnswerSubmitRespVO respVO = buildSubmitResp(context, record, detail, false);
        respVO.setFinished(true);
        respVO.setCorrectCount(defaultZero(record.getCorrectCount()));
        respVO.setWrongCount(defaultZero(record.getWrongCount()));
        respVO.setAccuracy(record.getAccuracy());
        ActivityAnswerRewardLogDO rewardLog = selectRewardLogByRecord(record.getId(), context.memberMobile);
        if (rewardLog == null) {
            respVO.setHasReward(false);
            respVO.setMessage("很遗憾，奖品已发完");
        } else {
            fillRewardResp(respVO, rewardLog);
        }
        return respVO;
    }

    /**
     * 查询任务列表。
     */
    @Override
    public List<ActivityAnswerTaskRespVO> getTaskList(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerDO answer = loadAnswerSetting(reqVO.getActivityId());
        ActivityDO activity = loadActivity(reqVO.getActivityId());
        validateCommunityIfNecessary(activity);
        mergeActivitySnapshot(answer, activity);
        SessionInfo sessionInfo = buildCurrentSessionInfo(activity, answer.getCalculationRules());
        return buildTaskList(answer, getMemberMobile(reqVO.getMemberId()), sessionInfo.chancePeriodKey);
    }

    /**
     * 完成签到任务。
     */
    @Override
    public ActivityAnswerTaskCompleteRespVO signTask(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(reqVO.getActivityId());
        ActivityAnswerDO answer = cacheData != null && cacheData.getAnswer() != null
                ? cacheData.getAnswer() : loadAnswerSetting(reqVO.getActivityId());
        ActivityDO activity = cacheData == null ? null : cacheData.getActivity();
        return completeTask(answer, activity, reqVO.getStoreId(), reqVO.getMemberId(), TASK_SIGN,
                "签到", Objects.equals(answer.getSignStatus(), 1), defaultZero(answer.getSignCount()));
    }

    /**
     * 检测分享任务是否可完成。
     */
    @Override
    public Boolean shareCheck(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerDO answer = loadAnswerSetting(reqVO.getActivityId());
        TaskValidateResult validateResult = validateTask(answer, reqVO.getStoreId(), reqVO.getMemberId(),
                Objects.equals(answer.getShareEvent(), 1), defaultZero(answer.getShareCount()));
        ActivityAnswerTaskDO taskDO = getTaskRecord(answer.getActivityId(), validateResult.memberMobile,
                TASK_SHARE, validateResult.sessionInfo.chancePeriodKey);
        if (defaultZero(taskDO == null ? null : taskDO.getFinishCount()) >= defaultZero(answer.getShareCount())) {
            throw exception(ANSWER_TASK_LIMIT_REACHED);
        }
        return true;
    }

    /**
     * 完成分享任务。
     */
    @Override
    public ActivityAnswerTaskCompleteRespVO shareTask(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerDO answer = loadAnswerSetting(reqVO.getActivityId());
        return completeTask(answer, reqVO.getStoreId(), reqVO.getMemberId(), TASK_SHARE,
                "分享", Objects.equals(answer.getShareEvent(), 1), defaultZero(answer.getShareCount()));
    }

    /**
     * 完成浏览首页任务。
     */
    @Override
    public ActivityAnswerTaskCompleteRespVO browseHomeTask(ActivityAnswerBaseReqVO reqVO) {
        ActivityAnswerDO answer = loadAnswerSetting(reqVO.getActivityId());
        return completeTask(answer, reqVO.getStoreId(), reqVO.getMemberId(), TASK_BROWSE,
                "浏览首页", Objects.equals(answer.getBrowseType(), 1), defaultZero(answer.getBrowseCount()));
    }

    /**
     * 处理下单获得答题次数任务。
     */
    @Override
    public void handleOrderTask(ActivityJkOrderReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.getStoreId() == null || reqDTO.getMemberId() == null) {
            return;
        }
        List<ActivityAnswerDO> answerList = activityAnswerMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerDO>()
                .eq(ActivityAnswerDO::getOrderStatus, 1));
        if (CollectionUtils.isEmpty(answerList)) {
            return;
        }
        Map<Long, ActivityAnswerDO> answerMap = answerList.stream()
                .filter(item -> item.getActivityId() != null)
                .collect(Collectors.toMap(ActivityAnswerDO::getActivityId, Function.identity(), (left, right) -> left));
        List<Long> activityIds = new ArrayList<>(answerMap.keySet());
        if (CollectionUtils.isEmpty(activityIds)) {
            return;
        }
        List<ActivityDO> activityList = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                .in(ActivityDO::getId, activityIds)
                .eq(ActivityDO::getActivityType, ActivityTypeEnum.ANSWER.getCode())
                .eq(ActivityDO::getIsEnabled, 1));
        if (CollectionUtils.isEmpty(activityList)) {
            return;
        }
        WxMemberVO wxMember = getWxMember(reqDTO.getMemberId());
        Long memberMobile = parseMemberMobile(wxMember == null ? null : wxMember.getMemberMobile());
        if (memberMobile == null) {
            return;
        }

        Map<Long, List<Long>> activityStoreMap = buildActivityStoreMap(activityList);
        Set<Long> commodityActivityIds = answerList.stream()
                .filter(item -> Objects.equals(item.getPlaceOrderType(), 2) && Objects.equals(item.getPlaceOrderProduct(), 2))
                .map(ActivityAnswerDO::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, List<Long>> commodityMap = buildAnswerCommodityMap(commodityActivityIds);

        for (ActivityDO activityDO : activityList) {
            ActivityAnswerDO answer = answerMap.get(activityDO.getId());
            if (answer == null) {
                continue;
            }
            mergeActivitySnapshot(answer, activityDO);
            if (!TimeValidationUtil.isTimeValid(activityDO.getStartDate(), activityDO.getEndDate(),
                    activityDO.getDayNumbers(), activityDO.getWeekNumbers(), activityDO.getTimeRange())) {
                continue;
            }
            if (!isStoreMatched(activityDO, activityStoreMap.get(activityDO.getId()), reqDTO.getStoreId())) {
                continue;
            }
            if (!isOrderMatched(answer, commodityMap.getOrDefault(activityDO.getId(), Collections.emptyList()), reqDTO)) {
                continue;
            }
            int orderTaskLimit = Objects.equals(answer.getPlaceOrderAnswer(), 1)
                    ? Integer.MAX_VALUE : defaultZero(answer.getPlaceOrderAnswerNumber());
            try {
                completeTask(answer, activityDO, reqDTO.getStoreId(), reqDTO.getMemberId(), memberMobile,
                        TASK_ORDER, "下单", true, orderTaskLimit, false);
            } catch (Exception e) {
                // 单个活动不满足条件或已达到上限时，不影响同一订单匹配其他活动。
                log.warn("处理有奖问答下单任务失败，activityId={}，storeId={}，memberId={}",
                        activityDO.getId(), reqDTO.getStoreId(), reqDTO.getMemberId(), e);
            }
        }
    }

    /**
     * 查询我的答题记录。
     */
    @Override
    public PageResult<ActivityAnswerMyRecordRespVO> getMyRecord(ActivityAnswerMyRecordPageReqVO reqVO) {
        Long memberMobile = getRequiredMemberMobile(reqVO.getMemberId());
        PageResult<ActivityAnswerRecordDO> pageResult = recordMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityAnswerRecordDO::getMemberMobile, memberMobile)
                .eqIfPresent(ActivityAnswerRecordDO::getStoreId, reqVO.getStoreId())
                .eqIfPresent(ActivityAnswerRecordDO::getStatus, reqVO.getStatus())
                .orderByDesc(ActivityAnswerRecordDO::getCreateTime)
                .orderByDesc(ActivityAnswerRecordDO::getId));
        List<ActivityAnswerMyRecordRespVO> list = pageResult.getList().stream()
                .map(this::buildMyRecordResp)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    /**
     * 查询我的奖励记录。
     */
    @Override
    public PageResult<ActivityAnswerMyRewardRespVO> getMyReward(ActivityAnswerMyRewardPageReqVO reqVO) {
        Long memberMobile = getRequiredMemberMobile(reqVO.getMemberId());
        PageResult<ActivityAnswerRewardLogDO> pageResult = rewardLogMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityAnswerRewardLogDO::getMemberMobile, memberMobile)
                .eqIfPresent(ActivityAnswerRewardLogDO::getStoreId, reqVO.getStoreId())
                .eqIfPresent(ActivityAnswerRewardLogDO::getPrizeType, reqVO.getPrizeType())
                .eqIfPresent(ActivityAnswerRewardLogDO::getClaimStatus, reqVO.getClaimStatus())
                .orderByDesc(ActivityAnswerRewardLogDO::getGrantTime)
                .orderByDesc(ActivityAnswerRewardLogDO::getId));
        List<ActivityAnswerMyRewardRespVO> list = pageResult.getList().stream()
                .map(this::buildMyRewardResp)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    /**
     * 保存实物奖品收货地址。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRewardAddress(ActivityAnswerRewardAddressSaveReqVO reqVO) {
        Long memberMobile = getRequiredMemberMobile(reqVO.getMemberId());
        ActivityAnswerRewardLogDO rewardLog = rewardLogMapper.selectOne(new LambdaQueryWrapperX<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getId, reqVO.getRewardLogId())
                .eq(ActivityAnswerRewardLogDO::getMemberMobile, memberMobile)
                .last("LIMIT 1"));
        if (rewardLog == null || !Objects.equals(rewardLog.getActivityId(), reqVO.getActivityId())
                || !Objects.equals(rewardLog.getMemberMobile(), memberMobile)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "奖励记录不存在");
        }
        if (!Objects.equals(rewardLog.getPrizeType(), 4)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "非实物奖品无需填写地址");
        }
        if (Objects.equals(rewardLog.getPrizeState(), 9)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "奖品已退回，不能填写地址");
        }
        int updated = rewardLogMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getId, rewardLog.getId())
                .eq(ActivityAnswerRewardLogDO::getMemberMobile, memberMobile)
                .set(ActivityAnswerRewardLogDO::getReceiveUser, reqVO.getReceiveUser())
                .set(ActivityAnswerRewardLogDO::getReceiveMobile, reqVO.getReceiveMobile())
                .set(ActivityAnswerRewardLogDO::getReceiveAddress, reqVO.getReceiveAddress())
                .set(ActivityAnswerRewardLogDO::getPrizeState, 2));
        if (updated == 0) {
            throw exception(ANSWER_SYSTEM_BUSY);
        }
        return true;
    }

    /**
     * 校验并构建答题上下文。
     */
    private ActivityAnswerContext validateAnswerContext(Long activityId, Long storeId, Long memberId) {
        // 10.社群限制
        WxMemberVO member = getCurrentLoginMember(memberId);
        Long memberMobile = parseMemberMobile(member == null ? null : member.getMemberMobile());
        if (memberMobile == null) {
            throw exception(ANSWER_MEMBER_MOBILE_REQUIRED);
        }
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        ActivityAnswerDO answer = cacheData != null && cacheData.getAnswer() != null
                ? cacheData.getAnswer() : loadAnswerSetting(activityId);
        ActivityDO activity = cacheData == null ? null : cacheData.getActivity();
        if (activity == null) {
            activity = loadActivity(activityId);
        }
        mergeActivitySnapshot(answer, activity);
        validateCommunityIfNecessary(activity);
        SessionInfo sessionInfo = buildCurrentSessionInfo(activity, answer.getCalculationRules());
        if (!Objects.equals(answer.getState(), 1) || !Objects.equals(sessionInfo.activityStatus, STATUS_RUNNING)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), sessionInfo.statusText);
        }
        if (!isStoreMatched(activity, cacheData == null ? null : cacheData.getStoreIds(), storeId)) {
            throw exception(ANSWER_STORE_NOT_MATCH);
        }
        List<ActivityAnswerQuestionDO> questions = loadQuestions(activityId, activity);
        if (CollectionUtils.isEmpty(questions)) {
            throw exception(ANSWER_QUESTION_NOT_EMPTY);
        }
        ActivityAnswerContext context = new ActivityAnswerContext();
        context.answer = answer;
        context.activity = activity;
        context.memberId = memberId;
        context.member = member;
        context.memberMobile = memberMobile;
        context.storeId = storeId;
        context.sessionInfo = sessionInfo;
        context.questions = questions;
        context.questionMap = questions.stream().collect(Collectors.toMap(ActivityAnswerQuestionDO::getId, Function.identity(), (a, b) -> a));
        return context;
    }

    /**
     * 构建单题提交上下文，避免每题重复查询题库和校验社群关系。
     */
    private ActivityAnswerContext validateSubmitContext(Long activityId, Long storeId, Long memberId) {
        WxMemberVO member = getWxMember(memberId);
        Long memberMobile = parseMemberMobile(member == null ? null : member.getMemberMobile());
        if (memberMobile == null) {
            throw exception(ANSWER_MEMBER_MOBILE_REQUIRED);
        }
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        ActivityAnswerDO answer = cacheData != null && cacheData.getAnswer() != null
                ? cacheData.getAnswer() : loadAnswerSetting(activityId);
        ActivityDO activity = cacheData == null ? null : cacheData.getActivity();
        if (activity == null) {
            activity = loadActivity(activityId);
        }
        mergeActivitySnapshot(answer, activity);
        SessionInfo sessionInfo = buildCurrentSessionInfo(activity, answer.getCalculationRules());
        if (!Objects.equals(answer.getState(), 1) || !Objects.equals(sessionInfo.activityStatus, STATUS_RUNNING)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), sessionInfo.statusText);
        }
        if (!isStoreMatched(activity, storeId)) {
            throw exception(ANSWER_STORE_NOT_MATCH);
        }
        ActivityAnswerContext context = new ActivityAnswerContext();
        context.answer = answer;
        context.activity = activity;
        context.memberId = memberId;
        context.member = member;
        context.memberMobile = memberMobile;
        context.storeId = storeId;
        context.sessionInfo = sessionInfo;
        return context;
    }

    /**
     * 历史答题明细缺失时按需加载题库进行补齐。
     */
    private void loadContextQuestions(ActivityAnswerContext context) {
        if (!CollectionUtils.isEmpty(context.questions)) {
            return;
        }
        context.questions = loadQuestions(context.answer.getActivityId());
    }

    /**
     * 获取必填手机号。
     */
    private Long getRequiredMemberMobile(Long memberId) {
        Long memberMobile = getMemberMobile(memberId);
        if (memberMobile == null) {
            throw exception(ANSWER_MEMBER_MOBILE_REQUIRED);
        }
        return memberMobile;
    }

    /**
     * 创建答题主记录。
     */
    private ActivityAnswerRecordDO buildAnswerRecord(ActivityAnswerContext context) {
        ActivityAnswerRecordDO record = new ActivityAnswerRecordDO();
        String mobile = String.valueOf(context.memberMobile);
        record.setAnswerNo(System.currentTimeMillis() + mobile.substring(Math.max(0, mobile.length() - 4)));
        record.setActivityId(context.answer.getActivityId());
        record.setMemberId(context.memberId);
        record.setMemberMobile(context.memberMobile);
        record.setPeriodKey(context.sessionInfo.periodKey);
        record.setMemberName(loadMemberName(context.memberId));
        record.setStoreId(context.storeId);
        record.setStoreName(resolveStoreName(context.storeId));
        record.setQuestionCount(context.questions.size());
        record.setCorrectCount(0);
        record.setWrongCount(0);
        record.setAccuracy(BigDecimal.ZERO);
        record.setStatus(0);
        record.setCancelStatus(0);
        record.setStartTime(LocalDateTime.now());
        return record;
    }

    /**
     * 查询参与门店名称并保存到答题记录。
     */
    private String resolveStoreName(Long storeId) {
        if (storeId == null) {
            throw exception(ANSWER_STORE_NOT_MATCH);
        }
        String cacheKey = RedisKeyConstants.ANSWER_STORE_NAME + storeId;
        String cachedStoreName = redisCache.getCacheObject(cacheKey);
        if (StringUtils.hasText(cachedStoreName)) {
            return cachedStoreName;
        }
        CommonResult<StoreDTO> result;
        try {
            result = storeApi.getStoreByStoreId(storeId);
        } catch (Exception e) {
            log.warn("查询有奖问答参与门店名称失败，storeId={}", storeId, e);
            throw exception(ANSWER_SYSTEM_BUSY);
        }
        if (result == null || result.getData() == null || !StringUtils.hasText(result.getData().getStoreName())) {
            throw exception(ANSWER_STORE_NOT_MATCH);
        }
        String storeName = result.getData().getStoreName();
        redisCache.setCacheObjectQuietly(cacheKey, storeName, 30, TimeUnit.MINUTES);
        return storeName;
    }

    /**
     * 读取最新任务配置，避免活动修改后继续使用旧缓存中的任务上限。
     */
    private ActivityAnswerDO loadLatestTaskSetting(Long activityId) {
        ActivityAnswerDO answer = activityAnswerMapper.selectOne(ActivityAnswerDO::getActivityId, activityId);
        if (answer == null) {
            answer = activityAnswerMapper.selectById(activityId);
        }
        if (answer == null) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND);
        }
        ActivityDO activity = activityMapper.selectById(activityId);
        mergeActivitySnapshot(answer, activity);
        return answer;
    }

    /**
     * 构建开始或继续答题返回。
     */
    private ActivityAnswerJoinRespVO buildJoinResp(ActivityAnswerContext context, ActivityAnswerRecordDO record,
                                                   Integer joinType,
                                                   List<ActivityAnswerRecordDetailDO> recordQuestions,
                                                   int chanceCount) {
        List<ActivityAnswerDetailRecordRespVO> details = buildCurrentAnswerDetails(recordQuestions);
        ActivityAnswerJoinRespVO respVO = new ActivityAnswerJoinRespVO();
        respVO.setRecordId(record.getId());
        respVO.setAnswerNo(record.getAnswerNo());
        respVO.setJoinType(joinType);
        respVO.setChanceCount(chanceCount);
        respVO.setQuestionCount(defaultZero(record.getQuestionCount()));
        respVO.setQuestions(buildQuestionRespListFromDetails(recordQuestions));
        respVO.setAnsweredQuestionIds(details.stream()
                .filter(item -> Objects.equals(item.getIsAnswered(), 1))
                .map(ActivityAnswerDetailRecordRespVO::getQuestionId)
                .toList());
        respVO.setAnswers(details.stream().filter(item -> Objects.equals(item.getIsAnswered(), 1)).map(item -> {
            ActivityAnswerSelectedAnswerVO answerVO = new ActivityAnswerSelectedAnswerVO();
            answerVO.setQuestionId(item.getQuestionId());
            answerVO.setSelectedAnswer(item.getSelectedAnswer());
            return answerVO;
        }).toList());
        return respVO;
    }

    /**
     * 消耗任务获得的答题次数，免费次数无需写任务表。
     */
    private void consumeTaskChance(ActivityAnswerDO answer, Long memberMobile, String periodKey) {
        int freeCount = Objects.equals(answer.getFreeStatus(), 1) ? defaultZero(answer.getFreeCount()) : 0;
        int usedCount = countUsedRecords(answer, memberMobile, buildChanceSession(answer, periodKey));
        if (usedCount <= freeCount) {
            return;
        }
        List<ActivityAnswerTaskDO> taskList = taskMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerTaskDO>()
                .eq(ActivityAnswerTaskDO::getActivityId, answer.getActivityId())
                .eq(ActivityAnswerTaskDO::getMemberMobile, memberMobile)
                .eq(ActivityAnswerTaskDO::getPeriodKey, periodKey)
                .orderByAsc(ActivityAnswerTaskDO::getCreateTime));
        for (ActivityAnswerTaskDO taskDO : taskList) {
            if (defaultZero(taskDO.getGainCount()) > defaultZero(taskDO.getConsumeCount())) {
                updateTaskConsumeCount(taskDO, defaultZero(taskDO.getConsumeCount()) + 1);
                return;
            }
        }
    }

    /**
     * 构建次数统计周期。
     */
    private SessionInfo buildChanceSession(ActivityAnswerDO answer, String periodKey) {
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.periodKey = periodKey;
        sessionInfo.chancePeriodKey = periodKey;
        return sessionInfo;
    }

    /**
     * 校验答题记录是否可提交。
     */
    private void validateSubmitRecord(ActivityAnswerRecordDO record, ActivityAnswerContext context) {
        if (record == null || !Objects.equals(record.getActivityId(), context.answer.getActivityId())
                || !Objects.equals(record.getMemberMobile(), context.memberMobile)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "答题记录不存在");
        }
        if (!Objects.equals(record.getPeriodKey(), context.sessionInfo.periodKey)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "当前场次已结束");
        }
        if (!Objects.equals(record.getStatus(), 0)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "答题已完成");
        }
        if (Objects.equals(record.getCancelStatus(), 1)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), "本次答题已取消");
        }
    }

    /**
     * 查询单题明细。
     */
    private ActivityAnswerRecordDetailDO selectRecordDetail(Long recordId, Long questionId, Long memberMobile) {
        return recordDetailMapper.selectOne(new LambdaQueryWrapperX<ActivityAnswerRecordDetailDO>()
                .eq(ActivityAnswerRecordDetailDO::getRecordId, recordId)
                .eq(ActivityAnswerRecordDetailDO::getQuestionId, questionId)
                .eq(ActivityAnswerRecordDetailDO::getMemberMobile, memberMobile)
                .last("LIMIT 1"));
    }

    /**
     * 更新单题作答结果，避免更新分片键 member_mobile。
     */
    private boolean updateRecordDetailAnswer(ActivityAnswerRecordDetailDO detail) {
        return recordDetailMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerRecordDetailDO>()
                .eq(ActivityAnswerRecordDetailDO::getId, detail.getId())
                .eq(ActivityAnswerRecordDetailDO::getMemberMobile, detail.getMemberMobile())
                .eq(ActivityAnswerRecordDetailDO::getIsAnswered, 0)
                .set(ActivityAnswerRecordDetailDO::getMemberId, detail.getMemberId())
                .set(ActivityAnswerRecordDetailDO::getSelectedAnswer, detail.getSelectedAnswer())
                .set(ActivityAnswerRecordDetailDO::getAnswerResult, detail.getAnswerResult())
                .set(ActivityAnswerRecordDetailDO::getIsAnswered, detail.getIsAnswered())) > 0;
    }

    /**
     * 开始答题时写入当时题目明细，后续继续答题、提交和导出都以这里为准。
     */
    private List<ActivityAnswerRecordDetailDO> initRecordQuestionDetails(ActivityAnswerContext context,
                                                                          ActivityAnswerRecordDO record) {
        if (CollectionUtils.isEmpty(context.questions)) {
            return Collections.emptyList();
        }
        List<ActivityAnswerRecordDetailDO> details = context.questions.stream()
                .map(question -> buildRecordDetail(context, record, question))
                .toList();
        recordDetailMapper.insertBatch(details);
        return details;
    }

    /**
     * 兼容历史未完成记录：如果旧记录没有预置题目明细，则按当前题库补齐一次。
     */
    private List<ActivityAnswerRecordDetailDO> ensureRecordQuestionDetails(ActivityAnswerContext context,
                                                                            ActivityAnswerRecordDO record) {
        List<ActivityAnswerRecordDetailDO> details = selectRecordQuestionDetailsCached(
                record.getId(), record.getMemberMobile());
        if (!CollectionUtils.isEmpty(details)) {
            return details;
        }
        return initRecordQuestionDetails(context, record);
    }

    /**
     * 构建未作答题目明细。
     */
    private ActivityAnswerRecordDetailDO buildRecordDetail(ActivityAnswerContext context, ActivityAnswerRecordDO record,
                                                           ActivityAnswerQuestionDO question) {
        ActivityAnswerRecordDetailDO detail = new ActivityAnswerRecordDetailDO();
        detail.setActivityId(context.answer.getActivityId());
        detail.setRecordId(record.getId());
        detail.setMemberId(context.memberId);
        detail.setMemberMobile(context.memberMobile);
        detail.setQuestionId(question.getId());
        detail.setQuestionType(question.getQuestionType());
        detail.setQuestionTitle(question.getQuestionTitle());
        detail.setQuestionTips(question.getQuestionTips());
        detail.setOptionsJson(question.getOptionsJson());
        detail.setCorrectAnswer(question.getCorrectAnswer());
        detail.setIsAnswered(0);
        detail.setSelectedAnswer(null);
        detail.setAnswerResult(null);
        detail.setAnswerTime(question.getAnswerTime() == null ? null : question.getAnswerTime().longValue());
        return detail;
    }

    /**
     * 构建提交返回，最后一题时结算奖励。
     */
    private ActivityAnswerSubmitRespVO buildSubmitResp(ActivityAnswerContext context, ActivityAnswerRecordDO record,
                                                       ActivityAnswerRecordDetailDO detail, boolean allowFinish) {
        List<ActivityAnswerRecordDetailDO> detailList = recordDetailMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerRecordDetailDO>()
                .eq(ActivityAnswerRecordDetailDO::getRecordId, record.getId())
                .eq(ActivityAnswerRecordDetailDO::getMemberMobile, context.memberMobile)
                .orderByAsc(ActivityAnswerRecordDetailDO::getCreateTime)
                .orderByAsc(ActivityAnswerRecordDetailDO::getId));
        int answeredCount = (int) detailList.stream().filter(item -> Objects.equals(item.getIsAnswered(), 1)).count();
        int questionCount = defaultZero(record.getQuestionCount());
        boolean finished = questionCount > 0 && answeredCount >= questionCount;
        ActivityAnswerSubmitRespVO respVO = new ActivityAnswerSubmitRespVO();
        respVO.setActivityId(context.answer.getActivityId());
        respVO.setActivityType(ActivityTypeEnum.ANSWER.getCode());
        respVO.setRecordId(record.getId());
        respVO.setAnswerNo(record.getAnswerNo());
        respVO.setIsCorrect(Objects.equals(detail.getAnswerResult(), 1));
        respVO.setFinished(finished);
        respVO.setAnsweredCount(answeredCount);
        respVO.setQuestionCount(questionCount);
        respVO.setNextQuestionId(findNextQuestionId(detailList));
        respVO.setDetails(detailList.stream().map(item -> {
            ActivityAnswerSubmitDetailRespVO detailRespVO = new ActivityAnswerSubmitDetailRespVO();
            detailRespVO.setQuestionId(item.getQuestionId());
            detailRespVO.setQuestionTitle(item.getQuestionTitle());
            detailRespVO.setIsAnswered(Objects.requireNonNullElse(item.getIsAnswered(), 0));
            detailRespVO.setIsCorrect(Objects.equals(item.getAnswerResult(), 1));
            return detailRespVO;
        }).toList());
        if (finished && allowFinish) {
            finishRecordAndGrantReward(context, record, detailList, respVO);
        }
        return respVO;
    }

    /**
     * 查询下一题 ID。
     */
    private Long findNextQuestionId(List<ActivityAnswerRecordDetailDO> details) {
        return details.stream()
                .filter(item -> !Objects.equals(item.getIsAnswered(), 1))
                .map(ActivityAnswerRecordDetailDO::getQuestionId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 完成答题并发放奖励。
     */
    private void finishRecordAndGrantReward(ActivityAnswerContext context, ActivityAnswerRecordDO record,
                                            List<ActivityAnswerRecordDetailDO> detailList, ActivityAnswerSubmitRespVO respVO) {
        int questionCount = defaultZero(record.getQuestionCount());
        int correctCount = (int) detailList.stream().filter(item -> Objects.equals(item.getIsAnswered(), 1))
                .filter(item -> Objects.equals(item.getAnswerResult(), 1)).count();
        int wrongCount = (int) detailList.stream().filter(item -> Objects.equals(item.getIsAnswered(), 1))
                .filter(item -> Objects.equals(item.getAnswerResult(), 0)).count();
        BigDecimal accuracy = questionCount == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(questionCount), 2, RoundingMode.HALF_UP);

        respVO.setCorrectCount(correctCount);
        respVO.setWrongCount(wrongCount);
        respVO.setAccuracy(accuracy);
        int updated = recordMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getId, record.getId())
                .eq(ActivityAnswerRecordDO::getMemberMobile, context.memberMobile)
                .eq(ActivityAnswerRecordDO::getStatus, 0)
                .and(item -> item.isNull(ActivityAnswerRecordDO::getCancelStatus)
                        .or().eq(ActivityAnswerRecordDO::getCancelStatus, 0))
                .set(ActivityAnswerRecordDO::getMemberId, context.memberId)
                .set(ActivityAnswerRecordDO::getQuestionCount, questionCount)
                .set(ActivityAnswerRecordDO::getCorrectCount, correctCount)
                .set(ActivityAnswerRecordDO::getWrongCount, wrongCount)
                .set(ActivityAnswerRecordDO::getAccuracy, accuracy)
                .set(ActivityAnswerRecordDO::getStatus, 1)
                .set(ActivityAnswerRecordDO::getSubmitTime, LocalDateTime.now()));
        if (updated == 0) {
            ActivityAnswerRewardLogDO rewardLog = selectRewardLogByRecord(record.getId(), context.memberMobile);
            if (rewardLog != null) {
                fillRewardResp(respVO, rewardLog);
            }
            return;
        }
        grantReward(context, record, correctCount, respVO);
    }

    /**
     * 根据答题记录查询已发放奖励。
     */
    private ActivityAnswerRewardLogDO selectRewardLogByRecord(Long recordId, Long memberMobile) {
        return rewardLogMapper.selectOne(new LambdaQueryWrapperX<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getRecordId, recordId)
                .eq(ActivityAnswerRewardLogDO::getMemberMobile, memberMobile)
                .last("LIMIT 1"));
    }

    /**
     * 按正确数降级匹配并发放奖励。
     */
    private void grantReward(ActivityAnswerContext context, ActivityAnswerRecordDO record, int correctCount,
                             ActivityAnswerSubmitRespVO respVO) {
        boolean alipayMember = context.member != null && Objects.equals(context.member.getMemberCategory(), 1);
        List<ActivityAnswerRewardDO> rewards = loadRewards(context.answer, context.storeId).stream()
                .filter(item -> item.getCorrectCount() != null && item.getCorrectCount() <= correctCount)
                .filter(item -> !alipayMember || !Objects.equals(item.getPrizeType(), 5))
                .sorted((a, b) -> b.getCorrectCount().compareTo(a.getCorrectCount()))
                .toList();
        for (ActivityAnswerRewardDO reward : rewards) {
            if (!reserveRewardStock(context.answer, reward, context.storeId)) {
                continue;
            }
            try {
                ActivityAnswerRewardLogDO rewardLog = buildRewardLog(context, record, reward);
                rewardLogMapper.insert(rewardLog);
                issueActualReward(context, reward, rewardLog);
                fillRewardResp(respVO, reward, rewardLog);
                return;
            } catch (Exception e) {
                rollbackRewardStock(context.answer, reward, context.storeId);
                log.error("有奖问答奖品发放失败，activityId={}，recordId={}，rewardId={}，prizeType={}，awardId={}",
                        context.answer.getActivityId(), record.getId(), reward.getId(), reward.getPrizeType(),
                        reward.getAwardId(), e);
                throw e;
            }
        }
        respVO.setHasReward(false);
        respVO.setMessage("很遗憾，奖品已发完");
    }

    /**
     * 加载奖励列表。
     */
    private List<ActivityAnswerRewardDO> loadRewards(ActivityAnswerDO answer, Long storeId) {
        String key = Objects.equals(answer.getPrizePoolRules(), 2)
                ? RedisKeyConstants.ANSWER_REWARD + answer.getActivityId() + ":store:" + storeId
                : RedisKeyConstants.ANSWER_REWARD + answer.getActivityId();
        List<ActivityAnswerRewardDO> rewards = redisCache.getCacheObject(key);
        if (!CollectionUtils.isEmpty(rewards)) {
            return rewards;
        }
        rewards = rewardMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerRewardDO>()
                .eq(ActivityAnswerRewardDO::getActivityId, answer.getActivityId()));
        redisCache.setCacheObject(key, rewards, rewardCacheExpireSeconds(answer.getActivityId()), TimeUnit.SECONDS);
        return rewards == null ? Collections.emptyList() : rewards;
    }

    /**
     * 预占奖励库存。
     */
    private boolean reserveRewardStock(ActivityAnswerDO answer, ActivityAnswerRewardDO reward, Long storeId) {
        if (Objects.equals(answer.getPrizePoolRules(), 2)) {
            return reserveStoreRewardStock(answer, reward, storeId);
        }
        return rewardStockService.reserve(reward.getId());
    }

    /**
     * 预占门店独立库存。
     */
    private boolean reserveStoreRewardStock(ActivityAnswerDO answer, ActivityAnswerRewardDO reward, Long storeId) {
        String stockKey = RedisKeyConstants.ANSWER_REWARD_STOCK + answer.getActivityId() + ":store:" + storeId + ":reward:" + reward.getId();
        String lockKey = stockKey + ":lock";
        String lockValue = UUID.randomUUID().toString();
        if (!redisCache.lock(lockKey, lockValue, 60)) {
            return false;
        }
        try {
            int totalNum = defaultZero(reward.getTotalNum());
            Integer usedNum = redisCache.getCacheObject(stockKey);
            int used = usedNum == null ? selectStoreRewardUsedCount(answer.getActivityId(), reward, storeId) : usedNum;
            if (totalNum > 0 && used >= totalNum) {
                return false;
            }
            redisCache.setCacheObject(stockKey, used + 1,
                    rewardCacheExpireSeconds(answer.getActivityId()), TimeUnit.SECONDS);
            return true;
        } finally {
            redisCache.unlock(lockKey, lockValue);
        }
    }

    /**
     * 回滚奖励库存。
     */
    private void rollbackRewardStock(ActivityAnswerDO answer, ActivityAnswerRewardDO reward, Long storeId) {
        if (Objects.equals(answer.getPrizePoolRules(), 2)) {
            String stockKey = RedisKeyConstants.ANSWER_REWARD_STOCK + answer.getActivityId() + ":store:" + storeId + ":reward:" + reward.getId();
            String lockKey = stockKey + ":lock";
            String lockValue = UUID.randomUUID().toString();
            if (!redisCache.lock(lockKey, lockValue, 60)) {
                log.warn("回滚门店独立库存获取锁失败，activityId={}，storeId={}，rewardId={}",
                        answer.getActivityId(), storeId, reward.getId());
                return;
            }
            try {
                Integer usedNum = redisCache.getCacheObject(stockKey);
                redisCache.setCacheObject(stockKey, Math.max(defaultZero(usedNum) - 1, 0),
                        rewardCacheExpireSeconds(answer.getActivityId()), TimeUnit.SECONDS);
            } finally {
                redisCache.unlock(lockKey, lockValue);
            }
            return;
        }
        rewardStockService.rollback(reward.getId());
    }

    /**
     * Redis 独立库存缺失时，按门店真实有效发放记录恢复已使用数量。
     */
    private int selectStoreRewardUsedCount(Long activityId, ActivityAnswerRewardDO reward, Long storeId) {
        LambdaQueryWrapperX<ActivityAnswerRewardLogDO> wrapper = new LambdaQueryWrapperX<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getActivityId, activityId)
                .eq(ActivityAnswerRewardLogDO::getRewardId, reward.getId())
                .eq(ActivityAnswerRewardLogDO::getStoreId, storeId);
        if (Objects.equals(reward.getPrizeType(), 5)) {
            wrapper.in(ActivityAnswerRewardLogDO::getClaimStatus, List.of(1, 2));
        } else {
            wrapper.and(item -> item.isNull(ActivityAnswerRewardLogDO::getPrizeState)
                    .or().ne(ActivityAnswerRewardLogDO::getPrizeState, 9));
        }
        Long count = rewardLogMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    /**
     * 奖励缓存最长保留到活动结束日。
     */
    private int rewardCacheExpireSeconds(Long activityId) {
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        ActivityDO activity = cacheData == null ? null : cacheData.getActivity();
        return calculateActivityExpireSeconds(activity == null ? activityMapper.selectById(activityId) : activity);
    }

    /**
     * 实际发放可直接复用的奖品。
     */
    private void issueActualReward(ActivityAnswerContext context, ActivityAnswerRewardDO reward,
                                   ActivityAnswerRewardLogDO rewardLog) {
        WxMemberVO memberVO = context.member == null ? getWxMember(context.memberId) : context.member;
        if (Objects.equals(reward.getPrizeType(), 1)) {
            issuePoints(context, reward, memberVO);
        } else if (Objects.equals(reward.getPrizeType(), 2)) {
            issueCoupon(reward, memberVO);
        } else if (Objects.equals(reward.getPrizeType(), 3)) {
            issueCouponPackage(context, reward, memberVO);
        } else if (Objects.equals(reward.getPrizeType(), 5)) {
            issueRedPacket(context, reward, rewardLog, memberVO);
        } else if (!Objects.equals(reward.getPrizeType(), 4)) {
            throw exception(ANSWER_REWARD_CONFIG_ERROR);
        }
    }

    /**
     * 发放现金红包并记录前端调起领取所需参数。
     */
    private void issueRedPacket(ActivityAnswerContext context, ActivityAnswerRewardDO reward,
                                ActivityAnswerRewardLogDO rewardLog, WxMemberVO memberVO) {
        if (memberVO == null || !StringUtils.hasText(memberVO.getOpenid())
                || reward.getPrizeValue() == null || reward.getPrizeValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ANSWER_REWARD_CONFIG_ERROR);
        }
        LotteryRedPacketVo redPacketVo = new LotteryRedPacketVo();
        redPacketVo.setOpenId(memberVO.getOpenid());
        redPacketVo.setUserName(resolveMemberName(memberVO));
        redPacketVo.setTransferAmount(reward.getPrizeValue().multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValue());
        redPacketVo.setTransferRemark("有奖问答活动红包");
        redPacketVo.setUserRecvPerception("有奖问答活动红包奖励");
        redPacketVo.setActivityId(context.answer.getActivityId());

        TransferToUser.TransferToUserResponse response;
        try {
            response = lotteryRedPacketService.transferUser(redPacketVo);
        } catch (Exception e) {
            log.error("有奖问答红包调用微信转账失败，activityId={}，recordId={}，outBillNo={}",
                    context.answer.getActivityId(), rewardLog.getRecordId(), redPacketVo.getOutBillNo(), e);
            throw exception(ANSWER_RED_PACKET_ISSUE_FAILED);
        }
        if (!isRedPacketTransferSuccess(response)) {
            throw exception(ANSWER_RED_PACKET_ISSUE_FAILED);
        }
        rewardLog.setPackageInfo(response.getPackageInfo());
        rewardLog.setOutBillNo(response.getOutBillNo());
        rewardLog.setClaimStatus(1);
        rewardLogMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerRewardLogDO>()
                .eq(ActivityAnswerRewardLogDO::getId, rewardLog.getId())
                .eq(ActivityAnswerRewardLogDO::getMemberMobile, rewardLog.getMemberMobile())
                .set(ActivityAnswerRewardLogDO::getPackageInfo, rewardLog.getPackageInfo())
                .set(ActivityAnswerRewardLogDO::getOutBillNo, rewardLog.getOutBillNo())
                .set(ActivityAnswerRewardLogDO::getClaimStatus, rewardLog.getClaimStatus()));
    }

    /**
     * 获取红包收款人展示名称。
     */
    private String resolveMemberName(WxMemberVO memberVO) {
        if (memberVO != null && StringUtils.hasText(memberVO.getMemberNickName())) {
            return memberVO.getMemberNickName();
        }
        if (memberVO != null && StringUtils.hasText(memberVO.getMemberName())) {
            return memberVO.getMemberName();
        }
        return "微信用户";
    }

    /**
     * 按会员 ID 查询会员资料并获取写入记录的会员名称。
     */
    private String loadMemberName(Long memberId) {
        if (memberId == null) {
            return "微信用户";
        }
        try {
            CommonResult<WxMemberVO> result = wxMemberApi.getWxMemberById(memberId);
            return resolveMemberName(result == null ? null : result.getData());
        } catch (Exception e) {
            log.warn("查询会员名称失败，memberId={}", memberId, e);
            return "微信用户";
        }
    }

    /**
     * 判断微信红包转账是否已受理成功。
     */
    private boolean isRedPacketTransferSuccess(TransferToUser.TransferToUserResponse response) {
        if (response == null || response.getCode() == null || response.getState() == null) {
            return false;
        }
        return Objects.equals(response.getCode(), 200)
                && (Objects.equals(response.getState(), TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM)
                || Objects.equals(response.getState(), TransferToUser.TransferBillStatus.SUCCESS));
    }

    /**
     * 发放积分。
     */
    private void issuePoints(ActivityAnswerContext context, ActivityAnswerRewardDO reward, WxMemberVO memberVO) {
        int points = reward.getPrizeValue() == null ? 0 : reward.getPrizeValue().intValue();
        if (memberVO == null || points <= 0) {
            throw exception(ANSWER_REWARD_CONFIG_ERROR);
        }
        int newIntegral = defaultZero(memberVO.getMemberIntegral()) + points;
        wxMemberApi.updateMemberById(memberVO.getMemberId(), newIntegral);
        PointsLogDO pointsLog = new PointsLogDO();
        pointsLog.setMemberId(memberVO.getMemberId());
        pointsLog.setPointsChange((long) points);
        pointsLog.setMemberName(memberVO.getMemberName());
        pointsLog.setMemberNickName(memberVO.getMemberNickName());
        pointsLog.setMemberMobile(memberVO.getMemberMobile());
        pointsLog.setLogCode(String.valueOf(System.currentTimeMillis()));
        long id = identifierGenerator.nextId(null).longValue();
        pointsLog.setPointsLogId(id);
        pointsLog.setPointsType(1);
        pointsLog.setIsDelete(0);
        pointsLog.setCreateTime(new Date());
        pointsLog.setUpdateTime(new Date());
        pointsLog.setPointsLogStatus(1);
        pointsLog.setIsPointsProduct(2);
        pointsLog.setProductType(3);
        pointsLog.setProductId(context.answer.getActivityId());
        pointsLog.setShardingValue(memberVO.getShardingValue());
        if (pointsLog.getShardingValue() != null) {
            BigInteger finalId = new BigInteger(id + String.valueOf(pointsLog.getShardingValue())).mod(BigInteger.valueOf(Long.MAX_VALUE));
            pointsLog.setPointsLogId(finalId.longValueExact());
        }
        lotteryAddLogService.addPointsLog(pointsLog);
    }

    /**
     * 发放优惠券。
     */
    private void issueCoupon(ActivityAnswerRewardDO reward, WxMemberVO memberVO) {
        if (memberVO == null || reward.getAwardId() == null) {
            throw exception(ANSWER_REWARD_CONFIG_ERROR);
        }
        GoodCouponDO goodCoupon = lotteryAddLogService.getGoodCoupon(reward.getAwardId());
        if (goodCoupon == null) {
            throw exception(ANSWER_COUPON_NOT_FOUND);
        }
        UserCouponDO coupon = BeanUtils.toBean(goodCoupon, UserCouponDO.class);
        coupon.setUserId(memberVO.getMemberId());
        coupon.setCouponId(reward.getAwardId());
        coupon.setIsUsed(0);
        coupon.setId(null);
        coupon.setUseTime(null);
        parseUserCouponTime(goodCoupon, coupon);
        coupon.setMemberMobile(memberVO.getMemberMobile());
        coupon.setCouponSource(CouponSourceType.ANSWER.getCode());
        lotteryAddLogService.addCoupon(coupon);
    }

    /**
     * 发放优惠券包。
     */
    private void issueCouponPackage(ActivityAnswerContext context, ActivityAnswerRewardDO reward,
                                    WxMemberVO memberVO) {
        if (memberVO == null || reward.getAwardId() == null) {
            throw exception(ANSWER_REWARD_CONFIG_ERROR);
        }
        WxMemberDTO memberDTO = BeanUtils.toBean(memberVO, WxMemberDTO.class);
        Boolean success = couponPackageService.claimCouponPackage(
                memberDTO,
                reward.getAwardId(),
                CouponSourceType.ANSWER.getCode(),
                null,
                context.storeId
        );
        if (!Boolean.TRUE.equals(success)) {
            throw exception(ANSWER_COUPON_PACKAGE_ISSUE_FAILED);
        }
    }

    /**
     * 构建奖励发放记录。
     */
    private ActivityAnswerRewardLogDO buildRewardLog(ActivityAnswerContext context, ActivityAnswerRecordDO record,
                                                     ActivityAnswerRewardDO reward) {
        ActivityAnswerRewardLogDO rewardLog = new ActivityAnswerRewardLogDO();
        rewardLog.setRecordId(record.getId());
        rewardLog.setAnswerNo(record.getAnswerNo());
        rewardLog.setActivityId(context.answer.getActivityId());
        rewardLog.setMemberId(context.memberId);
        rewardLog.setMemberName(StringUtils.hasText(record.getMemberName())
                ? record.getMemberName() : loadMemberName(context.memberId));
        rewardLog.setMemberMobile(context.memberMobile);
        rewardLog.setStoreId(context.storeId);
        rewardLog.setStoreName(record.getStoreName());
        rewardLog.setRewardId(reward.getId());
        rewardLog.setRewardCorrectCount(reward.getCorrectCount());
        rewardLog.setPrizeType(reward.getPrizeType());
        rewardLog.setPrizeId(reward.getAwardId());
        rewardLog.setPrizeName(reward.getPrizeName());
        rewardLog.setPrizeImgUrl(reward.getPrizeImgUrl());
        rewardLog.setPrizeValue(reward.getPrizeValue());
        rewardLog.setClaimStatus(Objects.equals(reward.getPrizeType(), 5) ? 1 : null);
        rewardLog.setPrizeState(Objects.equals(reward.getPrizeType(), 4) ? 1 : 0);
        rewardLog.setGrantTime(LocalDateTime.now());
        return rewardLog;
    }

    /**
     * 填充发奖返回。
     */
    private void fillRewardResp(ActivityAnswerSubmitRespVO respVO, ActivityAnswerRewardDO reward, ActivityAnswerRewardLogDO rewardLog) {
        respVO.setHasReward(true);
        respVO.setRewardLogId(rewardLog.getId());
        respVO.setRewardCorrectCount(reward.getCorrectCount());
        respVO.setPrizeType(reward.getPrizeType());
        respVO.setPrizeId(reward.getAwardId());
        respVO.setPrizeName(reward.getPrizeName());
        respVO.setPrizeImgUrl(reward.getPrizeImgUrl());
        respVO.setPrizeValue(reward.getPrizeValue());
        respVO.setClaimStatus(rewardLog.getClaimStatus());
        respVO.setPackageInfo(rewardLog.getPackageInfo());
        respVO.setOutBillNo(rewardLog.getOutBillNo());
        respVO.setMessage("恭喜获得奖励");
    }

    /**
     * 使用已存在的发放记录填充幂等返回。
     */
    private void fillRewardResp(ActivityAnswerSubmitRespVO respVO, ActivityAnswerRewardLogDO rewardLog) {
        respVO.setHasReward(true);
        respVO.setRewardLogId(rewardLog.getId());
        respVO.setRewardCorrectCount(rewardLog.getRewardCorrectCount());
        respVO.setPrizeType(rewardLog.getPrizeType());
        respVO.setPrizeId(rewardLog.getPrizeId());
        respVO.setPrizeName(rewardLog.getPrizeName());
        respVO.setPrizeImgUrl(rewardLog.getPrizeImgUrl());
        respVO.setPrizeValue(rewardLog.getPrizeValue());
        respVO.setClaimStatus(effectiveClaimStatus(rewardLog));
        respVO.setPackageInfo(rewardLog.getPackageInfo());
        respVO.setOutBillNo(rewardLog.getOutBillNo());
        respVO.setMessage("恭喜获得奖励");
    }

    /**
     * 构建我的答题记录返回。
     */
    private ActivityAnswerMyRecordRespVO buildMyRecordResp(ActivityAnswerRecordDO record) {
        ActivityAnswerMyRecordRespVO respVO = new ActivityAnswerMyRecordRespVO();
        respVO.setRecordId(record.getId());
        respVO.setAnswerNo(record.getAnswerNo());
        respVO.setActivityId(record.getActivityId());
        respVO.setAnswerTitle(loadAnswerSetting(record.getActivityId()).getAnswerTitle());
        respVO.setStoreId(record.getStoreId());
        respVO.setStoreName(record.getStoreName());
        respVO.setQuestionCount(record.getQuestionCount());
        respVO.setCorrectCount(record.getCorrectCount());
        respVO.setWrongCount(record.getWrongCount());
        respVO.setAccuracy(record.getAccuracy());
        respVO.setStatus(record.getStatus());
        respVO.setCancelStatus(Objects.requireNonNullElse(record.getCancelStatus(), 0));
        respVO.setStartTime(record.getStartTime());
        respVO.setParticipationTime(record.getStartTime());
        respVO.setSubmitTime(record.getSubmitTime());
        respVO.setDetails(buildSubmitDetails(record.getId(), record.getMemberMobile()));
        return respVO;
    }

    /**
     * 构建提交明细列表。
     */
    private List<ActivityAnswerSubmitDetailRespVO> buildSubmitDetails(Long recordId, Long memberMobile) {
        List<ActivityAnswerRecordDetailDO> details = recordDetailMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerRecordDetailDO>()
                .eq(ActivityAnswerRecordDetailDO::getRecordId, recordId)
                .eq(ActivityAnswerRecordDetailDO::getMemberMobile, memberMobile));
        if (CollectionUtils.isEmpty(details)) {
            return Collections.emptyList();
        }
        return details.stream().map(item -> {
            ActivityAnswerSubmitDetailRespVO respVO = new ActivityAnswerSubmitDetailRespVO();
            respVO.setQuestionId(item.getQuestionId());
            respVO.setIsCorrect(Objects.equals(item.getAnswerResult(), 1));
            return respVO;
        }).toList();
    }

    /**
     * 构建我的奖励返回。
     */
    private ActivityAnswerMyRewardRespVO buildMyRewardResp(ActivityAnswerRewardLogDO rewardLog) {
        ActivityAnswerMyRewardRespVO respVO = new ActivityAnswerMyRewardRespVO();
        respVO.setRewardLogId(rewardLog.getId());
        respVO.setRecordId(rewardLog.getRecordId());
        respVO.setAnswerNo(rewardLog.getAnswerNo());
        respVO.setActivityId(rewardLog.getActivityId());
        respVO.setActivityType(ActivityTypeEnum.ANSWER.getCode());
        respVO.setAnswerTitle(loadAnswerSetting(rewardLog.getActivityId()).getAnswerTitle());
        respVO.setPrizeType(rewardLog.getPrizeType());
        respVO.setPrizeId(rewardLog.getPrizeId());
        respVO.setPrizeName(rewardLog.getPrizeName());
        respVO.setPrizeImgUrl(rewardLog.getPrizeImgUrl());
        respVO.setPrizeValue(rewardLog.getPrizeValue());
        respVO.setClaimStatus(effectiveClaimStatus(rewardLog));
        respVO.setPackageInfo(rewardLog.getPackageInfo());
        respVO.setOutBillNo(rewardLog.getOutBillNo());
        respVO.setPrizeState(rewardLog.getPrizeState());
        respVO.setReceiveUser(rewardLog.getReceiveUser());
        respVO.setReceiveMobile(rewardLog.getReceiveMobile());
        respVO.setReceiveAddress(rewardLog.getReceiveAddress());
        respVO.setExpressCompany(rewardLog.getExpressCompany());
        respVO.setTrackingNumber(rewardLog.getTrackingNumber());
        respVO.setGrantTime(rewardLog.getGrantTime());
        return respVO;
    }

    /**
     * 红包超过 24 小时未领取则展示已失效。
     */
    private Integer effectiveClaimStatus(ActivityAnswerRewardLogDO rewardLog) {
        if (rewardLog == null || !Objects.equals(rewardLog.getPrizeType(), 5) || !Objects.equals(rewardLog.getClaimStatus(), 1)
                || rewardLog.getGrantTime() == null) {
            return rewardLog == null ? null : rewardLog.getClaimStatus();
        }
        return !rewardLog.getGrantTime().plusHours(24).isAfter(LocalDateTime.now()) ? 3 : rewardLog.getClaimStatus();
    }

    /**
     * 获取会员信息。
     */
    private WxMemberVO getWxMember(Long memberId) {
        if (memberId == null) {
            return null;
        }
        String cacheKey = RedisKeyConstants.ANSWER_MEMBER + memberId;
        WxMemberVO cachedMember = redisCache.getCacheObject(cacheKey);
        if (cachedMember != null) {
            return cachedMember;
        }
        try {
            CommonResult<WxMemberVO> result = wxMemberApi.getWxMemberById(memberId);
            WxMemberVO member = result == null ? null : result.getData();
            if (member != null) {
                redisCache.setCacheObjectQuietly(cacheKey, member, 30, TimeUnit.MINUTES);
            }
            return member;
        } catch (Exception e) {
            log.warn("获取会员信息失败，memberId={}", memberId, e);
            return null;
        }
    }

    /**
     * 解析用户优惠券有效期。
     */
    private void parseUserCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        if (goodCoupon.getUseType() == 0) {
            userCoupon.setExpirationTime(goodCoupon.getCouponEndTime());
            userCoupon.setVaildStartTime(goodCoupon.getCouponStartTime());
        } else if (goodCoupon.getUseType() == 1) {
            userCoupon.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.parseInt(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        } else if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[0] + DateUtils.T_00_00_00));
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, split[1] + DateUtils.T_23_59_59));
        }
    }

    /**
     * 按缓存优先加载有奖问答配置。
     */
    private ActivityAnswerDO loadAnswerSetting(Long activityId) {
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        if (cacheData != null && cacheData.getAnswer() != null) {
            ActivityAnswerDO answer = cacheData.getAnswer();
            mergeActivitySnapshot(answer, cacheData.getActivity());
            return answer;
        }
        ActivityAnswerDO answer = redisCache.getCacheObject(RedisKeyConstants.ANSWER_SETTING + activityId);
        if (answer != null) {
            return answer;
        }
        answer = activityAnswerMapper.selectOne(ActivityAnswerDO::getActivityId, activityId);
        if (answer == null) {
            answer = new ActivityAnswerDO();
            answer.setActivityId(activityId);
        }
        ActivityDO activity = activityMapper.selectById(activityId);
        mergeActivitySnapshot(answer, activity);
        redisCache.setCacheObjectQuietly(RedisKeyConstants.ANSWER_SETTING + activityId, answer,
                calculateActivityExpireSeconds(activity), TimeUnit.SECONDS);
        return answer;
    }

    /**
     * 按聚合缓存优先加载有奖问答配置，缓存结构参考抽奖 LotterySettingsCacheDataVO。
     */
    private ActivityAnswerSettingsCacheDataVO loadAnswerCache(Long activityId) {
        return redisCache.getCacheObject(RedisKeyConstants.ANSWER_CACHE + activityId);
    }

    /**
     * 优先从聚合缓存读取活动主表信息，缓存缺失时再查询数据库。
     */
    private ActivityDO loadActivity(Long activityId) {
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        if (cacheData != null && cacheData.getActivity() != null) {
            return cacheData.getActivity();
        }
        return activityMapper.selectById(activityId);
    }

    /**
     * 仅社群专享活动执行社群校验，普通活动不产生额外查询和远程调用。
     */
    private void validateCommunityIfNecessary(ActivityDO activity) {
        if (activity == null || !Objects.equals(activity.getCommunityFlag(), 2)) {
            return;
        }
        String unionId = SecurityFrameworkUtils.getLoginUnionid();
        String cacheKey = unionId == null ? null
                : RedisKeyConstants.ANSWER_COMMUNITY + activity.getId() + ":" + unionId;
        Boolean cached = cacheKey == null ? null : redisCache.getCacheObject(cacheKey);
        if (Boolean.TRUE.equals(cached)) {
            return;
        }
        if (!activityAppService.checkCanJoin(activity)) {
            throw exception(LOTTERY_WECOMGROUP_ERROR);
        }
        if (cacheKey != null) {
            redisCache.setCacheObjectQuietly(cacheKey, true, 5, TimeUnit.MINUTES);
        }
    }

    /**
     * 开始答题只需要手机号和昵称，优先从登录上下文构建，避免额外会员远程调用。
     */
    private WxMemberVO getCurrentLoginMember(Long memberId) {
        if (Objects.equals(memberId, SecurityFrameworkUtils.getLoginUserId())) {
            String mobile = SecurityFrameworkUtils.getLoginMobile();
            if (StringUtils.hasText(mobile)) {
                WxMemberVO member = new WxMemberVO();
                member.setMemberMobile(mobile);
                member.setMemberNickName(SecurityFrameworkUtils.getLoginUserNickname());
                return member;
            }
        }
        return getWxMember(memberId);
    }

    /**
     * 按缓存优先加载题目列表。
     */
    private List<ActivityAnswerQuestionDO> loadQuestions(Long activityId) {
        return loadQuestions(activityId, null);
    }

    /**
     * 加载题目时复用已查询的活动，避免缓存缺失时重复查询活动主表。
     */
    private List<ActivityAnswerQuestionDO> loadQuestions(Long activityId, ActivityDO loadedActivity) {
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
        if (cacheData != null && !CollectionUtils.isEmpty(cacheData.getQuestions())) {
            return cacheData.getQuestions();
        }
        List<ActivityAnswerQuestionDO> questions = redisCache.getCacheObject(RedisKeyConstants.ANSWER_QUESTION + activityId);
        if (!CollectionUtils.isEmpty(questions)) {
            return questions;
        }
        questions = questionMapper.selectList(ActivityAnswerQuestionDO::getActivityId, activityId);
        ActivityDO activity = loadedActivity == null ? activityMapper.selectById(activityId) : loadedActivity;
        int expireSeconds = calculateActivityExpireSeconds(activity);
        List<ActivityAnswerQuestionDO> cachedQuestions = questions == null ? Collections.emptyList() : questions;
        redisCache.setCacheObjectQuietly(RedisKeyConstants.ANSWER_QUESTION + activityId,
                cachedQuestions, expireSeconds, TimeUnit.SECONDS);
        if (cacheData != null) {
            cacheData.setQuestions(cachedQuestions);
            redisCache.setCacheObjectQuietly(RedisKeyConstants.ANSWER_CACHE + activityId,
                    cacheData, expireSeconds, TimeUnit.SECONDS);
        }
        return cachedQuestions;
    }

    /**
     * 合并活动主表快照字段。
     */
    private void mergeActivitySnapshot(ActivityAnswerDO answer, ActivityDO activity) {
        if (answer == null || activity == null) {
            return;
        }
        answer.setAnswerTitle(activity.getActivityName());
        answer.setAnswerRule(activity.getActivityRules());
        answer.setState(activity.getIsEnabled());
        answer.setActivityStore(activity.getActivityStore());
        answer.setCommunityFlag(activity.getCommunityFlag());
        answer.setGuideImage(activity.getGuideImage());
    }

    /**
     * 构建详情基础信息。
     */
    private ActivityAnswerAppDetailRespVO buildBaseDetail(ActivityAnswerDO answer) {
        ActivityAnswerAppDetailRespVO respVO = new ActivityAnswerAppDetailRespVO();
        respVO.setActivityId(answer.getActivityId());
        respVO.setAnswerTitle(answer.getAnswerTitle());
        respVO.setAnswerRule(answer.getAnswerRule());
        respVO.setActivityImgUrl(answer.getActivityImgUrl());
        respVO.setActivityBackground(answer.getActivityBackground());
        respVO.setButtonImgUrl(answer.getButtonImgUrl());
        respVO.setActivityDetailLongImage(answer.getActivityDetailLongImage());
        respVO.setQuestionBackgroundImage(answer.getQuestionBackgroundImage());
        respVO.setUnselectedOptionImage(answer.getUnselectedOptionImage());
        respVO.setSelectedOptionImage(answer.getSelectedOptionImage());
        respVO.setPreviousButtonImage(answer.getPreviousButtonImage());
        respVO.setNextButtonImage(answer.getNextButtonImage());
        respVO.setSubmitButtonImage(answer.getSubmitButtonImage());
        respVO.setShareTitle(answer.getShareTitle());
        respVO.setShareNote(answer.getShareNote());
        respVO.setShareImgUrl(answer.getShareImgUrl());
        respVO.setShareType(answer.getShareType());
        respVO.setAnswerAction(ACTION_DISABLED);
        respVO.setBackgroundColor(answer.getBackgroundColor());
        respVO.setAnswerActionText("活动未开始");
        return respVO;
    }

    /**
     * 构建题目返回列表。
     */
    private List<ActivityAnswerQuestionRespVO> buildQuestionRespList(List<ActivityAnswerQuestionDO> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }
        return questions.stream().map(question -> {
            ActivityAnswerQuestionRespVO respVO = new ActivityAnswerQuestionRespVO();
            respVO.setQuestionId(question.getId());
            respVO.setQuestionType(question.getQuestionType());
            respVO.setQuestionTitle(question.getQuestionTitle());
            respVO.setQuestionTips(question.getQuestionTips());
            respVO.setCorrectAnswer(question.getCorrectAnswer());
            respVO.setAnswerTime(question.getAnswerTime());
            respVO.setOptions(parseOptions(question.getOptionsJson()));
            respVO.setIsAnswered(0);
            return respVO;
        }).toList();
    }

    /**
     * 将答题记录明细里的当次题目转换为前端题目列表。
     */
    private List<ActivityAnswerQuestionRespVO> buildQuestionRespListFromDetails(List<ActivityAnswerRecordDetailDO> details) {
        if (CollectionUtils.isEmpty(details)) {
            return Collections.emptyList();
        }
        return details.stream().map(detail -> {
            ActivityAnswerQuestionRespVO respVO = new ActivityAnswerQuestionRespVO();
            respVO.setQuestionId(detail.getQuestionId());
            respVO.setQuestionType(detail.getQuestionType());
            respVO.setQuestionTitle(detail.getQuestionTitle());
            respVO.setQuestionTips(detail.getQuestionTips());
            respVO.setCorrectAnswer(detail.getCorrectAnswer());
            respVO.setAnswerTime(detail.getAnswerTime() == null ? null : detail.getAnswerTime().intValue());
            respVO.setOptions(parseOptions(detail.getOptionsJson()));
            respVO.setIsAnswered(defaultZero(detail.getIsAnswered()));
            respVO.setSelectedAnswer(detail.getSelectedAnswer());
            return respVO;
        }).toList();
    }

    /**
     * 解析题目选项 JSON。
     */
    private List<ActivityAnswerQuestionOptionRespVO> parseOptions(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = JSON.parseArray(optionsJson);
            List<ActivityAnswerQuestionOptionRespVO> options = new ArrayList<>();
            for (Object item : array) {
                ActivityAnswerQuestionOptionRespVO option = new ActivityAnswerQuestionOptionRespVO();
                if (item instanceof JSONObject object) {
                    option.setCode(firstText(object, "code", "key", "value"));
                    option.setText(firstText(object, "text", "label", "name", "value"));
                } else if (item != null) {
                    option.setCode(String.valueOf(item));
                    option.setText(String.valueOf(item));
                }
                options.add(option);
            }
            return options;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 从 JSON 对象中读取第一个非空文本。
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
     * 构建任务配置返回。
     */
    private ActivityAnswerTaskConfigRespVO buildTaskConfig(ActivityAnswerDO answer) {
        ActivityAnswerTaskConfigRespVO respVO = new ActivityAnswerTaskConfigRespVO();
        respVO.setFreeStatus(answer.getFreeStatus());
        respVO.setFreeCount(answer.getFreeCount());
        respVO.setSignStatus(answer.getSignStatus());
        respVO.setSignCount(answer.getSignCount());
        respVO.setOrderStatus(answer.getOrderStatus());
        respVO.setPlaceOrderAnswerNumber(answer.getPlaceOrderAnswerNumber());
        respVO.setShareEvent(answer.getShareEvent());
        respVO.setShareCount(answer.getShareCount());
        respVO.setBrowseType(answer.getBrowseType());
        respVO.setBrowseCount(answer.getBrowseCount());
        return respVO;
    }

    /**
     * 构建任务列表。
     */
    private List<ActivityAnswerTaskRespVO> buildTaskList(ActivityAnswerDO answer, Long memberMobile, String periodKey) {
        Map<Integer, ActivityAnswerTaskDO> taskMap = loadTaskRecordMap(answer.getActivityId(), memberMobile, periodKey);
        return buildTaskList(answer, taskMap);
    }

    /**
     * 使用已查询的任务记录构建任务列表。
     */
    private List<ActivityAnswerTaskRespVO> buildTaskList(ActivityAnswerDO answer,
                                                         Map<Integer, ActivityAnswerTaskDO> taskMap) {
        List<ActivityAnswerTaskRespVO> tasks = new ArrayList<>();
        if (Objects.equals(answer.getSignStatus(), 1)) {
            tasks.add(buildTask(taskMap, TASK_SIGN, "sign", "每日签到",
                    defaultZero(answer.getSignCount()), BigDecimal.ZERO));
        }
        if (Objects.equals(answer.getBrowseType(), 1)) {
            tasks.add(buildTask(taskMap, TASK_BROWSE, "browse", "浏览首页",
                    defaultZero(answer.getBrowseCount()), BigDecimal.ZERO));
        }
        if (Objects.equals(answer.getOrderStatus(), 1)) {
            BigDecimal paymentThreshold = Objects.equals(answer.getPaymentType(), 1) && answer.getPaymentCount() != null
                    ? answer.getPaymentCount() : BigDecimal.ZERO;
            int orderTaskLimit = Objects.equals(answer.getPlaceOrderAnswer(), 1)
                    ? -1 : defaultZero(answer.getPlaceOrderAnswerNumber());
            tasks.add(buildTask(taskMap, TASK_ORDER, "order", "订单满额",
                    orderTaskLimit, paymentThreshold));
        }
        if (Objects.equals(answer.getShareEvent(), 1)) {
            tasks.add(buildTask(taskMap, TASK_SHARE, "share", "分享活动",
                    defaultZero(answer.getShareCount()), BigDecimal.ZERO));
        }
        return tasks;
    }

    /**
     * 一次查询当前周期的全部任务记录，避免任务列表逐项访问分表。
     */
    private Map<Integer, ActivityAnswerTaskDO> loadTaskRecordMap(Long activityId, Long memberMobile, String periodKey) {
        if (memberMobile == null || !StringUtils.hasText(periodKey)) {
            return Collections.emptyMap();
        }
        String cacheKey = buildTaskCacheKey(activityId, memberMobile, periodKey);
        List<ActivityAnswerTaskDO> cachedTasks = redisCache.getCacheObjectOrDelete(cacheKey);
        if (cachedTasks != null) {
            return cachedTasks.stream().collect(Collectors.toMap(ActivityAnswerTaskDO::getTaskType,
                    Function.identity(), (first, second) -> second));
        }
        List<ActivityAnswerTaskDO> taskList = taskMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerTaskDO>()
                .eq(ActivityAnswerTaskDO::getActivityId, activityId)
                .eq(ActivityAnswerTaskDO::getMemberMobile, memberMobile)
                .eq(ActivityAnswerTaskDO::getPeriodKey, periodKey));
        redisCache.setCacheObjectQuietly(cacheKey,
                taskList == null ? Collections.emptyList() : taskList, 5, TimeUnit.MINUTES);
        if (CollectionUtils.isEmpty(taskList)) {
            return Collections.emptyMap();
        }
        return taskList.stream().collect(Collectors.toMap(ActivityAnswerTaskDO::getTaskType,
                Function.identity(), (first, second) -> second));
    }

    /**
     * 构建当前周期任务缓存键。
     */
    private String buildTaskCacheKey(Long activityId, Long memberMobile, String periodKey) {
        return RedisKeyConstants.ANSWER_TASK + activityId + ":" + memberMobile + ":" + periodKey;
    }

    /**
     * 构建单个任务返回。
     */
    private ActivityAnswerTaskRespVO buildTask(Map<Integer, ActivityAnswerTaskDO> taskMap, int taskTypeCode,
                                               String taskType, String taskName, int limitCount,
                                               BigDecimal paymentThreshold) {
        ActivityAnswerTaskDO taskDO = taskMap.get(taskTypeCode);
        int finishCount = defaultZero(taskDO == null ? null : taskDO.getFinishCount());
        ActivityAnswerTaskRespVO respVO = new ActivityAnswerTaskRespVO();
        respVO.setTaskType(taskType);
        respVO.setTaskName(taskName);
        respVO.setRewardChance(TASK_REWARD_COUNT);
        respVO.setFinishCount(finishCount);
        respVO.setLimitCount(limitCount);
        respVO.setPaymentThreshold(paymentThreshold);
        respVO.setTaskStatus(limitCount < 0 ? 0 : (limitCount == 0 || finishCount >= limitCount ? 2 : 0));
        return respVO;
    }

    /**
     * 计算当前可用答题次数。
     */
    private int calculateChanceCount(ActivityAnswerDO answer, Long memberMobile, SessionInfo sessionInfo, boolean useCache) {
        return calculateChanceCount(answer, memberMobile, sessionInfo, useCache, null);
    }

    /**
     * 计算答题次数，允许复用调用方已经查询的已使用次数。
     */
    private int calculateChanceCount(ActivityAnswerDO answer, Long memberMobile, SessionInfo sessionInfo,
                                     boolean useCache, Integer knownUsedCount) {
        return calculateChanceCount(answer, memberMobile, sessionInfo, useCache, knownUsedCount, null);
    }

    /**
     * 计算答题次数，复用已查询的任务次数和已使用次数。
     */
    private int calculateChanceCount(ActivityAnswerDO answer, Long memberMobile, SessionInfo sessionInfo,
                                     boolean useCache, Integer knownUsedCount, Integer knownTaskGainCount) {
        if (memberMobile == null || !StringUtils.hasText(sessionInfo.chancePeriodKey)) {
            return 0;
        }
        String cacheKey = RedisKeyConstants.ANSWER_NUM + answer.getActivityId() + ":" + memberMobile + ":" + sessionInfo.chancePeriodKey;
        Integer cached = useCache ? redisCache.getCacheObject(cacheKey) : null;
        if (cached != null) {
            return Math.max(cached, 0);
        }
        int totalChance = Objects.equals(answer.getFreeStatus(), 1) ? defaultZero(answer.getFreeCount()) : 0;
        totalChance += knownTaskGainCount == null
                ? countTaskGainChance(answer, memberMobile, sessionInfo.chancePeriodKey) : knownTaskGainCount;
        int usedCount = knownUsedCount == null ? countUsedRecords(answer, memberMobile, sessionInfo) : knownUsedCount;
        int chanceCount = Math.max(totalChance - usedCount, 0);
        int totalLimit = defaultZero(answer.getAnswerTotalNumber());
        if (totalLimit > 0) {
            int totalRemaining = Math.max(totalLimit - countActivityUsedRecords(answer.getActivityId(), memberMobile), 0);
            chanceCount = Math.min(chanceCount, totalRemaining);
        }
        redisCache.setCacheObjectQuietly(cacheKey, chanceCount, sessionInfo.expireSeconds, TimeUnit.SECONDS);
        return chanceCount;
    }

    /**
     * 统计任务累计获得的答题次数，已使用次数统一由答题记录扣减，避免重复扣减。
     */
    private int countTaskGainChance(ActivityAnswerDO answer, Long memberMobile, String periodKey) {
        if (memberMobile == null || !StringUtils.hasText(periodKey)) {
            return 0;
        }
        List<ActivityAnswerTaskDO> taskList = taskMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerTaskDO>()
                .eq(ActivityAnswerTaskDO::getActivityId, answer.getActivityId())
                .eq(ActivityAnswerTaskDO::getMemberMobile, memberMobile)
                .eq(ActivityAnswerTaskDO::getPeriodKey, periodKey));
        if (CollectionUtils.isEmpty(taskList)) {
            return 0;
        }
        return taskList.stream()
                .mapToInt(item -> Math.max(defaultZero(item.getGainCount()), 0))
                .sum();
    }

    /**
     * 统计已加载任务记录获得的答题次数。
     */
    private int countTaskGainChance(Map<Integer, ActivityAnswerTaskDO> taskMap) {
        return taskMap.values().stream()
                .mapToInt(item -> Math.max(defaultZero(item.getGainCount()), 0))
                .sum();
    }

    /**
     * 统计当前周期已使用答题次数。
     */
    private int countUsedRecords(ActivityAnswerDO answer, Long memberMobile, SessionInfo sessionInfo) {
        if (memberMobile == null) {
            return 0;
        }
        LambdaQueryWrapperX<ActivityAnswerRecordDO> wrapper = new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getActivityId, answer.getActivityId())
                .eq(ActivityAnswerRecordDO::getMemberMobile, memberMobile)
                .in(ActivityAnswerRecordDO::getStatus, List.of(0, 1));
        if (Objects.equals(answer.getCalculationRules(), 2)) {
            wrapper.eq(ActivityAnswerRecordDO::getPeriodKey, sessionInfo.periodKey);
        } else {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            wrapper.ge(ActivityAnswerRecordDO::getStartTime, todayStart)
                    .lt(ActivityAnswerRecordDO::getStartTime, todayStart.plusDays(1));
        }
        Long count = recordMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    /**
     * 按手机号统计活动期间已经开始的答题次数。
     */
    private int countActivityUsedRecords(Long activityId, Long memberMobile) {
        Long count = recordMapper.selectCount(new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getActivityId, activityId)
                .eq(ActivityAnswerRecordDO::getMemberMobile, memberMobile)
                .in(ActivityAnswerRecordDO::getStatus, List.of(0, 1)));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 填充开始或继续答题状态。
     */
    private void fillAnswerAction(ActivityAnswerAppDetailRespVO respVO, ActivityDO activity, List<Long> storeIds,
                                  ActivityAnswerDO answer, Long storeId, SessionInfo sessionInfo, int chanceCount,
                                  ActivityAnswerRecordDO currentRecord) {
        if (!Objects.equals(answer.getState(), 1)) {
            disable(respVO, "活动未开启");
            return;
        }
        if (activity == null) {
            disable(respVO, ANSWER_ACTIVITY_NOT_FOUND.getMsg());
            return;
        }
        if (!isStoreMatched(activity, storeIds, storeId)) {
            disable(respVO, ANSWER_STORE_NOT_MATCH.getMsg());
            return;
        }
        if (!Objects.equals(sessionInfo.activityStatus, STATUS_RUNNING)) {
            disable(respVO, sessionInfo.statusText);
            return;
        }

        if (currentRecord != null && Objects.equals(currentRecord.getStatus(), 0)) {
            respVO.setUnfinishedRecordId(currentRecord.getId());
            respVO.setAnswerAction(ACTION_CONTINUE);
            respVO.setAnswerActionText("继续答题");
            return;
        }
        if (chanceCount > 0) {
            respVO.setAnswerAction(ACTION_START);
            respVO.setAnswerActionText("开始答题");
            return;
        }
        disable(respVO, "答题次数已用完");
    }

    /**
     * 禁用答题按钮。
     */
    private void disable(ActivityAnswerAppDetailRespVO respVO, String text) {
        respVO.setAnswerAction(ACTION_DISABLED);
        respVO.setAnswerActionText(text);
    }

    /**
     * 查询当前周期最新答题记录。
     */
    private ActivityAnswerRecordDO selectCurrentPeriodRecord(Long activityId, Long memberMobile, String periodKey) {
        if (memberMobile == null || !StringUtils.hasText(periodKey)) {
            return null;
        }
        return recordMapper.selectOne(new LambdaQueryWrapperX<ActivityAnswerRecordDO>()
                .eq(ActivityAnswerRecordDO::getActivityId, activityId)
                .eq(ActivityAnswerRecordDO::getMemberMobile, memberMobile)
                .eq(ActivityAnswerRecordDO::getPeriodKey, periodKey)
                .and(item -> item.isNull(ActivityAnswerRecordDO::getCancelStatus)
                        .or().eq(ActivityAnswerRecordDO::getCancelStatus, 0))
                .orderByDesc(ActivityAnswerRecordDO::getCreateTime)
                .last("LIMIT 1"));
    }

    /**
     * 短缓存当前场次记录，降低详情接口对答题记录分表的访问压力。
     */
    private ActivityAnswerRecordDO selectCurrentPeriodRecordCached(Long activityId, Long memberMobile,
                                                                   String periodKey) {
        if (memberMobile == null || !StringUtils.hasText(periodKey)) {
            return null;
        }
        String cacheKey = buildCurrentRecordCacheKey(activityId, memberMobile, periodKey);
        List<ActivityAnswerRecordDO> cachedRecords = redisCache.getCacheObjectOrDelete(cacheKey);
        if (cachedRecords != null) {
            return CollectionUtils.isEmpty(cachedRecords) ? null : cachedRecords.get(0);
        }
        ActivityAnswerRecordDO record = selectCurrentPeriodRecord(activityId, memberMobile, periodKey);
        List<ActivityAnswerRecordDO> cacheRecords = new ArrayList<>();
        if (record != null) {
            cacheRecords.add(record);
        }

        redisCache.setCacheObject(
                cacheKey,
                cacheRecords,
                10,
                TimeUnit.SECONDS
        );
        return record;
    }

    /**
     * 清除当前场次答题记录短缓存。
     */
    private void clearCurrentRecordCache(Long activityId, Long memberMobile, String periodKey) {
        if (activityId != null && memberMobile != null && StringUtils.hasText(periodKey)) {
            redisCache.deleteObject(buildCurrentRecordCacheKey(activityId, memberMobile, periodKey));
        }
    }

    /**
     * 构建当前场次答题记录缓存键。
     */
    private String buildCurrentRecordCacheKey(Long activityId, Long memberMobile, String periodKey) {
        return RedisKeyConstants.ANSWER_CURRENT_RECORD + activityId + ":" + memberMobile + ":" + periodKey;
    }

    /**
     * 填充当前场次答题记录和每题作答明细。
     */
    private void fillCurrentAnswerRecord(ActivityAnswerAppDetailRespVO respVO, ActivityAnswerRecordDO currentRecord) {
        if (currentRecord == null) {
            respVO.setAnsweredCount(0);
            respVO.setCorrectCount(0);
            respVO.setWrongCount(0);
            respVO.setAnswerDetails(Collections.emptyList());
            return;
        }
        respVO.setCurrentRecordId(currentRecord.getId());
        respVO.setAnswerStatus(currentRecord.getStatus());
        respVO.setCorrectCount(defaultZero(currentRecord.getCorrectCount()));
        respVO.setWrongCount(defaultZero(currentRecord.getWrongCount()));
        respVO.setAccuracy(currentRecord.getAccuracy());
        List<ActivityAnswerRecordDetailDO> recordQuestions = selectRecordQuestionDetailsCached(
                currentRecord.getId(), currentRecord.getMemberMobile());
        List<ActivityAnswerDetailRecordRespVO> details = buildCurrentAnswerDetails(recordQuestions);
        if (!CollectionUtils.isEmpty(recordQuestions)) {
            respVO.setQuestions(buildQuestionRespListFromDetails(recordQuestions));
            respVO.setQuestionCount(defaultZero(currentRecord.getQuestionCount()));
        }
        respVO.setAnswerDetails(details);
        respVO.setAnsweredCount((int) details.stream().filter(item -> Objects.equals(item.getIsAnswered(), 1)).count());
    }

    /**
     * 查询当前答题记录的作答明细。
     */
    private List<ActivityAnswerRecordDetailDO> selectRecordQuestionDetails(Long recordId, Long memberMobile) {
        if (recordId == null || memberMobile == null) {
            return Collections.emptyList();
        }
        return recordDetailMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerRecordDetailDO>()
                .eq(ActivityAnswerRecordDetailDO::getRecordId, recordId)
                .eq(ActivityAnswerRecordDetailDO::getMemberMobile, memberMobile)
                .orderByAsc(ActivityAnswerRecordDetailDO::getCreateTime)
                .orderByAsc(ActivityAnswerRecordDetailDO::getId));
    }

    /**
     * 短缓存当前记录题目明细，提交答案后立即失效。
     */
    private List<ActivityAnswerRecordDetailDO> selectRecordQuestionDetailsCached(Long recordId, Long memberMobile) {
        if (recordId == null || memberMobile == null) {
            return Collections.emptyList();
        }
        String cacheKey = buildRecordDetailCacheKey(recordId, memberMobile);
        List<ActivityAnswerRecordDetailDO> cachedDetails = redisCache.getCacheObjectOrDelete(cacheKey);
        if (cachedDetails != null) {
            return cachedDetails;
        }
        List<ActivityAnswerRecordDetailDO> details = selectRecordQuestionDetails(recordId, memberMobile);
        redisCache.setCacheObjectQuietly(cacheKey, details == null ? Collections.emptyList() : details,
                10, TimeUnit.SECONDS);
        return details == null ? Collections.emptyList() : details;
    }

    /**
     * 构建答题明细短缓存键。
     */
    private String buildRecordDetailCacheKey(Long recordId, Long memberMobile) {
        return RedisKeyConstants.ANSWER_RECORD_DETAIL + recordId + ":" + memberMobile;
    }

    /**
     * 缓存当前周期答题记录。
     */
    private void cacheCurrentRecord(ActivityAnswerContext context, ActivityAnswerRecordDO record) {
        String cacheKey = buildCurrentRecordCacheKey(context.answer.getActivityId(), context.memberMobile,
                context.sessionInfo.periodKey);
        List<ActivityAnswerRecordDO> cacheRecords = new ArrayList<>();
        if (record != null) {
            cacheRecords.add(record);
        }
        redisCache.setCacheObjectQuietly(cacheKey, cacheRecords, 10, TimeUnit.SECONDS);
    }

    /**
     * 答题事务提交后缓存新记录和题目明细。
     */
    private void registerJoinCacheAfterCommit(ActivityAnswerContext context, ActivityAnswerRecordDO record,
                                              List<ActivityAnswerRecordDetailDO> details) {
        Runnable cacheAction = () -> {
            cacheCurrentRecord(context, record);
            redisCache.setCacheObjectQuietly(buildRecordDetailCacheKey(record.getId(), context.memberMobile),
                    details == null ? Collections.emptyList() : details, 10, TimeUnit.SECONDS);
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            cacheAction.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                cacheAction.run();
            }
        });
    }

    /**
     * 将分布式锁延迟到事务完成后释放，防止下一请求读到未提交记录。
     */
    private boolean registerUnlockAfterTransaction(String lockKey, String lockValue) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return false;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                redisCache.unlock(lockKey, lockValue);
            }
        });
        return true;
    }

    /**
     * 构建小程序详情短缓存键。
     */
    private String buildAppDetailCacheKey(Long activityId, Long storeId, Long memberMobile) {
        if (activityId == null || storeId == null || memberMobile == null) {
            return null;
        }
        return RedisKeyConstants.ANSWER_APP_DETAIL + activityId + ":" + storeId + ":" + memberMobile;
    }

    /**
     * 缓存完整详情十秒，降低高并发重复组装和多次缓存访问。
     */
    private void cacheAppDetail(String cacheKey, ActivityAnswerAppDetailRespVO detail) {
        if (cacheKey != null && detail != null) {
            redisCache.setCacheObjectQuietly(cacheKey, detail, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * 用户状态发生变化后清理对应门店详情缓存。
     */
    private void clearAppDetailCache(Long activityId, Long storeId, Long memberMobile) {
        String cacheKey = buildAppDetailCacheKey(activityId, storeId, memberMobile);
        if (cacheKey != null) {
            redisCache.deleteObject(cacheKey);
        }
    }

    /**
     * 构建当前答题记录的作答明细返回。
     */
    private List<ActivityAnswerDetailRecordRespVO> buildCurrentAnswerDetails(List<ActivityAnswerRecordDetailDO> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyList();
        }
        return detailList.stream().map(detail -> {
            ActivityAnswerDetailRecordRespVO respVO = new ActivityAnswerDetailRecordRespVO();
            respVO.setQuestionId(detail.getQuestionId());
            respVO.setQuestionTitle(detail.getQuestionTitle());
            respVO.setIsAnswered(Objects.requireNonNullElse(detail.getIsAnswered(), 0));
            respVO.setSelectedAnswer(detail.getSelectedAnswer());
            respVO.setAnswerResult(detail.getAnswerResult());
            respVO.setIsCorrect(Objects.equals(detail.getAnswerResult(), 1));
            return respVO;
        }).toList();
    }

    /**
     * 判断门店是否可参与。
     */
    private boolean isStoreMatched(ActivityDO activity, Long storeId) {
        if (activity == null || storeId == null) {
            return false;
        }
        if (Objects.equals(activity.getActivityStore(), 1)) {
            return true;
        }
        ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activity.getId());
        if (cacheData != null && !CollectionUtils.isEmpty(cacheData.getStoreIds())) {
            return cacheData.getStoreIds().contains(storeId);
        }
        List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(activity.getId());
        return !CollectionUtils.isEmpty(storeIds) && storeIds.contains(storeId);
    }

    /**
     * 判断门店是否在已加载的活动门店范围内。
     */
    private boolean isStoreMatched(ActivityDO activity, List<Long> storeIds, Long storeId) {
        if (activity == null || storeId == null) {
            return false;
        }
        if (Objects.equals(activity.getActivityStore(), 1)) {
            return true;
        }
        if (!CollectionUtils.isEmpty(storeIds)) {
            return storeIds.contains(storeId);
        }
        List<Long> persistedStoreIds = activityStoreService.selectStoreIdsByActivityId(activity.getId());
        return !CollectionUtils.isEmpty(persistedStoreIds) && persistedStoreIds.contains(storeId);
    }

    /**
     * 完成一次普通任务。
     */
    private ActivityAnswerTaskCompleteRespVO completeTask(ActivityAnswerDO answer, Long storeId, Long memberId,
                                                          int taskType, String taskName, boolean enabled, int limitCount) {
        return completeTask(answer, storeId, memberId, getMemberMobile(memberId), taskType, taskName, enabled, limitCount);
    }

    /**
     * 复用已经读取的聚合缓存完成任务，避免再次反序列化活动缓存。
     */
    private ActivityAnswerTaskCompleteRespVO completeTask(ActivityAnswerDO answer, ActivityDO activity,
                                                           Long storeId, Long memberId, int taskType,
                                                           String taskName, boolean enabled, int limitCount) {
        return completeTask(answer, activity, storeId, memberId, getMemberMobile(memberId), taskType,
                taskName, enabled, limitCount, true);
    }

    /**
     * 完成一次任务并刷新可用答题次数。
     */
    private ActivityAnswerTaskCompleteRespVO completeTask(ActivityAnswerDO answer, Long storeId, Long memberId,
                                                           Long memberMobile, int taskType, String taskName,
                                                           boolean enabled, int limitCount) {
        return completeTask(answer, storeId, memberId, memberMobile, taskType, taskName, enabled, limitCount, true);
    }

    /**
     * 完成任务并按调用场景决定是否校验社群关系。
     */
    private ActivityAnswerTaskCompleteRespVO completeTask(ActivityAnswerDO answer, Long storeId, Long memberId,
                                                           Long memberMobile, int taskType, String taskName,
                                                           boolean enabled, int limitCount, boolean validateCommunity) {
        return completeTask(answer, null, storeId, memberId, memberMobile, taskType, taskName,
                enabled, limitCount, validateCommunity);
    }

    /**
     * 完成任务的核心流程。
     */
    private ActivityAnswerTaskCompleteRespVO completeTask(ActivityAnswerDO answer, ActivityDO activity,
                                                           Long storeId, Long memberId, Long memberMobile,
                                                           int taskType, String taskName, boolean enabled,
                                                           int limitCount, boolean validateCommunity) {
        TaskValidateResult validateResult = validateTask(answer, activity, storeId, memberId, memberMobile,
                enabled, limitCount, validateCommunity);

        String lockValue = UUID.randomUUID().toString();
        String lockKey = RedisKeyConstants.ANSWER_TASK_LOCK + answer.getActivityId() + ":" + validateResult.memberMobile
                + ":" + validateResult.sessionInfo.chancePeriodKey;
        boolean locked = redisCache.lock(lockKey, lockValue, 10);
        if (!locked) {
            throw exception(ANSWER_SYSTEM_BUSY);
        }
        try {
            int chanceBefore = calculateChanceCount(answer, validateResult.memberMobile,
                    validateResult.sessionInfo, true);
            ActivityAnswerTaskDO taskDO = loadTaskRecordMap(answer.getActivityId(), validateResult.memberMobile,
                    validateResult.sessionInfo.chancePeriodKey).get(taskType);
            int finishCount = defaultZero(taskDO == null ? null : taskDO.getFinishCount());
            if (finishCount >= limitCount) {
                throw exception(ANSWER_TASK_LIMIT_REACHED);
            }
            if (taskDO == null) {
                try {
                    taskMapper.insert(buildInitTask(answer.getActivityId(), memberId, validateResult.memberMobile,
                            taskType, validateResult.sessionInfo.chancePeriodKey));
                } catch (DuplicateKeyException e) {
                    taskDO = getTaskRecord(answer.getActivityId(), validateResult.memberMobile,
                            taskType, validateResult.sessionInfo.chancePeriodKey);
                    if (taskDO == null || taskMapper.increaseTaskCount(
                            taskDO.getId(), taskDO.getMemberMobile(), limitCount) == 0) {
                        throw exception(ANSWER_TASK_LIMIT_REACHED);
                    }
                }
            } else {
                if (taskMapper.increaseTaskCount(taskDO.getId(), taskDO.getMemberMobile(), limitCount) == 0) {
                    throw exception(ANSWER_TASK_LIMIT_REACHED);
                }
            }
            redisCache.deleteObject(buildTaskCacheKey(answer.getActivityId(), validateResult.memberMobile,
                    validateResult.sessionInfo.chancePeriodKey));
            int chanceCount = increaseChanceAfterTask(answer, validateResult.memberMobile,
                    validateResult.sessionInfo, chanceBefore);
            int addedChance = Math.max(chanceCount - chanceBefore, 0);
            String message = addedChance > 0 ? taskName + "任务完成，答题次数+" + addedChance
                    : taskName + "任务完成，已达到活动答题次数上限";
            clearAppDetailCache(answer.getActivityId(), storeId, validateResult.memberMobile);
            return taskResult(true, addedChance, chanceCount, message);
        } finally {
            try {
                redisCache.unlock(lockKey, lockValue);
            } catch (Exception ignored) {
                // 锁自然过期时不影响任务完成结果。
            }
        }
    }

    /**
     * 校验任务是否可完成。
     */
    private TaskValidateResult validateTask(ActivityAnswerDO answer, Long storeId, Long memberId,
                                            boolean enabled, int limitCount) {
        return validateTask(answer, storeId, memberId, getMemberMobile(memberId), enabled, limitCount);
    }

    /**
     * 校验任务是否可完成。
     */
    private TaskValidateResult validateTask(ActivityAnswerDO answer, Long storeId, Long memberId, Long memberMobile,
                                             boolean enabled, int limitCount) {
        return validateTask(answer, storeId, memberId, memberMobile, enabled, limitCount, true);
    }

    /**
     * 校验任务，并允许订单链路复用前置校验结果。
     */
    private TaskValidateResult validateTask(ActivityAnswerDO answer, Long storeId, Long memberId, Long memberMobile,
                                             boolean enabled, int limitCount, boolean validateCommunity) {
        return validateTask(answer, null, storeId, memberId, memberMobile, enabled, limitCount, validateCommunity);
    }

    /**
     * 复用已加载的活动主表数据校验任务。
     */
    private TaskValidateResult validateTask(ActivityAnswerDO answer, ActivityDO loadedActivity, Long storeId,
                                             Long memberId, Long memberMobile, boolean enabled,
                                             int limitCount, boolean validateCommunity) {
        TaskValidateResult result = new TaskValidateResult();
        result.memberMobile = memberMobile;
        if (answer == null || answer.getActivityId() == null) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND);
        }
        ActivityDO activity = loadedActivity == null ? loadActivity(answer.getActivityId()) : loadedActivity;
        mergeActivitySnapshot(answer, activity);
        if (validateCommunity) {
            validateCommunityIfNecessary(activity);
        }
        result.sessionInfo = buildCurrentSessionInfo(activity, answer.getCalculationRules());
        if (memberId == null || memberMobile == null) {
            throw exception(ANSWER_MEMBER_MOBILE_REQUIRED);
        }
        if (!enabled) {
            throw exception(ANSWER_TASK_NOT_ENABLED);
        }
        if (limitCount <= 0) {
            throw exception(ANSWER_TASK_COUNT_NOT_CONFIGURED);
        }
        if (!Objects.equals(answer.getState(), 1) || !Objects.equals(result.sessionInfo.activityStatus, STATUS_RUNNING)) {
            throw exception(ANSWER_ACTIVITY_NOT_FOUND.getCode(), result.sessionInfo.statusText);
        }
        if (!isStoreMatched(activity, storeId)) {
            throw exception(ANSWER_STORE_NOT_MATCH);
        }
        return result;
    }

    /**
     * 构建初始化任务记录。
     */
    private ActivityAnswerTaskDO buildInitTask(Long activityId, Long memberId, Long memberMobile,
                                               int taskType, String periodKey) {
        ActivityAnswerTaskDO taskDO = new ActivityAnswerTaskDO();
        taskDO.setActivityId(activityId);
        taskDO.setMemberId(memberId);
        taskDO.setMemberMobile(memberMobile);
        taskDO.setTaskType(taskType);
        taskDO.setPeriodKey(periodKey);
        taskDO.setTaskDate(LocalDate.now());
        taskDO.setFinishCount(1);
        taskDO.setGainCount(TASK_REWARD_COUNT);
        taskDO.setConsumeCount(0);
        return taskDO;
    }

    /**
     * 更新任务消耗次数，避免更新分片键 member_mobile。
     */
    private void updateTaskConsumeCount(ActivityAnswerTaskDO taskDO, int consumeCount) {
        taskMapper.update(null, new LambdaUpdateWrapper<ActivityAnswerTaskDO>()
                .eq(ActivityAnswerTaskDO::getId, taskDO.getId())
                .eq(ActivityAnswerTaskDO::getMemberMobile, taskDO.getMemberMobile())
                .set(ActivityAnswerTaskDO::getConsumeCount, consumeCount));
    }

    /**
     * 查询当前周期任务记录。
     */
    private ActivityAnswerTaskDO getTaskRecord(Long activityId, Long memberMobile, int taskType, String periodKey) {
        if (memberMobile == null || !StringUtils.hasText(periodKey)) {
            return null;
        }
        return taskMapper.selectOne(new LambdaQueryWrapperX<ActivityAnswerTaskDO>()
                .eq(ActivityAnswerTaskDO::getActivityId, activityId)
                .eq(ActivityAnswerTaskDO::getMemberMobile, memberMobile)
                .eq(ActivityAnswerTaskDO::getTaskType, taskType)
                .eq(ActivityAnswerTaskDO::getPeriodKey, periodKey)
                .last("LIMIT 1"));
    }

    /**
     * 刷新可用答题次数缓存。
     */
    private int refreshChanceCount(ActivityAnswerDO answer, Long memberMobile, SessionInfo sessionInfo) {
        String cacheKey = RedisKeyConstants.ANSWER_NUM + answer.getActivityId() + ":" + memberMobile + ":" + sessionInfo.chancePeriodKey;
        int chanceCount = calculateChanceCount(answer, memberMobile, sessionInfo, false);
        redisCache.setCacheObjectQuietly(cacheKey, chanceCount, sessionInfo.expireSeconds, TimeUnit.SECONDS);
        return chanceCount;
    }

    /**
     * 开始新一轮答题后直接递减已校验的次数缓存，避免在锁内重复聚合任务和答题记录。
     */
    private int decreaseChanceAfterJoin(ActivityAnswerDO answer, Long memberMobile,
                                        SessionInfo sessionInfo, int chanceBefore) {
        int chanceCount = Math.max(chanceBefore - 1, 0);
        String cacheKey = RedisKeyConstants.ANSWER_NUM + answer.getActivityId() + ":" + memberMobile
                + ":" + sessionInfo.chancePeriodKey;
        redisCache.setCacheObjectQuietly(cacheKey, chanceCount, sessionInfo.expireSeconds, TimeUnit.SECONDS);
        return chanceCount;
    }

    /**
     * 完成任务后直接递增可用次数，并按活动总次数上限封顶。
     */
    private int increaseChanceAfterTask(ActivityAnswerDO answer, Long memberMobile,
                                        SessionInfo sessionInfo, int chanceBefore) {
        int chanceCount = chanceBefore + TASK_REWARD_COUNT;
        int totalLimit = defaultZero(answer.getAnswerTotalNumber());
        if (totalLimit > 0) {
            int totalRemaining = Math.max(totalLimit
                    - countActivityUsedRecords(answer.getActivityId(), memberMobile), 0);
            chanceCount = Math.min(chanceCount, totalRemaining);
        }
        String cacheKey = RedisKeyConstants.ANSWER_NUM + answer.getActivityId() + ":" + memberMobile
                + ":" + sessionInfo.chancePeriodKey;
        redisCache.setCacheObjectQuietly(cacheKey, chanceCount, sessionInfo.expireSeconds, TimeUnit.SECONDS);
        return chanceCount;
    }

    /**
     * 构建任务完成返回。
     */
    private ActivityAnswerTaskCompleteRespVO taskResult(boolean success, int addChance, int chanceCount, String message) {
        ActivityAnswerTaskCompleteRespVO respVO = new ActivityAnswerTaskCompleteRespVO();
        respVO.setSuccess(success);
        respVO.setAddChance(addChance);
        respVO.setChanceCount(Math.max(chanceCount, 0));
        respVO.setMessage(message);
        return respVO;
    }

    /**
     * 批量构建活动门店映射。
     */
    private Map<Long, List<Long>> buildActivityStoreMap(List<ActivityDO> activityList) {
        List<Long> limitedStoreActivityIds = activityList.stream()
                .filter(activity -> Objects.equals(activity.getActivityStore(), 0))
                .map(ActivityDO::getId)
                .toList();
        if (CollectionUtils.isEmpty(limitedStoreActivityIds)) {
            return Collections.emptyMap();
        }
        return activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>()
                        .in(ActivityStoreDO::getActivityId, limitedStoreActivityIds))
                .stream()
                .collect(Collectors.groupingBy(ActivityStoreDO::getActivityId,
                        Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())));
    }

    /**
     * 批量构建有奖问答下单商品范围映射。
     */
    private Map<Long, List<Long>> buildAnswerCommodityMap(Set<Long> activityIds) {
        if (CollectionUtils.isEmpty(activityIds)) {
            return Collections.emptyMap();
        }
        Map<Long, List<Long>> result = new java.util.HashMap<>();
        Set<Long> missActivityIds = new java.util.HashSet<>();
        for (Long activityId : activityIds) {
            ActivityAnswerSettingsCacheDataVO cacheData = loadAnswerCache(activityId);
            if (cacheData != null) {
                result.put(activityId, cacheData.getCommodityIds() == null ? Collections.emptyList() : cacheData.getCommodityIds());
            } else {
                missActivityIds.add(activityId);
            }
        }
        if (CollectionUtils.isEmpty(missActivityIds)) {
            return result;
        }
        result.putAll(commodityMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerCommodityDO>()
                        .in(ActivityAnswerCommodityDO::getActivityId, missActivityIds))
                .stream()
                .collect(Collectors.groupingBy(ActivityAnswerCommodityDO::getActivityId,
                        Collectors.mapping(ActivityAnswerCommodityDO::getCommodityId, Collectors.toList()))));
        return result;
    }

    /**
     * 判断订单是否满足下单任务条件。
     */
    private boolean isOrderMatched(ActivityAnswerDO answer, List<Long> configuredCommodityIds, ActivityJkOrderReqDTO reqDTO) {
        if (answer == null || reqDTO == null) {
            return false;
        }
        boolean categoryRestricted = Objects.equals(answer.getPlaceOrderType(), 1);
        if (categoryRestricted && !isCategoryMatched(answer.getCategoryType(), reqDTO.getOrderProductType())) {
            return false;
        }
        if (!categoryRestricted && Objects.equals(answer.getPlaceOrderProduct(), 2)) {
            if (CollectionUtils.isEmpty(configuredCommodityIds) || CollectionUtils.isEmpty(reqDTO.getCommodityIds())) {
                return false;
            }
            boolean matched = reqDTO.getCommodityIds().stream().anyMatch(configuredCommodityIds::contains);
            if (!matched) {
                return false;
            }
        }
        if (Objects.equals(answer.getPaymentType(), 1)) {
            BigDecimal paymentAmount = reqDTO.getPaymentAmount();
            BigDecimal thresholdAmount = answer.getPaymentCount();
            return paymentAmount != null && thresholdAmount != null && paymentAmount.compareTo(thresholdAmount) >= 0;
        }
        return true;
    }

    /**
     * 判断订单品类是否满足配置。
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
     * 构建当前场次信息。
     */
    private SessionInfo buildCurrentSessionInfo(ActivityDO activity, Integer calculationRules) {
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.periodKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        sessionInfo.chancePeriodKey = sessionInfo.periodKey;
        sessionInfo.expireSeconds = secondsUntilEndOfDay();
        if (activity == null) {
            sessionInfo.activityStatus = STATUS_ENDED;
            sessionInfo.statusText = ANSWER_ACTIVITY_NOT_FOUND.getMsg();
            return sessionInfo;
        }
        sessionInfo.periodKey = buildPeriodKey(activity, calculationRules);
        sessionInfo.chancePeriodKey = Objects.equals(calculationRules, 2) ? sessionInfo.periodKey
                : LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        if (!Objects.equals(activity.getIsEnabled(), 1)) {
            sessionInfo.activityStatus = STATUS_NOT_STARTED;
            sessionInfo.statusText = "活动未开启";
            return sessionInfo;
        }
        if (isBeforeStart(activity.getStartDate())) {
            sessionInfo.activityStatus = STATUS_NOT_STARTED;
            sessionInfo.statusText = "活动未开始";
            return sessionInfo;
        }
        if (isAfterEnd(activity.getEndDate())) {
            sessionInfo.activityStatus = STATUS_ENDED;
            sessionInfo.statusText = "活动已结束";
            return sessionInfo;
        }
        boolean valid = TimeValidationUtil.isTimeValid(activity.getStartDate(), activity.getEndDate(),
                activity.getDayNumbers(), activity.getWeekNumbers(), activity.getTimeRange());
        if (!valid) {
            sessionInfo.activityStatus = STATUS_NOT_IN_SESSION;
            sessionInfo.statusText = "不在当前场次";
            return sessionInfo;
        }
        SessionRange currentRange = findCurrentSessionRange(activity.getTimeRange());
        if (currentRange != null) {
            sessionInfo.periodKey = currentRange.periodKey;
            if (Objects.equals(calculationRules, 2)) {
                sessionInfo.chancePeriodKey = currentRange.periodKey;
                sessionInfo.expireSeconds = secondsUntil(currentRange.endTime);
            }
        }
        sessionInfo.activityStatus = STATUS_RUNNING;
        sessionInfo.statusText = "活动进行中";
        return sessionInfo;
    }

    /**
     * 根据次数计算规则生成周期 key。
     */
    private String buildPeriodKey(ActivityDO activity, Integer calculationRules) {
        if (Objects.equals(calculationRules, 2)) {
            SessionRange currentRange = findCurrentSessionRange(activity.getTimeRange());
            if (currentRange != null) {
                return currentRange.periodKey;
            }
        }
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    /**
     * 查找当前时间所属场次。
     */
    private SessionRange findCurrentSessionRange(String timeRange) {
        if (!StringUtils.hasText(timeRange)) {
            SessionRange range = new SessionRange();
            range.periodKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_ALLDAY";
            range.endTime = LocalTime.of(23, 59, 59);
            return range;
        }
        LocalTime now = LocalTime.now();
        for (String item : timeRange.split(",")) {
            String normalized = normalizeTimeRange(item);
            String[] parts = normalized.split("-");
            if (parts.length != 2) {
                continue;
            }
            LocalTime start = parseTime(parts[0]);
            LocalTime end = parseTime(parts[1]);
            if (start == null || end == null) {
                continue;
            }
            if (!now.isBefore(start) && !now.isAfter(end)) {
                SessionRange range = new SessionRange();
                range.periodKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                        + "_" + start.format(DateTimeFormatter.ofPattern("HHmm"))
                        + end.format(DateTimeFormatter.ofPattern("HHmm"));
                range.endTime = end;
                return range;
            }
        }
        return null;
    }

    /**
     * 统一时间段格式。
     */
    private String normalizeTimeRange(String timeRange) {
        String cleaned = timeRange == null ? "" : timeRange.replaceAll("\\s", "");
        if (cleaned.matches("\\d{1,2}-\\d{1,2}")) {
            String[] parts = cleaned.split("-");
            return parts[0] + ":00-" + parts[1] + ":00";
        }
        if (cleaned.matches("\\d{1,2}:\\d{2}-\\d{1,2}")) {
            String[] parts = cleaned.split("-");
            return parts[0] + "-" + parts[1] + ":00";
        }
        if (cleaned.matches("\\d{1,2}-\\d{1,2}:\\d{2}")) {
            String[] parts = cleaned.split("-");
            return parts[0] + ":00-" + parts[1];
        }
        return cleaned;
    }

    /**
     * 解析时间字符串。
     */
    private LocalTime parseTime(String value) {
        try {
            if ("24".equals(value) || "24:00".equals(value)) {
                return LocalTime.of(23, 59, 59);
            }
            if (value.length() <= 2) {
                return LocalTime.of(Integer.parseInt(value), 0);
            }
            return LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断是否早于活动开始日期。
     */
    private boolean isBeforeStart(Date startDate) {
        return startDate != null && LocalDate.now().isBefore(toLocalDate(startDate));
    }

    /**
     * 判断是否晚于活动结束日期。
     */
    private boolean isAfterEnd(Date endDate) {
        return endDate != null && LocalDate.now().isAfter(toLocalDate(endDate));
    }

    /**
     * Date 转 LocalDate。
     */
    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 计算活动缓存过期秒数。
     */
    private int calculateActivityExpireSeconds(ActivityDO activity) {
        if (activity == null || activity.getEndDate() == null) {
            return secondsUntilEndOfDay();
        }
        LocalDate endDate = toLocalDate(activity.getEndDate());
        LocalDateTime expireTime = endDate.atTime(23, 59, 59);
        long seconds = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        return (int) Math.max(seconds, 60);
    }

    /**
     * 计算到当天结束的秒数。
     */
    private int secondsUntilEndOfDay() {
        long seconds = Duration.between(LocalDateTime.now(), LocalDate.now().atTime(23, 59, 59)).getSeconds();
        return (int) Math.max(seconds, 60);
    }

    /**
     * 计算到指定时间的秒数。
     */
    private int secondsUntil(LocalTime endTime) {
        long seconds = Duration.between(LocalDateTime.now(), LocalDate.now().atTime(endTime)).getSeconds();
        return (int) Math.max(seconds, 60);
    }

    /**
     * 获取会员手机号。
     */
    private Long getMemberMobile(Long memberId) {
        if (memberId == null) {
            return null;
        }
        if (Objects.equals(memberId, SecurityFrameworkUtils.getLoginUserId())) {
            Long loginMobile = parseMemberMobile(SecurityFrameworkUtils.getLoginMobile());
            if (loginMobile != null) {
                return loginMobile;
            }
        }
        WxMemberVO member = getWxMember(memberId);
        return parseMemberMobile(member == null ? null : member.getMemberMobile());
    }

    /**
     * 将会员手机号转换为数字，适配 member_mobile 数字分片字段。
     */
    private Long parseMemberMobile(String memberMobile) {
        if (!StringUtils.hasText(memberMobile)) {
            return null;
        }
        try {
            return Long.valueOf(memberMobile.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 空数字转 0。
     */
    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 任务校验结果。
     */
    private static class TaskValidateResult {
        private Long memberMobile;
        private SessionInfo sessionInfo;
    }

    /**
     * 答题上下文。
     */
    private static class ActivityAnswerContext {
        private ActivityAnswerDO answer;
        private ActivityDO activity;
        private Long memberId;
        private WxMemberVO member;
        private Long memberMobile;
        private Long storeId;
        private SessionInfo sessionInfo;
        private List<ActivityAnswerQuestionDO> questions;
        private Map<Long, ActivityAnswerQuestionDO> questionMap;
    }

    /**
     * 当前场次信息。
     */
    private static class SessionInfo {
        private Integer activityStatus;
        private String statusText;
        private String periodKey;
        private String chancePeriodKey;
        private int expireSeconds;
    }

    /**
     * 场次时间范围。
     */
    private static class SessionRange {
        private String periodKey;
        private LocalTime endTime;
    }
}
