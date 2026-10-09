package com.htyoudao.youdao.module.promotion.service.activityCqDraw;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqPendingMemberDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCq.ActivityCqMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLog.ActivityCqLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityPrize.ActivityPrizeMapper;
import com.htyoudao.youdao.module.promotion.enums.ActivityCqPrizeTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.CouponSourceType;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
@RefreshScope
public class ActivityCqDrawServiceImpl implements ActivityCqDrawService {

    private static final int ALIPAY_MEMBER_CATEGORY = 1;
    private static final String CQ_DRAW_EXECUTE_LOCK_PREFIX = "lock:cq:draw:execute:";
    private static final int DRAW_STATUS_INIT = 0;
    private static final int DRAW_STATUS_DOING = 1;
    private static final int DRAW_STATUS_DONE = 2;
    private static final int RESULT_STATUS_PENDING = 0;
    private static final int RESULT_STATUS_LOSE = 1;
    private static final int RESULT_STATUS_WIN = 2;
    private static final int RED_PACKET_CLAIM_STATUS_PENDING = 1;
    private static final int PRIZE_STATE_ISSUED = 0;
    private static final int PRIZE_STATE_PENDING_ADDRESS = 1;
    private static final int DRAW_MEMBER_PAGE_SIZE = 2000;
    private static final int MAX_SAMPLE_TIMES = 100;

    @Resource
    private ActivityCqMapper activityCqMapper;
    @Resource
    private ActivityCqLogMapper activityCqLogMapper;
    @Resource
    private ActivityPrizeMapper activityPrizeMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private WxMemberApi wxMemberApi;
    @Resource
    private LotteryRedPacketService lotteryRedPacketService;
    @Resource
    private LotteryAddLogService lotteryAddLogService;
    @Resource
    private CouponPackageService couponPackageService;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Value("${activity.cq.alipayGrandPrizeExclude:0}")
    private Integer alipayGrandPrizeExclude;
    @Value("${activity.cq.grandPrizeMobileBlacklist:}")
    private String grandPrizeMobileBlacklist;

    @Override
    public void executeDueDraw() {
        // 定时扫描所有“已到开奖时间且仍是待开奖”的活动，逐个复用统一开奖入口执行。
        List<ActivityCqDO> dueActivityList = activityCqMapper.selectList(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getDrawStatus, DRAW_STATUS_INIT)
                .isNotNull(ActivityCqDO::getResultPublishTime)
                .le(ActivityCqDO::getResultPublishTime, LocalDateTime.now()));
        log.info("扫描到待开奖活动 count={}", dueActivityList == null ? 0 : dueActivityList.size());
        for (ActivityCqDO activityCqDO : dueActivityList) {
            log.info("准备执行到期开奖 activityId={}, cqId={}, resultPublishTime={}, drawStatus={}",
                    activityCqDO.getActivityId(), activityCqDO.getId(),
                    activityCqDO.getResultPublishTime(), activityCqDO.getDrawStatus());
            executeDraw(activityCqDO.getActivityId());
        }
    }

    @Override
    public void executeDraw(Long activityId) {
        if (activityId == null) {
            return;
        }
        // 后台手动开奖、延迟消息消费、定时扫描都会走到这里，真正的幂等控制统一收口在该方法。
        log.info("开奖入口开始 activityId={}", activityId);
        RLock lock = redissonClient.getLock(CQ_DRAW_EXECUTE_LOCK_PREFIX + activityId);
        boolean locked = false;
        try {
            // 先抢活动级分布式锁，避免同一活动被多台机器或多个入口同时开奖。
            locked = lock.tryLock(200, 600000, TimeUnit.MILLISECONDS);
            if (!locked) {
                log.info("开奖锁获取失败，跳过本次执行 activityId={}", activityId);
                return;
            }
            // 加锁后重新读活动最新状态，避免用旧数据继续执行。
            ActivityCqDO activityCqDO = activityCqMapper.selectOne(new LambdaQueryWrapperX<ActivityCqDO>()
                    .eq(ActivityCqDO::getActivityId, activityId)
                    .last("limit 1"));

            // 只有“待开奖”状态才允许继续，已开奖/开奖中都直接返回，保证重复触发幂等。
            if (activityCqDO == null || !Objects.equals(activityCqDO.getDrawStatus(), DRAW_STATUS_INIT)) {
                log.info("开奖前置校验未通过，跳过执行 activityId={}, exists={}, drawStatus={}",
                        activityId, activityCqDO != null, activityCqDO == null ? null : activityCqDO.getDrawStatus());
                return;
            }

            // 补齐业务上下文，后续调用会员、发奖等链路需要知道当前所属项目。
            BusinessContextHolder.setBusinessId(activityCqDO.getBusinessId());

            // 即使被人工提前调用，也必须再次校验开奖时间，防止活动未到公布时间就提前出结果。
            if (activityCqDO.getResultPublishTime() == null || activityCqDO.getResultPublishTime().isAfter(LocalDateTime.now())) {
                log.info("开奖时间未到，跳过执行 activityId={}, resultPublishTime={}, now={}",
                        activityId, activityCqDO.getResultPublishTime(), LocalDateTime.now());
                return;
            }
            // 先切到“开奖中”，避免同一活动被重复扫描后再次执行。
            updateDrawStatus(activityCqDO.getId(), DRAW_STATUS_DOING);
            log.info("开奖状态更新为进行中 activityId={}, cqId={}", activityId, activityCqDO.getId());
            try {
                doExecuteDraw(activityCqDO);
                updateDrawStatus(activityCqDO.getId(), DRAW_STATUS_DONE);
                log.info("开奖执行完成 activityId={}, cqId={}", activityId, activityCqDO.getId());
            } catch (Exception e) {
                log.error("抽签开奖失败 activityId={}", activityId, e);
                updateDrawStatus(activityCqDO.getId(), DRAW_STATUS_INIT);
                log.warn("开奖失败后状态回滚为待开奖 activityId={}, cqId={}", activityId, activityCqDO.getId());
                throw e;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("开奖锁释放 activityId={}", activityId);
            }
        }
    }

    private void updateDrawStatus(Long id, Integer drawStatus) {
        UpdateWrapper<ActivityCqDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("draw_status", drawStatus)
                .set("update_time", LocalDateTime.now());
        activityCqMapper.update(null, updateWrapper);
    }

    private void doExecuteDraw(ActivityCqDO activityCqDO) {
        Long activityId = activityCqDO.getActivityId();
        // 手机号为空的签码不具备中奖资格，开奖前直接结束，避免进入大奖或普通奖候选池。
        markPendingLogsWithoutMobileLose(activityId);
        // 一次性加载活动奖品配置，后续用户开奖都基于这份快照做候选筛选和兜底处理。
        List<ActivityPrizeDO> allPrizeList = activityPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityPrizeDO>()
                .eq(ActivityPrizeDO::getActivityId, activityId)
                .orderByAsc(ActivityPrizeDO::getId));
        log.info("加载开奖奖品 activityId={}, totalPrizeCount={}", activityId, allPrizeList == null ? 0 : allPrizeList.size());
        if (allPrizeList.isEmpty()) {
            log.warn("开奖未查询到奖品，待开奖记录全部置未中奖 activityId={}", activityId);
            markAllPendingLogsLose(activityId);
            return;
        }
        List<ActivityPrizeDO> grandPrizeList = allPrizeList.stream()
                .filter(item -> Objects.equals(item.getPrizeType(), ActivityCqPrizeTypeEnum.GRAND_PRIZE.getCode()))
                .collect(Collectors.toList());
        executeGrandPrizeRound(activityCqDO, grandPrizeList);
        List<ActivityPrizeDO> drawPrizeList = allPrizeList.stream()
                .filter(item -> !Objects.equals(item.getIsGuarantees(), 1))
                .filter(item -> !Objects.equals(item.getPrizeType(), ActivityCqPrizeTypeEnum.GRAND_PRIZE.getCode()))
                .collect(Collectors.toList());
        log.info("过滤概率抽签奖品 activityId={}, drawPrizeCount={}", activityId, drawPrizeList.size());
        int processedMemberCount = 0;
        while (true) {
            // 按“待开奖用户”分页读取，而不是按签码明细全量读取，避免大活动开奖时内存膨胀。
            List<ActivityCqPendingMemberDO> pendingMemberList = activityCqLogMapper.selectPendingMemberPage(
                    activityId, RESULT_STATUS_PENDING, 0, DRAW_MEMBER_PAGE_SIZE);
            if (pendingMemberList == null || pendingMemberList.isEmpty()) {
                log.info("开奖批次处理完成，已无待开奖用户 activityId={}", activityId);
                return;
            }
            log.info("处理开奖批次 activityId={}, pendingMemberCount={}", activityId, pendingMemberList.size());
            for (ActivityCqPendingMemberDO pendingMemberDO : pendingMemberList) {
                if (pendingMemberDO.getMemberId() == null) {
                    continue;
                }
                processedMemberCount++;
                // 按用户分页开奖，避免把全量签码一次性拉进内存。
                drawMember(activityId, pendingMemberDO.getMemberId(), defaultZero(pendingMemberDO.getSignCount()),
                        drawPrizeList, processedMemberCount);
            }
        }
    }

    private void executeGrandPrizeRound(ActivityCqDO activityCqDO, List<ActivityPrizeDO> grandPrizeList) {
        Long activityId = activityCqDO.getActivityId();
        List<GrandPrizeBucket> grandPrizeBucketList = buildGrandPrizeBucketList(grandPrizeList);
        int totalGrandPrizeCount = grandPrizeBucketList.stream().mapToInt(GrandPrizeBucket::getRemainingStock).sum();
        if (totalGrandPrizeCount <= 0) {
            log.info("活动无可发放大奖，跳过大奖轮次 activityId={}", activityId);
            return;
        }
        boolean excludeAlipayMember = isAlipayGrandPrizeExcludeEnabled();
        Set<String> excludedGrandPrizeMobiles = resolveGrandPrizeMobileBlacklist();
        List<ActivityCqPendingMemberDO> pendingMemberPool = loadPendingMemberPool(activityId, excludeAlipayMember);
        if (pendingMemberPool.isEmpty()) {
            log.info("大奖轮次提前结束，待开奖用户池为空 activityId={}, remainingGrandPrizeCount={}",
                    activityId, totalGrandPrizeCount);
            return;
        }
        log.info("开始执行大奖轮次 activityId={}, grandPrizeCount={}, grandPrizeTypeCount={}, excludeAlipayMember={}, mobileBlacklistCount={}, grandPrizeIds={}",
                activityId, totalGrandPrizeCount, grandPrizeBucketList.size(), excludeAlipayMember, excludedGrandPrizeMobiles.size(),
                grandPrizeBucketList.stream().map(item -> item.getPrizeDO().getId()).collect(Collectors.toList()));
        while (totalGrandPrizeCount > 0 && !pendingMemberPool.isEmpty()) {
            int memberIndex = ThreadLocalRandom.current().nextInt(pendingMemberPool.size());
            ActivityCqPendingMemberDO pendingMemberDO = pendingMemberPool.get(memberIndex);
            if (pendingMemberDO == null || pendingMemberDO.getMemberId() == null) {
                log.warn("大奖轮次用户池数据异常，移除后继续 activityId={}, memberIndex={}, pendingMemberPoolSize={}",
                        activityId, memberIndex, pendingMemberPool.size());
                removeMemberFromPool(pendingMemberPool, memberIndex);
                continue;
            }
            GrandPrizeBucket selectedBucket = randomGrandPrizeBucket(grandPrizeBucketList);
            if (selectedBucket == null) {
                log.info("大奖轮次提前结束，已无可用大奖库存 activityId={}", activityId);
                return;
            }
            log.info("大奖轮次抽取结果 activityId={}, memberId={}, signCount={}, prizeId={}, prizeType={}, prizeName={}, pendingMemberCount={}, memberOffset={}, prizeRemainingStock={}",
                    activityId, pendingMemberDO.getMemberId(), pendingMemberDO.getSignCount(),
                    selectedBucket.getPrizeDO().getId(), selectedBucket.getPrizeDO().getPrizeType(),
                    selectedBucket.getPrizeDO().getPrizeName(), pendingMemberPool.size(), memberIndex,
                    selectedBucket.getRemainingStock());
            GrandPrizeDrawResult drawResult = drawGrandPrizeMember(activityId,
                    pendingMemberDO.getMemberId(),
                    defaultZero(pendingMemberDO.getSignCount()),
                    selectedBucket.getPrizeDO(),
                    excludeAlipayMember,
                    excludedGrandPrizeMobiles);
            if (drawResult == GrandPrizeDrawResult.SUCCESS) {
                selectedBucket.decreaseStock();
                totalGrandPrizeCount--;
                removeMemberFromPool(pendingMemberPool, memberIndex);
                log.info("大奖轮次发放成功 activityId={}, memberId={}, prizeId={}, prizeName={}, prizeRemainingStock={}, remainingGrandPrizeCount={}",
                        activityId, pendingMemberDO.getMemberId(), selectedBucket.getPrizeDO().getId(),
                        selectedBucket.getPrizeDO().getPrizeName(), selectedBucket.getRemainingStock(), totalGrandPrizeCount);
                continue;
            }
            if (drawResult == GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE) {
                removeMemberFromPool(pendingMemberPool, memberIndex);
                log.info("大奖轮次用户已从用户池剔除但未消耗大奖库存 activityId={}, memberId={}, prizeId={}, remainingMemberCount={}",
                        activityId, pendingMemberDO.getMemberId(), selectedBucket.getPrizeDO().getId(), pendingMemberPool.size());
                continue;
            }
            if (drawResult == GrandPrizeDrawResult.REMOVE_MEMBER_AND_PRIZE) {
                removeMemberFromPool(pendingMemberPool, memberIndex);
                selectedBucket.clearStock();
                totalGrandPrizeCount = grandPrizeBucketList.stream().mapToInt(GrandPrizeBucket::getRemainingStock).sum();
                log.warn("大奖轮次奖品占用失败，已移除用户和该大奖后继续 activityId={}, memberId={}, prizeId={}, remainingGrandPrizeCount={}, remainingMemberCount={}",
                        activityId, pendingMemberDO.getMemberId(), selectedBucket.getPrizeDO().getId(),
                        totalGrandPrizeCount, pendingMemberPool.size());
                continue;
            }
            log.info("大奖轮次本次未消耗大奖库存，保留后继续 activityId={}, memberId={}, prizeId={}, drawResult={}",
                    activityId, pendingMemberDO.getMemberId(), selectedBucket.getPrizeDO().getId(), drawResult);
        }
        log.info("大奖轮次执行完成 activityId={}, remainingGrandPrizeCount={}, remainingMemberCount={}",
                activityId, totalGrandPrizeCount, pendingMemberPool.size());
    }

    protected void drawMember(Long activityId, Long memberId, int signCount,
                              List<ActivityPrizeDO> drawPrizeList, int processedMemberCount) {
        if (memberId == null || signCount <= 0) {
            return;
        }
        // 开奖粒度控制在“每个用户最多中一次”，signCount 仅影响普通奖轮次的抽样次数。
        log.info("开始处理用户开奖 activityId={}, memberId={}, signCount={}, processedMemberCount={}",
                activityId, memberId, signCount, processedMemberCount);
        WxMemberVO wxMemberVO = getCqWxMemberOrNull(memberId);
        if (wxMemberVO == null) {
            // 会员基础信息缺失时无法安全发奖，直接把该用户剩余签码结束掉，避免开奖任务反复卡住。
            log.warn("抽签开奖跳过无会员数据用户 activityId={}, memberId={}, signCount={}",
                    activityId, memberId, signCount);
            markMemberPendingLogsLose(activityId, memberId, signCount, findGuaranteePrize(activityId));
            return;
        }
        ActivityPrizeDO guaranteePrize = findGuaranteePrize(activityId);
        ActivityCqLogDO winningLog = transactionTemplate.execute(status -> {
            // 用户可能有多张签码，但最终只保留一张作为“中奖/兜底结果承载记录”。
            // 一条 activity_cq_log 就是一张签码，最终只选其中一条承载中奖结果。
            int winningOffset = ThreadLocalRandom.current().nextInt(signCount);
            ActivityCqLogDO pendingWinningLog = activityCqLogMapper.selectPendingLogByOffset(
                    activityId, memberId, RESULT_STATUS_PENDING, winningOffset);
            if (pendingWinningLog == null) {
                log.info("用户已无待开奖签码 activityId={}, memberId={}", activityId, memberId);
                return null;
            }

            // 用户有多少张签码，就做多少次概率抽样，再从候选结果里挑“最优奖品”。
            ActivityPrizeDO finalPrize = resolveFinalPrize(signCount, drawPrizeList, processedMemberCount);
            // 会员类型会影响可发奖品，例如支付宝会员不能发微信红包，这里在真正落库前做最后修正。
            finalPrize = adjustPrizeForMember(activityId, wxMemberVO, finalPrize);

            // “未中奖”同样是有库存限制的奖品，只有占到库存后才落为命中；占用失败仍按既有规则走兜底。
            if (finalPrize != null && tryOccupyPrize(finalPrize.getId())) {
                if (!fillWinningLog(pendingWinningLog, finalPrize)) {
                    throw exception(LOTTERY_SYSTEM_AGAIN);
                }
                log.info("用户开奖命中奖品 activityId={}, memberId={}, logId={}, prizeId={}, prizeType={}, prizeName={}",
                        activityId, memberId, pendingWinningLog.getId(), finalPrize.getId(),
                        finalPrize.getPrizeType(), finalPrize.getPrizeName());
            } else {
                if (!fillGuaranteedResult(pendingWinningLog, guaranteePrize)) {
                    throw exception(LOTTERY_SYSTEM_AGAIN);
                }
                log.info("用户开奖命中兜底结果 activityId={}, memberId={}, logId={}, selectedPrizeId={}, guaranteePrizeId={}, guaranteePrizeType={}",
                        activityId, memberId, pendingWinningLog.getId(), finalPrize == null ? null : finalPrize.getId(),
                        guaranteePrize == null ? null : guaranteePrize.getId(),
                        guaranteePrize == null ? null : guaranteePrize.getPrizeType());
            }

            // 同一用户剩余待开奖签码全部置为未中奖，避免后续再次参与开奖。
            UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("activity_id", activityId)
                    .eq("member_id", memberId)
                    .eq("result_status", RESULT_STATUS_PENDING)
                    .ne("id", pendingWinningLog.getId())
                    .set("result_status", RESULT_STATUS_LOSE)
                    .set("update_time", LocalDateTime.now());
            int loseCount = activityCqLogMapper.update(null, updateWrapper);
            if (loseCount > 0) {
                log.info("同用户剩余签码批量置未中奖 activityId={}, memberId={}, affectedCount={}",
                        activityId, memberId, loseCount);
            }
            return pendingWinningLog;
        });

        if (winningLog == null) {
            return;
        }
        // 这里先完成开奖结果落库，再异步风格地继续发奖；即使发奖失败，也保留记录用于兜底或补偿。
        log.info("用户开奖落库完成，准备发奖 activityId={}, memberId={}, logId={}, resultStatus={}, prizeId={}, prizeType={}",
                activityId, memberId, winningLog.getId(), winningLog.getResultStatus(),
                winningLog.getPrizeId(), winningLog.getPrizeType());
        issuePrizeAfterDraw(winningLog);
    }

    private GrandPrizeDrawResult drawGrandPrizeMember(Long activityId, Long memberId, int signCount,
                                                      ActivityPrizeDO grandPrize, boolean excludeAlipayMember,
                                                      Set<String> excludedGrandPrizeMobiles) {
        if (memberId == null || signCount <= 0 || grandPrize == null) {
            log.warn("大奖开奖参数非法，跳过本次且不消耗大奖库存 activityId={}, memberId={}, signCount={}, prizeId={}",
                    activityId, memberId, signCount, grandPrize == null ? null : grandPrize.getId());
            return GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE;
        }
        log.info("开始处理用户大奖开奖 activityId={}, memberId={}, signCount={}, prizeId={}",
                activityId, memberId, signCount, grandPrize.getId());
        WxMemberVO wxMemberVO = getCqWxMemberOrNull(memberId);
        if (wxMemberVO == null) {
            log.warn("大奖开奖跳过无会员数据用户 activityId={}, memberId={}, signCount={}",
                    activityId, memberId, signCount);
            markMemberPendingLogsLose(activityId, memberId, signCount, findGuaranteePrize(activityId));
            return GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE;
        }
        if (isGrandPrizeMobileBlacklisted(wxMemberVO, excludedGrandPrizeMobiles)) {
            log.info("大奖轮次排除手机号黑名单用户 activityId={}, memberId={}, prizeId={}",
                    activityId, memberId, grandPrize.getId());
            return GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE;
        }
        if (excludeAlipayMember && isAlipayMember(wxMemberVO)) {
            log.info("大奖轮次排除支付宝用户 activityId={}, memberId={}, prizeId={}",
                    activityId, memberId, grandPrize.getId());
            return GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE;
        }
        GrandPrizeTransactionResult transactionResult = transactionTemplate.execute(status -> {
            int winningOffset = ThreadLocalRandom.current().nextInt(signCount);
            ActivityCqLogDO pendingWinningLog = activityCqLogMapper.selectPendingLogByOffset(
                    activityId, memberId, RESULT_STATUS_PENDING, winningOffset);
            if (pendingWinningLog == null) {
                log.info("用户已无待开奖签码，跳过大奖开奖 activityId={}, memberId={}", activityId, memberId);
                return GrandPrizeTransactionResult.removeMemberKeepPrize();
            }
            if (!tryOccupyPrize(grandPrize.getId())) {
                return GrandPrizeTransactionResult.removeMemberAndPrize();
            }
            if (!fillWinningLog(pendingWinningLog, grandPrize)) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("activity_id", activityId)
                    .eq("member_id", memberId)
                    .eq("result_status", RESULT_STATUS_PENDING)
                    .ne("id", pendingWinningLog.getId())
                    .set("result_status", RESULT_STATUS_LOSE)
                    .set("update_time", LocalDateTime.now());
            int loseCount = activityCqLogMapper.update(null, updateWrapper);
            if (loseCount > 0) {
                log.info("大奖命中后剔除用户剩余签码 activityId={}, memberId={}, affectedCount={}",
                        activityId, memberId, loseCount);
            }
            return GrandPrizeTransactionResult.success(pendingWinningLog);
        });
        if (transactionResult == null) {
            log.warn("大奖开奖事务未返回结果，保留大奖库存待重试 activityId={}, memberId={}, prizeId={}",
                    activityId, memberId, grandPrize.getId());
            return GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE;
        }
        if (transactionResult.getDrawResult() != GrandPrizeDrawResult.SUCCESS) {
            log.info("大奖开奖未命中成功结果 activityId={}, memberId={}, prizeId={}, drawResult={}",
                    activityId, memberId, grandPrize.getId(), transactionResult.getDrawResult());
            return transactionResult.getDrawResult();
        }
        ActivityCqLogDO winningLog = transactionResult.getWinningLog();
        log.info("用户大奖开奖落库完成，准备发奖 activityId={}, memberId={}, logId={}, prizeId={}, prizeType={}",
                activityId, memberId, winningLog.getId(), winningLog.getPrizeId(), winningLog.getPrizeType());
        issuePrizeAfterDraw(winningLog);
        return GrandPrizeDrawResult.SUCCESS;
    }

    private List<ActivityCqPendingMemberDO> loadPendingMemberPool(Long activityId, boolean excludeAlipayMember) {
        List<ActivityCqPendingMemberDO> pendingMemberPool = new ArrayList<>();
        int offset = 0;
        while (true) {
            List<ActivityCqPendingMemberDO> batchList = excludeAlipayMember
                    ? activityCqLogMapper.selectPendingMemberPageExcludeMemberCategory(
                    activityId, RESULT_STATUS_PENDING, ALIPAY_MEMBER_CATEGORY, offset, DRAW_MEMBER_PAGE_SIZE)
                    : activityCqLogMapper.selectPendingMemberPage(
                    activityId, RESULT_STATUS_PENDING, offset, DRAW_MEMBER_PAGE_SIZE);
            if (batchList == null || batchList.isEmpty()) {
                log.info("大奖轮次待开奖用户池加载完成 activityId={}, excludeAlipayMember={}, memberCount={}",
                        activityId, excludeAlipayMember, pendingMemberPool.size());
                return pendingMemberPool;
            }
            pendingMemberPool.addAll(batchList);
            log.info("大奖轮次待开奖用户池分批加载 activityId={}, excludeAlipayMember={}, batchSize={}, loadedCount={}",
                    activityId, excludeAlipayMember, batchList.size(), pendingMemberPool.size());
            if (batchList.size() < DRAW_MEMBER_PAGE_SIZE) {
                log.info("大奖轮次待开奖用户池加载完成 activityId={}, excludeAlipayMember={}, memberCount={}",
                        activityId, excludeAlipayMember, pendingMemberPool.size());
                return pendingMemberPool;
            }
            offset += batchList.size();
        }
    }

    private boolean isAlipayGrandPrizeExcludeEnabled() {
        return Objects.equals(alipayGrandPrizeExclude, 1);
    }

    private Set<String> resolveGrandPrizeMobileBlacklist() {
        if (grandPrizeMobileBlacklist == null || grandPrizeMobileBlacklist.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(grandPrizeMobileBlacklist.split("[,，;；\\r\\n]+"))
                .map(this::normalizeMobile)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private boolean isGrandPrizeMobileBlacklisted(WxMemberVO memberVO, Set<String> excludedGrandPrizeMobiles) {
        if (memberVO == null || excludedGrandPrizeMobiles == null || excludedGrandPrizeMobiles.isEmpty()) {
            return false;
        }
        String memberMobile = normalizeMobile(memberVO.getMemberMobile());
        return memberMobile != null && excludedGrandPrizeMobiles.contains(memberMobile);
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return null;
        }
        return mobile.trim();
    }

    private void removeMemberFromPool(List<ActivityCqPendingMemberDO> pendingMemberPool, int memberIndex) {
        int lastIndex = pendingMemberPool.size() - 1;
        if (memberIndex < 0 || memberIndex > lastIndex) {
            return;
        }
        if (memberIndex != lastIndex) {
            pendingMemberPool.set(memberIndex, pendingMemberPool.get(lastIndex));
        }
        pendingMemberPool.remove(lastIndex);
    }

    private ActivityPrizeDO adjustPrizeForMember(Long activityId, WxMemberVO wxMemberVO, ActivityPrizeDO finalPrize) {
        if (finalPrize == null || !isAlipayMember(wxMemberVO) || !isRedPacketPrize(finalPrize.getPrizeType())) {
            return finalPrize;
        }
        // 支付宝用户无法直接走微信红包能力，命中红包时要改派到非红包兜底奖。
        ActivityPrizeDO guaranteePrize = findGuaranteePrize(activityId);
        if (guaranteePrize == null || isRedPacketPrize(guaranteePrize.getPrizeType())) {
            log.warn("支付宝用户命中红包但未找到可用非红包兜底，按未中奖处理 activityId={}, memberId={}, prizeId={}",
                    activityId, wxMemberVO == null ? null : wxMemberVO.getMemberId(), finalPrize.getId());
            return null;
        }
        log.info("支付宝用户过滤红包奖品，改派兜底奖品 activityId={}, memberId={}, redPacketPrizeId={}, guaranteePrizeId={}, guaranteePrizeType={}",
                activityId, wxMemberVO.getMemberId(), finalPrize.getId(),
                guaranteePrize.getId(), guaranteePrize.getPrizeType());
        return guaranteePrize;
    }

    private ActivityPrizeDO resolveFinalPrize(int signCount, List<ActivityPrizeDO> drawPrizeList,
                                              int processedMemberCount) {
        // 真正参与本轮抽样的奖品必须同时满足：有库存、已解锁、配置了正概率。
        List<ActivityPrizeDO> availableNormalPrizeList = drawPrizeList.stream()
                .filter(this::hasPrizeStock)
                .filter(item -> isPrizeUnlockedForDraw(item, processedMemberCount))
                .filter(item -> item.getProbability() != null && item.getProbability().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (availableNormalPrizeList.isEmpty()) {
            return null;
        }
        List<ActivityPrizeDO> candidatePrizeList = new ArrayList<>();
        int sampleTimes = Math.min(signCount, MAX_SAMPLE_TIMES);
        // 用户签码越多，抽样次数越多，但设上限避免单用户签码过大导致开奖耗时失控。
        for (int i = 0; i < sampleTimes; i++) {
            ActivityPrizeDO prizeDO = randomPrize(availableNormalPrizeList);
            if (prizeDO != null) {
                candidatePrizeList.add(prizeDO);
            }
        }
        if (candidatePrizeList.isEmpty()) {
            return null;
        }
        // 每张签都对应一次抽样结果，最终从这些候选结果里再随机取 1 个，不再按类型或 prizeValue 比较。
        return candidatePrizeList.get(ThreadLocalRandom.current().nextInt(candidatePrizeList.size()));
    }

    private boolean isPrizeUnlockedForDraw(ActivityPrizeDO prizeDO, int processedMemberCount) {
        if (prizeDO == null) {
            return false;
        }
        // 保底奖不受解锁人数限制，其余奖品可通过 minimumNumber 控制在处理到指定人数后才开放。
        if (Objects.equals(prizeDO.getIsGuarantees(), 1)) {
            return true;
        }
        Integer minimumNumber = prizeDO.getMinimumNumber();
        if (minimumNumber == null || minimumNumber <= 0) {
            return true;
        }
        return processedMemberCount > minimumNumber;
    }

    private ActivityPrizeDO randomPrize(List<ActivityPrizeDO> prizeList) {
        // 标准权重抽样：按概率累计区间命中对应奖品。
        BigDecimal totalProbability = prizeList.stream()
                .map(ActivityPrizeDO::getProbability)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalProbability.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal randomValue = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(totalProbability.doubleValue()));
        BigDecimal current = BigDecimal.ZERO;
        for (ActivityPrizeDO prizeDO : prizeList) {
            current = current.add(prizeDO.getProbability());
            if (randomValue.compareTo(current) <= 0) {
                return prizeDO;
            }
        }
        return prizeList.get(prizeList.size() - 1);
    }

    private boolean tryOccupyPrize(Long prizeId) {
        UpdateWrapper<ActivityPrizeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", prizeId)
                // 通过库存条件更新控制并发超发，更新成功才算真正占到奖品。
                .apply("(prize_num IS NULL OR IFNULL(remain_num, 0) < prize_num)")
                .setSql("remain_num = IFNULL(remain_num, 0) + 1")
                .set("update_time", LocalDateTime.now());
        boolean success = activityPrizeMapper.update(null, updateWrapper) > 0;
        if (!success) {
            log.info("奖品占用失败，库存可能不足 prizeId={}", prizeId);
        }
        return success;
    }

    private boolean hasPrizeStock(ActivityPrizeDO prizeDO) {
        if (prizeDO == null) {
            return false;
        }
        Integer totalStock = prizeDO.getPrizeNum();
        Integer usedStock = prizeDO.getRemainNum() == null ? 0 : prizeDO.getRemainNum();
        return totalStock == null || usedStock < totalStock;
    }

    private boolean fillWinningLog(ActivityCqLogDO winningLog, ActivityPrizeDO prizeDO) {
        // 中奖结果先持久化到签码记录，后续发奖都围绕这条记录继续推进。
        Integer initialPrizeState = resolveInitialPrizeState(prizeDO.getPrizeType());
        UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", winningLog.getId())
                .eq("result_status", RESULT_STATUS_PENDING)
                .set("result_status", RESULT_STATUS_WIN)
                .set("prize_id", prizeDO.getId())
                .set("prize_type", prizeDO.getPrizeType())
                .set("prize_content", prizeDO.getPrizeName())
                .set("prize_img_url", prizeDO.getPrizeImgUrl())
                .set("prize_state", initialPrizeState)
                .set("update_time", LocalDateTime.now());
        int affected = activityCqLogMapper.update(null, updateWrapper);
        if (affected != 1) {
            log.warn("中奖记录更新失败 activityId={}, memberId={}, logId={}, prizeId={}, affected={}",
                    winningLog.getActivityId(), winningLog.getMemberId(), winningLog.getId(), prizeDO.getId(), affected);
            return false;
        }
        winningLog.setResultStatus(RESULT_STATUS_WIN);
        winningLog.setPrizeId(prizeDO.getId());
        winningLog.setPrizeType(prizeDO.getPrizeType());
        winningLog.setPrizeContent(prizeDO.getPrizeName());
        winningLog.setPrizeImgUrl(prizeDO.getPrizeImgUrl());
        winningLog.setPrizeState(initialPrizeState);
        return true;
    }

    private boolean fillLoseLog(ActivityCqLogDO logDO, ActivityPrizeDO guaranteePrize) {
        // 未中奖也会回填展示字段，便于前端统一展示“未中奖/谢谢参与”等兜底文案和图片。
        UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", logDO.getId())
                .eq("result_status", RESULT_STATUS_PENDING)
                .set("result_status", RESULT_STATUS_LOSE)
                .set("update_time", LocalDateTime.now());
        applyLosePrizeFields(updateWrapper, guaranteePrize);
        int affected = activityCqLogMapper.update(null, updateWrapper);
        if (affected != 1) {
            log.warn("未中奖记录更新失败 activityId={}, memberId={}, logId={}, guaranteePrizeId={}, affected={}",
                    logDO.getActivityId(), logDO.getMemberId(), logDO.getId(),
                    guaranteePrize == null ? null : guaranteePrize.getId(), affected);
            return false;
        }
        logDO.setResultStatus(RESULT_STATUS_LOSE);
        logDO.setPrizeId(guaranteePrize == null ? null : guaranteePrize.getId());
        logDO.setPrizeType(ActivityCqPrizeTypeEnum.NO_PRIZE.getCode());
        logDO.setPrizeContent(guaranteePrize == null ? "未中奖" : guaranteePrize.getPrizeName());
        logDO.setPrizeImgUrl(guaranteePrize == null ? null : guaranteePrize.getPrizeImgUrl());
        logDO.setPrizeState(0);
        return true;
    }

    private boolean fillGuaranteedResult(ActivityCqLogDO logDO, ActivityPrizeDO guaranteePrize) {
        if (guaranteePrize == null) {
            return fillLoseLog(logDO, null);
        }
        // 存在保底奖时，虽然业务上是兜底命中，但记录层仍按中奖结果处理，后续继续走正常发奖链路。
        return fillWinningLog(logDO, guaranteePrize);
    }

    private void markAllPendingLogsLose(Long activityId) {
        UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("activity_id", activityId)
                .eq("result_status", RESULT_STATUS_PENDING)
                .set("result_status", RESULT_STATUS_LOSE)
                .set("update_time", LocalDateTime.now());
        int affected = activityCqLogMapper.update(null, updateWrapper);
        log.info("活动待开奖记录全部置未中奖 activityId={}, affectedCount={}", activityId, affected);
    }

    private void markPendingLogsWithoutMobileLose(Long activityId) {
        UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("activity_id", activityId)
                .eq("result_status", RESULT_STATUS_PENDING)
                .and(wrapper -> wrapper.isNull("member_mobile")
                        .or()
                        .apply("TRIM(member_mobile) = ''"))
                .set("result_status", RESULT_STATUS_LOSE)
                .set("update_time", LocalDateTime.now());
        int affected = activityCqLogMapper.update(null, updateWrapper);
        if (affected > 0) {
            log.info("无手机号签码已过滤并置未中奖 activityId={}, affectedCount={}", activityId, affected);
        }
    }

    private void markMemberPendingLogsLose(Long activityId, Long memberId, int signCount, ActivityPrizeDO guaranteePrize) {
        transactionTemplate.executeWithoutResult(status -> {
            int winningOffset = ThreadLocalRandom.current().nextInt(signCount);
            ActivityCqLogDO pendingWinningLog = activityCqLogMapper.selectPendingLogByOffset(
                    activityId, memberId, RESULT_STATUS_PENDING, winningOffset);
            if (pendingWinningLog == null) {
                log.info("会员数据缺失且已无待开奖签码 activityId={}, memberId={}", activityId, memberId);
                return;
            }

            // 即使查不到用户，也只保留一条签码承载最终兜底结果，和正常开奖口径保持一致。
            if (!fillGuaranteedResult(pendingWinningLog, guaranteePrize)) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }

            UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("activity_id", activityId)
                    .eq("member_id", memberId)
                    .eq("result_status", RESULT_STATUS_PENDING)
                    .ne("id", pendingWinningLog.getId())
                    .set("result_status", RESULT_STATUS_LOSE)
                    .set("update_time", LocalDateTime.now());
            int affected = activityCqLogMapper.update(null, updateWrapper);
            log.info("会员数据缺失，用户剩余待开奖记录置未中奖 activityId={}, memberId={}, guaranteedLogId={}, affectedCount={}",
                    activityId, memberId, pendingWinningLog.getId(), affected);
        });
    }

    private void applyLosePrizeFields(UpdateWrapper<ActivityCqLogDO> updateWrapper, ActivityPrizeDO guaranteePrize) {
        updateWrapper.set("prize_id", guaranteePrize == null ? null : guaranteePrize.getId())
                .set("prize_type", ActivityCqPrizeTypeEnum.NO_PRIZE.getCode())
                .set("prize_content", guaranteePrize == null ? "未中奖" : guaranteePrize.getPrizeName())
                .set("prize_img_url", guaranteePrize == null ? null : guaranteePrize.getPrizeImgUrl())
                .set("prize_state", 0);
    }

    private void issuePrizeAfterDraw(ActivityCqLogDO winningLog) {
        // 奖开完后按照奖品类型分发到不同发奖通道；实物奖不在这里即时发，只等待用户填地址。
        log.info("开奖后发奖开始 activityId={}, logId={}, memberId={}, prizeId={}, prizeType={}",
                winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(),
                winningLog.getPrizeId(), winningLog.getPrizeType());

        if (Objects.equals(winningLog.getPrizeType(), ActivityCqPrizeTypeEnum.POINTS.getCode())) {
            sendPointsAfterDraw(winningLog);
            return;
        }
        if (Objects.equals(winningLog.getPrizeType(), ActivityCqPrizeTypeEnum.COUPON.getCode())) {
            sendCouponAfterDraw(winningLog);
            return;
        }
        if (Objects.equals(winningLog.getPrizeType(), ActivityCqPrizeTypeEnum.COUPON_PACKAGE.getCode())) {
            sendCouponPackageAfterDraw(winningLog);
            return;
        }
        if (Objects.equals(winningLog.getPrizeType(), ActivityCqPrizeTypeEnum.RED_PACKET.getCode())) {
            sendRedPacketAfterDraw(winningLog);
        }
    }

    private void sendPointsAfterDraw(ActivityCqLogDO winningLog) {
        try {
            // 积分奖直接累加会员积分，并补一条积分流水，成功后把 prize_state 更新为已发放。
            ActivityPrizeDO prizeDO = activityPrizeMapper.selectById(winningLog.getPrizeId());
            BigDecimal prizeValue = prizeDO == null ? null : prizeDO.getPrizeValue();
            if (prizeValue == null) {
                log.warn("抽签积分发放失败，未查询到奖品积分值，logId={}，prizeId={}",
                        winningLog.getId(), winningLog.getPrizeId());
                return;
            }
            int pointAmount = prizeValue.intValue();
            if (pointAmount <= 0) {
                log.warn("抽签积分发放失败，奖品积分值非法，logId={}，prizeId={}，prizeValue={}",
                        winningLog.getId(), winningLog.getPrizeId(), prizeValue);
                return;
            }

            WxMemberVO wxMemberVO = getCqWxMember(winningLog.getMemberId());
            int currentIntegral = wxMemberVO.getMemberIntegral() == null ? 0 : wxMemberVO.getMemberIntegral();
            int newIntegral = currentIntegral + pointAmount;
            wxMemberApi.updateMemberById(wxMemberVO.getMemberId(), newIntegral);
            addCqPointsLog(winningLog.getActivityId(), pointAmount, wxMemberVO);
            markPrizeIssued(winningLog);
            log.info("抽签积分发放成功 activityId={}, logId={}, memberId={}, pointAmount={}, newIntegral={}",
                    winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(), pointAmount, newIntegral);
        } catch (Exception e) {
            log.error("抽签积分发放异常，中奖记录保留待补偿，logId={}，memberId={}",
                    winningLog.getId(), winningLog.getMemberId(), e);
        }
    }

    private void sendCouponAfterDraw(ActivityCqLogDO winningLog) {
        try {
            // 优惠券奖通过复制券模板生成用户券；配置缺失或发放异常时，直接回退兜底，避免用户拿到脏中奖记录。
            ActivityPrizeDO prizeDO = activityPrizeMapper.selectById(winningLog.getPrizeId());
            if (prizeDO == null || prizeDO.getAwardId() == null) {
                log.warn("抽签优惠券发放失败，未查询到奖品配置或awardId，logId={}，prizeId={}",
                        winningLog.getId(), winningLog.getPrizeId());
                fallbackToGuaranteePrize(winningLog);
                return;
            }
            GoodCouponDO goodCouponDO = lotteryAddLogService.getGoodCoupon(prizeDO.getAwardId());
            if (goodCouponDO == null) {
                log.warn("抽签优惠券发放失败，未查询到优惠券配置，logId={}，awardId={}",
                        winningLog.getId(), prizeDO.getAwardId());
                fallbackToGuaranteePrize(winningLog);
                return;
            }

            UserCouponDO userCouponDO = BeanUtils.toBean(goodCouponDO, UserCouponDO.class);
            userCouponDO.setUserId(winningLog.getMemberId());
            userCouponDO.setCouponId(prizeDO.getAwardId());
            userCouponDO.setIsUsed(0);
            userCouponDO.setId(null);
            userCouponDO.setUseTime(null);
            parseUserCouponTime(goodCouponDO, userCouponDO);
            userCouponDO.setMemberMobile(winningLog.getMemberMobile());
            userCouponDO.setCouponSource(CouponSourceType.PRIZE_DRAW.getCode());
            lotteryAddLogService.addCoupon(userCouponDO);
            markPrizeIssued(winningLog);
            log.info("抽签优惠券发放成功 activityId={}, logId={}, memberId={}, prizeId={}, awardId={}",
                    winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(),
                    winningLog.getPrizeId(), prizeDO.getAwardId());
        } catch (Exception e) {
            log.error("抽签优惠券发放异常，走兜底，logId={}，prizeId={}",
                    winningLog.getId(), winningLog.getPrizeId(), e);
            fallbackToGuaranteePrize(winningLog);
        }
    }

    private void sendCouponPackageAfterDraw(ActivityCqLogDO winningLog) {
        try {
            // 优惠券包依赖外部发放接口，返回失败时回退兜底；网络类不确定异常则保留原记录待人工补偿。
            ActivityPrizeDO prizeDO = activityPrizeMapper.selectById(winningLog.getPrizeId());
            if (prizeDO == null || prizeDO.getAwardId() == null) {
                log.warn("抽签优惠券包发放失败，未查询到奖品配置或awardId，logId={}，prizeId={}",
                        winningLog.getId(), winningLog.getPrizeId());
                return;
            }

            WxMemberVO wxMemberVO = getValidCqWxMember(winningLog.getMemberId());
            WxMemberDTO wxMemberDTO = BeanUtils.toBean(wxMemberVO, WxMemberDTO.class);
            Integer marketId = winningLog.getActivityId() == null ? null : Math.toIntExact(winningLog.getActivityId());
            Boolean success = couponPackageService.claimCouponPackage(
                    wxMemberDTO,
                    prizeDO.getAwardId(),
                    CouponSourceType.DRAW_LOTS.getCode(),
                    marketId,
                    winningLog.getStoreId()
            );
            if (!Boolean.TRUE.equals(success)) {
                log.error("抽签优惠券包发放返回失败，走兜底，logId={}，packageId={}",
                        winningLog.getId(), prizeDO.getAwardId());
                fallbackToGuaranteePrize(winningLog);
            } else {
                markPrizeIssued(winningLog);
                log.info("抽签优惠券包发放成功 activityId={}, logId={}, memberId={}, prizeId={}, packageId={}",
                        winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(),
                        winningLog.getPrizeId(), prizeDO.getAwardId());
            }
        } catch (Exception e) {
            if (shouldFallbackOnCouponPackageException(e)) {
                log.error("抽签优惠券包发放明确失败，走兜底，logId={}，prizeId={}",
                        winningLog.getId(), winningLog.getPrizeId(), e);
                fallbackToGuaranteePrize(winningLog);
                return;
            }
            log.error("抽签优惠券包发放结果不确定，保留原中奖记录待补偿，logId={}，prizeId={}",
                    winningLog.getId(), winningLog.getPrizeId(), e);
        }
    }

    private void sendRedPacketAfterDraw(ActivityCqLogDO winningLog) {
        try {
            // 红包发放调用微信转账能力，成功后记录单号与领取状态，供后续查询/补偿使用。
            WxMemberVO wxMemberVO = getValidCqWxMember(winningLog.getMemberId());
            LotteryRedPacketVo redPacketVo = buildCqRedPacketVo(wxMemberVO, winningLog);

            log.info("抽签红包发放 redPacketVo={}", JSON.toJSONString(redPacketVo));
            TransferToUser.TransferToUserResponse response = lotteryRedPacketService.transferUser(redPacketVo);
            log.info("抽签红包发放 response={}", response);

            if (!isRedPacketTransferSuccess(response)) {
                log.warn("抽签红包发放失败，走兜底，logId={}，memberId={}",
                        winningLog.getId(), winningLog.getMemberId());
                fallbackToGuaranteePrize(winningLog);
                return;
            }

            UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", winningLog.getId())
                    .set("out_bill_no", response.getOutBillNo())
                    .set("package_info", response.getPackageInfo())
                    .set("claim_status", RED_PACKET_CLAIM_STATUS_PENDING)
                    .set("update_time", LocalDateTime.now());
            activityCqLogMapper.update(null, updateWrapper);
            winningLog.setOutBillNo(response.getOutBillNo());
            winningLog.setPackageInfo(response.getPackageInfo());
            winningLog.setClaimStatus(RED_PACKET_CLAIM_STATUS_PENDING);
            log.info("抽签红包发放成功 activityId={}, logId={}, memberId={}, outBillNo={}, claimStatus={}",
                    winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(),
                    response.getOutBillNo(), RED_PACKET_CLAIM_STATUS_PENDING);
        } catch (Exception e) {
            if (shouldFallbackOnRedPacketException(e)) {
                log.error("抽签红包发放明确失败，走兜底，logId={}，memberId={}",
                        winningLog.getId(), winningLog.getMemberId(), e);
                fallbackToGuaranteePrize(winningLog);
                return;
            }
            log.error("抽签红包发放结果不确定，保留原中奖记录待补偿，logId={}，memberId={}",
                    winningLog.getId(), winningLog.getMemberId(), e);
        }
    }

    /**
     * 红包异常分流：
     * 1. 网络超时/连接中断这类“不确定微信侧是否已受理”的异常，保留待补偿，避免重复发奖。
     * 2. 其余本地校验/构造/运行时异常视为明确失败，可直接回退兜底。
     */
    private boolean shouldFallbackOnRedPacketException(Throwable throwable) {
        return !isUncertainRedPacketException(throwable);
    }

    private boolean shouldFallbackOnCouponPackageException(Throwable throwable) {
        return !isUncertainCouponPackageException(throwable);
    }

    private boolean isUncertainRedPacketException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof TimeoutException
                    || current instanceof UncheckedIOException
                    || current instanceof IOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isUncertainCouponPackageException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof TimeoutException
                    || current instanceof UncheckedIOException
                    || current instanceof IOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void fallbackToGuaranteePrize(ActivityCqLogDO winningLog) {
        // 发奖阶段失败后，先回滚原奖品库存，再尝试切换到兜底奖；如果兜底也不可发，则最终落未中奖。
        ActivityPrizeDO currentPrize = winningLog.getPrizeId() == null ? null : activityPrizeMapper.selectById(winningLog.getPrizeId());
        boolean currentPrizeIsGuarantee = currentPrize != null && Objects.equals(currentPrize.getIsGuarantees(), 1);
        rollbackOccupiedPrize(currentPrize);

        ActivityPrizeDO guaranteePrize = findGuaranteePrize(winningLog.getActivityId());
        UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", winningLog.getId())
                .set("out_bill_no", null)
                .set("package_info", null)
                .set("claim_status", null)
                .set("update_time", LocalDateTime.now());

        if (currentPrizeIsGuarantee
                || guaranteePrize == null
                || Objects.equals(guaranteePrize.getPrizeType(), ActivityCqPrizeTypeEnum.RED_PACKET.getCode())) {
            // 为避免在异常场景里无限递归回退，这里明确把“无保底/保底仍不可发”的情况直接收敛成未中奖。
            // 注意：NO_PRIZE 兜底本身就是允许承载最终结果的一条签码记录，因此不在这里降级成 LOSE。
            updateWrapper.set("result_status", RESULT_STATUS_LOSE)
                    .set("prize_id", guaranteePrize == null ? null : guaranteePrize.getId())
                    .set("prize_type", ActivityCqPrizeTypeEnum.NO_PRIZE.getCode())
                    .set("prize_content", guaranteePrize == null ? "未中奖" : guaranteePrize.getPrizeName())
                    .set("prize_img_url", guaranteePrize == null ? null : guaranteePrize.getPrizeImgUrl())
                    .set("prize_state", 0);
            activityCqLogMapper.update(null, updateWrapper);
            winningLog.setResultStatus(RESULT_STATUS_LOSE);
            winningLog.setPrizeId(guaranteePrize == null ? null : guaranteePrize.getId());
            winningLog.setPrizeType(ActivityCqPrizeTypeEnum.NO_PRIZE.getCode());
            winningLog.setPrizeContent(guaranteePrize == null ? "未中奖" : guaranteePrize.getPrizeName());
            winningLog.setPrizeImgUrl(guaranteePrize == null ? null : guaranteePrize.getPrizeImgUrl());
            winningLog.setPrizeState(0);
            winningLog.setOutBillNo(null);
            winningLog.setPackageInfo(null);
            winningLog.setClaimStatus(null);
            log.info("奖品发放失败后回退未中奖 activityId={}, logId={}, memberId={}, currentPrizeIsGuarantee={}, guaranteePrizeId={}",
                    winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(),
                    currentPrizeIsGuarantee, guaranteePrize == null ? null : guaranteePrize.getId());
            return;
        }

        updateWrapper.set("result_status", RESULT_STATUS_WIN)
                .set("prize_id", guaranteePrize.getId())
                .set("prize_type", guaranteePrize.getPrizeType())
                .set("prize_content", guaranteePrize.getPrizeName())
                .set("prize_img_url", guaranteePrize.getPrizeImgUrl())
                .set("prize_state", 0);
        activityCqLogMapper.update(null, updateWrapper);
        winningLog.setResultStatus(RESULT_STATUS_WIN);
        winningLog.setPrizeId(guaranteePrize.getId());
        winningLog.setPrizeType(guaranteePrize.getPrizeType());
        winningLog.setPrizeContent(guaranteePrize.getPrizeName());
        winningLog.setPrizeImgUrl(guaranteePrize.getPrizeImgUrl());
        winningLog.setPrizeState(0);
        winningLog.setOutBillNo(null);
        winningLog.setPackageInfo(null);
        winningLog.setClaimStatus(null);
        log.info("奖品发放失败后回退兜底奖品（仅兜底一次） activityId={}, logId={}, memberId={}, guaranteePrizeId={}, guaranteePrizeType={}",
                winningLog.getActivityId(), winningLog.getId(), winningLog.getMemberId(),
                guaranteePrize.getId(), guaranteePrize.getPrizeType());
        issuePrizeAfterDraw(winningLog);
    }

    private void rollbackOccupiedPrize(ActivityPrizeDO prizeDO) {
        if (prizeDO == null) {
            return;
        }
        // 回退原中奖奖品的已占用数量，避免发奖失败后库存永久被锁死。
        UpdateWrapper<ActivityPrizeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", prizeDO.getId())
                .apply("IFNULL(remain_num, 0) > 0")
                .setSql("remain_num = IFNULL(remain_num, 0) - 1")
                .set("update_time", LocalDateTime.now());
        int affected = activityPrizeMapper.update(null, updateWrapper);
        log.info("回滚原中奖奖品占用 activityId={}, prizeId={}, affected={}",
                prizeDO.getActivityId(), prizeDO.getId(), affected);
    }

    private ActivityPrizeDO findGuaranteePrize(Long activityId) {
        if (activityId == null) {
            return null;
        }
        // 优先取显式配置的保底奖；若未配置，则退化到“未中奖”奖项作为最终兜底描述。
        List<ActivityPrizeDO> prizeList = activityPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityPrizeDO>()
                .eq(ActivityPrizeDO::getActivityId, activityId)
                .orderByAsc(ActivityPrizeDO::getId));
        if (prizeList == null || prizeList.isEmpty()) {
            return null;
        }
        ActivityPrizeDO guaranteePrize = prizeList.stream()
                .filter(Objects::nonNull)
                .filter(item -> Objects.equals(item.getIsGuarantees(), 1))
                .findFirst()
                .orElse(null);
        if (guaranteePrize != null) {
            return guaranteePrize;
        }
        return prizeList.stream()
                .filter(Objects::nonNull)
                .filter(item -> Objects.equals(item.getPrizeType(), ActivityCqPrizeTypeEnum.NO_PRIZE.getCode()))
                .findFirst()
                .orElse(null);
    }

    private WxMemberVO getCqWxMember(Long memberId) {
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
        if (memberResult == null || !memberResult.isSuccess() || memberResult.getData() == null) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        return memberResult.getData();
    }

    private WxMemberVO getCqWxMemberOrNull(Long memberId) {
        CommonResult<WxMemberVO> memberResult = wxMemberApi.getWxMemberById(memberId);
        if (memberResult == null || !memberResult.isSuccess() || memberResult.getData() == null) {
            return null;
        }
        return memberResult.getData();
    }

    private WxMemberVO getValidCqWxMember(Long memberId) {
        WxMemberVO wxMemberVO = getCqWxMember(memberId);
        if (wxMemberVO.getOpenid() == null || wxMemberVO.getOpenid().isBlank()) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        return wxMemberVO;
    }

    private boolean isAlipayMember(WxMemberVO wxMemberVO) {
        return wxMemberVO != null && Objects.equals(wxMemberVO.getMemberCategory(), ALIPAY_MEMBER_CATEGORY);
    }

    private void addCqPointsLog(Long activityId, int pointAmount, WxMemberVO wxMemberVO) {
        PointsLogDO pointsLogDO = new PointsLogDO();
        pointsLogDO.setMemberId(wxMemberVO.getMemberId());
        pointsLogDO.setPointsChange(Long.valueOf(pointAmount));
        pointsLogDO.setMemberName(wxMemberVO.getMemberName());
        pointsLogDO.setMemberNickName(wxMemberVO.getMemberNickName());
        pointsLogDO.setMemberMobile(wxMemberVO.getMemberMobile());
        pointsLogDO.setLogCode(generateCqNumber());
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

    private LotteryRedPacketVo buildCqRedPacketVo(WxMemberVO wxMemberVO, ActivityCqLogDO winningLog) {
        LotteryRedPacketVo redPacketVo = new LotteryRedPacketVo();
        redPacketVo.setOpenId(wxMemberVO.getOpenid());
        redPacketVo.setUserName(resolveMemberName(wxMemberVO));
        redPacketVo.setTransferAmount(resolveRedPacketAmountFen(winningLog));
        redPacketVo.setTransferRemark("抽签活动红包");
        redPacketVo.setUserRecvPerception("抽签活动红包奖励");
        redPacketVo.setActivityId(winningLog.getActivityId());
        return redPacketVo;
    }

    private int resolveRedPacketAmountFen(ActivityCqLogDO winningLog) {
        ActivityPrizeDO prizeDO = activityPrizeMapper.selectById(winningLog.getPrizeId());
        BigDecimal prizeValue = prizeDO == null ? null : prizeDO.getPrizeValue();
        if (prizeValue == null) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
        return prizeValue.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String resolveMemberName(WxMemberVO wxMemberVO) {
        if (wxMemberVO == null) {
            return null;
        }
        if (wxMemberVO.getMemberNickName() != null && !wxMemberVO.getMemberNickName().isBlank()) {
            return wxMemberVO.getMemberNickName();
        }
        return wxMemberVO.getMemberName();
    }

    private boolean isRedPacketTransferSuccess(TransferToUser.TransferToUserResponse response) {
        if (response == null || response.getCode() == null || response.getState() == null) {
            return false;
        }
        return Objects.equals(response.getCode(), 200)
                && (Objects.equals(response.getState(), TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM)
                || Objects.equals(response.getState(), TransferToUser.TransferBillStatus.SUCCESS));
    }

    private boolean isRedPacketPrize(Integer prizeType) {
        return Objects.equals(prizeType, ActivityCqPrizeTypeEnum.RED_PACKET.getCode());
    }

    private Integer resolveInitialPrizeState(Integer prizeType) {
        if (Objects.equals(prizeType, ActivityCqPrizeTypeEnum.PHYSICAL.getCode())
                || Objects.equals(prizeType, ActivityCqPrizeTypeEnum.GRAND_PRIZE.getCode())) {
            return PRIZE_STATE_PENDING_ADDRESS;
        }
        if (Objects.equals(prizeType, ActivityCqPrizeTypeEnum.RED_PACKET.getCode())) {
            return null;
        }
        return PRIZE_STATE_ISSUED;
    }

    private List<GrandPrizeBucket> buildGrandPrizeBucketList(List<ActivityPrizeDO> grandPrizeList) {
        if (grandPrizeList == null || grandPrizeList.isEmpty()) {
            log.info("未配置大奖奖品，返回空大奖奖池");
            return new ArrayList<>();
        }
        List<GrandPrizeBucket> bucketList = new ArrayList<>();
        for (ActivityPrizeDO grandPrize : grandPrizeList) {
            int remainingStock = resolveRemainingStock(grandPrize);
            if (remainingStock <= 0) {
                log.info("大奖奖品无剩余库存，跳过加入奖池 prizeId={}, prizeName={}, prizeNum={}, remainNum={}",
                        grandPrize.getId(), grandPrize.getPrizeName(), grandPrize.getPrizeNum(), grandPrize.getRemainNum());
                continue;
            }
            log.info("大奖奖品加入奖池 prizeId={}, prizeName={}, prizeType={}, remainingStock={}",
                    grandPrize.getId(), grandPrize.getPrizeName(), grandPrize.getPrizeType(), remainingStock);
            bucketList.add(new GrandPrizeBucket(grandPrize, remainingStock));
        }
        return bucketList;
    }

    private GrandPrizeBucket randomGrandPrizeBucket(List<GrandPrizeBucket> bucketList) {
        if (bucketList == null || bucketList.isEmpty()) {
            log.info("大奖奖池为空，无法随机抽取大奖");
            return null;
        }
        int totalRemainingStock = bucketList.stream().mapToInt(GrandPrizeBucket::getRemainingStock).sum();
        if (totalRemainingStock <= 0) {
            log.info("大奖奖池总库存为0，无法随机抽取大奖");
            return null;
        }
        int randomValue = ThreadLocalRandom.current().nextInt(totalRemainingStock);
        int current = 0;
        for (GrandPrizeBucket bucket : bucketList) {
            current += bucket.getRemainingStock();
            if (randomValue < current) {
                return bucket;
            }
        }
        return bucketList.get(bucketList.size() - 1);
    }

    private int resolveRemainingStock(ActivityPrizeDO prizeDO) {
        if (prizeDO == null || prizeDO.getPrizeNum() == null) {
            return 0;
        }
        return Math.max(defaultZero(prizeDO.getPrizeNum()) - defaultZero(prizeDO.getRemainNum()), 0);
    }

    private void markPrizeIssued(ActivityCqLogDO winningLog) {
        UpdateWrapper<ActivityCqLogDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", winningLog.getId())
                .set("prize_state", PRIZE_STATE_ISSUED)
                .set("update_time", LocalDateTime.now());
        activityCqLogMapper.update(null, updateWrapper);
        winningLog.setPrizeState(PRIZE_STATE_ISSUED);
    }

    private void parseUserCouponTime(GoodCouponDO goodCouponDO, UserCouponDO userCouponDO) {
        if (Objects.equals(goodCouponDO.getUseType(), 0)) {
            userCouponDO.setExpirationTime(goodCouponDO.getCouponEndTime());
            userCouponDO.setVaildStartTime(goodCouponDO.getCouponStartTime());
        }
        if (Objects.equals(goodCouponDO.getUseType(), 1)) {
            userCouponDO.setVaildStartTime(new Date());
            String endTime = DateUtils.localDateToString(
                    LocalDate.now().plusDays(Integer.parseInt(goodCouponDO.getUseTime()) - 1), DateUtils.YYYY_MM_DD);
            userCouponDO.setExpirationTime(
                    DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
        if (Objects.equals(goodCouponDO.getUseType(), 2)) {
            String[] split = goodCouponDO.getUseTime().split("#");
            String startTime = DateUtils.localDateToString(
                    LocalDate.now().plusDays(Integer.parseInt(split[0])), DateUtils.YYYY_MM_DD);
            userCouponDO.setVaildStartTime(
                    DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime + DateUtils.T_00_00_00));
            String endTime = DateUtils.localDateToString(
                    LocalDate.now().plusDays(Integer.parseInt(split[0])).plusDays(Integer.parseInt(split[1]) - 1),
                    DateUtils.YYYY_MM_DD);
            userCouponDO.setExpirationTime(
                    DateUtils.dateTime(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime + DateUtils.T_23_59_59));
        }
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String generateCqNumber() {
        return String.valueOf(System.currentTimeMillis());
    }

    private static class GrandPrizeBucket {

        private final ActivityPrizeDO prizeDO;
        private int remainingStock;

        private GrandPrizeBucket(ActivityPrizeDO prizeDO, int remainingStock) {
            this.prizeDO = prizeDO;
            this.remainingStock = remainingStock;
        }

        private ActivityPrizeDO getPrizeDO() {
            return prizeDO;
        }

        private int getRemainingStock() {
            return remainingStock;
        }

        private void decreaseStock() {
            if (remainingStock > 0) {
                remainingStock--;
            }
        }

        private void clearStock() {
            remainingStock = 0;
        }
    }

    private enum GrandPrizeDrawResult {
        SUCCESS,
        REMOVE_MEMBER_KEEP_PRIZE,
        REMOVE_MEMBER_AND_PRIZE
    }

    private static class GrandPrizeTransactionResult {

        private final GrandPrizeDrawResult drawResult;
        private final ActivityCqLogDO winningLog;

        private GrandPrizeTransactionResult(GrandPrizeDrawResult drawResult, ActivityCqLogDO winningLog) {
            this.drawResult = drawResult;
            this.winningLog = winningLog;
        }

        private static GrandPrizeTransactionResult success(ActivityCqLogDO winningLog) {
            return new GrandPrizeTransactionResult(GrandPrizeDrawResult.SUCCESS, winningLog);
        }

        private static GrandPrizeTransactionResult removeMemberKeepPrize() {
            return new GrandPrizeTransactionResult(GrandPrizeDrawResult.REMOVE_MEMBER_KEEP_PRIZE, null);
        }

        private static GrandPrizeTransactionResult removeMemberAndPrize() {
            return new GrandPrizeTransactionResult(GrandPrizeDrawResult.REMOVE_MEMBER_AND_PRIZE, null);
        }

        private GrandPrizeDrawResult getDrawResult() {
            return drawResult;
        }

        private ActivityCqLogDO getWinningLog() {
            return winningLog;
        }
    }
}
