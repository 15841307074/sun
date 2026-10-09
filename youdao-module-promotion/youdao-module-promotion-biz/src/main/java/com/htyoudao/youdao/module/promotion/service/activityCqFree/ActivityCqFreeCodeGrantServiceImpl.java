package com.htyoudao.youdao.module.promotion.service.activityCqFree;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqJoinDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityTaskDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCq.ActivityCqMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCqJoin.ActivityCqJoinMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLog.ActivityCqLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityTask.ActivityTaskMapper;
import com.htyoudao.youdao.module.promotion.enums.ActivityTaskTypeEnum;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class ActivityCqFreeCodeGrantServiceImpl implements ActivityCqFreeCodeGrantService {

    private static final String CQ_DAILY_FREE_JOB_LOCK_PREFIX = "lock:cq:free:job:";
    private static final int RESULT_STATUS_PENDING = 0;
    private static final int RESULT_STATUS_LOSE = 1;
    private static final int SIGN_CODE_LENGTH = 10;
    private static final int SIGN_CODE_RADIX = 36;
    private static final int TASK_LIMIT_DAILY_ONCE = 1;
    private static final int JOIN_PAGE_SIZE = 500;
    private static final int JOB_LOCK_LEASE_MILLIS = 30 * 60 * 1000;

    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityCqMapper activityCqMapper;
    @Resource
    private ActivityCqJoinMapper activityCqJoinMapper;
    @Resource
    private ActivityTaskMapper activityTaskMapper;
    @Resource
    private ActivityCqLogMapper activityCqLogMapper;
    @Resource
    private WxMemberApi wxMemberApi;
    @Resource
    private StoreApi storeApi;
    @Resource
    private IdentifierGenerator identifierGenerator;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public void executeDailyFreeCodeGrant() {
        LocalDate today = LocalDate.now();
        List<ActivityCqDO> candidateCqList = loadCandidateActivityCqList();
        log.info("抽签免费签码定时任务开始 date={}, cqActivityCount={}", today, candidateCqList.size());
        Map<Long, List<ActivityCqDO>> cqGroupByBusinessId = candidateCqList.stream()
                .filter(item -> item.getBusinessId() != null)
                .collect(Collectors.groupingBy(ActivityCqDO::getBusinessId));
        for (Map.Entry<Long, List<ActivityCqDO>> entry : cqGroupByBusinessId.entrySet()) {
            Long businessId = entry.getKey();
            List<ActivityCqDO> activityCqList = entry.getValue();
            if (activityCqList == null || activityCqList.isEmpty()) {
                continue;
            }
            handleBusinessActivityDailyFreeGrant(businessId, activityCqList, today);
        }
    }

    private List<ActivityCqDO> loadCandidateActivityCqList() {
        List<ActivityCqDO> activityCqList = activityCqMapper.selectList(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getFreeEvent, 1)
                .gt(ActivityCqDO::getFreeCount, 0)
                .ne(ActivityCqDO::getDrawStatus, 2)
                .and(wrapper -> wrapper.isNull(ActivityCqDO::getResultPublishTime)
                        .or()
                        .gt(ActivityCqDO::getResultPublishTime, LocalDateTime.now()))
                .orderByAsc(ActivityCqDO::getActivityId)
                .orderByDesc(ActivityCqDO::getId));
        Map<Long, ActivityCqDO> latestCqMap = new HashMap<>();
        for (ActivityCqDO activityCqDO : activityCqList) {
            if (activityCqDO == null || activityCqDO.getActivityId() == null) {
                continue;
            }
            latestCqMap.putIfAbsent(activityCqDO.getActivityId(), activityCqDO);
        }
        return latestCqMap.values().stream()
                .sorted(Comparator.comparing(ActivityCqDO::getActivityId))
                .collect(Collectors.toList());
    }

    private void handleBusinessActivityDailyFreeGrant(Long businessId, List<ActivityCqDO> activityCqList, LocalDate today) {
        if (businessId == null || activityCqList == null || activityCqList.isEmpty()) {
            return;
        }
        BusinessContextHolder.setBusinessId(businessId);
        try {
            List<Long> activityIds = activityCqList.stream()
                    .map(ActivityCqDO::getActivityId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (activityIds.isEmpty()) {
                return;
            }
            List<ActivityDO> activityList = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                    .in(ActivityDO::getId, activityIds)
                    .eq(ActivityDO::getActivityType, ActivityTypeEnum.CQ.getCode())
                    .eq(ActivityDO::getIsEnabled, 1)
                    .orderByAsc(ActivityDO::getId));
            Map<Long, ActivityDO> activityMap = activityList.stream()
                    .filter(item -> item.getId() != null)
                    .collect(Collectors.toMap(ActivityDO::getId, item -> item, (left, right) -> left));
            for (ActivityCqDO activityCqDO : activityCqList) {
                ActivityDO activityDO = activityMap.get(activityCqDO.getActivityId());
                if (activityDO == null) {
                    continue;
                }
                handleActivityDailyFreeGrant(activityDO, activityCqDO, today);
            }
        } finally {
            BusinessContextHolder.clear();
        }
    }

    private void handleActivityDailyFreeGrant(ActivityDO activityDO, ActivityCqDO activityCqDO, LocalDate today) {
        Long activityId = activityDO.getId();
        RLock lock = redissonClient.getLock(CQ_DAILY_FREE_JOB_LOCK_PREFIX + activityId + ":" + today);
        boolean locked = false;
        try {
            locked = lock.tryLock(100, JOB_LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);
            if (!locked) {
                log.info("抽签免费签码任务获取活动锁失败，跳过 activityId={}, date={}", activityId, today);
                return;
            }
            if (!isEligibleForDailyGrant(activityDO, activityCqDO, today)) {
                return;
            }
            long lastJoinId = 0L;
            int grantCount = 0;
            while (true) {
                List<ActivityCqJoinDO> joinList = activityCqJoinMapper.selectList(new LambdaQueryWrapperX<ActivityCqJoinDO>()
                        .eq(ActivityCqJoinDO::getActivityId, activityId)
                        .gt(ActivityCqJoinDO::getId, lastJoinId)
                        .orderByAsc(ActivityCqJoinDO::getId)
                        .last("limit " + JOIN_PAGE_SIZE));
                if (joinList == null || joinList.isEmpty()) {
                    break;
                }
                for (ActivityCqJoinDO joinDO : joinList) {
                    lastJoinId = joinDO.getId();
                    if (joinDO.getMemberId() == null) {
                        continue;
                    }
                    try {
                        if (grantDailyFreeCodeToMember(activityDO, activityCqDO, joinDO.getMemberId())) {
                            grantCount++;
                        }
                    } catch (Exception e) {
                        log.error("抽签免费签码发放失败 activityId={}, memberId={}", activityId, joinDO.getMemberId(), e);
                    }
                }
            }
            log.info("抽签免费签码任务完成 activityId={}, date={}, grantCount={}", activityId, today, grantCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("抽签免费签码任务中断 activityId={}, date={}", activityId, today, e);
        } catch (Exception e) {
            log.error("抽签免费签码任务执行失败 activityId={}, date={}", activityId, today, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean grantDailyFreeCodeToMember(ActivityDO activityDO, ActivityCqDO activityCqDO, Long memberId) {
        Boolean granted = transactionTemplate.execute(status -> {
            ActivityTaskDO taskDO = getOrInitTaskRecord(activityDO.getId(), memberId, ActivityTaskTypeEnum.FREE, TASK_LIMIT_DAILY_ONCE);
            if (defaultZero(taskDO.getFinishCount()) > 0) {
                return false;
            }
            int rewardCount = defaultZero(activityCqDO.getFreeCount());
            saveOrUpdateTaskRecord(activityDO.getId(), memberId, ActivityTaskTypeEnum.FREE, TASK_LIMIT_DAILY_ONCE, rewardCount);
            createCodeLogs(activityDO.getId(), memberId, null, activityCqDO, ActivityTaskTypeEnum.FREE.getCode(), rewardCount);
            return true;
        });
        return Boolean.TRUE.equals(granted);
    }

    private boolean isEligibleForDailyGrant(ActivityDO activityDO, ActivityCqDO activityCqDO, LocalDate today) {
        if (activityDO == null || activityCqDO == null) {
            return false;
        }
        if (!Objects.equals(activityDO.getIsEnabled(), 1)) {
            return false;
        }
        if (!Objects.equals(activityCqDO.getFreeEvent(), 1) || defaultZero(activityCqDO.getFreeCount()) <= 0) {
            return false;
        }
        if (Objects.equals(activityCqDO.getDrawStatus(), 2)) {
            return false;
        }
        if (activityCqDO.getResultPublishTime() != null && !activityCqDO.getResultPublishTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        return isActivityDateMatched(activityDO, today) && isActivityWeekMatched(activityDO, today);
    }

    private boolean isActivityDateMatched(ActivityDO activityDO, LocalDate today) {
        Date startDate = activityDO.getStartDate();
        if (startDate != null && today.isBefore(toLocalDate(startDate))) {
            return false;
        }
        Date endDate = activityDO.getEndDate();
        if (endDate != null && today.isAfter(toLocalDate(endDate))) {
            return false;
        }
        String dayNumbers = activityDO.getDayNumbers();
        if (dayNumbers == null || dayNumbers.isBlank()) {
            return true;
        }
        for (String value : dayNumbers.split(",")) {
            if (String.valueOf(today.getDayOfMonth()).equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isActivityWeekMatched(ActivityDO activityDO, LocalDate today) {
        String weekNumbers = activityDO.getWeekNumbers();
        if (weekNumbers == null || weekNumbers.isBlank()) {
            return true;
        }
        int dayOfWeek = mapWeekValue(today);
        for (String value : weekNumbers.split(",")) {
            if (String.valueOf(dayOfWeek).equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private int mapWeekValue(LocalDate date) {
        int isoDay = date.getDayOfWeek().getValue();
        return isoDay == 7 ? 1 : isoDay + 1;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private ActivityTaskDO getOrInitTaskRecord(Long activityId, Long memberId, ActivityTaskTypeEnum taskTypeEnum, Integer taskLimit) {
        ActivityTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO != null) {
            return taskDO;
        }
        ActivityTaskDO latestTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
        if (latestTaskDO == null) {
            return insertInitTaskRecord(activityId, memberId, taskTypeEnum);
        }
        latestTaskDO.setFinishCount(0);
        latestTaskDO.setGainCount(0);
        latestTaskDO.setConsumeCount(0);
        latestTaskDO.setUpdateTime(LocalDateTime.now());
        updateTaskRecord(latestTaskDO);
        return latestTaskDO;
    }

    private ActivityTaskDO insertInitTaskRecord(Long activityId, Long memberId, ActivityTaskTypeEnum taskTypeEnum) {
        ActivityTaskDO taskDO = new ActivityTaskDO();
        taskDO.setActivityId(activityId);
        taskDO.setMemberId(memberId);
        taskDO.setTaskType(taskTypeEnum.getCode());
        taskDO.setFinishCount(0);
        taskDO.setGainCount(0);
        taskDO.setConsumeCount(0);
        activityTaskMapper.insert(taskDO);
        return taskDO;
    }

    private ActivityTaskDO getTaskRecord(Long activityId, Long memberId, Integer taskType, Integer taskLimit) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        return activityTaskMapper.selectOne(new LambdaQueryWrapperX<ActivityTaskDO>()
                .eq(ActivityTaskDO::getActivityId, activityId)
                .eq(ActivityTaskDO::getMemberId, memberId)
                .eq(ActivityTaskDO::getTaskType, taskType)
                .ge(ActivityTaskDO::getUpdateTime, todayStart)
                .lt(ActivityTaskDO::getUpdateTime, tomorrowStart)
                .orderByDesc(ActivityTaskDO::getUpdateTime)
                .orderByDesc(ActivityTaskDO::getCreateTime)
                .orderByDesc(ActivityTaskDO::getId)
                .last("limit 1"));
    }

    private ActivityTaskDO getLatestTaskRecord(Long activityId, Long memberId, Integer taskType) {
        return activityTaskMapper.selectOne(new LambdaQueryWrapperX<ActivityTaskDO>()
                .eq(ActivityTaskDO::getActivityId, activityId)
                .eq(ActivityTaskDO::getMemberId, memberId)
                .eq(ActivityTaskDO::getTaskType, taskType)
                .orderByDesc(ActivityTaskDO::getUpdateTime)
                .orderByDesc(ActivityTaskDO::getCreateTime)
                .orderByDesc(ActivityTaskDO::getId)
                .last("limit 1"));
    }

    private void saveOrUpdateTaskRecord(Long activityId, Long memberId, ActivityTaskTypeEnum taskTypeEnum, Integer taskLimit, int rewardCount) {
        ActivityTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO == null) {
            ActivityTaskDO latestTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
            if (latestTaskDO == null) {
                insertFirstTaskRecord(activityId, memberId, taskTypeEnum, rewardCount);
                return;
            }
            latestTaskDO.setFinishCount(1);
            latestTaskDO.setGainCount(rewardCount);
            latestTaskDO.setConsumeCount(0);
            latestTaskDO.setUpdateTime(LocalDateTime.now());
            updateTaskRecord(latestTaskDO);
            return;
        }
        if (defaultZero(taskDO.getFinishCount()) >= TASK_LIMIT_DAILY_ONCE) {
            return;
        }
        taskDO.setFinishCount(defaultZero(taskDO.getFinishCount()) + 1);
        taskDO.setGainCount(defaultZero(taskDO.getGainCount()) + rewardCount);
        taskDO.setConsumeCount(defaultZero(taskDO.getConsumeCount()));
        updateTaskRecord(taskDO);
    }

    private void insertFirstTaskRecord(Long activityId, Long memberId, ActivityTaskTypeEnum taskTypeEnum, int rewardCount) {
        ActivityTaskDO taskDO = new ActivityTaskDO();
        taskDO.setActivityId(activityId);
        taskDO.setMemberId(memberId);
        taskDO.setTaskType(taskTypeEnum.getCode());
        taskDO.setFinishCount(1);
        taskDO.setGainCount(rewardCount);
        taskDO.setConsumeCount(0);
        activityTaskMapper.insert(taskDO);
    }

    private void updateTaskRecord(ActivityTaskDO taskDO) {
        UpdateWrapper<ActivityTaskDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", taskDO.getId())
                .eq("member_id", taskDO.getMemberId())
                .set("finish_count", taskDO.getFinishCount())
                .set("gain_count", taskDO.getGainCount())
                .set("consume_count", taskDO.getConsumeCount())
                .set("update_time", taskDO.getUpdateTime() == null ? LocalDateTime.now() : taskDO.getUpdateTime());
        activityTaskMapper.update(null, updateWrapper);
    }

    private List<String> createCodeLogs(Long activityId, Long memberId, Long storeId, ActivityCqDO activityCqDO, Integer obtainType, int count) {
        if (activityId == null || memberId == null || count <= 0) {
            return new ArrayList<>();
        }
        MemberSnapshot memberSnapshot = getMemberSnapshot(memberId);
        StoreSnapshot storeSnapshot = resolveStoreSnapshot(activityId, memberId, storeId);
        List<String> signCodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ActivityCqLogDO logDO = buildCodeLog(activityId, memberId, storeSnapshot, activityCqDO, obtainType, memberSnapshot);
            activityCqLogMapper.insert(logDO);
            signCodes.add(logDO.getSignCode());
        }
        return signCodes;
    }

    private ActivityCqLogDO buildCodeLog(Long activityId, Long memberId, StoreSnapshot storeSnapshot,
                                         ActivityCqDO activityCqDO, Integer obtainType, MemberSnapshot memberSnapshot) {
        ActivityCqLogDO logDO = new ActivityCqLogDO();
        logDO.setActivityId(activityId);
        logDO.setSignCode(generateSignCode());
        logDO.setMemberId(memberId);
        logDO.setMemberName(memberSnapshot == null ? null : memberSnapshot.getMemberName());
        logDO.setMemberMobile(memberSnapshot == null ? null : memberSnapshot.getMemberMobile());
        logDO.setGender(memberSnapshot == null ? null : memberSnapshot.getGender());
        logDO.setMemberCategory(memberSnapshot == null ? null : memberSnapshot.getMemberCategory());
        logDO.setObtainType(obtainType);
        logDO.setStoreId(storeSnapshot == null ? null : storeSnapshot.getStoreId());
        logDO.setStoreName(storeSnapshot == null ? null : storeSnapshot.getStoreName());
        logDO.setDrawTime(LocalDateTime.now());
        logDO.setResultStatus(resolveInitialResultStatus(activityCqDO));
        logDO.setPrizeState(0);
        logDO.setBusinessId(BusinessContextHolder.getBusinessId());
        return logDO;
    }

    private StoreSnapshot resolveStoreSnapshot(Long activityId, Long memberId, Long storeId) {
        if (storeId != null) {
            StoreSnapshot storeSnapshot = new StoreSnapshot();
            storeSnapshot.setStoreId(storeId);
            storeSnapshot.setStoreName(resolveStoreName(storeId));
            return storeSnapshot;
        }
        ActivityCqLogDO latestStoreLog = activityCqLogMapper.selectOne(new LambdaQueryWrapperX<ActivityCqLogDO>()
                .eq(ActivityCqLogDO::getActivityId, activityId)
                .eq(ActivityCqLogDO::getMemberId, memberId)
                .isNotNull(ActivityCqLogDO::getStoreId)
                .orderByDesc(ActivityCqLogDO::getDrawTime)
                .orderByDesc(ActivityCqLogDO::getId)
                .last("limit 1"));
        if (latestStoreLog == null) {
            return null;
        }
        StoreSnapshot storeSnapshot = new StoreSnapshot();
        storeSnapshot.setStoreId(latestStoreLog.getStoreId());
        storeSnapshot.setStoreName(ObjectUtil.isNotEmpty(latestStoreLog.getStoreName())
                ? latestStoreLog.getStoreName() : resolveStoreName(latestStoreLog.getStoreId()));
        return storeSnapshot;
    }

    private String resolveStoreName(Long storeId) {
        if (storeId == null) {
            return null;
        }
        try {
            CommonResult<StoreDTO> result = storeApi.getStoreByStoreId(storeId);
            StoreDTO storeDTO = result != null && result.isSuccess() ? result.getData() : null;
            return storeDTO == null ? null : storeDTO.getStoreName();
        } catch (Exception e) {
            log.warn("抽签免费签码任务查询门店名称失败 storeId={}", storeId, e);
            return null;
        }
    }

    private Integer resolveInitialResultStatus(ActivityCqDO activityCqDO) {
        LocalDateTime resultPublishTime = activityCqDO == null ? null : activityCqDO.getResultPublishTime();
        return resultPublishTime == null || LocalDateTime.now().isBefore(resultPublishTime)
                ? RESULT_STATUS_PENDING : RESULT_STATUS_LOSE;
    }

    private String generateSignCode() {
        long rawId = identifierGenerator.nextId(null).longValue();
        String signCode = Long.toUnsignedString(rawId, SIGN_CODE_RADIX).toUpperCase();
        if (signCode.length() == SIGN_CODE_LENGTH) {
            return signCode;
        }
        if (signCode.length() > SIGN_CODE_LENGTH) {
            return signCode.substring(signCode.length() - SIGN_CODE_LENGTH);
        }
        return "0".repeat(SIGN_CODE_LENGTH - signCode.length()) + signCode;
    }

    private MemberSnapshot getMemberSnapshot(Long memberId) {
        MemberSnapshot snapshot = new MemberSnapshot();
        try {
            CommonResult<WxMemberDTO> result = wxMemberApi.getMemberById(memberId);
            WxMemberDTO memberDTO = result != null && result.isSuccess() ? result.getData() : null;
            if (memberDTO != null) {
                snapshot.setMemberName(ObjectUtil.isNotEmpty(memberDTO.getMemberNickName()) ? memberDTO.getMemberNickName() : memberDTO.getMemberName());
                snapshot.setMemberMobile(memberDTO.getMemberMobile());
                snapshot.setGender(memberDTO.getGender());
                snapshot.setMemberCategory(memberDTO.getMemberCategory());
            }
        } catch (Exception e) {
            log.warn("抽签免费签码任务查询会员信息失败 memberId={}", memberId, e);
        }
        return snapshot;
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    @Data
    private static class MemberSnapshot {
        private String memberName;
        private String memberMobile;
        private Integer gender;
        private Integer memberCategory;
    }

    @Data
    private static class StoreSnapshot {
        private Long storeId;
        private String storeName;
    }
}
