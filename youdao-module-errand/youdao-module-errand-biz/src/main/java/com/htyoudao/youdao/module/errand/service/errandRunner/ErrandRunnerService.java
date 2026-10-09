package com.htyoudao.youdao.module.errand.service.errandRunner;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO.ErrandRunnerDetailVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO.ErrandRunnerPageReqVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO.ErrandRunnerPageVO;
import com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO.*;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;

import java.math.BigDecimal;

/**
 * 跑腿员服务接口。
 */
public interface ErrandRunnerService extends IService<ErrandRunnerDO> {

    /**
     * 提交骑手进驻申请。
     *
     * @param memberId 当前会员 ID
     * @param reqVO 申请资料
     * @return 跑腿员 ID
     */
    Long apply(Long memberId, AppErrandRunnerApplyReqVO reqVO);

    /**
     * 修改审核失败后的骑手进驻申请。
     *
     * @param reqVO 申请资料
     * @return 是否修改成功
     */
    Boolean updateApply(Long memberId, AppErrandRunnerApplyReqVO reqVO);

    /**
     * 标记当前骑手弹窗已展示。
     *
     * @param memberId 当前会员 ID
     * @return 是否修改成功
     */
    Boolean markPopupShown(Long memberId);

    /**
     * 查询当前会员的骑手进驻详情。
     *
     * @param memberId 当前会员 ID
     * @return 进驻详情；未申请时返回 null
     */
    AppErrandRunnerRespVO getApplyInfo(Long memberId);

    /**
     * 校验当前登录会员是否可作为跑腿员接单。
     *
     * @return 校验通过返回 true
     */
    boolean checkCurrentRunnerAvailable();



    /**
     * 验证交易密码。
     *
     * @param memberId 会员 ID
     * @param tranPassword 交易密码
     * @return 是否验证通过
     */
    boolean verifyTranPassword(Long memberId, String tranPassword);



    /**
     * 验证会员是否存在跑腿员记录。
     *
     * @param memberId 会员 ID
     * @return 是否存在
     */
    boolean checkMemberExists(Long memberId);

    /**
     * 根据手机号获取跑腿员信息。
     *
     * @param mobile 手机号
     * @return 跑腿员信息
     */
    ErrandRunnerDO getByMobile(String mobile);

    /**
     * 根据会员ID获取跑腿员信息。
     *
     * @param memberId 会员ID
     * @return 跑腿员信息
     */
    ErrandRunnerDO getByMemberId(Long memberId);

    /**
     * 审核跑腿员。
     *
     * @param runnerId 跑腿员 ID
     * @param auditStatus 审核状态：1通过 2失败
     * @param auditReason 审核失败原因
     * @return 是否成功
     */
    boolean auditRunner(Long runnerId, Integer auditStatus, String auditReason);

    /**
     * 封禁跑腿员。
     *
     * @param runnerId 跑腿员 ID
     * @return 是否成功
     */
    boolean banRunner(Long runnerId);

    /**
     * 解封跑腿员。
     *
     * @param runnerId 跑腿员 ID
     * @return 是否成功
     */
    boolean unbanRunner(Long runnerId);

    /**
     * 检查跑腿员是否可用。
     *
     * @param runnerId 跑腿员 ID
     * @return 是否可用
     */
    boolean isRunnerAvailable(Long runnerId);

    /**
     * 获取待审核跑腿员列表。
     *
     * @param current 当前页
     * @param size 每页大小
     * @return 待审核列表
     */
    Page<ErrandRunnerDO> getPendingAuditList(Integer current, Integer size);

    /**
     * 分页查询跑腿员。
     *
     * @param reqVO 查询请求参数
     * @return 分页结果
     */
    Page<ErrandRunnerPageVO> pageQuery(ErrandRunnerPageReqVO reqVO);

    /**
     * 获取跑腿员余额明细。
     *
     * @param memberId 会员 ID
     * @return 余额明细
     */
    BalanceDetailRespVO getBalanceDetail(Long memberId,ErrandRunnerDO runner);

    /**
     * 分页查询余额流水。
     *
     * @param memberId 会员 ID
     * @param reqVO 查询请求参数
     * @return 流水分页结果
     */
    BalanceLogPageRespVO pageBalanceLog(Long memberId, BalanceLogPageReqVO reqVO,ErrandRunnerDO runner);

    /**
     * 获取余额明细及流水。
     *
     * @param memberId 会员 ID
     * @param reqVO 查询请求参数
     * @return 余额明细及流水
     */
    BalanceDetailWithLogRespVO getBalanceDetailWithLog(Long memberId, BalanceLogPageReqVO reqVO);

    /**
     * 跑腿赏金冻结入账。
     *
     * @param runnerMemberId 跑腿员会员 ID
     * @param orderSn 订单号
     * @param amount 赏金金额
     * @return 是否处理成功
     */
    boolean createRewardIncome(Long runnerMemberId, String orderSn, BigDecimal amount);

    /**
     * 跑腿赏金退款扣回。
     *
     * @param runnerMemberId 跑腿员会员 ID
     * @param orderSn 订单号
     * @param amount 扣回金额
     * @return 是否处理成功
     */
    boolean refundRewardDeduct(Long runnerMemberId, String orderSn, BigDecimal amount);

    /**
     * 解冻超过配置冻结时长的赏金。
     *
     * @return 解冻流水数量
     */
    int unfreezeExpiredRewards();

    /**
     * 获取跑腿员详情
     *
     * @param id 跑腿员id
     * @return 跑腿员详情
     */
    ErrandRunnerDetailVO getDetailById(Long id);
}
