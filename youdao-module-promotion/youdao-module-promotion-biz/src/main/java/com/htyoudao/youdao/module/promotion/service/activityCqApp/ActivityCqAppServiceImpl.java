package com.htyoudao.youdao.module.promotion.service.activityCqApp;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.member.api.wxmember.dto.WxMemberDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo.ActivityCqSaveOrUpdateReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqDetailVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqPrizeListVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.AddressSaveReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.DrawRecordVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.MyCodeVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.MyResultVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.PrizeVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.TaskVO;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.WinningRecordVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqJoinDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityHelpDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityTaskDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeExchangeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCq.ActivityCqMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCqJoin.ActivityCqJoinMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityHelp.ActivityHelpMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLog.ActivityCqLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityPrize.ActivityPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityTask.ActivityTaskMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.CQKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.ActivityTaskTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityAppService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class ActivityCqAppServiceImpl extends ServiceImpl<ActivityCqMapper, ActivityCqDO> implements ActivityCqAppService {

    private static final String CQ_DETAIL_LOCK_PREFIX = "lock:cq:detail:";
    private static final String CQ_LIST_LOCK_PREFIX = "lock:cq:list:";
    private static final String CQ_HELP_LOCK_PREFIX = "lock:cq:help:";
    private static final int TASK_LIMIT_UNLIMITED = 0;
    private static final int CQ_SETTING_CACHE_SECONDS = 300;
    private static final int CQ_LIST_CACHE_SECONDS = 300;
    private static final int RESULT_STATUS_PENDING = 0;
    private static final int RESULT_STATUS_LOSE = 1;
    private static final int RESULT_STATUS_WIN = 2;
    private static final int SIGN_CODE_LENGTH = 10;
    private static final int SIGN_CODE_RADIX = 36;

    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityStoreService activityStoreService;
    @Resource
    private ActivityPrizeMapper activityPrizeMapper;
    @Resource
    private ActivityTaskMapper activityTaskMapper;
    @Resource
    private ActivityCqJoinMapper activityCqJoinMapper;
    @Resource
    private ActivityHelpMapper activityHelpMapper;
    @Resource
    private ActivityCqLogMapper activityCqLogMapper;
    @Resource
    private WxMemberApi wxMemberApi;
    @Resource
    private StoreApi storeApi;
    @Resource
    private RedisCache redisCache;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ActivityAppService activityAppService;
    @Resource
    private IdentifierGenerator identifierGenerator;

    @Override
    public ActivityCqDetailVO getActivityCqDetail(Long activityId, Long storeId, Long memberId) {
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(activityId, null, false, false);
        Integer drawStatus = resolveLatestDrawStatus(activityId, cacheData);
        ActivityCqDetailVO detailVO = new ActivityCqDetailVO();
        detailVO.setActivityId(activityId);
        detailVO.setActivityTitle(cacheData.getActivityName());
        detailVO.setActivityCoverImage(cacheData.getActivityCoverImage());
        detailVO.setActivityBackgroundImage(cacheData.getActivityBackgroundImage());
        detailVO.setActivityDetailsImage(cacheData.getActivityDetailsImage());
        detailVO.setButtonBackgroundColor(cacheData.getButtonBackgroundColor());
        detailVO.setActivityStartTime(cacheData.getStartDate());
        detailVO.setActivityEndTime(cacheData.getEndDate());
        detailVO.setActivityRule(cacheData.getActivityRules());
        detailVO.setShareTitle(cacheData.getShareTitle());
        detailVO.setShareNote(cacheData.getShareNote());
        detailVO.setShareImgUrl(cacheData.getShareImgUrl());
        detailVO.setDailyAttendance(cacheData.getDailyAttendance());
        detailVO.setFreeEvent(cacheData.getFreeEvent());
        detailVO.setFreeCount(cacheData.getFreeCount());
        detailVO.setPlaceOrderStatus(cacheData.getPlaceOrderStatus());
        detailVO.setShareEvent(cacheData.getShareEvent());
        detailVO.setBrowseHomeEvent(cacheData.getBrowseHomeEvent());
        detailVO.setBrowseHomeCount(cacheData.getBrowseHomeCount());
        detailVO.setPaymentThreshold(cacheData.getPaymentCount());
        detailVO.setPlaceOrderCodeNumber(cacheData.getPlaceOrderCodeNumber());
        detailVO.setShareCount(cacheData.getShareCount());
        detailVO.setResultPublishTime(toLocalDateTime(cacheData.getResultPublishTime()));
        detailVO.setDrawStatus(drawStatus);
        detailVO.setActivityStatus(calculateActivityStatus(
                drawStatus,
                cacheData.getStartDate(),
                cacheData.getResultPublishTime()));
        detailVO.setJoined(hasJoined(activityId, memberId));
//        detailVO.setPrizeList(convertPrizeVOList(getPrizeConfigList(activityId)));
        return detailVO;
    }

    @Override
    public List<TaskVO> getTaskList(ActivityCqReqVO reqVO) {
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), false, false);
        List<TaskVO> taskList = new ArrayList<>(4);
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getDailyAttendance(), cacheData.getDailyAttenCount(), ActivityTaskTypeEnum.SIGN, BigDecimal.ZERO);
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getBrowseHomeEvent(), cacheData.getBrowseHomeCount(), ActivityTaskTypeEnum.BROWSE, BigDecimal.ZERO);
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getPlaceOrderStatus(), resolveOrderTaskLimit(cacheData), ActivityTaskTypeEnum.ORDER, resolveOrderTaskAmount(cacheData));
        buildTaskVO(taskList, reqVO.getActivityId(), reqVO.getMemberId(),
                cacheData.getShareEvent(), cacheData.getShareCount(), ActivityTaskTypeEnum.SHARE, BigDecimal.ZERO);
        return taskList;
    }

    @Override
    public Boolean join(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true, true);
        validateBeforePublish(reqVO.getActivityId());
        if (hasJoined(reqVO.getActivityId(), reqVO.getMemberId())) {
            return true;
        }
        ActivityCqJoinDO joinDO = new ActivityCqJoinDO();
        joinDO.setActivityId(reqVO.getActivityId());
        joinDO.setMemberId(reqVO.getMemberId());
        joinDO.setFirstJoinTime(LocalDateTime.now());
        joinDO.setBusinessId(BusinessContextHolder.getBusinessId());
        try {
            activityCqJoinMapper.insert(joinDO);
        } catch (DuplicateKeyException e) {
            return true;
        }
        grantFreeTaskOnJoin(reqVO.getActivityId(), reqVO.getStoreId(), reqVO.getMemberId());
        return true;
    }

    @Override
    public void validateJoined(Long activityId, Long memberId) {
        if (activityId == null || memberId == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        if (!hasJoined(activityId, memberId)) {
            throw exception(LOTTERY_NOT_JOIN);
        }
    }

    @Override
    public List<String> signTask(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        validateJoined(reqVO.getActivityId(), reqVO.getMemberId());
        validateBeforePublish(reqVO.getActivityId());
        int rewardCount = completeTask(reqVO, ActivityTaskTypeEnum.SIGN, 1);
        return createCodeLogs(reqVO.getActivityId(), reqVO.getMemberId(), reqVO.getStoreId(),
                ActivityTaskTypeEnum.SIGN.getCode(), rewardCount);
    }

    @Override
    public Boolean orderTask(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
//        validateJoined(reqVO.getActivityId(), reqVO.getMemberId());
        validateBeforePublish(reqVO.getActivityId());
        int rewardCount = completeTask(reqVO, ActivityTaskTypeEnum.ORDER, 1);
        createCodeLogs(reqVO.getActivityId(), reqVO.getMemberId(), reqVO.getStoreId(), ActivityTaskTypeEnum.ORDER.getCode(), rewardCount);
        return true;
    }

    @Override
    public List<String> simpleShareTask(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        validateJoined(reqVO.getActivityId(), reqVO.getMemberId());
        validateBeforePublish(reqVO.getActivityId());
        int rewardCount = completeTask(reqVO, ActivityTaskTypeEnum.SHARE, 1);
        return createCodeLogs(reqVO.getActivityId(), reqVO.getMemberId(), reqVO.getStoreId(),
                ActivityTaskTypeEnum.SHARE.getCode(), rewardCount);
    }

    @Override
    public List<String> browseHomeTask(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        validateJoined(reqVO.getActivityId(), reqVO.getMemberId());
        validateBeforePublish(reqVO.getActivityId());
        int rewardCount = completeTask(reqVO, ActivityTaskTypeEnum.BROWSE, 1);
        return createCodeLogs(reqVO.getActivityId(), reqVO.getMemberId(), reqVO.getStoreId(),
                ActivityTaskTypeEnum.BROWSE.getCode(), rewardCount);
    }

    @Override
    public Boolean shareTask(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null || reqVO.getInviterMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        validateJoined(reqVO.getActivityId(), reqVO.getInviterMemberId());
        validateBeforePublish(reqVO.getActivityId());
        int rewardCount = completeShareHelp(reqVO);
        createCodeLogs(reqVO.getActivityId(), reqVO.getInviterMemberId(), reqVO.getStoreId(), ActivityTaskTypeEnum.SHARE.getCode(), rewardCount);
        return true;
    }

    @Override
    public Boolean shareCheck(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null || reqVO.getInviterMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        validateJoined(reqVO.getActivityId(), reqVO.getInviterMemberId());
        validateBeforePublish(reqVO.getActivityId());
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true, false);
        if (!isTaskEnabled(cacheData.getShareEvent())) {
            throw exception(JK_TASK_NOT_ENABLED);
        }
        validateDailyHelpLimit(reqVO.getActivityId(), reqVO.getInviterMemberId(), reqVO.getMemberId(), LocalDate.now());
        return true;
    }

    @Override
    public ActivityCqPrizeListVO getPrizeList(ActivityCqReqVO reqVO) {
        getValidatedActivity(reqVO.getActivityId(), null, false, false);
        ActivityCqPrizeListVO respVO = new ActivityCqPrizeListVO();
        respVO.setPrizeList(convertPrizeVOList(getPrizeConfigList(reqVO.getActivityId())));
        return respVO;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public Boolean saveAddress(AddressSaveReqVO reqVO) {

        if (reqVO == null || reqVO.getId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }

        // 直接使用 UpdateWrapper，不更新分片键
        LambdaUpdateWrapper<ActivityCqLogDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ActivityCqLogDO::getId, reqVO.getId())
                .eq(ActivityCqLogDO::getMemberId, reqVO.getMemberId())
                .set(ActivityCqLogDO::getReceiveUser, reqVO.getMemberNickName())
                .set(ActivityCqLogDO::getReceiveMobile, reqVO.getMemberMobile())
                .set(ActivityCqLogDO::getReceiveAddress, reqVO.getReceiveAddress())
                .set(ActivityCqLogDO::getPrizeState, 2);

        // 根据 prizeType 决定是否更新 prizeState
        // 注意：这里需要先查询 prizeType，或者用另一种方式
        // 方案1A：先查询出 prizeType
//        ActivityCqLogDO exchangeDO = activityCqLogMapper.selectById(reqVO.getId());
//
//        if (exchangeDO != null && Objects.equals(exchangeDO.getPrizeType(), 3)) {
//            updateWrapper.set(ActivityCqLogDO::getPrizeState, 2);
//        }

        int count = activityCqLogMapper.update(null, updateWrapper);
        return count > 0;
    }

    @Override
    public PageResult<DrawRecordVO> getDrawRecord(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        PageResult<ActivityCqLogDO> pageResult = activityCqLogMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityCqLogDO>()
                .eq(ActivityCqLogDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityCqLogDO::getMemberId, reqVO.getMemberId())
                .orderByDesc(ActivityCqLogDO::getDrawTime)
                .orderByDesc(ActivityCqLogDO::getId));
        List<DrawRecordVO> list = pageResult.getList().stream().map(this::buildDrawRecordVO).collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public MyCodeVO getMyCodeInfo(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        List<ActivityCqLogDO> logList = activityCqLogMapper.selectList(new LambdaQueryWrapperX<ActivityCqLogDO>()
                .eq(ActivityCqLogDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityCqLogDO::getMemberId, reqVO.getMemberId())
                .orderByDesc(ActivityCqLogDO::getDrawTime)
                .orderByDesc(ActivityCqLogDO::getId));
        MyCodeVO myCodeVO = new MyCodeVO();
        myCodeVO.setTotalCount(logList.size());
        myCodeVO.setPendingCount((int) logList.stream().filter(item -> Objects.equals(item.getResultStatus(), RESULT_STATUS_PENDING)).count());
        myCodeVO.setLoseCount((int) logList.stream().filter(item -> Objects.equals(item.getResultStatus(), RESULT_STATUS_LOSE)).count());
        myCodeVO.setWinningCount((int) logList.stream().filter(item -> Objects.equals(item.getResultStatus(), RESULT_STATUS_WIN)).count());
        myCodeVO.setSignCount((int) logList.stream().filter(item -> Objects.equals(item.getObtainType(), ActivityTaskTypeEnum.SIGN.getCode())).count());
        myCodeVO.setOrderCount((int) logList.stream().filter(item -> Objects.equals(item.getObtainType(), ActivityTaskTypeEnum.ORDER.getCode())).count());
        myCodeVO.setShareCount((int) logList.stream().filter(item -> Objects.equals(item.getObtainType(), ActivityTaskTypeEnum.SHARE.getCode())).count());
        return myCodeVO;
    }

    @Override
    public PageResult<MyResultVO> getMyResultRecord(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        PageResult<ActivityCqLogDO> pageResult = activityCqLogMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityCqLogDO>()
                .eq(ActivityCqLogDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityCqLogDO::getMemberId, reqVO.getMemberId())
                .eq(ActivityCqLogDO::getResultStatus, RESULT_STATUS_WIN)
                .orderByDesc(ActivityCqLogDO::getDrawTime)
                .orderByDesc(ActivityCqLogDO::getId));
        List<MyResultVO> list = pageResult.getList().stream().map(this::buildMyResultVO).collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public PageResult<WinningRecordVO> getWinningRecord(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        reqVO.setPageSize(Math.min(defaultZero(reqVO.getPageSize()), 20));
        if (reqVO.getPageSize() <= 0) {
            reqVO.setPageSize(20);
        }
        PageResult<ActivityCqLogDO> pageResult = activityCqLogMapper.selectPage(reqVO, new LambdaQueryWrapperX<ActivityCqLogDO>()
                .eq(ActivityCqLogDO::getActivityId, reqVO.getActivityId())
                .eq(ActivityCqLogDO::getResultStatus, RESULT_STATUS_WIN)
                .isNotNull(ActivityCqLogDO::getMemberMobile)
                .ne(ActivityCqLogDO::getMemberMobile, "")
                .orderByDesc(ActivityCqLogDO::getDrawTime)
                .orderByDesc(ActivityCqLogDO::getId));
        List<WinningRecordVO> list = pageResult.getList().stream().map(this::buildWinningRecordVO).collect(Collectors.toList());
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public List<String> createCodeLogs(Long activityId, Long memberId, Long storeId, Integer obtainType, int count) {
        if (activityId == null || memberId == null || count <= 0) {
            return Collections.emptyList();
        }
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(activityId, storeId, false, false);
        MemberSnapshot memberSnapshot = getMemberSnapshot(memberId);
        List<String> signCodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ActivityCqLogDO logDO = buildCodeLog(activityId, memberId, storeId, cacheData, obtainType, memberSnapshot);
            activityCqLogMapper.insert(logDO);
            signCodes.add(logDO.getSignCode());
        }
        return signCodes;
    }

    private ActivityCqLogDO buildCodeLog(Long activityId, Long memberId, Long storeId,
                                         ActivityCqSaveOrUpdateReqVO cacheData, Integer obtainType, MemberSnapshot memberSnapshot) {
        ActivityCqLogDO logDO = new ActivityCqLogDO();
        logDO.setActivityId(activityId);
        logDO.setSignCode(generateSignCode(activityId, memberId));
        logDO.setMemberId(memberId);
        logDO.setMemberName(memberSnapshot == null ? null : memberSnapshot.getMemberName());
        logDO.setMemberMobile(memberSnapshot == null ? null : memberSnapshot.getMemberMobile());
        logDO.setGender(memberSnapshot == null ? null : memberSnapshot.getGender());
        logDO.setMemberCategory(memberSnapshot == null ? null : memberSnapshot.getMemberCategory());
        logDO.setObtainType(obtainType);
        logDO.setStoreId(storeId);
        logDO.setStoreName(resolveStoreName(storeId));
        logDO.setDrawTime(LocalDateTime.now());
        logDO.setResultStatus(resolveInitialResultStatus(cacheData));
        logDO.setPrizeState(0);
        logDO.setBusinessId(BusinessContextHolder.getBusinessId());
        return logDO;
    }

    private Integer resolveInitialResultStatus(ActivityCqSaveOrUpdateReqVO cacheData) {
        LocalDateTime resultPublishTime = toLocalDateTime(cacheData == null ? null : cacheData.getResultPublishTime());
        return cacheData == null || cacheData.getResultPublishTime() == null
                || LocalDateTime.now().isBefore(resultPublishTime) ? RESULT_STATUS_PENDING : RESULT_STATUS_LOSE;
    }

    private String generateSignCode(Long activityId, Long memberId) {
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

    private WxMemberVO getMember(Long memberId) {
        try {
            CommonResult<WxMemberVO> result = wxMemberApi.getWxMemberById(memberId);
            return result != null && result.isSuccess() ? result.getData() : null;
        } catch (Exception e) {
            log.warn("查询会员信息失败 memberId={}", memberId, e);
            return null;
        }
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
            log.warn("查询会员信息失败 memberId={}", memberId, e);
        }
        return snapshot;
    }

    private String resolveMemberName(WxMemberVO memberVO) {
        if (memberVO == null) {
            return null;
        }
        return ObjectUtil.isNotEmpty(memberVO.getMemberNickName()) ? memberVO.getMemberNickName() : memberVO.getMemberName();
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
            log.warn("查询门店名称失败 storeId={}", storeId, e);
            return null;
        }
    }

    private List<PrizeVO> convertPrizeVOList(List<ActivityPrizeDO> prizeList) {
        if (prizeList == null) {
            return Collections.emptyList();
        }
        return prizeList.stream().map(prizeDO -> {
            PrizeVO prizeVO = new PrizeVO();
            prizeVO.setId(prizeDO.getId());
            prizeVO.setPrizeType(prizeDO.getPrizeType());
            prizeVO.setPrizeName(prizeDO.getPrizeName());
            prizeVO.setPrizeImgUrl(prizeDO.getPrizeImgUrl());
            prizeVO.setPrizeValue(prizeDO.getPrizeValue());
            prizeVO.setTotalStock(defaultZero(prizeDO.getPrizeNum()));
            prizeVO.setUsedStock(defaultZero(prizeDO.getRemainNum()));
            prizeVO.setRemainStock(Math.max(defaultZero(prizeDO.getPrizeNum()) - defaultZero(prizeDO.getRemainNum()), 0));
            return prizeVO;
        }).collect(Collectors.toList());
    }

    private DrawRecordVO buildDrawRecordVO(ActivityCqLogDO logDO) {
        DrawRecordVO recordVO = new DrawRecordVO();
        recordVO.setId(logDO.getId());
        recordVO.setSignCode(logDO.getSignCode());
        recordVO.setObtainType(logDO.getObtainType());
        recordVO.setResultStatus(logDO.getResultStatus());
        recordVO.setPrizeId(logDO.getPrizeId());
        recordVO.setPrizeType(logDO.getPrizeType());
        recordVO.setPrizeContent(logDO.getPrizeContent());
        recordVO.setPrizeImgUrl(logDO.getPrizeImgUrl());
        recordVO.setPrizeState(logDO.getPrizeState());
        recordVO.setDrawTime(logDO.getDrawTime());
        return recordVO;
    }

    private MyResultVO buildMyResultVO(ActivityCqLogDO logDO) {
        MyResultVO resultVO = new MyResultVO();
        resultVO.setId(logDO.getId());
        resultVO.setSignCode(logDO.getSignCode());
        resultVO.setResultStatus(logDO.getResultStatus());
        resultVO.setPrizeId(logDO.getPrizeId());
        resultVO.setPrizeType(logDO.getPrizeType());
        resultVO.setPrizeContent(logDO.getPrizeContent());
        resultVO.setPrizeImgUrl(logDO.getPrizeImgUrl());
        resultVO.setPrizeState(logDO.getPrizeState());
        resultVO.setTrackingNumber(logDO.getTrackingNumber());
        resultVO.setPackageInfo(logDO.getPackageInfo());
        resultVO.setOutBillNo(logDO.getOutBillNo());
        resultVO.setClaimStatus(logDO.getClaimStatus());
        resultVO.setDrawTime(logDO.getDrawTime());
        return resultVO;
    }

    private WinningRecordVO buildWinningRecordVO(ActivityCqLogDO logDO) {
        WinningRecordVO recordVO = new WinningRecordVO();
        recordVO.setMemberMobile(logDO.getMemberMobile());
        recordVO.setPrizeContent(logDO.getPrizeContent());
        recordVO.setSignCode(logDO.getSignCode());
        return recordVO;
    }

    private ActivityCqSaveOrUpdateReqVO getValidatedActivity(Long activityId, Long storeId, boolean validateStore, boolean validateCommunity) {
        if (activityId == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        ActivityCqSaveOrUpdateReqVO cacheData = getCachedCqSetting(activityId);
        if (cacheData == null) {
            cacheData = loadCqSettingWithLock(activityId);
        }
        if (cacheData == null || !Objects.equals(cacheData.getIsEnabled(), 1)) {
            throw exception(LOTTERY_CQ_CLOSED);
        }
        if (!isCqActivityTimeValid(cacheData)) {
            throw exception(LOTTERY_CQ_CLOSED);
        }
        if (validateCommunity && !activityAppService.checkCanJoin(activityId)) {
            throw exception(LOTTERY_WECOMGROUP_ERROR);
        }
        if (validateStore) {
            validateStore(activityId, storeId);
        }
        return cacheData;
    }

    /**
     * 抽签活动不依赖 endDate 控制参与有效期，只校验开始时间和日历时段。
     * 最终截止时间统一由开奖时间 resultPublishTime 和 drawStatus 控制。
     */
    private boolean isCqActivityTimeValid(ActivityCqSaveOrUpdateReqVO cacheData) {
        if (cacheData == null) {
            return false;
        }
        return TimeValidationUtil.isTimeValid(
                cacheData.getStartDate(), cacheData.getEndDate(),
                convertListToString(cacheData.getDayNumberList()),
                convertListToString(cacheData.getWeekNumberList()),
                convertListToString(cacheData.getTimeRangeList()));
    }

    private void validateStore(Long activityId, Long storeId) {
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (activityDO == null || !Objects.equals(activityDO.getActivityStore(), 0)) {
            return;
        }
        if (storeId == null) {
            throw exception(LOTTERY_NOT_STORE);
        }
        boolean matched = activityStoreService.count(new LambdaQueryWrapperX<ActivityStoreDO>()
                .eq(ActivityStoreDO::getActivityId, activityId)
                .eq(ActivityStoreDO::getStoreId, storeId)) > 0;
        if (!matched) {
            throw exception(LOTTERY_NOT_STORE);
        }
    }

    private ActivityCqSaveOrUpdateReqVO getCachedCqSetting(Long activityId) {
        Object cachedObject = redisCache.getCacheObject(CQKeyConstants.CQ_SETTING + activityId);
        return cachedObject instanceof ActivityCqSaveOrUpdateReqVO reqVO ? reqVO : null;
    }

    private ActivityCqSaveOrUpdateReqVO loadCqSettingWithLock(Long activityId) {
        RLock lock = redissonClient.getLock(CQ_DETAIL_LOCK_PREFIX + activityId);
        boolean locked = false;
        try {
            locked = lock.tryLock(200, 3000, TimeUnit.MILLISECONDS);
            ActivityCqSaveOrUpdateReqVO cached = getCachedCqSetting(activityId);
            if (cached != null) {
                return cached;
            }
            if (!locked) {
                throw exception(LOTTERY_SYSTEM_AGAIN);
            }
            ActivityDO activityDO = activityMapper.selectById(activityId);
            if (activityDO == null
                    || !Objects.equals(activityDO.getActivityType(), ActivityTypeEnum.CQ.getCode())
                    || !Objects.equals(activityDO.getIsEnabled(), 1)) {
                return null;
            }
            Long businessId = BusinessContextHolder.getBusinessId();
            if (businessId != null && !Objects.equals(activityDO.getBusinessId(), businessId)) {
                return null;
            }
            ActivityCqDO activityCqDO = this.getOne(new LambdaQueryWrapperX<ActivityCqDO>()
                    .eq(ActivityCqDO::getActivityId, activityId)
                    .last("limit 1"));
            if (activityCqDO == null) {
                return null;
            }
            ActivityCqSaveOrUpdateReqVO reqVO = new ActivityCqSaveOrUpdateReqVO();
            BeanUtils.copyProperties(activityDO, reqVO);
            BeanUtils.copyProperties(activityCqDO, reqVO);
            reqVO.setId(activityCqDO.getId());
            reqVO.setActivityId(activityId);
            reqVO.setActivityName(activityDO.getActivityName());
            reqVO.setActivityRemark(activityDO.getActivityRemark());
            reqVO.setActivityRules(activityDO.getActivityRules());
            reqVO.setStartDate(resolveCqStartDate(activityDO, activityCqDO));
            reqVO.setEndDate(resolveCqEndDate(activityDO, activityCqDO));
            reqVO.setDayNumberList(parseIntegerList(activityDO.getDayNumbers()));
            reqVO.setWeekNumberList(parseIntegerList(activityDO.getWeekNumbers()));
            reqVO.setTimeRangeList(parseStringList(activityDO.getTimeRange()));
            redisCache.setCacheObject(CQKeyConstants.CQ_SETTING + activityId, reqVO, CQ_SETTING_CACHE_SECONDS, TimeUnit.SECONDS);
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

    private List<ActivityPrizeDO> getPrizeConfigList(Long activityId) {
        return getOrLoadListCache(
                CQKeyConstants.CQ_PRIZE + activityId,
                CQ_LIST_LOCK_PREFIX + "prize:" + activityId,
                () -> activityPrizeMapper.selectList(new LambdaQueryWrapperX<ActivityPrizeDO>()
                        .eq(ActivityPrizeDO::getActivityId, activityId)
                        .orderByAsc(ActivityPrizeDO::getId))
        );
    }

    private <T> List<T> getOrLoadListCache(String cacheKey, String lockKey, Supplier<List<T>> loader) {
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
            List<T> list = loader.get();
            List<T> safeList = list == null ? new ArrayList<>() : list;
            redisCache.setCacheObject(cacheKey, safeList, CQ_LIST_CACHE_SECONDS, TimeUnit.SECONDS);
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

    private void buildTaskVO(List<TaskVO> taskList, Long activityId, Long memberId, Integer enabledFlag,
                             Integer taskLimit, ActivityTaskTypeEnum taskTypeEnum, java.math.BigDecimal amount) {
        if (!isTaskEnabled(enabledFlag)) {
            return;
        }
        int limit = taskLimit == null ? TASK_LIMIT_UNLIMITED : taskLimit;
        ActivityTaskDO taskDO = getOrInitTaskRecord(activityId, memberId, taskTypeEnum, limit);
        TaskVO taskVO = new TaskVO();
        taskVO.setTaskType(taskTypeEnum.getCode());
        taskVO.setTaskLimit(limit);
        taskVO.setAmount(amount);
        taskVO.setFinishCount(defaultZero(taskDO.getFinishCount()));
        taskList.add(taskVO);
    }

    private ActivityTaskDO getOrInitTaskRecord(Long activityId, Long memberId, ActivityTaskTypeEnum taskTypeEnum, Integer taskLimit) {
        ActivityTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO != null) {
            return taskDO;
        }
        if (isUnlimitedLimit(taskLimit)) {
            return insertInitTaskRecord(activityId, memberId, taskTypeEnum);
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
        try {
            activityTaskMapper.insert(taskDO);
            return taskDO;
        } catch (DuplicateKeyException e) {
            ActivityTaskDO existsTaskDO = getLatestTaskRecord(activityId, memberId, taskTypeEnum.getCode());
            if (existsTaskDO != null) {
                return existsTaskDO;
            }
            throw e;
        }
    }

    private ActivityTaskDO getTaskRecord(Long activityId, Long memberId, Integer taskType, Integer taskLimit) {
        LambdaQueryWrapperX<ActivityTaskDO> queryWrapper = new LambdaQueryWrapperX<ActivityTaskDO>()
                .eq(ActivityTaskDO::getActivityId, activityId)
                .eq(ActivityTaskDO::getMemberId, memberId)
                .eq(ActivityTaskDO::getTaskType, taskType)
                .orderByDesc(ActivityTaskDO::getUpdateTime)
                .orderByDesc(ActivityTaskDO::getCreateTime)
                .orderByDesc(ActivityTaskDO::getId)
                .last("limit 1");
        if (!isUnlimitedLimit(taskLimit)) {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime tomorrowStart = todayStart.plusDays(1);
            queryWrapper.ge(ActivityTaskDO::getUpdateTime, todayStart)
                    .lt(ActivityTaskDO::getUpdateTime, tomorrowStart);
        }
        return activityTaskMapper.selectOne(queryWrapper);
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

    private int completeTask(ActivityCqReqVO reqVO, ActivityTaskTypeEnum taskTypeEnum, int rewardCount) {
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true, false);
        Integer enabledFlag = resolveTaskEnabledFlag(cacheData, taskTypeEnum);
        if (!isTaskEnabled(enabledFlag)) {
            throw exception(JK_TASK_NOT_ENABLED);
        }
        int taskLimit = defaultZero(resolveTaskLimit(cacheData, taskTypeEnum));
        saveOrUpdateTaskRecord(reqVO.getActivityId(), reqVO.getMemberId(), taskTypeEnum, taskLimit, rewardCount);
        return rewardCount;
    }

    private int completeShareHelp(ActivityCqReqVO reqVO) {
        if (reqVO == null || reqVO.getActivityId() == null || reqVO.getMemberId() == null || reqVO.getInviterMemberId() == null) {
            throw exception(MJ_IS_NOT_ID);
        }
        if (Objects.equals(reqVO.getMemberId(), reqVO.getInviterMemberId())) {
            throw exception(JK_HELP_SELF_NOT_ALLOW);
        }
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(reqVO.getActivityId(), reqVO.getStoreId(), true, false);
        if (!isTaskEnabled(cacheData.getShareEvent())) {
            throw exception(JK_TASK_NOT_ENABLED);
        }
        RLock inviterLock = redissonClient.getLock(CQ_HELP_LOCK_PREFIX + "inviter:" + reqVO.getActivityId() + ":" + reqVO.getInviterMemberId());
        RLock inviteeLock = redissonClient.getLock(CQ_HELP_LOCK_PREFIX + "invitee:" + reqVO.getActivityId() + ":" + reqVO.getMemberId());
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
            ActivityTaskDO inviterTaskDO = getTaskRecord(reqVO.getActivityId(), reqVO.getInviterMemberId(), ActivityTaskTypeEnum.SHARE.getCode(), shareLimit);
            int currentFinishCount = inviterTaskDO == null ? 0 : defaultZero(inviterTaskDO.getFinishCount());
            if (!isUnlimitedLimit(shareLimit) && currentFinishCount >= shareLimit) {
                throw exception(JK_HELP_TARGET_LIMIT_REACHED);
            }
            ActivityHelpDO helpDO = new ActivityHelpDO();
            helpDO.setActivityId(reqVO.getActivityId());
            helpDO.setInviterMemberId(reqVO.getInviterMemberId());
            helpDO.setInviteeMemberId(reqVO.getMemberId());
            helpDO.setHelpDate(today);
            helpDO.setScopeKey(reqVO.getActivityId() + ":" + reqVO.getMemberId() + ":" + today);
            activityHelpMapper.insert(helpDO);
            syncHelpCacheAfterInsert(helpDO);
            int rewardCount = isSevenDayNewMember(reqVO.getMemberId()) ? 2 : 1;
            saveOrUpdateTaskRecord(reqVO.getActivityId(), reqVO.getInviterMemberId(), ActivityTaskTypeEnum.SHARE, shareLimit, rewardCount);
            return rewardCount;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw exception(LOTTERY_SYSTEM_AGAIN);
        } finally {
            if (locked && helpLock.isHeldByCurrentThread()) {
                helpLock.unlock();
            }
        }
    }

    private void validateDailyHelpLimit(Long activityId, Long inviterMemberId, Long inviteeMemberId, LocalDate helpDate) {
        List<ActivityHelpDO> helpList = getHelpListWithCache(activityId, inviteeMemberId, helpDate);
        boolean repeated = helpList.stream().anyMatch(item -> Objects.equals(item.getInviterMemberId(), inviterMemberId));
        if (repeated) {
            throw exception(JK_HELP_REPEAT);
        }
        if (helpList.size() >= 5) {
            throw exception(JK_HELP_DAILY_LIMIT);
        }
    }

    private List<ActivityHelpDO> getHelpListWithCache(Long activityId, Long inviteeMemberId, LocalDate helpDate) {
        String cacheKey = buildHelpCacheKey(activityId, inviteeMemberId, helpDate);
        Object cachedObject = redisCache.getCacheObject(cacheKey);
        if (cachedObject instanceof List<?> cachedList) {
            return castHelpList(cachedList);
        }
        List<ActivityHelpDO> helpList = activityHelpMapper.selectList(new LambdaQueryWrapperX<ActivityHelpDO>()
                .eq(ActivityHelpDO::getActivityId, activityId)
                .eq(ActivityHelpDO::getInviteeMemberId, inviteeMemberId)
                .eq(ActivityHelpDO::getHelpDate, helpDate)
                .orderByAsc(ActivityHelpDO::getCreateTime)
                .orderByAsc(ActivityHelpDO::getId));
        redisCache.setCacheObject(cacheKey, new ArrayList<>(helpList), getHelpCacheSeconds(helpDate), TimeUnit.SECONDS);
        return helpList;
    }

    private void syncHelpCacheAfterInsert(ActivityHelpDO helpDO) {
        String cacheKey = buildHelpCacheKey(helpDO.getActivityId(), helpDO.getInviteeMemberId(), helpDO.getHelpDate());
        try {
            Object cachedObject = redisCache.getCacheObject(cacheKey);
            List<ActivityHelpDO> latestList = cachedObject instanceof List<?> cachedList ? castHelpList(cachedList) : new ArrayList<>();
            boolean exists = latestList.stream().anyMatch(item -> Objects.equals(item.getId(), helpDO.getId()));
            if (!exists) {
                latestList.add(helpDO);
            }
            redisCache.setCacheObject(cacheKey, latestList, getHelpCacheSeconds(helpDO.getHelpDate()), TimeUnit.SECONDS);
        } catch (Exception e) {
            redisCache.deleteObject(cacheKey);
        }
    }

    private String buildHelpCacheKey(Long activityId, Long inviteeMemberId, LocalDate helpDate) {
        return CQKeyConstants.CQ_HELP + activityId + ":" + inviteeMemberId + ":" + helpDate;
    }

    private Integer getHelpCacheSeconds(LocalDate helpDate) {
        LocalDate targetDate = helpDate == null ? LocalDate.now() : helpDate;
        long seconds = Duration.between(LocalDateTime.now(), targetDate.plusDays(1).atStartOfDay()).getSeconds();
        return Math.toIntExact(Math.max(seconds, 1L));
    }

    private List<ActivityHelpDO> castHelpList(List<?> cachedList) {
        List<ActivityHelpDO> helpList = new ArrayList<>();
        for (Object item : cachedList) {
            if (item instanceof ActivityHelpDO helpDO) {
                helpList.add(helpDO);
            }
        }
        return helpList;
    }

    private boolean isSevenDayNewMember(Long memberId) {
        WxMemberVO memberVO = getMember(memberId);
        if (memberVO == null || memberVO.getRegisterTime() == null) {
            return false;
        }
        LocalDate registerDate = memberVO.getRegisterTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return !registerDate.isBefore(LocalDate.now().minusDays(7));
    }

    private void saveOrUpdateTaskRecord(Long activityId, Long memberId, ActivityTaskTypeEnum taskTypeEnum, Integer taskLimit, int rewardCount) {
        ActivityTaskDO taskDO = getTaskRecord(activityId, memberId, taskTypeEnum.getCode(), taskLimit);
        if (taskDO == null) {
            if (isUnlimitedLimit(taskLimit)) {
                insertFirstTaskRecord(activityId, memberId, taskTypeEnum, rewardCount);
                return;
            }
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
        int currentFinishCount = defaultZero(taskDO.getFinishCount());
        if (!isUnlimitedLimit(taskLimit) && currentFinishCount >= taskLimit) {
            throw exception(JK_TASK_LIMIT_REACHED);
        }
        taskDO.setFinishCount(currentFinishCount + 1);
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
        if (activityTaskMapper.update(null, updateWrapper) <= 0) {
            throw exception(LOTTERY_SYSTEM_AGAIN);
        }
    }

    private boolean isTaskEnabled(Integer enabledFlag) {
        return Objects.equals(enabledFlag, 1);
    }

    private boolean isUnlimitedLimit(Integer taskLimit) {
        return taskLimit == null || taskLimit == TASK_LIMIT_UNLIMITED;
    }

    private Integer resolveOrderTaskLimit(ActivityCqSaveOrUpdateReqVO cacheData) {
        return isUnlimitedOrderTask(cacheData) ? TASK_LIMIT_UNLIMITED : cacheData.getPlaceOrderCodeNumber();
    }

    private boolean isUnlimitedOrderTask(ActivityCqSaveOrUpdateReqVO cacheData) {
        if (cacheData == null) {
            return true;
        }
        return Objects.equals(cacheData.getPlaceOrderCode(), 1)
                || cacheData.getPlaceOrderCodeNumber() == null
                || cacheData.getPlaceOrderCodeNumber() == 0;
    }

    private BigDecimal resolveOrderTaskAmount(ActivityCqSaveOrUpdateReqVO cacheData) {
        if (cacheData == null || !Objects.equals(cacheData.getPaymentThreshold(), 1)) {
            return BigDecimal.ZERO;
        }
        return cacheData.getPaymentCount() == null ? BigDecimal.ZERO : cacheData.getPaymentCount();
    }

    private Integer resolveTaskEnabledFlag(ActivityCqSaveOrUpdateReqVO reqVO, ActivityTaskTypeEnum taskTypeEnum) {
        return switch (taskTypeEnum) {
            case SIGN -> reqVO.getDailyAttendance();
            case FREE -> reqVO.getFreeEvent();
            case ORDER -> reqVO.getPlaceOrderStatus();
            case SHARE -> reqVO.getShareEvent();
            case BROWSE -> reqVO.getBrowseHomeEvent();
        };
    }

    private Integer resolveTaskLimit(ActivityCqSaveOrUpdateReqVO reqVO, ActivityTaskTypeEnum taskTypeEnum) {
        return switch (taskTypeEnum) {
            case SIGN -> reqVO.getDailyAttenCount();
            case FREE -> 1;
            case ORDER -> reqVO.getPlaceOrderCodeNumber();
            case SHARE -> reqVO.getShareCount();
            case BROWSE -> reqVO.getBrowseHomeCount();
        };
    }

    private void grantFreeTaskOnJoin(Long activityId, Long storeId, Long memberId) {
        if (activityId == null || memberId == null) {
            return;
        }
        ActivityCqSaveOrUpdateReqVO cacheData = getValidatedActivity(activityId, storeId, false, false);
        if (!isTaskEnabled(cacheData.getFreeEvent()) || defaultZero(cacheData.getFreeCount()) <= 0) {
            return;
        }
        if (Objects.equals(cacheData.getDrawStatus(), 2)) {
            return;
        }
        LocalDateTime resultPublishTime = toLocalDateTime(cacheData.getResultPublishTime());
        if (resultPublishTime != null && !LocalDateTime.now().isBefore(resultPublishTime)) {
            return;
        }
        ActivityTaskDO taskDO = getOrInitTaskRecord(activityId, memberId, ActivityTaskTypeEnum.FREE, 1);
        if (defaultZero(taskDO.getFinishCount()) > 0) {
            return;
        }
        int rewardCount = defaultZero(cacheData.getFreeCount());
        saveOrUpdateTaskRecord(activityId, memberId, ActivityTaskTypeEnum.FREE, 1, rewardCount);
        createCodeLogs(activityId, memberId, storeId, ActivityTaskTypeEnum.FREE.getCode(), rewardCount);
    }

    private Integer calculateActivityStatus(Integer drawStatus, Date startDate, Date resultPublishTime) {
        if (Objects.equals(drawStatus, 2)) {
            return 2;
        }
        if (startDate == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        if (now < startDate.getTime()) {
            return 0;
        }
        if (resultPublishTime != null && now >= resultPublishTime.getTime()) {
            return 2;
        }
        return 1;
    }

    private Integer resolveLatestDrawStatus(Long activityId, ActivityCqSaveOrUpdateReqVO cacheData) {
        Integer cacheDrawStatus = cacheData.getDrawStatus();
        if (Objects.equals(cacheDrawStatus, 2)) {
            return cacheDrawStatus;
        }
        Date resultPublishTime = cacheData.getResultPublishTime();
        if (resultPublishTime == null || System.currentTimeMillis() < resultPublishTime.getTime()) {
            return cacheDrawStatus;
        }
        ActivityCqDO latestActivityCq = this.getOne(new LambdaQueryWrapperX<ActivityCqDO>()
            .eq(ActivityCqDO::getActivityId, activityId)
            .last("limit 1"));
        if (latestActivityCq == null || latestActivityCq.getDrawStatus() == null) {
            return cacheDrawStatus;
        }
        if (!Objects.equals(latestActivityCq.getDrawStatus(), cacheDrawStatus)) {
            cacheData.setDrawStatus(latestActivityCq.getDrawStatus());
            redisCache.setCacheObject(CQKeyConstants.CQ_SETTING + activityId, cacheData, CQ_SETTING_CACHE_SECONDS, TimeUnit.SECONDS);
        }
        return latestActivityCq.getDrawStatus();
    }

    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Integer> parseIntegerList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(value.split(","))
                .filter(item -> item != null && !item.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<String> parseStringList(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(value.split(","))
                .filter(item -> item != null && !item.isBlank())
                .collect(Collectors.toList());
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasJoined(Long activityId, Long memberId) {
        if (activityId == null || memberId == null) {
            return false;
        }
        return activityCqJoinMapper.selectCount(new LambdaQueryWrapperX<ActivityCqJoinDO>()
                .eq(ActivityCqJoinDO::getActivityId, activityId)
                .eq(ActivityCqJoinDO::getMemberId, memberId)) > 0;
    }

    private void validateBeforePublish(Long activityId) {
        ActivityCqSaveOrUpdateReqVO cacheData = getCachedCqSetting(activityId);
        if (cacheData == null) {
            cacheData = loadCqSettingWithLock(activityId);
        }
        if (cacheData == null) {
            throw exception(LOTTERY_CQ_CLOSED);
        }
        if (Objects.equals(cacheData.getDrawStatus(), 2)) {
            throw exception(LOTTERY_CQ_CLOSED);
        }
        LocalDateTime resultPublishTime = toLocalDateTime(cacheData.getResultPublishTime());
        if (resultPublishTime != null && !LocalDateTime.now().isBefore(resultPublishTime)) {
            throw exception(LOTTERY_CQ_CLOSED);
        }
    }

    /**
     * Date 转 LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("Asia/Shanghai"));
    }

    private DateTime resolveCqStartDate(ActivityDO activityDO, ActivityCqDO activityCqDO) {
        if (activityCqDO != null && activityCqDO.getStartDateTime() != null) {
            return DateTime.of(Date.from(activityCqDO.getStartDateTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant()));
        }
        return activityDO == null || activityDO.getStartDate() == null ? null : DateTime.of(activityDO.getStartDate());
    }

    private DateTime resolveCqEndDate(ActivityDO activityDO, ActivityCqDO activityCqDO) {
        if (activityCqDO != null && activityCqDO.getEndDateTime() != null) {
            return DateTime.of(Date.from(activityCqDO.getEndDateTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant()));
        }
        return activityDO == null || activityDO.getEndDate() == null ? null : DateTime.of(activityDO.getEndDate());
    }

    @lombok.Data
    private static class MemberSnapshot {
        private String memberName;
        private String memberMobile;
        private Integer gender;
        private Integer memberCategory;
    }
}
