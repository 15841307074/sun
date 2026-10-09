package com.htyoudao.youdao.module.promotion.service.lottery.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.Points.PointsLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryPrizeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotterySettingsMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.usercoupon.UserCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.promotion.enums.LotteryStateEnum;
import com.htyoudao.youdao.module.promotion.enums.LotteryTypeEnum;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryAddLogService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryMobileService;
import com.htyoudao.youdao.module.promotion.util.DateUtils;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Service
@Slf4j
@DS(DsNameConstants.SHARDING)
public class LotteryAddLogServiceImpl implements LotteryAddLogService {

    @Resource
    private LotteryLogMapper lotteryLogMapper;
    @Resource
    private PointsLogMapper pointsLogMapper;
    @Resource
    private GoodCouponMapper goodCouponMapper;
    @Resource
    private UserCouponMapper userCouponMapper;

    /**
     * 添加抽奖记录
     */
    @Override
    public void addLotteryLog(LotteryLogDO log) {
        log.setBusinessId(BusinessContextHolder.getBusinessId());
        lotteryLogMapper.insert(log);

    }

    /**
     * 添加积分记录
     */
    @Override
    public void addPointsLog(PointsLogDO log) {
        log.setBusinessId(BusinessContextHolder.getBusinessId());
        pointsLogMapper.insert(log);

    }

    /**
     * 查询抽奖次数
     */
    @Override
    public int selectLotteryNum(Long memberId, Long lotteryId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        return lotteryLogMapper.selectCount(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, memberId)
                .eq(LotteryLogDO::getLotteryId, lotteryId)
                .ge(LotteryLogDO::getCreateTime, startOfDay)
                .lt(LotteryLogDO::getCreateTime, endOfDay)).intValue();

    }
    /**
     * 按场次查询抽奖次数（在指定时间段内的抽奖记录）
     * @param memberId 用户ID
     * @param lotteryId 抽奖活动ID
     * @param startTime 场次开始时间（当天的LocalTime）
     * @param endTime 场次结束时间（当天的LocalTime）
     * @return 该场次内的抽奖次数
     */
    @Override
    public int selectLotteryNumBySession(Long memberId, Long lotteryId, LocalTime startTime, LocalTime endTime) {
        // 1. 构建场次的完整时间区间（结合当天日期）
        LocalDate today = LocalDate.now();
        LocalDateTime sessionStart = LocalDateTime.of(today, startTime);
        LocalDateTime sessionEnd = LocalDateTime.of(today, endTime);

        // 2. 特殊处理：若场次结束时间是23:59:59（对应原始24:00），确保覆盖到当天最后一刻
        if (endTime.equals(LocalTime.of(23, 59, 59))) {
            sessionEnd = sessionEnd.plusSeconds(1).minusNanos(1); // 转为23:59:59.999999999
        }

        // 3. 查询该场次时间区间内的抽奖记录数
        return lotteryLogMapper.selectCount(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, memberId)
                .eq(LotteryLogDO::getLotteryId, lotteryId)
                .ge(LotteryLogDO::getCreateTime, sessionStart) // 大于等于场次开始时间
                .lt(LotteryLogDO::getCreateTime, sessionEnd)   // 小于场次结束时间（左闭右开）
        ).intValue();
    }
    /**
     * 查询总抽奖次数
     */
    @Override
    public int selectLotterySum(Long memberId, Long lotteryId) {
        return lotteryLogMapper.selectCount(new LambdaQueryWrapperX<LotteryLogDO>()
                .eq(LotteryLogDO::getMemberId, memberId)
                .eq(LotteryLogDO::getLotteryId, lotteryId)
        ).intValue();

    }

    /**
     * 查询优惠卷
     */
    @Override
    public GoodCouponDO getGoodCoupon(Long awardId) {
        return goodCouponMapper.selectById(awardId);
    }

    /**
     * 添加优惠卷
     */
    public void addCoupon(UserCouponDO userCouponDO) {
        userCouponDO.setBusinessId(BusinessContextHolder.getBusinessId());
        userCouponMapper.insert(userCouponDO);
    }
}
