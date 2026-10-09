package com.htyoudao.youdao.module.promotion.service.lottery.impl;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.excel.core.service.vo.LotteryLogStatisticsVO;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.QueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillSpreadRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.ParseResult;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.TransferNotify;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer.ActivityAnswerRewardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk.ActivityJkPrizeExchangeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote.ActivityVoteRewardLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.ActivityJkPrizeExchange.ActivityJkPrizeExchangeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityAnswer.ActivityAnswerRewardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityLog.ActivityCqLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySign.ActivitySignRewardRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityVote.ActivityVoteRewardLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.*;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.module.promotion.enums.LotteryStateEnum;
import com.htyoudao.youdao.module.promotion.enums.LotteryTransferStatus;
import com.htyoudao.youdao.module.promotion.service.activitySign.ActivitySignService;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogQueryService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryLedger;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.LOTTERY_PAGE_1000_NOT_E;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.POINTS_PAGE_1000_NOT_E;
import static com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDAnalysisServiceImpl.convertDateToLocalDateTime;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;


@Service
@Slf4j
public class LotteryLogServiceImpl implements LotteryLogService {

    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Shanghai");

    @Resource
    private LotteryLogMapper lotteryLogMapper;
    @Resource
    private LotteryLedger lotteryLedger;
    @Resource
    private LotteryPrizeMapper lotteryPrizeMapper;

    @Resource
    private LotterySettingsMapper lotterySettingsMapper;

    @Resource
    private IEventService eventService;
    @Resource
    public RedisCache redisCache;

    @Autowired
    private LotteryLogQueryService lotteryLogQueryService;

    @Autowired
    private ActivityJkPrizeExchangeMapper activityJkPrizeExchangeMapper;


    @Resource
    private ExcelActionService<LotteryLogExportRespVO> logExportRespVOExcelActionService;

    @Resource
    private ElasticsearchClient esClient;


    @DubboReference
    private StoreApi storeApi;

    @Autowired
    private LotteryTransferRecordMapper lotteryTransferRecordMapper;

    @Resource
    private ActivityCqLogMapper activityCqLogMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivitySignService activitySignService;

    @Resource
    private ActivitySignRewardRecordMapper activitySignRewardRecordMapper;

    @Resource
    private ActivityAnswerRewardLogMapper activityAnswerRewardLogMapper;

    @Resource
    private ActivityVoteRewardLogMapper activityVoteRewardLogMapper;

    @DS(DsNameConstants.SHARDING)
    @Override
    public PageResult<LotteryLogDO> getLotteryLogList(Integer pageNum, Integer pageSize, LotteryLogReqVO lotteryLogReqVO) {

        if (pageNum > 1000) {
            throw exception(LOTTERY_PAGE_1000_NOT_E);
        }

        LambdaQueryWrapper<LotteryLogDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        logDOLambdaQueryWrapper.orderBy(true, false, LotteryLogDO::getCreateTime, LotteryLogDO::getId);
        if (StringUtils.isNotBlank(lotteryLogReqVO.getMemberMobile())) {
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getMemberMobile, lotteryLogReqVO.getMemberMobile());
        }
        if (lotteryLogReqVO.getLotteryId() != null) {
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getLotteryId, lotteryLogReqVO.getLotteryId());
        }
        if (lotteryLogReqVO.getPrizeType() != null) {
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getPrizeType, lotteryLogReqVO.getPrizeType());
        }
        if (lotteryLogReqVO.getLotteryStartDate() != null) {
            logDOLambdaQueryWrapper.le(LotteryLogDO::getLotteryStartTime, lotteryLogReqVO.getLotteryStartDate());
        }
        if (lotteryLogReqVO.getLotteryEndDate() != null) {
            logDOLambdaQueryWrapper.ge(LotteryLogDO::getLotteryEndTime, lotteryLogReqVO.getLotteryEndDate());
        }
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(pageNum);
        pageParam.setPageSize(pageSize);
        return lotteryLogMapper.selectPage(pageParam, logDOLambdaQueryWrapper);
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    @LogRecord(type = SYSTEM_LOTTERY_LOG_TYPE, subType = SYSTEM_LOTTERY_LOG_TYPE_ADD_EXPRESS_TYPE, bizNo = "{{#lotteryLogReqVO.id}}", success = SYSTEM_LOTTERY_LOG_TYPE_ADD_EXPRESS_SUCCESS)
    public Integer updateExpress(LotteryLogReqVO lotteryLogReqVO) {
        LambdaQueryWrapper<LotteryLogDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        logDOLambdaQueryWrapper.eq(LotteryLogDO::getId, lotteryLogReqVO.getId());
        logDOLambdaQueryWrapper.eq(LotteryLogDO::getMemberId, lotteryLogReqVO.getMemberId());
        int result = lotteryLogMapper.update(BeanUtils.toBean(lotteryLogReqVO, LotteryLogDO.class), logDOLambdaQueryWrapper);
        LogRecordContext.putVariable("lotteryLogReqVO", lotteryLogReqVO);
        return result;
    }

    /**
     * 退回积分、奖品
     */
    @Override
    @DS(DsNameConstants.SHARDING)
    @DataPermission(enable = false)
    public void returnReal() {
        LocalDateTime now = LocalDateTime.now();
        // 退回奖品
        LocalDateTime oneDayAgo = now.minus(2, ChronoUnit.DAYS);
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 格式化 LocalDateTime 为字符串
        String formattedDate = oneDayAgo.format(formatter);

        List<LotteryLogDO> list = lotteryLogMapper.selectList(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getPrizeState, 1)
                .eq(LotteryLogDO::getDeleted, 0)
                .eq(LotteryLogDO::getPrizeType, 3)
                .lt(LotteryLogDO::getCreateTime, formattedDate));
        if (list.isEmpty() || list.size() == 0) {
            return;
        }
        Set<Long> uniqueActivityIds = new HashSet<>();
        for (LotteryLogDO lotteryLog : list) {
            if (lotteryLedger.queuePhysicalReturn(lotteryLog)) continue;
            uniqueActivityIds.add(lotteryLog.getLotteryId());
            LotteryPrizeDO lotteryPrize = lotteryPrizeMapper.selectOne(new LambdaQueryWrapperX<LotteryPrizeDO>().eq(LotteryPrizeDO::getId, lotteryLog.getLotteryPrizeId()));
            lotteryPrize.setRemainNum(lotteryPrize.getRemainNum() - 1);
            lotteryPrizeMapper.updateById(lotteryPrize);
            lotteryLog.setPrizeState(9);
            lotteryLogMapper.update(lotteryLog, new LambdaQueryWrapperX<LotteryLogDO>()
                    .eq(LotteryLogDO::getId, lotteryLog.getId()).eq(LotteryLogDO::getMemberId, lotteryLog.getMemberId()));
        }
        for (Long lotteryId : uniqueActivityIds) {
            List<LotteryPrizeDO> prizzeList = lotteryPrizeMapper.selectList(new LambdaQueryWrapperX<LotteryPrizeDO>().eq(LotteryPrizeDO::getLotteryId, lotteryId).eq(LotteryPrizeDO::getDeleted, 0));
            redisCache.setCacheObject(RedisKeyConstants.LOTTERY_PRIZE + lotteryId, prizzeList);
        }
    }


    @DS(DsNameConstants.SHARDING)
    @Override
    public PageResult<LotteryLogDO> getByLotteryLogList(LotteryLogReqVO lotteryLogReqVO) {
        if (lotteryLogReqVO.getPageNo() > 1000) {
            throw exception(LOTTERY_PAGE_1000_NOT_E);
        }
        LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, lotteryLogReqVO.getLotteryId())
                        .or()
                        .eq(LotterySettingsDO::getActivityId, lotteryLogReqVO.getLotteryId())
                )
        );
        LambdaQueryWrapper<LotteryLogDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (lotterySettingsDO != null) {
            // 获取抽奖活动开始时间
            Date lotteryStartTime = lotterySettingsDO.getLotteryStartTime();
            Date lotteryEndTime = lotterySettingsDO.getLotteryEndTime();
            LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
            LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0); // 清除纳秒，确保时间精确到秒
            }


            logDOLambdaQueryWrapper.ge(LotteryLogDO::getCreateTime, start);
            logDOLambdaQueryWrapper.le(LotteryLogDO::getCreateTime, end);
        }


//        logDOLambdaQueryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') >= DATE_FORMAT(lottery_start_time, '%Y-%m-%d 00:00:00')");
//        logDOLambdaQueryWrapper.apply("DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') <= DATE_FORMAT(lottery_end_time, '%Y-%m-%d 23:59:59')");

//        // 添加createTime在lotteryStartTime和lotteryEndTime字段值范围内的查询条件（只比较日期部分）
//        logDOLambdaQueryWrapper.apply("DATE(create_time) >= DATE(lottery_start_time)");
//        logDOLambdaQueryWrapper.apply("DATE(create_time) <= DATE(lottery_end_time)");

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getMemberName())) {
            logDOLambdaQueryWrapper
                    .nested(wrapper -> wrapper
                            .eq(LotteryLogDO::getMemberMobile, lotteryLogReqVO.getMemberName())
                    );
        }
        // 新增：处理 claimStatus 为 3 的特殊查询条件
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getClaimStatus())) {
            Integer claimStatus = lotteryLogReqVO.getClaimStatus();

            // 基础条件：奖品类型为5
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getPrizeType, 5);

            if (claimStatus.equals(3)) {
                // 查询状态为3的数据，或者状态为2且更新时间大于等于24小时的数据
                logDOLambdaQueryWrapper.and(wrapper ->
                        wrapper.eq(LotteryLogDO::getClaimStatus, 3)
                                .or(subWrapper ->
                                        subWrapper.eq(LotteryLogDO::getClaimStatus, 2)
                                                .apply("TIMESTAMPDIFF(HOUR, update_time, NOW()) >= 24")
                                )
                );
            } else if (claimStatus.equals(2)) {
                // 查询状态为2且更新时间小于24小时的数据
                logDOLambdaQueryWrapper
                        .eq(LotteryLogDO::getClaimStatus, 2)
                        .apply("TIMESTAMPDIFF(HOUR, update_time, NOW()) < 24");
            } else if (claimStatus.equals(1)) {
                // 查询状态为1的数据
                logDOLambdaQueryWrapper.eq(LotteryLogDO::getClaimStatus, 1);
            }
        }
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryId())) {
            logDOLambdaQueryWrapper.and(wrapper ->
                    wrapper.eq(LotteryLogDO::getLotteryId, lotteryLogReqVO.getLotteryId())
                            .or()
                            .eq(LotteryLogDO::getLotteryId, lotterySettingsDO.getActivityId())
            );
        }
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getPrizeType())) {
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getPrizeType, lotteryLogReqVO.getPrizeType());
        }
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryStartDate())) {
            logDOLambdaQueryWrapper.ge(LotteryLogDO::getCreateTime, lotteryLogReqVO.getLotteryStartDate());
        }
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryEndDate())) {
            logDOLambdaQueryWrapper.le(LotteryLogDO::getCreateTime, lotteryLogReqVO.getLotteryEndDate());
        }
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getReceiveAddressType())) {
            if (lotteryLogReqVO.getReceiveAddressType().equals(0)) {
                logDOLambdaQueryWrapper.isNull(LotteryLogDO::getReceiveAddress);
            } else if (lotteryLogReqVO.getReceiveAddressType().equals(1)) {
                logDOLambdaQueryWrapper.isNotNull(LotteryLogDO::getReceiveAddress);
            }
        }
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getTrackingType())) {
            if (lotteryLogReqVO.getTrackingType().equals(0)) {
                logDOLambdaQueryWrapper.isNull(LotteryLogDO::getTrackingNumber);
            } else if (lotteryLogReqVO.getTrackingType().equals(1)) {
                logDOLambdaQueryWrapper.isNotNull(LotteryLogDO::getTrackingNumber);
            }
        }
        logDOLambdaQueryWrapper.orderBy(true, false, LotteryLogDO::getCreateTime, LotteryLogDO::getId);
        PageParam pageParam = new PageParam();
        pageParam.setPageNo(lotteryLogReqVO.getPageNo());
        pageParam.setPageSize(lotteryLogReqVO.getPageSize());
        PageResult<LotteryLogDO> lotteryLogDOPageResult = lotteryLogMapper.selectPage(pageParam, logDOLambdaQueryWrapper);
        List<LotteryLogDO> list = lotteryLogDOPageResult.getList();

        if (ObjectUtil.isNotEmpty(list)) {
//            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

            list.stream()
                    .filter(log -> log.getPrizeType() == 5 &&
                            log.getClaimStatus() == 2 &&
                            log.getUpdateTime() != null &&  // 确保updateTime不为空
                            Duration.between(log.getCreateTime(), log.getUpdateTime()).toHours() >= 24)
                    .forEach(log -> {
                        // 1. 设置状态为3
                        log.setClaimStatus(3);
                    });
            list.stream().forEach(log -> {
                // 2. 根据 storeId 查询门店名称
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(log.getStoreId());
                StoreDTO data = storeByStoreId.getData();
                if (data != null) {
                    log.setStoreName(data.getStoreName());
                }

            });


        }

        return lotteryLogDOPageResult;
    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public LotteryLogStatisticsRespVO lotteryDrawStatistics(LotteryLogReqVO lotteryLogReqVO) {
        LotteryLogStatisticsRespVO vo = new LotteryLogStatisticsRespVO();
        LambdaQueryWrapper<LotterySettingsDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.and(wrapper -> wrapper.eq(LotterySettingsDO::getId, lotteryLogReqVO.getLotteryId())
                .or()
                .eq(LotterySettingsDO::getActivityId, lotteryLogReqVO.getLotteryId()));
        LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(lambdaQueryWrapper);
        Date lotteryStartTime = lotterySettingsDO.getLotteryStartTime();
        Date lotteryEndTime = lotterySettingsDO.getLotteryEndTime();
        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);

        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0);
        }

        // 打印时间范围
        System.out.println("查询时间范围: " + start + " 到 " + end);
        System.out.println("lotteryId: " + lotteryLogReqVO.getLotteryId());

        QueryWrapper<LotteryLogDO> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT member_id) as participantCount",
                        "SUM(price) as totalPrizeValue",
                        "COUNT(id) as totalRecords")
                .and(w -> w.eq("lottery_id", lotteryLogReqVO.getLotteryId())
                        .or()
                        .eq("lottery_id", lotterySettingsDO.getActivityId()))
                .ge("create_time", start)
                .le("create_time", end);


        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getClaimStatus())) {
            Integer claimStatus = lotteryLogReqVO.getClaimStatus();

            // 基础条件：奖品类型为5
            wrapper.eq("prize_type", 5);

            if (claimStatus.equals(3)) {
                // 查询状态为3的数据，或者状态为2且更新时间大于等于24小时的数据
                wrapper.and(w ->
                        w.eq("claim_status", 3)
                                .or(w2 ->
                                        w2.eq("claim_status", 2)
                                                .apply("TIMESTAMPDIFF(HOUR, update_time, NOW()) >= 24")
                                )
                );
            } else if (claimStatus.equals(2)) {
                // 查询状态为2且更新时间小于24小时的数据
                wrapper.eq("claim_status", 2)
                        .apply("TIMESTAMPDIFF(HOUR, update_time, NOW()) < 24");
            } else if (claimStatus.equals(1)) {
                // 查询状态为1的数据
                wrapper.eq("claim_status", 1);
            }
        }

// 添加 memberName 查询条件
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getMemberName())) {
            wrapper.and(queryWrapper -> queryWrapper
                    .like("member_name", lotteryLogReqVO.getMemberName())
                    .or()
                    .like("member_mobile", lotteryLogReqVO.getMemberName())
            );
        }


        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getClaimStatus())) {
            Integer claimStatus = lotteryLogReqVO.getClaimStatus();

            // 基础条件：奖品类型为5
            wrapper.eq("prize_type", 5);

            if (claimStatus.equals(3)) {
                // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于等于24小时的数据
                wrapper.and(w ->
                        w.eq("claim_status", 3)
                                .or(w2 ->
                                        w2.eq("claim_status", 2)
                                                .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) >= 24")
                                )
                );
            } else if (claimStatus.equals(2)) {
                // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                wrapper.eq("claim_status", 2)
                        .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
            } else if (claimStatus.equals(1)) {
                // 查询状态为1的数据
                wrapper.eq("claim_status", 1);
            }
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getPrizeType())) {
            wrapper.eq("prize_type", lotteryLogReqVO.getPrizeType());
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryStartDate())) {
            wrapper.ge("create_time", lotteryLogReqVO.getLotteryStartDate());
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryEndDate())) {
            wrapper.le("create_time", lotteryLogReqVO.getLotteryEndDate());
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getReceiveAddressType())) {
            if (lotteryLogReqVO.getReceiveAddressType().equals(0)) {
                wrapper.isNull("receive_address");
            } else if (lotteryLogReqVO.getReceiveAddressType().equals(1)) {
                wrapper.isNotNull("receive_address");
            }
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getTrackingType())) {
            if (lotteryLogReqVO.getTrackingType().equals(0)) {
                wrapper.isNull("tracking_number");
            } else if (lotteryLogReqVO.getTrackingType().equals(1)) {
                wrapper.isNotNull("tracking_number");
            }
        }

        // 打印生成的SQL语句
        System.out.println("生成的SQL: " + wrapper.getTargetSql());


        // 先查询原始数据验证
        QueryWrapper<LotteryLogDO> detailWrapper = new QueryWrapper<>();
        detailWrapper.and(w -> w.eq("lottery_id", lotteryLogReqVO.getLotteryId())
                        .or()
                        .eq("lottery_id", lotterySettingsDO.getActivityId()))
                .ge("create_time", start)
                .le("create_time", end)
                .orderByAsc("member_id");

//        List<LotteryLogDO> detailList = lotteryLogMapper.selectList(detailWrapper);
//        QueryWrapper<LotteryLogDO> detailWrapper = new QueryWrapper<>();
//        detailWrapper.eq("lottery_id", lotteryLogReqVO.getLotteryId())
//                .ge("create_time", start)
//                .le("create_time", end)
//                .orderByAsc("member_id");

// 添加 memberName 查询条件
        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getMemberName())) {
            detailWrapper.and(lotteryLogDOQueryWrapper -> lotteryLogDOQueryWrapper
                    .like("member_name", lotteryLogReqVO.getMemberName())
                    .or()
                    .like("member_mobile", lotteryLogReqVO.getMemberName())
            );
        }


        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getClaimStatus())) {
            Integer claimStatus = lotteryLogReqVO.getClaimStatus();

            // 基础条件：奖品类型为5
            detailWrapper.eq("prize_type", 5);

            if (claimStatus.equals(3)) {
                // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于等于24小时的数据
                detailWrapper.and(w ->
                        w.eq("claim_status", 3)
                                .or(w2 ->
                                        w2.eq("claim_status", 2)
                                                .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) >= 24")
                                )
                );
            } else if (claimStatus.equals(2)) {
                // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                detailWrapper.eq("claim_status", 2)
                        .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
            } else if (claimStatus.equals(1)) {
                // 查询状态为1的数据
                detailWrapper.eq("claim_status", 1);
            }
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getPrizeType())) {
            detailWrapper.eq("prize_type", lotteryLogReqVO.getPrizeType());
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryStartDate())) {
            detailWrapper.ge("create_time", lotteryLogReqVO.getLotteryStartDate());
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getLotteryEndDate())) {
            detailWrapper.le("create_time", lotteryLogReqVO.getLotteryEndDate());
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getReceiveAddressType())) {
            if (lotteryLogReqVO.getReceiveAddressType().equals(0)) {
                detailWrapper.isNull("receive_address");
            } else if (lotteryLogReqVO.getReceiveAddressType().equals(1)) {
                detailWrapper.isNotNull("receive_address");
            }
        }

        if (ObjectUtil.isNotEmpty(lotteryLogReqVO.getTrackingType())) {
            if (lotteryLogReqVO.getTrackingType().equals(0)) {
                detailWrapper.isNull("tracking_number");
            } else if (lotteryLogReqVO.getTrackingType().equals(1)) {
                detailWrapper.isNotNull("tracking_number");
            }
        }

        List<LotteryLogDO> detailList = lotteryLogMapper.selectList(detailWrapper);
        System.out.println("符合条件的总记录数: " + detailList.size());

        // 手动统计独立用户数
        Set<Long> distinctMembers = detailList.stream()
                .map(LotteryLogDO::getMemberId)
                .collect(Collectors.toSet());
        Long participationCount = Long.valueOf(distinctMembers.size());

        vo.setParticipationCount(participationCount);
        System.out.println("手动统计独立用户数: " + distinctMembers.size());
        System.out.println("独立用户ID列表: " + distinctMembers);

        // 检查member_id分布
        Map<Long, Long> memberCountMap = detailList.stream()
                .collect(Collectors.groupingBy(LotteryLogDO::getMemberId, Collectors.counting()));
        System.out.println("各用户参与次数: " + memberCountMap);

        // 检查是否有null的member_id
        long nullMemberCount = detailList.stream()
                .filter(log -> log.getMemberId() == null)
                .count();
        System.out.println("member_id为null的记录数: " + nullMemberCount);

        // 执行聚合查询
        List<Map<String, Object>> resultMaps = lotteryLogMapper.selectMaps(wrapper);
        System.out.println("聚合查询结果: " + resultMaps);

        List<LotteryLogStatisticsRespVO> collect = resultMaps.stream()
                .map(map -> {

                    vo.setLotteryCount((Long) map.get("totalRecords"));
                    Object prizeValue = map.get("totalPrizeValue");
                    vo.setPrizeValue(prizeValue != null ? (BigDecimal) prizeValue : BigDecimal.ZERO);
                    vo.setParticipation((Long) map.get("participantCount"));
//                    vo.setParticipationCount((Long) map.get("participantCount"));
                    return vo;
                })
                .collect(Collectors.toList());

        return collect.get(0);
    }

//    @Override
//    @DS(DsNameConstants.SHARDING)
//    public void exportLotteryList(LotteryLogExportVO lotteryLogExportVO, HttpServletRequest request, HttpServletResponse response) {
//        Long businessId = BusinessContextHolder.getBusinessId();
//
//        // 构建查询条件
//        LambdaQueryWrapper<LotteryLogDO> queryWrapper = buildQueryWrapper(lotteryLogExportVO, businessId);
//
//        // 设置分页参数，每页1万条
//        int pageSize = 10000;
//        Page<LotteryLogExportRespVO> pageParam = new Page<>(1, pageSize);
//
//        // 使用分页方式导出
//        logExportRespVOExcelActionService.exportAsyncExcel(
//                LotteryLogExportRespVO.class,
//                pageParam,
//                param -> getLotteryLogData(param, queryWrapper),
//                "抽奖记录列表"
//        );
//    }

    @Override
    @DS(DsNameConstants.SHARDING)
    public void exportLotteryList(LotteryLogExportVO lotteryLogExportVO, HttpServletRequest request, HttpServletResponse response) {
        Long businessId = BusinessContextHolder.getBusinessId();


        // 构建查询条件
        LambdaQueryWrapper<LotteryLogDO> queryWrapper = buildQueryWrapper(lotteryLogExportVO, businessId);
        // 3. 固定数据快照（使用导出开始时间）
        LocalDateTime exportSnapshotTime = LocalDateTime.now();
        queryWrapper.le(LotteryLogDO::getCreateTime, exportSnapshotTime);
        // 添加排序，确保分页稳定性
        queryWrapper.orderBy(true, true, LotteryLogDO::getCreateTime, LotteryLogDO::getId);

        // 设置分页参数，每页1万条
        int pageSize = 10000;
        Page<LotteryLogExportRespVO> pageParam = new Page<>(1, pageSize);

        // 生成统计数据（第二个Sheet的内容）
        List<LotteryLogStatisticsVO> statistics = generateEnhancedStatistics(lotteryLogExportVO);

        // 使用新的双Sheet导出方法
        logExportRespVOExcelActionService.exportAsyncExcel(
                LotteryLogExportRespVO.class,
                pageParam,
                (Page<LotteryLogExportRespVO> param) -> getLotteryLogData(param, queryWrapper),
                buildExportFileName(lotteryLogExportVO),
                statistics
        );
    }


    /**
     * 构建导出文件名
     */
    private String buildExportFileName(LotteryLogExportVO lotteryLogExportVO) {
        StringBuilder fileName = new StringBuilder("抽奖记录");

        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getLotteryId())) {
            fileName.append("_活动").append(lotteryLogExportVO.getLotteryId());
        }

        // 添加时间戳
        fileName.append("_").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        return fileName.toString();
    }

    /**
     * 增强的统计方法（添加标题和格式）
     */
    private List<LotteryLogStatisticsVO> generateEnhancedStatistics(LotteryLogExportVO lotteryLogExportVO) {
        List<LotteryLogStatisticsVO> statistics = new ArrayList<>();

        try {
            // 1. 添加报表标题行（第一行：大标题）
            LotteryLogStatisticsVO title = new LotteryLogStatisticsVO();
            title.setStoreName("【抽奖活动门店统计报表】");
            statistics.add(title);

            // 2. 添加活动信息行（第二行）
            if (lotteryLogExportVO.getLotteryId() != null) {
                LotterySettingsDO settings = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                        .and(wrapper -> wrapper
                                .eq(LotterySettingsDO::getId, lotteryLogExportVO.getLotteryId())
                                .or()
                                .eq(LotterySettingsDO::getActivityId, lotteryLogExportVO.getLotteryId())
                        )
                );
                if (settings != null) {
                    LotteryLogStatisticsVO activityInfo = new LotteryLogStatisticsVO();
                    activityInfo.setStoreName("活动名称: " + settings.getLotteryTitle());
                    statistics.add(activityInfo);
                }
            }

            // 3. 添加空行（第三行）
            statistics.add(new LotteryLogStatisticsVO());

            // 4. 添加表头行（第四行）
            LotteryLogStatisticsVO header = new LotteryLogStatisticsVO();
            header.setStoreName("门店名称");
            header.setParticipantsCount("参与人数");  // 表头标识
            header.setLotteryCount("抽奖次数");       // 表头标识
            statistics.add(header);

            // 5. 获取门店分组统计数据
            List<StoreStatisticsDTO> storeStats = getStoreStatistics(lotteryLogExportVO);

            // 6. 添加门店数据行
            int totalParticipants = 0;
            int totalLotteryCount = 0;
//
            for (StoreStatisticsDTO stat : storeStats) {
                String storeName = getStoreNameById(stat.getStoreId());

                LotteryLogStatisticsVO vo = new LotteryLogStatisticsVO();
//                vo.setStoreId(stat.getStoreId());
                vo.setStoreName(storeName);
                vo.setParticipantsCount(stat.getParticipantsCount());
                vo.setLotteryCount(stat.getLotteryCount());
                statistics.add(vo);
                String participantsCount = stat.getParticipantsCount();
                Integer numParticipantsCount = Integer.valueOf(participantsCount);
                String lotteryCount = stat.getLotteryCount();
                Integer numLotteryCount = Integer.valueOf(lotteryCount);
                totalParticipants += numParticipantsCount;
                totalLotteryCount += numLotteryCount;
            }


            // 10. 添加生成时间
            statistics.add(new LotteryLogStatisticsVO());
            LotteryLogStatisticsVO timeRow = new LotteryLogStatisticsVO();
            timeRow.setStoreName("生成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            statistics.add(timeRow);


        } catch (Exception e) {
            log.error("生成统计报表失败", e);
            // 返回简单的错误信息
            LotteryLogStatisticsVO error = new LotteryLogStatisticsVO();
            error.setStoreName("统计生成失败: " + e.getMessage());
            statistics.add(error);
        }

        return statistics;
    }


    private List<StoreStatisticsDTO> getStoreStatistics(LotteryLogExportVO lotteryLogExportVO) {
        try {
            LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                    .and(wrapper -> wrapper
                            .eq(LotterySettingsDO::getId, lotteryLogExportVO.getLotteryId())
                            .or()
                            .eq(LotterySettingsDO::getActivityId, lotteryLogExportVO.getLotteryId())
                    )
            );
            if (lotterySettingsDO != null) {
                Date lotteryStartTime = lotterySettingsDO.getLotteryStartTime();
                Date lotteryEndTime = lotterySettingsDO.getLotteryEndTime();
                LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
                LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);

                if (end != null) {
                    end = end.withHour(23)
                            .withMinute(59)
                            .withSecond(59)
                            .withNano(0);
                }
                QueryWrapper<LotteryLogDO> rawWrapper = new QueryWrapper<>();
                rawWrapper.select("store_id", "member_id")
                        .and(w -> w.eq("lottery_id", lotteryLogExportVO.getLotteryId())
                                .or()
                                .eq("lottery_id", lotterySettingsDO.getActivityId()))
                        .ge("create_time", start)
                        .le("create_time", end)
                        .isNotNull("store_id");

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getMemberName())) {
                    rawWrapper.and(queryWrapper -> queryWrapper
                            .like("member_name", lotteryLogExportVO.getMemberName())
                            .or()
                            .like("member_mobile", lotteryLogExportVO.getMemberName())
                    );
                }

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getClaimStatus())) {
                    Integer claimStatus = lotteryLogExportVO.getClaimStatus();

                    // 基础条件：奖品类型为5
                    rawWrapper.eq("prize_type", 5);

                    if (claimStatus.equals(3)) {
                        // 查询状态为3的数据，或者状态为2且创建时间大于24小时的数据
                        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

                        rawWrapper.and(w ->
                                w.eq("claim_status", 3)  // 状态3的数据
                                        .or(w2 ->
                                                w2.eq("claim_status", 2)  // 状态2的数据
                                                        .lt("create_time", twentyFourHoursAgo)  // 创建时间超过24小时
                                        )
                        );
                    } else if (claimStatus.equals(2)) {
                        // 查询状态2且创建时间在24小时内的数据
                        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

                        rawWrapper.eq("claim_status", 2)
                                .ge("create_time", twentyFourHoursAgo);  // 创建时间在24小时内
                    } else if (claimStatus.equals(1)) {
                        // 查询状态1且创建时间在24小时内的数据
                        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

                        rawWrapper.eq("claim_status", 1)
                                .ge("create_time", twentyFourHoursAgo);  // 创建时间在24小时内
                    }
                }

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getPrizeType())) {
                    rawWrapper.eq("prize_type", lotteryLogExportVO.getPrizeType());
                }

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getLotteryStartDate())) {
                    rawWrapper.ge("create_time", lotteryLogExportVO.getLotteryStartDate());
                }

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getLotteryEndDate())) {
                    rawWrapper.le("create_time", lotteryLogExportVO.getLotteryEndDate());
                }

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getReceiveAddressType())) {
                    if (lotteryLogExportVO.getReceiveAddressType().equals(0)) {
                        rawWrapper.isNull("receive_address");
                    } else if (lotteryLogExportVO.getReceiveAddressType().equals(1)) {
                        rawWrapper.isNotNull("receive_address");
                    }
                }

                if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getTrackingType())) {
                    if (lotteryLogExportVO.getTrackingType().equals(0)) {
                        rawWrapper.isNull("tracking_number");
                    } else if (lotteryLogExportVO.getTrackingType().equals(1)) {
                        rawWrapper.isNotNull("tracking_number");
                    }
                }

                List<LotteryLogDO> rawData = lotteryLogMapper.selectList(rawWrapper);

                // 手动分组统计
                Map<Long, StoreStatisticsDTO> resultMap = new HashMap<>();
                Map<Long, Set<Long>> storeMemberMap = new HashMap<>();

                log.info("开始处理{}条原始数据", rawData.size());

                for (LotteryLogDO record : rawData) {
                    Long storeId = record.getStoreId();
                    Long memberId = record.getMemberId();

                    if (storeId == null || storeId <= 0) continue;

                    log.debug("处理记录: storeId={}, memberId={}", storeId, memberId);

                    // 统计抽奖次数
                    StoreStatisticsDTO dto = resultMap.computeIfAbsent(storeId, k -> {
                        StoreStatisticsDTO newDto = new StoreStatisticsDTO();
                        newDto.setStoreId(storeId);
                        newDto.setLotteryCount("0");
                        newDto.setParticipantsCount("0");
                        return newDto;
                    });

                    // 更新抽奖次数
                    int currentCount = Integer.parseInt(dto.getLotteryCount());
                    dto.setLotteryCount(String.valueOf(currentCount + 1));

                    // 收集会员ID用于去重统计
                    if (memberId != null) {
                        storeMemberMap.computeIfAbsent(storeId, k -> new HashSet<>())
                                .add(memberId);
                    }
                }

                // 设置参与人数
                storeMemberMap.forEach((storeId, memberSet) -> {
                    StoreStatisticsDTO dto = resultMap.get(storeId);
                    if (dto != null) {
                        dto.setParticipantsCount(String.valueOf(memberSet.size()));
                    }
                });

                List<StoreStatisticsDTO> result = new ArrayList<>(resultMap.values());

                log.info("手动分组统计完成，共{}个门店", result.size());
                result.forEach(dto -> {
                    log.info("门店统计: storeId={}, participants={}, lotteryCount={}",
                            dto.getStoreId(), dto.getParticipantsCount(), dto.getLotteryCount());
                });

                return result;
            } else {
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("手动统计异常", e);
            return new ArrayList<>();
        }
    }


    // 新增：获取门店名称的方法
    private String getStoreNameById(Long storeId) {
        if (storeId == null || storeId == 0) {
            return "未知门店";
        }

        try {
            CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(storeId);
            StoreDTO data = storeByStoreId.getData();
            if (data != null) {
                return data.getStoreName();
            } else {
                return "未知门店";
            }
        } catch (Exception e) {
            log.warn("查询门店名称失败，storeId: {}", storeId, e);
            return "未知门店";
        }
    }

    private List<LotteryLogExportRespVO> getLotteryLogData(Page<LotteryLogExportRespVO> pageParam, LambdaQueryWrapper<LotteryLogDO> queryWrapper) {
        // 创建DO的分页参数
        Page<LotteryLogDO> currentPageParam = new Page<>(pageParam.getCurrent(), pageParam.getSize());
        // 通过Service方法调用，确保@DS注解生效
        Page<LotteryLogDO> resultPage = lotteryLogQueryService.selectPage(currentPageParam, queryWrapper);
        List<LotteryLogDO> logList = resultPage.getRecords();

        // 转换为响应对象
        List<LotteryLogExportRespVO> respList = new ArrayList<>(logList.size());
        for (LotteryLogDO lotteryLogDO : logList) {
            LotteryLogExportRespVO lotteryLogExportRespVO = new LotteryLogExportRespVO();
            BeanUtils.copyProperties(lotteryLogDO, lotteryLogExportRespVO);
            lotteryLogExportRespVO.setMemberId(lotteryLogDO.getMemberId().toString());
            String prizeTypeToName = convertPrizeTypeToName(lotteryLogDO.getPrizeType());
            if (ObjectUtil.isNotEmpty(lotteryLogDO.getStoreId())) {
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(lotteryLogDO.getStoreId());
                StoreDTO data = storeByStoreId.getData();
                if (data != null) {
                    lotteryLogExportRespVO.setStoreName(data.getStoreName());
                }
            }
            if (ObjectUtil.isNotEmpty(lotteryLogDO.getLotteryType())) {
                if (lotteryLogDO.getPrizeType() == 5) {
                    if (lotteryLogDO.getClaimStatus().equals(1)) {
                        lotteryLogExportRespVO.setClaimName("未领取");
                    } else if (lotteryLogDO.getClaimStatus().equals(2)) {
                        // 检查时间字段是否为空
                        if (lotteryLogDO.getCreateTime() != null && lotteryLogDO.getUpdateTime() != null) {
                            // 计算时间差（小时）
                            long hoursBetween = Duration.between(lotteryLogDO.getCreateTime(), lotteryLogDO.getUpdateTime()).toHours();

                            if (hoursBetween >= 24) {
                                lotteryLogExportRespVO.setClaimName("已失效");
                            } else {
                                lotteryLogExportRespVO.setClaimName("已领取");
                            }
                        } else {
                            // 如果时间为空，按已领取处理或根据业务需求处理
                            lotteryLogExportRespVO.setClaimName("已领取");
                        }
                    } else if (lotteryLogDO.getClaimStatus().equals(3)) {
                        lotteryLogExportRespVO.setClaimName("已失效");
                    }
                }
            }
            lotteryLogExportRespVO.setPrizeTypeName(prizeTypeToName);
            respList.add(lotteryLogExportRespVO);
        }

        return respList;
    }

    private String convertClaimStatusToName(Integer claimStatus) {
        if (claimStatus == null) {
            return "";
        }
        switch (claimStatus) {
            case 1:
                return "未领取";
            case 2:
                return "已领取";
            case 3:
                return "已过期";
            default:
                return "";
        }
    }

    // 提取查询条件构建方法
    private LambdaQueryWrapper<LotteryLogDO> buildQueryWrapper(LotteryLogExportVO lotteryLogExportVO, Long businessId) {
        LambdaQueryWrapper<LotteryLogDO> logDOLambdaQueryWrapper = new LambdaQueryWrapper<>();

        LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, lotteryLogExportVO.getLotteryId())
                        .or()
                        .eq(LotterySettingsDO::getActivityId, lotteryLogExportVO.getLotteryId())
                )
        );
        if (lotterySettingsDO != null) {
            Date lotteryStartTime = lotterySettingsDO.getLotteryStartTime();
            Date lotteryEndTime = lotterySettingsDO.getLotteryEndTime();
            LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
            LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);

            if (end != null) {
                end = end.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0);
            }

            logDOLambdaQueryWrapper.ge(LotteryLogDO::getCreateTime, start);
            logDOLambdaQueryWrapper.le(LotteryLogDO::getCreateTime, end);
        }
        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getClaimStatus())) {
            Integer claimStatus = lotteryLogExportVO.getClaimStatus();

            // 基础条件：奖品类型为5
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getPrizeType, 5);

            if (claimStatus.equals(3)) {
                // 查询状态为3的数据，或者状态为2且 update_time 比 create_time 大于24小时的数据
                logDOLambdaQueryWrapper.and(w ->
                        w.eq(LotteryLogDO::getClaimStatus, 3)
                                .or(w2 ->
                                        w2.eq(LotteryLogDO::getClaimStatus, 2)
                                                .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) > 24")
                                )
                );
            } else if (claimStatus.equals(2)) {
                // 查询状态为2且 update_time 比 create_time 小于24小时的数据
                logDOLambdaQueryWrapper.eq(LotteryLogDO::getClaimStatus, 2)
                        .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
            } else if (claimStatus.equals(1)) {
                // 查询状态为1的数据
                logDOLambdaQueryWrapper.eq(LotteryLogDO::getClaimStatus, 1);
            }
        }


        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getMemberName())) {
            logDOLambdaQueryWrapper
                    .nested(wrapper -> wrapper
                            .like(LotteryLogDO::getMemberName, lotteryLogExportVO.getMemberName())
                            .or()
                            .like(LotteryLogDO::getMemberMobile, lotteryLogExportVO.getMemberName())
                    );
        }

        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getLotteryId())) {
            logDOLambdaQueryWrapper.and(wrapper ->
                    wrapper.eq(LotteryLogDO::getLotteryId, lotteryLogExportVO.getLotteryId())
                            .or()
                            .eq(LotteryLogDO::getLotteryId, lotterySettingsDO.getActivityId())
            );
        }
        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getPrizeType())) {
            logDOLambdaQueryWrapper.eq(LotteryLogDO::getPrizeType, lotteryLogExportVO.getPrizeType());
        }
        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getLotteryStartDate())) {
            Date startDate = convertStringToDate(lotteryLogExportVO.getLotteryStartDate());
            logDOLambdaQueryWrapper.ge(LotteryLogDO::getCreateTime, startDate);
        }
        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getLotteryEndDate())) {
            Date endDate = convertStringToDate(lotteryLogExportVO.getLotteryEndDate());
            logDOLambdaQueryWrapper.le(LotteryLogDO::getCreateTime, endDate);
        }
        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getReceiveAddressType())) {
            if (lotteryLogExportVO.getReceiveAddressType().equals(0)) {
                logDOLambdaQueryWrapper.isNull(LotteryLogDO::getReceiveAddress);
            } else if (lotteryLogExportVO.getReceiveAddressType().equals(1)) {
                logDOLambdaQueryWrapper.isNotNull(LotteryLogDO::getReceiveAddress);
            }
        }
        if (ObjectUtil.isNotEmpty(lotteryLogExportVO.getTrackingType())) {
            if (lotteryLogExportVO.getTrackingType().equals(0)) {
                logDOLambdaQueryWrapper.isNull(LotteryLogDO::getTrackingNumber);
            } else if (lotteryLogExportVO.getTrackingType().equals(1)) {
                logDOLambdaQueryWrapper.isNotNull(LotteryLogDO::getTrackingNumber);
            }
        }

        logDOLambdaQueryWrapper.eq(LotteryLogDO::getBusinessId, businessId);


        return logDOLambdaQueryWrapper;
    }


    public Date convertStringToDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式错误: " + dateStr);
        }
    }

    @Override
    public Map<String, Object> getLotteryLogDailyAnalysis(LotteryLogEventReqVO lotteryLogEventReqVO) {

        Long businessId = BusinessContextHolder.getBusinessId();

        Map<String, Map<String, Long>> result = new HashMap<>();

        // 获取抽奖活动开始时间
        LocalDateTime startTime = lotteryLogEventReqVO.getStartTime();
        LocalDateTime endTime = lotteryLogEventReqVO.getEndTime();
        String id = lotteryLogEventReqVO.getId();

        // 若startTime与endTime均为空 默认最近30天
        if (startTime == null && endTime == null) {
            // 结束时间：当前时间的23:59:59
            endTime = LocalDateTime.now()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 忽略纳秒

            // 开始时间：30天前的00:00:00
            startTime = endTime.minusDays(29)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            if (startTime.isAfter(endTime)) {
                throw exception(ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_ERROR);
            }

            // 3. 计算两个时间之间的完整天数差（忽略时分秒，按日期计算）
            // 例如：2025-09-25 23:59:59 到 2025-09-26 00:00:00 算1天
            long daysDiff = ChronoUnit.DAYS.between(startTime, endTime) + 1;
            if (daysDiff > 180) {
                throw exception(ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_OUT_ERROR);
            }
        }

        buildLuckyDrawDailyResult(result, id, startTime, endTime, businessId);

        return convertStatsMapToArrays(result);
    }


    @Override
    @DS(DsNameConstants.SHARDING)
    public Map<String, Object> getLotteryLogAnalysis(String id) {

        Long businessId = BusinessContextHolder.getBusinessId();

        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Long> result = new HashMap<>();
        LotterySettingsDO lotterySettingsDO = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper.eq(LotterySettingsDO::getId, Long.valueOf(id))
                        .or()
                        .eq(LotterySettingsDO::getActivityId, Long.valueOf(id))));

        // 获取抽奖活动开始时间
        Date lotteryStartTime = lotterySettingsDO.getLotteryStartTime();
        Date lotteryEndTime = lotterySettingsDO.getLotteryEndTime();
        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }

        LambdaQueryWrapper<LotteryLogDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.select(LotteryLogDO::getMemberId);
        lambdaQueryWrapper.and(wrapper -> wrapper.eq(LotteryLogDO::getLotteryId, lotterySettingsDO.getId())
                        .or()
                        .eq(LotteryLogDO::getLotteryId, lotterySettingsDO.getActivityId()))
                .ge(LotteryLogDO::getCreateTime, start)
                .le(LotteryLogDO::getCreateTime, end);


        List<Long> sum = new ArrayList<>();
        ResultHandler<Long> resultHandler = new ResultHandler<Long>() {
            @Override
            public void handleResult(ResultContext<? extends Long> resultContext) {
                // 获取当前行的结果（即 memberId）
                Long memberId = resultContext.getResultObject();
                // 过滤 null 值并添加到集合
                if (Objects.nonNull(memberId)) {
                    sum.add(memberId);
                }
            }
        };
        lotteryLogMapper.selectObjs(lambdaQueryWrapper, resultHandler);

        Set<Long> distinctSum = new HashSet<>(sum);

        result.put("sum", (long) sum.size());
        result.put("distinctSum", (long) distinctSum.size());

        long consumePoints = 0L;

        // 计算抽奖活动消耗积分
        QueryWrapperX<LotteryLogDO> sumWrapper = new QueryWrapperX<>();

        sumWrapper.select("SUM(price) as consumePoints");
        sumWrapper.and(wrapper -> wrapper.eq("lottery_id", lotterySettingsDO.getId())
                        .or()
                        .eq("lottery_id", lotterySettingsDO.getActivityId()))
                .ge("create_time", start)
                .le("create_time", end);

        List<Object> objects = lotteryLogMapper.selectObjs(sumWrapper);
        if (objects != null) {
            Object o = objects.get(0);
            if (o != null) {
                consumePoints = ((BigDecimal) o).longValue();
            }
        }

        result.put("consumePoints", consumePoints);
        // 3. 统计优惠券发放张数（prizeType=1）
        LambdaQueryWrapper<LotteryLogDO> couponWrapper = new LambdaQueryWrapper<>();
        couponWrapper.and(wrapper -> wrapper.eq(LotteryLogDO::getLotteryId, lotterySettingsDO.getId())
                        .or()
                        .eq(LotteryLogDO::getLotteryId, lotterySettingsDO.getActivityId()))
                .ge(LotteryLogDO::getCreateTime, start)
                .le(LotteryLogDO::getCreateTime, end)
                .eq(LotteryLogDO::getPrizeType, 1);
        long couponCount = lotteryLogMapper.selectCount(couponWrapper);
        result.put("couponCount", couponCount);
        // 4. 统计现金发放总金额（prizeType=5）
        QueryWrapperX<LotteryLogDO> cashWrapper = new QueryWrapperX<>();
        cashWrapper.select("SUM(prize_value) as totalCash");
        cashWrapper.and(wrapper -> wrapper.eq("lottery_id", lotterySettingsDO.getId())
                        .or()
                        .eq("lottery_id", lotterySettingsDO.getActivityId()))
                .ge("create_time", start)
                .le("create_time", end)
                .eq("claim_status", 2)
                .eq("prize_type", 5)
                .apply("TIMESTAMPDIFF(HOUR, create_time, update_time) < 24");
        List<Object> cashObjects = lotteryLogMapper.selectObjs(cashWrapper);
        BigDecimal totalCash = new BigDecimal(0);
        if (cashObjects != null && !cashObjects.isEmpty() && cashObjects.get(0) != null) {
            totalCash = (BigDecimal) cashObjects.get(0);
        }
        // ES统计总浏览量 总访客量
        buildLuckyDrawResult(result, id, lotteryStartTime, lotteryEndTime, businessId);
        // ES统计分享人数
        buildShareResult(result, id, lotteryStartTime, lotteryEndTime, businessId);
        resultObj.putAll(result);
        resultObj.put("totalCash", totalCash);
        return resultObj;
    }

    /**
     * 处理转账回调
     */
    @Transactional
    @DS(DsNameConstants.SHARDING)
    public boolean processTransferNotify(TransferNotify notifyData) {
        try {
            String outBillNo = notifyData.getOut_bill_no();
            String eventType = notifyData.getEvent_type();
            String state = notifyData.getState();
            String failReason = notifyData.getFail_reason();
            LambdaQueryWrapper<LotteryTransferRecordDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(LotteryTransferRecordDO::getOutBillNo, outBillNo);

            List<LotteryTransferRecordDO> lotteryTransferRecordDOS = lotteryTransferRecordMapper.selectList(lambdaQueryWrapper);

            if (ObjectUtil.isEmpty(lotteryTransferRecordDOS)) {
                log.warn("未找到对应的转账记录: outBillNo={}", outBillNo);
                return false;
            }

            LotteryTransferRecordDO record = lotteryTransferRecordDOS.get(0);
            ParseResult parseResult = parseOriginalActivityId(outBillNo);

            switch (notifyData.getEvent_type()) {
                case "TRANSFER.SUCCESS":
                    record.setStatus(LotteryTransferStatus.SUCCESS);
                    log.info("转账成功: outBillNo={}, amount={}", outBillNo, record.getTransferAmount());
                    break;

                case "TRANSFER.FAILED":
                    record.setStatus(LotteryTransferStatus.FAIL);
                    record.setFailReason(notifyData.getFail_reason());
                    log.warn("转账失败: outBillNo={}, reason={}", outBillNo, notifyData.getFail_reason());
                    break;
                case "MCHTRANSFER.BILL.FINISHED":
                    record.setStatus(LotteryTransferStatus.SUCCESS);
                    log.info("MCHTRANSFER.BILL.FINISHED,转账成功: outBillNo={}, amount={}", outBillNo, record.getTransferAmount());
                    break;

                default:
                    log.info("收到转账状态更新: outBillNo={}, eventType={}", outBillNo, notifyData.getEvent_type());
                    break;
            }

            lotteryTransferRecordMapper.updateById(record);
            if (parseResult.isSuccess()) {
                Long activityId = parseResult.getActivityId();
                log.info("本次活动id是========{}", activityId);
                ActivityDO activityDO = activityMapper.selectById(activityId);

                log.info("查询出来的对象是-------{}", activityDO);
                if (activityDO == null) {

                    // 1. 更新 ActivityCqLogDO
                    updateActivityCqLog(outBillNo);

                    // 2. 更新 LotteryLogDO
                    updateLotteryLog(outBillNo);

                    // 3. 更新 ActivityJkPrizeExchangeDO
                    updateActivityJkPrizeExchange(outBillNo);

                    updateActivityVoteRewardLogDO(outBillNo);
                    return true;
                }
                Integer activityType = activityDO.getActivityType();

                if (activityType.equals(7)) {
                    // 活动类型7：只更新 ActivityCqLogDO
                    LambdaQueryWrapper<ActivityCqLogDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ActivityCqLogDO::getOutBillNo, outBillNo);
                    wrapper.eq(ActivityCqLogDO::getPrizeType, 5);
                    wrapper.select(ActivityCqLogDO::getId, ActivityCqLogDO::getUpdateTime);
                    List<ActivityCqLogDO> list = activityCqLogMapper.selectList(wrapper);

                    if (ObjectUtil.isNotEmpty(list)) {
                        ActivityCqLogDO entity = list.get(0);
                        ActivityCqLogDO updateEntity = new ActivityCqLogDO();
                        updateEntity.setId(entity.getId());
                        updateEntity.setMemberId(entity.getMemberId());
                        updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
                        activityCqLogMapper.updateById(updateEntity);
                    }

                } else if (activityType.equals(6)) {
                    // 活动类型6：只更新 ActivityJkPrizeExchangeDO
                    LambdaQueryWrapper<ActivityJkPrizeExchangeDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ActivityJkPrizeExchangeDO::getOutBillNo, outBillNo);
                    wrapper.eq(ActivityJkPrizeExchangeDO::getPrizeType, 5);
                    wrapper.select(ActivityJkPrizeExchangeDO::getId, ActivityJkPrizeExchangeDO::getUpdateTime);
                    List<ActivityJkPrizeExchangeDO> list = activityJkPrizeExchangeMapper.selectList(wrapper);

                    if (ObjectUtil.isNotEmpty(list)) {
                        ActivityJkPrizeExchangeDO entity = list.get(0);
                        ActivityJkPrizeExchangeDO updateEntity = new ActivityJkPrizeExchangeDO();
                        updateEntity.setId(entity.getId());
                        updateEntity.setMemberId(entity.getMemberId());
                        updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
                        activityJkPrizeExchangeMapper.updateById(updateEntity);
                    }

                } else if (activityType.equals(3)) {
                    LambdaQueryWrapper<LotteryLogDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(LotteryLogDO::getOutBillNo, outBillNo);
                    wrapper.eq(LotteryLogDO::getPrizeType, 5);
                    wrapper.select(LotteryLogDO::getId, LotteryLogDO::getUpdateTime);
                    List<LotteryLogDO> list = lotteryLogMapper.selectList(wrapper);

                    if (ObjectUtil.isNotEmpty(list)) {
                        LotteryLogDO entity = list.get(0);
                        LotteryLogDO updateEntity = new LotteryLogDO();
                        updateEntity.setId(entity.getId());
                        updateEntity.setMemberId(entity.getMemberId());
                        updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
                        lotteryLogMapper.updateById(updateEntity);
                        return true;
                    }

                } else if (activityType.equals(8)) {
                    Boolean b = activitySignService.updateSignRedPacketClaimStatusByOutBillNo(outBillNo, failReason);

                    return b;
                } else if (activityType.equals(9)) {
                    LambdaQueryWrapper<ActivityAnswerRewardLogDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ActivityAnswerRewardLogDO::getOutBillNo, outBillNo);
                    wrapper.eq(ActivityAnswerRewardLogDO::getPrizeType, 5);
                    wrapper.select(ActivityAnswerRewardLogDO::getId, ActivityAnswerRewardLogDO::getUpdateTime);
                    List<ActivityAnswerRewardLogDO> list = activityAnswerRewardLogMapper.selectList(wrapper);

                    if (ObjectUtil.isNotEmpty(list)) {
                        ActivityAnswerRewardLogDO entity = list.get(0);
                        ActivityAnswerRewardLogDO updateEntity = new ActivityAnswerRewardLogDO();
                        updateEntity.setId(entity.getId());
                        updateEntity.setMemberMobile(entity.getMemberMobile());
                        updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
                        activityAnswerRewardLogMapper.updateById(updateEntity);
                        return true;
                    }

                } else if (activityType.equals(10)) {
                    // 投票活动：更新 ActivityVoteRewardLogDO 的 claimStatus
                    LambdaQueryWrapper<ActivityVoteRewardLogDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ActivityVoteRewardLogDO::getOutBillNo, outBillNo);
                    wrapper.eq(ActivityVoteRewardLogDO::getPrizeType, 5);
                    wrapper.select(ActivityVoteRewardLogDO::getId, ActivityVoteRewardLogDO::getUpdateTime);
                    List<ActivityVoteRewardLogDO> list = activityVoteRewardLogMapper.selectList(wrapper);

                    if (ObjectUtil.isNotEmpty(list)) {
                        ActivityVoteRewardLogDO entity = list.get(0);
                        ActivityVoteRewardLogDO updateEntity = new ActivityVoteRewardLogDO();
                        updateEntity.setId(entity.getId());
                        updateEntity.setMemberMobile(entity.getMemberMobile());
                        updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
                        activityVoteRewardLogMapper.updateById(updateEntity);
                    }
                } else {
                    // 其他活动类型：更新三张表
                    // 1. 更新 ActivityCqLogDO
                    updateActivityCqLog(outBillNo);

                    // 2. 更新 LotteryLogDO
                    updateLotteryLog(outBillNo);

                    // 3. 更新 ActivityJkPrizeExchangeDO
                    updateActivityJkPrizeExchange(outBillNo);

                    updateActivityAnswerRewardLog(outBillNo);

                    updateActivityVoteRewardLogDO(outBillNo);
                }

            } else {
//                // 解析失败的情况
//                // 优先更新 LotteryLogDO，如果没有则更新其他两张表
//                if (!updateLotteryLog(outBillNo)) {
//                    updateActivityCqLog(outBillNo);
//                    updateActivityJkPrizeExchange(outBillNo);
//                }

                // 1. 更新 ActivityCqLogDO
                updateActivityCqLog(outBillNo);

                // 2. 更新 LotteryLogDO
                updateLotteryLog(outBillNo);

                // 3. 更新 ActivityJkPrizeExchangeDO
                updateActivityJkPrizeExchange(outBillNo);

                updateActivityVoteRewardLogDO(outBillNo);
            }
            return true;

        } catch (Exception e) {
            log.error("更新转账记录状态失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @DS(DsNameConstants.SHARDING)
    public LotteryLogDO selectLotteryId(Long lotteryId) {

        if (ObjectUtil.isEmpty(lotteryId)) {
            return null;
        }
        LotteryLogDO lotteryLogDO = lotteryLogMapper.selectById(lotteryId);
        return lotteryLogDO;
    }

    private void buildShareResult(Map<String, Long> result, String id, Date lotteryStartTime, Date lotteryEndTime, Long businessId) {

        // 抽奖设置主体
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, id)
                        .or()
                        .eq(LotterySettingsDO::getActivityId, id)
                )
        );

        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }
        Long activeCount = 0L;
        Long count = eventService.statUv(EventType.SHARE, id, start, end, businessId);
        if (lotterySettings != null) {
            activeCount = eventService.statUv(EventType.SHARE, lotterySettings.getActivityId().toString(), start, end, businessId);
        }


        result.put("shareCount", count + activeCount);
    }

    private void buildLuckyDrawResult(Map<String, Long> result, String id, Date lotteryStartTime, Date lotteryEndTime, Long businessId) {
        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }
        // 抽奖设置主体
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, id)
                        .or()
                        .eq(LotterySettingsDO::getActivityId, id)
                )
        );
        // 初始化时直接用空HashMap兜底，避免后续赋值null
        Map<String, Long> stringLongActivityMap = new HashMap<>();
        Map<String, Long> stringLongMap = new HashMap<>(); // 先初始化为空Map，防止null

        if (eventService != null) {
            stringLongMap = eventService.statPvUv(EventType.LUCKY_DRAW, id, start, end, businessId);
            if (stringLongMap == null) {
                stringLongMap = new HashMap<>();
            }
            if (lotterySettings != null && lotterySettings.getActivityId() != null) {
                String activityId = lotterySettings.getActivityId().toString();
                Map<String, Long> tempActivityMap = eventService.statPvUv(EventType.LUCKY_DRAW, activityId, start, end, businessId);
                stringLongActivityMap = tempActivityMap != null ? tempActivityMap : new HashMap<>();
            }
        }
        Map<String, Long> totalMap = getStringLongMap(stringLongMap, stringLongActivityMap);
        result.putAll(totalMap);
    }

    private static Map<String, Long> getStringLongMap(Map<String, Long> stringLongMap, Map<String, Long> stringLongActivityMap) {

        Map<String, Long> map1 = stringLongMap == null ? new HashMap<>() : stringLongMap;
        Map<String, Long> map2 = stringLongActivityMap == null ? new HashMap<>() : stringLongActivityMap;

        Map<String, Long> totalMap = new HashMap<>();
        // 遍历第一个Map，值为null则替换为0L
        for (Map.Entry<String, Long> entry : map1.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue() == null ? 0L : entry.getValue();
            totalMap.put(key, value);
        }

        // 遍历第二个Map，累加数值（Key不存在则取0L）
        for (Map.Entry<String, Long> entry : map2.entrySet()) {
            String key = entry.getKey();
            Long activityValue = entry.getValue() == null ? 0L : entry.getValue();
            Long totalValue = totalMap.getOrDefault(key, 0L) + activityValue;
            totalMap.put(key, totalValue);
        }
        return totalMap;
    }

    private void buildLuckyDrawDailyResult(Map<String, Map<String, Long>> result, String id, LocalDateTime start, LocalDateTime end, Long businessId) {
        // 抽奖设置主体
        LotterySettingsDO lotterySettings = lotterySettingsMapper.selectOne(new LambdaQueryWrapperX<LotterySettingsDO>()
                .and(wrapper -> wrapper
                        .eq(LotterySettingsDO::getId, id)
                        .or()
                        .eq(LotterySettingsDO::getActivityId, id)
                )
        );
        Map<String, Map<String, Long>> stringLongMap = eventService.statDailyPvUv(EventType.LUCKY_DRAW, id, start, end, businessId);
        if (stringLongMap == null) {
            stringLongMap = new HashMap<>();
        }
        if (lotterySettings != null && lotterySettings.getActivityId() != null) {
            // 关键修正：第二个查询应该用activityId而非原id，否则逻辑无意义
            Map<String, Map<String, Long>> stringActivityLongMap = eventService.statDailyPvUv(
                    EventType.LUCKY_DRAW,
                    lotterySettings.getActivityId().toString(),
                    start,
                    end,
                    businessId
            );

            if (stringActivityLongMap != null && !stringActivityLongMap.isEmpty()) {
                mergeNestedMap(stringLongMap, stringActivityLongMap);
            }
        }

        if (result == null) {
            result = new HashMap<>();
        }
        result.putAll(stringLongMap);
    }

    private void mergeNestedMap(Map<String, Map<String, Long>> targetMap, Map<String, Map<String, Long>> sourceMap) {
        for (Map.Entry<String, Map<String, Long>> sourceEntry : sourceMap.entrySet()) {
            String outerKey = sourceEntry.getKey();
            Map<String, Long> sourceInnerMap = sourceEntry.getValue();

            if (!targetMap.containsKey(outerKey)) {
                targetMap.put(outerKey, new HashMap<>(sourceInnerMap));
                continue;
            }

            Map<String, Long> targetInnerMap = targetMap.get(outerKey);
            if (targetInnerMap == null) {
                targetInnerMap = new HashMap<>();
                targetMap.put(outerKey, targetInnerMap);
            }

            for (Map.Entry<String, Long> innerEntry : sourceInnerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                Long sourceValue = innerEntry.getValue() == null ? 0L : innerEntry.getValue();

                targetInnerMap.put(
                        innerKey,
                        targetInnerMap.getOrDefault(innerKey, 0L) + sourceValue
                );
            }
        }
    }

    /**
     * Date 转 LocalDateTime（指定业务时区，避免时区误差）
     *
     * @param date 待转换的 Date 对象（null 时返回 null）
     * @return LocalDateTime 转换后的本地时间
     */
    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        if (date == null) {
            return null; // 处理 null，避免空指针
        }
        // 步骤：Date -> Instant -> ZonedDateTime（指定时区）-> LocalDateTime
        return Instant.ofEpochMilli(date.getTime())
                .atZone(BUSINESS_TIME_ZONE)
                .toLocalDateTime();
    }

    /**
     * 转换统计数据为包含数组的Map
     *
     * @param statsMap 原始统计数据，结构为:
     *                 外层Key: 日期(yyyy-MM-dd)
     *                 内层Map: Key为指标名("pv"或"uv")，Value为指标值
     * @return 转换后的Map，包含三个键:
     * - "times": 日期数组(String[])
     * - "pvValues": PV数值数组(Long[])
     * - "uvValues": UV数值数组(Long[])
     * @throws IllegalArgumentException 当原始数据为空或格式错误时抛出
     */
    public Map<String, Object> convertStatsMapToArrays(Map<String, Map<String, Long>> statsMap) {
        // 校验原始数据非空
        if (statsMap == null || statsMap.isEmpty()) {
            throw new IllegalArgumentException("原始统计数据 Map 不能为空");
        }

        // 提取外层日期并排序
        List<String> dateList = new ArrayList<>(statsMap.keySet());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期顺序排序
        dateList.sort((dateStr1, dateStr2) -> {
            try {
                LocalDate date1 = LocalDate.parse(dateStr1, dateFormatter);
                LocalDate date2 = LocalDate.parse(dateStr2, dateFormatter);
                return date1.compareTo(date2);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "日期格式错误，需为 yyyy-MM-dd：" + dateStr1 + " / " + dateStr2, e);
            }
        });

        // 初始化目标数组
        int dataSize = dateList.size();
        String[] dateArray = new String[dataSize];
        Long[] uvArray = new Long[dataSize];
        Long[] pvArray = new Long[dataSize];

        // 填充数组数据
        for (int i = 0; i < dataSize; i++) {
            String currentDate = dateList.get(i);
            Map<String, Long> innerMap = statsMap.get(currentDate);

            // 校验内层Map非空
            if (innerMap == null) {
                throw new IllegalArgumentException(
                        "日期 " + currentDate + " 对应的内层指标 Map 不能为空");
            }

            // 填充日期数组
            dateArray[i] = currentDate;

            // 填充UV数组，缺失时补0
            uvArray[i] = innerMap.getOrDefault("uv", 0L);

            // 填充PV数组，缺失时补0
            pvArray[i] = innerMap.getOrDefault("pv", 0L);
        }

        // 构建结果Map
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("times", dateArray);
        resultMap.put("pvValues", pvArray);
        resultMap.put("uvValues", uvArray);

        return resultMap;
    }

    private String convertPrizeTypeToName(Integer prizeType) {
        if (prizeType == null) {
            return "";
        }
        switch (prizeType) {
            case 1:
                return "优惠券";
            case 2:
                return "积分";
            case 3:
                return "实物";
            case 4:
                return "无奖品";
            case 5:
                return "现金红包";
            default:
                return "";
        }
    }

    // 解析原始ID
    private ParseResult parseOriginalActivityId(String outBillNo) {
        try {
            int separatorIndex = outBillNo.indexOf("A");
            if (separatorIndex > 0) {
                String encodedId = outBillNo.substring(0, separatorIndex);
                Long activityId = Long.parseLong(encodedId, 36);
                return new ParseResult(true, activityId);
            }
        } catch (Exception e) {
        }
        return new ParseResult(false, null);
    }

    public static void main(String[] args) {
        String outBillNo = "fol95mgo0x6pAmq7gu2rl";
        int separatorIndex = outBillNo.indexOf("A");
        if (separatorIndex > 0) {
            String encodedId = outBillNo.substring(0, separatorIndex);
            Long activityId = Long.parseLong(encodedId, 36);
            System.out.println("活动id" + activityId);
        } else {
            System.out.println("解析失败");
        }
    }

//    // 公共方法：根据更新时间判断状态
//    private Integer getClaimStatusByUpdateTime(LocalDateTime updateTime) {
//        if (updateTime == null) {
//            return 2; // 如果没有更新时间，默认返回2
//        }
//
//        LocalDateTime now = LocalDateTime.now();
//        long hoursBetween = ChronoUnit.HOURS.between(updateTime, now);
//
//        if (hoursBetween >= 24) {
//            return 3; // 超过24小时，状态设为3
//        } else {
//            return 2; // 小于24小时，状态设为2
//        }
//    }

    // 更精确的时间判断
    private Integer getClaimStatusByUpdateTime(LocalDateTime updateTime) {
        if (updateTime == null) {
            return 2;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);

        // 如果更新时间在24小时之前，返回3；否则返回2
        if (updateTime.isBefore(twentyFourHoursAgo)) {
            return 3; // 超过24小时
        } else {
            return 2; // 24小时内
        }
    }

    // 更新 ActivityCqLogDO
    private boolean updateActivityCqLog(String outBillNo) {
        LambdaQueryWrapper<ActivityCqLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityCqLogDO::getOutBillNo, outBillNo);
        wrapper.eq(ActivityCqLogDO::getPrizeType, 5);
        wrapper.select(ActivityCqLogDO::getId, ActivityCqLogDO::getUpdateTime);
        List<ActivityCqLogDO> list = activityCqLogMapper.selectList(wrapper);

        if (ObjectUtil.isNotEmpty(list)) {
            ActivityCqLogDO entity = list.get(0);
            ActivityCqLogDO updateEntity = new ActivityCqLogDO();
            updateEntity.setId(entity.getId());
            updateEntity.setMemberId(entity.getMemberId());
            updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
            activityCqLogMapper.updateById(updateEntity);
            return true;
        }
        return false;
    }

    // 更新 ActivityVoteRewardLogDO
    private boolean updateActivityVoteRewardLogDO(String outBillNo) {
        LambdaQueryWrapper<ActivityVoteRewardLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityVoteRewardLogDO::getOutBillNo, outBillNo);
        wrapper.eq(ActivityVoteRewardLogDO::getPrizeType, 5);
        wrapper.select(ActivityVoteRewardLogDO::getId, ActivityVoteRewardLogDO::getUpdateTime);
        List<ActivityVoteRewardLogDO> list = activityVoteRewardLogMapper.selectList(wrapper);

        if (ObjectUtil.isNotEmpty(list)) {
            ActivityVoteRewardLogDO entity = list.get(0);
            ActivityVoteRewardLogDO updateEntity = new ActivityVoteRewardLogDO();
            updateEntity.setId(entity.getId());
            updateEntity.setMemberMobile(entity.getMemberMobile());
            updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
            activityVoteRewardLogMapper.updateById(updateEntity);
            return true;
        }
        return false;
    }

    // 更新 LotteryLogDO
    private boolean updateLotteryLog(String outBillNo) {
        LambdaQueryWrapper<LotteryLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LotteryLogDO::getOutBillNo, outBillNo);
        wrapper.eq(LotteryLogDO::getPrizeType, 5);
        wrapper.select(LotteryLogDO::getId, LotteryLogDO::getUpdateTime);
        List<LotteryLogDO> list = lotteryLogMapper.selectList(wrapper);

        if (ObjectUtil.isNotEmpty(list)) {
            LotteryLogDO entity = list.get(0);
            LotteryLogDO updateEntity = new LotteryLogDO();
            updateEntity.setId(entity.getId());
            updateEntity.setMemberId(entity.getMemberId());
            updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
            lotteryLogMapper.updateById(updateEntity);
            return true;
        }
        return false;
    }

    // 更新 ActivityJkPrizeExchangeDO
    private boolean updateActivityJkPrizeExchange(String outBillNo) {
        LambdaQueryWrapper<ActivityJkPrizeExchangeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityJkPrizeExchangeDO::getOutBillNo, outBillNo);
        wrapper.eq(ActivityJkPrizeExchangeDO::getPrizeType, 5);
        wrapper.select(ActivityJkPrizeExchangeDO::getId, ActivityJkPrizeExchangeDO::getUpdateTime);
        List<ActivityJkPrizeExchangeDO> list = activityJkPrizeExchangeMapper.selectList(wrapper);

        if (ObjectUtil.isNotEmpty(list)) {
            ActivityJkPrizeExchangeDO entity = list.get(0);
            ActivityJkPrizeExchangeDO updateEntity = new ActivityJkPrizeExchangeDO();
            updateEntity.setId(entity.getId());
            updateEntity.setMemberId(entity.getMemberId());
            updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
            activityJkPrizeExchangeMapper.updateById(updateEntity);
            return true;
        }
        return false;
    }

    // 更新 ActivityJkPrizeExchangeDO
    private boolean updateActivityAnswerRewardLog(String outBillNo) {
        LambdaQueryWrapper<ActivityAnswerRewardLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityAnswerRewardLogDO::getOutBillNo, outBillNo);
        wrapper.eq(ActivityAnswerRewardLogDO::getPrizeType, 5);
        wrapper.select(ActivityAnswerRewardLogDO::getId, ActivityAnswerRewardLogDO::getUpdateTime);
        List<ActivityAnswerRewardLogDO> list = activityAnswerRewardLogMapper.selectList(wrapper);

        if (ObjectUtil.isNotEmpty(list)) {
            ActivityAnswerRewardLogDO entity = list.get(0);
            ActivityAnswerRewardLogDO updateEntity = new ActivityAnswerRewardLogDO();
            updateEntity.setId(entity.getId());
            updateEntity.setMemberMobile(entity.getMemberMobile());
            updateEntity.setClaimStatus(getClaimStatusByUpdateTime(entity.getUpdateTime()));
            activityAnswerRewardLogMapper.updateById(updateEntity);
            return true;
        }
        return false;
    }


}
