package com.htyoudao.youdao.module.promotion.service.activityVoteApp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServerException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo.ActivityVoteSaveOrUpdateReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteOptionDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteOptionMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteRewardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteRewardMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.Points.PointsLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.VoteKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.CouponTimeUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Slf4j
@Service
@DS(DsNameConstants.SHARDING)
public class ActivityVoteAppServiceImpl implements ActivityVoteAppService {

    private static final int VOTE_SETTING_CACHE_SECONDS = 300;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String NO_REWARD_MESSAGE = "投票成功，感谢您的参与~";

    @Resource
    private ActivityVoteMapper activityVoteMapper;
    @Resource
    private ActivityVoteOptionMapper activityVoteOptionMapper;
    @Resource
    private ActivityVoteRewardMapper activityVoteRewardMapper;
    @Resource
    private ActivityVoteRewardLogMapper activityVoteRewardLogMapper;
    @Resource
    private ActivityVoteLogMapper activityVoteLogMapper;
    @Resource
    private ActivityService activityService;
    @Resource private ActivityAppService activityAppService;
    @Resource
    private ActivityStoreService activityStoreService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @DubboReference
    private WxMemberApi wxMemberApi;
    @Resource
    private CouponPackageService couponPackageService;
    @Resource
    private LotteryRedPacketService lotteryRedPacketService;
    @Resource
    private PointsLogMapper pointsLogMapper;
    @Resource
    private GoodCouponMapper goodCouponMapper;
    @Resource
    private UserCouponMapper userCouponMapper;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private TransactionTemplate transactionTemplate;

    private StringRedisTemplate getRedisTemplate() {
        return stringRedisTemplate;
    }

    // ==================== 公开方法 ====================

    @Override
    public AppVoteDetailVO getVoteDetail(Long activityId, Long storeId, String memberMobile) {
        // 1. 从缓存获取活动配置（cache-aside + Redisson锁）
        ActivityVoteSaveOrUpdateReqVO voteVO = getCachedVoteSetting(activityId);
        if (voteVO == null) {
            throw exception(VOTE_NOT_EXISTS);
        }

        // 2. 校验活动启用状态
        if (!Objects.equals(voteVO.getIsEnabled(), 1)) {
            throw exception(VOTE_NOT_IN_PROGRESS);
        }

        // 3. 构建返回VO
        AppVoteDetailVO detailVO = new AppVoteDetailVO();
        detailVO.setActivityId(activityId);
        detailVO.setActivityName(voteVO.getActivityName());
        detailVO.setActivityStatus(getActivityStatus(voteVO.getStartDate(), voteVO.getEndDate()));
        detailVO.setActivityImgUrl(voteVO.getActivityImgUrl());
        detailVO.setActivityBackground(voteVO.getActivityBackground());
        detailVO.setVoteBackground(voteVO.getVoteBackground());
        detailVO.setRankingBackground(voteVO.getRankingBackground());
        detailVO.setBackgroundColor(voteVO.getBackgroundColor());
        detailVO.setVoteCountFlag(voteVO.getVoteCountFlag());
        detailVO.setVoteCount(voteVO.getVoteCount());
        detailVO.setVoteBtnTitle(voteVO.getVoteBtnTitle());
        detailVO.setShareTitle(voteVO.getShareTitle());
        detailVO.setShareNote(voteVO.getShareNote());
        detailVO.setShareImgUrl(voteVO.getShareImgUrl());
        detailVO.setShareType(voteVO.getShareType());
        detailVO.setPublicButton(voteVO.getPublicButton());
        detailVO.setCommunityFlag(voteVO.getCommunityFlag());
        detailVO.setGuideImage(voteVO.getGuideImage());
        detailVO.setActivityRules(voteVO.getActivityRules());
        detailVO.setStartDate(voteVO.getStartDate());
        detailVO.setEndDate(voteVO.getEndDate());

        // 4. 从缓存获取选项列表，票数从Redis计数器读取
        List<ActivityVoteOptionDO> options = getCachedOptions(activityId);
        Long totalVotes = getOptionTotalCount(activityId);

        List<AppVoteDetailVO.OptionVO> optionVOList = new ArrayList<>();
        for (ActivityVoteOptionDO option : options) {
            AppVoteDetailVO.OptionVO optionVO = new AppVoteDetailVO.OptionVO();
            optionVO.setId(option.getId());
            optionVO.setOptionName(option.getOptionName());
            optionVO.setOptionUrl(option.getOptionUrl());
            optionVO.setOptionDetailUrl(option.getOptionDetailUrl());
            optionVO.setVoteDetail(option.getVoteDetail());

            Long voteNum = getOptionVoteCount(activityId, option.getId());
            optionVO.setVoteNum(voteNum);
            if (totalVotes > 0) {
                BigDecimal percent = BigDecimal.valueOf(voteNum * 100.0 / totalVotes).setScale(1, RoundingMode.HALF_UP);
                optionVO.setVotePercent(percent.toPlainString() + "%");
            } else {
                optionVO.setVotePercent("0%");
            }
            optionVOList.add(optionVO);
        }
        detailVO.setOptionList(optionVOList);

        // 5. 从Redis读取当前用户（手机号）投票次数
        if (ObjectUtil.isNotEmpty(memberMobile)) {
            Long mobile = Long.parseLong(memberMobile);
            long mobileTotal = getMobileTotalCount(activityId, mobile, calcMemberKeyTtlSeconds(voteVO.getEndDate()));
            detailVO.setHasVoted(mobileTotal > 0);
            detailVO.setRemainVoteCount(calcRemainFromRedis(voteVO, activityId, mobile));
        } else {
            detailVO.setHasVoted(false);
            detailVO.setRemainVoteCount(voteVO.getVoteCountFlag() == 0 ? -1 : voteVO.getVoteCount());
        }

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppVoteResultVO doVote(AppVoteActionReqVO reqVO) {
        Long activityId = reqVO.getActivityId();
        Long memberMobile = reqVO.getMemberMobile();
        Long optionId = reqVO.getOptionId();

        // 1. 从缓存获取活动配置
        ActivityVoteSaveOrUpdateReqVO voteVO = getCachedVoteSetting(activityId);
        if (voteVO == null) {
            throw exception(VOTE_NOT_EXISTS);
        }

        // 2. 校验活动启用+时间（直接从缓存判断，无需查DB）
        if (!Objects.equals(voteVO.getIsEnabled(), 1)) {
            throw exception(VOTE_NOT_IN_PROGRESS);
        }
        Integer status = getActivityStatus(voteVO.getStartDate(), voteVO.getEndDate());
        if (status == 1) {
            // 未开始
            throw exception(VOTE_BEFORE_PROGRESS);
        }
        if (status == 3) {
            // 已结束
            throw exception(VOTE_AFTER_PROGRESS);
        }

        // 3. 校验门店（指定门店模式下直接查DB）
        validateStore(voteVO, activityId, reqVO.getStoreId());

        // 3.1 调用活动统一参与校验：社群专享不满足时这里会抛出业务异常，阻断后续落库和发奖。
        activityAppService.checkCanJoin(reqVO.getActivityId());

        // 4. 校验选项存在（从缓存）
        List<ActivityVoteOptionDO> options = getCachedOptions(activityId);
        ActivityVoteOptionDO optionDO = options.stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElse(null);
        if (optionDO == null) {
            throw exception(VOTE_OPTION_NOT_EXISTS);
        }

        // 4. 用户级分布式锁（同一用户同一活动串行，不同用户并行）
        String voteLockKey = VoteKeyConstants.VOTE_LOCK_PREFIX + "doVote:" + activityId + ":" + memberMobile;
        RLock voteLock = redissonClient.getLock(voteLockKey);
        boolean locked = false;
        try {
            locked = voteLock.tryLock(500, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw exception(VOTE_COUNT_LIMIT);
            }

            // 5. Redis校验投票次数限制 + 原子递增（基于手机号）
            validateAndIncrementVoteCount(voteVO, activityId, memberMobile);

            // 6~8 如果后续步骤异常，需回退Redis计数
            String optionCountKey = VoteKeyConstants.VOTE_OPTION_COUNT + activityId + ":" + optionId;
            boolean optionIncremented = false;
            try {
                // 6. 选项票数Redis原子递增（先确保key已从DB初始化，防止驱逐后从0开始）
                ensureOptionKeyInitialized(optionCountKey, activityId, optionId);
                getRedisTemplate().opsForValue().increment(optionCountKey, 1);
                optionIncremented = true;

                // 7. 获取会员昵称 + 门店名称（后续投票记录和奖励记录都需要）
                String memberNickName = reqVO.getMemberNickName();
                String storeName = reqVO.getStoreName();

                // 8. 投票记录异步落库（不阻塞主流程，DB写入耗时~400ms）
                ActivityVoteLogDO voteLog = new ActivityVoteLogDO();
                voteLog.setActivityId(activityId);
                voteLog.setOptionId(optionId);
                voteLog.setOptionName(optionDO.getOptionName());
                voteLog.setMemberId(reqVO.getMemberId());
                voteLog.setMemberMobile(memberMobile);
                voteLog.setMemberName(memberNickName);
                voteLog.setStoreId(reqVO.getStoreId());
                voteLog.setStoreName(storeName);
                voteLog.setVoteTime(LocalDateTime.now());
                Long insertBusinessId = BusinessContextHolder.getBusinessId();
                CompletableFuture.runAsync(() -> {
                    DynamicDataSourceContextHolder.push(DsNameConstants.SHARDING);
                    BusinessContextHolder.setBusinessId(insertBusinessId);
                    try {
                        activityVoteLogMapper.insert(voteLog);
                    } catch (Exception e) {
                        log.error("[doVote] 异步写入投票记录异常, activityId={}, optionId={}, memberMobile={}",
                                activityId, optionId, memberMobile, e);
                    } finally {
                        BusinessContextHolder.clear();
                        DynamicDataSourceContextHolder.poll();
                    }
                });

                // 9. 尝试发放奖励（基于手机号判定是否已领取）
                AppVoteResultVO resultVO = new AppVoteResultVO();
                resultVO.setSuccess(true);
                resultVO.setRemainVoteCount(calcRemainFromRedis(voteVO, activityId, memberMobile));

                List<AppVoteMyRewardRespVO> rewardList = tryGrantReward(activityId, memberMobile, reqVO.getMemberId(), reqVO.getStoreId(),
                        memberNickName, storeName, calcMemberKeyTtlSeconds(voteVO.getEndDate()));
                if (CollUtil.isNotEmpty(rewardList)) {
                    resultVO.setFirstVote(true);
                    resultVO.setRewardList(rewardList);
                } else {
                    resultVO.setFirstVote(false);
                    resultVO.setMessage(NO_REWARD_MESSAGE);
                }

                return resultVO;
            } catch (Exception e) {
                // 回退投票次数计数
                rollbackVoteCount(voteVO, activityId, memberMobile);
                // 回退选项票数
                if (optionIncremented) {
                    getRedisTemplate().opsForValue().increment(optionCountKey, -1);
                }
                throw e;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(VOTE_COUNT_LIMIT);
        } finally {
            if (locked && voteLock.isHeldByCurrentThread()) {
                voteLock.unlock();
            }
        }
    }

    @Override
    public AppVoteRankingVO getRanking(Long activityId) {
        AppVoteRankingVO rankingVO = new AppVoteRankingVO();

        Long totalVotes = getOptionTotalCount(activityId);
        rankingVO.setTotalVoteCount(totalVotes);

        // 参与人数：按手机号去重
        /*List<ActivityVoteLogDO> distinctMembers = activityVoteLogMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteLogDO>()
                        .select(ActivityVoteLogDO::getMemberMobile)
                        .eq(ActivityVoteLogDO::getActivityId, activityId)
                        .groupBy(ActivityVoteLogDO::getMemberMobile));
        rankingVO.setTotalParticipantCount((long) distinctMembers.size());*/

        // 选项排行（选项从缓存，票数从Redis）
        List<ActivityVoteOptionDO> options = getCachedOptions(activityId);
        List<AppVoteRankingVO.RankItem> rankList = new ArrayList<>();
        for (ActivityVoteOptionDO option : options) {
            AppVoteRankingVO.RankItem item = new AppVoteRankingVO.RankItem();
            item.setOptionId(option.getId());
            item.setOptionName(option.getOptionName());
            item.setOptionUrl(option.getOptionUrl());

            Long voteNum = getOptionVoteCount(activityId, option.getId());
            item.setVoteNum(voteNum);
            if (totalVotes > 0) {
                BigDecimal percent = BigDecimal.valueOf(voteNum * 100.0 / totalVotes).setScale(1, RoundingMode.HALF_UP);
                item.setVotePercent(percent.toPlainString() + "%");
            } else {
                item.setVotePercent("0%");
            }
            rankList.add(item);
        }

        rankList.sort((a, b) -> Long.compare(b.getVoteNum(), a.getVoteNum()));
        for (int i = 0; i < rankList.size(); i++) {
            rankList.get(i).setRank(i + 1);
        }
        rankingVO.setRankList(rankList);

        return rankingVO;
    }

    @Override
    public AppVoteShareVO getShareVO(Long activityId) {
        ActivityVoteSaveOrUpdateReqVO voteVO = getCachedVoteSetting(activityId);
        AppVoteShareVO shareVO = new AppVoteShareVO();
        shareVO.setActivityId(activityId);
        if (voteVO != null) {
            shareVO.setShareTitle(voteVO.getShareTitle());
            shareVO.setShareNote(voteVO.getShareNote());
            shareVO.setShareImgUrl(voteVO.getShareImgUrl());
            shareVO.setShareType(voteVO.getShareType());
        }
        return shareVO;
    }

    @Override
    public List<AppVoteMyRewardRespVO> getMyRewards(Long activityId, Long memberMobile) {
        List<ActivityVoteRewardLogDO> logs = activityVoteRewardLogMapper.selectList(
                new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                        .eq(ActivityVoteRewardLogDO::getActivityId, activityId)
                        .eq(ActivityVoteRewardLogDO::getMemberMobile, memberMobile)
                        .orderByDesc(ActivityVoteRewardLogDO::getGrantTime));
        return logs.stream().map(log -> {
            AppVoteMyRewardRespVO vo = new AppVoteMyRewardRespVO();
            vo.setId(log.getId());
            vo.setActivityId(log.getActivityId());
            vo.setPrizeType(log.getPrizeType());
            vo.setPrizeName(log.getPrizeName());
            vo.setPrizeImgUrl(log.getPrizeImgUrl());
            vo.setPrizeValue(log.getPrizeValue());
            vo.setClaimStatus(log.getClaimStatus());
            vo.setPrizeState(log.getPrizeState());
            vo.setReceiveUser(log.getReceiveUser());
            vo.setReceiveMobile(log.getReceiveMobile());
            vo.setReceiveAddress(log.getReceiveAddress());
            vo.setExpressCompany(log.getExpressCompany());
            vo.setTrackingNumber(log.getTrackingNumber());
            vo.setGrantTime(log.getGrantTime());
            vo.setPackageInfo(log.getPackageInfo());
            vo.setOutBillNo(log.getOutBillNo());
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAddress(AppVoteSaveAddressReqVO reqVO, Long memberMobile) {
        // 校验记录存在且属于当前用户
        ActivityVoteRewardLogDO rewardLog = activityVoteRewardLogMapper.selectById(reqVO.getRewardLogId());
        if (rewardLog == null || !Objects.equals(rewardLog.getMemberMobile(), memberMobile)) {
            throw exception(VOTE_NOT_EXISTS);
        }
        // 仅实物奖品（prizeType=4）且状态为未填写地址（prizeState=1）时允许保存
        if (rewardLog.getPrizeType() == null || rewardLog.getPrizeType() != 4) {
            throw exception(VOTE_NOT_EXISTS);
        }
        if (rewardLog.getPrizeState() == null || rewardLog.getPrizeState() != 1) {
            throw exception(VOTE_NOT_EXISTS);
        }
        // 更新地址信息，状态改为待发货
        LambdaUpdateWrapper<ActivityVoteRewardLogDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityVoteRewardLogDO::getId, reqVO.getRewardLogId())
                .eq(ActivityVoteRewardLogDO::getMemberMobile, memberMobile)
                .eq(ActivityVoteRewardLogDO::getPrizeState, 1)
                .set(ActivityVoteRewardLogDO::getReceiveUser, reqVO.getReceiveUser())
                .set(ActivityVoteRewardLogDO::getReceiveMobile, reqVO.getReceiveMobile())
                .set(ActivityVoteRewardLogDO::getReceiveAddress, reqVO.getReceiveAddress())
                .set(ActivityVoteRewardLogDO::getPrizeState, 2);
        activityVoteRewardLogMapper.update(null, updateWrapper);
    }

    /*@Override
    public void shipReward(AppVoteShipReqVO reqVO, Long memberMobile) {
        // 校验记录存在且属于当前用户
        ActivityVoteRewardLogDO rewardLog = activityVoteRewardLogMapper.selectById(reqVO.getRewardLogId());
        if (rewardLog == null || !Objects.equals(rewardLog.getMemberMobile(), memberMobile)) {
            throw exception(VOTE_NOT_EXISTS);
        }
        // 仅实物奖品（prizeType=4）且状态为待发货（prizeState=2）时允许发货
        if (rewardLog.getPrizeType() == null || rewardLog.getPrizeType() != 4) {
            throw exception(VOTE_NOT_EXISTS);
        }
        if (rewardLog.getPrizeState() == null || rewardLog.getPrizeState() != 2) {
            throw exception(VOTE_NOT_EXISTS);
        }
        // 更新快递信息，状态改为已发货
        LambdaUpdateWrapper<ActivityVoteRewardLogDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityVoteRewardLogDO::getId, reqVO.getRewardLogId())
                .eq(ActivityVoteRewardLogDO::getMemberMobile, memberMobile)
                .eq(ActivityVoteRewardLogDO::getPrizeState, 2)
                .set(ActivityVoteRewardLogDO::getExpressCompany, reqVO.getExpressCompany())
                .set(ActivityVoteRewardLogDO::getTrackingNumber, reqVO.getTrackingNumber())
                .set(ActivityVoteRewardLogDO::getPrizeState, 3);
        activityVoteRewardLogMapper.update(null, updateWrapper);
    }*/

    // ==================== 奖励发放逻辑 ====================

    /**
     * 尝试发放奖励（首次投票发放所有有库存的奖励）
     * 1. SETNX判定用户是否已领取（手机号标识，TTL=活动结束+7天）
     * 2. 从缓存获取奖励配置
     * 3. 遍历所有奖励，逐个扣库存并发放
     * 4. 全部发完则返回空列表（提示"感谢您的参与"）
     * 5. 每个成功扣减的奖励记录领奖日志
     */
    private List<AppVoteMyRewardRespVO> tryGrantReward(Long activityId, Long memberMobile, Long memberId, Long storeId,
                                                            String memberNickName, String storeName, long ttlSeconds) {
        long gt0 = System.currentTimeMillis();
        // 1. SETNX标记已领取（TTL=活动结束+7天，活动重新启用时从DB恢复）
        String claimedKey = VoteKeyConstants.VOTE_REWARD_CLAIMED + activityId + ":" + memberMobile;
        Boolean claimed = getRedisTemplate().opsForValue().setIfAbsent(claimedKey, "1", ttlSeconds, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(claimed)) {
            log.info("[tryGrantReward] SETNX已存在, cost={}ms", System.currentTimeMillis() - gt0);
            return Collections.emptyList();
        }
        log.info("[tryGrantReward] step1 SETNX cost={}ms", System.currentTimeMillis() - gt0);

        // 1.1 DB回源防重校验：SETNX成功但reward_log已有记录，说明claimed key丢失（Redis数据丢失/键驱逐）
        // 或发放残留用户在禁用→启用重建后重领，直接按已领过拦截。查询异常不catch，上抛中断发放（fail-closed，
        // 与SETNX异常语义一致，doVote外层catch会回退票数）
        Long existedRewardLogCount = activityVoteRewardLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteRewardLogDO>()
                        .eq(ActivityVoteRewardLogDO::getActivityId, activityId)
                        .eq(ActivityVoteRewardLogDO::getMemberMobile, memberMobile));
        if (existedRewardLogCount != null && existedRewardLogCount > 0) {
            log.warn("[tryGrantReward] claimed key缺失但存在发放记录，疑似Redis数据丢失，已按记录拦截重复发放, activityId={}, memberMobile={}, rewardLogCount={}",
                    activityId, memberMobile, existedRewardLogCount);
            return Collections.emptyList();
        }

        // 2. 从缓存获取奖励配置列表
        List<ActivityVoteRewardDO> rewards = getCachedRewardConfig(activityId);
        if (CollUtil.isEmpty(rewards)) {
            return Collections.emptyList();
        }
        log.info("[tryGrantReward] step1+2 SETNX+getCachedRewardConfig cost={}ms, rewards={}", System.currentTimeMillis() - gt0, rewards.size());

        // 获取openid（红包发放需要）：优先从SecurityContext取，取不到再查wxMember
        String openid = SecurityFrameworkUtils.getLoginOpenid();
        // 存在依赖会员信息的奖励（积分/优惠券/券包/红包，prizeType∈{1,2,3,5}）时，
        // 同步请求线程内一次性查询会员并全链路透传，同一memberId仅一次RPC。
        // RPC失败不阻断发放流程：红包fail-closed跳过，非红包由各发放方法判空抛错并由单奖品隔离兜底。
        boolean hasMemberDependentReward = rewards.stream().anyMatch(r -> r.getPrizeType() != null
                && (r.getPrizeType() == 1 || r.getPrizeType() == 2 || r.getPrizeType() == 3 || r.getPrizeType() == 5));
        WxMemberDTO wxMember = null;
        Exception memberQueryEx = null;
        if (hasMemberDependentReward) {
            try {
                wxMember = getWxMemberDTO(memberId);
            } catch (Exception e) {
                memberQueryEx = e;
                log.warn("[tryGrantReward] 查询会员信息异常, memberId={}", memberId, e);
            }
            if (wxMember != null && StrUtil.isEmpty(openid)) {
                openid = wxMember.getOpenid();
            }
        }

        // 4. 遍历所有奖励，全部同步发放
        List<AppVoteMyRewardRespVO> grantedList = new ArrayList<>();
        for (ActivityVoteRewardDO reward : rewards) {
            long gt2 = System.currentTimeMillis();
            // 红包用户类型过滤：红包仅发放给微信用户，支付宝用户跳过（不占库存、不插发放记录、不进响应）。
            // 口径对齐抽签/抽奖 isAlipayMember：memberCategory==1 才算支付宝，null按微信放行
            if (reward.getPrizeType() != null && reward.getPrizeType() == 5) {
                if (memberQueryEx != null) {
                    // 会员查询RPC失败，fail-closed跳过红包
                    log.warn("[tryGrantReward] 会员查询失败跳过红包发放, activityId={}, rewardId={}, memberId={}",
                            activityId, reward.getId(), memberId);
                    continue;
                }
                if (wxMember == null) {
                    log.warn("[tryGrantReward] 会员信息不存在跳过红包发放, activityId={}, rewardId={}, memberId={}",
                            activityId, reward.getId(), memberId);
                    continue;
                }
                if (Objects.equals(wxMember.getMemberCategory(), 1)) {
                    log.warn("[tryGrantReward] 支付宝用户跳过红包发放, activityId={}, rewardId={}, memberId={}",
                            activityId, reward.getId(), memberId);
                    continue;
                }
            }
            // 库存预检：无库存的奖励不进入rewardList、不提交发放任务（INCR兜底仍在发放路径内）
            if (!hasStock(reward)) {
                log.warn("[tryGrantReward] 奖励库存不足跳过, activityId={}, rewardId={}, prizeType={}",
                        activityId, reward.getId(), reward.getPrizeType());
                continue;
            }
            // 构建基础发放结果
            AppVoteMyRewardRespVO rewardInfo = new AppVoteMyRewardRespVO();
            rewardInfo.setActivityId(activityId);
            rewardInfo.setPrizeType(reward.getPrizeType());
            rewardInfo.setPrizeName(reward.getPrizeName());
            rewardInfo.setPrizeImgUrl(reward.getPrizeImgUrl());
            rewardInfo.setPrizeValue(reward.getPrizeValue());
            rewardInfo.setPrizeState(reward.getPrizeType() == 4 ? 1 : 0);
            rewardInfo.setGrantTime(LocalDateTime.now());

            // 该奖励是否“真实发放成功且记录插入成功”——只有为true才允许进入响应清单，保证响应与reward_log记录严格一致
            boolean grantSuccess;
            if (reward.getPrizeType() == 5) {
                // ===== 红包：同步发放（需要返回packageInfo给前端） =====
                Integer rpClaimStatus = 1;
                String rpPackageInfo = null;
                String rpOutBillNo = null;
                boolean stockIncremented = false;
                boolean transferSuccess = false;
                try {
                    // 库存检查
                    if (reward.getTotalNum() > 0) {
                        String stockKey = VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId();
                        ensureStockKeyInitialized(stockKey, reward.getId());
                        Long usedCount = getRedisTemplate().opsForValue().increment(stockKey, 1);
                        if (usedCount == null || usedCount > reward.getTotalNum()) {
                            getRedisTemplate().opsForValue().increment(stockKey, -1);
                            log.warn("[tryGrantReward] 红包发放库存不足跳过, activityId={}, rewardId={}, prizeType={}",
                                    activityId, reward.getId(), reward.getPrizeType());
                            continue;
                        }
                        stockIncremented = true;
                    }

                    if (StrUtil.isNotEmpty(openid)) {
                        LotteryRedPacketVo redPacketVo = new LotteryRedPacketVo();
                        redPacketVo.setOpenId(openid);
                        redPacketVo.setTransferAmount(reward.getPrizeValue()
                                .multiply(new BigDecimal(100))
                                .setScale(0, RoundingMode.HALF_UP)
                                .intValue());
                        redPacketVo.setTransferRemark("投票活动红包");
                        redPacketVo.setActivityId(activityId);

                        // REQUIRES_NEW隔离事务，防止transferUser异常标记外层doVote事务rollback-only
                        // 注意：不能修改共享的transactionTemplate bean（线程不安全），每次新建实例
                        long gtTransfer = System.currentTimeMillis();
                        TransactionTemplate requiresNewTemplate = new TransactionTemplate(transactionTemplate.getTransactionManager());
                        requiresNewTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                        TransferToUser.TransferToUserResponse response;
                        response = requiresNewTemplate.execute(status ->
                                lotteryRedPacketService.transferUser(redPacketVo));
                        log.info("[tryGrantReward] transferUser cost={}ms, prizeType=5", System.currentTimeMillis() - gtTransfer);
                        if (response != null && response.getCode() == 200) {
                            rpPackageInfo = response.getPackageInfo();
                            rpOutBillNo = response.getOutBillNo();
                            rewardInfo.setPackageInfo(rpPackageInfo);
                            rewardInfo.setOutBillNo(rpOutBillNo);
                            // 领取状态保持未领取(1)，由微信回调接口更新为已领取
                            transferSuccess = true;
                            log.info("[tryGrantReward] 红包发放成功, memberId={}, outBillNo={}", memberId, rpOutBillNo);
                        } else {
                            String msg = response != null ? response.getMessage() : "null response";
                            log.error("[tryGrantReward] 红包转账失败, 不插发放记录、不计used_num, activityId={}, rewardId={}, memberId={}, msg={}",
                                    activityId, reward.getId(), memberId, msg);
                            // 红包发放失败，回退库存，且不插记录、不进响应（SETNX已标记，不会重复尝试）
                            if (stockIncremented) {
                                getRedisTemplate().opsForValue().increment(VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId(), -1);
                            }
                        }
                    } else {
                        log.warn("[tryGrantReward] 会员openId为空, 红包不发放、不插记录, activityId={}, rewardId={}, memberId={}",
                                activityId, reward.getId(), memberId);
                        // openId为空，回退库存，且不插记录、不进响应
                        if (stockIncremented) {
                            getRedisTemplate().opsForValue().increment(VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId(), -1);
                        }
                    }
                } catch (Exception e) {
                    log.error("[tryGrantReward] 红包发放异常, 不插发放记录, activityId={}, rewardId={}, memberId={}",
                            activityId, reward.getId(), memberId, e);
                    // 异常回退库存，且不插记录、不进响应
                    if (stockIncremented) {
                        getRedisTemplate().opsForValue().increment(VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId(), -1);
                    }
                }
                rewardInfo.setClaimStatus(rpClaimStatus);

                grantSuccess = false;
                if (transferSuccess) {
                    // 仅转账真实成功才插发放日志并计used_num；插入失败则钱已转出但无记录，需人工对账，且该奖品不进响应清单
                    try {
                        Long rpRewardLogId = insertRewardLogAndGrant(activityId, memberMobile, memberId, storeId, memberNickName, storeName, reward,
                                rpPackageInfo, rpOutBillNo, rpClaimStatus, wxMember);
                        // 回填刚插入的activity_vote_reward_log记录id到响应VO
                        rewardInfo.setId(rpRewardLogId);
                        if (reward.getTotalNum() > 0) {
                            updateUsedNum(reward.getId());
                        }
                        grantSuccess = true;
                    } catch (Exception e) {
                        log.error("[tryGrantReward] 红包转账已成功但发放记录插入失败, 需人工对账! activityId={}, rewardId={}, memberId={}, outBillNo={}",
                                activityId, reward.getId(), memberId, rpOutBillNo, e);
                    }
                }
            } else {
                // ===== 非红包：同步发放（库存检查 + 记录日志 + 发放 + 更新usedNum），仅真实成功才进响应清单 =====
                Long grantedRewardLogId = grantNonRedPacketReward(activityId, memberMobile, memberId, storeId, memberNickName, storeName, reward, wxMember);
                grantSuccess = grantedRewardLogId != null;
                if (grantSuccess) {
                    // 回填刚插入的activity_vote_reward_log记录id到响应VO
                    rewardInfo.setId(grantedRewardLogId);
                }
            }

            log.info("[tryGrantReward] reward prizeType={} cost={}ms", reward.getPrizeType(), System.currentTimeMillis() - gt2);
            // 仅“真实发放成功且记录插入成功”的奖励才进入响应清单，保证响应与reward_log记录严格一致
            if (grantSuccess) {
                grantedList.add(rewardInfo);
            }
        }

        log.info("[tryGrantReward] TOTAL cost={}ms", System.currentTimeMillis() - gt0);
        return grantedList;
    }

    /**
     * 库存预检（同步阶段只读GET，不做扣减；实际扣减仍由发放路径内的INCR兜底）
     * 读库存失败（GET为null或解析异常）时fail-open返回true，不阻断发放
     */
    private boolean hasStock(ActivityVoteRewardDO reward) {
        if (reward.getTotalNum() == null || reward.getTotalNum() <= 0) {
            return true; // 不限库存
        }
        String stockKey = VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId();
        Object value;
        try {
            ensureStockKeyInitialized(stockKey, reward.getId());
            value = getRedisTemplate().opsForValue().get(stockKey);
        } catch (Exception e) {
            log.warn("[tryGrantReward] 库存预检读取失败fail-open, activityId={}, rewardId={}, prizeType={}",
                    reward.getActivityId(), reward.getId(), reward.getPrizeType(), e);
            return true;
        }
        if (value == null) {
            return true;
        }
        long usedCount;
        try {
            usedCount = Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            log.warn("[tryGrantReward] 库存预检值解析异常fail-open, rewardId={}, value={}", reward.getId(), value);
            return true;
        }
        return usedCount < reward.getTotalNum();
    }

    /**
     * 确保奖励已下发数量key已初始化（防止Redis驱逐后从0开始）
     */
    private void ensureStockKeyInitialized(String stockKey, Long rewardId) {
        if (Boolean.TRUE.equals(getRedisTemplate().hasKey(stockKey)) && !isCorruptedNumericKey(stockKey)) {
            return;
        }
        ActivityVoteRewardDO dbReward = activityVoteRewardMapper.selectById(rewardId);
        int currentUsed = (dbReward != null && dbReward.getUsedNum() != null) ? dbReward.getUsedNum() : 0;
        getRedisTemplate().opsForValue().setIfAbsent(stockKey, String.valueOf(currentUsed));
    }

    /**
     * 非红包奖励同步发放（库存INCR检查 + 实际发放 + 插领奖日志 + 更新usedNum）
     * 单个奖励发放失败（异常/库存超限）只记录日志并回退库存，不向外抛异常，不影响其他奖励发放
     *
     * @return 仅当“发放+插领奖日志”完全成功返回插入的发放记录id；库存超限或异常均返回null（调用方据此决定是否加入响应清单）
     */
    private Long grantNonRedPacketReward(Long activityId, Long memberMobile, Long memberId, Long storeId,
                                         String memberNickName, String storeName, ActivityVoteRewardDO reward,
                                         WxMemberDTO wxMember) {
        boolean stockIncremented = false;
        try {
            // 库存检查
            if (reward.getTotalNum() > 0) {
                String stockKey = VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId();
                ensureStockKeyInitialized(stockKey, reward.getId());
                Long usedCount = getRedisTemplate().opsForValue().increment(stockKey, 1);
                if (usedCount == null || usedCount > reward.getTotalNum()) {
                    getRedisTemplate().opsForValue().increment(stockKey, -1);
                    log.warn("[grantNonRedPacketReward] 库存不足跳过, activityId={}, rewardId={}, prizeType={}",
                            activityId, reward.getId(), reward.getPrizeType());
                    return null;
                }
                stockIncremented = true;
            }
            // 插入领奖日志 + 实际发放
            Long insertedRewardLogId = insertRewardLogAndGrant(activityId, memberMobile, memberId, storeId, memberNickName, storeName, reward,
                    null, null, null, wxMember);
            if (reward.getTotalNum() > 0) {
                updateUsedNum(reward.getId());
            }
            return insertedRewardLogId;
        } catch (Exception e) {
            log.error("[grantNonRedPacketReward] 发放非红包奖励异常, activityId={}, rewardId={}, prizeType={}",
                    activityId, reward.getId(), reward.getPrizeType(), e);
            // 发放失败，回退库存
            if (stockIncremented) {
                getRedisTemplate().opsForValue().increment(VoteKeyConstants.VOTE_REWARD_STOCK + reward.getId(), -1);
            }
            return null;
        }
    }

    /**
     * 同步更新DB奖励已使用数量
     */
    private void updateUsedNum(Long rewardId) {
        LambdaUpdateWrapper<ActivityVoteRewardDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ActivityVoteRewardDO::getId, rewardId)
                .setSql("used_num = used_num + 1");
        activityVoteRewardMapper.update(null, wrapper);
    }

    /**
     * 异步插入领奖记录 + 实际发放奖品（红包路径使用，内部包装async）
     */
    private void asyncInsertRewardLog(Long activityId, Long memberMobile, Long memberId, Long storeId,
                                      String memberNickName, String storeName,
                                      ActivityVoteRewardDO reward,
                                      String rpPackageInfo, String rpOutBillNo, Integer rpClaimStatus) {
        Long businessId = BusinessContextHolder.getBusinessId();
        CompletableFuture.runAsync(() -> {
            DynamicDataSourceContextHolder.push(DsNameConstants.SHARDING);
            BusinessContextHolder.setBusinessId(businessId);
            try {
                insertRewardLogAndGrant(activityId, memberMobile, memberId, storeId, memberNickName, storeName, reward,
                        rpPackageInfo, rpOutBillNo, rpClaimStatus, null);
            } catch (Exception e) {
                log.error("[asyncInsertRewardLog] 异步插入领奖记录失败, activityId={}, mobile={}", activityId, memberMobile, e);
            } finally {
                BusinessContextHolder.clear();
                DynamicDataSourceContextHolder.poll();
            }
        });
    }

    /**
     * 同步插入领奖记录 + 实际发放奖品（红包/非红包路径均直接调用）
     *
     * @return 插入成功返回发放记录主键id（MyBatis-Plus insert后回填）
     */
    private Long insertRewardLogAndGrant(Long activityId, Long memberMobile, Long memberId, Long storeId,
                                         String memberNickName, String storeName,
                                         ActivityVoteRewardDO reward,
                                         String rpPackageInfo, String rpOutBillNo, Integer rpClaimStatus,
                                         WxMemberDTO wxMember) {
        if (reward.getPrizeType() == 5) {
            // 红包已在tryGrantReward中同步发放，直接插入日志
            ActivityVoteRewardLogDO rewardLog = buildRewardLog(activityId, memberMobile, memberId, storeId, memberNickName, storeName, reward);
            rewardLog.setPackageInfo(rpPackageInfo);
            rewardLog.setOutBillNo(rpOutBillNo);
            rewardLog.setClaimStatus(rpClaimStatus != null ? rpClaimStatus : 1);
            activityVoteRewardLogMapper.insert(rewardLog);
            return rewardLog.getId();
        } else {
            // 非红包：先发放，成功后再插日志
            // 发放失败→异常向上抛→外层回退库存→DB无记录→SETNX可恢复重试
            grantRewardActual(activityId, memberId, memberMobile, storeId, reward, wxMember);

            ActivityVoteRewardLogDO rewardLog = buildRewardLog(activityId, memberMobile, memberId, storeId, memberNickName, storeName, reward);
            activityVoteRewardLogMapper.insert(rewardLog);
            return rewardLog.getId();
        }
    }

    private ActivityVoteRewardLogDO buildRewardLog(Long activityId, Long memberMobile, Long memberId,
                                                    Long storeId, String memberNickName, String storeName,
                                                    ActivityVoteRewardDO reward) {
        ActivityVoteRewardLogDO rewardLog = new ActivityVoteRewardLogDO();
        rewardLog.setActivityId(activityId);
        rewardLog.setMemberId(memberId);
        rewardLog.setMemberMobile(memberMobile);
        rewardLog.setMemberName(memberNickName);
        rewardLog.setStoreId(storeId);
        rewardLog.setStoreName(storeName);
        rewardLog.setPrizeType(reward.getPrizeType());
        rewardLog.setPrizeId(reward.getPrizeId());
        rewardLog.setPrizeName(reward.getPrizeName());
        rewardLog.setPrizeImgUrl(reward.getPrizeImgUrl());
        rewardLog.setPrizeValue(reward.getPrizeValue());
        rewardLog.setGrantTime(LocalDateTime.now());
        rewardLog.setPrizeState(reward.getPrizeType() == 4 ? 1 : 0); // 实物：未填写地址
        return rewardLog;
    }

    /**
     * 实际发放奖品（根据prizeType分发到不同的发放逻辑）
     * 异常不捕获，向上传播给调用方处理（回退库存等）
     */
    private void grantRewardActual(Long activityId, Long memberId, Long memberMobile, Long storeId,
                                   ActivityVoteRewardDO reward, WxMemberDTO wxMember) {
        switch (reward.getPrizeType()) {
            case 1 -> grantPointsReward(activityId, memberId, memberMobile, reward, wxMember);
            case 2 -> grantCouponReward(activityId, memberId, memberMobile, storeId, reward, wxMember);
            case 3 -> grantCouponPackageReward(activityId, memberId, memberMobile, storeId, reward, wxMember);
            // case 4: 实物奖品仅记录不发放，等用户填写地址后发货
            case 4 -> {
            }
            // case 5: 红包已在tryGrantReward中同步发放，此处不再重复处理
            default -> log.warn("[grantRewardActual] 未知奖品类型, prizeType={}", reward.getPrizeType());
        }
    }

    /**
     * 发放积分（参考SurveyAnswerServiceImpl.grantPointsReward）
     * 1. 获取会员当前积分 → 累加 → 回写
     * 2. 插入积分日志
     */
    private void grantPointsReward(Long activityId, Long memberId, Long memberMobile, ActivityVoteRewardDO reward,
                                   WxMemberDTO wxMember) {
        // 会员信息由tryGrantReward预查后透传，预查失败/不存在时为null，保持原失败语义
        if (wxMember == null) {
            throw exception(new ErrorCode(1_004_001_000, "积分发放失败: 会员信息不存在, memberId=" + memberId));
        }

        int rewardPoints = reward.getPrizeValue().intValue();
        int newIntegral = wxMember.getMemberIntegral() + rewardPoints;
        wxMemberApi.updateMemberById(memberId, newIntegral);

        // 插入积分日志
        PointsLogDO pointsLog = new PointsLogDO();
        pointsLog.setMemberId(memberId);
        pointsLog.setMemberMobile(String.valueOf(memberMobile));
        pointsLog.setMemberName(wxMember.getMemberName());
        pointsLog.setMemberNickName(wxMember.getMemberNickName());
        pointsLog.setPointsChange((long) rewardPoints);
        pointsLog.setPointsType(1); // 1=获取
        pointsLog.setIsDelete(0);
        pointsLog.setPointsLogStatus(1);
        pointsLog.setIsPointsProduct(2);
        pointsLog.setProductType(6); // 6=投票奖励
        pointsLog.setProductId(activityId);

        Number number = identifierGenerator.nextId(null);
        long logId = number.longValue();
        pointsLog.setPointsLogId(logId);
        pointsLog.setLogCode(String.valueOf(logId));
        pointsLog.setShardingValue(wxMember.getShardingValue());
        if (pointsLog.getShardingValue() != null) {
            String pointsLogIdStr = logId + "" + pointsLog.getShardingValue();
            BigInteger finalId = (new BigInteger(pointsLogIdStr)).mod(BigInteger.valueOf(Long.MAX_VALUE));
            pointsLog.setPointsLogId(finalId.longValueExact());
        }

        pointsLog.setBusinessId(BusinessContextHolder.getBusinessId());
        pointsLog.setCreateTime(new Date());
        pointsLog.setUpdateTime(new Date());
        pointsLogMapper.insert(pointsLog);

        log.info("[grantPointsReward] 积分发放成功, memberId={}, points={}, activityId={}",
                memberId, rewardPoints, activityId);
    }

    /**
     * 发放优惠券（参考LotteryMobileServiceImpl.updateData case 1）
     * 1. 查优惠券模板(GoodCouponDO)
     * 2. 通过buildUserCoupon构建UserCouponDO（内部解析有效期）
     * 3. 插入user_coupon表
     */
    private void grantCouponReward(Long activityId, Long memberId, Long memberMobile, Long storeId,
                                   ActivityVoteRewardDO reward, WxMemberDTO wxMember) {
        GoodCouponDO goodCouponDO = goodCouponMapper.selectById(reward.getPrizeId());
        if (goodCouponDO == null) {
            log.warn("[grantCouponReward] 优惠券模板不存在, couponId={}", reward.getPrizeId());
            return;
        }

        // 会员信息由tryGrantReward预查后透传，预查失败/不存在时为null，保持原失败语义
        if (wxMember == null) {
            throw exception(new ErrorCode(1_004_001_000, "优惠券发放失败: 会员信息不存在, memberId=" + memberId));
        }

        GoodCouponRespVO goodCoupon = BeanUtils.toBean(goodCouponDO, GoodCouponRespVO.class);
        UserCouponDO coupon = buildUserCoupon(memberId, goodCoupon, wxMember,
                CouponSourceType.VOTE_REWARD.getCode(), storeId);
        userCouponMapper.insert(coupon);

        log.info("[grantCouponReward] 优惠券发放成功, memberId={}, couponId={}, activityId={}",
                memberId, reward.getPrizeId(), activityId);
    }

    private UserCouponDO buildUserCoupon(Long userId, GoodCouponRespVO goodCoupon, WxMemberDTO wxMember,
                                                    Integer couponSource, Long storeId) {
        UserCouponDO userCoupon = new UserCouponDO();
        BeanUtil.copyProperties(goodCoupon, userCoupon);
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(goodCoupon.getId());
        userCoupon.setCouponType(Integer.valueOf(goodCoupon.getCouponType()));
        userCoupon.setId(null);
        userCoupon.setIsUsed(0);
        userCoupon.setUseTime(null);
        CouponTimeUtil.parseCouponTime(goodCoupon, userCoupon);
        userCoupon.setDistributionMethod(0L);
        userCoupon.setMemberMobile(wxMember.getMemberMobile());
        userCoupon.setMemberName(wxMember.getMemberNickName());
        userCoupon.setCouponSource(couponSource);
        userCoupon.setStoreId(storeId);
        userCoupon.setDeleted(false);
        userCoupon.setCouponUseTime(goodCoupon.getUseTime());
        userCoupon.setCreateTime(goodCoupon.getCreateTime());
        userCoupon.setCouponCreateTime(LocalDateTime.now());
        userCoupon.setBusinessId(goodCoupon.getBusinessId());
        return userCoupon;
    }

    /**
     * 发放优惠券包（参考SurveyAnswerServiceImpl.grantCouponPackageReward）
     * 直接调用CouponPackageService.claimCouponPackage
     */
    private void grantCouponPackageReward(Long activityId, Long memberId, Long memberMobile, Long storeId,
                                          ActivityVoteRewardDO reward, WxMemberDTO wxMember) {
        // 会员信息由tryGrantReward预查后透传，预查失败/不存在时为null，保持原失败语义
        if (wxMember == null) {
            throw exception(new ErrorCode(1_004_001_000, "优惠券包发放失败: 会员信息不存在, memberId=" + memberId));
        }

        couponPackageService.claimCouponPackage(wxMember, reward.getPrizeId(),
                CouponSourceType.VOTE_REWARD.getCode(), null, storeId);

        log.info("[grantCouponPackageReward] 优惠券包发放成功, memberId={}, packageId={}, activityId={}",
                memberId, reward.getPrizeId(), activityId);
    }

    /**
     * 获取会员信息DTO（通过Dubbo RPC调用member模块）
     */
    private WxMemberDTO getWxMemberDTO(Long memberId) {
        CommonResult<WxMemberVO> result = wxMemberApi.getWxMemberById(memberId);
        if (result == null || result.getData() == null) {
            log.warn("[getWxMemberDTO] 会员信息不存在, memberId={}", memberId);
            return null;
        }
        return BeanUtils.toBean(result.getData(), WxMemberDTO.class);
    }

    // ==================== Redis 缓存读取（cache-aside + Redisson锁） ====================

    private ActivityVoteSaveOrUpdateReqVO getCachedVoteSetting(Long activityId) {
        String cacheKey = VoteKeyConstants.VOTE_SETTING + activityId;
        Object cached = redisCache.getCacheObject(cacheKey);
        if (cached instanceof ActivityVoteSaveOrUpdateReqVO voteVO) {
            return voteVO;
        }
        return loadVoteSettingWithLock(activityId);
    }

    private ActivityVoteSaveOrUpdateReqVO loadVoteSettingWithLock(Long activityId) {
        String lockKey = VoteKeyConstants.VOTE_LOCK_PREFIX + "setting:" + activityId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            String cacheKey = VoteKeyConstants.VOTE_SETTING + activityId;
            Object cached = redisCache.getCacheObject(cacheKey);
            if (cached instanceof ActivityVoteSaveOrUpdateReqVO voteVO) {
                return voteVO;
            }
            if (!locked) {
                throw exception(VOTE_NOT_EXISTS);
            }
            // 回源DB：读取基础活动表 + 投票扩展表，组装缓存VO
            ActivityDO activityDO = activityService.selectById(activityId);
            ActivityVoteDO voteDO = activityVoteMapper.selectOne(
                    new LambdaQueryWrapper<ActivityVoteDO>().eq(ActivityVoteDO::getActivityId, activityId));
            if (activityDO == null || voteDO == null) {
                return null;
            }
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
            // TTL = 活动结束时间 + 7天
            int ttlSeconds = calcCacheTtl(activityDO.getEndDate());
            redisCache.setCacheObject(cacheKey, cacheVO, ttlSeconds, TimeUnit.SECONDS);
            return cacheVO;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(VOTE_NOT_EXISTS);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<ActivityVoteOptionDO> getCachedOptions(Long activityId) {
        String cacheKey = VoteKeyConstants.VOTE_OPTIONS + activityId;
        Object cached = redisCache.getCacheObject(cacheKey);
        if (cached instanceof List<?>) {
            return (List<ActivityVoteOptionDO>) cached;
        }
        return loadOptionsWithLock(activityId);
    }

    @SuppressWarnings("unchecked")
    private List<ActivityVoteOptionDO> loadOptionsWithLock(Long activityId) {
        String lockKey = VoteKeyConstants.VOTE_LOCK_PREFIX + "options:" + activityId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            String cacheKey = VoteKeyConstants.VOTE_OPTIONS + activityId;
            Object cached = redisCache.getCacheObject(cacheKey);
            if (cached instanceof List<?>) {
                return (List<ActivityVoteOptionDO>) cached;
            }
            if (!locked) {
                return Collections.emptyList();
            }
            List<ActivityVoteOptionDO> options = activityVoteOptionMapper.selectList(
                    new LambdaQueryWrapper<ActivityVoteOptionDO>().eq(ActivityVoteOptionDO::getActivityId, activityId));
            List<ActivityVoteOptionDO> safeList = options == null ? new ArrayList<>() : options;
            // TTL = 活动结束时间 + 7天
            ActivityDO act = activityService.selectById(activityId);
            int ttl = calcCacheTtl(act != null ? act.getEndDate() : null);
            redisCache.setCacheObject(cacheKey, safeList, ttl, TimeUnit.SECONDS);
            return safeList;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 获取奖励配置缓存（cache-aside + Redisson锁）
     */
    @SuppressWarnings("unchecked")
    private List<ActivityVoteRewardDO> getCachedRewardConfig(Long activityId) {
        String cacheKey = VoteKeyConstants.VOTE_REWARD_CONFIG + activityId;
        Object cached = redisCache.getCacheObject(cacheKey);
        if (cached instanceof List<?>) {
            return (List<ActivityVoteRewardDO>) cached;
        }
        return loadRewardConfigWithLock(activityId);
    }

    @SuppressWarnings("unchecked")
    private List<ActivityVoteRewardDO> loadRewardConfigWithLock(Long activityId) {
        String lockKey = VoteKeyConstants.VOTE_LOCK_PREFIX + "reward:" + activityId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            String cacheKey = VoteKeyConstants.VOTE_REWARD_CONFIG + activityId;
            Object cached = redisCache.getCacheObject(cacheKey);
            if (cached instanceof List<?>) {
                return (List<ActivityVoteRewardDO>) cached;
            }
            if (!locked) {
                return Collections.emptyList();
            }
            List<ActivityVoteRewardDO> rewards = activityVoteRewardMapper.selectList(
                    new LambdaQueryWrapper<ActivityVoteRewardDO>().eq(ActivityVoteRewardDO::getActivityId, activityId));
            List<ActivityVoteRewardDO> safeList = rewards == null ? new ArrayList<>() : rewards;
            redisCache.setCacheObject(cacheKey, safeList);
            return safeList;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==================== Redis 计数器操作 ====================

    private Long getOptionVoteCount(Long activityId, Long optionId) {
        String key = VoteKeyConstants.VOTE_OPTION_COUNT + activityId + ":" + optionId;
        Object val = getRedisTemplate().opsForValue().get(key);
        if (val != null) {
            try {
                return Long.parseLong(val.toString());
            } catch (NumberFormatException e) {
                // 脏数据（旧JDK序列化二进制值），删除后从DB重建
                getRedisTemplate().delete(key);
            }
        }
        Long dbCount = activityVoteLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteLogDO>()
                        .eq(ActivityVoteLogDO::getActivityId, activityId)
                        .eq(ActivityVoteLogDO::getOptionId, optionId));
        getRedisTemplate().opsForValue().setIfAbsent(key, dbCount.toString());
        return dbCount;
    }

    private Long getOptionTotalCount(Long activityId) {
        List<ActivityVoteOptionDO> options = getCachedOptions(activityId);
        long total = 0;
        for (ActivityVoteOptionDO option : options) {
            total += getOptionVoteCount(activityId, option.getId());
        }
        return total;
    }

    private long getMobileTotalCount(Long activityId, Long memberMobile, long ttlSeconds) {
        String key = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
        Object val = getRedisTemplate().opsForValue().get(key);
        if (val != null) {
            try {
                return Long.parseLong(val.toString());
            } catch (NumberFormatException e) {
                // 脏数据（旧JDK序列化二进制值），删除后从DB重建
                getRedisTemplate().delete(key);
            }
        }
        Long dbCount = activityVoteLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteLogDO>()
                        .eq(ActivityVoteLogDO::getActivityId, activityId)
                        .eq(ActivityVoteLogDO::getMemberMobile, memberMobile));
        getRedisTemplate().opsForValue().set(key, dbCount.toString(), ttlSeconds, TimeUnit.SECONDS);
        return dbCount;
    }

    private long getMobileDailyCount(Long activityId, Long memberMobile) {
        String today = LocalDate.now().format(DAY_FMT);
        String key = VoteKeyConstants.VOTE_MEMBER_DAILY + activityId + ":" + memberMobile + ":" + today;
        Object val = getRedisTemplate().opsForValue().get(key);
        if (val == null) {
            return 0;
        }
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            // 脏数据兜底：删除后按0处理（当日计数由INCRBY重建）
            getRedisTemplate().delete(key);
            return 0;
        }
    }

    /**
     * 校验投票次数并原子递增（基于手机号）
     * 用getMobileTotalCount替代ensureTotalKeyInitialized，合并读取+初始化为单次get，减少Redis往返
     */
    private void validateAndIncrementVoteCount(ActivityVoteSaveOrUpdateReqVO voteVO, Long activityId, Long memberMobile) {
        Integer voteCountFlag = voteVO.getVoteCountFlag();
        // 会员级key的TTL：活动结束+7天缓冲，活动结束后自动清理，防止大量key永久残留
        long ttlSeconds = calcMemberKeyTtlSeconds(voteVO.getEndDate());
        if (voteCountFlag == null || voteCountFlag == 0) {
            // 不限制，仅递增总计数用于统计
            String totalKey = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
            getMobileTotalCount(activityId, memberMobile, ttlSeconds);
            getRedisTemplate().opsForValue().increment(totalKey, 1);
            return;
        }

        if (voteCountFlag == 1) {
            // 活动期间内每人每天可投票 voteCount 次
            String today = LocalDate.now().format(DAY_FMT);
            String dailyKey = VoteKeyConstants.VOTE_MEMBER_DAILY + activityId + ":" + memberMobile + ":" + today;
            Long current = getRedisTemplate().opsForValue().increment(dailyKey, 1);
            if (current != null && current > voteVO.getVoteCount()) {
                getRedisTemplate().opsForValue().increment(dailyKey, -1);
                throw exception(VOTE_COUNT_LIMIT);
            }
            getRedisTemplate().expire(dailyKey, 1, TimeUnit.DAYS);
            // 同时递增总计数
            String totalKey = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
            getMobileTotalCount(activityId, memberMobile, ttlSeconds);
            getRedisTemplate().opsForValue().increment(totalKey, 1);
        } else if (voteCountFlag == 2) {
            // 活动期间内每人最多可投票 voteCount 次
            String totalKey = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
            long currentTotal = getMobileTotalCount(activityId, memberMobile, ttlSeconds);
            if (currentTotal + 1 > voteVO.getVoteCount()) {
                throw exception(VOTE_COUNT_LIMIT);
            }
            getRedisTemplate().opsForValue().increment(totalKey, 1);
        }
    }

    /**
     * 计算会员级key的TTL：活动结束时间+7天缓冲；活动无结束时间时兜底30天
     */
    private long calcMemberKeyTtlSeconds(LocalDateTime endDate) {
        LocalDateTime expireAt = (endDate != null) ? endDate.plusDays(7) : LocalDateTime.now().plusDays(30);
        long seconds = Duration.between(LocalDateTime.now(), expireAt).getSeconds();
        return Math.max(seconds, 60);
    }

    /**
     * 回退投票次数计数（投票后续步骤异常时调用）
     */
    private void rollbackVoteCount(ActivityVoteSaveOrUpdateReqVO voteVO, Long activityId, Long memberMobile) {
        Integer voteCountFlag = voteVO.getVoteCountFlag();
        if (voteCountFlag == null || voteCountFlag == 0) {
            String totalKey = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
            getRedisTemplate().opsForValue().increment(totalKey, -1);
        } else if (voteCountFlag == 1) {
            String today = LocalDate.now().format(DAY_FMT);
            String dailyKey = VoteKeyConstants.VOTE_MEMBER_DAILY + activityId + ":" + memberMobile + ":" + today;
            getRedisTemplate().opsForValue().increment(dailyKey, -1);
            String totalKey = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
            getRedisTemplate().opsForValue().increment(totalKey, -1);
        } else if (voteCountFlag == 2) {
            String totalKey = VoteKeyConstants.VOTE_MEMBER_TOTAL + activityId + ":" + memberMobile;
            getRedisTemplate().opsForValue().increment(totalKey, -1);
        }
    }

    private void ensureOptionKeyInitialized(String optionKey, Long activityId, Long optionId) {
        String val = getRedisTemplate().opsForValue().get(optionKey);
        if (val != null) {
            try {
                Long.parseLong(val);
                return; // key存在且值合法
            } catch (NumberFormatException e) {
                getRedisTemplate().delete(optionKey);
            }
        }
        Long dbCount = activityVoteLogMapper.selectCount(
                new LambdaQueryWrapper<ActivityVoteLogDO>()
                        .eq(ActivityVoteLogDO::getActivityId, activityId)
                        .eq(ActivityVoteLogDO::getOptionId, optionId));
        getRedisTemplate().opsForValue().set(optionKey, dbCount.toString());
    }

    /**
     * 检查key的值是否为合法数字。旧JDK序列化写入的二进制值会导致INCRBY报
     * "value is not an integer"，此处检测到脏数据直接删除，由调用方从DB重建
     *
     * @return true=key存在脏数据（已删除）或key不存在, false=key值合法可安全INCRBY
     */
    private boolean isCorruptedNumericKey(String key) {
        String val = getRedisTemplate().opsForValue().get(key);
        if (val == null) {
            return true;
        }
        try {
            Long.parseLong(val);
            return false;
        } catch (NumberFormatException e) {
            getRedisTemplate().delete(key);
            return true;
        }
    }

    private Integer calcRemainFromRedis(ActivityVoteSaveOrUpdateReqVO voteVO, Long activityId, Long memberMobile) {
        Integer voteCountFlag = voteVO.getVoteCountFlag();
        if (voteCountFlag == null || voteCountFlag == 0) {
            return -1;
        }
        if (voteCountFlag == 1) {
            long dailyCount = getMobileDailyCount(activityId, memberMobile);
            return Math.max(0, voteVO.getVoteCount() - (int) dailyCount);
        } else {
            long totalCount = getMobileTotalCount(activityId, memberMobile, calcMemberKeyTtlSeconds(voteVO.getEndDate()));
            return Math.max(0, voteVO.getVoteCount() - (int) totalCount);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 校验门店（指定门店模式下用Redis Set判断，Set不存在时回源DB重建）
     */
    private void validateStore(ActivityVoteSaveOrUpdateReqVO voteVO, Long activityId, Long storeId) {
        if (!Objects.equals(voteVO.getActivityStore(), 0)) {
            return;
        }
        if (storeId == null) {
            throw exception(VOTE_NOT_STORE);
        }
        String storeKey = VoteKeyConstants.VOTE_STORE + activityId;
        // Set不存在时回源重建
        if (!Boolean.TRUE.equals(getRedisTemplate().hasKey(storeKey))) {
            rebuildStoreSet(activityId, storeKey);
        }
        Boolean isMember = getRedisTemplate().opsForSet().isMember(storeKey, String.valueOf(storeId));
        if (!Boolean.TRUE.equals(isMember)) {
            throw exception(VOTE_NOT_STORE);
        }
    }

    /**
     * 回源DB重建门店Set
     */
    private void rebuildStoreSet(Long activityId, String storeKey) {
        List<Long> storeIds = activityStoreService.selectStoreIdsByActivityId(activityId);
        if (CollUtil.isNotEmpty(storeIds)) {
            String[] members = storeIds.stream().map(String::valueOf).toArray(String[]::new);
            getRedisTemplate().opsForSet().add(storeKey, members);
            getRedisTemplate().expire(storeKey, 7, TimeUnit.DAYS);
        }
    }

    /**
     * 计算缓存TTL：活动结束时间往后延迟一周
     */
    private int calcCacheTtl(Date endDate) {
        if (endDate == null) {
            return VOTE_SETTING_CACHE_SECONDS;
        }
        long expireMillis = endDate.getTime() + 7 * 24 * 3600 * 1000L;
        long ttlSeconds = (expireMillis - System.currentTimeMillis()) / 1000;
        return (int) Math.max(ttlSeconds, 60);
    }

    private Integer getActivityStatus(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return 1;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startDate)) {
            return 1;
        } else if (now.isAfter(endDate)) {
            return 3;
        } else {
            return 2;
        }
    }

    /** Date → LocalDateTime（从ActivityDO读出到VO） */
    private LocalDateTime toLdt(Date d) {
        return d == null ? null : LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
    }
}
