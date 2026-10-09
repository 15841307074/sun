package com.htyoudao.youdao.module.errand.service.errandRunnerBalanceLog;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ErrandRunnerBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ErrandRunnerBalanceLogRespVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.RunnerDetailRespVO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog.ErrandRunnerBalanceLogDO;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunner.ErrandRunnerMapper;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerBalanceLog.ErrandRunnerBalanceLogMapper;
import com.htyoudao.youdao.module.errand.enums.errandRunnerBalanceLog.DirectionEnum;
import com.htyoudao.youdao.module.errand.enums.errandRunnerBalanceLog.FlowTypeEnum;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * 跑腿员余额流水服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class ErrandRunnerBalanceLogServiceImpl
        extends ServiceImpl<ErrandRunnerBalanceLogMapper, ErrandRunnerBalanceLogDO>
        implements ErrandRunnerBalanceLogService {

    private final ErrandRunnerBalanceLogMapper balanceLogMapper;

    @Resource
    private ErrandRunnerMapper runnerMapper;

    @Override
    public List<ErrandRunnerBalanceLogDO> getByRunnerId(Long runnerId) {
        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runnerId)
                .orderByDesc(ErrandRunnerBalanceLogDO::getCreateTime);
        return list(wrapper);
    }

    @Override
    public ErrandRunnerBalanceLogDO getByBizNo(String bizNo) {
        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getBizNo, bizNo);
        return getOne(wrapper, false);
    }

    @Override
    public BigDecimal getTotalIncome(Long runnerId) {
        return balanceLogMapper.sumIncomeByRunnerId(runnerId);
    }

    @Override
    public BigDecimal getTotalWithdraw(Long runnerId) {
        return balanceLogMapper.sumWithdrawByRunnerId(runnerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createBalanceLog(ErrandRunnerBalanceLogDO balanceLog) {
        // 防重校验
        if (balanceLog.getBizNo() != null && !balanceLog.getBizNo().isEmpty()) {
            ErrandRunnerBalanceLogDO existLog = getByBizNo(balanceLog.getBizNo());
            if (existLog != null) {
                log.warn("余额流水已存在，bizNo: {}", balanceLog.getBizNo());
                return false;
            }
        }

        balanceLog.setCreateTime(LocalDateTime.now());
        return save(balanceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createIncomeLog(Long runnerId, Long runnerMemberId, String orderSn,
                                   BigDecimal amount, BigDecimal beforeBalance,
                                   BigDecimal afterBalance, String bizNo) {
        ErrandRunnerBalanceLogDO balanceLog = ErrandRunnerBalanceLogDO.builder()
                .runnerId(runnerId)
                .runnerMemberId(runnerMemberId)
                .orderSn(orderSn)
                .flowType(FlowTypeEnum.INCOME.getCode())
                .direction(DirectionEnum.INCOME.getCode())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .bizNo(bizNo)
                .remark("赏金入账")
                .build();

        log.info("创建赏金入账流水，runnerId: {}, amount: {}, bizNo: {}", runnerId, amount, bizNo);
        return createBalanceLog(balanceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createWithdrawLog(Long runnerId, Long runnerMemberId, Long withdrawId,
                                     BigDecimal amount, BigDecimal beforeBalance,
                                     BigDecimal afterBalance, String bizNo) {
        ErrandRunnerBalanceLogDO balanceLog = ErrandRunnerBalanceLogDO.builder()
                .runnerId(runnerId)
                .runnerMemberId(runnerMemberId)
                .withdrawId(withdrawId)
                .flowType(FlowTypeEnum.WITHDRAW.getCode())
                .direction(DirectionEnum.EXPENDITURE.getCode())
                .amount(amount)
                .beforeBalance(beforeBalance)
                .afterBalance(afterBalance)
                .bizNo(bizNo)
                .remark("提现扣减")
                .build();

        log.info("创建提现扣减流水，runnerId: {}, amount: {}, bizNo: {}", runnerId, amount, bizNo);
        return createBalanceLog(balanceLog);
    }


    @Override
    public BigDecimal getCurrentBalance(Long runnerId) {
        // 查询最新的一条流水记录，获取变动后余额
        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runnerId)
                .orderByDesc(ErrandRunnerBalanceLogDO::getCreateTime)
                .last("LIMIT 1");
        ErrandRunnerBalanceLogDO latestLog = getOne(wrapper, false);
        if (latestLog != null) {
            return latestLog.getAfterBalance();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTodayIncome(Long runnerId) {
        // 获取今日开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date todayStart = calendar.getTime();

        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runnerId)
                .eq(ErrandRunnerBalanceLogDO::getDirection, 1) // 收入
                .ge(ErrandRunnerBalanceLogDO::getCreateTime, todayStart);

        BigDecimal todayIncome = list(wrapper).stream()
                .map(ErrandRunnerBalanceLogDO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return todayIncome;
    }

    @Override
    public BigDecimal getWeekIncome(Long runnerId) {
        // 获取本周开始时间（周一）
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date weekStart = calendar.getTime();

        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runnerId)
                .eq(ErrandRunnerBalanceLogDO::getDirection, 1) // 收入
                .ge(ErrandRunnerBalanceLogDO::getCreateTime, weekStart);

        BigDecimal weekIncome = list(wrapper).stream()
                .map(ErrandRunnerBalanceLogDO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weekIncome;
    }

    @Override
    public BigDecimal getMonthIncome(Long runnerId) {
        // 获取本月开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date monthStart = calendar.getTime();

        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runnerId)
                .eq(ErrandRunnerBalanceLogDO::getDirection, 1) // 收入
                .ge(ErrandRunnerBalanceLogDO::getCreateTime, monthStart);

        BigDecimal monthIncome = list(wrapper).stream()
                .map(ErrandRunnerBalanceLogDO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return monthIncome;
    }

    @Override
    public PageResult<ErrandRunnerBalanceLogRespVO> selectByMemberId(ErrandRunnerBalanceLogReqVO reqVO) {
        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErrandRunnerBalanceLogDO::getRunnerMemberId, reqVO.getMemberId());
        queryWrapper.in(ErrandRunnerBalanceLogDO::getFlowType, List.of(1, 2, 3, 4, 5));
        queryWrapper.orderByDesc(ErrandRunnerBalanceLogDO::getCreateTime);
        Page<ErrandRunnerBalanceLogDO> page = page(new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), queryWrapper);
        PageResult<ErrandRunnerBalanceLogRespVO> pageResult = PageResult.empty(page.getTotal());
        List<ErrandRunnerBalanceLogRespVO> bean = BeanUtils.toBean(page.getRecords(), ErrandRunnerBalanceLogRespVO.class);
        for (ErrandRunnerBalanceLogRespVO errandRunnerBalanceLogRespVO : bean) {
            errandRunnerBalanceLogRespVO.setWithdrawable(errandRunnerBalanceLogRespVO.getBalance().subtract(errandRunnerBalanceLogRespVO.getFrozenBalance()));
        }
        pageResult.setList(bean);
        return pageResult;
    }

    @Override
    public RunnerDetailRespVO getRunnerDetailByMemberId(Long memberId) {
        LambdaQueryWrapper<ErrandRunnerDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErrandRunnerDO::getMemberId, memberId);
        ErrandRunnerDO runner = runnerMapper.selectOne(queryWrapper);
        RunnerDetailRespVO runnerDetail = new RunnerDetailRespVO();
        if(ObjectUtil.isEmpty(runner)){
            return runnerDetail;

        }
        runnerDetail.setBalance(runner.getBalance());
        runnerDetail.setAvailableBalance(runner.getBalance().subtract(runner.getFrozenBalance()));
        return runnerDetail;
    }
}
