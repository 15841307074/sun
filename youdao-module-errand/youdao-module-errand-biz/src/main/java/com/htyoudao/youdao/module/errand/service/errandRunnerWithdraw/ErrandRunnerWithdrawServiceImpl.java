package com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ExportBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerWithdraw.vo.ErrandRunnerWithdrawExcelVO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog.ErrandRunnerBalanceLogDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw.ErrandRunnerWithdrawDO;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunner.ErrandRunnerMapper;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerBalanceLog.ErrandRunnerBalanceLogMapper;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerWithdraw.ErrandRunnerWithdrawMapper;
import com.htyoudao.youdao.module.errand.enums.errandRunnerWithdraw.WithdrawStatusEnum;
import com.htyoudao.youdao.module.errand.service.errandRunner.ErrandRunnerService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.errand.api.enums.ErrorCodeConstants.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.errand.api.enums.ErrorCodeConstants.WITHDRAW_EXPORT_TOO_MANY;

/**
 * 跑腿员提现服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ErrandRunnerWithdrawServiceImpl
        extends ServiceImpl<ErrandRunnerWithdrawMapper, ErrandRunnerWithdrawDO>
        implements ErrandRunnerWithdrawService {

    @Autowired
    private ErrandRunnerWithdrawMapper withdrawMapper;

    @Resource
    private ExcelActionService excelActionService;

    @Resource
    private ErrandRunnerService runnerService;

    @Resource
    private RedissonClient redissonClient;

    @DubboReference
    private StoreApi storeApi;
    @Resource
    private ErrandRunnerMapper errandRunnerMapper;
    @Resource
    private ErrandRunnerBalanceLogMapper errandRunnerBalanceLogMapper;
    @Resource
    private WechatTransferService wechatTransferService;

    /**
     * 提现状态：提现中
     */
    private static final Integer WITHDRAW_STATUS_PROCESSING = 1;
    /**
     * 提现状态：已提现
     */
    private static final Integer WITHDRAW_STATUS_SUCCESS = 2;
    /**
     * 提现状态：提现失败
     */
    private static final Integer WITHDRAW_STATUS_FAIL = 3;

    /**
     * 流水类型：提现扣减
     */
    private static final Integer FLOW_TYPE_WITHDRAW = 2;
    /**
     * 流水类型：提现失败退回
     */
    private static final Integer FLOW_TYPE_WITHDRAW_FAIL_BACK = 4;

    /**
     * 方向：支出
     */
    private static final Integer DIRECTION_OUT = 2;
    /**
     * 方向：收入
     */
    private static final Integer DIRECTION_IN = 1;

    @Override
    public ErrandRunnerWithdrawDO getByWithdrawSn(String withdrawSn) {
        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerWithdrawDO::getWithdrawSn, withdrawSn);
        return getOne(wrapper, false);
    }

    @Override
    public List<ErrandRunnerWithdrawDO> getByRunnerId(Long runnerId) {
        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerWithdrawDO::getRunnerId, runnerId)
                .orderByDesc(ErrandRunnerWithdrawDO::getCreateTime);
        return list(wrapper);
    }

    @Override
    public BigDecimal getTotalSuccessAmount(Long runnerId) {
        return withdrawMapper.sumSuccessAmountByRunnerId(runnerId);
    }

    @Override
    public BigDecimal getTotalServiceFee(Long runnerId) {
        return withdrawMapper.sumServiceFeeByRunnerId(runnerId);
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleWithdrawSuccess(Long withdrawId, String wechatBatchNo, String wechatDetailNo) {
        int result = withdrawMapper.updateToSuccess(withdrawId, wechatBatchNo, wechatDetailNo);
        if (result > 0) {
            log.info("提现成功处理，withdrawId: {}, wechatBatchNo: {}", withdrawId, wechatBatchNo);
            return true;
        }
        log.warn("提现成功处理失败，withdrawId: {}", withdrawId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleWithdrawFail(Long withdrawId, String failReason) {
        int result = withdrawMapper.updateToFail(withdrawId, failReason);
        if (result > 0) {
            log.info("提现失败处理，withdrawId: {}, failReason: {}", withdrawId, failReason);
            return true;
        }
        log.warn("提现失败处理失败，withdrawId: {}", withdrawId);
        return false;
    }

    @Override
    public List<ErrandRunnerWithdrawDO> getPendingWithdrawList() {
        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ErrandRunnerWithdrawDO::getStatus,
                        WithdrawStatusEnum.PENDING.getCode(),
                        WithdrawStatusEnum.PROCESSING.getCode())
                .orderByAsc(ErrandRunnerWithdrawDO::getApplyTime);
        return list(wrapper);
    }

    @Override
    public BigDecimal getTodayWithdrawAmount(Long runnerId) {
        // 获取今日开始时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Date startDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());

        // 获取今日结束时间
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Date endDate = Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerWithdrawDO::getRunnerId, runnerId)
                .eq(ErrandRunnerWithdrawDO::getStatus, WithdrawStatusEnum.SUCCESS.getCode())
                .between(ErrandRunnerWithdrawDO::getCreateTime, startDate, endDate);

        List<ErrandRunnerWithdrawDO> list = list(wrapper);
        return list.stream()
                .map(ErrandRunnerWithdrawDO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

//    /**
//     * 生成提现单号
//     * 格式：WD + yyyyMMddHHmmss + 随机数
//     */
//    private String generateWithdrawSn() {
//        return "WD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//    }

    @Override
    public Void exportErrandList(ExportBalanceLogReqVO reqVO) {
        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ErrandRunnerWithdrawDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime());
        Long l = withdrawMapper.selectCount(wrapper);
        if(l > 300000){
            throw exception(WITHDRAW_EXPORT_TOO_MANY);
        }
        Page<ErrandRunnerWithdrawDO> page = new Page<>(1, 5000);
        excelActionService.exportAsyncExcel(ErrandRunnerWithdrawExcelVO.class,page, param -> this.queryUserCouponData(page,reqVO), "提现数据");
        return null;
    }

    private List<ErrandRunnerWithdrawExcelVO> queryUserCouponData(Page<ErrandRunnerWithdrawDO> page, ExportBalanceLogReqVO reqVO) {
        List<ErrandRunnerWithdrawExcelVO> list = new ArrayList<>();
        ErrandRunnerWithdrawExcelVO vo = new ErrandRunnerWithdrawExcelVO();

        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(ErrandRunnerWithdrawDO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime());
        Page<ErrandRunnerWithdrawDO> errandRunnerWithdrawDOPage = withdrawMapper.selectPage(page, wrapper);
        List<ErrandRunnerWithdrawDO> records = errandRunnerWithdrawDOPage.getRecords();
        if(CollectionUtil.isNotEmpty(records)){
            List<Long> runnerIds = records.stream().map(ErrandRunnerWithdrawDO::getRunnerId).toList();
            LambdaQueryWrapper<ErrandRunnerDO> runnerWrapper = new LambdaQueryWrapper<>();
            runnerWrapper.select(ErrandRunnerDO::getId, ErrandRunnerDO::getPhone,ErrandRunnerDO::getStoreId,ErrandRunnerDO::getStudentNo,ErrandRunnerDO::getName);
            runnerWrapper.in(ErrandRunnerDO::getId, runnerIds);
            List<ErrandRunnerDO> runners = runnerService.list(runnerWrapper);
            Map<Long, ErrandRunnerDO> runnerMap = runners.stream()
                    .collect(Collectors.toMap(ErrandRunnerDO::getId, Function.identity()));

            List<Long> storeIds = runners.stream().map(ErrandRunnerDO::getStoreId).toList();
            CommonResult<List<StoreInfoDTO>> storesByStoreIds = storeApi.getStoresByStoreIds(storeIds);
            List<StoreInfoDTO> stores = storesByStoreIds.getData();
            Map<Long, StoreInfoDTO> storeInfoDTOMap = stores.stream()
                    .collect(Collectors.toMap(StoreInfoDTO::getStoreId, Function.identity()));

            for (ErrandRunnerWithdrawDO record : records) {
                vo = new ErrandRunnerWithdrawExcelVO();
                Long runnerId = record.getRunnerId();
                ErrandRunnerDO runner = runnerMap.get(runnerId);

                vo.setName(runner.getName());
                vo.setPhone(runner.getPhone());
                vo.setStudentNo(runner.getStudentNo());

                StoreInfoDTO storeInfoDTO = storeInfoDTOMap.get(runner.getStoreId());
                vo.setStoreName(storeInfoDTO.getStoreName());

                vo.setFinishTime(record.getFinishTime());
                vo.setStatus(record.getStatus());
                vo.setAmount(record.getAmount());
                vo.setBalance(record.getBalance());
                list.add(vo);
            }
        }
        return list;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ErrandRunnerWithdrawDO applyWithdraw(Long memberId, BigDecimal amount, String openId) {
        log.info("=== 开始提现申请 ===");
        log.info("请求参数: memberId={}, amount={}, openId={}", memberId, amount, openId);

        // 1. 参数校验
        if (memberId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("提现参数校验失败: memberId={}, amount={}", memberId, amount);
            throw exception(1200, "参数错误");
        }

        String lockKey = "withdraw:member:" + memberId;
        RLock lock = redissonClient.getLock(lockKey);
        log.info("尝试获取分布式锁: lockKey={}", lockKey);

        try {
            // 尝试获取锁，最多等待3秒，锁持有时间10秒（防止死锁）
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("获取分布式锁失败: memberId={}", memberId);
                throw exception(1300, "请稍后操作");
            }
            log.info("获取分布式锁成功: memberId={}", memberId);

            // 2. 获取跑腿员信息（锁内无需再用数据库行锁，分布式锁已保证并发安全）
            log.info("查询跑腿员信息: memberId={}", memberId);
            ErrandRunnerDO runner = errandRunnerMapper.selectByMemberId(memberId);
            if (runner == null) {
                log.error("跑腿员不存在: memberId={}", memberId);
                throw exception(ERRAND_RUNNER_NOT_EXISTS.getCode(), ERRAND_RUNNER_NOT_EXISTS.getMsg());
            }
            log.info("跑腿员信息: runnerId={}, balance={}, auditStatus={}", runner.getId(), runner.getBalance(), runner.getFirstAuditStatus());

            // 3. 校验跑腿员状态
            if (runner.getFirstAuditStatus() != 1) {
                log.warn("跑腿员审核未通过: memberId={}, auditStatus={}", memberId, runner.getFirstAuditStatus());
                throw exception(ERRAND_RUNNER_AUDIT_PENDING.getCode(), "跑腿员审核未通过，无法提现");
            }

            // 4. 校验余额是否充足
            if (runner.getBalance().compareTo(amount) < 0) {
                log.warn("余额不足: memberId={}, balance={}, amount={}", memberId, runner.getBalance(), amount);
                throw exception(WITHDRAW_BALANCE_NOT_ENOUGH.getCode(), WITHDRAW_BALANCE_NOT_ENOUGH.getMsg());
            }
            log.info("余额校验通过: balance={}, amount={}", runner.getBalance(), amount);

            // 5. 生成提现单号
            String withdrawSn = generateWithdrawSn();
            log.info("生成提现单号: withdrawSn={}", withdrawSn);

            // 6. 计算实际到账金额
            BigDecimal serviceFee = BigDecimal.ZERO;
            BigDecimal actualAmount = amount;

            // 7. 扣减余额
            BigDecimal oldBalance = runner.getBalance();
            BigDecimal newBalance = oldBalance.subtract(amount);
            runner.setBalance(newBalance);
            log.info("开始扣减余额: oldBalance={}, amount={}, newBalance={}", oldBalance, amount, newBalance);

            // 使用乐观锁更新（防止并发扣减，配合分布式锁双重保障）
            int updateCount = errandRunnerMapper.updateBalance(memberId, oldBalance, newBalance);
            if (updateCount == 0) {
                log.warn("余额更新失败（乐观锁冲突）: memberId={}, oldBalance={}, newBalance={}", memberId, oldBalance, newBalance);
                throw exception(WITHDRAW_BALANCE_NOT_ENOUGH.getCode(), "余额已变动，请重试");
            }
            log.info("余额扣减成功: updateCount={}", updateCount);

            // 8. 创建提现记录（状态：处理中）
            log.info("创建提现记录: status={}", WITHDRAW_STATUS_PROCESSING);
            ErrandRunnerWithdrawDO withdraw = ErrandRunnerWithdrawDO.builder()
                    .withdrawSn(withdrawSn)
                    .runnerId(runner.getId())
                    .runnerMemberId(runner.getMemberId())
                    .amount(amount)
                    .serviceFee(serviceFee)
                    .actualAmount(actualAmount)
                    .status(WITHDRAW_STATUS_PROCESSING)
                    .balance(newBalance)
                    .applyTime(new Date())
                    .build();

            // 9. 调用微信转账接口
            log.info("开始调用微信转账接口: withdrawSn={}, openId={}, amount={}分", withdrawSn, openId, amount.multiply(BigDecimal.valueOf(100)).intValue());
            try {
                TransferToUser.TransferToUserResponse res = wechatTransferService.transferToUser(
                        withdrawSn,
                        openId,
                        amount.multiply(BigDecimal.valueOf(100)).intValue(),
                        "跑腿员提现"
                );
                log.info("微信转账接口调用成功: withdrawSn={}, packageInfo={}", withdrawSn, res.getPackageInfo());

                withdraw.setWechatBatchNo(res.getPackageInfo());
                withdrawMapper.insert(withdraw);
                log.info("提现记录插入成功: withdrawId={}, withdrawSn={}", withdraw.getId(), withdrawSn);

                // 转账成功：创建余额流水
                log.info("创建余额流水: withdrawId={}", withdraw.getId());
                ErrandRunnerBalanceLogDO balanceLog = ErrandRunnerBalanceLogDO.builder()
                        .runnerId(runner.getId())
                        .runnerMemberId(runner.getMemberId())
                        .withdrawId(withdraw.getId())
                        .flowType(FLOW_TYPE_WITHDRAW)
                        .direction(DIRECTION_OUT)
                        .amount(amount)
                        .beforeBalance(oldBalance)
                        .afterBalance(newBalance)
                        .balance(newBalance)
                        .frozenBalance(BigDecimal.ZERO)
                        .bizNo(withdrawSn)
                        .remark("提现申请扣款：" + amount + "元")
                        .build();
                errandRunnerBalanceLogMapper.insert(balanceLog);
                log.info("余额流水插入成功: logId={}", balanceLog.getId());

                // 更新提现记录的logId（可选，如果需要双向关联）
                withdraw.setLogId(balanceLog.getId());
                withdrawMapper.updateById(withdraw);
                balanceLog.setWithdrawId(withdraw.getId());
                errandRunnerBalanceLogMapper.updateById(balanceLog);
                log.info("提现记录和流水关联更新成功: withdrawId={}, logId={}", withdraw.getId(), balanceLog.getId());

            } catch (Exception e) {
                log.warn("微信转账失败: withdrawSn={}, error={}", withdrawSn, e.getMessage(), e);
                throw exception(WITHDRAW_FAIL.getCode(), "提现失败");
            }

            log.info("=== 提现申请完成 ===");
            log.info("返回结果: withdrawId={}, withdrawSn={}, status={}, amount={}",
                    withdraw.getId(), withdraw.getWithdrawSn(), withdraw.getStatus(), withdraw.getAmount());
            return withdraw;

        } catch (InterruptedException e) {
            log.warn("获取锁被中断: memberId={}, error={}", memberId, e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw exception(1400, "请稍后重试");
        } finally {
            // 释放锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("分布式锁释放成功: memberId={}", memberId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processWithdrawCallback(String outBillNo, String eventType, String state, String failReason) {
        log.info("=== 开始处理提现回调 ===");
        log.info("回调参数: outBillNo={}, eventType={}, state={}, failReason={}",
                outBillNo, eventType, state, failReason);

        // 1. 查询提现记录
        log.info("查询提现记录: outBillNo={}", outBillNo);
        ErrandRunnerWithdrawDO withdraw = withdrawMapper.selectByWithdrawSn(outBillNo);
        if (withdraw == null) {
            log.warn("提现记录不存在: outBillNo={}", outBillNo);
            return false;
        }
        log.info("查询到提现记录: withdrawId={}, runnerId={}, amount={}, currentStatus={}",
                withdraw.getId(), withdraw.getRunnerId(), withdraw.getAmount(), withdraw.getStatus());

        // 3. 获取跑腿员信息
        log.info("查询跑腿员信息: runnerId={}", withdraw.getRunnerId());
        ErrandRunnerDO runner = errandRunnerMapper.selectByRunnerIdForUpdate(withdraw.getRunnerId());
        if (runner == null) {
            log.warn("跑腿员不存在: runnerId={}", withdraw.getRunnerId());
            return false;
        }
        log.info("查询到跑腿员信息: runnerId={}, balance={}", runner.getId(), runner.getBalance());

        log.info("开始处理事件类型: eventType={}", eventType);
        switch (eventType) {
            case "TRANSFER.SUCCESS":
                log.info("处理TRANSFER.SUCCESS事件");
                withdraw.setStatus(WITHDRAW_STATUS_SUCCESS);
                withdraw.setFinishTime(new Date());
                withdrawMapper.updateById(withdraw);
                log.info("提现成功: outBillNo={}, withdrawId={}, amount={}", outBillNo, withdraw.getId(), withdraw.getAmount());
                break;

            case "MCHTRANSFER.BILL.FINISHED":
                log.info("处理MCHTRANSFER.BILL.FINISHED事件, state={}", state);
                Integer statusByUpdateTime = getStatusByUpdateTime(withdraw.getUpdateTime());
                log.info("根据更新时间获取状态: statusByUpdateTime={}", statusByUpdateTime);

                if (statusByUpdateTime.equals(WITHDRAW_STATUS_SUCCESS)) {
                    log.info("状态为SUCCESS，更新提现成功");
                    withdraw.setStatus(WITHDRAW_STATUS_SUCCESS);
                    withdraw.setFinishTime(new Date());
                    withdrawMapper.updateById(withdraw);
                    log.info("提现成功: outBillNo={}, withdrawId={}, amount={}", outBillNo, withdraw.getId(), withdraw.getAmount());
                } else if (statusByUpdateTime.equals(WITHDRAW_STATUS_FAIL)) {
                    log.info("状态为FAIL，开始处理提现失败");
                    // 提现失败（包括超时未领取等情况），需要退回余额
                    withdraw.setStatus(WITHDRAW_STATUS_FAIL);
                    withdraw.setFailReason(failReason);
                    withdraw.setFinishTime(new Date());
                    withdrawMapper.updateById(withdraw);
                    log.info("提现记录更新为失败: withdrawId={}, failReason={}", withdraw.getId(), failReason);

                    // 5. 退回余额（重要：防止重复退，先检查是否已有退款流水）
                    log.info("检查是否已有退款流水: outBillNo={}, flowType={}", outBillNo, FLOW_TYPE_WITHDRAW_FAIL_BACK);
                    LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ErrandRunnerBalanceLogDO::getBizNo, outBillNo)
                            .eq(ErrandRunnerBalanceLogDO::getFlowType, FLOW_TYPE_WITHDRAW_FAIL_BACK);
                    Long existCount = errandRunnerBalanceLogMapper.selectCount(wrapper);
                    log.info("已存在退款流水数: count={}", existCount);

                    if (existCount == 0) {
                        log.info("不存在退款流水，开始退回余额");
                        // 退回余额
                        BigDecimal oldBalance = runner.getBalance();
                        BigDecimal newBalance = oldBalance.add(withdraw.getAmount());
                        runner.setBalance(newBalance);
                        errandRunnerMapper.updateById(runner);
                        log.info("余额退回成功: runnerId={}, oldBalance={}, amount={}, newBalance={}",
                                runner.getId(), oldBalance, withdraw.getAmount(), newBalance);

                        // 创建退款流水记录
                        log.info("创建退款流水记录");
                        ErrandRunnerBalanceLogDO refundLog = new ErrandRunnerBalanceLogDO();
                        refundLog.setRunnerId(runner.getId());
                        refundLog.setRunnerMemberId(runner.getMemberId());
                        refundLog.setWithdrawId(withdraw.getId());
                        refundLog.setFlowType(FLOW_TYPE_WITHDRAW_FAIL_BACK);
                        refundLog.setDirection(DIRECTION_IN);
                        refundLog.setAmount(withdraw.getAmount());
                        refundLog.setBeforeBalance(oldBalance);
                        refundLog.setAfterBalance(newBalance);
                        refundLog.setBalance(newBalance);
                        refundLog.setFrozenBalance(BigDecimal.ZERO);
                        refundLog.setBizNo(outBillNo);
                        refundLog.setRemark("提现失败退回：" + withdraw.getAmount() + "元，原因：" + (failReason != null ? failReason : "未知"));
                        refundLog.setBusinessId(10L);
                        errandRunnerBalanceLogMapper.insert(refundLog);
                        log.info("退款流水创建成功: logId={}, outBillNo={}, amount={}",
                                refundLog.getId(), outBillNo, withdraw.getAmount());
                    } else {
                        log.info("退款流水已存在，跳过重复退: outBillNo={}", outBillNo);
                    }
                } else {
                    log.info("未知状态: statusByUpdateTime={}", statusByUpdateTime);
                }
                break;

            default:
                log.warn("收到未知事件类型: eventType={}, state={}", eventType, state);
                break;
        }

        log.info("=== 提现回调处理完成 ===");
        log.info("最终结果: outBillNo={}, status={}", outBillNo, withdraw.getStatus());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processWithdrawCallbackTest(String outBillNo) {
        ErrandRunnerWithdrawDO withdraw = withdrawMapper.selectByWithdrawSn(outBillNo);
        if (withdraw == null) {
            log.warn("提现记录不存在: outBillNo={}", outBillNo);
            return false;
        }

        // 3. 获取跑腿员信息
        ErrandRunnerDO runner = errandRunnerMapper.selectByRunnerIdForUpdate(withdraw.getRunnerId());
        if (runner == null) {
            log.warn("跑腿员不存在: runnerId={}", withdraw.getRunnerId());
            return false;
        }

        Integer statusByUpdateTime = getStatusByUpdateTime(withdraw.getUpdateTime());
        if(statusByUpdateTime.equals(WITHDRAW_STATUS_SUCCESS)){
            withdraw.setStatus(WITHDRAW_STATUS_SUCCESS);
            withdraw.setFinishTime(new Date());
            withdrawMapper.updateById(withdraw);
            log.info("提现成功: outBillNo={}, amount={}", outBillNo, withdraw.getAmount());
        }else if(statusByUpdateTime.equals(WITHDRAW_STATUS_FAIL)) {
            // 提现失败（包括超时未领取等情况），需要退回余额
            withdraw.setStatus(WITHDRAW_STATUS_FAIL);
            withdraw.setFinishTime(new Date());
            withdrawMapper.updateById(withdraw);

            // 5. 退回余额（重要：防止重复退，先检查是否已有退款流水）
            LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ErrandRunnerBalanceLogDO::getBizNo, outBillNo)
                    .eq(ErrandRunnerBalanceLogDO::getFlowType, FLOW_TYPE_WITHDRAW_FAIL_BACK);
            Long existCount = errandRunnerBalanceLogMapper.selectCount(wrapper);

            if (existCount == 0) {
                // 退回余额
                BigDecimal oldBalance = runner.getBalance();
                BigDecimal newBalance = oldBalance.add(withdraw.getAmount());
                runner.setBalance(newBalance);
                errandRunnerMapper.updateById(runner);

                // 创建退款流水记录
                ErrandRunnerBalanceLogDO refundLog = ErrandRunnerBalanceLogDO.builder()
                        .runnerId(runner.getId())
                        .runnerMemberId(runner.getMemberId())
                        .withdrawId(withdraw.getId())
                        .flowType(FLOW_TYPE_WITHDRAW_FAIL_BACK)
                        .direction(DIRECTION_IN)
                        .amount(withdraw.getAmount())
                        .beforeBalance(oldBalance)
                        .afterBalance(newBalance)
                        .balance(newBalance)
                        .frozenBalance(BigDecimal.ZERO)
                        .bizNo(outBillNo)
                        .build();
                errandRunnerBalanceLogMapper.insert(refundLog);
                log.info("提现失败，余额已退回: outBillNo={}, amount={}", outBillNo, withdraw.getAmount());
            } else {
                log.info("提现失败退款流水已存在，跳过重复退: outBillNo={}", outBillNo);
            }
        }
        return false;
    }

    // 更精确的时间判断
    private Integer getStatusByUpdateTime(LocalDateTime updateTime) {
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

    /**
     * 生成提现单号
     * 格式: WD + 时间戳(秒) + 8位随机数
     */
    private String generateWithdrawSn() {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String random = RandomUtil.randomNumbers(8);
        return "WD" + timestamp + random;
    }

}