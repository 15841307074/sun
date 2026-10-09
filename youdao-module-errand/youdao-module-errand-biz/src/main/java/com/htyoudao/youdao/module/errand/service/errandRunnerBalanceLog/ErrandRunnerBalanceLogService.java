package com.htyoudao.youdao.module.errand.service.errandRunnerBalanceLog;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ErrandRunnerBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ErrandRunnerBalanceLogRespVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.RunnerDetailRespVO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog.ErrandRunnerBalanceLogDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 跑腿员余额流水服务接口
 */
public interface ErrandRunnerBalanceLogService extends IService<ErrandRunnerBalanceLogDO> {

    /**
     * 根据跑腿员ID查询余额流水
     * @param runnerId 跑腿员ID
     * @return 流水列表
     */
    List<ErrandRunnerBalanceLogDO> getByRunnerId(Long runnerId);

    /**
     * 根据业务流水号查询（防重）
     * @param bizNo 业务流水号
     * @return 流水记录
     */
    ErrandRunnerBalanceLogDO getByBizNo(String bizNo);

    /**
     * 统计跑腿员总收入
     * @param runnerId 跑腿员ID
     * @return 总收入金额
     */
    BigDecimal getTotalIncome(Long runnerId);

    /**
     * 统计跑腿员总提现金额
     * @param runnerId 跑腿员ID
     * @return 总提现金额
     */
    BigDecimal getTotalWithdraw(Long runnerId);

    /**
     * 创建余额流水（带防重校验）
     * @param balanceLog 流水记录
     * @return 是否创建成功
     */
    boolean createBalanceLog(ErrandRunnerBalanceLogDO balanceLog);

    /**
     * 赏金入账流水
     * @param runnerId 跑腿员ID
     * @param runnerMemberId 跑腿员会员ID
     * @param orderSn 订单号
     * @param amount 金额
     * @param beforeBalance 变动前余额
     * @param afterBalance 变动后余额
     * @param bizNo 业务流水号
     * @return 是否创建成功
     */
    boolean createIncomeLog(Long runnerId, Long runnerMemberId, String orderSn,
                            BigDecimal amount, BigDecimal beforeBalance,
                            BigDecimal afterBalance, String bizNo);

    /**
     * 提现扣减流水
     * @param runnerId 跑腿员ID
     * @param runnerMemberId 跑腿员会员ID
     * @param withdrawId 提现单ID
     * @param amount 金额
     * @param beforeBalance 变动前余额
     * @param afterBalance 变动后余额
     * @param bizNo 业务流水号
     * @return 是否创建成功
     */
    boolean createWithdrawLog(Long runnerId, Long runnerMemberId, Long withdrawId,
                              BigDecimal amount, BigDecimal beforeBalance,
                              BigDecimal afterBalance, String bizNo);

    // 在 ErrandRunnerBalanceLogService 接口中添加以下方法

    /**
     * 获取跑腿员当前可用余额
     * @param runnerId 跑腿员ID
     * @return 当前可用余额
     */
    BigDecimal getCurrentBalance(Long runnerId);

    /**
     * 获取跑腿员今日收入
     * @param runnerId 跑腿员ID
     * @return 今日收入
     */
    BigDecimal getTodayIncome(Long runnerId);

    /**
     * 获取跑腿员本周收入
     * @param runnerId 跑腿员ID
     * @return 本周收入
     */
    BigDecimal getWeekIncome(Long runnerId);

    /**
     * 获取跑腿员本月收入
     * @param runnerId 跑腿员ID
     * @return 本月收入
     */
    BigDecimal getMonthIncome(Long runnerId);

    /**
     * 根据会员ID查询余额流水
     * @param reqVO reqVO
     * @return ErrandRunnerBalanceLogRespVO
     */
    PageResult<ErrandRunnerBalanceLogRespVO> selectByMemberId(ErrandRunnerBalanceLogReqVO reqVO);

    /**
     * 获取跑腿员详情
     * @param memberId memberId
     * @return RunnerDetailRespVO
     */
    RunnerDetailRespVO getRunnerDetailByMemberId(Long memberId);
}
