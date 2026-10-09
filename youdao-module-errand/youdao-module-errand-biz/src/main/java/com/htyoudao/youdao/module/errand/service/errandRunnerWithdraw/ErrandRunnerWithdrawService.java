package com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ExportBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw.ErrandRunnerWithdrawDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 跑腿员提现服务接口
 */
public interface ErrandRunnerWithdrawService extends IService<ErrandRunnerWithdrawDO> {

    /**
     * 根据提现单号查询
     * @param withdrawSn 提现单号
     * @return 提现记录
     */
    ErrandRunnerWithdrawDO getByWithdrawSn(String withdrawSn);

    /**
     * 根据跑腿员ID查询提现记录
     * @param runnerId 跑腿员ID
     * @return 提现记录列表
     */
    List<ErrandRunnerWithdrawDO> getByRunnerId(Long runnerId);

    /**
     * 统计跑腿员总提现金额
     * @param runnerId 跑腿员ID
     * @return 总提现金额
     */
    BigDecimal getTotalSuccessAmount(Long runnerId);

    /**
     * 统计跑腿员总手续费
     * @param runnerId 跑腿员ID
     * @return 总手续费
     */
    BigDecimal getTotalServiceFee(Long runnerId);


    /**
     * 提现成功处理
     * @param withdrawId 提现记录ID
     * @param wechatBatchNo 微信批次号
     * @param wechatDetailNo 微信明细号
     * @return 是否成功
     */
    boolean handleWithdrawSuccess(Long withdrawId, String wechatBatchNo, String wechatDetailNo);

    /**
     * 提现失败处理
     * @param withdrawId 提现记录ID
     * @param failReason 失败原因
     * @return 是否成功
     */
    boolean handleWithdrawFail(Long withdrawId, String failReason);

    /**
     * 查询待处理的提现记录
     * @return 待处理提现记录列表
     */
    List<ErrandRunnerWithdrawDO> getPendingWithdrawList();

    /**
     * 统计跑腿员今日提现金额
     * @param runnerId 跑腿员ID
     * @return 今日提现金额
     */
    BigDecimal getTodayWithdrawAmount(Long runnerId);

    /**
     * 导出跑腿员余额流水
     * @param reqVO reqVO
     * @return Void
     */
    Void exportErrandList(ExportBalanceLogReqVO reqVO);


    /**
     * 发起提现申请
     *
     * @param memberId 跑腿员会员ID
     * @param amount 提现金额
     * @return 提现记录
     */
    ErrandRunnerWithdrawDO applyWithdraw(Long memberId, BigDecimal amount,String openId);

    /**
     * 处理提现回调（微信支付回调）
     *
     * @param outBillNo 商户单号
     * @param status 转账状态 SUCCESS/FAIL
     * @param failReason 失败原因
     * @return 是否处理成功
     */
    boolean processWithdrawCallback(String outBillNo, String eventType,String state, String failReason);

    /**
     * 处理提现回调(测试版)
     *
     * @param outBillNo 商户单号
     * @return 是否处理成功
     */
    boolean processWithdrawCallbackTest(String outBillNo);
}
