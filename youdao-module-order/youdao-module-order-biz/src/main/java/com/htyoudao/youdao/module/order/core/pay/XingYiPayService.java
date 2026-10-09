package com.htyoudao.youdao.module.order.core.pay;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.XingYiPayReqVO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.SysPayRecordDO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.PaymentMethodEnum;
import com.htyoudao.youdao.module.order.service.pay.SysPayRecordService;
import com.htyoudao.youdao.module.order.util.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_PAY_RECORD_NOT_EXISTS;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.PAY_COMMON_EXCEPTION;

/**
 * <p>
 * 星驿付相关
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-11
 */
@Slf4j
@Service
public class XingYiPayService {

    private static final String NOTIFY_URL = "admin-order0bz-order-pay0notify";
    private static final String ERRAND_REWARD_SPLIT_METHOD = "errandRewardSplit";
    private static final String ERRAND_REWARD_SPLIT_REVOKE_METHOD = "errandRewardSplitRevoke";
    private static final String ERRAND_SPLIT_REVOKE_FULL_METHOD = ERRAND_REWARD_SPLIT_REVOKE_METHOD + "Revoke";
    private static final String ERRAND_SPLIT_REVOKE_FOOD_METHOD = "errandSplitRevokeFood";
    private static final String ERRAND_SPLIT_REVOKE_REWARD_METHOD = "errandSplitRevokeReward";
    private static final String PAY_RECORD_STATUS_PROCESSING = "PROCESSING";
    private static final String PAY_RECORD_STATUS_SUCCESS = "SUCCESS";
    private static final String PAY_RECORD_STATUS_FAIL = "FAIL";
    private static final int ERRAND_PAY_FEE_RATE_NUMERATOR = 38;
    private static final int ERRAND_PAY_FEE_RATE_DENOMINATOR = 10000;
    private static final int ERRAND_SETTLE_RATE_NUMERATOR = 2;
    private static final int ERRAND_SETTLE_RATE_DENOMINATOR = 10000;

    @Value("${xingyipay.agetId}")
    private String agetId;
    @Value("${xingyipay.callbackurl}")
    private String SLD_API_URL;
    @Value("${xingyipay.eOLink.url}")
    private String eOLinkUrl;
    @Value("${xingyipay.public.key}")
    private String publicKey;
    @Value("${xingyipay.errandRewardInCustId}")
    private String errandRewardInCustId;
    @Value("${xingyipay.errandRewardSplitAreaId}")
    private String errandRewardSplitAreaId;

    @Resource
    private SysPayRecordService sysPayRecordService;
    @Resource
    private PlatformTransactionManager transactionManager;

    /**
     * 统一下单
     *
     * @param reqVO
     * @return
     */
    public JSONObject xingYiPay(XingYiPayReqVO reqVO) {
        validateXingYiCustId(reqVO.getTerminalSn());
        Map<String, String> params = new HashMap<>();
        SysPayRecordDO sysPayRecord = new SysPayRecordDO();
        sysPayRecord.setMethodName("wxZfbPay");
        sysPayRecord.setOrderId(reqVO.getPaySn());
        sysPayRecord.setMethodParam(JSON.toJSONString(reqVO));
        sysPayRecord.setAgetId(agetId);
        sysPayRecord.setCustId(reqVO.getTerminalSn());
        sysPayRecord.setCreateTime(reqVO.getCreateTime());

        params.put("orderNo", reqVO.getPaySn());
        params.put("txamt", String.valueOf(AmountUtil.yuan2Fen(new BigDecimal(reqVO.getPayAmount()))));
        params.put("payWay", reqVO.getPayWay());
        params.put("remark", NOTIFY_URL);
        params.put("openid", SecurityFrameworkUtils.getLoginOpenid());
        params.put("traType", "8");
        params.put("custId", reqVO.getTerminalSn());
        params.put("agetId", agetId);
//     TODO   map.put("outTime", String.valueOf(DateUtils.trunToSubCurrMinute(xingYiPay.getTimeOut(), DateUtils.YYYYMMDDHHMMSS)));

        if (Objects.equals(reqVO.getPayWay(), String.valueOf(PaymentMethodEnum.WECHAT_PAYMENT.getCode()))) {
            params.put("wxAppid", reqVO.getAppId());
        }
        if (Objects.equals(reqVO.getPayWay(), String.valueOf(PaymentMethodEnum.ALIPAY_PAYMENT.getCode()))) {
            params.put("zfbappid", reqVO.getAppId());
        }

        try {
            params.put("ip", IpUtils.getClientIp());
        } catch (Exception e) {
            params.put("ip", "127.0.0.1");
        }

        //星驿付支付
        JSONObject from = this.sendPaymentRequest(params, OrderConstants.C_TO_B_E_O_LINK_PAY);
        if (!ObjectUtil.equals(from.get("code").toString(), "000000")) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), (String) from.get("msg"));
        }

        JSONObject data = JSONObject.from(from.get("data"));
        sysPayRecord.setThridOrderNo(data.get("orderNo").toString());
        sysPayRecord.setMethodReturn(JSON.toJSONString(from.get("data")));
        sysPayRecordService.save(sysPayRecord);

        return data;
    }

    /**
     * 统一下单
     *
     * @param reqVO
     * @return
     */
    public String xingYiQuery(XingYiPayReqVO reqVO) {
        validateXingYiCustId(reqVO.getTerminalSn());
        Map<String, String> params = new HashMap<>();
        params.put("orderNo", reqVO.getPaySn());
        params.put("custId", reqVO.getTerminalSn());
        params.put("agetId", agetId);
        params.put("orderTime", DateUtils.localDateTimeToString(reqVO.getCreateTime(), DateUtils.YYYYMMDD));

        //星驿付支付
        JSONObject from = this.sendPaymentRequest(params, OrderConstants.C_TO_B_E_O_LINK_QUERY);
        return from.get("code").toString();
    }


    /**
     * 星驿付退款
     *
     * @param reqVO
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void xingYiRefund(XingYiPayReqVO reqVO) {
        xingYiRefund(reqVO, "refund");
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void xingYiRefund(XingYiPayReqVO reqVO, String methodName) {
        Map<String, String> params = new HashMap<>();
        SysPayRecordDO sysPayRecordOld = sysPayRecordService.getOne(
                new LambdaQueryWrapper<SysPayRecordDO>()
                        .eq(SysPayRecordDO::getOrderId, reqVO.getPaySn())
                        .eq(SysPayRecordDO::getCreateTime, reqVO.getCreateTime())
                        .eq(SysPayRecordDO::getMethodName, "wxZfbPay")

        );
        if (ObjectUtil.isEmpty(sysPayRecordOld)) {
            sysPayRecordOld = sysPayRecordService.getOne(
                    new LambdaQueryWrapper<SysPayRecordDO>()
                            .eq(SysPayRecordDO::getOrderId, reqVO.getPaySn())
                            .eq(SysPayRecordDO::getCreateTime, reqVO.getCreateTime())
                            .eq(SysPayRecordDO::getMethodName, "bScanCdoPay")

            );
            if (ObjectUtil.isEmpty(sysPayRecordOld)) {
                throw exception(ORDER_PAY_RECORD_NOT_EXISTS);
            }
        }
        validateXingYiCustId(sysPayRecordOld.getCustId());

        SysPayRecordDO sysPayRecordDO = new SysPayRecordDO();
        sysPayRecordDO.setMethodName(ObjectUtil.isEmpty(methodName) ? "refund" : methodName);
        sysPayRecordDO.setOrderId(reqVO.getPaySn());
        sysPayRecordDO.setCreateTime(LocalDateTime.now());
        sysPayRecordDO.setMethodParam(JSON.toJSONString(reqVO));

        String s = SerialNumberGenerator.generateSerialNumberFive();
        params.put("orderNo", reqVO.getPaySn() + "_" + s);
        params.put("reOrderNo", sysPayRecordOld.getThridOrderNo());
        params.put("custId", sysPayRecordOld.getCustId());
        params.put("agetId", sysPayRecordOld.getAgetId());
        params.put("oldTOrderNo", reqVO.getPaySn());
        params.put("refundAmount", String.valueOf(AmountUtil.yuan2Fen(new BigDecimal(reqVO.getPayAmount()))));
        //微信 2 支付宝1
        if (reqVO.getPayWay().contains(PaymentMethodEnum.ALIPAY_PAYMENT.getEngMsg())) {
            params.put("tag", "1");
        }
        if (reqVO.getPayWay().contains(PaymentMethodEnum.WECHAT_PAYMENT.getEngMsg())) {
            params.put("tag", "2");
        }

        JSONObject from = this.sendPaymentRequest(params, OrderConstants.E_O_LINK_REFUND);
        if (!ObjectUtil.equals(from.get("code").toString(), "000000")) {
            sysPayRecordDO.setMethodReturn(from.get("msg").toString());
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), (String) from.get("msg"));
        }

        sysPayRecordDO.setMethodReturn(JSON.toJSONString(from.get("data")));
        sysPayRecordService.save(sysPayRecordDO);
    }

    /**
     * 跑腿赏金分账到赏金商户号。
     *
     * @param order 代取订单
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void xingYiErrandRewardSplit(BzOrderDO order) {
        BigDecimal rewardAmount = order.getErrandRewardAmount() == null ? BigDecimal.ZERO : order.getErrandRewardAmount();
        if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        SysPayRecordDO existSplitRecord = sysPayRecordService.getOne(
                new LambdaQueryWrapper<SysPayRecordDO>()
                        .eq(SysPayRecordDO::getOrderId, order.getPaySn())
                        .eq(SysPayRecordDO::getCreateTime, order.getCreateTime())
                        .eq(SysPayRecordDO::getMethodName, ERRAND_REWARD_SPLIT_METHOD)
                        .eq(SysPayRecordDO::getPayUrlResult, PAY_RECORD_STATUS_SUCCESS)
                        .last("LIMIT 1")
        );
        if (ObjectUtil.isNotEmpty(existSplitRecord)) {
            log.info("跑腿赏金分账记录已存在，orderSn: {}", order.getOrderSn());
            return;
        }

        SysPayRecordDO payRecord = sysPayRecordService.getOne(
                new LambdaQueryWrapper<SysPayRecordDO>()
                        .eq(SysPayRecordDO::getOrderId, order.getPaySn())
                        .eq(SysPayRecordDO::getCreateTime, order.getCreateTime())
                        .eq(SysPayRecordDO::getMethodName, "wxZfbPay")
                        .last("LIMIT 1")
        );
        if (ObjectUtil.isEmpty(payRecord)) {
            throw exception(ORDER_PAY_RECORD_NOT_EXISTS);
        }

        int payAmountFen = AmountUtil.yuan2Fen(order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount());
        int splitAmountFen = AmountUtil.yuan2Fen(order.getBalanceAmount() == null ? BigDecimal.ZERO : order.getBalanceAmount());
        int rewardAmountFen = AmountUtil.yuan2Fen(rewardAmount);
        if (payAmountFen <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "订单支付金额不能为空");
        }
        if (splitAmountFen <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "订单星驿付净到账金额不能为空");
        }
        if (splitAmountFen > payAmountFen) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "订单星驿付净到账金额不能大于订单支付金额");
        }
        if (rewardAmountFen > payAmountFen) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "跑腿赏金金额不能大于订单支付金额");
        }
        if (ObjectUtil.isEmpty(payRecord.getCustId())) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付商户号不能为空");
        }
        if (ObjectUtil.isEmpty(payRecord.getThridOrderNo())) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付平台订单号不能为空");
        }
        if (ObjectUtil.isEmpty(errandRewardInCustId)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付跑腿赏金收款商户号未配置");
        }
        if (ObjectUtil.isEmpty(errandRewardSplitAreaId)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付跑腿赏金分账商圈ID未配置");
        }

        ErrandRewardSplitAmount splitAmount = calculateErrandRewardSplitAmount(payAmountFen, splitAmountFen, rewardAmountFen);

        List<Map<String, Object>> receiveList = new ArrayList<>();
        if (splitAmount.getStoreReceiveFen() > 0) {
            receiveList.add(buildSplitReceive(payRecord.getCustId(), splitAmount.getStoreReceiveFen()));
        }
        receiveList.add(buildSplitReceive(errandRewardInCustId, splitAmount.getRewardReceiveFen()));

        Map<String, Object> params = new HashMap<>();
        params.put("agetId", payRecord.getAgetId());
        params.put("custId", payRecord.getCustId());
        params.put("orderNo", payRecord.getThridOrderNo());
        params.put("areaId", errandRewardSplitAreaId);
        params.put("splitType", "3");
        params.put("receiveList", receiveList);
        params.put("remark", "跑腿赏金分账");

        SysPayRecordDO splitRecord = new SysPayRecordDO();
        splitRecord.setMethodName(ERRAND_REWARD_SPLIT_METHOD);
        splitRecord.setOrderId(order.getPaySn());
        splitRecord.setMethodParam(JSON.toJSONString(buildErrandSplitMethodParam(order)));
        splitRecord.setAgetId(payRecord.getAgetId());
        splitRecord.setCustId(payRecord.getCustId());
        splitRecord.setCreateTime(order.getCreateTime());
        splitRecord.setPayUrlResult(PAY_RECORD_STATUS_PROCESSING);
        splitRecord.setMethodPostParam(JSON.toJSONString(params));
        this.savePayRecordInNewTransaction(splitRecord);

        JSONObject from = this.sendSplitRequest(params, OrderConstants.XING_YI_SPLIT);
        if (!ObjectUtil.equals(from.get("code").toString(), "000000")) {
            this.updatePayRecordResultInNewTransaction(splitRecord, PAY_RECORD_STATUS_FAIL, from, from.getString("msg"), null);
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), (String) from.get("msg"));
        }

        JSONObject data = JSONObject.from(from.get("data"));
        this.updatePayRecordResultInNewTransaction(splitRecord, PAY_RECORD_STATUS_SUCCESS, from, JSON.toJSONString(data), data.getString("batchNo"));
    }

    private Map<String, Object> buildSplitReceive(String custIdReceiv, int txamt) {
        Map<String, Object> receive = new HashMap<>();
        receive.put("custIdReceiv", custIdReceiv);
        receive.put("txamt", String.valueOf(txamt));
        return receive;
    }

    static ErrandRewardSplitAmount calculateErrandRewardSplitAmount(int payAmountFen, int splitAmountFen, int rewardAmountFen) {
        int storeGrossAmountFen = payAmountFen - rewardAmountFen;
        if (storeGrossAmountFen < 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "门店代取应收金额不能小于0");
        }
        if (storeGrossAmountFen == 0) {
            return new ErrandRewardSplitAmount(0, splitAmountFen, 0, 0);
        }

        int storePayFeeFen = divideToFen((long) storeGrossAmountFen * ERRAND_PAY_FEE_RATE_NUMERATOR,
                ERRAND_PAY_FEE_RATE_DENOMINATOR, RoundingMode.HALF_UP);
        int storeTargetSettleFen = storeGrossAmountFen - storePayFeeFen;
        if (storeTargetSettleFen <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "门店扣除星驿付手续费后金额必须大于0");
        }

        int storeReceiveFen = divideToFen((long) storeTargetSettleFen * ERRAND_SETTLE_RATE_DENOMINATOR,
                ERRAND_SETTLE_RATE_DENOMINATOR - ERRAND_SETTLE_RATE_NUMERATOR, RoundingMode.CEILING);
        int rewardReceiveFen = splitAmountFen - storeReceiveFen;
        if (rewardReceiveFen <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "跑腿赏金扣除星驿付手续费及落卡手续费后金额必须大于0");
        }
        return new ErrandRewardSplitAmount(storeReceiveFen, rewardReceiveFen, storePayFeeFen, storeTargetSettleFen);
    }

    private static int divideToFen(long numerator, int denominator, RoundingMode roundingMode) {
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 0, roundingMode)
                .intValueExact();
    }

    static class ErrandRewardSplitAmount {
        private final int storeReceiveFen;
        private final int rewardReceiveFen;
        private final int storePayFeeFen;
        private final int storeTargetSettleFen;

        ErrandRewardSplitAmount(int storeReceiveFen, int rewardReceiveFen, int storePayFeeFen, int storeTargetSettleFen) {
            this.storeReceiveFen = storeReceiveFen;
            this.rewardReceiveFen = rewardReceiveFen;
            this.storePayFeeFen = storePayFeeFen;
            this.storeTargetSettleFen = storeTargetSettleFen;
        }

        int getStoreReceiveFen() {
            return storeReceiveFen;
        }

        int getRewardReceiveFen() {
            return rewardReceiveFen;
        }

        int getStorePayFeeFen() {
            return storePayFeeFen;
        }

        int getStoreTargetSettleFen() {
            return storeTargetSettleFen;
        }
    }

    private void savePayRecordInNewTransaction(SysPayRecordDO record) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Boolean saved = transactionTemplate.execute(status -> sysPayRecordService.save(record));
        if (!Boolean.TRUE.equals(saved) || ObjectUtil.isEmpty(record.getId())) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "保存星驿付请求记录失败");
        }
    }

    private void updatePayRecordResultInNewTransaction(SysPayRecordDO record, String recordStatus,
                                                       JSONObject response, String methodReturn, String thridOrderNo) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Boolean updated = transactionTemplate.execute(status -> sysPayRecordService.update(
                new LambdaUpdateWrapper<SysPayRecordDO>()
                        .set(SysPayRecordDO::getPayUrlResult, recordStatus)
                        .set(SysPayRecordDO::getMethodPostResult, JSON.toJSONString(response))
                        .set(SysPayRecordDO::getMethodReturn, methodReturn)
                        .set(thridOrderNo != null, SysPayRecordDO::getThridOrderNo, thridOrderNo)
                        .eq(SysPayRecordDO::getId, record.getId())
                        .eq(SysPayRecordDO::getCreateTime, record.getCreateTime())
        ));
        if (!Boolean.TRUE.equals(updated)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "更新星驿付请求记录状态失败");
        }
    }

    /**
     * 跑腿赏金订单分账撤销。
     *
     * @param order    代取订单
     * @param crossDay 是否跨天，当前代取退款业务已限制不支持跨天退款
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void xingYiErrandRewardSplitRevoke(BzOrderDO order, boolean crossDay) {
        xingYiErrandSplitRevoke(order, ErrandSplitRevokeType.FULL);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void xingYiErrandSplitRevoke(BzOrderDO order, ErrandSplitRevokeType revokeType) {
        SysPayRecordDO splitRecord = getErrandRewardSplitRecord(order.getPaySn(), order.getCreateTime());
        if (ObjectUtil.isEmpty(splitRecord)) {
            log.info("跑腿赏金未分账，无需撤销退回，orderSn: {}", order.getOrderSn());
            return;
        }

        String revokeMethod = getErrandSplitRevokeMethod(revokeType);
        SysPayRecordDO existRevokeRecord = sysPayRecordService.getOne(
                new LambdaQueryWrapper<SysPayRecordDO>()
                        .eq(SysPayRecordDO::getOrderId, order.getPaySn())
                        .eq(SysPayRecordDO::getCreateTime, order.getCreateTime())
                        .eq(SysPayRecordDO::getMethodName, revokeMethod)
                        .eq(SysPayRecordDO::getPayUrlResult, PAY_RECORD_STATUS_SUCCESS)
                        .last("LIMIT 1")
        );
        if (ObjectUtil.isNotEmpty(existRevokeRecord)) {
            log.info("跑腿赏金分账撤销退回记录已存在，orderSn: {}, methodName: {}", order.getOrderSn(), revokeMethod);
            return;
        }

        JSONObject splitReq = JSON.parseObject(splitRecord.getMethodPostParam());
        JSONObject splitResp = JSON.parseObject(splitRecord.getMethodReturn());
        String batchNo = splitRecord.getThridOrderNo();
        if (ObjectUtil.isEmpty(batchNo) && splitResp != null) {
            batchNo = splitResp.getString("batchNo");
        }
        if (ObjectUtil.isEmpty(batchNo)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账批次号不能为空");
        }
        String taskId = null;
        if (ErrandSplitRevokeType.REWARD.equals(revokeType)) {
            taskId = getTaskIdByCustId(splitResp, errandRewardInCustId, true);
        } else if (ErrandSplitRevokeType.FOOD.equals(revokeType)) {
            taskId = getTaskIdByCustId(splitResp, splitRecord.getCustId(), true);
        }
        if (splitReq == null) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账请求记录不能为空");
        }
        String areaId = splitReq.getString("areaId");
        String custId = splitReq.getString("custId");
        String orderNo = splitReq.getString("orderNo");
        if (ObjectUtil.isEmpty(areaId)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账商圈ID不能为空");
        }
        if (ObjectUtil.isEmpty(custId)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付商户号不能为空");
        }
        if (ObjectUtil.isEmpty(orderNo)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付平台订单号不能为空");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("agetId", splitRecord.getAgetId());
        params.put("areaId", areaId);
        params.put("custId", custId);
        params.put("orderNo", orderNo);
        if (ErrandSplitRevokeType.FULL.equals(revokeType)) {
            params.put("batchNo", batchNo);
        } else {
            params.put("taskId", taskId);
        }
        params.put("operType", "1");

        SysPayRecordDO revokeRecord = new SysPayRecordDO();
        revokeRecord.setMethodName(revokeMethod);
        revokeRecord.setOrderId(order.getPaySn());
        revokeRecord.setMethodParam(JSON.toJSONString(buildErrandSplitMethodParam(order)));
        revokeRecord.setAgetId(splitRecord.getAgetId());
        revokeRecord.setCustId(splitRecord.getCustId());
        revokeRecord.setCreateTime(order.getCreateTime());
        revokeRecord.setPayUrlResult(PAY_RECORD_STATUS_PROCESSING);
        revokeRecord.setMethodPostParam(JSON.toJSONString(params));
        this.savePayRecordInNewTransaction(revokeRecord);

        JSONObject from = this.sendSplitRequest(params, OrderConstants.XING_YI_SPLIT_REVOKE);
        if (!ObjectUtil.equals(from.get("code").toString(), "000000")) {
            this.updatePayRecordResultInNewTransaction(revokeRecord, PAY_RECORD_STATUS_FAIL, from, from.getString("msg"), null);
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), (String) from.get("msg"));
        }

        JSONObject data = JSONObject.from(from.get("data"));
        this.updatePayRecordResultInNewTransaction(revokeRecord, PAY_RECORD_STATUS_SUCCESS, from, JSON.toJSONString(data), data.getString("batchNo"));
    }

    private String getErrandSplitRevokeMethod(ErrandSplitRevokeType revokeType) {
        if (ErrandSplitRevokeType.FOOD.equals(revokeType)) {
            return ERRAND_SPLIT_REVOKE_FOOD_METHOD;
        }
        if (ErrandSplitRevokeType.REWARD.equals(revokeType)) {
            return ERRAND_SPLIT_REVOKE_REWARD_METHOD;
        }
        return ERRAND_SPLIT_REVOKE_FULL_METHOD;
    }

    private Map<String, Object> buildErrandSplitMethodParam(BzOrderDO order) {
        Map<String, Object> methodParam = new HashMap<>();
        methodParam.put("orderSn", order.getOrderSn());
        methodParam.put("paySn", order.getPaySn());
        methodParam.put("payAmount", order.getPayAmount());
        methodParam.put("balanceAmount", order.getBalanceAmount());
        methodParam.put("errandRewardAmount", order.getErrandRewardAmount());
        methodParam.put("errandStatus", order.getErrandStatus());
        return methodParam;
    }

    private String getTaskIdByCustId(JSONObject splitResp, String custIdReceiv, boolean required) {
        if (splitResp == null) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账返回记录不能为空");
        }
        if (ObjectUtil.isEmpty(custIdReceiv)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账接收商户号不能为空");
        }
        JSONArray taskList = splitResp.getJSONArray("taskList");
        if (taskList == null || taskList.isEmpty()) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账任务列表不能为空");
        }
        for (int i = 0; i < taskList.size(); i++) {
            JSONObject task = taskList.getJSONObject(i);
            if (task != null && Objects.equals(custIdReceiv, task.getString("custIdReceiv"))) {
                String taskId = task.getString("taskId");
                if (ObjectUtil.isEmpty(taskId)) {
                    throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付分账任务ID不能为空");
                }
                return taskId;
            }
        }
        if (required) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "未找到对应的星驿付分账任务");
        }
        return null;
    }

    public enum ErrandSplitRevokeType {
        FULL,
        FOOD,
        REWARD
    }

    public boolean isErrandRewardSplitCrossDay(String paySn, LocalDateTime createTime) {
        SysPayRecordDO splitRecord = getErrandRewardSplitRecord(paySn, createTime);
        if (ObjectUtil.isEmpty(splitRecord)) {
            return false;
        }
        return !LocalDate.now().equals(splitRecord.getCreateTime().toLocalDate());
    }

    /**
     * 星驿付B扫C
     *
     * @param reqVO
     * @return
     */
    public JSONObject xingYiBScanCdoPay(XingYiPayReqVO reqVO) {
        validateXingYiCustId(reqVO.getTerminalSn());
        Map<String, String> map = new HashMap<>();
        SysPayRecordDO sysPayRecord = new SysPayRecordDO();
        sysPayRecord.setMethodName("bScanCdoPay");
        sysPayRecord.setOrderId(reqVO.getPaySn());
        sysPayRecord.setCreateTime(reqVO.getCreateTime());
        sysPayRecord.setMethodParam(JSON.toJSONString(reqVO));
        sysPayRecord.setAgetId(agetId);
        sysPayRecord.setCustId(reqVO.getTerminalSn());
        int amount = AmountUtil.yuan2Fen(new BigDecimal(reqVO.getPayAmount()));

        map.put("txamt", String.valueOf(amount));
        map.put("code", reqVO.getDynamicId());
        map.put("type", "P");
        map.put("orderNo", reqVO.getPaySn());
        map.put("custId", reqVO.getTerminalSn());
        map.put("agetId", agetId);
        map.put("remark", NOTIFY_URL);
//        map.put("outTime", String.valueOf(DateUtils.trunToSubCurrMinute(xingYiPay.getTimeOut(), DateUtils.YYYYMMDDHHMMSS))); TODO

        try {
            map.put("tradingIp", IpUtils.getClientIp());
        } catch (Exception e) {
            map.put("tradingIp", "127.0.0.1");
        }

        //星驿付支付
        JSONObject from = this.sendPaymentRequest(map, OrderConstants.B_TO_C_E_O_LINK_PAY);
        if (!ObjectUtil.equals(from.get("code").toString(), "000000") && !ObjectUtil.equals(from.get("code").toString(), "555555") && !ObjectUtil.equals(from.get("code").toString(), "222222")) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), (String) from.get("msg"));
        }
        JSONObject data = JSONObject.from(from.get("data"));

        sysPayRecord.setThridOrderNo(data.get("orderNo").toString());
        sysPayRecord.setMethodReturn(JSON.toJSONString(from.get("data")));
        sysPayRecordService.save(sysPayRecord);

        return data;
    }

    /**
     * 发起请求
     *
     * @param params
     * @param path
     * @return
     */
    private JSONObject sendPaymentRequest(Map<String, String> params, String path) {
        String url = eOLinkUrl + path;

        params.put("asyncNotify", SLD_API_URL);
        params.put("version", "1.0.0");
        params.put("timeStamp", DateUtils.dateTimeNow());

        String signStr = SHA1Utils.ASCII(params, null);
        //验证码需要计算出来 根据参数计算出来通过sha1加密后一起放入参数.调用接口时使用
        String sha256 = SHA1Utils.getSHA256(signStr);
        String sign = SHA1Utils.encrypt(publicKey, sha256);
        params.put("sign", sign);

        log.info("星驿付请求: {} | 参数: {}", url, JSON.toJSONString(params));

        String response = HttpUtil.post(url, JSON.toJSONString(params));
        JSONObject jsonResponse = JSON.parseObject(response);

        log.info("星驿付响应: {}", LogUtil.maskSensitiveInfo(jsonResponse));
        return jsonResponse;
    }

    private JSONObject sendSplitRequest(Map<String, Object> params, String path) {
        String url = eOLinkUrl + path;

        params.put("version", "1.0.0");
        params.put(Objects.equals(path, OrderConstants.XING_YI_SPLIT) ? "timeStamp" : "timestamp", DateUtils.dateTimeNow());

        String signStr = SHA1Utils.ASCII(toSignMap(params), null);
        String sha256 = SHA1Utils.getSHA256(signStr);
        String sign = SHA1Utils.encrypt(publicKey, sha256);
        params.put("sign", sign);

        log.info("星驿付分账请求 URL: {} | 参数: {}", url, LogUtil.maskSensitiveInfo(JSON.toJSONString(params)));
        String response = HttpUtil.post(url, JSON.toJSONString(params));
        JSONObject jsonResponse = JSON.parseObject(response);

        log.info("星驿付分账响应: {}", LogUtil.maskSensitiveInfo(jsonResponse));
        return jsonResponse;
    }

    private Map<String, String> toSignMap(Map<String, Object> params) {
        Map<String, String> signMap = new HashMap<>();
        params.forEach((key, value) -> {
            if (key != null && value != null && !"sign".equals(key)) {
                signMap.put(key, value instanceof String ? value.toString() : JSON.toJSONString(value));
            }
        });
        return signMap;
    }

    private SysPayRecordDO getErrandRewardSplitRecord(String paySn, LocalDateTime createTime) {
        return sysPayRecordService.getOne(
                new LambdaQueryWrapper<SysPayRecordDO>()
                        .eq(SysPayRecordDO::getOrderId, paySn)
                        .eq(SysPayRecordDO::getCreateTime, createTime)
                        .eq(SysPayRecordDO::getMethodName, ERRAND_REWARD_SPLIT_METHOD)
                        .eq(SysPayRecordDO::getPayUrlResult, PAY_RECORD_STATUS_SUCCESS)
                        .last("LIMIT 1")
        );
    }

    /**
     * 验签
     *
     * @param body
     * @return
     */
    public boolean verifySignature(Map<String, Object> body) {
        try {
            String sign = (String) body.get("sign");
            Map<String, String> params = body.entrySet().stream()
                    .filter(e -> !"sign".equals(e.getKey()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().toString()));

            String calculatedHash = SHA1Utils.getSHA256(SHA1Utils.ASCII(params, null));
            String decryptedSign = SHA1Utils.publicKeyDecrypt2(publicKey, sign);

            return calculatedHash.equals(decryptedSign);
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 二次支付查询
     *
     * @param reqVO
     * @return
     */
    public JSONObject zfbSecondaryPayment(XingYiPayReqVO reqVO) {
        SysPayRecordDO sysPayRecordDO = sysPayRecordService.getOne(
                new LambdaQueryWrapper<SysPayRecordDO>()
                        .eq(SysPayRecordDO::getOrderId, reqVO.getPaySn())
                        .eq(SysPayRecordDO::getCreateTime, reqVO.getCreateTime())
        );
        if (ObjectUtil.isEmpty(sysPayRecordDO)) {
            throw exception(ORDER_PAY_RECORD_NOT_EXISTS);
        }
        return JSONObject.parseObject(sysPayRecordDO.getMethodReturn());
    }

    private void validateXingYiCustId(String custId) {
        if (ObjectUtil.isEmpty(custId)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "星驿付商户号不能为空");
        }
    }
}
