package com.htyoudao.youdao.module.promotion.service.activityJkApp;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo.ActivityJkCardReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo.ActivityJkPrizeReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo.ActivityJkSaveOrUpdateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo.ActivityCollectAppShareVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkDetailVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkPrizeListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ActivityJkReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.AddressSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.DrawReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.DrawRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.DrawResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.ExchangeRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.MyCardListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.PrizeExchangeReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.PrizeExchangeResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.PrizeVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.TaskVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.UniversalExchangeReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo.UserCardVO;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkCardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityCardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkHelpDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeExchangeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkTaskDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkCard.ActivityJkCardMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityCardLog.ActivityCardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrize.ActivityJkPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrizeExchange.ActivityJkPrizeExchangeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCq.ActivityCqMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJk.ActivityJkMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJkHelp.ActivityJkHelpMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityJkTask.ActivityJkTaskMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.JKKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.ActivityJkTaskTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.DateTimeRangeUtil;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.Comparator;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Slf4j
@RefreshScope
@DS(DsNameConstants.SHARDING)
public class ActivityJkAppServiceImpl extends ServiceImpl<ActivityJkMapper, ActivityJkDO> implements ActivityJkAppService {


    /**
     * 集卡活动详情缓存锁前缀。
     * 用于缓存未命中时串行化回源，避免同一活动在高并发下重复查库。
     */
    private static final String JK_DETAIL_LOCK_PREFIX = "lock:jk:detail:";

    /**
     * 集卡卡片、奖品列表缓存锁前缀。
     */
    private static final String JK_LIST_LOCK_PREFIX = "lock:jk:list:";

    /**
     * 助力分享锁前缀。
     * 用于串行化同一邀请人和被邀请人的助力操作，避免并发下重复写入。
     */
    private static final String JK_HELP_LOCK_PREFIX = "lock:jk:help:";

    /**
     * 抽卡锁前缀。
     * 用于串行化同一用户在同一活动下的抽卡请求，避免并发下重复扣减次数。
     */
    private static final String JK_DRAW_LOCK_PREFIX = "lock:jk:draw:";

    /**
     * 卡片缓存占用锁前缀。
     * 用于在高并发下基于缓存原子占用卡片库存，避免超量发卡。
     */
    private static final String JK_CARD_CACHE_LOCK_PREFIX = "lock:jk:card:cache:";

    /**
     * 万能卡兑换锁前缀。
     * 用于避免同一张万能卡记录在并发下被重复兑换。
     */
    private static final String JK_UNIVERSAL_EXCHANGE_LOCK_PREFIX = "lock:jk:universal:exchange:";

    /**
     * 奖品兑换锁前缀。
     * 用于避免同一用户并发重复消耗同一批卡片去兑奖。
     */
    private static final String JK_PRIZE_EXCHANGE_LOCK_PREFIX = "lock:jk:prize:exchange:";

    /**
     * 任务次数为 0 时表示不限次数。
     */
    private static final int TASK_LIMIT_UNLIMITED = 0;

    /**
     * 活动配置缓存时长。
     */
    private static final int JK_SETTING_CACHE_SECONDS = 300;

    /**
     * 列表类缓存时长，例如卡片列表、奖品列表。
     */
    private static final int JK_LIST_CACHE_SECONDS = 300;

    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityCqMapper activityCqMapper;
    @Resource
    private ActivityAnswerMapper activityAnswerMapper;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivityJkCardMapper activityJkCardMapper;

    @Resource
    private ActivityJkPrizeMapper activityJkPrizeMapper;

    @Resource
    private ActivityJkPrizeExchangeMapper activityJkPrizeExchangeMapper;

    @Resource
    private ActivityCardLogMapper activityCardLogMapper;

    @Resource
    private ActivityJkTaskMapper activityJkTaskMapper;

    @Resource
    private ActivityJkHelpMapper activityJkHelpMapper;

    @Resource
    private WxMemberApi wxMemberApi;

    @Resource
    private LotteryRedPacketService lotteryRedPacketService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedisCache redisCache;

    @Resource
    private RedissonClient redissonClient;

    @Qualifier("promotionThreadPool")
    @Resource
    private Executor promotionThreadPool;
    @Resource
    private LotterySettingsMapper lotterySettingsMapper;
    @Resource
    private ActivityAppService activityAppService;
    @Resource
    private LotteryAddLogService lotteryAddLogService;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private UserCouponMapper userCouponMapper;
    @Override

    /**
     * 查询可参与的集卡活动列表。
     */
    public List<ActivityJkListVO> getActivityJkList(Long storeId) {
        if (storeId == null) {
            return Collections.emptyList();
        }

        List<ActivityJkDO> activityJkList = this.list(new LambdaQueryWrapperX<ActivityJkDO>()
                .eq(ActivityJkDO::getPublicButton, 0));
        List<Long> jkActivityIds = activityJkList.stream()
                .map(ActivityJkDO::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, ActivityJkDO> activityJkMap = activityJkList.stream()
                .filter(item -> item.getActivityId() != null)
                .collect(Collectors.toMap(ActivityJkDO::getActivityId, item -> item, (left, right) -> left));

        List<ActivityCqDO> activityCqList = activityCqMapper.selectList(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getPublicButton, 0));
        List<Long> cqActivityIds = activityCqList.stream()
                .map(ActivityCqDO::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, ActivityCqDO> activityCqMap = activityCqList.stream()
                .filter(item -> item.getActivityId() != null)
                .filter(item -> DateTimeRangeUtil.isNowInRange(item.getStartDateTime(), item.getEndDateTime()))
                .collect(Collectors.toMap(ActivityCqDO::getActivityId, item -> item, (left, right) -> left));
        List<LotterySettingsDO> lotterySettingsList = lotterySettingsMapper.selectList(new LambdaQueryWrapperX<LotterySettingsDO>()
                .eq(LotterySettingsDO::getState, 1)
                .eq(LotterySettingsDO::getPublicButton, 0)
                .isNotNull(LotterySettingsDO::getActivityId)
                .le(LotterySettingsDO::getLotteryStartTime, LocalDate.now())
                .ge(LotterySettingsDO::getLotteryEndTime, LocalDate.now()));
        List<Long> lotteryActivityIds = lotterySettingsList.stream()
                .map(LotterySettingsDO::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<ActivityAnswerDO> activityAnswerList = activityAnswerMapper.selectList(new LambdaQueryWrapperX<ActivityAnswerDO>()
                .eq(ActivityAnswerDO::getPublicButton, 0)
                .isNotNull(ActivityAnswerDO::getActivityId));
        List<Long> answerActivityIds = activityAnswerList.stream()
                .map(ActivityAnswerDO::getActivityId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Set<Long> queryActivityIdSet = new HashSet<>();
        queryActivityIdSet.addAll(jkActivityIds);
        queryActivityIdSet.addAll(cqActivityIds);
        queryActivityIdSet.addAll(lotteryActivityIds);
        queryActivityIdSet.addAll(answerActivityIds);
        if (queryActivityIdSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> queryActivityIds = new ArrayList<>(queryActivityIdSet);
        List<ActivityDO> activityList = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                .in(ActivityDO::getId, queryActivityIds));
        if (activityList.isEmpty() && activityAnswerList.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ActivityDO> activityMap = activityList.stream()
                .collect(Collectors.toMap(ActivityDO::getId, item -> item, (left, right) -> left));

        List<Long> specifiedStoreActivityIds = activityList.stream()
                .filter(activity -> !Objects.equals(activity.getActivityStore(), 1))
                .map(ActivityDO::getId)
                .collect(Collectors.toList());
        activityAnswerList.stream()
                .filter(answer -> !Objects.equals(answer.getActivityStore(), 1))
                .map(ActivityAnswerDO::getActivityId)
                .filter(Objects::nonNull)
                .forEach(specifiedStoreActivityIds::add);
        specifiedStoreActivityIds = specifiedStoreActivityIds.stream().distinct().collect(Collectors.toList());

        Map<Long, List<Long>> activityStoreMap = specifiedStoreActivityIds.isEmpty()
                ? Collections.emptyMap()
                : activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>()
                        .in(ActivityStoreDO::getActivityId, specifiedStoreActivityIds))
                .stream()
                .collect(Collectors.groupingBy(
                        ActivityStoreDO::getActivityId,
                        Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())
                ));

        List<ActivityJkListVO> result = new ArrayList<>();
        activityList.stream()
                .filter(activity -> Objects.equals(activity.getIsEnabled(), 1))
                .filter(activity -> Objects.equals(activity.getActivityType(), ActivityTypeEnum.JK.getCode()))
                .filter(activity -> activityJkMap.containsKey(activity.getId()))
                .filter(activity -> isActivityStoreMatched(activity, activityStoreMap, storeId))
                .filter(activity -> TimeValidationUtil.isTimeValid(
                        activity.getStartDate(),
                        activity.getEndDate(),
                        activity.getDayNumbers(),
                        activity.getWeekNumbers(),
                        null
                ))
                .map(activity -> buildJkListItem(activity, activityJkMap.get(activity.getId())))
                .forEach(result::add);

        activityList.stream()
                .filter(activity -> Objects.equals(activity.getIsEnabled(), 1))
                .filter(activity -> Objects.equals(activity.getActivityType(), ActivityTypeEnum.CQ.getCode()))
                .filter(activity -> activityCqMap.containsKey(activity.getId()))
                .filter(activity -> isActivityStoreMatched(activity, activityStoreMap, storeId))
                .map(activity -> buildCqListItem(activity, activityCqMap.get(activity.getId())))
                .filter(Objects::nonNull)
                .forEach(result::add);

        lotterySettingsList.stream()
                .filter(settings -> settings.getActivityId() != null)
                .map(settings -> buildLotteryListItem(settings, activityMap.get(settings.getActivityId()), activityStoreMap, storeId))
                .filter(Objects::nonNull)
                .forEach(result::add);

        activityAnswerList.stream()
                .map(answer -> buildAnswerListItem(answer, activityMap.get(answer.getActivityId()), activityStoreMap, storeId))
                .filter(Objects::nonNull)
                .forEach(result::add);
        return result;
    }

    /**
     * 构建集卡活动列表项。
     */
    private ActivityJkListVO buildJkListItem(ActivityDO activity, ActivityJkDO activityJk) {
        ActivityJkListVO vo = new ActivityJkListVO();
        vo.setActivityId(activity.getId());
        vo.setActivityTitle(activity.getActivityName());
        vo.setActivityCoverImage(activityJk.getActivityCoverImage());
        vo.setActivityType(5L);
        return vo;
    }

    /**
     * 构建抽奖活动列表项。
     */
    private ActivityJkListVO buildLotteryListItem(LotterySettingsDO settings, ActivityDO activity,
                                                  Map<Long, List<Long>> activityStoreMap, Long storeId) {
        if (activity == null || !Objects.equals(activity.getIsEnabled(), 1)
                || !Objects.equals(activity.getActivityType(), ActivityTypeEnum.LOTTERY.getCode())) {
            return null;
        }
        if (!isActivityStoreMatched(activity, activityStoreMap, storeId)) {
            return null;
        }
//        if (!activityAppService.checkCanJoin(activity.getId())) {
//            return null;
//        }
        if (!TimeValidationUtil.isTimeValid(
                activity.getStartDate(),
                activity.getEndDate(),
                activity.getDayNumbers(),
                activity.getWeekNumbers(),
                activity.getTimeRange()
        )) {
            return null;
        }

        ActivityJkListVO vo = new ActivityJkListVO();
        vo.setActivityId(settings.getId());
        vo.setActivityTitle(settings.getShareTitle());
        vo.setActivityCoverImage(settings.getActivityImgUrl());
        vo.setActivityType(settings.getLotteryType() == null ? null : settings.getLotteryType().longValue());
        return vo;
    }

    /**
     * 构建抽签活动列表项。
     */
    private ActivityJkListVO buildCqListItem(ActivityDO activity, ActivityCqDO activityCq) {
        if (activity == null || activityCq == null) {
            return null;
        }
        ActivityJkListVO vo = new ActivityJkListVO();
        vo.setActivityId(activity.getId());
        vo.setActivityTitle(activity.getActivityName());
        vo.setActivityCoverImage(activityCq.getActivityCoverImage());
        vo.setActivityType((long) ActivityTypeEnum.CQ.getCode());
        return vo;
    }

    /**
     * 校验活动是否满足门店参与条件。
     */
    private ActivityJkListVO buildAnswerListItem(ActivityAnswerDO answer, ActivityDO activity,
                                                 Map<Long, List<Long>> activityStoreMap, Long storeId) {
        if (answer == null || answer.getActivityId() == null || activity == null) {
            return null;
        }
        if (!Objects.equals(activity.getIsEnabled(), 1)
                || !Objects.equals(activity.getActivityType(), ActivityTypeEnum.ANSWER.getCode())) {
            return null;
        }
        if (!isActivityStoreMatched(activity, activityStoreMap, storeId)) {
            return null;
        }
//        if (!isCommunityMatched(activity)) {
//            return null;
//        }
        if (!TimeValidationUtil.isTimeValid(
                activity.getStartDate(),
                activity.getEndDate(),
                activity.getDayNumbers(),
                activity.getWeekNumbers(),
                activity.getTimeRange()
        )) {
            return null;
        }

        ActivityJkListVO vo = new ActivityJkListVO();
        vo.setActivityId(answer.getActivityId());
        vo.setActivityTitle(activity.getActivityName());
        vo.setActivityCoverImage(answer.getActivityImgUrl());
        vo.setActivityType((long) ActivityTypeEnum.ANSWER.getCode());
        return vo;
    }

    /**
     * 校验社群专享活动的参与资格，校验失败时仅过滤当前活动。
     */
    private boolean isCommunityMatched(ActivityDO activity) {
        if (activity == null || !Objects.equals(activity.getCommunityFlag(), 2)) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(activityAppService.checkCanJoin(activity.getId()));
        } catch (ServiceException exception) {
            return false;
        }
    }

    private boolean isActivityStoreMatched(ActivityDO activity, Map<Long, List<Long>> activityStoreMap, Long storeId) {
        return isActivityStoreMatched(activity.getActivityStore(), activity.getId(), activityStoreMap, storeId);
    }

    /**
     * 校验活动是否满足门店参与条件。
     */
    private boolean isActivityStoreMatched(Integer activityStore, Long activityId,
                                           Map<Long, List<Long>> activityStoreMap, Long storeId) {
        if (Objects.equals(activityStore, 1)) {
            return true;
        }
        List<Long> relatedStores = activityStoreMap.getOrDefault(activityId, Collections.emptyList());
        return relatedStores.contains(storeId);
    }

    @Override

    /**
     * 查询集卡活动详情。
     */
    public ActivityJkDetailVO getActivityJkDetail(Long activityId, Long memberId) {
        if (activityId == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        // 详情优先走缓存，缓存未命中时再加锁回源主表和子表。
        ActivityJkSaveOrUpdateReqVO cacheData = getValidatedActivity(activityId, null, false, false);

        ActivityJkDetailVO detailVO = convertReqCacheToDetail(cacheData, activityId);
        detailVO.setActivityStatus(calculateActivityStatus(cacheData.getIsEnabled(),cacheData.getStartDate(), cacheData.getEndDate()));
        return detailVO;
    }

    @Override

    /**
     * 校验卡片库存状态。
     */
    public Boolean checkCardStock(Long activityId, Long cardId) {
        return true;
    }

    @Override

    /**
     * 执行用户抽卡流程。
     */
    public DrawResultVO drawCard(DrawReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        ActivityJkSaveOrUpdateReqVO cacheData = getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true, true);

        ActivityJkReqVO activityReqVO = buildActivityReqVO(reqVO);
        int drawChanceCount = getValidatedDrawChanceCount(activityReqVO);
        if (drawChanceCount <= 0) {
            throw exception(JK_NOT_ENABLED_REACHED);
        }

        List<ActivityJkCardDO> cardList = getCardList(reqVO.getActivityId());
        if (cardList.isEmpty()) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        ActivityJkCardDO guaranteeCard = getGuaranteeCard(cardList);
        if (guaranteeCard == null) {
            throw exception(JK_GUARANTEED_CARD);
        }

        RLock drawLock = redissonClient.getLock(JK_DRAW_LOCK_PREFIX + reqVO.getActivityId() + ":" + reqVO.getMemberId());
        boolean locked = false;
        boolean chanceConsumed = false;
        ActivityJkCardDO lockedCard = null;
        try {
            locked = drawLock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                return fallbackDrawResult(reqVO, cacheData, guaranteeCard, chanceConsumed);
            }

            // 先扣减一次缓存中的抽卡次数，避免同一用户在并发下重复消耗同一份机会。
            consumeOneDrawChance(reqVO.getActivityId(), reqVO.getMemberId());
            chanceConsumed = true;

            Map<Long, Integer> ownedCardCountMap = queryOwnedCardCountMap(reqVO.getActivityId(), reqVO.getMemberId());
            lockedCard = lockCardInventoryFromCache(reqVO.getActivityId(), guaranteeCard, ownedCardCountMap);
            if (lockedCard == null) {
                return fallbackDrawResult(reqVO, cacheData, guaranteeCard, chanceConsumed);
            }

            DrawResultVO drawResultVO = buildDrawResultVO(lockedCard);
            persistDrawResult(reqVO, lockedCard, cacheData);
            refreshDrawResult(reqVO, lockedCard);
            return drawResultVO;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (lockedCard != null) {
                rollbackCardInventoryCache(reqVO.getActivityId(), lockedCard.getId());
            }
            return fallbackDrawResult(reqVO, cacheData, guaranteeCard, chanceConsumed);
        } catch (Exception e) {
            if (lockedCard != null) {
                rollbackCardInventoryCache(reqVO.getActivityId(), lockedCard.getId());
            }
            if (e instanceof com.htyoudao.youdao.framework.common.exception.ServiceException serviceException
                    && !Objects.equals(serviceException.getCode(), LOTTERY_SYSTEM_AGAIN.getCode())) {
                throw serviceException;
            }
            return fallbackDrawResult(reqVO, cacheData, guaranteeCard, chanceConsumed);
        } finally {
            if (locked && drawLock.isHeldByCurrentThread()) {
                drawLock.unlock();
            }
        }
    }

    /**
     * 构建活动校验请求对象，复用现有的活动、门店和次数校验逻辑。
     */
    private ActivityJkReqVO buildActivityReqVO(DrawReqVO reqVO) {
        ActivityJkReqVO activityReqVO = new ActivityJkReqVO();
        activityReqVO.setActivityId(reqVO.getActivityId());
        activityReqVO.setMemberId(reqVO.getMemberId());
        activityReqVO.setStoreId(reqVO.getStoreId());
        return activityReqVO;
    }

    /**
     * 查询兜底卡片。
     */
    private ActivityJkCardDO getGuaranteeCard(List<ActivityJkCardDO> cardList) {
        return cardList.stream()
                .filter(card -> Objects.equals(card.getIsGuarantees(), 1))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查询用户已经获得过的卡片，用于过滤不允许重复获得的卡片。
     */
    private Map<Long, Integer> queryOwnedCardCountMap(Long activityId, Long memberId) {
        List<ActivityCardLogDO> cardLogList = activityCardLogMapper.selectList(new LambdaQueryWrapperX<ActivityCardLogDO>()
                .eq(ActivityCardLogDO::getActivityId, activityId)
                .eq(ActivityCardLogDO::getMemberId, memberId));
        return cardLogList.stream()
                .map(ActivityCardLogDO::getCardId)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(cardId -> cardId, cardId -> 1, Integer::sum));
    }

    /**
     * 按概率选择本次抽中的卡片。
     * 如果没有可参与抽取的普通卡，则直接回退到兜底卡。
     */
    private ActivityJkCardDO selectDrawCard(List<ActivityJkCardDO> cardList, ActivityJkCardDO guaranteeCard, Map<Long, Integer> ownedCardCountMap) {
        List<ActivityJkCardDO> normalCardList = cardList.stream()
                .filter(card -> !Objects.equals(card.getIsGuarantees(), 1))
                .filter(card -> card.getProbability() != null && card.getProbability().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        if (normalCardList.isEmpty()) {
            return guaranteeCard;
        }

        BigDecimal totalProbability = normalCardList.stream()
                .map(ActivityJkCardDO::getProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalProbability.compareTo(BigDecimal.ZERO) <= 0) {
            return guaranteeCard;
        }

        BigDecimal randomValue = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(totalProbability.doubleValue()));
        BigDecimal cumulativeProbability = BigDecimal.ZERO;
        for (ActivityJkCardDO cardDO : normalCardList) {
            cumulativeProbability = cumulativeProbability.add(cardDO.getProbability());
            if (randomValue.compareTo(cumulativeProbability) <= 0) {
                return cardDO;
            }
        }
        return guaranteeCard;
    }

    /**
     * 判断卡片是否允许重复抽取。
     */
    private boolean canRepeatDraw(ActivityJkCardDO cardDO, Map<Long, Integer> ownedCardCountMap) {
        int ownedCount = defaultZero(ownedCardCountMap == null ? null : ownedCardCountMap.get(cardDO.getId()));
        if (Objects.equals(cardDO.getIsRepeat(), 0)) {
            return true;
        }
        Integer repeatCount = cardDO.getRepeatCount();
        if (repeatCount == null || repeatCount <= 0) {
            return false;
        }
        return ownedCount < repeatCount;
    }

    /**
     * 扣减本次抽卡次数。
     * 先消耗当天次数，当天次数不足时再消耗不限次数的缓存。
     */
    private void consumeOneDrawChance(Long activityId, Long memberId) {
        Integer dailyChanceCount;
        Integer unlimitedChanceCount;
        try {
            dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(activityId, memberId));
            unlimitedChanceCount = getCachedDrawChanceCount(buildUnlimitedChanceCacheKey(activityId, memberId));
            if (dailyChanceCount == null && unlimitedChanceCount != null) {
                refreshDailyDrawChanceCache(activityId, memberId);
                dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(activityId, memberId));
            } else if (dailyChanceCount == null || unlimitedChanceCount == null) {
                refreshDrawChanceCache(activityId, memberId);
                dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(activityId, memberId));
                unlimitedChanceCount = getCachedDrawChanceCount(buildUnlimitedChanceCacheKey(activityId, memberId));
            }
        } catch (Exception e) {
            forceRefreshDrawChanceCache(activityId, memberId);
            dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(activityId, memberId));
            unlimitedChanceCount = getCachedDrawChanceCount(buildUnlimitedChanceCacheKey(activityId, memberId));
        }

        if (defaultZero(dailyChanceCount) > 0) {
            redisCache.setCacheObject(buildDailyChanceCacheKey(activityId, memberId), defaultZero(dailyChanceCount) - 1,
                    Math.toIntExact(calculateEndOfDaySeconds()), TimeUnit.SECONDS);
            return;
        }
        if (defaultZero(unlimitedChanceCount) > 0) {
            consumeOneUnlimitedTaskChance(activityId, memberId);
            redisCache.setCacheObject(buildUnlimitedChanceCacheKey(activityId, memberId), defaultZero(unlimitedChanceCount) - 1);
            return;
        }
        throw exception(JK_NOT_ENABLED_REACHED);
    }

    /**
     * 基于缓存占用本次抽中的卡片库存。
     * 如果抽中的卡片已达到上限，或者用户已抽中过且该卡不允许重复获取，则自动切换为兜底卡。
     */
    private ActivityJkCardDO lockCardInventoryFromCache(Long activityId, ActivityJkCardDO guaranteeCard, Map<Long, Integer> ownedCardCountMap) throws InterruptedException {
        RLock cardCacheLock = redissonClient.getLock(JK_CARD_CACHE_LOCK_PREFIX + activityId);
        boolean locked = false;
        try {
            locked = cardCacheLock.tryLock(100, 2000, TimeUnit.MILLISECONDS);
            if (!locked) {
                return null;
            }

            List<ActivityJkCardDO> cardList = getCardList(activityId);
            if (cardList.isEmpty()) {
                return null;
            }

            ActivityJkCardDO cacheGuaranteeCard = getGuaranteeCard(cardList);
            ActivityJkCardDO selectedCard = selectDrawCard(cardList, cacheGuaranteeCard, ownedCardCountMap);
            if (!canRepeatDraw(selectedCard, ownedCardCountMap)) {
                selectedCard = cacheGuaranteeCard;
            }
            // 未达到卡片保底次数时，即使命中该卡也转成兜底卡。
            if (hasCardMinimumNumberRestriction(activityId, selectedCard)) {
                selectedCard = cacheGuaranteeCard;
            }
            ActivityJkCardDO occupiedCard = occupyCardFromCache(cardList, selectedCard, ownedCardCountMap);
            if (occupiedCard == null) {
                occupiedCard = occupyCardFromCache(cardList, cacheGuaranteeCard, ownedCardCountMap);
            }
            if (occupiedCard == null && guaranteeCard != null) {
                occupiedCard = occupyCardFromCache(cardList, findCardById(cardList, guaranteeCard.getId()), ownedCardCountMap);
            }
            if (occupiedCard == null) {
                return null;
            }

            redisCache.setCacheObject(JKKeyConstants.JK_CARD + activityId, cardList, JK_LIST_CACHE_SECONDS, TimeUnit.SECONDS);
            return BeanUtils.toBean(occupiedCard, ActivityJkCardDO.class);
        } finally {
            if (locked && cardCacheLock.isHeldByCurrentThread()) {
                cardCacheLock.unlock();
            }
        }
    }

    /**
     * 校验当前卡片是否已达到保底次数门槛。
     * 口径与抽奖活动保持一致：当前活动历史总抽卡数未达到 minimumNumber 时，普通卡命中后转兜底卡。
     */
    private boolean hasCardMinimumNumberRestriction(Long activityId, ActivityJkCardDO cardDO) {
        if (activityId == null || cardDO == null || isGuaranteeCard(cardDO)) {
            return false;
        }
        Integer minimumNumber = cardDO.getMinimumNumber();
        if (minimumNumber == null || minimumNumber <= 0) {
            return false;
        }
        try {
            Long drawCount = activityCardLogMapper.selectCount(new LambdaQueryWrapperX<ActivityCardLogDO>()
                    .eq(ActivityCardLogDO::getActivityId, activityId));
            return defaultZero(drawCount == null ? null : drawCount.intValue()) < minimumNumber;
        } catch (Exception e) {
            log.warn("集卡保底次数校验异常，activityId={}，cardId={}", activityId, cardDO.getId(), e);
            return true;
        }
    }

    /**
     * 尝试从缓存中占用一张卡片。
     * 占用成功后会直接递增缓存中的已领取数量。
     */
    private ActivityJkCardDO occupyCardFromCache(List<ActivityJkCardDO> cardList, ActivityJkCardDO targetCard, Map<Long, Integer> ownedCardCountMap) {
        if (targetCard == null) {
            return null;
        }
        ActivityJkCardDO cacheCard = findCardById(cardList, targetCard.getId());
        if (!isCardAvailable(cacheCard, ownedCardCountMap)) {
            return null;
        }
        if (!isGuaranteeCard(cacheCard)) {
            cacheCard.setRemainNum(defaultZero(cacheCard.getRemainNum()) + 1);
        }
        return cacheCard;
    }

    /**
     * 判断卡片当前是否仍可发放。
     */
    private boolean isCardAvailable(ActivityJkCardDO cardDO, Map<Long, Integer> ownedCardCountMap) {
        if (cardDO == null) {
            return false;
        }
        if (isGuaranteeCard(cardDO)) {
            return true;
        }
        if (!canRepeatDraw(cardDO, ownedCardCountMap)) {
            return false;
        }
        Integer cardNum = cardDO.getCardNum();
        return cardNum == null || defaultZero(cardDO.getRemainNum()) < cardNum;
    }

    /**
     * 按卡片ID从缓存列表中查找最新卡片对象。
     */
    private ActivityJkCardDO findCardById(List<ActivityJkCardDO> cardList, Long cardId) {
        if (cardId == null || cardList == null || cardList.isEmpty()) {
            return null;
        }
        return cardList.stream()
                .filter(card -> Objects.equals(card.getId(), cardId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 回滚缓存中的卡片库存。
     * 仅在异步落库失败时做补偿，避免缓存中的已领取数量长期偏大。
     */
    private void rollbackCardInventoryCache(Long activityId, Long cardId) {
        if (activityId == null || cardId == null) {
            return;
        }
        RLock cardCacheLock = redissonClient.getLock(JK_CARD_CACHE_LOCK_PREFIX + activityId);
        boolean locked = false;
        try {
            locked = cardCacheLock.tryLock(100, 2000, TimeUnit.MILLISECONDS);
            if (!locked) {
                return;
            }
            List<ActivityJkCardDO> cardList = getCardList(activityId);
            ActivityJkCardDO cacheCard = findCardById(cardList, cardId);
            if (cacheCard == null || isGuaranteeCard(cacheCard) || defaultZero(cacheCard.getRemainNum()) <= 0) {
                return;
            }
            cacheCard.setRemainNum(defaultZero(cacheCard.getRemainNum()) - 1);
            redisCache.setCacheObject(JKKeyConstants.JK_CARD + activityId, cardList, JK_LIST_CACHE_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (locked && cardCacheLock.isHeldByCurrentThread()) {
                cardCacheLock.unlock();
            }
        }
    }

    /**
     * 异步把卡片已领取数量增量同步到数据库。
     * 主流程已经先基于缓存占用库存，这里只做数据库加一，避免并发下被旧值覆盖。
     */
    private void increaseCardInventoryInDb(Long cardId) {
        if (cardId == null) {
            return;
        }
        UpdateWrapper<ActivityJkCardDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", cardId)
                .setSql("remain_num = IFNULL(remain_num, 0) + 1");
        activityJkCardMapper.update(null, updateWrapper);
    }

    /**
     * 判断当前卡片是否为兜底卡。
     */
    private boolean isGuaranteeCard(ActivityJkCardDO cardDO) {
        return cardDO != null && Objects.equals(cardDO.getIsGuarantees(), 1);
    }

    /**
     * 组装抽卡结果返回结构。
     */
    private DrawResultVO buildDrawResultVO(ActivityJkCardDO cardDO) {
        DrawResultVO drawResultVO = new DrawResultVO();
        drawResultVO.setJkCardId(cardDO.getId());
        drawResultVO.setJkCardName(cardDO.getCardName());
        drawResultVO.setJkCardType(defaultZero(cardDO.getCardType()));
        drawResultVO.setJkCardImgUrl(cardDO.getCardImgUrl());
        drawResultVO.setPackageInfo(null);
        drawResultVO.setOutBillNo(null);
        return drawResultVO;
    }

    /**
     * 返回兜底抽卡结果。
     * 这里只处理抽卡过程中的系统繁忙类异常，保证用户在极端情况下仍然能拿到兜底卡。
     */
    private DrawResultVO fallbackDrawResult(DrawReqVO reqVO, ActivityJkSaveOrUpdateReqVO cacheData,
                                           ActivityJkCardDO guaranteeCard, boolean chanceConsumed) {
        if (guaranteeCard == null) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }

        if (!chanceConsumed) {
            consumeOneDrawChance(reqVO.getActivityId(), reqVO.getMemberId());
        }

        ActivityJkCardDO occupiedGuaranteeCard;
        try {
            occupiedGuaranteeCard = lockCardInventoryFromCache(reqVO.getActivityId(), guaranteeCard, Collections.emptyMap());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            refreshDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        if (occupiedGuaranteeCard == null) {
            refreshDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }

        persistDrawResult(reqVO, occupiedGuaranteeCard, cacheData);
        refreshDrawResult(reqVO, occupiedGuaranteeCard);
        return buildDrawResultVO(occupiedGuaranteeCard);
    }

    /**
     * 同步落库抽卡结果，确保抽卡次数和卡片库存都有稳定的落库依据。
     */
    private void persistDrawResult(DrawReqVO reqVO, ActivityJkCardDO cardDO, ActivityJkSaveOrUpdateReqVO cacheData) {
        Boolean persisted = transactionTemplate.execute(status -> {
            saveDrawLog(reqVO, cardDO, cacheData);
            if (!isGuaranteeCard(cardDO)) {
                increaseCardInventoryInDb(cardDO.getId());
            }
            return Boolean.TRUE;
        });
        if (!Boolean.TRUE.equals(persisted)) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
    }

    /**
     * 异步刷新任务消耗次数和抽卡次数缓存。
     * 抽卡日志已经先行落库，这里即使失败也可以通过后续重刷恢复一致。
     */
    private void refreshDrawResult(DrawReqVO reqVO, ActivityJkCardDO cardDO) {
        try {
            refreshDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
        } catch (Exception e) {
            log.error("集卡抽卡同步刷新缓存失败，activityId={}，memberId={}，cardId={}",
                    reqVO.getActivityId(), reqVO.getMemberId(), cardDO.getId(), e);
            forceRefreshDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
        }
    }

    /**
     * 保存抽卡日志。
     * 当前集卡没有单独的用户卡片表，这里通过抽卡日志沉淀用户获得的卡片数据。
     */
    private void saveDrawLog(DrawReqVO reqVO, ActivityJkCardDO cardDO, ActivityJkSaveOrUpdateReqVO cacheData) {
        WxMemberVO wxMemberVO = null;
        try {
            CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(reqVO.getMemberId());
            if (memberResult != null && memberResult.isSuccess()) {
                wxMemberVO = memberResult.getData();
            }
        } catch (Exception e) {
            log.warn("集卡抽卡查询会员信息失败，memberId={}", reqVO.getMemberId(), e);
        }

        ActivityCardLogDO activityCardLogDO = new ActivityCardLogDO();
        activityCardLogDO.setActivityId(reqVO.getActivityId());
        activityCardLogDO.setCardId(cardDO.getId());
        activityCardLogDO.setCardType(cardDO.getCardType());
        activityCardLogDO.setCardName(cardDO.getCardName());
        activityCardLogDO.setCardImgUrl(cardDO.getCardImgUrl());
        activityCardLogDO.setMemberId(reqVO.getMemberId());
        activityCardLogDO.setMemberName(resolveMemberName(wxMemberVO));
        activityCardLogDO.setMemberMobile(wxMemberVO == null ? null : wxMemberVO.getMemberMobile());
        activityCardLogDO.setStoreId(reqVO.getStoreId());
        activityCardLogDO.setStatus(0);
        activityCardLogDO.setStartDate(cacheData.getStartDate());
        activityCardLogDO.setEndDate(cacheData.getEndDate());
        activityCardLogDO.setBusinessId(BusinessContextHolder.getBusinessId());
        activityCardLogMapper.insert(activityCardLogDO);
    }

    /**
     * 解析会员展示名称。
     */
    private String resolveMemberName(WxMemberVO wxMemberVO) {
        if (wxMemberVO == null) {
            return null;
        }
        if (ObjectUtil.isNotEmpty(wxMemberVO.getMemberNickName())) {
            return wxMemberVO.getMemberNickName();
        }
        return wxMemberVO.getMemberName();
    }
    @Override

    /**
     * 查询任务列表及完成情况。
     */
    public List<TaskVO> getTaskList(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        // 任务列表和详情共用活动缓存，避免每次请求都回查主表和集卡子表。
        ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(reqVO.getActivityId());
        if (cacheData == null) {
            cacheData = loadJkSettingWithLock(reqVO.getActivityId());
        }
        if (cacheData == null) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        if (!TimeValidationUtil.isTimeValid(
                cacheData.getStartDate(),
                cacheData.getEndDate(),
                convertListToString(cacheData.getDayNumberList()),
                convertListToString(cacheData.getWeekNumberList()),
                convertListToString(cacheData.getTimeRangeList())
        )) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }

        List<TaskVO> taskList = new ArrayList<>(4);
        // 这里只返回已开启的任务，次数上限由各任务自己的配置字段决定。
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getDailyAttendance(), cacheData.getDailyAttenCount(), ActivityJkTaskTypeEnum.SIGN,cacheData.getPaymentCount());
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getPlaceOrderStatus(), cacheData.getPlaceOrderCardNumber(), ActivityJkTaskTypeEnum.ORDER,cacheData.getPaymentCount());
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getShareEvent(), cacheData.getShareCount(), ActivityJkTaskTypeEnum.SHARE,cacheData.getPaymentCount());
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getBrowseType(), cacheData.getBrowseCount(), ActivityJkTaskTypeEnum.BROWSE,cacheData.getPaymentCount());
        return taskList;
    }

    /**
     * 完成签到任务。
     * 1. 校验活动和任务开关
     * 2. 根据次数上限决定是否按当天筛选
     * 3. 首次记录插入，非首次记录递增
     */
    @Override
    public Boolean signTask(ActivityJkReqVO reqVO) {
        completeTask(reqVO, ActivityJkTaskTypeEnum.SIGN, 1);
        return true;
    }

    /**
     * 完成浏览首页任务。
     */
    @Override
    public Boolean browseTask(ActivityJkReqVO reqVO) {
        completeTask(reqVO, ActivityJkTaskTypeEnum.BROWSE, 1);
        return true;
    }

    /**
     * 完成分享任务。
     * 1. 校验活动和分享任务开关
     * 2. 校验同一用户当天不能重复给同一个邀请人助力
     * 3. 校验单个用户每天最多助力 5 次
     * 4. 校验邀请人的助力成功次数不能超过活动配置
     */
    @Override
    public Boolean shareTask(ActivityJkReqVO reqVO) {
        completeShareHelp(reqVO);
        return true;
    }
    /**
     * 完成分享任务校验
     * 1. 校验活动和分享任务开关
     * 2. 校验同一用户当天不能重复给同一个邀请人助力
     * 3. 校验单个用户每天最多助力 5 次
     * 4. 校验邀请人的助力成功次数不能超过活动配置
     */
    @Override
    public Boolean shareCheck(ActivityJkReqVO reqVO) {
        ActivityJkSaveOrUpdateReqVO cacheData = getValidatedShareHelpActivity(reqVO.getActivityId());
        if (!isTaskEnabled(cacheData.getShareEvent())) {
            throw exception(JK_TASK_NOT_ENABLED);
        }
        LocalDate today = LocalDate.now();
        validateDailyHelpLimit(reqVO.getActivityId(), reqVO.getInviterMemberId(), reqVO.getMemberId(), today);
        return true;
    }
    /**
     * 校验用户是否具备抽卡资格。
     * 这里只校验活动状态和缓存中的可用抽卡次数，不处理真实抽卡扣减逻辑。
     */
    @Override
    public Boolean verifyDraw(ActivityJkReqVO reqVO) {
        int totalChanceCount = getValidatedDrawChanceCount(reqVO);
        if (totalChanceCount <= 0) {
            throw exception(LOTTERY_NO_CHANCE);
        }
        return true;
    }

    /**
     * 查询当前用户在当前活动下的可用集卡次数。
     * 会先校验活动状态和门店范围，再读取抽卡次数缓存。
     */
    @Override
    public Integer getDrawChanceCount(ActivityJkReqVO reqVO) {
        return getValidatedDrawChanceCount(reqVO);
    }

    @Override

    /**
     * 查询我的卡片列表。
     */
    public MyCardListVO getMyCardList(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        // 先校验活动是否存在且当前时间是否仍然有效，避免失效活动继续展示用户卡片数据。
        ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(reqVO.getActivityId());
        if (cacheData == null) {
            cacheData = loadJkSettingWithLock(reqVO.getActivityId());
        }
        if (cacheData == null) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        if (!TimeValidationUtil.isTimeValid(
                cacheData.getStartDate(),
                cacheData.getEndDate(),
                convertListToString(cacheData.getDayNumberList()),
                convertListToString(cacheData.getWeekNumberList()),
                convertListToString(cacheData.getTimeRangeList())
        )) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }

        List<ActivityJkCardDO> activityCardList = getCardList(reqVO.getActivityId());
        if (activityCardList.isEmpty()) {
            MyCardListVO emptyVO = new MyCardListVO();
            emptyVO.setCardList(Collections.emptyList());
            return emptyVO;
        }

        Map<Long, List<ActivityCardLogDO>> userCardLogMap = buildUserCardLogMap(reqVO.getActivityId(), reqVO.getMemberId());
        List<UserCardVO> userCardList = buildUserCardList(activityCardList, userCardLogMap);

        MyCardListVO myCardListVO = new MyCardListVO();
        myCardListVO.setCardList(userCardList);
        return myCardListVO;
    }

    @Override

    /**
     * 执行万能卡兑换。
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean exchangeUniversalCard(UniversalExchangeReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null
                || reqVO.getTargetCardId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        getValidatedActivity(reqVO.getActivityId(), null, false);

        List<ActivityJkCardDO> activityCardList = getCardList(reqVO.getActivityId());
        ActivityJkCardDO targetCard = activityCardList.stream()
                .filter(card -> Objects.equals(card.getId(), reqVO.getTargetCardId()))
                .findFirst()
                .orElse(null);
        if (targetCard == null || !Objects.equals(targetCard.getCardType(), 2)) {
            throw exception(JK_UNIVERSAL_TARGET_INVALID);
        }

        String lockKey = JK_UNIVERSAL_EXCHANGE_LOCK_PREFIX + reqVO.getActivityId() + ":" + reqVO.getMemberId();
        RLock exchangeLock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = exchangeLock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            ActivityCardLogDO universalCardLogDO = activityCardLogMapper.selectOne(new LambdaQueryWrapperX<ActivityCardLogDO>()
                    .eq(ActivityCardLogDO::getActivityId, reqVO.getActivityId())
                    .eq(ActivityCardLogDO::getMemberId, reqVO.getMemberId())
                    .eq(ActivityCardLogDO::getCardType, 3)
                    .eq(ActivityCardLogDO::getStatus, 0)
                    .orderByAsc(ActivityCardLogDO::getCreateTime)
                    .orderByAsc(ActivityCardLogDO::getId)
                    .last("limit 1"));
            if (universalCardLogDO == null) {
                throw exception(JK_UNIVERSAL_RECORD_NOT_FOUND);
            }

            // 万能卡兑换出的套系卡不受卡片数量和重复获取限制，只做记录转换。
            UpdateWrapper<ActivityCardLogDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", universalCardLogDO.getId())
                    .eq("status", 0)
                    .set("status", 1);
            if (activityCardLogMapper.update(null, updateWrapper) <= 0) {
                throw exception(JK_UNIVERSAL_RECORD_NOT_FOUND);
            }

            ActivityCardLogDO targetCardLogDO = new ActivityCardLogDO();
            targetCardLogDO.setActivityId(reqVO.getActivityId());
            targetCardLogDO.setCardId(targetCard.getId());
            targetCardLogDO.setCardType(targetCard.getCardType());
            targetCardLogDO.setCardName(targetCard.getCardName());
            targetCardLogDO.setCardImgUrl(targetCard.getCardImgUrl());
            targetCardLogDO.setMemberId(universalCardLogDO.getMemberId());
            targetCardLogDO.setMemberName(universalCardLogDO.getMemberName());
            targetCardLogDO.setMemberMobile(universalCardLogDO.getMemberMobile());
            targetCardLogDO.setStoreId(universalCardLogDO.getStoreId());
            targetCardLogDO.setStoreName(universalCardLogDO.getStoreName());
            targetCardLogDO.setStatus(0);
            targetCardLogDO.setStartDate(universalCardLogDO.getStartDate());
            targetCardLogDO.setEndDate(universalCardLogDO.getEndDate());
            targetCardLogDO.setBusinessId(universalCardLogDO.getBusinessId());
            activityCardLogMapper.insert(targetCardLogDO);
            saveUniversalExchangeRecord(reqVO.getActivityId(), universalCardLogDO, targetCard);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && exchangeLock.isHeldByCurrentThread()) {
                exchangeLock.unlock();
            }
        }
    }

    @Override

    /**
     * 查询活动奖品列表。
     */
    public ActivityJkPrizeListVO getPrizeList(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        List<ActivityJkPrizeDO> prizeDOList = getPrizeList(reqVO.getActivityId());
        Map<Long, List<ActivityCardLogDO>> userCardLogMap = buildUserCardLogMap(reqVO.getActivityId(), reqVO.getMemberId());
        ActivityJkPrizeListVO prizeListVO = new ActivityJkPrizeListVO();
        prizeListVO.setSetPrizeList(buildPrizeVOList(prizeDOList, 1, reqVO.getActivityId(), reqVO.getMemberId(), userCardLogMap));
        prizeListVO.setHiddenPrizeList(buildPrizeVOList(prizeDOList, 2, reqVO.getActivityId(), reqVO.getMemberId(), userCardLogMap));
        return prizeListVO;
    }

    /**
     * 执行奖品兑换。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrizeExchangeResultVO exchangePrize(PrizeExchangeReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null
                || reqVO.getPrizeId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true);
        ActivityJkPrizeDO prizeDO = getPrizeList(reqVO.getActivityId()).stream()
                .filter(item -> Objects.equals(item.getId(), reqVO.getPrizeId()))
                .findFirst()
                .orElse(null);
        if (prizeDO == null) {
            throw exception(JK_PRIZE_NOT_FOUND);
        }

        String lockKey = JK_PRIZE_EXCHANGE_LOCK_PREFIX + reqVO.getActivityId() + ":" + reqVO.getMemberId();
        RLock exchangeLock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = exchangeLock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            ActivityJkPrizeExchangeDO exchangeDO = executePrizeExchangeTransaction(reqVO, prizeDO);
            syncPrizeCacheInventory(reqVO.getActivityId(), prizeDO.getId());
            exchangeDO = issuePrizeAfterExchange(reqVO.getActivityId(), reqVO.getMemberId(), prizeDO, exchangeDO);
            if (Objects.equals(prizeDO.getPrizeType(), 5)) {
                exchangeDO = sendRedPacketAfterExchange(reqVO.getMemberId(), prizeDO, exchangeDO);
            }
            return buildPrizeExchangeResult(exchangeDO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && exchangeLock.isHeldByCurrentThread()) {
                exchangeLock.unlock();
            }
        }
    }

    /**
     * 在本地事务中完成奖品库存占用、卡片消耗和兑奖记录落库。
     */
    private ActivityJkPrizeExchangeDO executePrizeExchangeTransaction(PrizeExchangeReqVO reqVO, ActivityJkPrizeDO prizeDO) {
        ActivityJkPrizeExchangeDO exchangeDO = transactionTemplate.execute(status -> {
            List<ActivityCardLogDO> selectedCardLogList = findPrizeExchangeCardLogs(reqVO.getActivityId(), reqVO.getMemberId(), prizeDO);
            validatePrizeExchangeLimit(reqVO.getActivityId(), reqVO.getMemberId(), prizeDO);
            if (!tryIncreasePrizeInventory(prizeDO.getId())) {
                throw exception(JK_PRIZE_STOCK_NOT_ENOUGH);
            }

            markCardLogsUsed(reqVO.getActivityId(), reqVO.getMemberId(), selectedCardLogList);
            ActivityJkPrizeExchangeDO transactionExchangeDO = buildPrizeExchangeRecord(
                    reqVO.getActivityId(), reqVO.getMemberId(),reqVO.getStoreId(), prizeDO, selectedCardLogList.get(0)
            );
            if (Objects.equals(prizeDO.getPrizeType(), 5)) {
                transactionExchangeDO.setClaimStatus(null);
                transactionExchangeDO.setPackageInfo(null);
                transactionExchangeDO.setOutBillNo(null);
                transactionExchangeDO.setPrizeState(0);
            }
            activityJkPrizeExchangeMapper.insert(transactionExchangeDO);
            return transactionExchangeDO;
        });
        if (exchangeDO == null) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        return exchangeDO;
    }

    /**
     * 在本地事务提交后发起红包，并把结果回写到兑奖记录。
     */
    private ActivityJkPrizeExchangeDO sendRedPacketAfterExchange(Long memberId, ActivityJkPrizeDO prizeDO,
                                                                 ActivityJkPrizeExchangeDO exchangeDO) {
        try {
            WxMemberVO wxMemberVO = getValidJkWxMember(memberId);
            LotteryRedPacketVo redPacketVo = buildJkRedPacketVo(wxMemberVO, prizeDO);
            redPacketVo.setActivityId(exchangeDO.getActivityId());
            TransferToUser.TransferToUserResponse response = lotteryRedPacketService.transferUser(redPacketVo);
            if (!isRedPacketTransferSuccess(response)) {
                log.warn("集卡红包发放失败，兑奖记录保留待补偿，exchangeId={}，prizeId={}", exchangeDO.getId(), prizeDO.getId());
                exchangeDO.setPrizeState(8);
                return exchangeDO;
            }

            UpdateWrapper<ActivityJkPrizeExchangeDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", exchangeDO.getId())
                    .set("out_bill_no", response.getOutBillNo())
                    .set("package_info", response.getPackageInfo())
                    .set("claim_status", 1);
            activityJkPrizeExchangeMapper.update(null, updateWrapper);
            exchangeDO.setOutBillNo(response.getOutBillNo());
            exchangeDO.setPackageInfo(response.getPackageInfo());
            exchangeDO.setClaimStatus(1);
            return exchangeDO;
        } catch (Exception e) {
            log.error("集卡红包发放异常，兑奖记录保留待补偿，exchangeId={}，prizeId={}",
                    exchangeDO.getId(), prizeDO.getId(), e);
            exchangeDO.setPrizeState(8);
            return exchangeDO;
        }
    }

    /**
     * 在兑奖记录落库后补发积分、优惠券等账户型奖品。
     */
    private ActivityJkPrizeExchangeDO issuePrizeAfterExchange(Long activityId, Long memberId, ActivityJkPrizeDO prizeDO,
                                                              ActivityJkPrizeExchangeDO exchangeDO) {
        if (Objects.equals(prizeDO.getPrizeType(), 1)) {
            return sendPointsAfterExchange(activityId, memberId, prizeDO, exchangeDO);
        }
        if (Objects.equals(prizeDO.getPrizeType(), 2)) {
            return sendCouponAfterExchange(memberId, prizeDO, exchangeDO);
        }
        return exchangeDO;
    }

    /**
     * 兑换积分奖品后给用户发放积分，并回写兑换记录。
     */
    private ActivityJkPrizeExchangeDO sendPointsAfterExchange(Long activityId, Long memberId, ActivityJkPrizeDO prizeDO,
                                                              ActivityJkPrizeExchangeDO exchangeDO) {
        try {
            WxMemberVO wxMemberVO = getJkMember(memberId);
            int currentIntegral = wxMemberVO.getMemberIntegral() == null ? 0 : wxMemberVO.getMemberIntegral();
            int pointAmount = prizeDO.getPrizeValue() == null ? 0 : prizeDO.getPrizeValue().intValue();
            int newIntegral = currentIntegral + pointAmount;
            wxMemberApi.updateMemberById(memberId, newIntegral);
            addJkPointsLog(activityId, pointAmount, wxMemberVO);
            return exchangeDO;
        } catch (Exception e) {
            log.error("集卡积分发放异常，兑奖记录保留待补偿，exchangeId={}，prizeId={}",
                    exchangeDO.getId(), prizeDO.getId(), e);
            exchangeDO.setPrizeState(8);
            return exchangeDO;
        }
    }

    /**
     * 兑换优惠券奖品后给用户发放优惠券，并回写兑换记录。
     */
    private ActivityJkPrizeExchangeDO sendCouponAfterExchange(Long memberId, ActivityJkPrizeDO prizeDO,
                                                              ActivityJkPrizeExchangeDO exchangeDO) {
        try {
            GoodCouponDO goodCouponDO = lotteryAddLogService.getGoodCoupon(prizeDO.getAwardId());
            if (goodCouponDO == null) {
                log.warn("集卡优惠券发放失败，未查询到优惠券配置，exchangeId={}，awardId={}",
                        exchangeDO.getId(), prizeDO.getAwardId());
                return exchangeDO;
            }

            UserCouponDO userCouponDO = BeanUtils.toBean(goodCouponDO, UserCouponDO.class);
            userCouponDO.setUserId(memberId);
            userCouponDO.setCouponId(prizeDO.getAwardId());
            userCouponDO.setIsUsed(0);
            userCouponDO.setId(null);
            userCouponDO.setUseTime(null);
            parseJkUserCouponTime(goodCouponDO, userCouponDO);
            userCouponDO.setMemberMobile(exchangeDO.getMemberMobile());
            userCouponDO.setBusinessId(BusinessContextHolder.getBusinessId());
            userCouponDO.setCouponSource(CouponSourceType.COLLECT_CARD.getCode());
            userCouponMapper.insert(userCouponDO);

            UpdateWrapper<ActivityJkPrizeExchangeDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", exchangeDO.getId())
                    .set("award_id", prizeDO.getAwardId())
                    .set("user_coupon_id", userCouponDO.getId());
            activityJkPrizeExchangeMapper.update(null, updateWrapper);
            exchangeDO.setAwardId(prizeDO.getAwardId());
            exchangeDO.setUserCouponId(userCouponDO.getId());
            return exchangeDO;
        } catch (Exception e) {
            log.error("集卡优惠券发放异常，兑奖记录保留待补偿，exchangeId={}，prizeId={}",
                    exchangeDO.getId(), prizeDO.getId(), e);
            exchangeDO.setPrizeState(8);
            return exchangeDO;
        }
    }

    @Override

    /**
     * 保存兑奖收货地址。
     */
    public Boolean saveAddress(AddressSaveReqVO reqVO) {
        if (reqVO == null || reqVO.getId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        ActivityJkPrizeExchangeDO exchangeDO = activityJkPrizeExchangeMapper.selectOne(new LambdaQueryWrapperX<ActivityJkPrizeExchangeDO>()
                .eq(ActivityJkPrizeExchangeDO::getId, reqVO.getId())
                .eq(ActivityJkPrizeExchangeDO::getMemberId, reqVO.getMemberId())
                .last("limit 1"));
        if (exchangeDO == null) {
            throw exception(JK_EXCHANGE_RECORD_NOT_FOUND);
        }

        exchangeDO.setReceiveUser(reqVO.getMemberNickName());
        exchangeDO.setReceiveMobile(reqVO.getMemberMobile());
        exchangeDO.setReceiveAddress(reqVO.getReceiveAddress());
        if (Objects.equals(exchangeDO.getPrizeType(), 4)) {
            exchangeDO.setPrizeState(2);
        }
        activityJkPrizeExchangeMapper.updateById(exchangeDO);
        return true;
    }

    @Override

    /**
     * 分页查询抽卡记录。
     */
    public PageResult<DrawRecordVO> getDrawRecord(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        PageResult<ActivityCardLogDO> pageResult = activityCardLogMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityCardLogDO>()
                .eq(ActivityCardLogDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityCardLogDO::getMemberId, reqVO.getMemberId())
                .orderByDesc(ActivityCardLogDO::getCreateTime)
                .orderByDesc(ActivityCardLogDO::getId));
        List<DrawRecordVO> recordVOList = pageResult.getList().stream()
                .map(this::buildDrawRecordVO)
                .collect(Collectors.toList());
        return new PageResult<>(recordVOList, pageResult.getTotal());
    }

    @Override

    /**
     * 分页查询兑奖记录。
     */
    public PageResult<ExchangeRecordVO> getExchangeRecord(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        PageResult<ActivityJkPrizeExchangeDO> pageResult = activityJkPrizeExchangeMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityJkPrizeExchangeDO>()
                .eq(ActivityJkPrizeExchangeDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityJkPrizeExchangeDO::getMemberId, reqVO.getMemberId())
                .orderByDesc(ActivityJkPrizeExchangeDO::getCreateTime)
                .orderByDesc(ActivityJkPrizeExchangeDO::getId));
        List<ExchangeRecordVO> recordVOList = pageResult.getList().stream()
                .map(this::buildExchangeRecordVO)
                .collect(Collectors.toList());
        return new PageResult<>(recordVOList, pageResult.getTotal());
    }

    /**
     * 获取卡片数量
     * @param id
     * @param memberId
     * @return
     */
    @Override
    public Long getCardCount(Long id, Long memberId) {
        if(ObjectUtil.isNotEmpty(memberId)){
            LambdaQueryWrapper<ActivityCardLogDO> lambdaQueryWrapper = new LambdaQueryWrapper<ActivityCardLogDO>();
            lambdaQueryWrapper.eq(ActivityCardLogDO::getMemberId,memberId);
            lambdaQueryWrapper.eq(ActivityCardLogDO::getActivityId,id);
            lambdaQueryWrapper.eq(ActivityCardLogDO::getStatus,0);
            Long count = activityCardLogMapper.selectCount(lambdaQueryWrapper);
            return count;
        }

        return 0L;
    }
    @Override
    public ActivityCollectAppShareVO getShareVO(Long activityId) {

        LambdaQueryWrapper<ActivityJkDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityJkDO::getActivityId, activityId);
        ActivityJkDO activityJkDO = this.getOne(queryWrapper);
        if (ObjectUtil.isEmpty(activityJkDO)) {
            throw new ServiceException(ErrorCodeConstants.JD_NOT_FOUND);
        }
        ActivityCollectAppShareVO activityCollectAppShareVO = new ActivityCollectAppShareVO();
        BeanUtils.copyProperties(activityJkDO, activityCollectAppShareVO);
        return activityCollectAppShareVO;
    }
    /**
     * 校验活动是否有效。
     */
    private ActivityJkSaveOrUpdateReqVO getValidatedActivity(Long activityId) {
        return getValidatedActivity(activityId, null, false, false);
    }

    private ActivityJkSaveOrUpdateReqVO getValidatedShareHelpActivity(Long activityId) {
        return getValidatedActivity(activityId, null, false, false);
    }

    /**
     * 统一校验集卡活动是否开启、是否在有效时间内，以及是否满足门店条件。
     */
    private ActivityJkSaveOrUpdateReqVO getValidatedActivity(Long activityId, Long storeId, boolean validateStore) {
        return getValidatedActivity(activityId, storeId, validateStore, false);
    }

    private ActivityJkSaveOrUpdateReqVO getValidatedActivity(Long activityId, Long storeId, boolean validateStore,
                                                             boolean validateCommunity) {
        // 活动校验统一先读缓存，未命中时再回源数据库。
        ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(activityId);
        if (cacheData == null) {
            cacheData = loadJkSettingWithLock(activityId);
        }
        if (cacheData == null) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        if (cacheData.getIsEnabled()==0) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        if (!TimeValidationUtil.isTimeValid(
                cacheData.getStartDate(),
                cacheData.getEndDate(),
                convertListToString(cacheData.getDayNumberList()),
                convertListToString(cacheData.getWeekNumberList()),
                convertListToString(cacheData.getTimeRangeList())
        )) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        if (validateCommunity && !activityAppService.checkCanJoin(activityId)) {
            throw exception(LOTTERY_WECOMGROUP_ERROR);
        }
        if (validateStore) {
            validateJkStore(activityId, storeId);
        }
        return cacheData;
    }

    /**
     * 校验兑换奖品所使用的卡片记录是否满足奖品兑换条件。
     * 套系卡兑换要求传入完整一套套系卡记录，隐藏卡兑换要求传入隐藏卡记录。
     */
    private List<ActivityCardLogDO> findPrizeExchangeCardLogs(Long activityId, Long memberId, ActivityJkPrizeDO prizeDO) {
        if (Objects.equals(prizeDO.getExchangeConditions(), 2)) {
            ActivityCardLogDO hiddenCardLog = activityCardLogMapper.selectOne(new LambdaQueryWrapperX<ActivityCardLogDO>()
                    .eq(ActivityCardLogDO::getActivityId, activityId)
                    .eq(ActivityCardLogDO::getMemberId, memberId)
                    .eq(ActivityCardLogDO::getCardType, 4)
                    .eq(ActivityCardLogDO::getStatus, 0)
                    .orderByAsc(ActivityCardLogDO::getCreateTime)
                    .orderByAsc(ActivityCardLogDO::getId)
                    .last("limit 1"));
            if (hiddenCardLog == null) {
                throw exception(JK_PRIZE_CARD_INVALID);
            }
            return Collections.singletonList(hiddenCardLog);
        }

        if (!Objects.equals(prizeDO.getExchangeConditions(), 1)) {
            throw exception(JK_PRIZE_CARD_INVALID);
        }

        List<ActivityJkCardDO> setCardConfigList = getCardList(activityId).stream()
                .filter(card -> Objects.equals(card.getCardType(), 2))
                .sorted(Comparator.comparing(ActivityJkCardDO::getCardSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ActivityJkCardDO::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
        if (setCardConfigList.isEmpty()) {
            throw exception(JK_PRIZE_CARD_INVALID);
        }

        List<ActivityCardLogDO> selectedCardLogList = new ArrayList<>();
        for (ActivityJkCardDO setCardDO : setCardConfigList) {
            ActivityCardLogDO setCardLog = activityCardLogMapper.selectOne(new LambdaQueryWrapperX<ActivityCardLogDO>()
                    .eq(ActivityCardLogDO::getActivityId, activityId)
                    .eq(ActivityCardLogDO::getMemberId, memberId)
                    .eq(ActivityCardLogDO::getCardId, setCardDO.getId())
                    .eq(ActivityCardLogDO::getStatus, 0)
                    .orderByAsc(ActivityCardLogDO::getCreateTime)
                    .orderByAsc(ActivityCardLogDO::getId)
                    .last("limit 1"));
            if (setCardLog == null) {
                throw exception(JK_PRIZE_CARD_INVALID);
            }
            selectedCardLogList.add(setCardLog);
        }
        return selectedCardLogList;
    }

    /**
     * 校验奖品个人兑换次数限制。
     */
    private void validatePrizeExchangeLimit(Long activityId, Long memberId, ActivityJkPrizeDO prizeDO) {
        if (!Objects.equals(prizeDO.getExchangeRestrictions(), 1) || prizeDO.getExchangeCount() == null) {
            return;
        }
        Long exchangedCount = activityJkPrizeExchangeMapper.selectCount(new LambdaQueryWrapperX<ActivityJkPrizeExchangeDO>()
                .eq(ActivityJkPrizeExchangeDO::getActivityId, activityId)
                .eq(ActivityJkPrizeExchangeDO::getMemberId, memberId)
                .eq(ActivityJkPrizeExchangeDO::getPrizeId, prizeDO.getId()));
        if (exchangedCount != null && exchangedCount >= prizeDO.getExchangeCount()) {
            throw exception(JK_PRIZE_CARD_LIMIT);
        }
    }

    /**
     * 原子增加奖品已兑换数量，防止高并发下超量发放。
     */
    private boolean tryIncreasePrizeInventory(Long prizeId) {
        UpdateWrapper<ActivityJkPrizeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", prizeId)
                .apply("(prize_num IS NULL OR IFNULL(remain_num, 0) < prize_num)")
                .setSql("remain_num = IFNULL(remain_num, 0) + 1");
        return activityJkPrizeMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 同步奖品缓存中的已兑换数量，避免列表接口继续返回旧库存。
     */
    private void syncPrizeCacheInventory(Long activityId, Long prizeId) {
        Object cachedObject = redisCache.getCacheObject(JKKeyConstants.JK_PRIZE + activityId);
        if (!(cachedObject instanceof List<?> cachedList)) {
            return;
        }
        ActivityJkPrizeDO latestPrizeDO = activityJkPrizeMapper.selectById(prizeId);
        if (latestPrizeDO == null) {
            return;
        }
        List<ActivityJkPrizeDO> prizeDOList = (List<ActivityJkPrizeDO>) cachedList;
        prizeDOList.stream()
                .filter(item -> Objects.equals(item.getId(), prizeId))
                .findFirst()
                .ifPresent(item -> item.setRemainNum(latestPrizeDO.getRemainNum()));
        redisCache.setCacheObject(JKKeyConstants.JK_PRIZE + activityId, prizeDOList, JK_LIST_CACHE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 将参与兑换的卡片记录更新为已使用。
     */
    private void markCardLogsUsed(Long activityId, Long memberId, List<ActivityCardLogDO> selectedCardLogList) {
        for (ActivityCardLogDO cardLogDO : selectedCardLogList) {
            UpdateWrapper<ActivityCardLogDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", cardLogDO.getId())
                    .eq("activity_id", activityId)
                    .eq("member_id", memberId)
                    .eq("status", 0)
                    .set("status", 1);
            if (activityCardLogMapper.update(null, updateWrapper) <= 0) {
                throw exception(JK_PRIZE_CARD_INVALID);
            }
        }
    }

    /**
     * 构建奖品兑换记录。
     */
    private ActivityJkPrizeExchangeDO buildPrizeExchangeRecord(Long activityId, Long memberId,Long storeId,
                                                               ActivityJkPrizeDO prizeDO, ActivityCardLogDO cardLogDO) {
        ActivityJkPrizeExchangeDO exchangeDO = new ActivityJkPrizeExchangeDO();
        exchangeDO.setActivityId(activityId);
        exchangeDO.setPrizeId(prizeDO.getId());
        exchangeDO.setPrizeType(prizeDO.getPrizeType());
        exchangeDO.setAwardId(prizeDO.getAwardId());
        exchangeDO.setPrizeValue(prizeDO.getPrizeValue() == null ? null :  prizeDO.getPrizeValue()) ;
        exchangeDO.setPrizeName(prizeDO.getPrizeName());
        exchangeDO.setPrizeImgUrl(prizeDO.getPrizeImgUrl());
        exchangeDO.setMemberId(memberId);
        exchangeDO.setStoreId(storeId != null ? storeId : null);
        exchangeDO.setMemberName(cardLogDO.getMemberName());
        exchangeDO.setMemberMobile(cardLogDO.getMemberMobile());
        exchangeDO.setPrizeState(Objects.equals(prizeDO.getPrizeType(), 4) ? 1 : 0);
        exchangeDO.setClaimStatus(null);
        return exchangeDO;
    }

    /**
     * 组装奖品兑换结果。
     */
    private PrizeExchangeResultVO buildPrizeExchangeResult(ActivityJkPrizeExchangeDO exchangeDO) {
        PrizeExchangeResultVO resultVO = new PrizeExchangeResultVO();
        resultVO.setId(exchangeDO.getId());
        resultVO.setPrizeName(exchangeDO.getPrizeName());
        resultVO.setPrizeType(exchangeDO.getPrizeType());
        resultVO.setPrizeImgUrl(exchangeDO.getPrizeImgUrl());
        resultVO.setAwardId(exchangeDO.getAwardId());
        resultVO.setUserCouponId(exchangeDO.getUserCouponId());
        if (Objects.equals(exchangeDO.getPrizeType(), 1) && exchangeDO.getPrizeValue() != null) {
            resultVO.setPointAmount(exchangeDO.getPrizeValue().intValue());
        }
        if (Objects.equals(exchangeDO.getPrizeType(), 5) && exchangeDO.getPrizeValue() != null) {
            resultVO.setRedPacketAmount(exchangeDO.getPrizeValue());
        }
        resultVO.setPackageInfo(exchangeDO.getPackageInfo());
        resultVO.setOutBillNo(exchangeDO.getOutBillNo());
        return resultVO;
    }

    /**
     * 查询并校验红包发放所需的会员信息。
     */
    private WxMemberVO getValidJkWxMember(Long memberId) {
        WxMemberVO wxMemberVO = getJkMember(memberId);
        if (ObjectUtil.isEmpty(wxMemberVO.getOpenid())) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        return wxMemberVO;
    }

    /**
     * 查询集卡兑奖所需的会员信息。
     */
    private WxMemberVO getJkMember(Long memberId) {
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
        if (memberResult == null || !memberResult.isSuccess() || memberResult.getData() == null) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        return memberResult.getData();
    }

    /**
     * 构建集卡红包转账参数。
     */
    private LotteryRedPacketVo buildJkRedPacketVo(WxMemberVO wxMemberVO, ActivityJkPrizeDO prizeDO) {
        LotteryRedPacketVo redPacketVo = new LotteryRedPacketVo();
        redPacketVo.setOpenId(wxMemberVO.getOpenid());
        redPacketVo.setUserName(resolveMemberName(wxMemberVO));
        redPacketVo.setTransferAmount(prizeDO.getPrizeValue()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP).intValue()
                );
        redPacketVo.setTransferRemark("集卡活动红包");
        redPacketVo.setUserRecvPerception("集卡活动红包奖励");
        return redPacketVo;
    }

    /**
     * 判断红包转账是否成功发起。
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
     * 保存万能卡兑换记录。
     */
    private void saveUniversalExchangeRecord(Long activityId, ActivityCardLogDO universalCardLogDO, ActivityJkCardDO targetCard) {
        ActivityJkPrizeExchangeDO exchangeDO = new ActivityJkPrizeExchangeDO();
        exchangeDO.setActivityId(activityId);
        exchangeDO.setPrizeId(targetCard.getId());
        exchangeDO.setPrizeType(0);
        exchangeDO.setPrizeName("万能卡兑换  " + targetCard.getCardName());
        exchangeDO.setPrizeImgUrl(targetCard.getCardImgUrl());
        exchangeDO.setMemberId(universalCardLogDO.getMemberId());
        exchangeDO.setMemberName(universalCardLogDO.getMemberName());
        exchangeDO.setMemberMobile(universalCardLogDO.getMemberMobile());
        exchangeDO.setPrizeState(0);
   //     activityJkPrizeExchangeMapper.insert(exchangeDO);
    }


    /**
     * 组装抽卡记录返回对象。
     */
    private DrawRecordVO buildDrawRecordVO(ActivityCardLogDO cardLogDO) {
        DrawRecordVO recordVO = new DrawRecordVO();
        recordVO.setId(cardLogDO.getId());
        recordVO.setCardId(cardLogDO.getCardId());
        recordVO.setCardName(cardLogDO.getCardName());
        recordVO.setCardType(cardLogDO.getCardType());
        recordVO.setCardImgUrl(cardLogDO.getCardImgUrl());
        recordVO.setDrawTime(cardLogDO.getCreateTime() == null ? null : Date.from(cardLogDO.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        recordVO.setIsRare(Objects.equals(cardLogDO.getCardType(), 4));
        return recordVO;
    }

    /**
     * 组装兑奖记录返回对象。
     */
    private ExchangeRecordVO buildExchangeRecordVO(ActivityJkPrizeExchangeDO exchangeDO) {
        ExchangeRecordVO recordVO = new ExchangeRecordVO();
        recordVO.setId(exchangeDO.getId());
        recordVO.setPrizeId(exchangeDO.getPrizeId());
        recordVO.setPrizeName(exchangeDO.getPrizeName());
        recordVO.setPrizeType(exchangeDO.getPrizeType());
        recordVO.setPrizeImgUrl(exchangeDO.getPrizeImgUrl());
        recordVO.setExchangeTime(exchangeDO.getCreateTime() == null ? null : Date.from(exchangeDO.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        recordVO.setPrizeStatus(exchangeDO.getPrizeState());
        recordVO.setTrackingNumber(exchangeDO.getTrackingNumber());
        recordVO.setClaimStatus(exchangeDO.getClaimStatus());
        recordVO.setAwardId(exchangeDO.getAwardId());
        recordVO.setUserCouponId(exchangeDO.getUserCouponId());
        recordVO.setPackageInfo(exchangeDO.getPackageInfo());
        recordVO.setOutBillNo(exchangeDO.getOutBillNo());
        return recordVO;
    }

    /**
     * 记录集卡积分发放流水。
     */
    private void addJkPointsLog(Long activityId, int pointAmount, WxMemberVO wxMemberVO) {
        PointsLogDO pointsLogDO = new PointsLogDO();
        pointsLogDO.setMemberId(wxMemberVO.getMemberId());
        pointsLogDO.setPointsChange(Long.valueOf(pointAmount));
        pointsLogDO.setMemberName(wxMemberVO.getMemberName());
        pointsLogDO.setMemberNickName(wxMemberVO.getMemberNickName());
        pointsLogDO.setMemberMobile(wxMemberVO.getMemberMobile());
        pointsLogDO.setLogCode(generateJkNumber());
        long rawId = identifierGenerator.nextId(null).longValue();
        pointsLogDO.setPointsLogId(rawId);
        pointsLogDO.setPointsType(1);
        pointsLogDO.setIsDelete(0);
        pointsLogDO.setCreateTime(new Date());
        pointsLogDO.setPointsLogStatus(1);
        pointsLogDO.setIsPointsProduct(2);
        pointsLogDO.setProductType(3);
        pointsLogDO.setUpdateTime(new Date());
        pointsLogDO.setProductId(activityId);
        pointsLogDO.setShardingValue(wxMemberVO.getShardingValue());
        if (ObjectUtil.isNotEmpty(pointsLogDO.getShardingValue())) {
            String pointsLogId = rawId + "" + pointsLogDO.getShardingValue();
            BigInteger finalId = (new BigInteger(pointsLogId)).mod(BigInteger.valueOf(Long.MAX_VALUE));
            pointsLogDO.setPointsLogId(finalId.longValueExact());
        }
        lotteryAddLogService.addPointsLog(pointsLogDO);
    }

    /**
     * 按优惠券配置生成用户券有效期。
     */
    private void parseJkUserCouponTime(GoodCouponDO goodCouponDO, UserCouponDO userCouponDO) {
        if (Objects.equals(goodCouponDO.getUseType(), 0)) {
            userCouponDO.setExpirationTime(goodCouponDO.getCouponEndTime());
            userCouponDO.setVaildStartTime(goodCouponDO.getCouponStartTime());
        }
        if (Objects.equals(goodCouponDO.getUseType(), 1)) {
            userCouponDO.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(
                    LocalDate.now().plusDays(Integer.parseInt(goodCouponDO.getUseTime()) - 1),
                    DateUtils.YYYY_MM_DD
            );
            userCouponDO.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        if (Objects.equals(goodCouponDO.getUseType(), 2)) {
            String[] split = goodCouponDO.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(
                    LocalDate.now().plusDays(Integer.parseInt(split[0])),
                    DateUtils.YYYY_MM_DD
            );
            userCouponDO.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(
                    LocalDate.now().plusDays(Integer.parseInt(split[0])).plusDays(Integer.parseInt(split[1]) - 1),
                    DateUtils.YYYY_MM_DD
            );
            userCouponDO.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    /**
     * 生成集卡积分流水编号。
     */
    private String generateJkNumber() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 读取活动详情缓存。
     */
    private ActivityJkSaveOrUpdateReqVO getCachedJkSetting(Long activityId) {
        Object cachedObject = redisCache.getCacheObject(JKKeyConstants.JK_SETTING + activityId);
        if (cachedObject instanceof ActivityJkSaveOrUpdateReqVO reqVO) {
            return reqVO;
        }
        return null;
    }

    /**
     * 加锁加载活动详情缓存。
     */
    private ActivityJkSaveOrUpdateReqVO loadJkSettingWithLock(Long activityId) {
        String cacheKey = JKKeyConstants.JK_SETTING + activityId;
        RLock lock = redissonClient.getLock(JK_DETAIL_LOCK_PREFIX + activityId);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(activityId);
            if (cacheData != null) {
                return cacheData;
            }
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            ActivityDO activityDO = activityMapper.selectById(activityId);
            if (activityDO == null
                    || !Objects.equals(activityDO.getActivityType(), ActivityTypeEnum.JK.getCode())
                    || !Objects.equals(activityDO.getIsEnabled(), 1)) {
                return null;
            }
            Long businessId = BusinessContextHolder.getBusinessId();
            if (businessId != null && !Objects.equals(activityDO.getBusinessId(), businessId)) {
                return null;
            }

            ActivityJkDO activityJkDO = this.getOne(new LambdaQueryWrapperX<ActivityJkDO>()
                    .eq(ActivityJkDO::getActivityId, activityId)
                    .last("limit 1"));
            if (activityJkDO == null) {
                return null;
            }

            ActivityJkSaveOrUpdateReqVO reqVO = new ActivityJkSaveOrUpdateReqVO();
            BeanUtils.copyProperties(activityDO, reqVO);
            BeanUtils.copyProperties(activityJkDO, reqVO);
            reqVO.setId(activityId);
            reqVO.setActivityId(activityId);
            reqVO.setActivityName(activityDO.getActivityName());
            reqVO.setActivityRemark(activityDO.getActivityRemark());
            reqVO.setActivityRules(activityDO.getActivityRules());
            reqVO.setStartDate(DateTime.of(activityDO.getStartDate()));
            reqVO.setEndDate(DateTime.of(activityDO.getEndDate()));
            reqVO.setDayNumberList(parseIntegerList(activityDO.getDayNumbers()));
            reqVO.setWeekNumberList(parseIntegerList(activityDO.getWeekNumbers()));
            reqVO.setTimeRangeList(parseStringList(activityDO.getTimeRange()));
            redisCache.setCacheObject(cacheKey, reqVO, JK_SETTING_CACHE_SECONDS, TimeUnit.SECONDS);
            return reqVO;
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
     * 查询活动下的卡片列表。
     */
    private List<ActivityJkCardDO> getCardList(Long activityId) {
        return getOrLoadCardCache(activityId);
    }

    /**
     * 根据抽卡记录汇总当前用户在当前活动下仍可使用的卡片记录。
     * 这里只统计未使用的抽卡记录，便于后续兑换奖品时继续按记录ID做消耗。
     */
    private Map<Long, List<ActivityCardLogDO>> buildUserCardLogMap(Long activityId, Long memberId) {
        if (memberId == null) {
            return Collections.emptyMap();
        }
        List<ActivityCardLogDO> cardLogList = activityCardLogMapper.selectList(new LambdaQueryWrapperX<ActivityCardLogDO>()
                .eq(ActivityCardLogDO::getActivityId, activityId)
                .eq(ActivityCardLogDO::getMemberId, memberId)
                .eq(ActivityCardLogDO::getStatus, 0)
                .orderByAsc(ActivityCardLogDO::getCreateTime)
                .orderByAsc(ActivityCardLogDO::getId));
        if (cardLogList == null || cardLogList.isEmpty()) {
            return Collections.emptyMap();
        }
        return cardLogList.stream()
                .filter(item -> item.getCardId() != null)
                .collect(Collectors.groupingBy(ActivityCardLogDO::getCardId));
    }

    /**
     * 组装用户已持有的卡片列表。
     * 先获取活动下配置的全部卡片，再按用户抽卡记录汇总每张卡片的持有数量。
     * 如果用户未登录，则所有卡片数量都返回 0。
     */
    private List<UserCardVO> buildUserCardList(List<ActivityJkCardDO> activityCardList, Map<Long, List<ActivityCardLogDO>> userCardLogMap) {
        return activityCardList.stream()
                .map(cardDO -> {
                    List<ActivityCardLogDO> cardLogList = userCardLogMap.getOrDefault(cardDO.getId(), Collections.emptyList());
                    UserCardVO userCardVO = new UserCardVO();
                    userCardVO.setCardId(cardDO.getId());
                    userCardVO.setCardName(cardDO.getCardName());
                    userCardVO.setCardType(cardDO.getCardType());
                    userCardVO.setCardImgUrl(cardDO.getCardImgUrl());
                    userCardVO.setCardSort(cardDO.getCardSort());
                    userCardVO.setCount(cardLogList.size());
                    return userCardVO;
                })
                .sorted(Comparator.comparing(UserCardVO::getCardSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(UserCardVO::getCardId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    /**
     * 统计可用卡片记录数量。
     */
    private Integer getAvailableRecordCount(List<ActivityCardLogDO> cardLogList) {
        return cardLogList == null ? 0 : cardLogList.size();
    }

    /**
     * 查询奖品缓存列表。
     */
    private List<ActivityJkPrizeDO> getPrizeList(Long activityId) {
        return getOrLoadPrizeCache(activityId);
    }

    /**
     * 按兑换类型组装奖品列表。
     * 1 表示套卡兑换，2 表示隐藏卡兑换。
     */
    private List<PrizeVO> buildPrizeVOList(List<ActivityJkPrizeDO> prizeDOList, Integer exchangeType,
                                           Long activityId, Long memberId,
                                           Map<Long, List<ActivityCardLogDO>> userCardLogMap) {
        if (prizeDOList == null || prizeDOList.isEmpty()) {
            return Collections.emptyList();
        }
        return prizeDOList.stream()
                .filter(prizeDO -> Objects.equals(prizeDO.getExchangeConditions(), exchangeType))
                .map(prizeDO -> convertPrizeVO(prizeDO, activityId, memberId, userCardLogMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换奖品返回对象。
     */
    private PrizeVO convertPrizeVO(ActivityJkPrizeDO prizeDO, Long activityId, Long memberId,
                                   Map<Long, List<ActivityCardLogDO>> userCardLogMap) {
        PrizeVO prizeVO = new PrizeVO();
        prizeVO.setId(prizeDO.getId());
        prizeVO.setPrizeName(prizeDO.getPrizeName());
        prizeVO.setPrizeType(prizeDO.getPrizeType());
        prizeVO.setPrizeImgUrl(prizeDO.getPrizeImgUrl());
        prizeVO.setPrizeValue(prizeDO.getPrizeValue() == null ? null :  prizeDO.getPrizeValue() );
        prizeVO.setTotalStock(prizeDO.getPrizeNum());
        prizeVO.setExchangedCount(defaultZero(prizeDO.getRemainNum()));
        prizeVO.setRemainStock(calculatePrizeRemainStock(prizeDO));
        prizeVO.setExchangeType(prizeDO.getExchangeConditions());
        prizeVO.setNeedCardCount(calculateNeedCardCount(activityId, prizeDO, userCardLogMap));
        prizeVO.setNeedCardIds(buildNeedCardIds(activityId, prizeDO));
        prizeVO.setExchangeLimit(Objects.equals(prizeDO.getExchangeRestrictions(), 1) ? prizeDO.getExchangeCount() : 0);
        prizeVO.setUserExchangeCount(calculateUserExchangeCount(activityId, memberId, prizeDO.getId()));
        prizeVO.setUserCanExchange(calculateUserCanExchange(prizeVO, memberId));
        return prizeVO;
    }

    /**
     * 计算奖品剩余库存。
     */
    private Integer calculatePrizeRemainStock(ActivityJkPrizeDO prizeDO) {
        Integer totalStock = defaultZero(prizeDO.getPrizeNum());
        Integer exchangedCount = defaultZero(prizeDO.getRemainNum());
        return Math.max(totalStock - exchangedCount, 0);
    }

    /**
     * 计算兑奖还差的卡片数量。
     */
    private Integer calculateNeedCardCount(Long activityId, ActivityJkPrizeDO prizeDO,
                                           Map<Long, List<ActivityCardLogDO>> userCardLogMap) {
        if (Objects.equals(prizeDO.getExchangeConditions(), 2)) {
            long hiddenCardCount = userCardLogMap.values().stream()
                    .flatMap(List::stream)
                    .filter(cardLogDO -> Objects.equals(cardLogDO.getCardType(), 4))
                    .count();
            return hiddenCardCount > 0 ? 0 : 1;
        }
        if (!Objects.equals(prizeDO.getExchangeConditions(), 1)) {
            return 0;
        }
        List<ActivityJkCardDO> setCardList = getCardList(activityId).stream()
                .filter(cardDO -> Objects.equals(cardDO.getCardType(), 2))
                .collect(Collectors.toList());
        if (setCardList.isEmpty()) {
            return 0;
        }
        int ownedSetCardCount = (int) setCardList.stream()
                .filter(cardDO -> getAvailableRecordCount(userCardLogMap.get(cardDO.getId())) > 0)
                .count();
        return Math.max(setCardList.size() - ownedSetCardCount, 0);
    }

    /**
     * 构建兑奖所需卡片ID列表。
     */
    private List<Long> buildNeedCardIds(Long activityId, ActivityJkPrizeDO prizeDO) {
        if (Objects.equals(prizeDO.getExchangeConditions(), 1)) {
            return getCardList(activityId).stream()
                    .filter(cardDO -> Objects.equals(cardDO.getCardType(), 2))
                    .map(ActivityJkCardDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        if (Objects.equals(prizeDO.getExchangeConditions(), 2)) {
            return getCardList(activityId).stream()
                    .filter(cardDO -> Objects.equals(cardDO.getCardType(), 4))
                    .map(ActivityJkCardDO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 统计用户已兑换次数。
     */
    private Integer calculateUserExchangeCount(Long activityId, Long memberId, Long prizeId) {
        if (activityId == null || memberId == null || prizeId == null) {
            return 0;
        }
        Long count = activityJkPrizeExchangeMapper.selectCount(new LambdaQueryWrapperX<ActivityJkPrizeExchangeDO>()
                .eq(ActivityJkPrizeExchangeDO::getActivityId, activityId)
                .eq(ActivityJkPrizeExchangeDO::getMemberId, memberId)
                .eq(ActivityJkPrizeExchangeDO::getPrizeId, prizeId));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 计算当前用户是否满足奖品兑换条件。
     */
    private Boolean calculateUserCanExchange(PrizeVO prizeVO, Long memberId) {
        Integer exchangeLimit = defaultZero(prizeVO.getExchangeLimit());
        if (exchangeLimit <= 0) {
            return true;
        }
        return defaultZero(prizeVO.getUserExchangeCount()) < exchangeLimit;
    }


    /**
     * 读取或回源加载卡片缓存。
     */
    private List<ActivityJkCardDO> getOrLoadCardCache(Long activityId) {
        return getOrLoadListCache(
                JKKeyConstants.JK_CARD + activityId,
                JK_LIST_LOCK_PREFIX + "card:" + activityId,
                () -> activityJkCardMapper.selectList(new LambdaQueryWrapperX<ActivityJkCardDO>()
                        .eq(ActivityJkCardDO::getActivityId, activityId)
                        .orderByAsc(ActivityJkCardDO::getCardSort))
        );
    }


    /**
     * 读取或回源加载奖品缓存。
     */
    private List<ActivityJkPrizeDO> getOrLoadPrizeCache(Long activityId) {
        return getOrLoadListCache(
                JKKeyConstants.JK_PRIZE + activityId,
                JK_LIST_LOCK_PREFIX + "prize:" + activityId,
                () -> activityJkPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityJkPrizeDO>()
                        .eq(ActivityJkPrizeDO::getActivityId, activityId))
        );
    }

    /**
     * 转换详情缓存为详情返回对象。
     */
    private ActivityJkDetailVO convertReqCacheToDetail(ActivityJkSaveOrUpdateReqVO reqVO, Long activityId) {
        ActivityJkDetailVO detailVO = new ActivityJkDetailVO();
        BeanUtils.copyProperties(reqVO, detailVO);
        detailVO.setActivityId(ObjectUtil.defaultIfNull(reqVO.getActivityId(), activityId));
        detailVO.setActivityTitle(reqVO.getActivityName());
        detailVO.setActivityStartTime(reqVO.getStartDate());
        detailVO.setActivityEndTime(reqVO.getEndDate());
        detailVO.setActivityRule(reqVO.getActivityRules());
        detailVO.setPaymentThreshold(reqVO.getPaymentCount());
        return detailVO;
    }

    /**
     * 转换卡片配置列表。
     */
    private List<ActivityJkCardDO> convertCardList(List<ActivityJkCardReqVO> cardReqVOList, Long activityId) {
        if (cardReqVOList == null || cardReqVOList.isEmpty()) {
            return new ArrayList<>();
        }
        return cardReqVOList.stream().map(item -> {
            ActivityJkCardDO cardDO = BeanUtils.toBean(item, ActivityJkCardDO.class);
            cardDO.setActivityId(activityId);
            return cardDO;
        }).collect(Collectors.toList());
    }

    /**
     * 转换奖品配置列表。
     */
    private List<ActivityJkPrizeDO> convertPrizeList(List<ActivityJkPrizeReqVO> prizeReqVOList, Long activityId) {
        if (prizeReqVOList == null || prizeReqVOList.isEmpty()) {
            return new ArrayList<>();
        }
        return prizeReqVOList.stream().map(item -> {
            ActivityJkPrizeDO prizeDO = BeanUtils.toBean(item, ActivityJkPrizeDO.class);
            prizeDO.setActivityId(activityId);
            return prizeDO;
        }).collect(Collectors.toList());
    }

    /**
     * 计算活动状态。
     */
    private Integer calculateActivityStatus(int isEnabled ,Date startDate, Date endDate) {
        if (isEnabled==0) {
            return 0;
        }
        if (startDate == null || endDate == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (now < startDate.getTime()) {
            return 0;
        }
        long endTime = endDate.getTime() + 24 * 60 * 60 * 1000;

        if (now >= endTime) {
            return 2;
        }
        return 1;
    }

    /**
     * 解析整数列表字符串。
     */
    private List<Integer> parseIntegerList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(value.split(","))
                .filter(item -> item != null && !item.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 解析字符串列表。
     */
    private List<String> parseStringList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(value.split(","))
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * 将列表转换为逗号分隔字符串。
     */
    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * 组装单个任务返回对象。
     */
    private void buildTaskVO(List<TaskVO> taskList, Long activityId, Long memberId,
                             Integer enabledFlag, Integer taskLimit, ActivityJkTaskTypeEnum taskTypeEnum,BigDecimal amount) {
        if (!isTaskEnabled(enabledFlag)) {
            return;
        }

        int limit = taskLimit == null ? TASK_LIMIT_UNLIMITED : taskLimit;
        ActivityJkTaskDO taskDO = getOrInitTaskRecord(activityId, memberId, taskTypeEnum, limit);
        TaskVO taskVO = new TaskVO();
        taskVO.setTaskType(taskTypeEnum.getCode());
        taskVO.setTaskLimit(limit);
        taskVO.setAmount(amount);
        taskVO.setFinishCount(defaultZero(taskDO.getFinishCount()));
        taskList.add(taskVO);
    }

    /**
     * 查询或初始化任务记录。
     */
    private ActivityJkTaskDO getOrInitTaskRecord(Long activityId, Long memberId,
                                                 ActivityJkTaskTypeEnum taskTypeEnum, Integer taskLimit) {
        ActivityJkTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO != null) {
            return taskDO;
        }

        // 不限制次数的任务只有一条累计记录，直接补一条初始化数据即可。
        if (isUnlimitedLimit(taskLimit)) {
            return insertInitTaskRecord(activityId, memberId, taskTypeEnum, taskLimit);
        }

        // 有次数限制的任务按当天展示。
        // 由于表上有 activityId + memberId + taskType 唯一索引，跨天后不能重复插入，
        // 这里复用历史记录并重置为当天 0 次，避免再次插入触发唯一键冲突。
        ActivityJkTaskDO latestTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
        if (latestTaskDO == null) {
            return insertInitTaskRecord(activityId, memberId, taskTypeEnum, taskLimit);
        }
        latestTaskDO.setFinishCount(initTaskFinishCount(taskTypeEnum, taskLimit));
        latestTaskDO.setGainCount(initTaskGainCount(taskTypeEnum, taskLimit));
        latestTaskDO.setConsumeCount(0);
        latestTaskDO.setUpdateTime(LocalDateTime.now());
        updateTaskRecord(latestTaskDO);
        return latestTaskDO;
    }

    /**
     * 插入一条初始化任务记录。
     */
    private ActivityJkTaskDO insertInitTaskRecord(Long activityId, Long memberId, ActivityJkTaskTypeEnum taskTypeEnum, Integer taskLimit) {
        ActivityJkTaskDO initTaskDO = new ActivityJkTaskDO();
        initTaskDO.setActivityId(activityId);
        initTaskDO.setMemberId(memberId);
        initTaskDO.setTaskType(taskTypeEnum.getCode());
        initTaskDO.setFinishCount(initTaskFinishCount(taskTypeEnum, taskLimit));
        initTaskDO.setGainCount(initTaskGainCount(taskTypeEnum, taskLimit));
        initTaskDO.setConsumeCount(0);
        try {
            activityJkTaskMapper.insert(initTaskDO);
            return initTaskDO;
        } catch (DuplicateKeyException e) {
            ActivityJkTaskDO latestTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
            if (latestTaskDO == null) {
                throw e;
            }
            return latestTaskDO;
        }
    }

    private int initTaskFinishCount(ActivityJkTaskTypeEnum taskTypeEnum, Integer taskLimit) {
        return 0;
    }

    private int initTaskGainCount(ActivityJkTaskTypeEnum taskTypeEnum, Integer taskLimit) {
        return 0;
    }

    /**
     * 查询任务记录。
     * 次数限制为 0 时查累计记录，存在次数限制时只查当天记录。
     */
    private ActivityJkTaskDO getTaskRecord(Long activityId, Long memberId, Integer taskType, Integer taskLimit) {
        LambdaQueryWrapperX<ActivityJkTaskDO> queryWrapper = new LambdaQueryWrapperX<ActivityJkTaskDO>()
                .eq(ActivityJkTaskDO::getActivityId, activityId)
                .eq(ActivityJkTaskDO::getMemberId, memberId)
                .eq(ActivityJkTaskDO::getTaskType, taskType)
                .orderByDesc(ActivityJkTaskDO::getUpdateTime)
                .orderByDesc(ActivityJkTaskDO::getCreateTime)
                .orderByDesc(ActivityJkTaskDO::getId)
                .last("limit 1");
        if (!isUnlimitedLimit(taskLimit)) {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime tomorrowStart = todayStart.plusDays(1);
            queryWrapper.ge(ActivityJkTaskDO::getUpdateTime, todayStart)
                    .lt(ActivityJkTaskDO::getUpdateTime, tomorrowStart);
        }
        return activityJkTaskMapper.selectOne(queryWrapper);
    }

    /**
     * 查询当前任务的最新一条记录，不加当天时间限制。
     */
    private ActivityJkTaskDO getLatestTaskRecord(Long activityId, Long memberId, Integer taskType) {
        return activityJkTaskMapper.selectOne(new LambdaQueryWrapperX<ActivityJkTaskDO>()
                .eq(ActivityJkTaskDO::getActivityId, activityId)
                .eq(ActivityJkTaskDO::getMemberId, memberId)
                .eq(ActivityJkTaskDO::getTaskType, taskType)
                .orderByDesc(ActivityJkTaskDO::getUpdateTime)
                .orderByDesc(ActivityJkTaskDO::getCreateTime)
                .orderByDesc(ActivityJkTaskDO::getId)
                .last("limit 1"));
    }

    /**
     * 判断任务是否开启。
     */
    private boolean isTaskEnabled(Integer enabledFlag) {
        return Objects.equals(enabledFlag, 1);
    }

    /**
     * 判断任务次数是否不限制。
     */
    private boolean isUnlimitedLimit(Integer taskLimit) {
        return taskLimit == null || taskLimit == TASK_LIMIT_UNLIMITED;
    }

    /**
     * 空值按零处理。
     */
    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * 读取抽卡次数缓存。
     * 这里只兼容常见的数字类型，读取失败时按未命中处理。
     */
    private Integer getCachedDrawChanceCount(String cacheKey) {
        Object cachedObject = redisCache.getCacheObject(cacheKey);
        if (cachedObject instanceof Integer integerValue) {
            return integerValue;
        }
        if (cachedObject instanceof Long longValue) {
            return longValue.intValue();
        }
        if (cachedObject instanceof Number numberValue) {
            return numberValue.intValue();
        }
        return null;
    }

    /**
     * 查询并校验当前可用抽卡次数。
     * 如果缓存未命中，会先刷新缓存后再读取。
     */
    private int getValidatedDrawChanceCount(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        Integer dailyChanceCount;
        Integer unlimitedChanceCount;
        try {
            dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));
            unlimitedChanceCount = getCachedDrawChanceCount(buildUnlimitedChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));

            // 抽卡次数缓存未命中时，先按当前任务和抽卡记录刷新一次缓存，再重新读取。
            if (dailyChanceCount == null && unlimitedChanceCount != null) {
                refreshDailyDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
                dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));
            } else if (dailyChanceCount == null || unlimitedChanceCount == null) {
                refreshDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
                dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));
                unlimitedChanceCount = getCachedDrawChanceCount(buildUnlimitedChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));
            }
        } catch (Exception e) {
            forceRefreshDrawChanceCache(reqVO.getActivityId(), reqVO.getMemberId());
            dailyChanceCount = getCachedDrawChanceCount(buildDailyChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));
            unlimitedChanceCount = getCachedDrawChanceCount(buildUnlimitedChanceCacheKey(reqVO.getActivityId(), reqVO.getMemberId()));
        }
        return defaultZero(dailyChanceCount) + defaultZero(unlimitedChanceCount);
    }

    /**
     * 校验集卡活动门店权限。
     * 只有活动配置为指定门店参与时，才要求当前门店必须在活动门店列表中。
     */ 
    private void validateJkStore(Long activityId, Long storeId) {
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (activityDO == null || !Objects.equals(activityDO.getActivityStore(), 0)) {
            return;
        }
        if (storeId == null) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
        boolean matched = activityStoreService.count(new LambdaQueryWrapperX<ActivityStoreDO>()
                .eq(ActivityStoreDO::getActivityId, activityId)
                .eq(ActivityStoreDO::getStoreId, storeId)) > 0;
        if (!matched) {
            throw exception(LOTTERY_NOT_STORE);
        }
    }

    /**
     * 完成助力分享任务。
     * 当前请求中的 memberId 代表被邀请来助力的用户，inviterMemberId 代表发起分享的邀请人。
     */
    private void completeShareHelp(ActivityJkReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null || reqVO.getInviterMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        if (Objects.equals(reqVO.getMemberId(), reqVO.getInviterMemberId())) {
            throw exception(JK_HELP_SELF_NOT_ALLOW);
        }
        ActivityJkSaveOrUpdateReqVO cacheData = getValidatedShareHelpActivity(reqVO.getActivityId());
        if (!isTaskEnabled(cacheData.getShareEvent())) {
            throw exception(JK_TASK_NOT_ENABLED);
        }

        RLock inviterLock = redissonClient.getLock(JK_HELP_LOCK_PREFIX + "inviter:" + reqVO.getActivityId() + ":" + reqVO.getInviterMemberId());
        RLock inviteeLock = redissonClient.getLock(JK_HELP_LOCK_PREFIX + "invitee:" + reqVO.getActivityId() + ":" + reqVO.getMemberId());
        RLock helpLock = redissonClient.getMultiLock(inviterLock, inviteeLock);
        boolean locked = false;
        try {
            locked = helpLock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            LocalDate today = LocalDate.now();
            validateDailyHelpLimit(reqVO.getActivityId(), reqVO.getInviterMemberId(), reqVO.getMemberId(), today);

            int shareLimit = defaultZero(cacheData.getShareCount());
            ActivityJkTaskDO inviterTaskDO = getTaskRecord(reqVO.getActivityId(), reqVO.getInviterMemberId(), ActivityJkTaskTypeEnum.SHARE.getCode(), shareLimit);
            int currentFinishCount = inviterTaskDO == null ? 0 : defaultZero(inviterTaskDO.getFinishCount());
            if (!isUnlimitedLimit(shareLimit) && currentFinishCount >= shareLimit) {
                throw exception(JK_HELP_TARGET_LIMIT_REACHED);
            }

            ActivityJkHelpDO helpDO = new ActivityJkHelpDO();
            helpDO.setActivityId(reqVO.getActivityId());
            helpDO.setInviterMemberId(reqVO.getInviterMemberId());
            helpDO.setInviteeMemberId(reqVO.getMemberId());
            helpDO.setHelpDate(today);
            activityJkHelpMapper.insert(helpDO);
            syncHelpCacheAfterInsert(helpDO);
            int rewardCount = isSevenDayNewMember(reqVO.getMemberId()) ? 2 : 1;
            saveOrUpdateTaskRecord(reqVO.getActivityId(), reqVO.getInviterMemberId(), ActivityJkTaskTypeEnum.SHARE, shareLimit, rewardCount);
            refreshTaskGainCache(reqVO.getActivityId(), reqVO.getInviterMemberId());
            tryAwardHelperShareReward(reqVO.getActivityId(), reqVO.getMemberId(), shareLimit, rewardCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && helpLock.isHeldByCurrentThread()) {
                helpLock.unlock();
            }
        }
    }
    /**
     * 校验助力限制。
     * 1. 每天不能重复给同一个邀请人助力
     * 2. 每个用户每天最多成功助力 5 次
     */
    private void validateDailyHelpLimit(Long activityId, Long inviterMemberId, Long inviteeMemberId, LocalDate helpDate) {
        List<ActivityJkHelpDO> helpDOList = getHelpListWithCache(activityId, inviteeMemberId, helpDate);
        ActivityJkHelpDO repeatedHelp = helpDOList.stream()
                .filter(item -> Objects.equals(item.getInviterMemberId(), inviterMemberId))
                .findFirst()
                .orElse(null);
        if (repeatedHelp != null) {
            throw exception(JK_HELP_REPEAT);
        }

        long helpCount = helpDOList.size();
        if (helpCount >= 5) {
            throw exception(JK_HELP_DAILY_LIMIT);
        }
    }

    /**
     * 优先从缓存读取当天助力记录，缓存未命中时再查询表并回写缓存。
     */
    private List<ActivityJkHelpDO> getHelpListWithCache(Long activityId, Long inviteeMemberId, LocalDate helpDate) {
        String cacheKey = buildHelpCacheKey(activityId, inviteeMemberId, helpDate);
        try {
            Object cachedObject = redisCache.getCacheObject(cacheKey);
            if (cachedObject instanceof List<?> cachedList) {
                return castHelpList(cachedList);
            }
        } catch (Exception e) {
            log.warn("集卡助力缓存读取失败，回表查询，activityId={}，inviteeMemberId={}", activityId, inviteeMemberId, e);
        }

        List<ActivityJkHelpDO> helpDOList = activityJkHelpMapper.selectList(new LambdaQueryWrapperX<ActivityJkHelpDO>()
                .eq(ActivityJkHelpDO::getActivityId, activityId)
                .eq(ActivityJkHelpDO::getInviteeMemberId, inviteeMemberId)
                .eq(ActivityJkHelpDO::getHelpDate, helpDate)
                .orderByAsc(ActivityJkHelpDO::getCreateTime)
                .orderByAsc(ActivityJkHelpDO::getId));
        List<ActivityJkHelpDO> safeList = new ArrayList<>(helpDOList);
        try {
            redisCache.setCacheObject(cacheKey, safeList, getHelpCacheSeconds(helpDate), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("集卡助力缓存写入失败，activityId={}，inviteeMemberId={}", activityId, inviteeMemberId, e);
        }
        return safeList;
    }

    /**
     * 助力记录写表成功后同步更新缓存，更新失败时删除缓存让下次请求回表重建。
     */
    private void syncHelpCacheAfterInsert(ActivityJkHelpDO helpDO) {
        String cacheKey = buildHelpCacheKey(helpDO.getActivityId(), helpDO.getInviteeMemberId(), helpDO.getHelpDate());
        try {
            Object cachedObject = redisCache.getCacheObject(cacheKey);
            List<ActivityJkHelpDO> latestList;
            if (cachedObject instanceof List<?> cachedList) {
                latestList = castHelpList(cachedList);
                boolean exists = latestList.stream().anyMatch(item -> Objects.equals(item.getId(), helpDO.getId()));
                if (!exists) {
                    latestList.add(helpDO);
                }
            } else {
                latestList = activityJkHelpMapper.selectList(new LambdaQueryWrapperX<ActivityJkHelpDO>()
                        .eq(ActivityJkHelpDO::getActivityId, helpDO.getActivityId())
                        .eq(ActivityJkHelpDO::getInviteeMemberId, helpDO.getInviteeMemberId())
                        .eq(ActivityJkHelpDO::getHelpDate, helpDO.getHelpDate())
                        .orderByAsc(ActivityJkHelpDO::getCreateTime)
                        .orderByAsc(ActivityJkHelpDO::getId));
            }
            redisCache.setCacheObject(cacheKey, latestList, getHelpCacheSeconds(helpDO.getHelpDate()), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("集卡助力缓存同步失败，删除缓存等待回表重建，activityId={}，inviteeMemberId={}",
                    helpDO.getActivityId(), helpDO.getInviteeMemberId(), e);
            redisCache.deleteObject(cacheKey);
        }
    }

    /**
     * 构建助力记录缓存键。
     */
    private String buildHelpCacheKey(Long activityId, Long inviteeMemberId, LocalDate helpDate) {
        return JKKeyConstants.JK_HELP + activityId + ":" + inviteeMemberId + ":" + helpDate;
    }

    /**
     * 计算助力缓存的剩余有效秒数，缓存只保留到当天结束。
     */
    private Integer getHelpCacheSeconds(LocalDate helpDate) {
        LocalDate targetDate = helpDate == null ? LocalDate.now() : helpDate;
        LocalDateTime expireTime = targetDate.plusDays(1).atStartOfDay();
        long seconds = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        return Math.toIntExact(Math.max(seconds, 1L));
    }

    /**
     * 将缓存对象安全转换为助力记录列表。
     */
    private List<ActivityJkHelpDO> castHelpList(List<?> cachedList) {
        List<ActivityJkHelpDO> helpDOList = new ArrayList<>();
        for (Object item : cachedList) {
            if (item instanceof ActivityJkHelpDO helpDO) {
                helpDOList.add(helpDO);
            }
        }
        return helpDOList;
    }

    /**
     * 完成签到或分享任务。
     * 有次数上限时按当天筛选，无上限时按累计筛选。
     */
    private void completeTask(ActivityJkReqVO reqVO, ActivityJkTaskTypeEnum taskTypeEnum, int rewardCount) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        ActivityJkSaveOrUpdateReqVO cacheData = getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true);

        Integer enabledFlag = resolveTaskEnabledFlag(cacheData, taskTypeEnum);
        if (!isTaskEnabled(enabledFlag)) {
            throw exception(JK_TASK_NOT_ENABLED);
        }
        int taskLimit = defaultZero(resolveTaskLimit(cacheData, taskTypeEnum));
        saveOrUpdateTaskRecord(reqVO.getActivityId(), reqVO.getMemberId(), taskTypeEnum, taskLimit, rewardCount);
        refreshTaskGainCache(reqVO.getActivityId(), reqVO.getMemberId());
    }

    /**
     * 助力成功后，助力人自己也获得额外次数。
     * 这里不计入助力人自己的分享任务完成数，只增加可用次数。
     */
    private void tryAwardHelperShareReward(Long activityId, Long memberId, Integer shareLimit, int rewardCount) {
        saveOrUpdateTaskExtraGain(activityId, memberId, ActivityJkTaskTypeEnum.SHARE, shareLimit, rewardCount);
        refreshTaskGainCache(activityId, memberId);
    }

    /**
     * 首次完成任务时插入一条初始化记录，并直接记为完成 1 次、获得对应次数。
     */
    private boolean insertFirstTaskRecord(Long activityId, Long memberId, ActivityJkTaskTypeEnum taskTypeEnum, int rewardCount) {
        ActivityJkTaskDO initTaskDO = new ActivityJkTaskDO();
        initTaskDO.setActivityId(activityId);
        initTaskDO.setMemberId(memberId);
        initTaskDO.setTaskType(taskTypeEnum.getCode());
        initTaskDO.setFinishCount(1);
        initTaskDO.setGainCount(rewardCount);
        initTaskDO.setConsumeCount(0);
        try {
            activityJkTaskMapper.insert(initTaskDO);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    /**
     * 任务记录写入规则。
     * 1. 不限制次数时，直接查累计记录并更新
     * 2. 有次数限制时，只查当天记录；当天存在则更新，不存在则复用历史记录重置为当天数据
     */
    private void saveOrUpdateTaskRecord(Long activityId, Long memberId,
                                        ActivityJkTaskTypeEnum taskTypeEnum, Integer taskLimit, int rewardCount) {
        ActivityJkTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO == null) {
            if (isUnlimitedLimit(taskLimit)) {
                if (!insertFirstTaskRecord(activityId, memberId, taskTypeEnum, rewardCount)) {
                    saveOrUpdateTaskRecord(activityId, memberId, taskTypeEnum, taskLimit, rewardCount);
                }
                return;
            }

            ActivityJkTaskDO latestTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
            if (latestTaskDO == null) {
                if (!insertFirstTaskRecord(activityId, memberId, taskTypeEnum, rewardCount)) {
                    saveOrUpdateTaskRecord(activityId, memberId, taskTypeEnum, taskLimit, rewardCount);
                }
                return;
            }

            latestTaskDO.setFinishCount(1);
            latestTaskDO.setGainCount(rewardCount);
            latestTaskDO.setConsumeCount(0);
            latestTaskDO.setUpdateTime(LocalDateTime.now());
            updateTaskRecord(latestTaskDO);
            return;
        }

        int currentFinishCount = defaultZero(taskDO.getFinishCount());
        if (!isUnlimitedLimit(taskLimit) && currentFinishCount >= taskLimit) {
            throw exception(JK_TASK_LIMIT_REACHED);
        }
        taskDO.setFinishCount(currentFinishCount + 1);
        taskDO.setGainCount(defaultZero(taskDO.getGainCount()) + rewardCount);
        taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()));
        updateTaskRecord(taskDO);
    }

    /**
     * 仅增加任务获得次数，不增加任务完成数。
     * 用于“助力人获得额外次数”这类奖励场景。
     */
    private void saveOrUpdateTaskExtraGain(Long activityId, Long memberId,
                                           ActivityJkTaskTypeEnum taskTypeEnum, Integer taskLimit, int rewardCount) {
        ActivityJkTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO == null) {
            if (isUnlimitedLimit(taskLimit)) {
                if (!insertTaskExtraGainRecord(activityId, memberId, taskTypeEnum, rewardCount)) {
                    saveOrUpdateTaskExtraGain(activityId, memberId, taskTypeEnum, taskLimit, rewardCount);
                }
                return;
            }

            ActivityJkTaskDO latestTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
            if (latestTaskDO == null) {
                if (!insertTaskExtraGainRecord(activityId, memberId, taskTypeEnum, rewardCount)) {
                    saveOrUpdateTaskExtraGain(activityId, memberId, taskTypeEnum, taskLimit, rewardCount);
                }
                return;
            }

            latestTaskDO.setFinishCount(0);
            latestTaskDO.setGainCount(rewardCount);
            latestTaskDO.setConsumeCount(0);
            latestTaskDO.setUpdateTime(LocalDateTime.now());
            updateTaskRecord(latestTaskDO);
            return;
        }

        taskDO.setFinishCount(defaultZero(taskDO.getFinishCount()));
        taskDO.setGainCount(defaultZero(taskDO.getGainCount()) + rewardCount);
        taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()));
        updateTaskRecord(taskDO);
    }

    /**
     * 插入一条仅包含额外次数的任务记录。
     */
    private boolean insertTaskExtraGainRecord(Long activityId, Long memberId,
                                              ActivityJkTaskTypeEnum taskTypeEnum, int rewardCount) {
        ActivityJkTaskDO initTaskDO = new ActivityJkTaskDO();
        initTaskDO.setActivityId(activityId);
        initTaskDO.setMemberId(memberId);
        initTaskDO.setTaskType(taskTypeEnum.getCode());
        initTaskDO.setFinishCount(0);
        initTaskDO.setGainCount(rewardCount);
        initTaskDO.setConsumeCount(0);
        try {
            activityJkTaskMapper.insert(initTaskDO);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    /**
     * 刷新用户当前活动的抽卡次数缓存。
     * 这里会分别维护不限次数和当天限制次数两类缓存。
     */
    @Override
    public void refreshDrawChanceCache(Long activityId, Long memberId) {
        rebuildDrawChanceCache(activityId, memberId, true);
    }

    /**
     * 任务完成后只刷新可抽次数缓存，不在这里同步消耗次数。
     */
    private void refreshTaskGainCache(Long activityId, Long memberId) {
        rebuildDrawChanceCache(activityId, memberId, false);
    }

    /**
     * 第二天当天次数缓存过期时，只刷新当天次数，保留不限制次数的剩余缓存。
     */
    private void refreshDailyDrawChanceCache(Long activityId, Long memberId) {
        if (activityId == null || memberId == null) {
            return;
        }
        ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(activityId);
        if (cacheData == null) {
            cacheData = loadJkSettingWithLock(activityId);
        }
        if (cacheData == null) {
            return;
        }

        int todayDrawCount = countTodayDrawCount(activityId, memberId);
        List<ActivityJkTaskDO> limitedTaskList = buildLimitedTaskList(activityId, memberId, cacheData);
        int freeChanceCount = resolveFreeChanceCount(cacheData);
        int freeConsumedCount = Math.min(Math.max(todayDrawCount, 0), freeChanceCount);
        int targetLimitedConsumeCount = Math.min(Math.max(todayDrawCount - freeConsumedCount, 0), sumGainCount(limitedTaskList));

        if (sumConsumeCount(limitedTaskList) != targetLimitedConsumeCount) {
            syncTaskConsumeCount(limitedTaskList, targetLimitedConsumeCount);
        }

        int dailyTaskAvailableCount = limitedTaskList.stream()
                .mapToInt(task -> Math.max(defaultZero(task.getGainCount()) - defaultZero(task.getConsumeCount()), 0))
                .sum();
        int dailyAvailableCount = Math.max(freeChanceCount - freeConsumedCount, 0) + dailyTaskAvailableCount;

        redisCache.setCacheObject(buildDailyChanceCacheKey(activityId, memberId), dailyAvailableCount,
                Math.toIntExact(calculateEndOfDaySeconds()), TimeUnit.SECONDS);
    }

    /**
     * 按需重建抽卡次数缓存。
     * 抽卡链路会同步消耗次数，任务链路只刷新新增次数。
     */
    private void rebuildDrawChanceCache(Long activityId, Long memberId, boolean syncConsumeCount) {
        if (activityId == null || memberId == null) {
            return;
        }
        ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(activityId);
        if (cacheData == null) {
            cacheData = loadJkSettingWithLock(activityId);
        }
        if (cacheData == null) {
            return;
        }

        int todayDrawCount = countTodayDrawCount(activityId, memberId);
        List<ActivityJkTaskDO> limitedTaskList = buildLimitedTaskList(activityId, memberId, cacheData);
        List<ActivityJkTaskDO> unlimitedTaskList = buildUnlimitedTaskList(activityId, memberId, cacheData);
        int freeChanceCount = resolveFreeChanceCount(cacheData);
        int freeConsumedCount = Math.min(Math.max(todayDrawCount, 0), freeChanceCount);

        // 只用当天抽卡数同步当天限制任务；不限次数是跨天累计权益，只能在实际消耗时递增落库。
        int targetLimitedConsumeCount = Math.min(Math.max(todayDrawCount - freeConsumedCount, 0), sumGainCount(limitedTaskList));
        if (syncConsumeCount && sumConsumeCount(limitedTaskList) != targetLimitedConsumeCount) {
            syncTaskConsumeCount(limitedTaskList, targetLimitedConsumeCount);
        }

        int unlimitedAvailableCount = unlimitedTaskList.stream()
                .mapToInt(task -> Math.max(defaultZero(task.getGainCount()) - defaultZero(task.getConsumeCount()), 0))
                .sum();
        int dailyTaskAvailableCount = limitedTaskList.stream()
                .mapToInt(task -> Math.max(defaultZero(task.getGainCount()) - defaultZero(task.getConsumeCount()), 0))
                .sum();
        int dailyAvailableCount = Math.max(freeChanceCount - freeConsumedCount, 0) + dailyTaskAvailableCount;

        redisCache.setCacheObject(buildUnlimitedChanceCacheKey(activityId, memberId), unlimitedAvailableCount);
        redisCache.setCacheObject(buildDailyChanceCacheKey(activityId, memberId), dailyAvailableCount,
                Math.toIntExact(calculateEndOfDaySeconds()), TimeUnit.SECONDS);
    }

    /**
     * 抽卡次数缓存异常时，直接按任务表和抽卡记录强制回写缓存。
     */
    private void forceRefreshDrawChanceCache(Long activityId, Long memberId) {
        if (activityId == null || memberId == null) {
            return;
        }
        ActivityJkSaveOrUpdateReqVO cacheData = loadJkSettingWithLock(activityId);
        if (cacheData == null) {
            return;
        }

        int todayDrawCount = countTodayDrawCount(activityId, memberId);
        List<ActivityJkTaskDO> limitedTaskList = buildLimitedTaskList(activityId, memberId, cacheData);
        List<ActivityJkTaskDO> unlimitedTaskList = buildUnlimitedTaskList(activityId, memberId, cacheData);
        int freeChanceCount = resolveFreeChanceCount(cacheData);
        int freeConsumedCount = Math.min(Math.max(todayDrawCount, 0), freeChanceCount);

        int targetLimitedConsumeCount = Math.min(Math.max(todayDrawCount - freeConsumedCount, 0), sumGainCount(limitedTaskList));
        if (sumConsumeCount(limitedTaskList) != targetLimitedConsumeCount) {
            syncTaskConsumeCount(limitedTaskList, targetLimitedConsumeCount);
        }

        int unlimitedAvailableCount = unlimitedTaskList.stream()
                .mapToInt(task -> Math.max(defaultZero(task.getGainCount()) - defaultZero(task.getConsumeCount()), 0))
                .sum();
        int dailyTaskAvailableCount = limitedTaskList.stream()
                .mapToInt(task -> Math.max(defaultZero(task.getGainCount()) - defaultZero(task.getConsumeCount()), 0))
                .sum();
        int dailyAvailableCount = Math.max(freeChanceCount - freeConsumedCount, 0) + dailyTaskAvailableCount;

        redisCache.setCacheObject(buildUnlimitedChanceCacheKey(activityId, memberId), unlimitedAvailableCount);
        redisCache.setCacheObject(buildDailyChanceCacheKey(activityId, memberId), dailyAvailableCount,
                Math.toIntExact(calculateEndOfDaySeconds()), TimeUnit.SECONDS);
    }

    /**
     * 构建当天限制次数的任务列表。
     * 这里只处理配置为当天限制的任务，包括签到、下单、分享、浏览首页。
     */
    private List<ActivityJkTaskDO> buildLimitedTaskList(Long activityId, Long memberId, ActivityJkSaveOrUpdateReqVO cacheData) {
        List<ActivityJkTaskDO> taskList = new ArrayList<>();
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.SIGN, cacheData.getDailyAttendance(), cacheData.getDailyAttenCount(), false);
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.ORDER, cacheData.getPlaceOrderStatus(), resolveOrderTaskLimit(cacheData), false);
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.SHARE, cacheData.getShareEvent(), cacheData.getShareCount(), false);
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.BROWSE, cacheData.getBrowseType(), cacheData.getBrowseCount(), false);
        taskList.sort(Comparator.comparing(ActivityJkTaskDO::getTaskType));
        return taskList;
    }

    /**
     * 构建不限次数的任务列表。
     * 这部分会在当天限制任务全部扣完之后，继续承接剩余的抽卡消耗次数。
     */
    private List<ActivityJkTaskDO> buildUnlimitedTaskList(Long activityId, Long memberId, ActivityJkSaveOrUpdateReqVO cacheData) {
        List<ActivityJkTaskDO> taskList = new ArrayList<>();
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.SIGN, cacheData.getDailyAttendance(), cacheData.getDailyAttenCount(), true);
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.ORDER, cacheData.getPlaceOrderStatus(), resolveOrderTaskLimit(cacheData), true);
        addTask(taskList, activityId, memberId, ActivityJkTaskTypeEnum.SHARE, cacheData.getShareEvent(), cacheData.getShareCount(), true);
        taskList.sort(Comparator.comparing(ActivityJkTaskDO::getTaskType));
        return taskList;
    }

    /**
     * 根据是否不限次数，将任务加入对应的同步列表。
     */
    private void addTask(List<ActivityJkTaskDO> taskList, Long activityId, Long memberId,
                         ActivityJkTaskTypeEnum taskTypeEnum, Integer enabledFlag, Integer taskLimit, boolean unlimited) {
        if (!isTaskEnabled(enabledFlag) || isUnlimitedLimit(taskLimit) != unlimited) {
            return;
        }
        ActivityJkTaskDO taskDO = getOrInitTaskRecord(activityId, memberId, taskTypeEnum, unlimited ? TASK_LIMIT_UNLIMITED : taskLimit);
        if (taskDO != null) {
            taskList.add(taskDO);
        }
    }

    /**
     * 将抽卡消耗次数按顺序分摊到任务记录中。
     * 会先把当前列表里的任务尽量扣满，并返回剩余还未分摊的消耗次数。
     */
    private int syncTaskConsumeCount(List<ActivityJkTaskDO> taskList, int consumeCount) {
        int remainingConsumeCount = Math.max(consumeCount, 0);
        for (ActivityJkTaskDO taskDO : taskList) {
            int targetConsumeCount = Math.min(defaultZero(taskDO.getGainCount()), remainingConsumeCount);
            if (!Objects.equals(taskDO.getConsumeCount(), targetConsumeCount)) {
                taskDO.setConsumeCount(targetConsumeCount);
                updateTaskRecord(taskDO);
            }
            remainingConsumeCount -= targetConsumeCount;
            if (remainingConsumeCount <= 0) {
                remainingConsumeCount = 0;
            }
        }
        return remainingConsumeCount;
    }

    /**
     * 不限次数是跨天累计权益，必须在实际消耗时落库，不能用当天抽卡数反推。
     */
    private void consumeOneUnlimitedTaskChance(Long activityId, Long memberId) {
        ActivityJkSaveOrUpdateReqVO cacheData = getCachedJkSetting(activityId);
        if (cacheData == null) {
            cacheData = loadJkSettingWithLock(activityId);
        }
        if (cacheData == null) {
            throw exception(JK_NOT_ENABLED_REACHED);
        }

        List<ActivityJkTaskDO> unlimitedTaskList = buildUnlimitedTaskList(activityId, memberId, cacheData);
        for (ActivityJkTaskDO taskDO : unlimitedTaskList) {
            if (defaultZero(taskDO.getGainCount()) <= defaultZero(taskDO.getConsumeCount())) {
                continue;
            }
            taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()) + 1);
            taskDO.setUpdateTime(LocalDateTime.now());
            updateTaskRecord(taskDO);
            return;
        }
        throw exception(JK_NOT_ENABLED_REACHED);
    }

    /**
     * 按主键和分片键更新任务记录，避免 updateById 把 memberId 当作分片键更新。
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
     * 汇总当前任务列表中的消耗次数。
     * 只有总消耗次数和当天抽卡记录不一致时，才需要继续做同步。
     */
    private int sumConsumeCount(List<ActivityJkTaskDO> taskList) {
        return taskList.stream()
                .mapToInt(task -> defaultZero(task.getConsumeCount()))
                .sum();
    }

    /**
     * 汇总当前任务列表中的获得次数。
     */
    private int sumGainCount(List<ActivityJkTaskDO> taskList) {
        return taskList.stream()
                .mapToInt(task -> defaultZero(task.getGainCount()))
                .sum();
    }

    /**
     * 查询当天抽卡次数。
     */
    private int countTodayDrawCount(Long activityId, Long memberId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        Long drawCount = activityCardLogMapper.selectCount(new LambdaQueryWrapperX<ActivityCardLogDO>()
                .eq(ActivityCardLogDO::getActivityId, activityId)
                .eq(ActivityCardLogDO::getMemberId, memberId)
                .ge(ActivityCardLogDO::getCreateTime, todayStart)
                .lt(ActivityCardLogDO::getCreateTime, tomorrowStart));
        return drawCount == null ? 0 : drawCount.intValue();
    }

    /**
     * 判断助力人是否为注册七天内的新用户。
     * 满足时分享助力奖励增加 2 次，否则增加 1 次。
     */
    private boolean isSevenDayNewMember(Long memberId) {
        if (memberId == null) {
            return false;
        }
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
        if (memberResult == null || memberResult.getData() == null || memberResult.getData().getRegisterTime() == null) {
            return false;
        }
        LocalDate registerDate = memberResult.getData().getRegisterTime().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
        LocalDate sevenDayAgo = LocalDate.now().minusDays(7);
        return !registerDate.isBefore(sevenDayAgo);
    }

    /**
     * 订单任务是否属于不限次数配置。
     */
    private boolean isUnlimitedOrderTask(ActivityJkSaveOrUpdateReqVO cacheData) {
        return cacheData != null && (Objects.equals(cacheData.getPlaceOrderCard(), 1)
                || cacheData.getPlaceOrderCardNumber() == null
                || cacheData.getPlaceOrderCardNumber() == 0);
    }

    /**
     * 解析订单任务次数限制。
     * 不限次数时统一返回 0，和其他任务的无限制规则保持一致。
     */
    private Integer resolveOrderTaskLimit(ActivityJkSaveOrUpdateReqVO cacheData) {
        return isUnlimitedOrderTask(cacheData) ? TASK_LIMIT_UNLIMITED : cacheData.getPlaceOrderCardNumber();
    }

    /**
     * 构建不限次数缓存键。
     */
    private String buildUnlimitedChanceCacheKey(Long activityId, Long memberId) {
        return JKKeyConstants.JK_DRAW_CHANCE_UNLIMITED + activityId + ":" + memberId;
    }

    /**
     * 构建当日次数缓存键。
     */
    private String buildDailyChanceCacheKey(Long activityId, Long memberId) {
        return JKKeyConstants.JK_DRAW_CHANCE_DAILY + activityId + ":" + memberId + ":" + LocalDate.now();
    }

    /**
     * 计算当天剩余秒数。
     */
    private long calculateEndOfDaySeconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = now.toLocalDate().plusDays(1).atStartOfDay();
        return Math.max(Duration.between(now, tomorrowStart).getSeconds(), 1L);
    }

    /**
     * 解析任务开关字段。
     */
    private Integer resolveTaskEnabledFlag(ActivityJkSaveOrUpdateReqVO reqVO, ActivityJkTaskTypeEnum taskTypeEnum) {
        return switch (taskTypeEnum) {
            case SIGN -> reqVO.getDailyAttendance();
            case SHARE -> reqVO.getShareEvent();
            case ORDER -> reqVO.getPlaceOrderStatus();
            case BROWSE -> reqVO.getBrowseType();
        };
    }

    /**
     * 解析任务次数上限。
     */
    private Integer resolveTaskLimit(ActivityJkSaveOrUpdateReqVO reqVO, ActivityJkTaskTypeEnum taskTypeEnum) {
        return switch (taskTypeEnum) {
            case SIGN -> reqVO.getDailyAttenCount();
            case SHARE -> reqVO.getShareCount();
            case ORDER -> reqVO.getPlaceOrderCardNumber();
            case BROWSE -> reqVO.getBrowseCount();
        };
    }

    /**
     * 解析免费集卡可用次数。
     * 免费次数不进入任务体系，直接作为当天可用次数参与抽卡。
     */
    private int resolveFreeChanceCount(ActivityJkSaveOrUpdateReqVO cacheData) {
        if (cacheData == null || !isTaskEnabled(cacheData.getFreeStatus())) {
            return 0;
        }
        return defaultZero(cacheData.getFreeCount());
    }


    /**
     * 通用列表缓存加载模板。
     * 先查缓存，缓存未命中时抢短锁回源，避免热点数据同时打到数据库。
     */
    private <T> List<T> getOrLoadListCache(String cacheKey, String lockKey, CacheListLoader<T> loader) {
        Object cachedObject = redisCache.getCacheObject(cacheKey);
        if (cachedObject instanceof List<?>) {
            return (List<T>) cachedObject;
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(100, 2000, TimeUnit.MILLISECONDS);
            cachedObject = redisCache.getCacheObject(cacheKey);
            if (cachedObject instanceof List<?>) {
                return (List<T>) cachedObject;
            }
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
            List<T> dbList = loader.load();
            List<T> safeList = dbList == null ? Collections.emptyList() : dbList;
            redisCache.setCacheObject(cacheKey, safeList, JK_LIST_CACHE_SECONDS, TimeUnit.SECONDS);
            return safeList;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    private interface CacheListLoader<T> {
        List<T> load();
    }

}














