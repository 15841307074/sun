package com.htyoudao.youdao.module.promotion.service.lottery.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.TextFilterUtil;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.member.api.wecom.WecomGroupApi;
import com.htyoudao.youdao.module.member.api.wecom.vo.WecomGroupCheckAnyMemberReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.member.api.crowd.CrowdApi;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsNumVo;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityLotteryCommodity.ActivityLotteryCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryTask.LotteryTaskDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lotteryTask.LotteryTaskMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.enums.LotteryStateEnum;
import com.htyoudao.youdao.module.promotion.enums.LotteryTaskTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.LotteryTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.TransferBillStatus;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityLotteryCommodity.ActivityLotteryCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryMobileService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.promotion.util.string.StringUtils;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants.COMMODITY_LOTTERY_MEMBERID;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class LotteryMobileServiceImpl implements LotteryMobileService {
    @Resource
    private LotteryV2Service lotteryV2Service;
    @Resource
    private LotteryLedger lotteryLedger;
    @Resource
    private LotteryConfigurationCache lotteryConfigurationCache;
    @Resource
    private LotteryWinnerFeed winnerFeed;
    @Resource
    private LotteryAdmission lotteryAdmission;
    @Resource
    private LotteryCounterCache lotteryCounterCache;

    @Resource
    private LotteryScopeService lotteryScopeService;
    @Resource
    private LotteryRuntimeLifecycle lotteryRuntimeLifecycle;


    private static final String LOTTERY_TASK_LOCK_PREFIX = "lock:lottery:task:";
    private static final String LOTTERY_SHARE_LOCK_PREFIX = "lock:lottery:share:";

    // Redis库存真源：原子预占库存，失败返回-1，成功返回最新已用库存数。
    private static final DefaultRedisScript<Long> RESERVE_PRIZE_STOCK_SCRIPT;
    // Redis库存回滚：发奖失败时原子退回库存，并兜底修正为不小于0。
    private static final DefaultRedisScript<Long> ROLLBACK_PRIZE_STOCK_SCRIPT;

    static {
        RESERVE_PRIZE_STOCK_SCRIPT = new DefaultRedisScript<>();
        RESERVE_PRIZE_STOCK_SCRIPT.setResultType(Long.class);
        RESERVE_PRIZE_STOCK_SCRIPT.setScriptText(
                // KEYS[1]=奖品库存key，ARGV[1]=初始化已用库存，ARGV[2]=总库存
                "local current = redis.call('GET', KEYS[1]) "
                        + "if not current then "
                        + "  current = tonumber(ARGV[1]) "
                        + "  redis.call('SET', KEYS[1], current) "
                        + "end "
                        + "current = tonumber(current) "
                        + "local total = tonumber(ARGV[2]) "
                        // 当前已用库存达到上限时直接失败，避免超卖
                        + "if current >= total then return -1 end "
                        + "current = redis.call('INCR', KEYS[1]) "
                        // 并发边界下再次兜底校验，超了就立即回滚本次预占
                        + "if tonumber(current) > total then "
                        + "  redis.call('DECR', KEYS[1]) "
                        + "  return -1 "
                        + "end "
                        + "return tonumber(current)"
        );

        ROLLBACK_PRIZE_STOCK_SCRIPT = new DefaultRedisScript<>();
        ROLLBACK_PRIZE_STOCK_SCRIPT.setResultType(Long.class);
        ROLLBACK_PRIZE_STOCK_SCRIPT.setScriptText(
                // KEYS[1]=奖品库存key
                "local current = redis.call('GET', KEYS[1]) "
                        + "if not current then return 0 end "
                        + "current = tonumber(current) "
                        // 没有可回滚的库存时直接归零，避免出现负数
                        + "if current <= 0 then "
                        + "  redis.call('SET', KEYS[1], 0) "
                        + "  return 0 "
                        + "end "
                        + "current = redis.call('DECR', KEYS[1]) "
                        // 异常情况下二次兜底修正，保证库存key永远不小于0
                        + "if tonumber(current) < 0 then "
                        + "  redis.call('SET', KEYS[1], 0) "
                        + "  return 0 "
                        + "end "
                        + "return tonumber(current)"
        );
    }

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;
    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;
    @Resource
    private LotteryLogMapper lotteryLogMapper;
    @Resource
    private LotteryTaskMapper lotteryTaskMapper;
    @Resource
    public RedisCache redisCache;
    @Resource
    private WxMemberApi wxMemberApi;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private LotteryAddLogService lotteryAddLogService;
    @Resource
    protected StringRedisTemplate stringRedisTemplate;
    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivityLotteryCommodityService activityLotteryCommodityService;

    @Resource
    private ActivityMapper activityMapper;
    @DubboReference
    private CrowdApi crowdApi;
    @Resource
    private ActivityAppService activityAppService;

    @Resource
    private LotteryRedPacketService lotteryRedPacketService;

    /**
     * 获取活动分类列表
     *
     * @return
     */
    @Override
    public List<LotteryTypeVO> getLotteryTypeList(Long storeId) {
        if (storeId == null || storeId <= 0) return List.of();
        List<LotteryTypeVO> result = new ArrayList<>(legacyLotteryTypeList(storeId));
        try {
            for (Long settingsId : lotteryConfigurationCache.storeSettings(storeId)) {
                LotterySettingsCacheDataVO cfg = getLotterySettings(settingsId);
                if (!lotteryV2Service.enabled(cfg)) continue;
                if (!Objects.equals(cfg.getState(), 1)) continue;
                // 每次请求都重新判断活动日期和场次边界。
                if (!TimeValidationUtil.isTimeValid(cfg.getLotteryStartTime(), cfg.getLotteryEndTime(),
                        cfg.getDayNumbers(), cfg.getWeekNumbers(), cfg.getTimeRange())) continue;
                result.add(new LotteryTypeVO(BeanUtils.toBean(cfg, LotterySettingsDO.class)));
            }
        } catch (RuntimeException e) {
            // 新版列表缓存异常不阻断仍在运行的旧活动展示。
            log.warn("新版抽奖活动列表暂不可用，继续返回旧活动", e);
        }
        result.sort(Comparator.comparing(LotteryTypeVO::getLotteryType, Comparator.nullsLast(Integer::compareTo)));
        return result;
    }

    /** 旧活动重启前沿用原列表筛选规则，不改变原有展示范围。 */
    private List<LotteryTypeVO> legacyLotteryTypeList(Long storeId) {
        List<LotterySettingsDO> activities = lotterySettingsMapper.selectList(new LambdaQueryWrapperX<LotterySettingsDO>()
                        .eq(LotterySettingsDO::getState, 1)
                        .eq(LotterySettingsDO::getPublicButton, 0)
                        .le(LotterySettingsDO::getLotteryStartTime, LocalDate.now())
                        .ge(LotterySettingsDO::getLotteryEndTime, LocalDate.now())
                        .orderByAsc(LotterySettingsDO::getLotteryType)).stream()
                .filter(settings -> !Objects.equals(settings.getRuntimeVersion(), 2))
                .filter(settings -> settings.getActivityId() != null)
                .toList();
        if (activities.isEmpty()) return List.of();
        List<Long> activityIds = activities.stream().map(LotterySettingsDO::getActivityId).toList();
        Map<Long, ActivityDO> activityMap = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                        .in(ActivityDO::getId, activityIds)).stream()
                .collect(Collectors.toMap(ActivityDO::getId, Function.identity()));
        Map<Long, List<Long>> activityStoreMap = activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>()
                        .in(ActivityStoreDO::getActivityId, activityIds)).stream()
                .collect(Collectors.groupingBy(ActivityStoreDO::getActivityId,
                        Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())));
        return activities.stream().filter(settings -> {
                    ActivityDO activity = activityMap.get(settings.getActivityId());
                    if (activity == null) return false;
                    List<Long> stores = activityStoreMap.getOrDefault(settings.getActivityId(), List.of());
                    return (stores.isEmpty() || stores.contains(storeId)) && Boolean.TRUE.equals(
                            TimeValidationUtil.isTimeValid(settings.getLotteryStartTime(), settings.getLotteryEndTime(),
                                    activity.getDayNumbers(), activity.getWeekNumbers(), activity.getTimeRange()));
                }).map(LotteryTypeVO::new).toList();
    }

    /**
     * 辅助方法：将时间字符串（HH:mm）解析为LocalTime
     */
    private LocalTime parseTime(String timeStr, Long activityId, String session) {
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            log.warn("活动ID={}场次时间解析失败：{}（错误格式：{}）", activityId, session, timeStr);
            return null;
        }
    }


    /**
     * 获取活动详情
     *
     * @return
     */
    @Override
    public LotterySettingsResVO getLotteryDetail(LotteryVO lotteryVO) {
        if (lotteryVO == null || lotteryVO.getLotteryId() == null)
            throw new IllegalArgumentException("lotteryId 必填");
        LotterySettingsCacheDataVO cfg = getLotterySettings(lotteryVO.getLotteryId());
        if (!lotteryV2Service.enabled(cfg)) {
            LotterySettingsResVO legacy = new LotterySettingsResVO();
            legacy.setCacheData(cfg);
            legacy.setPrizes(lotteryConfigurationCache.displayPrizes(cfg,
                            Objects.requireNonNullElse(lotteryVO.getStoreId(), 0L)).stream()
                    .map(LotteryPrizeVO::new).collect(Collectors.toList()));
            return legacy;
        }
        if (lotteryVO.getStoreId() == null) throw new IllegalArgumentException("storeId 必填");
        validateLotteryBasicStatus(cfg);
        validateLotteryStore(cfg, lotteryVO);
        LotterySettingsResVO result = new LotterySettingsResVO();
        result.setCacheData(cfg);
        result.setPrizes(lotteryConfigurationCache.displayPrizes(cfg, lotteryVO.getStoreId()).stream()
                .map(LotteryPrizeVO::new).collect(Collectors.toList()));
        return result;
    }

    /**
     * 用户抽奖记录
     */
    @Override
    public List<LotteryLogDO> getLotteryLogByMemberId(LotteryLogVO lotteryLogVo) {
        Long lotteryId = lotteryLogVo.getLotteryId();
        Long memberId = lotteryLogVo.getMemberId();
        if (memberId == null) {
            return new ArrayList<>();
        }

        List<LotteryLogDO> result = null;
        if (lotteryId != null) {
            LotterySettingsDO lotterySettings = queryValidLotterySettings(lotteryId);
            if (lotterySettings != null && ObjectUtil.isNotEmpty(lotterySettings.getId())) {
                result = queryLotteryLogBySettings(lotterySettings, memberId);
            }
        } else {
            result = queryAllLotteryLogByMemberId(memberId);
        }
        processLotteryLogResult(result);
        return result;
    }

    /**
     * 查询有效的抽奖配置（状态为OPEN、时间在有效期内）
     *
     * @param lotteryId 抽奖ID/活动ID
     * @return 有效配置DO
     */
    private LotterySettingsDO queryValidLotterySettings(Long lotteryId) {
        return lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, lotteryId)
                        .or()
                        .eq(LotterySettingsDO::getActivityId, lotteryId)
                ));
    }

    /**
     * 根据抽奖配置查询会员的抽奖记录
     *
     * @param lotterySettings 抽奖配置
     * @param memberId        会员ID
     * @return 抽奖记录列表
     */
    private List<LotteryLogDO> queryLotteryLogBySettings(LotterySettingsDO lotterySettings, Long memberId) {
        return lotteryLogMapper.selectList(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, memberId)
                .and(wrapper -> wrapper
                        .eq(LotteryLogDO::getLotteryId, lotterySettings.getId())
                        .or()
                        .eq(LotteryLogDO::getLotteryId, lotterySettings.getActivityId())
                )
                .orderByDesc(LotteryLogDO::getCreateTime));
    }

    /**
     * 查询会员的所有抽奖记录（不限制抽奖ID）
     *
     * @param memberId 会员ID
     * @return 抽奖记录列表
     */
    private List<LotteryLogDO> queryAllLotteryLogByMemberId(Long memberId) {
        return lotteryLogMapper.selectList(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, memberId)
                .orderByDesc(LotteryLogDO::getCreateTime));
    }

    /**
     * 处理抽奖记录结果：将符合条件的记录claimStatus改为3（仅内存修改）
     *
     * @param result 抽奖记录列表
     */
    private void processLotteryLogResult(List<LotteryLogDO> result) {
        if (CollectionUtil.isNotEmpty(result)) {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            for (LotteryLogDO log : result) {
                // 校验条件：prizeType=5、claimStatus=1、创建时间超过24小时
                if (log.getPrizeType() == 5
                        && log.getClaimStatus() == 1
                        && log.getCreateTime() != null
                        && log.getCreateTime().isBefore(twentyFourHoursAgo)) {
                    log.setClaimStatus(3);
                }
            }
        }
    }

    /**
     * 设置收获地址
     */
    @Override
    public int updateReceivingAddress(LotteryLogVO lotteryLogVo) {
        LotteryLogDO lotteryLog = findLotteryLogById(lotteryLogVo.getId(), lotteryLogVo.getMemberId());

        if (Objects.isNull(lotteryLog)) {
            lotteryLog = findLatestLotteryLogByMemberIdAndPrizeType(lotteryLogVo);
        }

        if (Objects.nonNull(lotteryLog)) {
            return updateLotteryLogWithPrizeState(lotteryLogVo, lotteryLog);
        }

        return 0; // 或者抛出异常，根据业务需求决定
    }

    private LotteryLogDO findLotteryLogById(Long id, Long memberId) {
        return lotteryLogMapper.selectOne(new LambdaQueryWrapperX<LotteryLogDO>().eq(LotteryLogDO::getMemberId, memberId).eq(LotteryLogDO::getId, id));
    }

    private LotteryLogDO findLatestLotteryLogByMemberIdAndPrizeType(LotteryLogVO lotteryLogVo) {
        List<LotteryLogDO> list = lotteryLogMapper.selectList(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, lotteryLogVo.getMemberId())
                .eq(LotteryLogDO::getPrizeType, LotteryTypeEnum.REAL.getStatus())
                .eq(LotteryLogDO::getLotteryPrizeId, lotteryLogVo.getId())
                .orderByDesc(LotteryLogDO::getCreateTime));
        return list.isEmpty() ? null : list.get(0);
    }

    private int updateLotteryLogWithPrizeState(LotteryLogVO lotteryLogVo, LotteryLogDO lotteryLog) {
        lotteryLog.setPrizeState(2);
        lotteryLog.setReceiveAddress(lotteryLogVo.getReceiveAddress());
        lotteryLog.setReceiveUser(lotteryLogVo.getMemberNickName());
        lotteryLog.setReceiveMobile(lotteryLogVo.getMemberMobile());
        return lotteryLogMapper.update(new LambdaUpdateWrapper<LotteryLogDO>().set(LotteryLogDO::getPrizeState, 2)
                .set(LotteryLogDO::getReceiveAddress, lotteryLogVo.getReceiveAddress())
                .set(LotteryLogDO::getReceiveUser, lotteryLogVo.getMemberNickName())
                .set(LotteryLogDO::getReceiveMobile, lotteryLogVo.getMemberMobile()).eq(LotteryLogDO::getId, lotteryLog.getId())
                .eq(LotteryLogDO::getMemberId, lotteryLogVo.getMemberId()).in(LotteryLogDO::getPrizeState, 1, 2));
    }

    /**
     * 抽奖校验：校验活动状态、用户资格、抽奖次数等核心条件
     */
    @Override
    public CommonResult verifyLottery(LotteryVO lotteryVo) {
        log.info("抽奖校验开始，请求参数：{}", lotteryVo);
        // 1. 前置参数校验（避免空指针或无效参数进入后续逻辑）
        validateLotteryVo(lotteryVo);

        // 2. 获取活动配置（优先缓存，缓存失效则查库并更新缓存）
        LotterySettingsCacheDataVO lotterySettings = getLotterySettings(lotteryVo.getLotteryId());
        lotteryVo.setLotteryId(lotterySettings.getId());
        // 3. 校验活动基础状态（是否关闭、是否在有效期内）
        validateLotteryBasicStatus(lotterySettings);

        // 4. 获取并校验用户信息（用户是否存在）
        WxMemberVO wxMember = getAndValidateWxMember(lotteryVo.getMemberId());

        // 5. 校验抽奖时间合法性（匹配活动的日期/星期/时段限制）
        validateLotteryTime(lotterySettings);

        // 6. 校验门店权限（活动是否限制门店，用户是否在指定门店内）
        validateLotteryStore(lotterySettings, lotteryVo);

        // 7. 校验用户人群资格（是否符合活动指定的人群范围）
        validateUserCrowd(lotterySettings, wxMember);
        // 10.社群限制
        boolean canJoin = activityAppService.checkCanJoin(lotterySettings.getActivityId());
        if (!canJoin) {
            throw exception(LOTTERY_WECOMGROUP_ERROR);
        }
        // 8. 新规则支持多来源组合，老规则继续走原有单来源校验。
        if (lotteryV2Service.enabled(lotterySettings)) {
            if (v2Quota(lotterySettings, lotteryVo.getMemberId(), wxMember, true).getNum() == 0) throw exception(LOTTERY_NO_CHANCE);
            return CommonResult.success(true);
        }
        if (isMultiChanceMode(lotterySettings)) {
            validateMultiChance(lotterySettings, lotteryVo, wxMember);
        } else {
            // 旧逻辑：按单一抽奖方式校验资格与次数。
            validateLotteryMethod(lotterySettings, lotteryVo, wxMember);
            validateLotteryCountLimit(lotterySettings, lotteryVo);
        }
        log.info("抽奖校验通过，LotteryId：{}，MemberId：{}", lotteryVo.getLotteryId(), lotteryVo.getMemberId());
        return CommonResult.success(true);
    }

// ------------------------------ 以下为拆分的辅助方法 ------------------------------

    /**
     * 前置校验：LotteryVO参数非空及关键字段有效性
     */
    private void validateLotteryVo(LotteryVO lotteryVo) {
        if (lotteryVo == null) {
            throw exception(LOTTERY_NOT_NULL); // 建议新增「参数为空」的异常枚举
        }
        if (lotteryVo.getLotteryId() == null) {
            log.warn("抽奖校验失败：活动ID（LotteryId）为空");
            throw exception(LOTTERY_NOT_NULL);
        }
        if (lotteryVo.getMemberId() == null) {
            log.warn("抽奖校验失败：用户ID（MemberId）为空");
            throw exception(LOTTERY_NOT_NULL);

        }
    }


    /**
     * 校验活动基础状态：是否关闭、是否在活动有效期内
     */
    private void validateLotteryBasicStatus(LotterySettingsCacheDataVO settings) {
        // 活动配置为空（理论上已在getLotterySettings中拦截，此处做双重保障）
        if (Objects.isNull(settings) || Objects.isNull(settings.getId())) {
            throw exception(LOTTERY_NOT_NULL);
        }

        // 活动已关闭（state=0）
        if (settings.getState() == 0) {
            log.warn("活动已关闭，LotteryId：{}", settings.getId());
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
// 活动未开始判断（只比较日期，忽略时间）
        Date now = new Date();
        LocalDate currentDate;
        LocalDate startDate;

// 处理当前时间
        if (now instanceof java.sql.Date) {
            currentDate = ((java.sql.Date) now).toLocalDate();
        } else {
            currentDate = now.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

// 处理开始时间
        Date startTime = settings.getLotteryStartTime();
        if (startTime instanceof java.sql.Date) {
            // 对于java.sql.Date直接使用toLocalDate()
            startDate = ((java.sql.Date) startTime).toLocalDate();
        } else {
            // 对于java.util.Date通过Instant转换
            startDate = startTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

// 只有当前日期在开始日期之前，才判定为活动未开始
        if (currentDate.isBefore(startDate)) {
            log.warn("活动未开始，LotteryId：{}，开始时间：{}", settings.getId(), startTime);
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }
    }

    /**
     * 获取并校验微信用户：用户是否存在
     */
    private WxMemberVO getAndValidateWxMember(Long memberId) {
        // 调用远程接口获取用户信息
        CommonResult<WxMemberVO> memberResult;
        try {
            memberResult = wxMemberApi.getWxMemberById(memberId);
        } catch (Exception e) {
            log.warn("调用微信用户接口异常，MemberId：{}，异常信息：{}", memberId, e.getMessage(), e);
            throw exception(LOTTERY_SYSTEM_AGAIN); // 系统异常，提示重试
        }

        // 接口返回无效或用户不存在
        if (memberResult == null || !memberResult.isSuccess() || memberResult.getData() == null) {
            log.warn("获取用户信息失败，MemberId：{}，接口返回：{}", memberId, memberResult);
            throw exception(USER_NOT_EXISTS);
        }

        WxMemberVO wxMember = memberResult.getData();
        log.debug("获取用户信息成功，MemberId：{}，用户信息：{}", memberId, wxMember);
        return wxMember;
    }

    /**
     * 校验抽奖时间合法性：匹配活动的「日期/星期/时段」限制
     */
    private void validateLotteryTime(LotterySettingsCacheDataVO settings) {
        boolean isTimeValid;
        try {
            isTimeValid = TimeValidationUtil.isTimeValid(
                    settings.getLotteryStartTime(), settings.getLotteryEndTime(),
                    settings.getDayNumbers(), settings.getWeekNumbers(), settings.getTimeRange()
            );
        } catch (Exception e) {
            log.warn("时间校验工具类调用异常，LotteryId：{}，异常信息：{}", settings.getId(), e.getMessage(), e);
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }

        if (!isTimeValid) {
            log.warn("当前时间不在抽奖有效时段内，LotteryId：{}", settings.getId());
            throw exception(LOTTERY_SESSION_NOT_IN_RANGE);
        }
    }

    /**
     * 校验门店权限：活动是否限制门店，用户选择的门店是否在允许范围内
     */
    private void validateLotteryStore(LotterySettingsCacheDataVO settings, LotteryVO lotteryVo) {
        if (Objects.equals(settings.getAppScope(), 1)) {
            if (lotteryVo.getStoreId() == null || !lotteryScopeService.matches(settings.getActivityId(), lotteryVo.getStoreId()))
                throw exception(LOTTERY_NOT_STORE);
            return;
        }

        // 活动不限制门店（activityStore=1）→ 直接通过
        if (settings.getActivityStore() == 1) {
            return;
        }

        // 活动限制门店，但未配置允许的门店列表 → 视为无权限
        List<Long> allowedStoreIds = settings.getStoreIds();
        if (CollectionUtil.isEmpty(allowedStoreIds)) {
            log.warn("活动限制门店但未配置允许门店列表，LotteryId：{}", settings.getId());
            throw exception(LOTTERY_NOT_STORE);
        }

        // 用户选择的门店不在允许范围内 → 无权限
        Long userStoreId = lotteryVo.getStoreId();
        if (userStoreId == null || !allowedStoreIds.contains(userStoreId)) {
            log.warn("用户门店无权限，LotteryId：{}，用户门店：{}，允许门店：{}",
                    settings.getId(), userStoreId, allowedStoreIds);
            throw exception(LOTTERY_NOT_STORE);
        }
    }

    /**
     * 校验用户人群资格：是否符合活动指定的人群范围（新用户/老用户/指定人群等）
     */
    private void validateUserCrowd(LotterySettingsCacheDataVO settings, WxMemberVO wxMember) {
        // 活动不限制人群（participantGroup=1）→ 直接通过
        if (settings.getParticipantGroup() == 1) {
            return;
        }

        // 根据人群类型校验
        boolean isQualified = switch (settings.getParticipantGroup()) {
            case 2 -> userTypeSevenDay(wxMember); // 7天内用户
            case 3 -> userType(wxMember.getRegisterTime()); // 特定注册时间用户
            case 4 -> userTypeThirtyDay(wxMember); // 30天内用户
            case 5 -> checkCrowdApi(wxMember.getMemberId(), settings.getSelectedGroups()); // 调用人群接口校验
            default -> {
                log.warn("未知的人群类型，LotteryId：{}，人群类型：{}",
                        settings.getId(), settings.getParticipantGroup());
                yield false;
            }
        };

        if (!isQualified) {
            log.warn("用户不符合人群资格，LotteryId：{}，MemberId：{}，人群类型：{}",
                    settings.getId(), wxMember.getMemberId(), settings.getParticipantGroup());
            throw exception(LOTTERY_NOT_CROWD);
        }
    }

    /**
     * 调用人群接口校验用户资格（抽离独立方法，避免switch内逻辑臃肿）
     */
    private boolean checkCrowdApi(Long memberId, String selectedGroups) {
        try {
            return crowdApi.memberExist(memberId, selectedGroups);
        } catch (Exception e) {
            log.warn("调用人群接口异常，MemberId：{}，人群列表：{}，异常信息：{}",
                    memberId, selectedGroups, e.getMessage(), e);
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
    }

    /**
     * 新规则：免费、积分、下单、分享、浏览首页可组合开启。
     */
    private boolean isMultiChanceMode(LotterySettingsCacheDataVO settings) {
        return Objects.equals(settings.getFreeStatus(), 1)
                || Objects.equals(settings.getPointsStatus(), 1)
                || Objects.equals(settings.getOrderStatus(), 1)
                || Objects.equals(settings.getShareEvent(), 1)
                || Objects.equals(settings.getBrowseType(), 1);
    }

    /**
     * 新规则下只校验总次数和各来源是否至少存在一个可用机会。
     */
    private void validateMultiChance(LotterySettingsCacheDataVO settings, LotteryVO lotteryVo, WxMemberVO wxMember) {
        MultiChanceSummary summary = buildMultiChanceSummary(settings, lotteryVo.getMemberId(), wxMember, true);
        if (summary.totalRemaining == 0) {
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        }
        if (summary.hasAvailableChance()) {
            return;
        }
        if (summary.pointsEnabled && summary.pointsBlockedByBalance && !summary.hasNonPointsSource()) {
            throw exception(LOTTERY_INSUFFICIENT_POINTS);
        }
        if (Objects.equals(settings.getCalculationRules(), 2)) {
            throw exception(LOTTERY_SESSION_LIMIT_REACHED);
        }
        throw exception(LOTTERY_LIMIT_REACHED_AGAIN);
    }

    /**
     * 计算多来源抽奖机会摘要。
     * free / points 使用同一套 task 表记录 consume_count；
     * order / share / browse 使用 gain_count - consume_count 表示额外可用机会。
     */
    private MultiChanceSummary buildMultiChanceSummary(LotterySettingsCacheDataVO settings,
                                                       Long memberId,
                                                       WxMemberVO wxMember,
                                                       boolean applyTotalLimit) {
        MultiChanceSummary summary = new MultiChanceSummary();
        summary.scope = buildTaskScope(settings);
        if (lotteryV2Service.enabled(settings)) {
            LotterySettingsNumVo quota = v2Quota(settings, memberId, wxMember, applyTotalLimit);
            summary.totalRemaining=quota.getSum(); summary.aggregateAvailableCount=quota.getNum();
            summary.freeAvailableCount=quota.getFreeAvailableCount(); summary.pointsAvailableCount=quota.getPointsAvailableCount();
            summary.orderAvailableCount=quota.getOrderAvailableCount(); summary.shareAvailableCount=quota.getShareAvailableCount(); summary.browseAvailableCount=quota.getBrowseAvailableCount();
            summary.orderFinishCount=quota.getOrderFinishCount(); summary.shareFinishCount=quota.getShareFinishCount(); summary.browseFinishCount=quota.getBrowseFinishCount();
            summary.pointsEnabled=Objects.equals(settings.getPointsStatus(),1);
            summary.pointsBlockedByBalance=wxMember==null||defaultZero(wxMember.getMemberIntegral())<defaultZero(settings.getPrice());
            return summary;
        }
        summary.totalRemaining = calculateTotalRemaining(settings, memberId);
        Long activityId = resolveLotteryBizActivityId(settings);

        LotteryTaskDO freeTask = getScopedLotteryTask(activityId, memberId,
                LotteryTaskTypeEnum.FREE.getCode(), false, summary.scope, false);
        summary.freeAvailableCount = calculateFreeAvailableCount(settings, freeTask);

        LotteryTaskDO pointsTask = getScopedLotteryTask(activityId, memberId,
                LotteryTaskTypeEnum.POINTS.getCode(),
                isUnlimitedPoints(settings),
                summary.scope,
                false);
        summary.pointsEnabled = Objects.equals(settings.getPointsStatus(), 1);
        summary.pointsBlockedByBalance = summary.pointsEnabled
                && (wxMember == null || wxMember.getMemberIntegral() == null || wxMember.getMemberIntegral() < defaultZero(settings.getPrice()));
        summary.pointsAvailableCount = calculatePointsAvailableCount(settings, pointsTask, wxMember);

        if (Objects.equals(settings.getOrderStatus(), 1)) {
            LotteryTaskDO orderTask = getScopedLotteryTask(activityId, memberId,
                    LotteryTaskTypeEnum.ORDER.getCode(),
                    isUnlimitedOrderTask(settings),
                    summary.scope,
                    false);
            summary.orderAvailableCount = calculateTaskAvailableCount(orderTask);
            summary.orderFinishCount = orderTask == null ? 0 : defaultZero(orderTask.getFinishCount());
        }

        if (Objects.equals(settings.getShareEvent(), 1)) {
            LotteryTaskDO shareTask = getScopedLotteryTask(activityId, memberId,
                    LotteryTaskTypeEnum.SHARE.getCode(), false, summary.scope, false);
            summary.shareAvailableCount = calculateTaskAvailableCount(shareTask);
            summary.shareFinishCount = shareTask == null ? 0 : defaultZero(shareTask.getFinishCount());
        }

        if (Objects.equals(settings.getBrowseType(), 1)) {
            LotteryTaskDO browseTask = getScopedLotteryTask(activityId, memberId,
                    LotteryTaskTypeEnum.BROWSE.getCode(), false, summary.scope, false);
            summary.browseAvailableCount = calculateTaskAvailableCount(browseTask);
            summary.browseFinishCount = browseTask == null ? 0 : defaultZero(browseTask.getFinishCount());
        }

        summary.aggregateAvailableCount = mergeAvailableCount(summary, applyTotalLimit);
        return summary;
    }

    private int mergeAvailableCount(MultiChanceSummary summary, boolean applyTotalLimit) {
        boolean infinite = summary.pointsAvailableCount < 0;
        int finiteCount = Math.max(0, summary.freeAvailableCount)
                + Math.max(0, summary.orderAvailableCount)
                + Math.max(0, summary.shareAvailableCount)
                + Math.max(0, summary.browseAvailableCount)
                + Math.max(0, summary.pointsAvailableCount);
        int merged = infinite ? -1 : finiteCount;
        if (!applyTotalLimit || summary.totalRemaining < 0) {
            return merged;
        }
        if (merged < 0) {
            return summary.totalRemaining;
        }
        return Math.min(summary.totalRemaining, merged);
    }

    private int calculateFreeAvailableCount(LotterySettingsCacheDataVO settings, LotteryTaskDO taskDO) {
        if (!Objects.equals(settings.getFreeStatus(), 1)) {
            return 0;
        }
        int limit = Math.max(defaultZero(settings.getFreeCount()), 0);
        return Math.max(limit - defaultZero(taskDO == null ? 0 : taskDO.getConsumeCount()), 0);
    }

    private int calculatePointsAvailableCount(LotterySettingsCacheDataVO settings, LotteryTaskDO taskDO, WxMemberVO wxMember) {
        if (!Objects.equals(settings.getPointsStatus(), 1)) {
            return 0;
        }
        if (wxMember == null || wxMember.getMemberIntegral() == null || wxMember.getMemberIntegral() < defaultZero(settings.getPrice())) {
            return 0;
        }
        if (isUnlimitedPoints(settings)) {
            return -1;
        }
        int limit = Math.max(defaultZero(settings.getPointsCount()), 0);
        return Math.max(limit - defaultZero(taskDO == null ? 0 : taskDO.getConsumeCount()), 0);
    }

    private int calculateTaskAvailableCount(LotteryTaskDO taskDO) {
        if (taskDO == null) {
            return 0;
        }
        return Math.max(defaultZero(taskDO.getGainCount()) - defaultZero(taskDO.getConsumeCount()), 0);
    }

    private int calculateTotalRemaining(LotterySettingsCacheDataVO settings, Long memberId) {
        if (lotteryV2Service.enabled(settings)) return calculateRemainingNum(defaultZero(settings.getLotteryTotalNumber()),
                Math.toIntExact(lotteryLedger.consumed(BusinessContextHolder.getRequiredBusinessId(), settings.getActivityId(), memberId, "TOTAL", 0)));

        if (settings.getLotteryTotalNumber() == null || settings.getLotteryTotalNumber() == 0) {
            return -1;
        }
        String totalCacheKey = buildMultiTotalCacheKey(settings, memberId);
        Integer usedSum = getAndCacheUsedCount(
                totalCacheKey,
                () -> lotteryAddLogService.selectLotterySum(memberId, settings.getId()),
                null
        );
        return calculateRemainingNum(settings.getLotteryTotalNumber(), usedSum);
    }

    private boolean isUnlimitedPoints(LotterySettingsCacheDataVO settings) {
        return Objects.equals(settings.getPointsStatus(), 1) && Objects.equals(settings.getPointsType(), 1);
    }

    private boolean isUnlimitedOrderTask(LotterySettingsCacheDataVO settings) {
        return Objects.equals(settings.getOrderStatus(), 1) && Objects.equals(settings.getPlaceOrderLottery(), 1);
    }

    /**
     * 校验抽奖方式合法性：商品兑换（3）、积分兑换（2）等特殊规则
     */
    private void validateLotteryMethod(LotterySettingsCacheDataVO settings, LotteryVO lotteryVo, WxMemberVO wxMember) {
        String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());

        // 抽奖方式：商品兑换（3）→ 校验Redis中的商品资格标识
        if (settings.getLotteryMethod() == 3) {
            //yyyyMMdd
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String cacheKey = buildCommodityLotteryKey(lotteryVo.getMemberId(), settings, today);
            Integer codeFromRedis = 0;

            try {
                codeFromRedis = redisCache.getOpsForValue(cacheKey); // 原代码可能是getCacheObject，此处保持一致
            } catch (Exception e) {
                log.warn("Redis查询商品抽奖资格异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            if (ObjectUtil.isEmpty(codeFromRedis)) {
                log.warn("用户无商品抽奖资格（标识为空），CacheKey：{}", cacheKey);
                throw exception(LOTTERY_NO_CHANCE);
            }
            if (codeFromRedis == 0) {
                log.warn("用户商品抽奖资格已用尽，CacheKey：{}", cacheKey);
                throw exception(LOTTERY_LIMIT_REACHED);
            }
        }

        // 抽奖方式：积分兑换（2）→ 校验积分是否足够
        else if (settings.getLotteryMethod() == 2) {
            Integer requiredPoints = settings.getPrice();
            Integer userPoints = wxMember.getMemberIntegral();

            if (userPoints == null || userPoints < requiredPoints) {
                log.warn("用户积分不足，MemberId：{}，所需积分：{}，用户积分：{}",
                        wxMember.getMemberId(), requiredPoints, userPoints);
                throw exception(LOTTERY_INSUFFICIENT_POINTS);
            }
        }
    }

    /**
     * 校验抽奖次数限制：支持按天/按场次 + 总次数限制
     */
    private void validateLotteryCountLimit(LotterySettingsCacheDataVO settings, LotteryVO lotteryVo) {
        // 无任何限制（时段限制=0且总次数=0）→ 直接通过
        if (settings.getLotteryLimit() == 0 && settings.getLotteryTotalNumber() == 0) {
            return;
        }

        Long memberId = lotteryVo.getMemberId();
        Long lotterySettingId = settings.getId();
        Long activityId = resolveLotteryBizActivityId(settings);
        Integer calculationRules = settings.getCalculationRules(); // 1=按天，2=按场次

        // 1. 按规则校验时段内次数（每日 or 场次）
        if (calculationRules == 1) {
            // 原有逻辑：校验每日次数
            validateDailyLimit(settings, memberId, lotterySettingId, activityId);
        } else if (calculationRules == 2) {
            // 新增逻辑：校验当前场次次数
            validateSessionLimit(settings, memberId, lotterySettingId, activityId);
        } else {
            log.warn("未知计算规则，默认按天校验，规则值：{}", calculationRules);
            validateDailyLimit(settings, memberId, lotterySettingId, activityId);
        }

        // 2. 校验总抽奖次数（无论按天/按场次，总次数限制全局生效）
        validateTotalLimit(settings, memberId, lotterySettingId, activityId);
    }

// ------------------------------ 新增：场次次数校验 ------------------------------

    /**
     * 校验当前场次的抽奖次数限制
     */
    private void validateSessionLimit(LotterySettingsCacheDataVO settings, Long memberId, Long lotterySettingId, Long activityId) {
        // 场次限制为0 → 不校验（但需结合总次数限制）
        if (settings.getLotteryLimit() == 0) {
            return;
        }

        // 1. 解析当前活动的场次配置
        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            log.warn("场次校验失败：无有效场次配置，lotteryId：{}", lotterySettingId);
            throw exception(LOTTERY_SESSION_CONFIG_ERROR); // 新增场次配置错误异常
        }

        // 2. 判断当前时间所属场次
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            log.warn("当前不在任何有效场次内，memberId：{}，lotteryId：{}", memberId, lotterySettingId);
            throw exception(LOTTERY_SESSION_NOT_IN_RANGE); // 新增不在场次时间异常
        }

        // 3. 生成当前场次的缓存键
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String sessionCacheKey = RedisKeyConstants.LOTTERY_NUM
                + dateStr + "_" + activityId + ":" + memberId + ":" + sessionKey;

        // 4. 获取当前场次已用次数（缓存+数据库）
        Integer sessionUsedCount = getLotteryCount(
                sessionCacheKey,
                // 数据库查询当前场次内的抽奖次数
                () -> lotteryAddLogService.selectLotteryNumBySession(
                        memberId,
                        lotterySettingId,
                        currentSession.getStartLocalTime(),
                        currentSession.getEndLocalTime()
                ),
                // 缓存过期时间：当前场次结束（避免场次结束后缓存无效数据）
                () -> Duration.ofDays(Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds())
        );

        // 5. 校验场次次数是否超限
        if (sessionUsedCount >= settings.getLotteryLimit()) {
            log.warn("当前场次抽奖次数已用尽，memberId：{}，lotteryId：{}，场次：{}-{}，已用：{}，上限：{}",
                    memberId, lotterySettingId,
                    currentSession.getStartTimeStr(), currentSession.getEndTimeStr(),
                    sessionUsedCount, settings.getLotteryLimit());
            throw exception(LOTTERY_SESSION_LIMIT_REACHED); // 新增场次次数超限异常
        }
    }

// ------------------------------ 原有逻辑封装：每日校验 ------------------------------

    /**
     * 校验每日抽奖次数限制（原有逻辑封装）
     */
    private void validateDailyLimit(LotterySettingsCacheDataVO settings, Long memberId, Long lotterySettingId, Long activityId) {
        if (settings.getLotteryLimit() == 0) {
            return;
        }

        String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
        String dailyCacheKey = RedisKeyConstants.LOTTERY_NUM + currDate + "_" + activityId + ":" + memberId;

        Integer dailyUsedCount = getLotteryCount(
                dailyCacheKey,
                () -> lotteryAddLogService.selectLotteryNum(memberId, lotterySettingId),
                this::getDuration
        );

        if (dailyUsedCount >= settings.getLotteryLimit()) {
            log.warn("每日抽奖次数已用尽，MemberId：{}，LotteryId：{}，今日已用：{}，上限：{}",
                    memberId, lotterySettingId, dailyUsedCount, settings.getLotteryLimit());
            throw exception(LOTTERY_LIMIT_REACHED_AGAIN);
        }
    }

// ------------------------------ 原有逻辑封装：总次数校验 ------------------------------

    /**
     * 校验总抽奖次数限制（原有逻辑封装）
     */
    private void validateTotalLimit(LotterySettingsCacheDataVO settings, Long memberId, Long lotterySettingId, Long activityId) {
        if (settings.getLotteryTotalNumber() == 0) {
            return;
        }

        String totalCacheKey = RedisKeyConstants.LOTTERY_MEMBER_NUM + activityId + ":" + memberId;
        Integer totalUsedCount = getLotteryCount(
                totalCacheKey,
                () -> lotteryAddLogService.selectLotterySum(memberId, lotterySettingId),
                () -> calculateCacheDuration(new Date(), settings.getLotteryStartTime(), settings.getLotteryEndTime())
        );

        if (totalUsedCount >= settings.getLotteryTotalNumber()) {
            log.warn("总抽奖次数已用尽，MemberId：{}，LotteryId：{}，总已用：{}，上限：{}",
                    memberId, lotterySettingId, totalUsedCount, settings.getLotteryTotalNumber());
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        }
    }


    /**
     * 通用方法：获取抽奖次数（优先缓存，缓存失效则查库并更新缓存）
     *
     * @param cacheKey       缓存键
     * @param dbQuery        查库逻辑（函数式接口，避免重复代码）
     * @param expireSupplier 过期时间逻辑（函数式接口，支持不同场景的过期策略）
     * @return 已使用的抽奖次数
     */
    private Integer getLotteryCount(String cacheKey, Supplier<Integer> dbQuery, Supplier<Duration> expireSupplier) {
        Integer usedCount;

        // 1. 先查Redis缓存
        try {
            usedCount = redisCache.getCacheObject(cacheKey);
            if (usedCount != null) {
                log.debug("从Redis获取抽奖次数成功，CacheKey：{}，已用次数：{}", cacheKey, usedCount);
                return usedCount;
            }
        } catch (Exception e) {
            log.warn("Redis查询抽奖次数异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
            // 缓存异常不中断，继续查库
        }

        // 2. 缓存失效/异常，查库
        try {
            usedCount = dbQuery.get();
            if (usedCount == null) {
                usedCount = 0; // 查库无数据时默认0（与selectCount返回0一致）
            }
        } catch (Exception e) {
            log.warn("查库获取抽奖次数异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }

        // 3. 更新Redis缓存（带过期时间）
        try {
            redisCache.setCacheObject(cacheKey, usedCount);
            Duration expireDuration = expireSupplier.get();
            redisCache.expire(cacheKey, expireDuration.getSeconds(), TimeUnit.SECONDS);
            log.debug("更新抽奖次数到Redis成功，CacheKey：{}，已用次数：{}，过期时间：{}秒",
                    cacheKey, usedCount, expireDuration.getSeconds());
        } catch (Exception e) {
            log.warn("Redis更新抽奖次数异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
            // 缓存更新失败不中断业务（后续请求会再次尝试查库）
        }

        return usedCount;
    }

    /**
     * 抽奖
     */
    @Override
    public CommonResult lottery(LotteryVO lotteryVo) {
        LotterySettingsCacheDataVO resolved = getLotterySettings(lotteryVo.getLotteryId());
        // 历史活动重新启用并初始化前，继续使用原抽奖流程。
        if (!lotteryV2Service.enabled(resolved)) return legacyLottery(lotteryVo, resolved);
        if (lotteryVo.getRequestId() != null && !lotteryVo.getRequestId().isBlank()) {
            LotteryUserLogVO previous = lotteryV2Service.query(lotteryVo);
            if (previous != null) return CommonResult.success(previous);
        }
        return CommonResult.success(lotteryV2Service.drawAfterResultMiss(lotteryVo, resolved));
    }

    private CommonResult legacyLottery(LotteryVO lotteryVo, LotterySettingsCacheDataVO resolved) {
        // 配置缓存发布存在短暂延迟；旧链路落账前核对运行版本，避免迁移后双写库存。
        lotteryRuntimeLifecycle.rejectLegacyWrite(resolved.getId());
        lotteryVo.setLotteryId(resolved.getId());

        log.debug("抽奖开始:{}", lotteryVo);
        String userLotteryLockKey = "lottery:user:lock:" + lotteryVo.getLotteryId() + ":" + lotteryVo.getMemberId();
        RLock userLotteryLock = redissonClient.getLock(userLotteryLockKey);
        boolean userLotteryLocked = false;
        LotteryUserLogVO resultLog = new LotteryUserLogVO();
        List<LotteryPrizeDO> prizes = null;
        Integer prizePoolRules = 1; // 1=共用奖池，2=独立奖池
        boolean lotteryCountReserved = false;
        LotterySettingsCacheDataVO settings = null;
        ReservedDrawChance reservedChance = null;
        try {
            userLotteryLocked = userLotteryLock.tryLock();
            if (!userLotteryLocked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            verifyLottery(lotteryVo);

            // 1. 获取抽奖配置（包含奖池规则）
            settings = getLotterySettings(lotteryVo.getLotteryId());
            if (settings == null) {
                log.warn("未查询到抽奖配置，返回保底奖品");
                throw exception(LOTTERY_NOT_NULL);
            }
            lotteryVo.setLotteryId(settings.getId());
            prizePoolRules = settings.getPrizePoolRules(); // 1=共用奖池，2=独立奖池
            Long storeId = lotteryVo.getStoreId(); // 从请求中获取门店ID

            // 2. 独立奖池模式校验门店ID
            if (prizePoolRules == 2 && storeId == null) {
                log.warn("独立奖池模式下门店ID不能为空，返回保底奖品");
                throw exception(LOTTERY_NOT_STORE);
            }

            // 3. 按奖池规则获取奖品列表。
            prizes = getLotteryPrizes(lotteryVo.getLotteryId(), prizePoolRules, storeId);
            if (CollectionUtils.isEmpty(prizes)) {
                log.warn("未查询到奖品列表（规则：{}，门店：{}），返回保底奖品", prizePoolRules, storeId);
                throw exception(LOTTERY_NOT_NULL);
            }

            // 4. 先预占本次抽奖次数，成功后再执行抽奖，避免高并发下先抽后扣导致超抽。
            if (isMultiChanceMode(settings)) {
                WxMemberVO wxMember = getAndValidateWxMember(lotteryVo.getMemberId());
                reservedChance = reserveMultiChance(settings, lotteryVo, wxMember);
            } else {
                updateLotteryCount(lotteryVo, null);
                lotteryCountReserved = true;
            }

            // 5. 执行核心抽奖，库存只在关键位置短锁控制。
            resultLog = executeLottery(lotteryVo, prizes, prizePoolRules, storeId, settings);
        } catch (Exception e) {
            if (isLotteryCountLimitException(e)) {
                throw e;
            }
            log.warn("抽奖过程异常（门店：{}）", lotteryVo.getStoreId(), e);
            if (settings == null) {
                try {
                    settings = getLotterySettings(lotteryVo.getLotteryId());
                    if (settings != null) {
                        lotteryVo.setLotteryId(settings.getId());
                        prizePoolRules = settings.getPrizePoolRules();
                    }
                } catch (Exception ex) {
                    log.warn("获取配置异常", ex);
                }
            }
            if (reservedChance != null) {
                rollbackMultiChance(settings, lotteryVo, reservedChance);
                reservedChance = null;
            } else {
                rollbackLotteryCount(lotteryVo, settings, lotteryCountReserved);
                lotteryCountReserved = false;
            }
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
        } finally {
            if (userLotteryLocked && userLotteryLock.isHeldByCurrentThread()) {
                userLotteryLock.unlock();
            }
        }

        // 异步处理结果（传入奖池规则和门店ID，用于日志和统计）
        asyncProcessLotteryResult(lotteryVo, resultLog, prizePoolRules, lotteryVo.getStoreId(), reservedChance);

        return CommonResult.success(resultLog);
    }

// ------------------------------ 新增辅助方法：统一设置保底奖品 ------------------------------

    /**
     * 统一设置保底奖品（适配共用/独立奖池，所有异常场景的 fallback）
     *
     * @param resultLog      抽奖结果对象（需填充保底奖品）
     * @param lotteryId      活动ID
     * @param storeId        门店ID（独立奖池时必填）
     * @param prizePoolRules 奖池规则（1=共用，2=独立）
     */
    private void setGuaranteePrize(LotteryUserLogVO resultLog, Long lotteryId, Long storeId, Integer prizePoolRules) {
        LotteryPrizeDO guaranteePrize = null;
        try {
            // 1. 按奖池规则获取对应奖品列表（共用查全局，独立查门店）
            List<LotteryPrizeDO> prizes = getLotteryPrizes(lotteryId, prizePoolRules, storeId);
            if (CollectionUtils.isEmpty(prizes)) {
                log.warn("设置保底奖品失败：活动[{}]规则[{}]门店[{}]未查询到奖品列表",
                        lotteryId, prizePoolRules, storeId);
                setLotteryVO(resultLog, guaranteePrize); // 极端情况：无奖品时用默认值
                return;
            }

            // 2. 筛选当前奖池的保底奖品（isGuarantees=1，独立奖池需匹配门店）
            guaranteePrize = prizes.stream()
                    .filter(prize -> {
                        // 保底标识必须为1
                        if (prize.getIsGuarantees() != 1) {
                            return false;
                        }
                        // 独立奖池需额外校验门店匹配
                        if (prizePoolRules == 2) {
                            return prize.getStoreId().equals(storeId);
                        }
                        // 共用奖池无需校验门店
                        return true;
                    })
                    .findFirst()
                    .orElse(null);

            if (guaranteePrize != null) {
                // 3. 找到保底奖品后直接返回结果，保底奖品不参与库存扣减
                setLotteryVO(resultLog, guaranteePrize);
            } else {
                log.warn("活动[{}]规则[{}]门店[{}]未配置保底奖品，使用默认参与奖",
                        lotteryId, prizePoolRules, storeId);
                setLotteryVO(resultLog, guaranteePrize);
            }
        } catch (Exception e) {
            // 极端场景：获取/设置保底奖品失败→用最基础默认值
            log.warn("获取保底奖品发生异常（活动[{}]规则[{}]门店[{}]）",
                    lotteryId, prizePoolRules, storeId, e);
            setLotteryVO(resultLog, guaranteePrize);
        }
    }


    /**
     * 获取抽奖奖品列表（优先从缓存获取，适配共用/独立奖池）
     *
     * @param lotteryId      活动ID
     * @param prizePoolRules 奖池规则（1=共用奖池，2=独立奖池）
     * @param storeId        门店ID（独立奖池时必填）
     * @return 符合当前奖池规则的奖品列表
     */
    private List<LotteryPrizeDO> getLotteryPrizes(Long lotteryId, Integer prizePoolRules, Long storeId) {
        LotterySettingsCacheDataVO settings = getLotterySettings(lotteryId);
        if (settings == null || settings.getId() == null) {
            log.warn("获取奖品列表失败，未查询到抽奖配置，lotteryId={}", lotteryId);
            return Collections.emptyList();
        }
        Long settingsId = settings.getId();
        Long cacheLotteryId = settings.getActivityId() == null ? settingsId : settings.getActivityId();

        // 1. 按活动ID生成主缓存键，兼容前端传活动ID的场景。
        String cacheKey = buildPrizeCacheKey(cacheLotteryId, prizePoolRules, storeId);

        // 2. 优先从缓存获取
        List<LotteryPrizeDO> prizes = redisCache.getCacheObject(cacheKey);
        if (!CollectionUtils.isEmpty(prizes)) {
            log.debug("从缓存获取奖品列表，cacheKey={}，数量={}", cacheKey, prizes.size());
            return prizes;
        }
        // 兼容历史用设置ID写入的奖品缓存，命中后补写活动ID缓存。
        String settingsCacheKey = buildPrizeCacheKey(settingsId, prizePoolRules, storeId);
        if (!Objects.equals(cacheKey, settingsCacheKey)) {
            prizes = redisCache.getCacheObject(settingsCacheKey);
            if (!CollectionUtils.isEmpty(prizes)) {
                redisCache.setCacheObject(cacheKey, prizes, 24, TimeUnit.HOURS);
                log.debug("从设置ID缓存获取奖品列表并补写活动ID缓存，oldKey={}，cacheKey={}，数量={}", settingsCacheKey, cacheKey, prizes.size());
                return prizes;
            }
        }

        // 3. 缓存未命中，从数据库查询。奖品表 lottery_id 存的是抽奖设置ID，不是活动ID。
        prizes = queryPrizesFromDb(settingsId, prizePoolRules, storeId);
        if (CollectionUtils.isEmpty(prizes)) {
            // 历史缓存里可能存在 settings.id 被活动ID覆盖的脏数据；奖品查空时强制回表拿真实设置ID再查一次。
            LotterySettingsCacheDataVO latestSettings = addCache(lotteryId);
            if (latestSettings != null && latestSettings.getId() != null && !Objects.equals(latestSettings.getId(), settingsId)) {
                settingsId = latestSettings.getId();
                cacheLotteryId = latestSettings.getActivityId() == null ? settingsId : latestSettings.getActivityId();
                cacheKey = buildPrizeCacheKey(cacheLotteryId, prizePoolRules, storeId);
                prizes = queryPrizesFromDb(settingsId, prizePoolRules, storeId);
            }
        }
        prizes = syncPrizeRemainNumFromSource(prizes, prizePoolRules, storeId);

        // 4. 查询结果不为空时更新缓存（设置24小时过期，避免缓存长期无效）
        if (!CollectionUtils.isEmpty(prizes)) {
            redisCache.setCacheObject(cacheKey, prizes, 24, TimeUnit.HOURS);
            log.debug("数据库查询奖品列表并更新缓存，cacheKey={}，数量={}", cacheKey, prizes.size());
        } else {
            log.warn("未查询到活动[{}]规则[{}]门店[{}]的奖品列表", lotteryId, prizePoolRules, storeId);
        }

        return prizes;
    }

    /**
     * 奖品列表缓存缺失时，优先按库存真源缓存同步 remainNum；库存真源不存在时再按抽奖记录统计数量同步。
     */
    private List<LotteryPrizeDO> syncPrizeRemainNumFromSource(List<LotteryPrizeDO> prizes, Integer prizePoolRules, Long storeId) {
        if (CollectionUtils.isEmpty(prizes)) {
            return prizes;
        }
        Long lotteryId = prizes.get(0).getLotteryId();
        LotterySettingsCacheDataVO settings = getLotterySettings(lotteryId);
        Long activityId = settings == null || settings.getActivityId() == null ? lotteryId : settings.getActivityId();
        Map<Long, Integer> prizeLogCountMap = countPrizeUsedByLotteryLogs(lotteryId, activityId, prizePoolRules, storeId);
        for (LotteryPrizeDO prize : prizes) {
            Integer usedCount = getPrizeInventoryUsedCount(prize, prizePoolRules, storeId);
            if (usedCount == null) {
                usedCount = prizeLogCountMap.getOrDefault(prize.getId(), ObjectUtil.defaultIfNull(prize.getRemainNum(), 0));
            }
            prize.setRemainNum(usedCount);
        }
        return prizes;
    }

    /**
     * 按抽奖记录统计各奖品已领取数量，作为库存真源缓存缺失时的兜底依据。
     */
    private Map<Long, Integer> countPrizeUsedByLotteryLogs(Long lotteryId, Long activityId, Integer prizePoolRules, Long storeId) {
        Long actualActivityId = activityId == null ? lotteryId : activityId;
        List<Map<String, Object>> countRows = Objects.equals(prizePoolRules, 2) && storeId != null
                ? lotteryLogMapper.selectPrizeUsedCountGroupByPrizeIdAndStoreId(lotteryId, actualActivityId, storeId)
                : lotteryLogMapper.selectPrizeUsedCountGroupByPrizeId(lotteryId, actualActivityId);
        if (CollectionUtils.isEmpty(countRows)) {
            return Collections.emptyMap();
        }
        return countRows.stream()
                .filter(Objects::nonNull)
                .filter(row -> row.get("lotteryPrizeId") != null && row.get("usedCount") != null)
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("lotteryPrizeId")).longValue(),
                        row -> ((Number) row.get("usedCount")).intValue()
                ));
    }

    /**
     * 按奖池规则构建奖品缓存键
     */
    private String buildPrizeCacheKey(Long lotteryId, Integer prizePoolRules, Long storeId) {
        if (prizePoolRules == 1) {
            // 共用奖池：缓存键仅包含活动ID（全局唯一）
            return RedisKeyConstants.LOTTERY_PRIZE + lotteryId;
        } else {
            // 独立奖池：缓存键包含活动ID+门店ID（门店级唯一，避免跨门店缓存污染）
            if (storeId == null) {
                throw new IllegalArgumentException("独立奖池模式下，门店ID不能为空（缓存键构建失败）");
            }
            return RedisKeyConstants.LOTTERY_PRIZE + lotteryId + ":store:" + storeId;
        }
    }

    /**
     * 按奖池规则从数据库查询奖品
     */
    private List<LotteryPrizeDO> queryPrizesFromDb(Long lotteryId, Integer prizePoolRules, Long storeId) {
        LambdaQueryWrapperX<LotteryPrizeDO> queryWrapper = new LambdaQueryWrapperX<LotteryPrizeDO>()
                .eq(LotteryPrizeDO::getLotteryId, lotteryId); // 基础条件：活动ID

        if (prizePoolRules == 2) {
            // 独立奖池：仅查询当前门店的奖品（数据库表需有store_id字段）
            if (storeId == null) {
                throw new IllegalArgumentException("独立奖池模式下，门店ID不能为空（数据库查询失败）");
            }
            queryWrapper.eq(LotteryPrizeDO::getStoreId, storeId);
        }


        return lotteryPrizeMapper.selectList(queryWrapper);
    }

    /**
     * 执行核心抽奖逻辑（适配共用/独立奖池）
     * @return 抽奖结果对象
     */
    public LotteryDrawSnapshot prepareV2Draw(LotteryVO req, LotterySettingsCacheDataVO cfg, List<LotteryPrizeDO> prizes) {
        validateLotteryBasicStatus(cfg); validateLotteryTime(cfg); validateLotteryStore(cfg, req);
        WxMemberVO member = getAndValidateWxMember(req.getMemberId());
        validateUserCrowd(cfg, member);
        if (!activityAppService.checkCanJoin(cfg.getActivityId())) throw exception(LOTTERY_WECOMGROUP_ERROR);
        LotteryPrizeDO guarantee = getGuaranteePrize(prizes, cfg.getPrizePoolRules(), req.getStoreId());
        if (guarantee == null) throw exception(LOTTERY_GUARANTEED_PRODUCTS);
        LotteryDrawSnapshot snapshot = new LotteryDrawSnapshot();
        snapshot.setRequest(req); snapshot.setSettings(cfg); snapshot.setMember(member); snapshot.setGuarantee(guarantee);
        String period = buildTaskScope(cfg).scopeKey; snapshot.setScopeKey(period);
        List<LotteryDrawSnapshot.Chance> choices = snapshot.getChances();
        if (isMultiChanceMode(cfg)) {
            if (Objects.equals(cfg.getFreeStatus(), 1)) choices.add(new LotteryDrawSnapshot.Chance(1, period, defaultZero(cfg.getFreeCount()), 0, false));
            if (Objects.equals(cfg.getOrderStatus(), 1)) choices.add(new LotteryDrawSnapshot.Chance(3, isUnlimitedOrderTask(cfg) ? "TOTAL" : period, 0, 0, true));
            if (Objects.equals(cfg.getShareEvent(), 1)) choices.add(new LotteryDrawSnapshot.Chance(4, period, 0, 0, true));
            if (Objects.equals(cfg.getBrowseType(), 1)) choices.add(new LotteryDrawSnapshot.Chance(5, period, 0, 0, true));
            if (Objects.equals(cfg.getPointsStatus(), 1) && defaultZero(member.getMemberIntegral()) >= defaultZero(cfg.getPrice()))
                choices.add(new LotteryDrawSnapshot.Chance(2, isUnlimitedPoints(cfg) ? "TOTAL" : period,
                        isUnlimitedPoints(cfg) ? -1 : defaultZero(cfg.getPointsCount()), defaultZero(cfg.getPrice()), false));
        } else {
            int method = defaultZero(cfg.getLotteryMethod());
            if (method == 2 && defaultZero(member.getMemberIntegral()) < defaultZero(cfg.getPrice())) throw exception(LOTTERY_NO_CHANCE);
            choices.add(new LotteryDrawSnapshot.Chance(method, period, -1, method == 2 ? defaultZero(cfg.getPrice()) : 0, method == 3));
        }
        boolean needsDrawCount = prizes.stream().anyMatch(p -> defaultZero(p.getMinimumNumber()) > 0);
        int drawCount = needsDrawCount ? Math.toIntExact(lotteryLedger.activityDrawCount(BusinessContextHolder.getRequiredBusinessId(), cfg.getActivityId())) : 0;
        List<LotteryPrizeDO> drawable = prizes.stream().filter(prize -> isPrizeUnlockedForDraw(prize, guarantee, drawCount)).collect(Collectors.toList());
        LotteryPrizeDO selected = guarantee;
        BigDecimal total = calculateTotalProbability(drawable);
        if (total != null && total.signum() > 0) {
            BigDecimal random = generateRandomValue(total), cumulative = BigDecimal.ZERO;
            for (LotteryPrizeDO prize : drawable) {
                cumulative = cumulative.add(prize.getProbability());
                if (random.compareTo(cumulative) <= 0) { selected = prize; break; }
            }
        }
        if (hasCityRestriction(req, selected) || (Objects.equals(selected.getPrizeType(), 5) && Objects.equals(member.getMemberCategory(), 1))) selected = guarantee;
        snapshot.setPrize(selected);
        if (Objects.equals(selected.getPrizeType(), 1)) snapshot.setCoupon(lotteryAddLogService.getGoodCoupon(selected.getAwardId()));
        if (Objects.equals(guarantee.getPrizeType(), 1)) snapshot.setGuaranteeCoupon(
                Objects.equals(selected.getId(), guarantee.getId()) ? snapshot.getCoupon() : lotteryAddLogService.getGoodCoupon(guarantee.getAwardId()));
        return snapshot;
    }

    private LotteryUserLogVO executeLottery(LotteryVO lotteryVo, List<LotteryPrizeDO> prizes,
                                            Integer prizePoolRules, Long storeId,
                                            LotterySettingsCacheDataVO settings) {
        LotteryUserLogVO logVO = new LotteryUserLogVO();
        // 1. 获取当前奖池的保底奖品（共用=全局保底，独立=门店专属保底）
        LotteryPrizeDO guaranteePrize = getGuaranteePrize(prizes, prizePoolRules, storeId);
        List<LotteryPrizeDO> drawablePrizes = filterDrawablePrizes(lotteryVo, prizes, guaranteePrize, settings);
        if (CollectionUtils.isEmpty(drawablePrizes)) {
            setLotteryVO(logVO, guaranteePrize);
            return logVO;
        }


        // 3. 计算总概率并生成随机值
        BigDecimal totalProbability = calculateTotalProbability(drawablePrizes);
        if (totalProbability == null || totalProbability.compareTo(BigDecimal.ZERO) <= 0) {
            setLotteryVO(logVO, guaranteePrize);
            return logVO;
        }
        BigDecimal randomValue = generateRandomValue(totalProbability);
        log.debug("抽奖概率计算：总概率={}，随机值={}", totalProbability, randomValue);

        // 4. 根据概率区间选择奖品
        BigDecimal cumulativeProbability = BigDecimal.ZERO;
        for (LotteryPrizeDO prize : drawablePrizes) {
            cumulativeProbability = cumulativeProbability.add(prize.getProbability());
            if (randomValue.compareTo(cumulativeProbability) <= 0) {
                // 5. 处理选中的奖品（传入奖池规则和门店ID，适配库存校验）
                return processSelectedPrize(lotteryVo, prizes, logVO, guaranteePrize,
                        prize, prizePoolRules, storeId, settings);
            }
        }

        // 6. 未抽中任何奖品时返回保底奖品
        setLotteryVO(logVO, guaranteePrize);
        return logVO;
    }

    /**
     * minimumNumber 的语义是“达到门槛前不进入奖池”，而不是抽中后再回退。
     * 兜底奖品仍允许作为兜底返回，因此这里只控制其是否参与概率抽取。
     */
    private List<LotteryPrizeDO> filterDrawablePrizes(LotteryVO lotteryVo,
                                                      List<LotteryPrizeDO> prizes,
                                                      LotteryPrizeDO guaranteePrize,
                                                      LotterySettingsCacheDataVO settings) {
        if (CollectionUtils.isEmpty(prizes)) {
            return Collections.emptyList();
        }
        Integer lotteryDrawCount = getLotteryDrawCount(lotteryVo, settings);
        return prizes.stream()
                .filter(Objects::nonNull)
                .filter(prize -> isPrizeUnlockedForDraw(prize, guaranteePrize, lotteryDrawCount))
                .collect(Collectors.toList());
    }

    private boolean isPrizeUnlockedForDraw(LotteryPrizeDO prize,
                                           LotteryPrizeDO guaranteePrize,
                                           Integer lotteryDrawCount) {
        if (prize == null) {
            return false;
        }
        if (prize.getProbability() == null || prize.getProbability().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (isGuaranteePrize(prize) && guaranteePrize != null && Objects.equals(prize.getId(), guaranteePrize.getId())) {
            return true;
        }
        Integer minimumNumber = prize.getMinimumNumber();
        if (minimumNumber == null || minimumNumber <= 0) {
            return true;
        }
        return defaultZero(lotteryDrawCount) >= minimumNumber;
    }

    private Integer getLotteryDrawCount(LotteryVO lotteryVo) {
        return getLotteryDrawCount(lotteryVo, null);
    }

    private Integer getLotteryDrawCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO cachedSettings) {
        try {
            LotterySettingsCacheDataVO settings = cachedSettings == null ? getLotterySettings(lotteryVo.getLotteryId()) : cachedSettings;
            Long activityId = settings == null ? lotteryVo.getLotteryId() : resolveLotteryBizActivityId(settings);
            String cacheKey = RedisKeyConstants.LOTTERY_SUM + ":" + activityId;
            Integer count = redisCache.getCacheObject(cacheKey);
            if (count != null) {
                return count;
            }
            return lotteryLogMapper.selectCount(new LambdaQueryWrapperX<LotteryLogDO>()
                    .eq(LotteryLogDO::getLotteryId, lotteryVo.getLotteryId())).intValue();
        } catch (Exception e) {
            log.warn("获取活动开奖总次数异常，lotteryId={}", lotteryVo.getLotteryId(), e);
            return 0;
        }
    }


    /**
     * 获取当前奖池的保底奖品（适配奖池规则）
     */
    private LotteryPrizeDO getGuaranteePrize(List<LotteryPrizeDO> prizes,
                                             Integer prizePoolRules, Long storeId) {
        return prizes.stream()
                .filter(prize -> {
                    // 条件1：是保底奖品（isGuarantees=1）
                    if (prize.getIsGuarantees() != 1) {
                        return false;
                    }
                    // 条件2：独立奖池需匹配门店
                    if (prizePoolRules == 2) {
                        return prize.getStoreId().equals(storeId);
                    }
                    return true;
                })
                .findFirst()
                .orElse(null); // 无保底奖品时返回null，后续由processSelectedPrize处理
    }

    /**
     * 处理选中的奖品，包含各种校验逻辑（适配共用/独立奖池，确保已领取数量正确更新）
     */
    private LotteryUserLogVO processSelectedPrize(LotteryVO lotteryVo, List<LotteryPrizeDO> prizes,
                                                  LotteryUserLogVO logVO, LotteryPrizeDO guaranteePrize,
                                                  LotteryPrizeDO selectedPrize, Integer prizePoolRules, Long storeId,LotterySettingsCacheDataVO settings) {
        boolean useGuarantee = false;
        setLotteryVO(logVO, selectedPrize);

        // 非保底奖品需要进行各种校验
        if (selectedPrize.getIsGuarantees() == 0) {
            useGuarantee = checkNonGuaranteePrizeValidity(lotteryVo, selectedPrize, prizePoolRules, storeId);
        }

        // 使用保底奖品（校验并更新保底奖品的已领取数量）
        if (useGuarantee && guaranteePrize != null) {
            return handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        }

        // 抽中保底奖品时直接返回结果，兜底奖品不计入库存。
        if (isGuaranteePrize(selectedPrize)) {
            return handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        }

        try {
            // 红包奖品先占库存再发红包，失败时回滚库存并切换保底。
            if (selectedPrize.getPrizeType() == 5) {
                handleRedPacketTypePrize(lotteryVo, logVO, selectedPrize, prizes, guaranteePrize, prizePoolRules, storeId,settings.getActivityId());
                return logVO;
            }

            // 普通奖品只在库存关键位置短锁，减少整条链路阻塞。
            if (!updatePrizeInventoryAndCache(logVO, selectedPrize, prizes, prizePoolRules, storeId)) {
                return handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("奖品库存更新被中断，切换保底奖品，奖品ID={}", selectedPrize.getId(), e);
            return handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        } catch (Exception e) {
            log.warn("主奖品处理失败，切换保底奖品，奖品ID={}", selectedPrize.getId(), e);
            return handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        }

        return logVO;
    }

// ---------------------- 新增/抽离的工具方法 ----------------------

    /**
     * 校验非保底奖品是否有效（返回true表示需要使用保底）
     */
    private boolean checkNonGuaranteePrizeValidity(LotteryVO lotteryVo, LotteryPrizeDO selectedPrize,
                                                   Integer prizePoolRules, Long storeId) {


        // 1. 库存校验
        if (!checkPrizeInventory(selectedPrize, prizePoolRules, storeId)) {
            log.warn("奖品{}库存不足（已领取={}/总数量={}），规则={}，门店={}",
                    selectedPrize.getId(), selectedPrize.getRemainNum(), selectedPrize.getPrizeNum(),
                    prizePoolRules, storeId);
            return true;
        }
        if (selectedPrize.getIsRepeat() == 0) {
            // 重复中奖校验前加锁
            String repeatLockKey = "lottery:repeat:lock:" + lotteryVo.getMemberId() + ":" + selectedPrize.getId();
            RLock repeatLock = redissonClient.getLock(repeatLockKey);
            // 缓存Key定义
            String receivedCacheKey = "lottery:user:prize:received:" + ":" + lotteryVo.getLotteryId() + ":" + lotteryVo.getMemberId() + ":" + selectedPrize.getId();
            String pendingCacheKey = "lottery:user:prize:pending:" + ":" + lotteryVo.getLotteryId() + ":" + lotteryVo.getMemberId() + ":" + selectedPrize.getId(); // 新增：待确认标记
            // 0. 前置缓存校验：优先判断“已领取”或“领取中”
            Boolean isReceived = redisCache.getCacheObject(receivedCacheKey);
            if (Boolean.TRUE.equals(isReceived)) {
                log.warn("缓存命中：用户{}已领取奖品{}，直接拦截", lotteryVo.getMemberId(), selectedPrize.getId());
                return true;
            }
// 检查是否有未完成的异步领取（防止重复发起异步）
            Boolean isPending = redisCache.getCacheObject(pendingCacheKey);
            if (Boolean.TRUE.equals(isPending)) {
                log.warn("用户{}正在领取奖品{}（异步未完成），拦截重复请求", lotteryVo.getMemberId(), selectedPrize.getId());
                return true;
            }
            try {
                if (!repeatLock.tryLock(5, 30, TimeUnit.SECONDS)) {
                    log.warn("用户{}重复校验锁竞争失败，奖品{}", lotteryVo.getMemberId(), selectedPrize.getId());
                    return true; // 视为重复，防止并发问题
                }

                // 2. 锁内二次校验（数据库+缓存）
                if (hasUserWonPrize(lotteryVo, selectedPrize)) { // 查数据库确认是否已存在记录
                    log.warn("数据库校验：用户{}已领取奖品{}，拦截", lotteryVo.getMemberId(), selectedPrize.getId());
                    redisCache.setCacheObject(receivedCacheKey, true); // 补全缓存
                    return true;
                }
                // 3. 标记“领取中”（设置较短过期时间，如5分钟，避免异步失败后永久阻塞）
                redisCache.setCacheObject(pendingCacheKey, true, 5, TimeUnit.MINUTES);
                return false;
            } catch (Exception e) {
                // 关键：判断当前线程是否仍持有锁
                // 异常时清除“领取中”标记，允许重试
                redisCache.deleteObject(pendingCacheKey);
                if (repeatLock != null && repeatLock.isHeldByCurrentThread()) {
                    try {
                        repeatLock.unlock();
                        log.debug("用户{}的奖品{}重复校验锁已释放", lotteryVo.getMemberId(), selectedPrize.getId());
                    } catch (Exception a) {
                        log.warn("用户{}的奖品{}重复校验锁释放失败",
                                lotteryVo.getMemberId(), selectedPrize.getId(), a);
                    }
                } else {
                    log.debug("用户{}的奖品{}锁未被当前线程持有，无需释放",
                            lotteryVo.getMemberId(), selectedPrize.getId());
                }
                return true;
            } finally {
                // 关键：判断当前线程是否仍持有锁
                if (repeatLock != null && repeatLock.isHeldByCurrentThread()) {
                    try {
                        repeatLock.unlock();
                        log.debug("用户{}的奖品{}重复校验锁已释放", lotteryVo.getMemberId(), selectedPrize.getId());
                    } catch (Exception e) {
                        log.warn("用户{}的奖品{}重复校验锁释放失败",
                                lotteryVo.getMemberId(), selectedPrize.getId(), e);
                    }
                } else {
                    log.debug("用户{}的奖品{}锁未被当前线程持有，无需释放",
                            lotteryVo.getMemberId(), selectedPrize.getId());
                }
            }
        }
        // 3. 城市限制校验
        if (hasCityRestriction(lotteryVo, selectedPrize)) {
            log.warn("用户城市不符合奖品{}的限制", selectedPrize.getId());
            return true;
        }
        // 4. 抽奖次数限制校验
        if (hasLotteryCountRestriction(lotteryVo, selectedPrize)) {
            log.warn("用户{}抽奖次数超过限制", lotteryVo.getMemberId());
            return true;
        }
        return false;
    }

    /**
     * 处理保底奖品逻辑（统一复用）
     */
    private LotteryUserLogVO handleGuaranteePrize(LotteryUserLogVO logVO, LotteryPrizeDO selectedPrize,
                                                  LotteryPrizeDO guaranteePrize, List<LotteryPrizeDO> prizes,
                                                  Integer prizePoolRules, Long storeId) {
        if (guaranteePrize == null) {
            setLotteryVO(logVO, selectedPrize);
            return logVO;
        }
        setLotteryVO(logVO, guaranteePrize);
        return logVO;
    }

    /**
     * 处理红包类型奖品，失败时回滚库存并切换保底奖品。
     */
    private void handleRedPacketTypePrize(LotteryVO lotteryVo, LotteryUserLogVO logVO, LotteryPrizeDO selectedPrize,
                                          List<LotteryPrizeDO> prizes, LotteryPrizeDO guaranteePrize,
                                          Integer prizePoolRules, Long storeId, Long activityId) {
        log.info("查询用户，memberId={}", lotteryVo.getMemberId());
        WxMemberVO wxMemberVO = getValidWxMember(lotteryVo.getMemberId());
        WxMemberDO wxMember = BeanUtils.toBean(wxMemberVO, WxMemberDO.class);
        log.info("用户信息:{}", wxMember);
        if (wxMember.getMemberCategory() == 1) {
            setLotteryVO(logVO, guaranteePrize);
            return;
        }

        try {
            // 红包奖品先占用库存，确认库存成功后再发起微信红包。
            if (!updatePrizeInventoryAndCache(logVO, selectedPrize, prizes, prizePoolRules, storeId)) {
                handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
                return;
            }

            LotteryRedPacketVo redPacketVo = buildRedPacketVo(wxMember, selectedPrize);
            redPacketVo.setActivityId(activityId);
            TransferToUser.TransferToUserResponse response = lotteryRedPacketService.transferUser(redPacketVo);
            if (isTransferSuccessful(response)) {
                logVO.setPackageInfo(response.getPackageInfo());
                logVO.setOutBillNo(response.getOutBillNo());
                return;
            }

            // 红包发放失败时，回滚刚刚预占的库存，再切换到保底奖品。
            rollbackPrizeInventoryAndCache(selectedPrize, prizePoolRules, storeId);
            log.warn("红包转账失败，响应：{}，切换为保底奖品", response);
            handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            rollbackPrizeInventoryAndCache(selectedPrize, prizePoolRules, storeId);
            log.warn("红包奖品库存处理中断，回滚库存后切换保底奖品，奖品ID={}", selectedPrize.getId(), e);
            handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        } catch (Exception e) {
            rollbackPrizeInventoryAndCache(selectedPrize, prizePoolRules, storeId);
            log.warn("红包发放异常，回滚库存后切换保底奖品，奖品ID={}", selectedPrize.getId(), e);
            handleGuaranteePrize(logVO, selectedPrize, guaranteePrize, prizes, prizePoolRules, storeId);
        }
    }

    /**
     * 获取并校验微信用户信息（避免重复JSON转换）
     */
    private WxMemberVO getValidWxMember(Long memberId) {
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
        if (!memberResult.isSuccess() || Objects.isNull(memberResult.getData())) {
            throw exception(USER_NOT_EXISTS);
        }
        return memberResult.getData(); // 直接使用API返回的VO，无需二次JSON转换
    }

    /**
     * 构建红包转账参数
     */
    private LotteryRedPacketVo buildRedPacketVo(WxMemberDO wxMember, LotteryPrizeDO selectedPrize) {
        LotteryRedPacketVo redPacketVo = new LotteryRedPacketVo();
        redPacketVo.setOpenId(wxMember.getOpenid());
        // 元转分（四舍五入处理）
        redPacketVo.setTransferAmount(selectedPrize.getPrizeValue()
                .multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue());
        redPacketVo.setTransferRemark("抽奖活动红包");
        return redPacketVo;
    }

    /**
     * 判断红包转账是否成功
     */
    private boolean isTransferSuccessful(TransferToUser.TransferToUserResponse response) {
        return response.getCode() == 200 && TransferBillStatus.WAIT_USER_CONFIRM.name().equals(response.getState().name());
    }

    /**
     * 统一更新奖品库存并同步缓存（复用方法）
     */
    private boolean updatePrizeInventoryAndCache(LotteryUserLogVO logVO, LotteryPrizeDO prize,
                                                 List<LotteryPrizeDO> prizes, Integer prizePoolRules, Long storeId) throws InterruptedException {
        // 兜底奖品不计入库存，直接返回成功结果。
        if (isGuaranteePrize(prize)) {
            logVO.setLotteryPrizeId(prize.getId());
            return true;
        }

        String lockKey = buildPrizeInventoryLockKey(prize, prizePoolRules, storeId);
        RLock inventoryLock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = inventoryLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("奖品库存锁获取失败，奖品ID={}", prize.getId());
                return false;
            }

            Optional<LotteryPrizeDO> latestPrizeOptional = prizes.stream()
                    .filter(item -> Objects.equals(item.getId(), prize.getId()))
                    .findFirst();
            if (latestPrizeOptional.isEmpty()) {
                log.warn("库存同步时未查询到对应奖品，奖品ID={}", prize.getId());
                return false;
            }

            LotteryPrizeDO latestPrize = latestPrizeOptional.get();
            Long usedCount = reservePrizeInventory(prize, prizePoolRules, storeId);
            if (usedCount == null || usedCount < 0) {
                log.warn("奖品库存不足，奖品ID={}", prize.getId());
                return false;
            }

            latestPrize.setRemainNum(usedCount.intValue());
            String cacheKey = resolvePrizeCacheKey(prize.getLotteryId(), prizePoolRules, storeId);
            redisCache.setCacheObject(cacheKey, prizes);
            prize.setRemainNum(latestPrize.getRemainNum());
            logVO.setLotteryPrizeId(prize.getId());
            log.debug("奖品{}已领取数量更新成功，当前={}", prize.getId(), latestPrize.getRemainNum());
            return true;
        } finally {
            releaseLock(inventoryLock, locked);
        }
    }

    // 辅助方法：库存校验（适配 remainNum=已领取数量）
    private boolean checkPrizeInventory(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        Integer usedCount = getPrizeInventoryUsedCount(prize, prizePoolRules, storeId);
        if (usedCount != null) {
            if (prizePoolRules == 2) {
                return prize.getStoreId().equals(storeId) && usedCount < prize.getPrizeNum();
            }
            return usedCount < prize.getPrizeNum();
        }
        // 独立奖池需先匹配门店，再校验已领取数量 < 总数量
        if (prizePoolRules == 2) {
            return prize.getStoreId().equals(storeId) && prize.getRemainNum() < prize.getPrizeNum();
        }
        // 共用奖池直接校验已领取数量 < 总数量
        return prize.getRemainNum() < prize.getPrizeNum();
    }

    /**
     * 生成奖品库存锁键。
     */
    private String buildPrizeInventoryLockKey(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        if (prizePoolRules == 2) {
            return "prizeInventory:lock:lottery:" + prize.getLotteryId() + ":store:" + storeId + ":prize:" + prize.getId();
        }
        return "prizeInventory:lock:lottery:" + prize.getLotteryId() + ":prize:" + prize.getId();
    }

    /**
     * 奖品缓存主键使用活动ID；拿不到配置时兼容使用入参ID。
     */
    private String resolvePrizeCacheKey(Long lotteryId, Integer prizePoolRules, Long storeId) {
        LotterySettingsCacheDataVO settings = getLotterySettings(lotteryId);
        Long cacheLotteryId = settings == null || settings.getActivityId() == null ? lotteryId : settings.getActivityId();
        return buildPrizeCacheKey(cacheLotteryId, prizePoolRules, storeId);
    }

    /**
     * 回滚缓存中已预占的奖品库存。
     */
    private void rollbackPrizeInventoryAndCache(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        if (isGuaranteePrize(prize)) {
            return;
        }
        String lockKey = buildPrizeInventoryLockKey(prize, prizePoolRules, storeId);
        RLock inventoryLock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = inventoryLock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("回滚奖品库存时未获取到锁，奖品ID={}", prize.getId());
                return;
            }
            Long usedCount = rollbackPrizeInventory(prize, prizePoolRules, storeId);
            List<LotteryPrizeDO> latestPrizes = getLotteryPrizes(prize.getLotteryId(), prizePoolRules, storeId);
            if (CollectionUtils.isEmpty(latestPrizes)) {
                return;
            }
            latestPrizes.stream()
                    .filter(item -> Objects.equals(item.getId(), prize.getId()))
                    .findFirst()
                    .ifPresent(item -> item.setRemainNum(usedCount == null ? Math.max(0, item.getRemainNum() - 1) : usedCount.intValue()));
            redisCache.setCacheObject(buildPrizeCacheKey(prize.getLotteryId(), prizePoolRules, storeId), latestPrizes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("回滚奖品库存时线程被中断，奖品ID={}", prize.getId(), e);
        } finally {
            releaseLock(inventoryLock, locked);
        }
    }

    /**
     * 原子预占奖品库存。
     * Redis中的PRIZE_NUM作为库存真源，返回值为最新已用库存，-1表示库存不足。
     */
    private Long reservePrizeInventory(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        String stockKey = buildPrimaryPrizeStockKey(prize, prizePoolRules, storeId);
        String legacyStockKey = buildLegacyPrizeStockKey(prize, prizePoolRules, storeId);
        String currentValue = stringRedisTemplate.opsForValue().get(stockKey);
        if (StringUtils.isEmpty(currentValue) && StringUtils.isNotEmpty(legacyStockKey)) {
            currentValue = stringRedisTemplate.opsForValue().get(legacyStockKey);
        }
        String initUsedCount = currentValue;
        if (currentValue == null) {
            // 库存key首次不存在时，用数据库当前已用库存做初始化，避免从0开始导致超发。
            LotteryPrizeDO latestPrize = queryPrizeById(prize.getId(), prizePoolRules, storeId);
            initUsedCount = String.valueOf(latestPrize == null ? ObjectUtil.defaultIfNull(prize.getRemainNum(), 0) : ObjectUtil.defaultIfNull(latestPrize.getRemainNum(), 0));
        }
        Long usedCount = stringRedisTemplate.execute(
                RESERVE_PRIZE_STOCK_SCRIPT,
                Collections.singletonList(stockKey),
                initUsedCount,
                String.valueOf(prize.getPrizeNum())
        );
        syncLegacyPrizeStockKey(legacyStockKey, usedCount);
        return usedCount;
    }

    /**
     * 回滚已预占的奖品库存。
     * 发奖失败时退回Redis库存，并保证库存key不会回滚成负数。
     */
    private Long rollbackPrizeInventory(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        String stockKey = buildPrimaryPrizeStockKey(prize, prizePoolRules, storeId);
        String legacyStockKey = buildLegacyPrizeStockKey(prize, prizePoolRules, storeId);
        Long usedCount = stringRedisTemplate.execute(
                ROLLBACK_PRIZE_STOCK_SCRIPT,
                Collections.singletonList(stockKey)
        );
        syncLegacyPrizeStockKey(legacyStockKey, usedCount);
        return usedCount;
    }

    /**
     * 读取当前奖品在Redis中的已用库存。
     * 这里只读单独库存key，不依赖整包奖品列表缓存。
     */
    private Integer getPrizeInventoryUsedCount(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        String stockKey = buildPrimaryPrizeStockKey(prize, prizePoolRules, storeId);
        String legacyStockKey = buildLegacyPrizeStockKey(prize, prizePoolRules, storeId);
        String stockValue = stringRedisTemplate.opsForValue().get(stockKey);
        if (StringUtils.isEmpty(stockValue) && StringUtils.isNotEmpty(legacyStockKey)) {
            stockValue = stringRedisTemplate.opsForValue().get(legacyStockKey);
            if (StringUtils.isNotEmpty(stockValue)) {
                stringRedisTemplate.opsForValue().set(stockKey, stockValue);
            }
        }
        if (StringUtils.isEmpty(stockValue)) {
            return null;
        }
        try {
            return Integer.parseInt(stockValue);
        } catch (NumberFormatException e) {
            log.warn("奖品库存缓存格式异常，奖品ID={}，缓存值={}", prize.getId(), stockValue);
            return null;
        }
    }

    /**
     * 构建奖品库存真源key。
     * 共用奖池按活动+奖品隔离，独立奖池额外增加门店维度。
     */
    private String buildPrizeStockKey(Long lotteryId, Long prizeId, Integer prizePoolRules, Long storeId) {
        if (prizePoolRules == 2) {
            return RedisKeyConstants.PRIZE_NUM + lotteryId + ":store:" + storeId + ":prize:" + prizeId;
        }
        return RedisKeyConstants.PRIZE_NUM + lotteryId + ":prize:" + prizeId;
    }

    private String buildPrimaryPrizeStockKey(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        return buildPrizeStockKey(resolvePrizeBizActivityId(prize.getLotteryId()), prize.getId(), prizePoolRules, storeId);
    }

    private String buildLegacyPrizeStockKey(LotteryPrizeDO prize, Integer prizePoolRules, Long storeId) {
        Long activityId = resolvePrizeBizActivityId(prize.getLotteryId());
        if (Objects.equals(activityId, prize.getLotteryId())) {
            return null;
        }
        return buildPrizeStockKey(prize.getLotteryId(), prize.getId(), prizePoolRules, storeId);
    }

    private void syncLegacyPrizeStockKey(String legacyStockKey, Long usedCount) {
        if (StringUtils.isNotEmpty(legacyStockKey) && usedCount != null) {
            stringRedisTemplate.opsForValue().set(legacyStockKey, String.valueOf(usedCount));
        }
    }

    private Long resolvePrizeBizActivityId(Long lotteryId) {
        LotterySettingsCacheDataVO settings = getLotterySettings(lotteryId);
        if (settings == null || settings.getActivityId() == null) {
            return lotteryId;
        }
        return settings.getActivityId();
    }

    /**
     * 按当前奖池规则查询单个奖品，用于库存key首次初始化时回源数据库。
     */
    private LotteryPrizeDO queryPrizeById(Long prizeId, Integer prizePoolRules, Long storeId) {
        LambdaQueryWrapperX<LotteryPrizeDO> wrapper = new LambdaQueryWrapperX<LotteryPrizeDO>()
                .eq(LotteryPrizeDO::getId, prizeId);
        if (prizePoolRules == 2) {
            wrapper.eq(LotteryPrizeDO::getStoreId, storeId);
        }
        return lotteryPrizeMapper.selectOne(wrapper);
    }

    /**
     * 判断当前奖品是否为兜底奖品。
     */
    private boolean isGuaranteePrize(LotteryPrizeDO prize) {
        return prize != null && Objects.equals(prize.getIsGuarantees(), 1);
    }


    /**
     * 更新抽奖次数（支持按天/按场次 + 总次数 + 特殊方式3）
     */
    private void updateLotteryCount(LotteryVO lotteryVo, LotteryUserLogVO logVO) {
        try {
            LotterySettingsCacheDataVO settings = getLotterySettings(lotteryVo.getLotteryId());
            if (settings == null) {
                return;
            }
            String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
            Integer calculationRules = settings.getCalculationRules(); // 1=按天，2=按场次

            // 1. 根据计算规则更新时段内次数（每日 or 场次）
            if (calculationRules == 1) {
                // 更新每日抽奖次数
                updateDailyLotteryCount(lotteryVo, settings, currDate);
            } else if (calculationRules == 2) {
                // 更新当前场次抽奖次数
                updateSessionLotteryCount(lotteryVo, settings, currDate);
            } else {
                // 未知规则默认按天更新（兼容旧逻辑）
                updateDailyLotteryCount(lotteryVo, settings, currDate);
            }

            // 2. 更新总抽奖次数（无论按天/按场次，总次数同步更新）
            updateTotalLotteryCount(lotteryVo, settings);

            // 3. 特殊方式3的次数更新（保持原有逻辑）
            if (settings.getLotteryMethod() == 3) {
                updateCommodityLotteryCount(lotteryVo, settings, currDate);
            }

        } catch (Exception e) {
            log.warn("更新抽奖次数失败", e);
            forceUpdateLotteryCount(lotteryVo);
        }
    }

    /**
     * 缓存更新失败时，直接查表强制回写抽奖次数。
     */
    private void forceUpdateLotteryCount(LotteryVO lotteryVo) {
        try {
            LotterySettingsCacheDataVO settings = getLotterySettings(lotteryVo.getLotteryId());
            if (settings == null) {
                return;
            }
            String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
            if (Objects.equals(settings.getCalculationRules(), 2)) {
                forceRefreshSessionLotteryCount(lotteryVo, settings, currDate);
            } else {
                forceRefreshDailyLotteryCount(lotteryVo, settings, currDate);
            }
            forceRefreshTotalLotteryCount(lotteryVo, settings);
            if (settings.getLotteryMethod() == 3) {
                forceRefreshCommodityLotteryCount(lotteryVo, settings);
            }
        } catch (Exception ex) {
            log.warn("强制回写抽奖次数失败", ex);
        }
    }

    /**
     * 抽奖异常时回滚已预占的抽奖次数。
     */
    private void rollbackLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, boolean reserved) {
        if (!reserved || settings == null) {
            return;
        }
        try {
            String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
            if (Objects.equals(settings.getCalculationRules(), 2)) {
                rollbackSessionLotteryCount(lotteryVo, settings, currDate);
            } else {
                rollbackDailyLotteryCount(lotteryVo, settings, currDate);
            }
            rollbackTotalLotteryCount(lotteryVo, settings);
            if (settings.getLotteryMethod() == 3) {
                rollbackCommodityLotteryCount(lotteryVo, settings);
            }
        } catch (Exception ex) {
            log.warn("回滚抽奖次数失败", ex);
        }
    }

    /**
     * 新规则下预占抽奖来源和聚合抽奖次数。
     * 先占具体来源，再占聚合次数；失败时由上层统一回滚。
     */
    private ReservedDrawChance reserveMultiChance(LotterySettingsCacheDataVO settings,
                                                  LotteryVO lotteryVo,
                                                  WxMemberVO wxMember) {
        validateMultiTotalLimit(settings, lotteryVo.getMemberId());
        ReservedDrawChance reserved = reserveSourceChance(settings, lotteryVo.getMemberId(), wxMember);
        try {
            String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
            if (Objects.equals(settings.getCalculationRules(), 2)) {
                updateMultiSessionLotteryCount(lotteryVo, settings, currDate);
            } else {
                updateMultiDailyLotteryCount(lotteryVo, settings, currDate);
            }
            reserved.scopeCounterReserved = true;
            updateMultiTotalLotteryCount(lotteryVo, settings);
            reserved.totalCounterReserved = true;
            return reserved;
        } catch (Exception e) {
            rollbackMultiChance(settings, lotteryVo, reserved);
            throw e;
        }
    }

    private void rollbackMultiChance(LotterySettingsCacheDataVO settings,
                                     LotteryVO lotteryVo,
                                     ReservedDrawChance reserved) {
        if (settings == null || reserved == null) {
            return;
        }
        try {
            if (reserved.totalCounterReserved) {
                rollbackMultiTotalLotteryCount(lotteryVo, settings);
            }
            if (reserved.scopeCounterReserved) {
                String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
                if (Objects.equals(settings.getCalculationRules(), 2)) {
                    rollbackMultiSessionLotteryCount(lotteryVo, settings, currDate);
                } else {
                    rollbackMultiDailyLotteryCount(lotteryVo, settings, currDate);
                }
            }
            rollbackSourceChance(reserved);
        } catch (Exception ex) {
            log.warn("回滚多来源抽奖次数失败", ex);
        }
    }

    private ReservedDrawChance reserveSourceChance(LotterySettingsCacheDataVO settings,
                                                   Long memberId,
                                                   WxMemberVO wxMember) {
        TaskScope scope = buildTaskScope(settings);
        Long activityId = resolveLotteryBizActivityId(settings);
        ReservedDrawChance freeChance = tryReserveFreeChance(settings, memberId, scope);
        if (freeChance != null) {
            return freeChance;
        }
        if (Objects.equals(settings.getOrderStatus(), 1)) {
            ReservedDrawChance orderChance = tryReserveTaskChance(activityId, memberId, LotteryTaskTypeEnum.ORDER, scope, isUnlimitedOrderTask(settings));
            if (orderChance != null) {
                return orderChance;
            }
        }
        if (Objects.equals(settings.getShareEvent(), 1)) {
            ReservedDrawChance shareChance = tryReserveTaskChance(activityId, memberId, LotteryTaskTypeEnum.SHARE, scope, false);
            if (shareChance != null) {
                return shareChance;
            }
        }
        if (Objects.equals(settings.getBrowseType(), 1)) {
            ReservedDrawChance browseChance = tryReserveTaskChance(activityId, memberId, LotteryTaskTypeEnum.BROWSE, scope, false);
            if (browseChance != null) {
                return browseChance;
            }
        }
        ReservedDrawChance pointsChance = tryReservePointsChance(settings, memberId, wxMember, scope);
        if (pointsChance != null) {
            return pointsChance;
        }
        if (Objects.equals(settings.getPointsStatus(), 1)
                && (wxMember == null || wxMember.getMemberIntegral() == null || wxMember.getMemberIntegral() < defaultZero(settings.getPrice()))
                && !Objects.equals(settings.getFreeStatus(), 1)
                && !Objects.equals(settings.getOrderStatus(), 1)
                && !Objects.equals(settings.getShareEvent(), 1)
                && !Objects.equals(settings.getBrowseType(), 1)) {
            throw exception(LOTTERY_INSUFFICIENT_POINTS);
        }
        if (Objects.equals(settings.getCalculationRules(), 2)) {
            throw exception(LOTTERY_SESSION_LIMIT_REACHED);
        }
        throw exception(LOTTERY_LIMIT_REACHED_AGAIN);
    }

    private ReservedDrawChance tryReserveFreeChance(LotterySettingsCacheDataVO settings, Long memberId, TaskScope scope) {
        if (!Objects.equals(settings.getFreeStatus(), 1)) {
            return null;
        }
        LotteryTaskDO taskDO = getScopedLotteryTask(resolveLotteryBizActivityId(settings), memberId,
                LotteryTaskTypeEnum.FREE.getCode(), false, scope, true);
        int usedCount = defaultZero(taskDO.getConsumeCount());
        if (usedCount >= defaultZero(settings.getFreeCount())) {
            return null;
        }
        taskDO.setConsumeCount(usedCount + 1);
        updateLotteryTask(taskDO);
        return ReservedDrawChance.of(LotteryTaskTypeEnum.FREE, taskDO, scope, false, defaultZero(settings.getPrice()));
    }

    private ReservedDrawChance tryReservePointsChance(LotterySettingsCacheDataVO settings,
                                                      Long memberId,
                                                      WxMemberVO wxMember,
                                                      TaskScope scope) {
        if (!Objects.equals(settings.getPointsStatus(), 1)) {
            return null;
        }
        if (wxMember == null || wxMember.getMemberIntegral() == null || wxMember.getMemberIntegral() < defaultZero(settings.getPrice())) {
            return null;
        }
        boolean unlimited = isUnlimitedPoints(settings);
        LotteryTaskDO taskDO = getScopedLotteryTask(resolveLotteryBizActivityId(settings), memberId,
                LotteryTaskTypeEnum.POINTS.getCode(), unlimited, scope, true);
        int usedCount = defaultZero(taskDO.getConsumeCount());
        if (!unlimited && usedCount >= defaultZero(settings.getPointsCount())) {
            return null;
        }
        taskDO.setConsumeCount(usedCount + 1);
        updateLotteryTask(taskDO);
        return ReservedDrawChance.of(LotteryTaskTypeEnum.POINTS, taskDO, scope, unlimited, defaultZero(settings.getPrice()));
    }

    private ReservedDrawChance tryReserveTaskChance(Long activityId,
                                                    Long memberId,
                                                    LotteryTaskTypeEnum taskTypeEnum,
                                                    TaskScope scope,
                                                    boolean unlimited) {
        LotteryTaskDO taskDO = getScopedLotteryTask(activityId, memberId, taskTypeEnum.getCode(), unlimited, scope, false);
        if (taskDO == null) {
            return null;
        }
        int availableCount = calculateTaskAvailableCount(taskDO);
        if (availableCount <= 0) {
            return null;
        }
        taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()) + 1);
        updateLotteryTask(taskDO);
        return ReservedDrawChance.of(taskTypeEnum, taskDO, scope, unlimited, 0);
    }

    private void rollbackSourceChance(ReservedDrawChance reserved) {
        if (reserved == null || reserved.taskDO == null) {
            return;
        }
        LotteryTaskDO latestTask = getScopedLotteryTask(reserved.activityId,
                reserved.memberId,
                reserved.taskType,
                reserved.unlimited,
                reserved.scope,
                false);
        if (latestTask == null) {
            return;
        }
        latestTask.setConsumeCount(Math.max(defaultZero(latestTask.getConsumeCount()) - 1, 0));
        updateLotteryTask(latestTask);
    }

    private TaskScope buildTaskScope(LotterySettingsCacheDataVO settings) {
        if (Objects.equals(settings.getCalculationRules(), 2)) {
            List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
            SessionTime currentSession = getCurrentSession(sessionTimes, LocalTime.now());
            if (currentSession == null) {
                throw exception(LOTTERY_SESSION_NOT_IN_RANGE);
            }
            LocalDate today = LocalDate.now();
            LocalDateTime start = LocalDateTime.of(today, currentSession.getStartLocalTime());
            LocalDateTime end = currentSession.isAllDay()
                    ? LocalDateTime.of(today, LocalTime.of(23, 59, 59))
                    : LocalDateTime.of(today, currentSession.getEndLocalTime());
            String scopeKey = currentSession.isAllDay()
                    ? today.format(DateTimeFormatter.BASIC_ISO_DATE) + "_ALLDAY"
                    : today.format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "_" + currentSession.getStartTimeStr().replace(":", "")
                    + currentSession.getEndTimeStr().replace(":", "");
            return new TaskScope(scopeKey, start, end, true);
        }
        LocalDate today = LocalDate.now();
        return new TaskScope(
                today.format(DateTimeFormatter.BASIC_ISO_DATE),
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
                false
        );
    }

    private LotteryTaskDO getScopedLotteryTask(Long activityId,
                                               Long memberId,
                                               Integer taskType,
                                               boolean unlimited,
                                               TaskScope scope,
                                               boolean createIfMissing) {
        LotterySettingsCacheDataVO config = getLotterySettings(activityId);
        if (lotteryV2Service.enabled(config)) {
            long[] values = lotteryLedger.counter(BusinessContextHolder.getRequiredBusinessId(), activityId, memberId,
                    unlimited ? "TOTAL" : scope.scopeKey, taskType);
            LotteryTaskDO row = new LotteryTaskDO(); row.setActivityId(activityId); row.setMemberId(memberId); row.setTaskType(taskType);
            row.setGainCount(Math.toIntExact(values[0])); row.setConsumeCount(Math.toIntExact(values[1])); row.setFinishCount(Math.toIntExact(values[2]));
            return row;
        }

        LotteryTaskDO taskDO = lotteryTaskMapper.selectOne(new LambdaQueryWrapperX<LotteryTaskDO>()
                .eq(LotteryTaskDO::getActivityId, activityId)
                .eq(LotteryTaskDO::getMemberId, memberId)
                .eq(LotteryTaskDO::getTaskType, taskType)
                .last("limit 1"));
        if (taskDO == null) {
            if (!createIfMissing) {
                return null;
            }
            taskDO = new LotteryTaskDO();
            taskDO.setActivityId(activityId);
            taskDO.setMemberId(memberId);
            taskDO.setTaskType(taskType);
            taskDO.setFinishCount(0);
            taskDO.setGainCount(0);
            taskDO.setConsumeCount(0);
            try {
                lotteryTaskMapper.insert(taskDO);
                return taskDO;
            } catch (DuplicateKeyException e) {
                taskDO = lotteryTaskMapper.selectOne(new LambdaQueryWrapperX<LotteryTaskDO>()
                        .eq(LotteryTaskDO::getActivityId, activityId)
                        .eq(LotteryTaskDO::getMemberId, memberId)
                        .eq(LotteryTaskDO::getTaskType, taskType)
                        .last("limit 1"));
                if (taskDO == null) {
                    throw e;
                }
            }
        }
        if (!unlimited && scope != null && !isTaskInScope(taskDO, scope)) {
            taskDO.setFinishCount(0);
            taskDO.setGainCount(0);
            taskDO.setConsumeCount(0);
            taskDO.setUpdateTime(LocalDateTime.now());
            updateLotteryTask(taskDO);
        }
        return taskDO;
    }

    private boolean isTaskInScope(LotteryTaskDO taskDO, TaskScope scope) {
        LocalDateTime compareTime = taskDO.getUpdateTime() == null ? taskDO.getCreateTime() : taskDO.getUpdateTime();
        if (compareTime == null) {
            return false;
        }
        return !compareTime.isBefore(scope.startTime) && compareTime.isBefore(scope.endTime);
    }

    private void updateLotteryTask(LotteryTaskDO taskDO) {
        UpdateWrapper<LotteryTaskDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", taskDO.getId())
                .eq("member_id", taskDO.getMemberId())
                .set("finish_count", defaultZero(taskDO.getFinishCount()))
                .set("gain_count", defaultZero(taskDO.getGainCount()))
                .set("consume_count", defaultZero(taskDO.getConsumeCount()))
                .set("update_time", taskDO.getUpdateTime() == null ? LocalDateTime.now() : taskDO.getUpdateTime());
        if (lotteryTaskMapper.update(null, updateWrapper) <= 0) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
    }

    /**
     * 强制回写每日抽奖次数缓存。
     */
    private void forceRefreshDailyLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        String cacheKey = buildLegacyDailyCacheKey(settings, lotteryVo.getMemberId(), currDate);
        Integer count = lotteryAddLogService.selectLotteryNum(lotteryVo.getMemberId(), settings.getId());
        redisCache.setCacheObject(cacheKey, count);
        Duration duration = getDuration();
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 强制回写当前场次抽奖次数缓存。
     */
    private void forceRefreshSessionLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            return;
        }
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            return;
        }
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String sessionCacheKey = buildLegacySessionCacheKey(settings, lotteryVo.getMemberId(), currDate, sessionKey);
        Integer count = lotteryAddLogService.selectLotteryNumBySession(
                lotteryVo.getMemberId(),
                settings.getId(),
                currentSession.getStartLocalTime(),
                currentSession.isAllDay() ? LocalTime.of(23, 59, 59) : currentSession.getEndLocalTime()
        );
        redisCache.setCacheObject(sessionCacheKey, count);
        long expireSeconds = currentSession.isAllDay()
                ? Duration.between(currentTime, LocalTime.of(23, 59, 59)).getSeconds()
                : Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds();
        redisCache.expire(sessionCacheKey, Math.max(expireSeconds, 1), TimeUnit.SECONDS);
    }

    /**
     * 强制回写总抽奖次数缓存。
     */
    private void forceRefreshTotalLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String totalCacheKey = buildLegacyTotalCacheKey(settings, lotteryVo.getMemberId());
        Integer count = lotteryAddLogService.selectLotterySum(lotteryVo.getMemberId(), settings.getId());
        redisCache.setCacheObject(totalCacheKey, count);
    }

    /**
     * 强制回写商品抽奖次数缓存。
     */
    private void forceRefreshCommodityLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String cacheKey = buildCommodityLotteryKey(lotteryVo.getMemberId(), settings, today);
        Integer count = null;
        try {
            count = redisCache.getOpsForValue(cacheKey);
        } catch (Exception ignored) {
        }
        redisCache.setCacheObject(cacheKey, ObjectUtil.defaultIfNull(count, 0));
        Duration duration = getDuration();
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 判断异常是否属于抽奖次数已用尽。
     */
    private boolean isLotteryCountLimitException(Exception e) {
        if (!(e instanceof ServiceException serviceException)) {
            return false;
        }
        return Objects.equals(serviceException.getCode(), LOTTERY_NO_CHANCE.getCode())
                || Objects.equals(serviceException.getCode(), LOTTERY_LIMIT_REACHED_AGAIN.getCode())
                || Objects.equals(serviceException.getCode(), LOTTERY_TOT_LIMIT_REACHED_AGAIN.getCode())
                || Objects.equals(serviceException.getCode(), LOTTERY_SESSION_LIMIT_REACHED.getCode());
    }

// ------------------------------ 新增：更新场次抽奖次数 ------------------------------

    /**
     * 更新当前场次的抽奖次数（calculationRules=2时调用）
     */
    private void updateSessionLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        Long memberId = lotteryVo.getMemberId();
        Long lotterySettingId = settings.getId();

        // 1. 解析当前活动的场次配置
        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            log.warn("更新场次次数失败：无有效场次配置，lotteryId：{}", lotterySettingId);
            return; // 配置错误，不更新缓存（依赖数据库最终一致）
        }

        // 2. 判断当前时间所属场次（抽奖行为发生的时间）
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            log.warn("当前不在有效场次内，不更新场次次数，memberId：{}，lotteryId：{}", memberId, lotterySettingId);
            return; // 不在场次内，无需更新
        }

        // 3. 生成当前场次的缓存键（与校验/查询逻辑保持一致）
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String sessionCacheKey = buildLegacySessionCacheKey(settings, memberId, currDate, sessionKey);

        // 4. 更新缓存中的场次次数（自增1）
        try {
            // 普通缓存自增（适用于非method=3的场景）
            Integer count = redisCache.getCacheObject(sessionCacheKey);

            if (count != null) {
                stringRedisTemplate.opsForValue().increment(sessionCacheKey);
            } else {
                count = lotteryAddLogService.selectLotteryNumBySession(lotteryVo.getMemberId(),
                        settings.getId(),
                        currentSession.getStartLocalTime(),
                        currentSession.getEndLocalTime());
                redisCache.setCacheObject(sessionCacheKey, count + 1);
            }
            // 设置缓存过期时间（与查询时一致，避免缓存长期无效）
            long expireSeconds = currentSession.isAllDay()
                    ? Duration.between(currentTime, LocalTime.of(23, 59, 59)).getSeconds()
                    : Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds();
            redisCache.expire(sessionCacheKey, expireSeconds, TimeUnit.SECONDS);

            log.debug("更新场次次数成功，cacheKey：{}", sessionCacheKey);
        } catch (Exception e) {
            log.warn("Redis更新场次次数失败，cacheKey：{}，异常：{}", sessionCacheKey, e.getMessage(), e);
            // 缓存更新失败不抛异常，依赖数据库同步（最终一致性）
        }
    }

    /**
     * 回滚每日抽奖次数缓存。
     */
    private void rollbackDailyLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        String cacheKey = buildLegacyDailyCacheKey(settings, lotteryVo.getMemberId(), currDate);
        decrementLotteryCountCache(cacheKey);
        Duration duration = getDuration();
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 回滚总抽奖次数缓存。
     */
    private void rollbackTotalLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String cacheKey = buildLegacyTotalCacheKey(settings, lotteryVo.getMemberId());
        decrementLotteryCountCache(cacheKey);
        Duration duration = calculateCacheDuration(new Date(), settings.getLotteryStartTime(), settings.getLotteryEndTime());
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 回滚商品抽奖剩余次数缓存。
     */
    private void rollbackCommodityLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String cacheKey = buildCommodityLotteryKey(lotteryVo.getMemberId(), settings, today);
        try {
            stringRedisTemplate.opsForValue().increment(cacheKey);
            Duration duration = getDuration();
            redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("回滚商品抽奖次数失败，cacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
        }
    }

    /**
     * 回滚当前场次抽奖次数缓存。
     */
    private void rollbackSessionLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            return;
        }
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            return;
        }
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String sessionCacheKey = buildLegacySessionCacheKey(settings, lotteryVo.getMemberId(), currDate, sessionKey);
        decrementLotteryCountCache(sessionCacheKey);
        long expireSeconds = currentSession.isAllDay()
                ? Duration.between(currentTime, LocalTime.of(23, 59, 59)).getSeconds()
                : Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds();
        redisCache.expire(sessionCacheKey, Math.max(expireSeconds, 1), TimeUnit.SECONDS);
    }

    /**
     * 通用回滚：将次数缓存减一，并兜底避免出现负数。
     */
    private void decrementLotteryCountCache(String cacheKey) {
        Long remainingCount = stringRedisTemplate.opsForValue().decrement(cacheKey);
        if (remainingCount == null || remainingCount < 0) {
            redisCache.setCacheObject(cacheKey, 0);
        }
    }

    /**
     * 异步处理抽奖结果（适配共用/独立奖池，补充充奖池规则和门店信息）
     *
     * @param lotteryVo      抽奖请求参数
     * @param logVO          抽奖结果对象
     * @param prizePoolRules 奖池规则（1=共用奖池，2=独立奖池）
     * @param storeId        门店ID（独立奖池时使用）
     */
    private void asyncProcessLotteryResult(LotteryVO lotteryVo, LotteryUserLogVO logVO,
                                           Integer prizePoolRules, Long storeId,
                                           ReservedDrawChance reservedChance) {
        log.info("异步处理数据：活动ID={}，奖池规则={}，门店ID={}，{----------------------}",
                lotteryVo.getLotteryId(), prizePoolRules, storeId);

        threadPoolTaskExecutor.submit(() -> {
            try {
                // 传入奖池规则和门店ID，确保异步更新的数据与当前奖池匹配
                updateData(lotteryVo, logVO, prizePoolRules, storeId, reservedChance);
            } catch (Exception e) {
                log.warn("异步处理数据失败（活动ID={}，规则={}，门店={}）",
                        lotteryVo.getLotteryId(), prizePoolRules, storeId, e);
            }
        });
    }

    /**
     * 释放分布式锁
     */
    private void releaseLock(RLock lock, boolean isLocked) {
        if (isLocked && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("解锁成功--->当前线程--->" + Thread.currentThread().getId());
        }
    }

// 以下为辅助方法

    /**
     * 获取保底奖品
     */
    private LotteryPrizeDO getGuaranteePrize(List<LotteryPrizeDO> prizes) {
        return prizes.stream()
                .filter(prize -> prize.getIsGuarantees() == 1)
                .findFirst()
                .orElse(null);
    }

    /**
     * 计算总概率
     */
    private BigDecimal calculateTotalProbability(List<LotteryPrizeDO> prizes) {
        return prizes.stream()
                .map(LotteryPrizeDO::getProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 生成随机概率值
     */
    private BigDecimal generateRandomValue(BigDecimal totalProbability) {
        return new BigDecimal(new Random().nextDouble()).multiply(totalProbability);
    }

    /**
     * 检查用户是否已中过该奖品
     */
    private boolean hasUserWonPrize(LotteryVO lotteryVo, LotteryPrizeDO prize) {

        int count = lotteryLogMapper.selectCount(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, lotteryVo.getMemberId())
                .eq(LotteryLogDO::getLotteryId, lotteryVo.getLotteryId())
                .eq(LotteryLogDO::getLotteryPrizeId, prize.getId())).intValue();


        return count > 0;
    }

    /**
     * 检查城市限制
     */
    private boolean hasCityRestriction(LotteryVO lotteryVo, LotteryPrizeDO prize) {
        if (StringUtils.isEmpty(prize.getWinningCitys())) {
            return false;
        }
        return StringUtils.isEmpty(lotteryVo.getCityName())
                || !prize.getWinningCitys().contains(lotteryVo.getCityName());
    }

    /**
     * 检查抽奖次数限制
     */
    private boolean hasLotteryCountRestriction(LotteryVO lotteryVo, LotteryPrizeDO prize) {
        if (prize.getMinimumNumber() == null) {
            return false;
        }

        try {
            LotterySettingsCacheDataVO settings = getLotterySettings(lotteryVo.getLotteryId());
            Long activityId = settings == null ? lotteryVo.getLotteryId() : resolveLotteryBizActivityId(settings);
            String cacheKey = RedisKeyConstants.LOTTERY_SUM + ":" + activityId;
            Integer count = redisCache.getCacheObject(cacheKey);

            if (count == null) {
                count = lotteryLogMapper.selectCount(new LambdaQueryWrapperX<LotteryLogDO>()
                        .eq(LotteryLogDO::getLotteryId, lotteryVo.getLotteryId())).intValue();
            }

            return count < prize.getMinimumNumber();
        } catch (Exception e) {
            log.warn("检查抽奖次数限制异常", e);
            return true;
        }
    }

    /**
     * 更新奖品库存（适配共用/独立奖池，确保库存操作的准确性和隔离性）
     *
     * @param prizes         奖品列表（已按奖池规则筛选）
     * @param prizeId        待更新库存的奖品ID
     * @param prizePoolRules 奖池规则（1=共用奖池，2=独立奖池）
     * @param storeId        门店ID（独立奖池时使用）
     */
    private void updatePrizeInventory(List<LotteryPrizeDO> prizes, Long prizeId,
                                      Integer prizePoolRules, Long storeId) {
        // 遍历奖品列表，找到目标奖品并更新库存
        for (LotteryPrizeDO prize : prizes) {
            if (prize.getId().equals(prizeId)) {
                // 独立奖池额外校验：确保当前奖品属于目标门店，避免跨门店更新库存
                if (prizePoolRules == 2 && !prize.getStoreId().equals(storeId)) {
                    log.warn("独立奖池库存更新失败：奖品ID={}不属于门店ID={}", prizeId, storeId);
                    throw new RuntimeException("库存更新异常：奖品与门店不匹配");
                }


                int newRemainNum = prize.getRemainNum() + 1;
                if (newRemainNum < 0) {
                    log.warn("奖品ID={}库存不足，当前领取数量={}，扣减后为负数", prizeId, prize.getRemainNum());
                    // 可选：严格校验时抛出异常，防止超发
                    // throw new RuntimeException("奖品库存不足");
                }
                prize.setRemainNum(newRemainNum);
                log.debug("库存更新成功：奖品ID={}，原领取={}，新领取={}，规则={}，门店={}",
                        prizeId, prize.getRemainNum(), newRemainNum, prizePoolRules, storeId);
                break;
            }
        }
    }

    /**
     * 更新每日抽奖次数
     */
    private void updateDailyLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        String cacheKey = buildLegacyDailyCacheKey(settings, lotteryVo.getMemberId(), currDate);
        Integer count = redisCache.getCacheObject(cacheKey);

        if (count != null) {
            stringRedisTemplate.opsForValue().increment(cacheKey);
        } else {
            count = lotteryAddLogService.selectLotteryNum(lotteryVo.getMemberId(), settings.getId());
            redisCache.setCacheObject(cacheKey, count + 1);
        }

        Duration duration = getDuration();
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 更新总抽奖次数
     */
    private void updateTotalLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String cacheKey = buildLegacyTotalCacheKey(settings, lotteryVo.getMemberId());
        Integer sumCount = redisCache.getCacheObject(cacheKey);

        if (sumCount != null) {
            stringRedisTemplate.opsForValue().increment(cacheKey);
        } else {
            sumCount = lotteryAddLogService.selectLotterySum(lotteryVo.getMemberId(), settings.getId());
            redisCache.setCacheObject(cacheKey, sumCount + 1);
        }

        Duration duration = calculateCacheDuration(new Date(), settings.getLotteryStartTime(), settings.getLotteryEndTime());
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 更新下单抽奖次数
     */
    private void updateCommodityLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String cacheKey = buildCommodityLotteryKey(lotteryVo.getMemberId(), settings, today);
        try {
            // 通过RedisTemplate的decrement实现原子减操作
            Long remainingCount = redisCache.getRedisTemplate().opsForValue().decrement(cacheKey);
            // 检查操作结果
            if (remainingCount != null) {
                if (remainingCount >= 0) {
                    log.info("商品抽奖次数更新成功，CacheKey：{}，剩余次数：{}", cacheKey, remainingCount);
                } else {
                    log.warn("商品抽奖次数已用尽，CacheKey：{}", cacheKey);
                    // 重置为0，避免出现负数
                    redisCache.setCacheObject(cacheKey, 0);
                }
            } else {
                redisCache.setCacheObject(cacheKey, 0);
            }
        } catch (Exception e) {
            log.warn("Redis更新商品抽奖次数异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
    }

    /**
     * 抽奖次数
     */
    @Override
    public LotterySettingsNumVo lotteryNum(LotteryVO lotteryVO) {
        LotterySettingsNumVo resultVo = new LotterySettingsNumVo();
        String currentDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());

        // 校验 lotteryId 是否为空
        if (ObjectUtil.isEmpty(lotteryVO.getLotteryId())) {
            log.warn("LotteryID 为null");
            return resultVo;
        }

        try {
            // 获取并缓存 lottery 设置
            LotterySettingsCacheDataVO lotterySettings = getLotterySettings(lotteryVO.getLotteryId());
            if (lotterySettings == null) {
                return resultVo;
            }
            lotteryVO.setLotteryId(lotterySettings.getId());
            if (lotteryV2Service.enabled(lotterySettings)) return v2Quota(lotterySettings, lotteryVO.getMemberId(), getAndValidateWxMember(lotteryVO.getMemberId()), true);
            if (isMultiChanceMode(lotterySettings)) {
                WxMemberVO wxMember = getAndValidateWxMember(lotteryVO.getMemberId());
                MultiChanceSummary summary = buildMultiChanceSummary(lotterySettings, lotteryVO.getMemberId(), wxMember, true);
                resultVo.setNum(summary.aggregateAvailableCount);
                resultVo.setSum(summary.totalRemaining);
                resultVo.setFreeAvailableCount(summary.freeAvailableCount);
                resultVo.setPointsAvailableCount(summary.pointsAvailableCount);
                resultVo.setOrderAvailableCount(summary.orderAvailableCount);
                resultVo.setShareAvailableCount(summary.shareAvailableCount);
                resultVo.setBrowseAvailableCount(summary.browseAvailableCount);
                resultVo.setOrderFinishCount(summary.orderFinishCount);
                resultVo.setShareFinishCount(summary.shareFinishCount);
                resultVo.setBrowseFinishCount(summary.browseFinishCount);
                return resultVo;
            }
            // 计算每日剩余次数/场次剩余次数
            if (lotterySettings.getCalculationRules() != null) {
                if (lotterySettings.getCalculationRules() == 1) {
                    calculateDailyRemainingNum(lotterySettings, lotteryVO, currentDate, resultVo);
                } else {
                    calculateBySession(lotterySettings, lotteryVO, resultVo);
                }
            }else {
                calculateDailyRemainingNum(lotterySettings, lotteryVO, currentDate, resultVo);
            }
            // 计算总剩余次数
            calculateTotalRemainingNum(lotterySettings, lotteryVO, resultVo);

        } catch (Exception e) {
            log.warn("Redis操作异常: {}", e.getMessage(), e);
            return resultVo;
        }

        return resultVo;
    }

    /**
     * 获取 lottery 设置，不存在则添加到缓存
     */
    private LotterySettingsCacheDataVO getLotterySettings(Long lotteryId) {
        LotterySettingsCacheDataVO current;
        try {
            current = lotteryConfigurationCache.settings(lotteryId);
        } catch (RuntimeException e) {
            // 新缓存不可用时，仅旧活动回到原有缓存；新版活动保持失败关闭。
            LotterySettingsDO legacySettings = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                    .and(wrapper -> wrapper.eq(LotterySettingsDO::getId, lotteryId)
                            .or().eq(LotterySettingsDO::getActivityId, lotteryId)));
            if (legacySettings == null || Objects.equals(legacySettings.getRuntimeVersion(), 2)) throw e;
            LotterySettingsCacheDataVO legacy = redisCache.getCacheObject(RedisKeyConstants.LOTTERY_SETTING + lotteryId);
            return ObjectUtil.isEmpty(legacy) || legacy.getId() == null ? addCache(lotteryId) : legacy;
        }
        if (lotteryV2Service.enabled(current)) return current;
        LotterySettingsCacheDataVO legacy = redisCache.getCacheObject(RedisKeyConstants.LOTTERY_SETTING + lotteryId);
        return ObjectUtil.isEmpty(legacy) || legacy.getId() == null ? addCache(lotteryId) : legacy;
    }
    private LotterySettingsNumVo v2Quota(LotterySettingsCacheDataVO settings,Long memberId,WxMemberVO member,boolean applyLimits) {
        String period=buildTaskScope(settings).scopeKey;
        return LotteryQuota.calculate(settings,lotteryCounterCache.read(BusinessContextHolder.getRequiredBusinessId(),settings.getActivityId(),memberId,period),
                period,member==null?0:defaultZero(member.getMemberIntegral()),applyLimits);
    }

    /**
     * 计算每日剩余抽奖次数
     */
    private void calculateDailyRemainingNum(LotterySettingsCacheDataVO settings, LotteryVO lotteryVO,
                                            String currentDate, LotterySettingsNumVo resultVo) {
        // 无限制则返回-1
        if (settings.getLotteryLimit() == 0) {
            resultVo.setNum(-1);
            return;
        }

        // 构建缓存键
        String dailyCacheKey = buildLegacyDailyCacheKey(settings, lotteryVO.getMemberId(), currentDate);
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        // 获取当前已使用次数
        if (settings.getLotteryMethod() != 3) {
            Integer usedCount = getAndCacheUsedCount(
                    dailyCacheKey,
                    () -> lotteryAddLogService.selectLotteryNum(lotteryVO.getMemberId(), settings.getId()),
                    getDuration().getSeconds()
            );
            // 设置剩余次数
            resultVo.setNum(calculateRemainingNum(settings.getLotteryLimit(), usedCount));
        } else {
            String cacheKey = buildCommodityLotteryKey(lotteryVO.getMemberId(), settings, today);
            Integer codeFromRedis = 0;
            try {
                codeFromRedis = redisCache.getOpsForValue(cacheKey); // 原代码可能是getCacheObject，此处保持一致
            } catch (Exception e) {
                log.warn("Redis查询商品抽奖资格异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
            Integer dailyUsedCount = getLotteryCount(
                    dailyCacheKey,
                    () -> lotteryAddLogService.selectLotteryNum(lotteryVO.getMemberId(), settings.getId()),
                    this::getDuration
            );
            // 设置剩余次数
            int num = dailyUsedCount >= settings.getLotteryLimit() ? 0 : settings.getLotteryLimit() - dailyUsedCount.intValue();
            if (num >= codeFromRedis) {
                resultVo.setNum(codeFromRedis);
            } else {
                resultVo.setNum(num);
            }

        }

    }

    /**
     * 计算总剩余抽奖次数
     */
    private void calculateTotalRemainingNum(LotterySettingsCacheDataVO settings, LotteryVO lotteryVO,
                                            LotterySettingsNumVo resultVo) {
        // 无限制则返回-1
        if (settings.getLotteryTotalNumber() == 0) {
            resultVo.setSum(-1);
            return;
        }

        // 构建缓存键
        String totalCacheKey = buildLegacyTotalCacheKey(settings, lotteryVO.getMemberId());

        // 获取当前已使用次数
        Integer usedSum = getAndCacheUsedCount(
                totalCacheKey,
                () -> lotteryAddLogService.selectLotterySum(lotteryVO.getMemberId(), settings.getId()),
                null
        );

        // 设置剩余总次数
        resultVo.setSum(calculateRemainingNum(settings.getLotteryTotalNumber(), usedSum));
    }

    /**
     * 获取并缓存已使用次数
     */
    private Integer getAndCacheUsedCount(String cacheKey, Supplier<Integer> dbQuery, Long expireSeconds) {
        Integer count = redisCache.getCacheObject(cacheKey);

        if (count == null) {
            count = dbQuery.get();
            redisCache.setCacheObject(cacheKey, count);

            // 如果指定了过期时间，则设置
            if (expireSeconds != null) {
                redisCache.expire(cacheKey, expireSeconds, TimeUnit.SECONDS);
            }
        }

        return count;
    }

    /**
     * 计算剩余次数
     */
    private int calculateRemainingNum(int totalLimit, Integer usedCount) {
        int actualUsedCount = defaultZero(usedCount);
        return actualUsedCount >= totalLimit ? 0 : totalLimit - actualUsedCount;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    @Override
    public void delRedis(int type) {
        switch (type) {
            case 1:
                //清除活动
                redisCache.deleteObject(RedisKeyConstants.LOTTERY_SETTING);
                break;
            case 2:
                //清除奖品
                redisCache.deleteObject(RedisKeyConstants.LOTTERY_PRIZE);
                break;
            case 3:
                //清除次数
                redisCache.deleteObject(RedisKeyConstants.LOTTERY_NUM);
                break;
            case 4:
                //清除奖品数量
                redisCache.deleteObject(RedisKeyConstants.PRIZE_NUM);
                break;
        }
    }

    @Override
    public Integer getRedis(int type, Long memberId, Long lotteryId) {
        String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
        LotterySettingsCacheDataVO settings = getLotterySettings(lotteryId);
        Long activityId = settings == null ? lotteryId : resolveLotteryBizActivityId(settings);
        if (type == 1) {
            //次数
            return redisCache.getCacheObject(RedisKeyConstants.LOTTERY_NUM + currDate + "_" + activityId + ":" + memberId);
        }
        // 有效时间
        long expire = redisCache.getExpire(RedisKeyConstants.LOTTERY_NUM + currDate + "_" + activityId + ":" + memberId);
        return Math.toIntExact(expire);
    }

    @Override
    public List<LotteryTaskVO> getTaskList(LotteryTaskReqVO reqVO) {
        LotterySettingsCacheDataVO settings = getLotterySettings(reqVO.getLotteryId());
        TaskScope scope = buildTaskScope(settings);
        List<LotteryTaskVO> taskVOList = new ArrayList<>();
        if (Objects.equals(settings.getOrderStatus(), 1)) {
            taskVOList.add(buildLotteryTaskVO(settings, reqVO.getMemberId(), LotteryTaskTypeEnum.ORDER, scope,
                    isUnlimitedOrderTask(settings) ? -1 : defaultZero(settings.getPlaceOrderLotteryNumber()),
                    isUnlimitedOrderTask(settings)));
        }
        if (Objects.equals(settings.getShareEvent(), 1)) {
            taskVOList.add(buildLotteryTaskVO(settings, reqVO.getMemberId(), LotteryTaskTypeEnum.SHARE, scope,
                    defaultZero(settings.getShareCount()), false));
        }
        if (Objects.equals(settings.getBrowseType(), 1)) {
            taskVOList.add(buildLotteryTaskVO(settings, reqVO.getMemberId(), LotteryTaskTypeEnum.BROWSE, scope,
                    defaultZero(settings.getBrowseCount()), false));
        }
        return taskVOList;
    }

    @Override
    public Boolean shareCheck(LotteryTaskReqVO reqVO) {
        LotterySettingsCacheDataVO settings = loadTaskSettings(reqVO.getLotteryId(), reqVO.getMemberId());
        validateShareTaskEnabled(settings, reqVO);
        validateShareTaskCanComplete(settings, reqVO.getMemberId());
        return true;
    }

    @Override
    public Boolean shareTask(LotteryTaskReqVO reqVO) {
        LotterySettingsCacheDataVO settings = loadTaskSettings(reqVO.getLotteryId(), reqVO.getMemberId());
        validateShareTaskEnabled(settings, reqVO);
        String lockKey = LOTTERY_SHARE_LOCK_PREFIX + resolveLotteryBizActivityId(settings) + ":" + reqVO.getMemberId();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
            validateShareTaskCanComplete(settings, reqVO.getMemberId());
            TaskScope scope = buildTaskScope(settings);
            completeLotteryTask(settings, reqVO.getMemberId(), LotteryTaskTypeEnum.SHARE, scope,
                    defaultZero(settings.getShareCount()), false, 1);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            releaseLock(lock, locked);
        }
    }

    @Override
    public Boolean browseTask(LotteryTaskReqVO reqVO) {
        LotterySettingsCacheDataVO settings = loadTaskSettings(reqVO.getLotteryId(), reqVO.getMemberId());
        if (!Objects.equals(settings.getBrowseType(), 1)) {
            throw exception(LOTTERY_TASK_NOT_ENABLED);
        }
        TaskScope scope = buildTaskScope(settings);
        completeLotteryTask(settings, reqVO.getMemberId(), LotteryTaskTypeEnum.BROWSE, scope,
                defaultZero(settings.getBrowseCount()), false, 1);
        return true;
    }

    @Override
    public void handleOrderTask(ActivityJkOrderReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.getStoreId() == null || reqDTO.getMemberId() == null) {
            return;
        }
        List<LotterySettingsDO> settingsList = lotterySettingsMapper.selectList(new LambdaQueryWrapperX<LotterySettingsDO>()
                .eq(LotterySettingsDO::getState, 1)
                .eq(LotterySettingsDO::getOrderStatus, 1)
                .le(LotterySettingsDO::getLotteryStartTime, LocalDate.now())
                .ge(LotterySettingsDO::getLotteryEndTime, LocalDate.now()));
        if (CollectionUtils.isEmpty(settingsList)) {
            return;
        }
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(reqDTO.getMemberId());
        WxMemberVO wxMember = memberResult == null ? null : memberResult.getData();
        if (wxMember == null) {
            return;
        }
        Map<Long, ActivityDO> activityMap = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                        .in(ActivityDO::getId, settingsList.stream().map(LotterySettingsDO::getActivityId).filter(Objects::nonNull).toList()))
                .stream()
                .collect(Collectors.toMap(ActivityDO::getId, Function.identity(), (left, right) -> left));
        List<Long> limitedStoreActivityIds = activityMap.values().stream()
                .filter(activity -> Objects.equals(activity.getActivityStore(), 0))
                .map(ActivityDO::getId)
                .toList();
        Map<Long, List<Long>> activityStoreMap = limitedStoreActivityIds.isEmpty()
                ? Collections.emptyMap()
                : activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>()
                        .in(ActivityStoreDO::getActivityId, limitedStoreActivityIds)).stream()
                .collect(Collectors.groupingBy(ActivityStoreDO::getActivityId,
                        Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())));
        List<Long> commodityActivityIds = settingsList.stream()
                .filter(item -> Objects.equals(item.getPlaceOrderType(), 2) && Objects.equals(item.getActivityProduct(), 2))
                .map(LotterySettingsDO::getActivityId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<Long>> commodityMap = buildLotteryCommodityMap(commodityActivityIds);
        for (LotterySettingsDO settingsDO : settingsList) {
            ActivityDO activityDO = activityMap.get(settingsDO.getActivityId());
            if (activityDO == null) {
                continue;
            }
            if (!TimeValidationUtil.isTimeValid(activityDO.getStartDate(), activityDO.getEndDate(),
                    activityDO.getDayNumbers(), activityDO.getWeekNumbers(), activityDO.getTimeRange())) {
                continue;
            }
            if (!isLotteryStoreMatched(activityDO, activityStoreMap.get(activityDO.getId()), reqDTO.getStoreId())) {
                continue;
            }
            if (!isLotteryCrowdMatched(activityDO, wxMember)) {
                continue;
            }
            if (!isLotteryOrderMatched(settingsDO, commodityMap.getOrDefault(settingsDO.getActivityId(), Collections.emptyList()), reqDTO)) {
                continue;
            }
            LotterySettingsCacheDataVO cacheData = addCache(settingsDO.getId());
            if (cacheData == null) {
                continue;
            }
            completeLotteryTask(cacheData, reqDTO.getMemberId(), LotteryTaskTypeEnum.ORDER,
                    buildTaskScope(cacheData),
                    defaultZero(settingsDO.getPlaceOrderLotteryNumber()),
                    isUnlimitedOrderTask(cacheData),
                    1);
        }
    }

    private LotteryTaskVO buildLotteryTaskVO(LotterySettingsCacheDataVO settings,
                                             Long memberId,
                                             LotteryTaskTypeEnum taskTypeEnum,
                                             TaskScope scope,
                                             int taskLimit,
                                             boolean unlimited) {
        LotteryTaskDO taskDO = getScopedLotteryTask(resolveLotteryBizActivityId(settings), memberId, taskTypeEnum.getCode(), unlimited, scope, false);
        LotteryTaskVO taskVO = new LotteryTaskVO();
        taskVO.setTaskType(taskTypeEnum.getCode());
        taskVO.setTaskLimit(unlimited ? -1 : taskLimit);
        taskVO.setFinishCount(taskDO == null ? 0 : defaultZero(taskDO.getFinishCount()));
        taskVO.setAvailableCount(calculateTaskAvailableCount(taskDO));
        taskVO.setPaymentThreshold(BigDecimal.ZERO);
        if (Objects.equals(taskTypeEnum, LotteryTaskTypeEnum.ORDER)) {
            taskVO.setPaymentThreshold(Objects.equals(settings.getPaymentType(), 1) ? settings.getPaymentCount() : BigDecimal.ZERO);
        }
        return taskVO;
    }

    private LotterySettingsCacheDataVO loadTaskSettings(Long lotteryId, Long memberId) {
        LotterySettingsCacheDataVO settings = getLotterySettings(lotteryId);
        validateLotteryBasicStatus(settings);
        validateLotteryTime(settings);
        if (!activityAppService.checkCanJoin(settings.getActivityId())) {
            throw exception(LOTTERY_WECOMGROUP_ERROR);
        }
        if (memberId != null) {
            WxMemberVO wxMember = getAndValidateWxMember(memberId);
            validateUserCrowd(settings, wxMember);
        }
        return settings;
    }

    private void validateShareTaskEnabled(LotterySettingsCacheDataVO settings, LotteryTaskReqVO reqVO) {
        if (!Objects.equals(settings.getShareEvent(), 1)) {
            throw exception(LOTTERY_TASK_NOT_ENABLED);
        }
        if (reqVO.getMemberId() == null) {
            throw exception(LOTTERY_NOT_NULL);
        }
    }

    private void validateShareTaskCanComplete(LotterySettingsCacheDataVO settings, Long memberId) {
        TaskScope scope = buildTaskScope(settings);
        WxMemberVO wxMember = getAndValidateWxMember(memberId);
        MultiChanceSummary currentSummary = buildMultiChanceSummary(settings, memberId, wxMember, false);
        if (currentSummary.totalRemaining == 0) {
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        }
        if (currentSummary.totalRemaining > 0
                && (currentSummary.aggregateAvailableCount < 0
                || currentSummary.aggregateAvailableCount >= currentSummary.totalRemaining)) {
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        }
        LotteryTaskDO taskDO = getScopedLotteryTask(resolveLotteryBizActivityId(settings), memberId,
                LotteryTaskTypeEnum.SHARE.getCode(), false, scope, false);
        if (defaultZero(taskDO == null ? 0 : taskDO.getFinishCount()) >= defaultZero(settings.getShareCount())) {
            throw exception(LOTTERY_TASK_LIMIT_REACHED);
        }
    }

    private void completeLotteryTask(LotterySettingsCacheDataVO settings,
                                     Long memberId,
                                     LotteryTaskTypeEnum taskTypeEnum,
                                     TaskScope scope,
                                     int taskLimit,
                                     boolean unlimited,
                                     int rewardCount) {
        validateLotteryTaskEnabled(settings, taskTypeEnum);
        if (lotteryV2Service.enabled(settings)) {
            WxMemberVO member = getAndValidateWxMember(memberId);
            lotteryLedger.earn(BusinessContextHolder.getRequiredBusinessId(), settings, memberId, scope.scopeKey,
                    unlimited ? "TOTAL" : scope.scopeKey, taskTypeEnum.getCode(), taskLimit, unlimited, rewardCount, defaultZero(member.getMemberIntegral()));
            return;
        }
        Long activityId = resolveLotteryBizActivityId(settings);
        String lockKey = LOTTERY_TASK_LOCK_PREFIX + activityId + ":" + memberId + ":" + taskTypeEnum.getCode();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
            WxMemberVO wxMember = getAndValidateWxMember(memberId);
            MultiChanceSummary currentSummary = buildMultiChanceSummary(settings, memberId, wxMember, false);
            if (currentSummary.totalRemaining == 0) {
                throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
            }
            if (currentSummary.totalRemaining > 0
                    && (currentSummary.aggregateAvailableCount < 0
                    || currentSummary.aggregateAvailableCount >= currentSummary.totalRemaining)) {
                throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
            }
            LotteryTaskDO taskDO = getScopedLotteryTask(activityId, memberId, taskTypeEnum.getCode(), unlimited, scope, true);
            int currentFinish = defaultZero(taskDO.getFinishCount());
            if (!unlimited && currentFinish >= taskLimit) {
                throw exception(LOTTERY_TASK_LIMIT_REACHED);
            }
            taskDO.setFinishCount(currentFinish + 1);
            taskDO.setGainCount(defaultZero(taskDO.getGainCount()) + rewardCount);
            taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()));
            taskDO.setUpdateTime(LocalDateTime.now());
            updateLotteryTask(taskDO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            releaseLock(lock, locked);
        }
    }

    private Long resolveLotteryBizActivityId(LotterySettingsCacheDataVO settings) {
        return settings.getActivityId() == null ? settings.getId() : settings.getActivityId();
    }

    private String buildLegacyDailyCacheKey(LotterySettingsCacheDataVO settings, Long memberId, String currDate) {
        return RedisKeyConstants.LOTTERY_NUM + currDate + "_" + resolveLotteryBizActivityId(settings) + ":" + memberId;
    }

    private String buildLegacySessionCacheKey(LotterySettingsCacheDataVO settings, Long memberId, String currDate, String sessionKey) {
        return RedisKeyConstants.LOTTERY_NUM + currDate + "_" + resolveLotteryBizActivityId(settings) + ":" + memberId + ":" + sessionKey;
    }

    private String buildLegacyTotalCacheKey(LotterySettingsCacheDataVO settings, Long memberId) {
        return RedisKeyConstants.LOTTERY_MEMBER_NUM + resolveLotteryBizActivityId(settings) + ":" + memberId;
    }

    private String buildCommodityLotteryKey(Long memberId, LotterySettingsCacheDataVO settings, String dateKey) {
        return String.format(COMMODITY_LOTTERY_MEMBERID, memberId, resolveLotteryBizActivityId(settings), dateKey);
    }

    private String buildMultiTotalCacheKey(LotterySettingsCacheDataVO settings, Long memberId) {
        return RedisKeyConstants.LOTTERY_MEMBER_NUM + resolveLotteryBizActivityId(settings) + ":" + memberId;
    }

    private String buildMultiDailyCacheKey(LotterySettingsCacheDataVO settings, Long memberId, String currDate) {
        return RedisKeyConstants.LOTTERY_NUM + currDate + "_" + resolveLotteryBizActivityId(settings) + ":" + memberId;
    }

    private String buildMultiSessionCacheKey(LotterySettingsCacheDataVO settings, Long memberId, String currDate, String sessionKey) {
        return RedisKeyConstants.LOTTERY_NUM + currDate + "_" + resolveLotteryBizActivityId(settings) + ":" + memberId + ":" + sessionKey;
    }

    private void validateMultiTotalLimit(LotterySettingsCacheDataVO settings, Long memberId) {
        if (calculateTotalRemaining(settings, memberId) == 0) {
            throw exception(LOTTERY_TOT_LIMIT_REACHED_AGAIN);
        }
    }

    private void updateMultiDailyLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        String cacheKey = buildMultiDailyCacheKey(settings, lotteryVo.getMemberId(), currDate);
        Integer count = redisCache.getCacheObject(cacheKey);
        if (count != null) {
            stringRedisTemplate.opsForValue().increment(cacheKey);
        } else {
            count = lotteryAddLogService.selectLotteryNum(lotteryVo.getMemberId(), settings.getId());
            redisCache.setCacheObject(cacheKey, count + 1);
        }
        Duration duration = getDuration();
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    private void updateMultiSessionLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            return;
        }
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            return;
        }
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String cacheKey = buildMultiSessionCacheKey(settings, lotteryVo.getMemberId(), currDate, sessionKey);
        Integer count = redisCache.getCacheObject(cacheKey);
        if (count != null) {
            stringRedisTemplate.opsForValue().increment(cacheKey);
        } else {
            count = lotteryAddLogService.selectLotteryNumBySession(
                    lotteryVo.getMemberId(),
                    settings.getId(),
                    currentSession.getStartLocalTime(),
                    currentSession.isAllDay() ? LocalTime.of(23, 59, 59) : currentSession.getEndLocalTime()
            );
            redisCache.setCacheObject(cacheKey, count + 1);
        }
        long expireSeconds = currentSession.isAllDay()
                ? Duration.between(currentTime, LocalTime.of(23, 59, 59)).getSeconds()
                : Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds();
        redisCache.expire(cacheKey, Math.max(expireSeconds, 1), TimeUnit.SECONDS);
    }

    private void updateMultiTotalLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String cacheKey = buildMultiTotalCacheKey(settings, lotteryVo.getMemberId());
        Integer count = redisCache.getCacheObject(cacheKey);
        if (count != null) {
            stringRedisTemplate.opsForValue().increment(cacheKey);
        } else {
            count = lotteryAddLogService.selectLotterySum(lotteryVo.getMemberId(), settings.getId());
            redisCache.setCacheObject(cacheKey, count + 1);
        }
        Duration duration = calculateCacheDuration(new Date(), settings.getLotteryStartTime(), settings.getLotteryEndTime());
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    private void rollbackMultiDailyLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        String cacheKey = buildMultiDailyCacheKey(settings, lotteryVo.getMemberId(), currDate);
        decrementLotteryCountCache(cacheKey);
        Duration duration = getDuration();
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    private void rollbackMultiSessionLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings, String currDate) {
        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            return;
        }
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            return;
        }
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String cacheKey = buildMultiSessionCacheKey(settings, lotteryVo.getMemberId(), currDate, sessionKey);
        decrementLotteryCountCache(cacheKey);
        long expireSeconds = currentSession.isAllDay()
                ? Duration.between(currentTime, LocalTime.of(23, 59, 59)).getSeconds()
                : Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds();
        redisCache.expire(cacheKey, Math.max(expireSeconds, 1), TimeUnit.SECONDS);
    }

    private void rollbackMultiTotalLotteryCount(LotteryVO lotteryVo, LotterySettingsCacheDataVO settings) {
        String cacheKey = buildMultiTotalCacheKey(settings, lotteryVo.getMemberId());
        decrementLotteryCountCache(cacheKey);
        Duration duration = calculateCacheDuration(new Date(), settings.getLotteryStartTime(), settings.getLotteryEndTime());
        redisCache.expire(cacheKey, duration.getSeconds(), TimeUnit.SECONDS);
    }

    private void validateLotteryTaskEnabled(LotterySettingsCacheDataVO settings, LotteryTaskTypeEnum taskTypeEnum) {
        boolean enabled;
        switch (taskTypeEnum) {
            case ORDER:
                enabled = Objects.equals(settings.getOrderStatus(), 1);
                break;
            case SHARE:
                enabled = Objects.equals(settings.getShareEvent(), 1);
                break;
            case BROWSE:
                enabled = Objects.equals(settings.getBrowseType(), 1);
                break;
            case FREE:
                enabled = Objects.equals(settings.getFreeStatus(), 1);
                break;
            case POINTS:
                enabled = Objects.equals(settings.getPointsStatus(), 1);
                break;
            default:
                enabled = false;
                break;
        }
        if (!enabled) {
            throw exception(LOTTERY_TASK_NOT_ENABLED);
        }
    }

    private boolean isLotteryOrderMatched(LotterySettingsDO settingsDO, List<Long> configuredCommodityIds, ActivityJkOrderReqDTO reqDTO) {
        if (settingsDO == null || reqDTO == null || !Objects.equals(settingsDO.getOrderStatus(), 1)) {
            return false;
        }
        if (Objects.equals(settingsDO.getPlaceOrderType(), 1)
                && !isLotteryCategoryMatched(settingsDO.getCategoryType(), reqDTO.getOrderProductType())) {
            return false;
        }
        if (Objects.equals(settingsDO.getPlaceOrderType(), 2) && Objects.equals(settingsDO.getActivityProduct(), 2)) {
            if (configuredCommodityIds.isEmpty() || reqDTO.getCommodityIds() == null || reqDTO.getCommodityIds().isEmpty()) {
                return false;
            }
            boolean matched = reqDTO.getCommodityIds().stream().anyMatch(configuredCommodityIds::contains);
            if (!matched) {
                return false;
            }
        }
        if (Objects.equals(settingsDO.getPaymentType(), 1)) {
            if (reqDTO.getPaymentAmount() == null || settingsDO.getPaymentCount() == null
                    || reqDTO.getPaymentAmount().compareTo(settingsDO.getPaymentCount()) <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isLotteryCategoryMatched(Integer categoryType, Integer orderProductType) {
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

    private boolean isLotteryStoreMatched(ActivityDO activityDO, List<Long> relatedStores, Long storeId) {
        if (activityDO != null && Objects.equals(activityDO.getActivityStore(), 1)) {
            return true;
        }
        return relatedStores != null && relatedStores.contains(storeId);
    }

    private boolean isLotteryCrowdMatched(ActivityDO activityDO, WxMemberVO wxMemberVO) {
        if (activityDO == null || wxMemberVO == null || Objects.equals(activityDO.getParticipantGroup(), 1)) {
            return true;
        }
        return switch (activityDO.getParticipantGroup()) {
            case 2 -> userTypeSevenDay(wxMemberVO);
            case 3 -> userType(wxMemberVO.getRegisterTime());
            case 4 -> userTypeThirtyDay(wxMemberVO);
            case 5 -> checkCrowdApi(wxMemberVO.getMemberId(), activityDO.getSelectedGroups());
            default -> false;
        };
    }

    private Map<Long, List<Long>> buildLotteryCommodityMap(List<Long> activityIds) {
        if (CollectionUtils.isEmpty(activityIds)) {
            return Collections.emptyMap();
        }
        return activityLotteryCommodityService.list(new LambdaQueryWrapperX<ActivityLotteryCommodityDO>()
                        .eq(ActivityLotteryCommodityDO::getDeleted, 0)
                        .in(ActivityLotteryCommodityDO::getActivityId, activityIds))
                .stream()
                .filter(item -> item.getActivityId() != null && item.getCommodityId() != null)
                .collect(Collectors.groupingBy(
                        ActivityLotteryCommodityDO::getActivityId,
                        Collectors.mapping(ActivityLotteryCommodityDO::getCommodityId, Collectors.toList())
                ));
    }

    @Override
    public Map<Long, List<Long>> getLotteryList(Long storeId, Long memberId) {
        // 1. 查询符合条件的活动设置列表
        List<LotterySettingsDO> activityList = lotterySettingsMapper.selectList(
                new LambdaQueryWrapperX<LotterySettingsDO>()
                        .eq(LotterySettingsDO::getState, 1) // 状态为启用
                        .le(LotterySettingsDO::getLotteryStartTime, LocalDate.now()) // 开始时间不晚于今天
                        .ge(LotterySettingsDO::getLotteryEndTime, LocalDate.now()) // 结束时间不早于今天
                        .and(wrapper -> wrapper.isNull(LotterySettingsDO::getOrderStatus)
                                .or().eq(LotterySettingsDO::getOrderStatus, 0))
                        .eq(LotterySettingsDO::getLotteryMethod, 3) // 抽奖方式为指定类型
                        .orderByAsc(LotterySettingsDO::getLotteryType) // 按活动类型升序排序
        );

        // 若活动列表为空，直接返回空Map（提前退出）
        if (CollectionUtils.isEmpty(activityList)) {
            return new HashMap<>(0);
        }

        // 获取会员信息并做空值校验
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
        if (memberResult == null || memberResult.getData() == null) {
            log.warn("获取会员信息失败，memberId: {}", memberId);
            return new HashMap<>(0);
        }
        WxMemberVO wxMemberVO = memberResult.getData();

        // 创建新的过滤列表，避免在迭代中修改原列表导致ConcurrentModificationException
        List<LotterySettingsDO> filteredActivities = new ArrayList<>();

        for (LotterySettingsDO activity : activityList) {
            // 校验活动对象及活动ID的有效性
            if (activity == null || activity.getActivityId() == null) {
                log.warn("活动列表中存在无效活动对象或空活动ID");
                continue;
            }

            // 查询活动详情
            ActivityDO activityDOS = activityMapper.selectById(activity.getActivityId());
            // 活动详情为空时跳过
            if (activityDOS == null) {
                log.warn("活动详情不存在，activityId: {}", activity.getActivityId());
                continue;
            }

            // 校验人群资格
            boolean isQualified = true;
            // 仅当活动限制人群时才进行校验（participantGroup != 1表示有限制）
            if (activityDOS.getParticipantGroup() != 1) {
                isQualified = switch (activityDOS.getParticipantGroup()) {
                    case 2 -> userTypeSevenDay(wxMemberVO); // 校验7天内用户
                    case 3 -> userType(wxMemberVO.getRegisterTime()); // 校验特定注册时间用户
                    case 4 -> userTypeThirtyDay(wxMemberVO); // 校验30天内用户
                    case 5 -> checkCrowdApi(wxMemberVO.getMemberId(), activityDOS.getSelectedGroups()); // 调用接口校验人群
                    default -> {
                        log.warn("未知的人群类型，LotteryId：{}，人群类型：{}",
                                activityDOS.getId(), activityDOS.getParticipantGroup());
                        yield false; // 未知类型视为不通过
                    }
                };
            }

            // 人群校验不通过则跳过当前活动
            if (!isQualified) {
                log.warn("用户不符合人群资格，LotteryId：{}，MemberId：{}，人群类型：{}",
                        activityDOS.getId(), memberId, activityDOS.getParticipantGroup());
                continue;
            }

            // 仅当活动限制门店时才进行校验（activityStore != 1表示有限制）
            if (activityDOS.getActivityStore() != 1) {
                List<StoreInfoDTO> storeInfoDTOS = activityStoreService.storesByActivityId(activityDOS.getId());
                // 校验门店列表
                if (CollectionUtil.isNotEmpty(storeInfoDTOS)) {
                    List<Long> storeIds = storeInfoDTOS.stream()
                            .map(StoreInfoDTO::getStoreId)
                            .filter(Objects::nonNull) // 过滤空门店ID
                            .collect(Collectors.toList());

                    // 门店ID为空或不在允许列表中则跳过
                    if (storeId == null || !storeIds.contains(storeId)) {
                        log.warn("门店未被授权参与活动，LotteryId：{}，StoreId：{}",
                                activityDOS.getId(), storeId);
                        continue;
                    }
                } else {
                    log.warn("活动未配置关联门店，LotteryId：{}", activityDOS.getId());
                    continue;
                }
            }

            // 所有校验通过，添加到过滤后的列表
            filteredActivities.add(activity);
        }

        // 过滤后列表为空则返回空Map
        if (CollectionUtils.isEmpty(filteredActivities)) {
            return new HashMap<>(0);
        }

        // 2. 提取需要查询商品的活动ID（仅ActivityProduct=2的活动）
        List<Long> activityIdsNeedCommodity = filteredActivities.stream()
                .filter(activity -> 2 == activity.getActivityProduct()) // 筛选需要关联商品的活动
                .map(LotterySettingsDO::getActivityId)
                .filter(Objects::nonNull) // 过滤空ID
                .collect(Collectors.toList());

        // 3. 批量查询所有关联商品
        Map<Long, List<Long>> activityCommodityMap;
        if (!activityIdsNeedCommodity.isEmpty()) {
            List<ActivityLotteryCommodityDO> commodityList = activityLotteryCommodityService.list(
                    new LambdaQueryWrapper<ActivityLotteryCommodityDO>()
                            .eq(ActivityLotteryCommodityDO::getDeleted, 0)
                            .in(ActivityLotteryCommodityDO::getActivityId, activityIdsNeedCommodity)
            );

            // 4. 构建活动ID到商品ID列表的映射
            activityCommodityMap = commodityList.stream()
                    .filter(Objects::nonNull) // 过滤空对象
                    .collect(Collectors.groupingBy(
                            ActivityLotteryCommodityDO::getActivityId,
                            Collectors.mapping(
                                    ActivityLotteryCommodityDO::getCommodityId,
                                    Collectors.filtering(Objects::nonNull, Collectors.toList()) // 过滤空商品ID
                            )
                    ));
        } else {
            activityCommodityMap = new HashMap<>();
        }

        // 5. 填充所有活动的映射关系
        return filteredActivities.stream()
                .filter(activity -> activity.getId() != null) // 过滤活动ID为空的记录
                .collect(Collectors.toMap(
                        LotterySettingsDO::getId, // 键：业务活动ID
                        activity -> activityCommodityMap.getOrDefault(activity.getActivityId(), new ArrayList<>()), // 值：商品列表（默认空列表）
                        (existing, replacement) -> {
                            log.warn("发现重复的活动ID: {}", existing);
                            return existing; // 处理重复活动ID（保留第一个）
                        }
                ));
    }

    /**
     * 异步处理数据（适配共用/独立奖池，补充奖池规则和门店信息）
     *
     * @param lotteryVO        抽奖请求参数
     * @param lotteryUserLogVO 抽奖结果对象
     * @param prizePoolRules   奖池规则（1=共用奖池，2=独立奖池）
     * @param storeId          门店ID（独立奖池时使用）
     */
    @Transactional
    public void updateData(LotteryVO lotteryVO, LotteryUserLogVO lotteryUserLogVO,
                           Integer prizePoolRules, Long storeId,
                           ReservedDrawChance reservedChance) {
        log.info("异步处理数据：活动ID={}，奖池规则={}，门店ID={}，{----------------------}",
                lotteryVO.getLotteryId(), prizePoolRules, storeId);

        // 1. 查询活动配置
        LotterySettingsCacheDataVO lotterySettings = redisCache.getCacheObject(RedisKeyConstants.LOTTERY_SETTING + lotteryVO.getLotteryId());
        if (ObjectUtil.isEmpty(lotterySettings)) {
            lotterySettings = addCache(lotteryVO.getLotteryId());
        }
        log.info("活动配置：{}，奖池规则：{}", lotterySettings, prizePoolRules);
        // 2. 获取当前中奖奖品信息（按奖池规则筛选）
        Long prizeId = lotteryUserLogVO.getLotteryPrizeId();
        LambdaQueryWrapperX<LotteryPrizeDO> prizeWrapper = new LambdaQueryWrapperX<LotteryPrizeDO>()
                .eq(LotteryPrizeDO::getId, prizeId);
        if (prizePoolRules == 2) {
            prizeWrapper.eq(LotteryPrizeDO::getStoreId, storeId);
        }
        LotteryPrizeDO lotteryPrize = lotteryPrizeMapper.selectOne(prizeWrapper);
        if (lotteryPrize == null) {
            log.warn("未查询到奖品：ID={}，规则={}，门店={}", prizeId, prizePoolRules, storeId);
            return;
        }
        log.info("奖品信息：{}，总数量={}，当前已领取={}",
                lotteryPrize.getId(), lotteryPrize.getPrizeNum(), lotteryPrize.getRemainNum());

        // 3. 主流程已经同步更新Redis库存，这里异步把库存落到数据库并补重复中奖缓存标记。
        if (lotteryPrize.getIsGuarantees() != 1) {
            Integer usedCount = getPrizeInventoryUsedCount(lotteryPrize, prizePoolRules, storeId);
            if (usedCount != null && !Objects.equals(usedCount, lotteryPrize.getRemainNum())) {
                LotteryPrizeDO updateDO = new LotteryPrizeDO();
                updateDO.setId(prizeId);
                updateDO.setRemainNum(usedCount);
                lotteryPrizeMapper.updateById(updateDO);
                lotteryPrize.setRemainNum(usedCount);
                log.info("异步同步奖品库存成功：奖品{}，Redis={}，数据库={}", prizeId, usedCount, usedCount);
            }
        }
        if (lotteryPrize.getIsGuarantees() != 1 && lotteryPrize.getIsRepeat() == 0) {
            String receivedCacheKey = "lottery:user:prize:received:" + ":" + lotteryPrize.getLotteryId() + ":" + lotteryVO.getMemberId() + ":" + lotteryPrize.getId();
            String pendingCacheKey = "lottery:user:prize:pending:" + ":" + lotteryPrize.getLotteryId() + ":" + lotteryVO.getMemberId() + ":" + lotteryPrize.getId();
            redisCache.setCacheObject(receivedCacheKey, true);
            redisCache.deleteObject(pendingCacheKey);
        }
        // 4. 查询用户信息（保持原有逻辑）
        log.info("查询用户：memberId={}", lotteryVO.getMemberId());
        WxMemberVO wxMemberVO = JSON.parseObject(
                JSON.toJSONString(wxMemberApi.getWxMemberById(lotteryVO.getMemberId()).getData()),
                WxMemberVO.class
        );
        if (Objects.isNull(wxMemberVO)) {
            throw exception(USER_NOT_EXISTS);
        }
        WxMemberDO wxMember = BeanUtils.toBean(wxMemberVO, WxMemberDO.class);
        log.info("用户信息：{}", wxMember);

        // 5. 积分处理：新规则按实际消耗来源扣积分；老规则保持原逻辑。
        boolean consumePoints = (reservedChance != null && Objects.equals(reservedChance.taskType, LotteryTaskTypeEnum.POINTS.getCode()))
                || (!isMultiChanceMode(lotterySettings) && lotterySettings.getLotteryMethod() == 2);
        int costPoints = reservedChance != null && reservedChance.pointsCost > 0
                ? reservedChance.pointsCost
                : defaultZero(lotterySettings.getPrice());
        if (consumePoints) {
            wxMember.setMemberIntegral(wxMember.getMemberIntegral() - costPoints);
            wxMemberApi.updateMemberById(wxMember.getMemberId(), wxMember.getMemberIntegral());
            addPointsLog(2, costPoints, lotteryVO, wxMember);
        }
        // 6. 添加抽奖记录（补充奖池规则）
        LotteryLogDO lotteryLog = new LotteryLogDO();
        lotteryLog.setLotteryPrizeId(prizeId);
        lotteryLog.setMemberId(lotteryVO.getMemberId());
        lotteryLog.setLotteryId(lotteryVO.getLotteryId());
        lotteryLog.setMemberMobile(wxMember.getMemberMobile());
        lotteryLog.setMemberName(TextFilterUtil.keepNormalChars(wxMember.getMemberNickName()));
        lotteryLog.setPrizeName(lotteryUserLogVO.getPrizeName());
        lotteryLog.setPrizeType(lotteryPrize.getPrizeType());
        lotteryLog.setIsGuarantees(lotteryPrize.getIsGuarantees());
        lotteryLog.setPrizeImgUrl(lotteryPrize.getPrizeImgUrl());
        lotteryLog.setPrice(consumePoints ? costPoints : 0);
        lotteryLog.setLotteryType(lotterySettings.getLotteryType() == 0 ? 1 : lotterySettings.getLotteryType());
        lotteryLog.setLotteryStartTime(lotterySettings.getLotteryStartTime());
        lotteryLog.setLotteryEndTime(lotterySettings.getLotteryEndTime());
        lotteryLog.setPrizeState(lotteryPrize.getPrizeType() == 3 ? 1 : 0);
        lotteryLog.setPrizeValue(lotteryPrize.getPrizeValue());
        lotteryLog.setClaimStatus(1);
        lotteryLog.setPackageInfo(lotteryUserLogVO.getPackageInfo());
        lotteryLog.setStoreId(storeId);
        if (lotteryPrize.getPrizeType() == 5) {
            lotteryLog.setOutBillNo(lotteryUserLogVO.getOutBillNo());
        }
        lotteryAddLogService.addLotteryLog(lotteryLog);
        winnerFeed.afterLogSaved(lotterySettings.getActivityId(), lotteryLog);
        log.info("添加抽奖记录：{}", lotteryLog);

        // 7. 奖品发放（保持原有逻辑）
        switch (lotteryPrize.getPrizeType()) {
            case 1:
                log.info("发放优惠券：{}", lotteryPrize);
                GoodCouponDO goodCouponVo = lotteryAddLogService.getGoodCoupon(lotteryPrize.getAwardId());
                UserCouponDO coupon = BeanUtils.toBean(goodCouponVo, UserCouponDO.class);
                coupon.setUserId(wxMember.getMemberId());
                coupon.setCouponId(lotteryPrize.getAwardId());
                coupon.setIsUsed(0);
                coupon.setId(null);
                coupon.setUseTime(null);
                parseUserCouponTime(goodCouponVo, coupon);
                coupon.setMemberMobile(wxMember.getMemberMobile());
                coupon.setCouponSource(CouponSourceType.PRIZE_DRAW.getCode());
                lotteryAddLogService.addCoupon(coupon);
                break;
            case 2:
                log.info("发放积分：{}", lotteryPrize);
                int newIntegral = wxMember.getMemberIntegral() + lotteryPrize.getPrizeValue().intValue();
                if (newIntegral > 0) {
                    wxMember.setMemberIntegral(newIntegral);
                    wxMemberApi.updateMemberById(wxMember.getMemberId(), wxMember.getMemberIntegral());
                    addPointsLog(1, lotteryPrize.getPrizeValue().intValue(), lotteryVO, wxMember);
                }
                break;
        }
    }

    /**
     * 重试更新已领取数量（补充超量判断）
     */
    // 添加积分记录
    public void addPointsLog(int type, int price, LotteryVO lotteryVO, WxMemberDO wxMember) {
        log.info(">>> 添加积分记录，wxMember={}, price={}", wxMember, price);
        PointsLogDO pointsLog = new PointsLogDO();
        //在新建的积分记录对象里插入用户 id
        pointsLog.setMemberId(wxMember.getMemberId());
        //在积分记录对象里放入这次新增的积分
        if (type == 1) {
            pointsLog.setPointsChange(Long.valueOf(price));
        } else {
            pointsLog.setPointsChange(-Long.valueOf(price));
        }
        pointsLog.setMemberName(wxMember.getMemberName());
        pointsLog.setMemberNickName(wxMember.getMemberNickName());
        pointsLog.setMemberMobile(wxMember.getMemberMobile());
        pointsLog.setLogCode(generateNumber());
        Number number = identifierGenerator.nextId(null);
        long l = number.longValue();
        pointsLog.setPointsLogId(l);
        pointsLog.setPointsType(type);
        pointsLog.setIsDelete(0);
        pointsLog.setCreateTime(new Date());
        pointsLog.setPointsLogStatus(1);
        pointsLog.setIsPointsProduct(2);
        pointsLog.setProductType(3);
        pointsLog.setCreateTime(new Date());
        pointsLog.setUpdateTime(new Date());
        pointsLog.setUpdateTime(new Date());
        pointsLog.setBusinessId(wxMember.getBusinessId());
        pointsLog.setIsDelete(0);
        //存活动id
        pointsLog.setProductId(lotteryVO.getLotteryId());
        pointsLog.setShardingValue(wxMember.getShardingValue());
        if (ObjectUtil.isNotEmpty(pointsLog.getShardingValue())) {
            String pointsLogId = l + "" + pointsLog.getShardingValue();
            BigInteger finalId = (new BigInteger(pointsLogId)).mod(BigInteger.valueOf(Long.MAX_VALUE));
            pointsLog.setPointsLogId(finalId.longValueExact());
            lotteryAddLogService.addPointsLog(pointsLog);
        }
    }

    // 生成编号
    public static String generateNumber() {
        return String.valueOf(System.currentTimeMillis());
    }


    private void setLotteryVO(LotteryUserLogVO lotteryVO, LotteryPrizeDO prize) {
        if (prize != null) {
            lotteryVO.setLotteryPrizeId(prize.getId());
            lotteryVO.setPrizeName(prize.getPrizeName());
            lotteryVO.setPrizeType(prize.getPrizeType());
            lotteryVO.setPrizeImgUrl(prize.getPrizeImgUrl());
        }
    }

    private void parseUserCouponTime(GoodCouponDO goodCoupon, UserCouponDO userCoupon) {
        //解析优惠券的开始结束时间
        if (goodCoupon.getUseType() == 0) {
            userCoupon.setExpirationTime(goodCoupon.getCouponEndTime());
            userCoupon.setVaildStartTime(goodCoupon.getCouponStartTime());
        }
        //立即生效
        if (goodCoupon.getUseType() == 1) {
            userCoupon.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(goodCoupon.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        //领券后N天生效
        if (goodCoupon.getUseType() == 2) {
            String[] split = goodCoupon.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])), DateUtils.YYYY_MM_DD);
            userCoupon.setVaildStartTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(LocalDate.now().plusDays(Integer.valueOf(split[0])).plusDays(Integer.valueOf(split[1]) - 1), DateUtils.YYYY_MM_DD);
            userCoupon.setExpirationTime(DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    private Duration getDuration() {
        LocalDateTime now = LocalDateTime.now();
        // 获取今天结束时间（即明天零点）
        LocalDateTime endOfToday = LocalDateTime.of(now.getYear(), now.getMonth(),
                now.getDayOfMonth(), 23, 59, 59);
        // 计算当前时间到今天结束时间的时间差
        return Duration.between(now, endOfToday);

    }

    private Duration calculateCacheDuration(Date now, Date activityStartTime, Date activityEndTime) {
        if (now == null || activityStartTime == null || activityEndTime == null) {
            throw new IllegalArgumentException("时间参数不能为null");
        }

        // 将可能的java.sql.Date转换为java.util.Date的Instant
        Instant nowInstant = now.toInstant();
        // 关键修复：通过getTime()获取毫秒数再转换为Instant
        Instant startInstant = Instant.ofEpochMilli(activityStartTime.getTime());
        Instant endInstant = Instant.ofEpochMilli(activityEndTime.getTime());

        if (nowInstant.isAfter(endInstant)) {
            return Duration.ZERO;
        }
        if (nowInstant.isBefore(startInstant)) {
            return Duration.between(nowInstant, startInstant);
        }
        return Duration.between(nowInstant, endInstant);
    }

    //组装缓存
    public LotterySettingsCacheDataVO addCache(Long lotteryId) {
        // 抽奖设置主体
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, lotteryId)
                        .or()
                        .eq(LotterySettingsDO::getActivityId, lotteryId)
                )
                .eq(LotterySettingsDO::getState, LotteryStateEnum.OPEN.getStatus())
                .le(LotterySettingsDO::getLotteryStartTime, LocalDate.now())
                .ge(LotterySettingsDO::getLotteryEndTime, LocalDate.now()));

        if (lotterySettings == null || ObjectUtil.isEmpty(lotterySettings.getId())) {
            throw exception(LOTTERY_ACTIVITY_CLOSED);
        }

        LotterySettingsCacheDataVO cacheData = new LotterySettingsCacheDataVO();
        // 复制抽奖设置主体属性到缓存对象
        BeanUtils.copyProperties(lotterySettings, cacheData);

        // 活动主体
        ActivityDO activityDO = activityMapper.selectById(lotterySettings.getActivityId());
        if (activityDO != null) {
            // 复制活动主体属性到缓存对象
            BeanUtils.copyProperties(activityDO, cacheData);
            cacheData.setId(lotterySettings.getId());

            // 处理指定门店
            if (activityDO.getActivityStore() == 0) {
                List<StoreInfoDTO> storeInfoDTOS = activityStoreService.storesByActivityId(activityDO.getId());
                if (CollectionUtil.isNotEmpty(storeInfoDTOS)) {
                    List<Long> storeIds = storeInfoDTOS.stream()
                            .map(StoreInfoDTO::getStoreId)
                            .collect(Collectors.toList());
                    cacheData.setStoreIds(storeIds);
                }
            }

            // 处理指定商品
            if (lotterySettings.getActivityProduct() == 2) {
                List<CommodityDTO> activityProductDTOS = activityLotteryCommodityService.listByActivityId(activityDO.getId());
                if (CollectionUtil.isNotEmpty(activityProductDTOS)) {
                    List<Long> commodityIds = activityProductDTOS.stream()
                            .map(CommodityDTO::getCommodityId)
                            .collect(Collectors.toList());
                    cacheData.setCommodityIds(commodityIds);
                }
            }
            // 处理人群
            if (activityDO.getParticipantGroup() != 1) {
                if (StringUtils.isNotBlank(activityDO.getSelectedGroups())) {
                    cacheData.setSelectedGroups(activityDO.getSelectedGroups());
                }
            }
        }

        // 同时使用抽奖设置ID和活动ID写缓存，兼容前端传活动ID或设置ID。
        redisCache.setCacheObject(RedisKeyConstants.LOTTERY_SETTING + lotterySettings.getId(), cacheData);
        if (lotterySettings.getActivityId() != null) {
            redisCache.setCacheObject(RedisKeyConstants.LOTTERY_SETTING + lotterySettings.getActivityId(), cacheData);
        }
        return cacheData;
    }

    /**
     * 新用户判断
     *
     * @param wxMember wxMember
     * @return boolean
     */
    public boolean userTypeSevenDay(WxMemberVO wxMember) {
        // 获取当前时间
        Date date = new Date();
        Date registerTime = wxMember.getRegisterTime();
        if (ObjectUtil.isEmpty(registerTime)) {
            return true;
        }
        long diffInMillis = Math.abs(date.getTime() - registerTime.getTime());
        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        return diffInDays <= 7;
    }

    /**
     * 老 用户判断
     *
     * @param userDate date
     * @param userDate 天数
     * @return boolean
     */
    public boolean userType(Date userDate) {
        // 获取当前时间
        if (ObjectUtil.isEmpty(userDate)) {
            return false;
        }
        Date date = new Date();
        long diffInMillis = Math.abs(date.getTime() - userDate.getTime());
        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        return diffInDays > 7;
    }

    /**
     * 回归用户判断
     *
     * @param wxMember wxMember
     * @return boolean
     */
    public boolean userTypeThirtyDay(WxMemberVO wxMember) {
        Date finalOrderFinishTime = wxMember.getFinalOrderFinishTime();
        Date registerTime = wxMember.getRegisterTime();
        log.info("判断是否是回归用户,{},{}", finalOrderFinishTime, registerTime);
        if (ObjectUtil.isEmpty(finalOrderFinishTime) || ObjectUtil.isEmpty(registerTime)) {
            return false;
        }
        // 获取当前时间
        Date date = new Date();
        long diffInMillis = Math.abs(date.getTime() - finalOrderFinishTime.getTime());
        long registerMillis = Math.abs(date.getTime() - registerTime.getTime());
        // 获取两个日期时间的毫秒差值，取绝对值避免顺序影响结果
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        long registerInDays = registerMillis / (1000 * 60 * 60 * 24);
        return diffInDays > 14 && registerInDays > 7;
    }


    /**
     * 按场次计算抽奖剩余次数（兼容00:00-24:00全天场次）
     */
    private void calculateBySession(LotterySettingsCacheDataVO settings, LotteryVO lotteryVO, LotterySettingsNumVo resultVo) {
        // 1. 解析场次时间段（支持00:00-24:00格式）
        if (settings.getLotteryLimit() == 0 && settings.getLotteryMethod() != 3) {
            resultVo.setNum(-1);
            return;
        }

        List<SessionTime> sessionTimes = parseSessionTime(settings.getTimeRange());
        if (sessionTimes.isEmpty()) {
            log.warn("按场次计算失败：timeRange格式错误，值为{}", settings.getTimeRange());
            resultVo.setNum(0);
            return;
        }

        // 2. 判断当前时间所属场次（优先处理全天场次）
        LocalTime currentTime = LocalTime.now();
        SessionTime currentSession = getCurrentSession(sessionTimes, currentTime);
        if (currentSession == null) {
            resultVo.setNum(0); // 不在任何场次内
            return;
        }

        // 3. 生成当前场次的缓存键（全天场次用固定标识）
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sessionKey = currentSession.isAllDay() ? "ALLDAY"
                : currentSession.getStartTimeStr().replace(":", "")
                + currentSession.getEndTimeStr().replace(":", "");
        String sessionCacheKey = buildLegacySessionCacheKey(settings, lotteryVO.getMemberId(), dateStr, sessionKey);
        long expireSeconds = currentSession.isAllDay()
                ? Duration.between(currentTime, LocalTime.of(23, 59, 59)).getSeconds()
                : Duration.between(currentTime, currentSession.getEndLocalTime()).getSeconds();

        // 4. 计算剩余次数（缓存过期时间适配全天场次）
        if (settings.getLotteryMethod() != 3) {
            // 计算缓存过期时间：全天场次过期到当天23:59:59，普通场次过期到场次结束
            Integer usedCount = getAndCacheUsedCount(
                    sessionCacheKey,
                    () -> lotteryAddLogService.selectLotteryNumBySession(
                            lotteryVO.getMemberId(),
                            settings.getId(),
                            currentSession.getStartLocalTime(),
                            currentSession.getEndLocalTime()
                    ),
                    expireSeconds
            );
            resultVo.setNum(calculateRemainingNum(settings.getLotteryLimit(), usedCount));
        } else {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String cacheKey = buildCommodityLotteryKey(lotteryVO.getMemberId(), settings, today);
            Integer codeFromRedis = 0;
            String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
            Integer dailyUsedCount = getAndCacheUsedCount(
                    sessionCacheKey,
                    () -> lotteryAddLogService.selectLotteryNumBySession(
                            lotteryVO.getMemberId(),
                            settings.getId(),
                            currentSession.getStartLocalTime(),
                            currentSession.getEndLocalTime()
                    ),
                    expireSeconds
            );
            try {
                codeFromRedis = redisCache.getOpsForValue(cacheKey); // 原代码可能是getCacheObject，此处保持一致
            } catch (Exception e) {
                log.warn("Redis查询商品抽奖资格异常，CacheKey：{}，异常信息：{}", cacheKey, e.getMessage(), e);
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
            // 设置剩余次数
            int num = dailyUsedCount >= settings.getLotteryLimit() ? 0 : settings.getLotteryLimit() - dailyUsedCount.intValue();
            if (num >= codeFromRedis) {
                resultVo.setNum(codeFromRedis);
            } else {
                resultVo.setNum(num);
            }

        }
    }


// ------------------------------ 辅助方法优化 ------------------------------

    /**
     * 解析场次时间（兼容00:00-24:00，将24:00转为23:59:59处理）
     */
    private List<SessionTime> parseSessionTime(String timeRange) {
        List<SessionTime> sessionTimes = new ArrayList<>();
        if (StringUtils.isBlank(timeRange)) {
            return sessionTimes;
        }
        String[] sessions = timeRange.split(",");
        for (String session : sessions) {
            String normalizedSession = session.replaceAll("\\s", "")
                    .replace("至", "-")
                    .replace("~", "-")
                    .replace("～", "-")
                    .replace("—", "-")
                    .replace("－", "-");
            String[] timePair = normalizedSession.split("-");
            if (timePair.length != 2) {
                log.warn("跳过格式错误的场次：{}", session);
                continue;
            }
            try {
                String startTimeStr = timePair[0].trim();
                String endTimeStr = timePair[1].trim();
                // 解析开始时间（00:00直接解析）
                LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                // 处理结束时间：24:00转为当天23:59:59（避免LocalTime解析错误，且符合实际业务）
                LocalTime endTime;
                if ("24:00".equals(endTimeStr)) {
                    endTime = LocalTime.of(23, 59, 59);
                } else {
                    endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                }
                // 判断是否为全天场次（00:00开始且24:00结束）
                boolean isAllDay = "00:00".equals(startTimeStr) && "24:00".equals(endTimeStr);

                sessionTimes.add(new SessionTime(
                        startTimeStr,
                        endTimeStr,
                        startTime,
                        endTime,
                        isAllDay
                ));
            } catch (DateTimeParseException e) {
                log.warn("跳过解析失败的场次：{}，原因：{}", session, e.getMessage());
            }
        }
        return sessionTimes;
    }

    /**
     * 判断当前时间所属场次（优先匹配全天场次）
     */
    private SessionTime getCurrentSession(List<SessionTime> sessionTimes, LocalTime currentTime) {
        // 1. 先检查是否有全天场次（00:00-24:00），若有则直接返回
        for (SessionTime session : sessionTimes) {
            if (session.isAllDay()) {
                return session;
            }
        }
        // 2. 非全天场次：判断当前时间是否在[start, end)区间内
        for (SessionTime session : sessionTimes) {
            LocalTime start = session.getStartLocalTime();
            LocalTime end = session.getEndLocalTime();
            // 处理跨时段（如23:00-01:00，但当前需求暂不考虑跨天场次，仅处理当天内场次）
            if (start.isBefore(end)) {
                // 正常时段（如08:00-09:30）：currentTime在[start, end)
                if (currentTime.isAfter(start.minusNanos(1)) && currentTime.isBefore(end)) {
                    return session;
                }
            } else {
                // 特殊时段（如23:00-23:59:59，实际等价于正常时段）
                if (currentTime.isAfter(start.minusNanos(1)) || currentTime.isBefore(end)) {
                    return session;
                }
            }
        }
        return null; // 不在任何场次
    }

    /**
     * 场次时间模型（新增全天标识）
     */
    @Data
    private static class SessionTime {
        private String startTimeStr;      // 原始开始时间字符串（如"00:00"）
        private String endTimeStr;        // 原始结束时间字符串（如"24:00"）
        private LocalTime startLocalTime; // 解析后的开始时间（LocalTime）
        private LocalTime endLocalTime;   // 解析后的结束时间（如24:00→23:59:59）
        private boolean isAllDay;         // 是否为全天场次（00:00-24:00）

        public SessionTime(String startTimeStr, String endTimeStr, LocalTime startLocalTime,
                           LocalTime endLocalTime, boolean isAllDay) {
            this.startTimeStr = startTimeStr;
            this.endTimeStr = endTimeStr;
            this.startLocalTime = startLocalTime;
            this.endLocalTime = endLocalTime;
            this.isAllDay = isAllDay;
        }
    }

    @Data
    private static class TaskScope {
        private final String scopeKey;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final boolean sessionScope;
    }

    private static class MultiChanceSummary {
        private TaskScope scope;
        private int totalRemaining;
        private int freeAvailableCount;
        private int pointsAvailableCount;
        private int orderAvailableCount;
        private int shareAvailableCount;
        private int browseAvailableCount;
        private int orderFinishCount;
        private int shareFinishCount;
        private int browseFinishCount;
        private int aggregateAvailableCount;
        private boolean pointsEnabled;
        private boolean pointsBlockedByBalance;

        private boolean hasAvailableChance() {
            return aggregateAvailableCount < 0 || aggregateAvailableCount > 0;
        }

        private boolean hasNonPointsSource() {
            return freeAvailableCount > 0 || orderAvailableCount > 0 || shareAvailableCount > 0 || browseAvailableCount > 0;
        }
    }

    private static class ReservedDrawChance {
        private Long activityId;
        private Long memberId;
        private Integer taskType;
        private LotteryTaskDO taskDO;
        private TaskScope scope;
        private boolean unlimited;
        private int pointsCost;
        private boolean scopeCounterReserved;
        private boolean totalCounterReserved;

        private static ReservedDrawChance of(LotteryTaskTypeEnum taskTypeEnum,
                                             LotteryTaskDO taskDO,
                                             TaskScope scope,
                                             boolean unlimited,
                                             int pointsCost) {
            ReservedDrawChance reserved = new ReservedDrawChance();
            reserved.activityId = taskDO.getActivityId();
            reserved.memberId = taskDO.getMemberId();
            reserved.taskType = taskTypeEnum.getCode();
            reserved.taskDO = taskDO;
            reserved.scope = scope;
            reserved.unlimited = unlimited;
            reserved.pointsCost = pointsCost;
            return reserved;
        }
    }
}
