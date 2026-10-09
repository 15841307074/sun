package com.htyoudao.youdao.module.order.service.pay;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mq.rabbitmq.enums.RabbitMQConstant;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityActivityDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.commodity.api.VO.CommodityActivityVO;
import com.htyoudao.youdao.module.commodity.enums.StockChangeEnum;
import com.htyoudao.youdao.module.errand.api.runner.ErrandRunnerApi;
import com.htyoudao.youdao.module.member.api.point.PointLogApi;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.BzOrderProductVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.ErrandFullRefundReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.ErrandRefundReqVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity.CommodityCondimentsVO;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.OrderPayReqVO;
import com.htyoudao.youdao.module.order.controller.app.pay.VO.XingYiPayReqVO;
import com.htyoudao.youdao.module.order.config.ErrandOrderConfig;
import com.htyoudao.youdao.module.order.controller.print.VO.PrintConfigVO;
import com.htyoudao.youdao.module.order.core.pay.XingYiPayService;
import com.htyoudao.youdao.module.order.core.pay.XingYiPayService.ErrandSplitRevokeType;
import com.htyoudao.youdao.module.order.core.submit.DTO.ActivityNjnzInfoDTO;
import com.htyoudao.youdao.module.order.core.submit.DTO.SubmitCacheDTO;
import com.htyoudao.youdao.module.order.core.ylb.service.CreateOrderService;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.*;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderPayMapper;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderMapper;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderCondimentsMapper;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderProductMapper;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderProductSonMapper;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderPurchaseMapper;
import com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.order.enums.*;
import com.htyoudao.youdao.module.order.framework.order.config.WechatAppConfig;
import com.htyoudao.youdao.module.order.service.order.BzOrderCondimentsService;
import com.htyoudao.youdao.module.order.service.order.BzOrderLogService;
import com.htyoudao.youdao.module.order.service.order.BzOrderService;
import com.htyoudao.youdao.module.order.util.*;
import com.htyoudao.youdao.module.order.util.feie.FeiESendUtil;
import com.htyoudao.youdao.module.order.util.xingye.XpYunSendUtil;
import com.htyoudao.youdao.module.promotion.api.activitycq.ActivityCqApi;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activityjk.ActivityJkApi;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.api.lottery.LotteryApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UsedCouponReqVO;
import com.htyoudao.youdao.module.system.api.printer.PrinterApi;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterSettingDTO;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterTableVO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.PUSH_HASH_KEY;
import static com.htyoudao.youdao.framework.common.constants.RedisKeyConstants.REQ_HASH_KEY;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants.COMMODITY_LOTTERY_MEMBERID;

/**
 * <p>
 * 支付 服务实现类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-03
 */
@DS(DsNameConstants.SHARDING)
@RefreshScope
@Slf4j
@Service
public class BzOrderPayServiceImpl extends ServiceImpl<BzOrderPayMapper, BzOrderPayDO> implements BzOrderPayService {

    @Resource
    private CallbackOrderDetailCache callbackOrderDetailCache;

    private static final String ORDER_REFUND_LOCK_KEY = "order:refund:lock:%s";
    private static final long ORDER_REFUND_LOCK_SECONDS = 60L;
    private static final int ERRAND_REWARD_REFUNDED = 1;
    private static final int ERRAND_FOOD_REFUNDED = 2;
    private static final String ERRAND_REFUND_TYPE_FULL = "FULL";
    private static final String ERRAND_REFUND_TYPE_FOOD = "FOOD";
    private static final String ERRAND_REFUND_TYPE_REWARD = "REWARD";
    private static final String ERRAND_REFUND_METHOD_FULL = "errandFullRefund";
    private static final String ERRAND_REFUND_METHOD_FOOD = "errandFoodRefund";
    private static final String ERRAND_REFUND_METHOD_REWARD = "errandRewardRefund";
    private final Map<String, SimulationPrintTask> simulationPrintTasks = new ConcurrentHashMap<>();

    //集点开关 默认关
    @Value("${notifyswitch.pointscollect:false}")
    private Boolean notifyPointsCollectSwitch;

    //积分开关 默认关
    @Value("${notifyswitch.addpoint:false}")
    private Boolean notifyAddPointSwitch;

    //集卡开关 默认关
    @Value("${notifyswitch.jk:false}")
    private Boolean notifyJkSwitch;

    //转盘开关 默认关
    @Value("${notifyswitch.activity:false}")
    private Boolean notifyActivitySwitch;

    //微信服务通知开关 默认关
    @Value("${notifyswitch.wxnotice:false}")
    private Boolean notifyWxNoticeSwitch;

    @DubboReference
    private StoreApi storeApi;
    @DubboReference
    private PrinterApi printerApi;
    @DubboReference
    private UserCouponApi userCouponApi;
    @DubboReference
    private PointLogApi pointLogApi;
    @DubboReference
    private WxActionApi wxActionApi;
    @DubboReference
    private CommodityApi commodityApi;
    @DubboReference
    private LotteryApi lotteryApi;
    @DubboReference
    private ActivityJkApi activityJkApi;
    @DubboReference
    private ActivityCqApi activityCqApi;
    @DubboReference
    private ErrandRunnerApi errandRunnerApi;
    @DubboReference
    private ActivityApi activityApi;


    @Resource
    private BzOrderService bzOrderService;
    @Resource
    private ErrandOrderConfig errandOrderConfig;
    @Resource
    private BzOrderMapper bzOrderMapper;
    @Resource
    private BzOrderCondimentsMapper bzOrderCondimentsMapper;
    @Resource
    private BzOrderProductMapper bzOrderProductMapper;
    @Resource
    private BzOrderProductSonMapper bzOrderProductSonMapper;
    @Resource
    private BzOrderPurchaseMapper bzOrderPurchaseMapper;
    @Resource
    private SysPayRecordService sysPayRecordService;
    @Resource
    private XingYiPayService xingYiPayService;
    @Resource
    private WechatAppConfig wechatAppConfig;
    @Resource
    private BzOrderCondimentsService iBzOrderCondimentsService;
    @Resource
    private BzOrderLogService iBzOrderLogService;
    @Resource
    private CreateOrderService ylbCreateOrderService;
    @Resource
    private RabbitMQService rabbitMQService;
    @Resource
    private ThreadPoolExecutor strongExecutor;
    @Resource
    private ThreadPoolExecutor weakExecutor;
    @Resource
    private ThreadPoolExecutor ioExecutor;
    @Resource
    private ThreadPoolExecutor simulationPrintExecutor;
    @Resource
    protected StringRedisTemplate stringRedisTemplate;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResult<JSONObject> doPay(OrderPayReqVO reqVO) {

        SubmitCacheDTO submitCacheDTO = this.getSubmitCacheDTO(reqVO);

        //二次支付
        if (!ObjectUtils.isEmpty(reqVO.getIsRepeatPay()) && reqVO.getIsRepeatPay() == 1) {
            try {
                return CommonResult.success(
                        xingYiPayService.zfbSecondaryPayment(
                                XingYiPayReqVO.builder()
                                        .paySn(submitCacheDTO.getPaySn())
                                        .createTime(submitCacheDTO.getCreateTime())
                                        .build()
                        )
                );
            } catch (Exception e) {
                // 二次未查询到支付记录 继续执行
                log.info("==> 调用星驿付二次未查询到支付记录 {}", e.getMessage());
            }
        }

        CommonResult<StoreDTO> storeResult = storeApi.getStoreByStoreId(submitCacheDTO.getStoreId());
        StoreDTO storeDTO = storeResult.getCheckedData();
        String terminalSn = this.getPayTerminalSn(storeDTO, submitCacheDTO.getOrderType());

        XingYiPayReqVO xingYiPayReqVO = XingYiPayReqVO.builder()
                .orderSn(reqVO.getOrderSn())
                .paySn(submitCacheDTO.getPaySn())
                .payWay(submitCacheDTO.getPaymentCode())
                .appId(wechatAppConfig.getAppId(submitCacheDTO.getBusinessId()))
                .secret(wechatAppConfig.getAppSecret(submitCacheDTO.getBusinessId()))
                .terminalSn(terminalSn)
                .payAmount(submitCacheDTO.getPayAmount().toString())
                .createTime(submitCacheDTO.getCreateTime())
                .build();

        JSONObject data;
        try {
            data = xingYiPayService.xingYiPay(xingYiPayReqVO);
        } catch (ServiceException e) {
            log.warn("==> 调用星驿付统一下单失败", e);
            if (ObjectUtils.isEmpty(terminalSn)) {
                return CommonResult.error(ORDER_NOT_CONFIG_PAY_CHANNEL_NO);
            }
            return CommonResult.error(e);
        }

        //更新三方单号
        this.update(
                new LambdaUpdateWrapper<BzOrderPayDO>()
                        .eq(BzOrderPayDO::getPaySn, submitCacheDTO.getPaySn())
                        .eq(BzOrderPayDO::getCreateTime, submitCacheDTO.getCreateTime())
                        .set(BzOrderPayDO::getTradeSn, data.get("orderNo").toString())
        );
        return CommonResult.success(data);
    }

    //切换渠道
    public static void main0(String[] args) {

        // 1) 多个 custId
        List<String> custIds = Arrays.asList(
        );

        // 2) 固定参数（除 custId / sign 外）
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("agetId", "61000000472395");
        baseParams.put("bakType", "01");
        baseParams.put("wxQdh", "863238759");
//        baseParams.put("smallAppid", "wx3b84773f5f12d87f");
        baseParams.put("mAppid", "64010200012026020616354800002664");
        baseParams.put("version", "1.0.0");

        String url = "https://yyfsvxm.postar.cn/yyfsevr/custConfig/config";
        String publicKey = "CLOUD_SECRET_REQUIRED";

        // 3) 汇总结果（custId -> 响应JSON / 或错误信息）
        Map<String, Object> resultMap = new LinkedHashMap<>();

        for (String custId : custIds) {
            try {
                // 每次循环都复制一份，避免 sign 污染 baseParams
                Map<String, String> params = new HashMap<>(baseParams);

                params.put("custId", custId);
                params.put("timeStamp", DateUtils.dateTimeNow());

                // 关键：每个 custId 都要重新算签名
                String signStr = SHA1Utils.ASCII(params, null);
                String sha256 = SHA1Utils.getSHA256(signStr);
                String sign = SHA1Utils.encrypt(publicKey, sha256);
                params.put("sign", sign);

                log.info("发起切换请求 => custId={} | URL: {} | 参数: {}", custId, url, LogUtil.maskSensitiveParams(params));

                String response = HttpUtil.post(url, JSON.toJSONString(params));
                JSONObject jsonResponse = JSON.parseObject(response);

                log.info("支付响应结果 => custId={} | {}", custId, LogUtil.maskSensitiveInfo(jsonResponse));

                resultMap.put(custId, jsonResponse);
            } catch (Exception e) {
                log.error("请求失败 => custId={}", custId, e);
                resultMap.put(custId, "ERROR: " + e.getMessage());
            }
        }

        log.info("全部 custId 调用完成 => {}", LogUtil.maskSensitiveInfo(resultMap));
    }

    public static void main(String[] args) {

        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("agetId", "61000000472395");
        baseParams.put("custId", "60000007402606");
        baseParams.put("version", "1.0.0");

        String url = "https://yyfsvxm.postar.cn/yyfsevr/auth/wxApplyResult";
        String publicKey = "CLOUD_SECRET_REQUIRED";

        // 3) 汇总结果（custId -> 响应JSON / 或错误信息）
        Map<String, Object> resultMap = new LinkedHashMap<>();


        // 每次循环都复制一份，避免 sign 污染 baseParams
        Map<String, String> params = new HashMap<>(baseParams);

        params.put("timeStamp", DateUtils.dateTimeNow());

        // 关键：每个 custId 都要重新算签名
        String signStr = SHA1Utils.ASCII(params, null);
        String sha256 = SHA1Utils.getSHA256(signStr);
        String sign = SHA1Utils.encrypt(publicKey, sha256);
        params.put("sign", sign);

        log.info("发起请求 =>  URL: {} | 参数: {}", url, LogUtil.maskSensitiveParams(params));

        String response = HttpUtil.post(url, JSON.toJSONString(params));

        log.info("响应结果 => {}", LogUtil.maskSensitiveInfo(response));
    }

    /**
     * 获取订单
     *
     * @param orderSn
     * @return
     */
    private BzOrderDO getBzOrderDOWithRetry(String orderSn) {
        BzOrderDO bzOrderDO = bzOrderService.getOne(
                new LambdaQueryWrapper<BzOrderDO>()
                        .eq(BzOrderDO::getOrderSn, orderSn)
        );

        if (bzOrderDO != null) {
            return bzOrderDO;
        }

        log.info("==> 订单 {} redis，mysql都没有查询到", orderSn);
        //重试后仍未查到
        throw exception(ORDER_RE_PAY);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public JSONObject bScanCdoPay(OrderPayReqVO reqVO) {
        SubmitCacheDTO submitCacheDTO = this.getSubmitCacheDTO(reqVO);

        String paySn = submitCacheDTO.getPaySn();
        //二次支付
        if (!ObjectUtils.isEmpty(reqVO.getIsRepeatPay()) && reqVO.getIsRepeatPay() == 1) {
            String payRand = SerialNumberGenerator.generateSerialNumberFive();
            paySn = paySn + "_" + payRand;
        }

        CommonResult<StoreDTO> storeResult = storeApi.getStoreByStoreId(submitCacheDTO.getStoreId());
        StoreDTO storeDTO = storeResult.getCheckedData();
        String terminalSn = this.getPayTerminalSn(storeDTO, submitCacheDTO.getOrderType());

        if (ObjectUtils.isEmpty(terminalSn)) {
            throw exception(ORDER_NOT_CONFIG_PAY_CHANNEL_NO);
        }

        //支付
        XingYiPayReqVO xingYiPayReqVO = XingYiPayReqVO.builder()
                .orderSn(reqVO.getOrderSn())
                .paySn(paySn)
                .payWay(submitCacheDTO.getPaymentCode())
                .appId(wechatAppConfig.getAppId(submitCacheDTO.getBusinessId()))
                .secret(wechatAppConfig.getAppSecret(submitCacheDTO.getBusinessId()))
                .dynamicId(reqVO.getDynamicId())
                .terminalSn(terminalSn)
                .payAmount(submitCacheDTO.getPayAmount().toString())
                .createTime(submitCacheDTO.getCreateTime())
                .build();

        //更新三方单号
        JSONObject data = xingYiPayService.xingYiBScanCdoPay(xingYiPayReqVO);

        //更新三方单号
        this.update(
                new LambdaUpdateWrapper<BzOrderPayDO>()
                        .eq(BzOrderPayDO::getPaySn, submitCacheDTO.getPaySn())
                        .eq(BzOrderPayDO::getCreateTime, submitCacheDTO.getCreateTime())
                        .set(BzOrderPayDO::getTradeSn, data.get("orderNo").toString())
        );
        return data;
    }

    private String getPayTerminalSn(StoreDTO storeDTO, Integer orderType) {
        if (Objects.equals(OrderTypeEnum.ERRAND.getCode(), orderType)) {
            return storeDTO.getTerminalKey();
        }
        return storeDTO.getTerminalSn();
    }

    /**
     * 获取缓存信息
     *
     * @param reqVO
     * @return
     */
    private SubmitCacheDTO getSubmitCacheDTO(OrderPayReqVO reqVO) {
        SubmitCacheDTO submitCacheDTO = new SubmitCacheDTO();
        String submitKey = RedisKeyConstants.ORDER_SUBMIT_CACHE + reqVO.getOrderSn();
        String submitCache = stringRedisTemplate.opsForValue().get(submitKey);

        if (ObjectUtils.isEmpty(submitCache)) {
            //订单
            BzOrderDO bzOrderDO = this.getBzOrderDOWithRetry(reqVO.getOrderSn());
            //支付单
            BzOrderPayDO bzOrderPayDO = this.getCheckBzOrderPayDO(bzOrderDO.getOrderSn());

            submitCacheDTO.setPayAmount(bzOrderDO.getPayAmount());
            submitCacheDTO.setOrderSn(bzOrderDO.getOrderSn());
            submitCacheDTO.setPaySn(bzOrderPayDO.getPaySn());
            submitCacheDTO.setBusinessId(bzOrderDO.getBusinessId());
            submitCacheDTO.setCreateTime(bzOrderDO.getCreateTime());
            submitCacheDTO.setStoreId(bzOrderDO.getStoreId());
            submitCacheDTO.setPaymentCode(bzOrderPayDO.getPaymentCode());
            submitCacheDTO.setOrderType(bzOrderDO.getOrderType());

            log.info("==> 支付获取订单缓存信息失败，查询DB:{}", submitCacheDTO);

        } else {
            submitCacheDTO = JSON.parseObject(submitCache, SubmitCacheDTO.class);
            if (ObjectUtils.isEmpty(submitCacheDTO.getOrderType())) {
                BzOrderDO bzOrderDO = this.getBzOrderDOWithRetry(reqVO.getOrderSn());
                submitCacheDTO.setOrderType(bzOrderDO.getOrderType());
            }
            log.info("==> 支付获取订单缓存信息:{}", submitCacheDTO);
        }


        return submitCacheDTO;
    }

    @Override
    public BzOrderDO refund(OrderPayReqVO reqVO) {
        return executeWithRefundLock(reqVO.getOrderSn(), () -> doRefund(reqVO));
    }

    @Override
    public void refundWaitingAcceptErrandPayment(BzOrderDO order) {
        executeWithRefundLock(order.getOrderSn(), () -> {
            doRefundWaitingAcceptErrandPayment(order);
            return order;
        });
    }

    private void doRefundWaitingAcceptErrandPayment(BzOrderDO order) {
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())
                || !Objects.equals(OrderStateEnum.WAITING_ACCEPT.getCode(), order.getOrderState())) {
            throw exception(ORDER_WRONG_STATE);
        }

        BzOrderPayDO bzOrderPayDO = this.getBzOrderPayDO(order.getOrderSn());
        String returnAmount = bzOrderPayDO.getPayAmount().toString();
        if (PaymentMethodEnum.CASH.getCode() != Integer.parseInt(order.getPaymentCode()) &&
                new BigDecimal(returnAmount).setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) > 0) {
            xingYiPayService.xingYiRefund(
                    XingYiPayReqVO.builder()
                            .payWay(PaymentMethodEnum.getEngMsgByCode(Integer.parseInt(order.getPaymentCode())))
                            .paySn(bzOrderPayDO.getPaySn())
                            .payAmount(returnAmount)
                            .createTime(bzOrderPayDO.getCreateTime())
                            .build()
            );
        }
    }

    private BzOrderDO doRefund(OrderPayReqVO reqVO) {
        //订单
        BzOrderDO bzOrderDO = bzOrderService.getBzOrderDO(reqVO.getOrderSn());
        if (Objects.equals(OrderStateEnum.PENDING_REFUND.getCode(), bzOrderDO.getOrderState())) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "订单已发起退款，请勿重复提交");
        }

        //支付单
        BzOrderPayDO bzOrderPayDO = this.getBzOrderPayDO(reqVO.getOrderSn());
        String returnAmount = bzOrderPayDO.getPayAmount().toString();

        //回退优惠券
        if (ObjectUtil.isNotEmpty(bzOrderDO.getUserCouponId())
//                && bzOrderDO.getOrderFrom() == OrderFromEnum.WECHAT_MINI_PROGRAM.getCode()
        ) {
            //超时回退优惠券
            userCouponApi.usedCoupon(
                    UsedCouponReqVO.builder()
                            .userCouponId(bzOrderDO.getUserCouponId())
                            .memberId(bzOrderDO.getMemberId())
                            .isUsed(OrderConstants.NO)
                            .build()
            );
        }

        //只有不是现金单，并且金额大于0才能走星驿付退款
        if (PaymentMethodEnum.CASH.getCode() != Integer.parseInt(bzOrderDO.getPaymentCode()) &&
                new BigDecimal(returnAmount).setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) > 0) {
            xingYiPayService.xingYiRefund(
                    XingYiPayReqVO.builder()
                            .payWay(PaymentMethodEnum.getEngMsgByCode(Integer.parseInt(bzOrderDO.getPaymentCode())))
                            .paySn(bzOrderPayDO.getPaySn())
                            .payAmount(returnAmount)
                            .createTime(bzOrderPayDO.getCreateTime())
                            .build()
            );
        }

        //更新状态
        bzOrderDO.setOrderState(OrderStateEnum.PENDING_REFUND.getCode());
        bzOrderService.updateOrderState(bzOrderDO);

        OrderDetailDTO detail = bzOrderService.getDetail(bzOrderDO.getOrderSn());

        weakExecutor.submit(() -> {
            try {
                log.info("==> 【订单退款】kafka发消息 {}", bzOrderDO.getOrderSn());
                bzOrderService.notifyOrder(bzOrderDO.getOrderSn(), "DELETE");
            } catch (Exception e) {
                log.error("==> 【订单退款】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
            }

            try {
                log.info("==> 【退款单】缓存商品销量- orderSn {}", bzOrderDO.getOrderSn());
                bzOrderService.incrementProductSales(detail.getProductDOList(), true);
                bzOrderService.incrementPurchaseSales(detail.getPurchaseDOList(), true);
            } catch (Exception e) {
                log.error("==> 【退款单】缓存商品销量- 失败 orderSn {}", bzOrderDO.getOrderSn(), e);
            }
        });

        strongExecutor.submit(() -> {
            if (!Objects.equals(bzOrderDO.getOrderFrom(), OrderFromEnum.POINT_SINGLE_MACHINE.getCode())) {
                try {
                    activityApi.refundMzGiftInventory(bzOrderDO.getOrderSn());
                } catch (Exception e) {
                    log.error("==> 【退款单】恢复满赠库存失败 orderSn={}", bzOrderDO.getOrderSn(), e);
                }
            }
            try {
                log.info("==> 【退款单】扣减原材料库存 {}", bzOrderDO.getOrderSn());
                commodityApi.cancelReturnStock(bzOrderDO.getOrderSn());
            } catch (Exception e) {
                log.error("==> 【退款单】扣减原材料库存 失败 {}", bzOrderDO.getOrderSn(), e);
            }

            //集点
            if (bzOrderDO.getMemberId() > 0) {
                try {
                    log.info("==> 【退款单】集点记录删除 {}", bzOrderDO.getOrderSn());
                    bzOrderService.delOrderPointsByOrderId(bzOrderDO.getOrderId());
                } catch (Exception e) {
                    log.error("==> 【退款单】集点记录删除 失败 {}", bzOrderDO.getOrderSn(), e);
                }
            }
        });

        return bzOrderDO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BzOrderDO errandFullRefund(ErrandFullRefundReqVO reqVO) {
        return this.errandFullRefund(reqVO.getOrderSn());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BzOrderDO errandFullRefund(OrderPayReqVO reqVO) {
        return this.errandFullRefund(reqVO.getOrderSn());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BzOrderDO errandFullRefund(String orderSn) {
        return executeWithRefundLock(orderSn, () -> doErrandRefund(orderSn, ERRAND_REFUND_TYPE_FULL));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BzOrderDO errandRefund(ErrandRefundReqVO reqVO) {
        return executeWithRefundLock(reqVO.getOrderSn(), () -> doErrandRefund(reqVO.getOrderSn(), reqVO.getRefundType()));
    }

    private BzOrderDO doErrandRefund(String orderSn, String refundType) {
        BzOrderDO order = bzOrderService.getBzOrderDO(orderSn);
        if (!Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType())) {
            throw exception(ORDER_WRONG_TYPE);
        }
        if (Objects.equals(OrderStateEnum.UNPAID.getCode(), order.getOrderState())
                || Objects.equals(OrderStateEnum.CANCELED.getCode(), order.getOrderState())) {
            throw exception(ORDER_WRONG_STATE);
        }
        if (isErrandRefundCrossDay(order) && !errandOrderConfig.isRefundCrossDayWhitelist(order.getOrderSn())) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "代取订单暂不支持跨天退款");
        }

        BzOrderPayDO orderPay = this.getBzOrderPayDO(orderSn);
        BigDecimal payAmount = orderPay.getPayAmount() == null ? BigDecimal.ZERO : orderPay.getPayAmount();
        BigDecimal rewardAmount = order.getErrandRewardAmount() == null ? BigDecimal.ZERO : order.getErrandRewardAmount();
        BigDecimal foodAmount = payAmount.subtract(rewardAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "订单支付金额不能为空");
        }
        if (rewardAmount.compareTo(BigDecimal.ZERO) < 0 || foodAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "代取订单退款金额异常");
        }

        int errandStatus = order.getErrandStatus() == null ? 0 : order.getErrandStatus();
        boolean rewardRefunded = hasErrandRefundFlag(errandStatus, ERRAND_REWARD_REFUNDED);
        boolean foodRefunded = hasErrandRefundFlag(errandStatus, ERRAND_FOOD_REFUNDED);
        String normalizedType = normalizeErrandRefundType(refundType);
        validateErrandRefundState(order, normalizedType, rewardRefunded, foodRefunded);

        if (ERRAND_REFUND_TYPE_REWARD.equals(normalizedType)) {
            return doErrandRewardRefund(order, orderPay, rewardAmount, errandStatus, rewardRefunded);
        }
        if (ERRAND_REFUND_TYPE_FOOD.equals(normalizedType)) {
            return doErrandFoodRefund(order, orderPay, foodAmount, errandStatus, foodRefunded);
        }
        return doErrandFullRefund(order, orderPay, payAmount, foodAmount, errandStatus, rewardRefunded, foodRefunded);
    }

    private BzOrderDO doErrandRewardRefund(BzOrderDO order, BzOrderPayDO orderPay, BigDecimal rewardAmount,
                                           int errandStatus, boolean rewardRefunded) {
        if (rewardRefunded) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "跑腿赏金已退款，请勿重复提交");
        }
        if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "跑腿赏金退款金额不能为空");
        }

        xingYiPayService.xingYiErrandSplitRevoke(order, ErrandSplitRevokeType.REWARD);
        refundErrandPayment(order, orderPay, rewardAmount, ERRAND_REFUND_METHOD_REWARD);
        refundErrandRunnerReward(order);
        int newErrandStatus = errandStatus | ERRAND_REWARD_REFUNDED;
        updateErrandRefundStatus(order, newErrandStatus);
        order.setErrandStatus(newErrandStatus);
        return order;
    }

    private BzOrderDO doErrandFoodRefund(BzOrderDO order, BzOrderPayDO orderPay, BigDecimal foodAmount,
                                         int errandStatus, boolean foodRefunded) {
        if (foodRefunded) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "餐费已退款，请勿重复提交");
        }
        if (foodAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "餐费退款金额不能为空");
        }

        xingYiPayService.xingYiErrandSplitRevoke(order, ErrandSplitRevokeType.FOOD);
        refundErrandPayment(order, orderPay, foodAmount, ERRAND_REFUND_METHOD_FOOD);
        markErrandOrderRefunded(order, errandStatus | ERRAND_FOOD_REFUNDED);
        return order;
    }

    private BzOrderDO doErrandFullRefund(BzOrderDO order, BzOrderPayDO orderPay, BigDecimal payAmount, BigDecimal foodAmount,
                                         int errandStatus, boolean rewardRefunded, boolean foodRefunded) {
        if (foodRefunded) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "餐费已退款，不能重复整单退款");
        }
        if (rewardRefunded) {
            if (foodAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "餐费退款金额不能为空");
            }
            xingYiPayService.xingYiErrandSplitRevoke(order, ErrandSplitRevokeType.FOOD);
            refundErrandPayment(order, orderPay, foodAmount, ERRAND_REFUND_METHOD_FULL);
            markErrandOrderRefunded(order, errandStatus | ERRAND_FOOD_REFUNDED);
            return order;
        }

        xingYiPayService.xingYiErrandSplitRevoke(order, ErrandSplitRevokeType.FULL);
        refundErrandPayment(order, orderPay, payAmount, ERRAND_REFUND_METHOD_FULL);
        refundErrandRunnerReward(order);
        markErrandOrderRefunded(order, errandStatus | ERRAND_REWARD_REFUNDED | ERRAND_FOOD_REFUNDED);
        return order;
    }

    private String normalizeErrandRefundType(String refundType) {
        if (ObjectUtils.isEmpty(refundType)) {
            return ERRAND_REFUND_TYPE_FULL;
        }
        String normalizedType = refundType.trim().toUpperCase(Locale.ROOT);
        if (!ERRAND_REFUND_TYPE_FULL.equals(normalizedType)
                && !ERRAND_REFUND_TYPE_FOOD.equals(normalizedType)
                && !ERRAND_REFUND_TYPE_REWARD.equals(normalizedType)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "代取退款类型错误");
        }
        return normalizedType;
    }

    private void validateErrandRefundState(BzOrderDO order, String refundType, boolean rewardRefunded, boolean foodRefunded) {
        if (ERRAND_REFUND_TYPE_REWARD.equals(refundType) && rewardRefunded) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "跑腿赏金已退款，请勿重复提交");
        }
        if (ERRAND_REFUND_TYPE_FOOD.equals(refundType) && foodRefunded) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "餐费已退款，请勿重复提交");
        }
        if (ERRAND_REFUND_TYPE_FULL.equals(refundType) && foodRefunded) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "餐费已退款，不能重复整单退款");
        }
        if (Objects.equals(OrderStateEnum.PENDING_REFUND.getCode(), order.getOrderState())
                && !ERRAND_REFUND_TYPE_REWARD.equals(refundType)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "订单已退款，不能重复提交");
        }
        if (Objects.equals(OrderStateEnum.PENDING_REFUND.getCode(), order.getOrderState()) && !foodRefunded) {
            throw exception(ORDER_WRONG_STATE);
        }
    }

    private boolean hasErrandRefundFlag(int errandStatus, int flag) {
        return (errandStatus & flag) == flag;
    }

    private void refundErrandPayment(BzOrderDO order, BzOrderPayDO orderPay, BigDecimal refundAmount, String methodName) {
        if (PaymentMethodEnum.CASH.getCode() == Integer.parseInt(order.getPaymentCode())
                || refundAmount.setScale(2, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        xingYiPayService.xingYiRefund(
                XingYiPayReqVO.builder()
                        .payWay(PaymentMethodEnum.getEngMsgByCode(Integer.parseInt(order.getPaymentCode())))
                        .paySn(orderPay.getPaySn())
                        .payAmount(refundAmount.setScale(2, RoundingMode.HALF_UP).toPlainString())
                        .createTime(orderPay.getCreateTime())
                        .build(),
                methodName
        );
    }

    private void updateErrandRefundStatus(BzOrderDO order, int errandStatus) {
        boolean updated = bzOrderService.update(
                new LambdaUpdateWrapper<BzOrderDO>()
                        .set(BzOrderDO::getErrandStatus, errandStatus)
                        .eq(BzOrderDO::getOrderSn, order.getOrderSn())
                        .eq(BzOrderDO::getCreateTime, order.getCreateTime())
        );
        if (!updated) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "更新代取退款状态失败");
        }
    }

    private void markErrandOrderRefunded(BzOrderDO order, int errandStatus) {
        rollbackCouponIfNecessary(order);
        order.setOrderState(OrderStateEnum.PENDING_REFUND.getCode());
        order.setErrandStatus(errandStatus);
        bzOrderService.updateOrderState(order);
        submitOrderRefundAfterTasks(order);
    }

    private void rollbackCouponIfNecessary(BzOrderDO order) {
        if (ObjectUtil.isEmpty(order.getUserCouponId())) {
            return;
        }
        userCouponApi.usedCoupon(
                UsedCouponReqVO.builder()
                        .userCouponId(order.getUserCouponId())
                        .memberId(order.getMemberId())
                        .isUsed(OrderConstants.NO)
                        .build()
        );
    }

    private void submitOrderRefundAfterTasks(BzOrderDO order) {
        OrderDetailDTO detail = bzOrderService.getDetail(order.getOrderSn());
        weakExecutor.submit(() -> {
            try {
                log.info("==> 【订单退款】kafka发消息 {}", order.getOrderSn());
                bzOrderService.notifyOrder(order.getOrderSn(), "DELETE");
            } catch (Exception e) {
                log.error("==> 【订单退款】kafka发消息 失败 {}", order.getOrderSn(), e);
            }

            try {
                log.info("==> 【退款单】缓存商品销量- orderSn {}", order.getOrderSn());
                bzOrderService.incrementProductSales(detail.getProductDOList(), true);
                bzOrderService.incrementPurchaseSales(detail.getPurchaseDOList(), true);
            } catch (Exception e) {
                log.error("==> 【退款单】缓存商品销量- 失败 orderSn {}", order.getOrderSn(), e);
            }
        });

        strongExecutor.submit(() -> {
            try {
                log.info("==> 【退款单】扣减原材料库存 {}", order.getOrderSn());
                commodityApi.cancelReturnStock(order.getOrderSn());
            } catch (Exception e) {
                log.error("==> 【退款单】扣减原材料库存 失败 {}", order.getOrderSn(), e);
            }

            if (order.getMemberId() > 0) {
                try {
                    log.info("==> 【退款单】集点记录删除 {}", order.getOrderSn());
                    bzOrderService.delOrderPointsByOrderId(order.getOrderId());
                } catch (Exception e) {
                    log.error("==> 【退款单】集点记录删除 失败 {}", order.getOrderSn(), e);
                }
            }
        });
    }

    private BzOrderDO executeWithRefundLock(String orderSn, Supplier<BzOrderDO> action) {
        String lockKey = String.format(ORDER_REFUND_LOCK_KEY, orderSn);
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, String.valueOf(System.currentTimeMillis()), ORDER_REFUND_LOCK_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "退款处理中，请勿重复提交");
        }
        try {
            return action.get();
        } finally {
            releaseRefundLockAfterTransaction(lockKey);
        }
    }

    private void releaseRefundLockAfterTransaction(String lockKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            stringRedisTemplate.delete(lockKey);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                stringRedisTemplate.delete(lockKey);
            }
        });
    }

    private OrderPayReqVO buildOrderPayReqVO(String orderSn) {
        OrderPayReqVO payReqVO = new OrderPayReqVO();
        payReqVO.setOrderSn(orderSn);
        return payReqVO;
    }

    private void refundErrandRunnerReward(BzOrderDO order) {
        BigDecimal rewardAmount = order.getErrandRewardAmount() == null ? BigDecimal.ZERO : order.getErrandRewardAmount();
        if (order.getDeliveryId() == null || rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Boolean deductResult = errandRunnerApi.refundRewardDeduct(order.getDeliveryId(), order.getOrderSn(), rewardAmount).getCheckedData();
        if (!Boolean.TRUE.equals(deductResult)) {
            throw new ServiceException(PAY_COMMON_EXCEPTION.getCode(), "跑腿赏金扣回失败");
        }
    }

    private boolean isErrandRefundCrossDay(BzOrderDO order) {
        LocalDate refundBaseDate = order.getPayTime() == null ? order.getCreateTime().toLocalDate() : order.getPayTime().toLocalDate();
        return !LocalDate.now().equals(refundBaseDate);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Map<String, String> notify(Map<String, Object> body) {
        log.info("==> 【notify】回传 | body: {}", body);

        HashMap<String, String> returnObj = new HashMap<>() {
            @Serial
            private static final long serialVersionUID = 8483692266366583673L;

            {
                put("rspCod", "");
                put("rspMsg", "success");
            }
        };

        try {
            // 1) 基础参数
            String paySn = String.valueOf(body.get("THREE_ORDER_NO"));
            String thridNo = String.valueOf(body.get("T_PAY_NO"));

            // 实际入账金额（极端情况下第一次会是 0，支付方会二次回调）
            BigDecimal netrAmtBd = new BigDecimal(String.valueOf(body.get("NETR_AMT")));
            BigDecimal netrAmt = BigDecimal.valueOf(AmountUtil.fen2Yuan(netrAmtBd)); // yuan

            // 2) 签名验证
            if (!xingYiPayService.verifySignature(body)) {
                return returnObj;
            }

            // 3) paySn 可能带 _ 后缀，取主单号查询
            String sortPaySn = paySn;
            if (paySn.contains("_")) {
                sortPaySn = paySn.split("_")[0];
            }

            // 4) 查支付记录
            BzOrderPayDO bzOrderPayDO = this.getOne(
                    new LambdaQueryWrapper<BzOrderPayDO>()
                            .eq(BzOrderPayDO::getPaySn, sortPaySn)
                            .eq(BzOrderPayDO::getCreateTime, DateUtils.parseOrderTimeSafe(sortPaySn))
            );

            if (ObjectUtils.isEmpty(bzOrderPayDO)) {
                log.warn("==> 【notify】支付记录为空 | paySn {}", paySn);
                returnObj.put("rspMsg", "error");
                return returnObj;
            }

            BusinessContextHolder.setBusinessId(bzOrderPayDO.getBusinessId());

            // 5) 查订单（补金额也要用到）
            BzOrderDO bzOrderDO = bzOrderService.getBzOrderDO(bzOrderPayDO.getOrderSn());
            if (ObjectUtils.isEmpty(bzOrderDO)) {
                log.error("==> 【notify】订单不存在 | orderSn={}, paySn={}", bzOrderPayDO.getOrderSn(), paySn);
                returnObj.put("rspMsg", "error");
                return returnObj;
            }

            boolean alreadyProcessed = OrderConstants.YES.toString().equals(bzOrderPayDO.getApiPayState());

            // =========================
            // 二次回调：只允许“补写入账金额”，其余流程不走
            // 条件：已处理过 && 订单当前 balanceAmount==0 && 本次 netrAmt>0
            // 并发安全：where balanceAmount=0 的条件更新，保证只补一次
            // =========================
            BigDecimal currentBalance = bzOrderDO.getBalanceAmount() == null ? BigDecimal.ZERO : bzOrderDO.getBalanceAmount();
            boolean needPatchAmount = alreadyProcessed
                    && currentBalance.compareTo(BigDecimal.ZERO) == 0
                    && netrAmt.compareTo(BigDecimal.ZERO) > 0;

            if (needPatchAmount) {
                boolean patched = bzOrderService.update(
                        new LambdaUpdateWrapper<BzOrderDO>()
                                .set(BzOrderDO::getBalanceAmount, netrAmt)
                                .eq(BzOrderDO::getOrderSn, bzOrderDO.getOrderSn())
                                .eq(BzOrderDO::getCreateTime, bzOrderDO.getCreateTime())
                                .eq(BzOrderDO::getBalanceAmount, BigDecimal.ZERO)
                );

                if (patched) {
                    log.warn("==> 【notify】二次回调补写入账金额成功 | orderSn={}, patchAmt={}", bzOrderDO.getOrderSn(), netrAmt);
                } else {
                    log.warn("==> 【notify】二次回调补金额被跳过（可能已补过/并发）| orderSn={}", bzOrderDO.getOrderSn());
                }

                // 二次回调补金额结束：直接返回，避免积分/销量/库存/MQ/kafka/打印重复执行
                return returnObj;
            }

            // =========================
            // 已处理过且不需要补金额：直接返回（幂等）
            // =========================
            if (alreadyProcessed) {
                log.info("==> 【notify】重复回调直接返回 | orderSn={}, paySn={}", bzOrderDO.getOrderSn(), bzOrderPayDO.getPaySn());
                return returnObj;
            }

            // =========================
            // 第一次回调：走全流程
            // =========================

            // 更新订单状态
            bzOrderDO.setExpressNumber(thridNo);
            if (Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderDO.getOrderType())) {
                bzOrderDO.setOrderState(OrderStateEnum.WAITING_ACCEPT.getCode());
            } else {
                bzOrderDO.setOrderState(OrderStateEnum.MAKING.getCode());
            }
            bzOrderDO.setPayTime(LocalDateTime.now());
            bzOrderDO.setPickUpNum(
                    OrderTypeEnum.TAKEAWAY.getCode() == bzOrderDO.getOrderType()
                            ? bzOrderService.getWmPickupCode(bzOrderDO.getStoreId())
                            : OrderTypeEnum.ERRAND.getCode() == bzOrderDO.getOrderType()
                            ? bzOrderService.getDqPickupCode(bzOrderDO.getStoreId())
                            : bzOrderService.getPickupCode(bzOrderDO.getStoreId())
            );
            // 第一次回调也写入（可能为0，后续二次补）
            bzOrderDO.setBalanceAmount(netrAmt);

            try {
                bzOrderService.updateFinalOrderFinishTime(bzOrderDO.getMemberId(), bzOrderDO);
            } catch (Exception e) {
                log.error("==> 【notify】更新最后下单时间失败 {}", bzOrderDO.getOrderSn(), e);
            }

            bzOrderService.updateOrderState(bzOrderDO);
            if (Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrderDO.getOrderType())) {
                bzOrderService.addErrandWaitingAcceptCancelTimerTask(bzOrderDO);
            }

            // 更新支付表：标记已处理（幂等开关）
            this.update(
                    new LambdaUpdateWrapper<BzOrderPayDO>()
                            .set(BzOrderPayDO::getApiPayState, OrderConstants.YES)
                            .set(BzOrderPayDO::getPayAmount, bzOrderDO.getPayAmount())
                            .set(BzOrderPayDO::getPaySn, paySn)
                            .eq(BzOrderPayDO::getPayId, bzOrderPayDO.getPayId())
                            .eq(BzOrderPayDO::getCreateTime, bzOrderPayDO.getCreateTime())
            );

            // 复用后续打印等流程需要的明细，判断满赠不新增 Redis/MySQL 查询。
            OrderDetailDTO cachedDetail = callbackOrderDetailCache.get(bzOrderDO.getBusinessId(), bzOrderDO.getOrderSn());
            OrderDetailDTO detail = cachedDetail != null ? cachedDetail : bzOrderService.getDetail(bzOrderDO.getOrderSn());

            if (!Objects.equals(bzOrderDO.getOrderFrom(), OrderFromEnum.POINT_SINGLE_MACHINE.getCode())
                    && detail.getProductDOList().stream()
                    .anyMatch(product -> OrderConstants.YES.equals(product.getIsGift()))) {
                try {
                    activityApi.confirmMzGiftInventory(bzOrderDO.getOrderSn());
                } catch (Exception e) {
                    log.error("==> 【支付成功】确认满赠库存失败 orderSn={}", bzOrderDO.getOrderSn(), e);
                }
            }

            // 记录支付log
            try {
                SysPayRecordDO sysPayRecordDO = new SysPayRecordDO();
                sysPayRecordDO.setMethodName("xyfNotify");
                sysPayRecordDO.setCreateTime(LocalDateTime.now());
                sysPayRecordDO.setMethodParam(JSON.toJSONString(body));
                sysPayRecordDO.setOrderId(String.valueOf(body.get("THREE_ORDER_NO")));
                sysPayRecordDO.setMethodReturn(JSON.toJSONString(returnObj));
                sysPayRecordDO.setBusinessId(bzOrderDO.getBusinessId());
                sysPayRecordDO.setCreateTime(bzOrderDO.getCreateTime());
                sysPayRecordDO.setCreator(bzOrderDO.getCreator());
                sysPayRecordService.save(sysPayRecordDO);
            } catch (Exception e) {
                log.error("==> 【notify】记录支付log失败 {}", bzOrderDO.getOrderSn(), e);
            }

            ioExecutor.submit(() -> {
                // 小票打印
                try {
                    log.info("==> 【notify】小票打印 {}", bzOrderDO.getOrderSn());
                    this.printReceipt(bzOrderDO, detail.getProductDOList(), detail.getProductSonDOList(), detail.getPurchaseDOList());
                } catch (Exception e) {
                    log.error("==> 【notify】小票打印 失败 {}", bzOrderDO.getOrderSn(), e);
                }

                Object push = stringRedisTemplate.opsForHash().get(PUSH_HASH_KEY, bzOrderDO.getStoreId().toString());
                log.info("==> 【notify】推送订单 {}", push);

                // 外卖调用云喇叭
                if (bzOrderDO.getOrderType() == OrderTypeEnum.TAKEAWAY.getCode() && ObjectUtil.isNotEmpty(push)) {
                    try {
                        log.info("==> 【notify】云喇叭推送订单 {}", bzOrderDO.getOrderSn());
                        ylbCreateOrderService.sendOrderRequest(bzOrderDO);
                    } catch (Exception e) {
                        log.error("==> 【notify】云喇叭推送订单 失败 {}", bzOrderDO.getOrderSn(), e);
                    }
                }
            });

            strongExecutor.submit(() -> {
                // 扣减原材料库存
                try {
                    log.info("==> 【notify】扣减原材料库存 {}", bzOrderDO.getOrderSn());
                    bzOrderService.changeStock(bzOrderDO, detail, StockChangeEnum.SALE);
                } catch (Exception e) {
                    log.error("==> 【notify】扣减原材料库存 失败 {}", bzOrderDO.getOrderSn(), e);
                }

                // 添加积分
                if (bzOrderDO.getMemberId() > 0 && notifyAddPointSwitch) {
                    try {
                        log.info("==> 【notify】增加积分 {}", bzOrderDO.getOrderSn());

                        ClientAddMemberPointReqVO reqVO = new ClientAddMemberPointReqVO();
                        reqVO.setMemberId(bzOrderDO.getMemberId())
                                .setBusinessId(bzOrderDO.getBusinessId())
                                .setMemberId(bzOrderDO.getMemberId())
                                .setOrderSn(bzOrderDO.getOrderSn())
                                .setPayAmount(bzOrderDO.getPayAmount())
                                .setOrderAmount(bzOrderDO.getOrderAmount());
                        pointLogApi.addMemberPoint(reqVO);
                    } catch (Exception e) {
                        log.error("==> 【notify】增加积分 失败 {}", bzOrderDO.getOrderSn(), e);
                    }
                }

                // 集卡
                if (bzOrderDO.getMemberId() > 0 && notifyJkSwitch) {
                    try {
                        log.info("==> 【notify】集卡 {}", bzOrderDO.getOrderSn());
                        List<BzOrderProductDO> productDOList = detail.getProductDOList();
                        Set<Integer> productTypeSet = new HashSet<>();
                        for (BzOrderProductDO item : productDOList) {
                            productTypeSet.add(item.getIsSingle());
                        }

                        ActivityJkOrderReqDTO reqDTO = new ActivityJkOrderReqDTO();
                        reqDTO.setStoreId(bzOrderDO.getStoreId())
                                .setMemberId(bzOrderDO.getMemberId())
                                .setCommodityIds(
                                        productDOList.stream().map(BzOrderProductDO::getCommodityId).collect(Collectors.toList())
                                )
                                .setOrderProductType(productTypeSet.size() == 1 ? productTypeSet.iterator().next() : 3)
                                .setPaymentAmount(bzOrderDO.getPayAmount());

                        activityJkApi.getActivityJkList(reqDTO);
//                        activityCqApi.getActivityCqList(reqDTO);
                    } catch (Exception e) {
                        log.error("==> 【notify】集卡 失败 {}", bzOrderDO.getOrderSn(), e);
                    }
                }

                // 集点
                if (bzOrderDO.getMemberId() > 0 && notifyPointsCollectSwitch) {
                    try {
                        log.info("==> 【notify】集点 {}", bzOrderDO.getOrderSn());
                        detail.setBzOrderDO(bzOrderDO);
                        bzOrderService.addOrderPoints(detail);
                    } catch (Exception e) {
                        log.error("==> 【notify】集点 失败 {}", bzOrderDO.getOrderSn(), e);
                    }
                }
            });

            weakExecutor.submit(() -> {
                // 发MQ
                this.sendMessage(bzOrderDO.getStoreId(), bzOrderDO.getOrderSn());

                // 优惠活动商品（抽奖）
                if ((bzOrderDO.getOrderFrom() == OrderFromEnum.ALIPAY_MINI_PROGRAM.getCode()
                        || bzOrderDO.getOrderFrom() == OrderFromEnum.WECHAT_MINI_PROGRAM.getCode())
                        && PaymentMethodEnum.CASH.getCode() != Integer.parseInt(bzOrderDO.getPaymentCode())) {
                    log.info("==> 【notify】抽奖活动商品 {}", bzOrderDO.getOrderSn());
                    try {
                        if (notifyActivitySwitch) {
                            this.lotteryHandleOrder(
                                    bzOrderDO.getMemberId(),
                                    bzOrderDO.getStoreId(),
                                    detail.getProductDOList().stream().map(BzOrderProductDO::getCommodityId).toList()
                            );
                        }
                    } catch (Exception e) {
                        log.error("==> 【notify】抽奖活动商品 失败 {}", bzOrderDO.getOrderSn(), e);
                    }
                }

                // 缓存商品销量
                try {
                    log.info("==> 【notify】缓存商品销量 orderSn {}", bzOrderDO.getOrderSn());
                    bzOrderService.incrementProductSales(detail.getProductDOList(), false);
                    bzOrderService.incrementPurchaseSales(detail.getPurchaseDOList(), false);
                } catch (Exception e) {
                    log.error("==> 【notify】缓存商品销量 失败 orderSn {}", bzOrderDO.getOrderSn(), e);
                }

                // kafka 发消息
                try {
                    log.info("==> 【notify】kafka发消息 {}", bzOrderDO.getOrderSn());
                    bzOrderService.notifyOrder(bzOrderDO.getOrderSn(), "INSERT");
                } catch (Exception e) {
                    log.info("==> 【notify】kafka发消息 失败 {}", bzOrderDO.getOrderSn(), e);
                }

                // 发送小程序消息
                try {
                    if (notifyWxNoticeSwitch) {
                        log.info("==> 【notify】发送小程序消息 orderSn {}", bzOrderDO.getOrderSn());

                        AppletNoticePushVO appletNoticePush = new AppletNoticePushVO();
                        appletNoticePush.setProjectOwnerShip(String.valueOf(10));
                        appletNoticePush.setTemplateType(AppletPushTemplateTypeEnum.PLACE_ORDER_SUCCESS.getCode());
                        appletNoticePush.setOpenId(bzOrderDO.getOpenId());
                        appletNoticePush.setBusinessId(bzOrderDO.getBusinessId());

                        List<String> valueList = new ArrayList<>();
                        valueList.add(OrderTypeEnum.getMsgByCode(bzOrderDO.getOrderType()));
                        valueList.add(bzOrderDO.getStoreName());
                        valueList.add(bzOrderDO.getPickUpNum());
                        wxActionApi.sendAppletNotice(valueList, appletNoticePush);
                    }
                } catch (Exception e) {
                    log.warn("==> 【notify】发送小程序消息 失败 orderSn {}", bzOrderDO.getOrderSn(), e);
                }
            });

            return returnObj;
        } catch (Exception e) {
            log.error("==> 【notify】回调处理异常 {}", body.get("THREE_ORDER_NO"), e);
            returnObj.put("rspMsg", "error");
            return returnObj;
        }
    }

    /**
     * 获取转盘商品
     *
     * @param memberId
     * @param commodityIds
     */
    public void lotteryHandleOrder(Long memberId, Long storeId, List<Long> commodityIds) {
        Map<Long, List<Long>> lotteryMap = lotteryApi.getLotteryList(storeId, memberId);
        log.info("==> 【notify】获取抽奖活动信息 {}", lotteryMap);

        if (lotteryMap == null || lotteryMap.isEmpty() || commodityIds == null || commodityIds.isEmpty()) {
            return;
        }
        //yyyyMMdd
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        for (Map.Entry<Long, List<Long>> entry : lotteryMap.entrySet()) {
            Long lotteryId = entry.getKey();
            List<Long> commodityList = entry.getValue();

            // 是否匹配成功
            boolean matched = false;
            if (commodityList == null || commodityList.isEmpty()) {
                // 对所有商品生效
                matched = true;
            } else {
                for (Long commodityId : commodityIds) {
                    if (commodityList.contains(commodityId)) {
                        matched = true;
                        break;
                    }
                }
            }

            if (matched) {
                String redisKey = String.format(COMMODITY_LOTTERY_MEMBERID, memberId, lotteryId, today);
                Long newValue = stringRedisTemplate.opsForValue().increment(redisKey);

                // 设置过期时间为当天 23:59:59
                if (newValue != null && newValue == 1) {
                    LocalDateTime expireTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).withNano(0);
                    long seconds = expireTime.atZone(ZoneId.systemDefault()).toEpochSecond()
                            - LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();

                    stringRedisTemplate.expire(redisKey, seconds, TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * 获取转盘商品
     *
     * @param bzOrder
     * @param productDOList
     */
    @Deprecated
    private void getActivityCommodityByMemberId(BzOrderDO bzOrder, List<BzOrderProductDO> productDOList) {
        log.info("-----------------------------orderSn={}", bzOrder.getOrderSn());

        String currDate = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date());
        String key = String.format(COMMODITY_LOTTERY_MEMBERID, bzOrder.getMemberId(), currDate);
        String codeFromRedis = Convert.toStr(stringRedisTemplate.opsForValue().get(key));

        List<Long> commodityActivityIds = productDOList.stream().map(BzOrderProductDO::getCommodityId).filter(ObjectUtil::isNotNull).distinct().collect(Collectors.toList());
        List<CommodityActivityDTO> commodityActivityList = commodityApi.getCommodityActivityList(new CommodityActivityVO(commodityActivityIds)).getCheckedData();

        log.info("-----------------------------获取commodity_activity中活动商品数据={}", commodityActivityList);
        log.info("-----------------------------获取redis中数据的value={}", codeFromRedis);
        log.info("----------------------------- redis KEY{}", key);
        if (ObjectUtil.isNotNull(codeFromRedis)) {
            log.info("-----------------------------通过redis中取数据={}", codeFromRedis);
            if (ObjectUtil.isNotEmpty(commodityActivityList)) {
                stringRedisTemplate.opsForValue().increment(key);
            }
        } else {
            // 从数据库中获取数据
            if (ObjectUtil.isNotEmpty(commodityActivityList)) {
                log.info("-----------------------------通过DB中取数据，存入redis=======");
                // 获取当前时间
                Duration durationToEndOfToday = this.getDuration();
                // 设置键在今天剩余时间后失效 将数据写入缓存
                stringRedisTemplate.opsForValue().set(key, "1");
                stringRedisTemplate.expire(key, durationToEndOfToday.getSeconds(), TimeUnit.SECONDS);
                log.info("------------------------ 存入redis======= 成功");
            }
        }
    }

    /**
     * 获取当前时间到今天结束的时间差
     */
    private Duration getDuration() {
        LocalDateTime now = LocalDateTime.now();
        // 获取今天结束时间（即明天零点）
        LocalDateTime endOfToday = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 23, 59, 59);
        // 计算当前时间到今天结束时间的时间差
        return Duration.between(now, endOfToday);
    }

    private void sendMessage(Long storeId, String orderSn) {
        log.info("==> 【notify】发送MQ消息 {} {}", String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), orderSn);
        rabbitMQService.sendMessage(String.format(RabbitMQConstant.EXCHANGE_NAME, storeId), "", orderSn);
    }

    @Override
    public int startSimulatedPrint(Long storeId, LocalDate date, String printerSn, String printerBrand) {
        if (storeId == null || date == null || org.springframework.util.StringUtils.isEmpty(printerSn)) {
            throw new IllegalArgumentException("storeId、date 和 printerSn 不能为空");
        }
        if (!"飞鹅".equals(printerBrand) && !"芯烨".equals(printerBrand)) {
            throw new IllegalArgumentException("printerBrand 仅支持飞鹅或芯烨");
        }

        stopAllSimulatedPrints();

        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.plusDays(1).atStartOfDay();
        String tableFix = SubTableUtil.getTableListByDateSingle("", DateUtils.localDateTimeToDate(startTime));
        log.info("模拟打印请求：门店={}, 日期={}, 订单表={}, 打印机SN={}, 打印机品牌={}",
                storeId, date, "bz_order" + tableFix, printerSn, printerBrand);
        List<BzOrderDO> orders = bzOrderMapper.selectValidListByStoreIdAndCreateTime(
                tableFix, storeId, startTime, endTime);
        if (CollectionUtils.isEmpty(orders)) {
            log.warn("模拟打印未查询到有效订单：门店={}, 日期={}, 订单表={}",
                    storeId, date, "bz_order" + tableFix);
            return 0;
        }

        String taskKey = buildSimulationPrintTaskKey(storeId, date, printerSn);
        SimulationPrintTask task = new SimulationPrintTask();
        simulationPrintTasks.put(taskKey, task);
        try {
            task.setFuture(simulationPrintExecutor.submit(() -> runSimulationPrintTask(
                    taskKey, task, orders, tableFix, printerSn, printerBrand)));
            log.info("模拟打印任务已提交：任务标识={}, 订单数={}", taskKey, orders.size());
        } catch (RuntimeException e) {
            simulationPrintTasks.remove(taskKey, task);
            log.error("模拟打印任务提交失败：任务标识={}", taskKey, e);
            throw e;
        }
        return orders.size();
    }

    @Override
    public int stopAllSimulatedPrints() {
        int stoppedCount = 0;
        for (Map.Entry<String, SimulationPrintTask> entry : simulationPrintTasks.entrySet()) {
            SimulationPrintTask task = entry.getValue();
            if (task.stop()) {
                stoppedCount++;
            }
            simulationPrintTasks.remove(entry.getKey(), task);
        }
        if (stoppedCount > 0) {
            log.info("已停止全部模拟打印任务：任务数={}", stoppedCount);
        }
        return stoppedCount;
    }

    private void runSimulationPrintTask(String taskKey, SimulationPrintTask task, List<BzOrderDO> orders,
                                        String tableFix, String printerSn, String printerBrand) {
        try {
            log.info("模拟打印任务开始执行：任务标识={}, 订单数={}, 打印机SN={}, 打印机品牌={}",
                    taskKey, orders.size(), printerSn, printerBrand);
            int startIndex = findSimulationStartIndex(orders, LocalTime.now());
            BzOrderDO immediateOrder = orders.get(startIndex);
            log.info("模拟打印选中最近订单立即打印：任务标识={}, 订单号={}, 创建时间={}, 跳过更早订单数={}",
                    taskKey, immediateOrder.getOrderSn(), immediateOrder.getCreateTime(), startIndex);
            for (int index = startIndex; index < orders.size(); index++) {
                BzOrderDO order = orders.get(index);
                if (isSimulationPrintStopped(task)) {
                    log.info("模拟打印任务已停止，跳过订单：任务标识={}, 订单号={}", taskKey, order.getOrderSn());
                    return;
                }
                boolean printImmediately = index == startIndex;
                if (!printImmediately && order.getCreateTime() != null) {
                    LocalDateTime expectedPrintTime = LocalDateTime.of(LocalDate.now(), order.getCreateTime().toLocalTime());
                    long delayMillis = Math.max(Duration.between(LocalDateTime.now(), expectedPrintTime).toMillis(), 0);
                    if (delayMillis > 0) {
                        log.info("模拟打印等待订单创建时刻：任务标识={}, 订单号={}, 创建时间={}, 等待毫秒={}, 预计打印时间={}",
                                taskKey, order.getOrderSn(), order.getCreateTime(), delayMillis, expectedPrintTime);
                        waitForSimulationPrintDelay(taskKey, order.getOrderSn(), delayMillis, task);
                        log.info("模拟打印等待结束：任务标识={}, 订单号={}", taskKey, order.getOrderSn());
                    } else {
                        log.info("订单创建时刻已过，立即打印：任务标识={}, 订单号={}, 创建时间={}",
                                taskKey, order.getOrderSn(), order.getCreateTime());
                    }
                } else if (printImmediately) {
                    log.info("模拟打印启动后立即打印最近订单：任务标识={}, 订单号={}, 创建时间={}",
                            taskKey, order.getOrderSn(), order.getCreateTime());
                }
                if (isSimulationPrintStopped(task)) {
                    log.info("模拟打印任务等待后已停止：任务标识={}, 订单号={}", taskKey, order.getOrderSn());
                    return;
                }
                try {
                    log.info("模拟打印开始处理订单：任务标识={}, 订单号={}, 创建时间={}",
                            taskKey, order.getOrderSn(), order.getCreateTime());
                    OrderDetailDTO detail = getSimulationOrderDetail(tableFix, order.getOrderSn());
                    List<BzOrderCondimentsDO> condiments = bzOrderCondimentsMapper
                            .selectSimulationList(tableFix, order.getOrderSn());
                    log.info("模拟打印订单详情加载完成：任务标识={}, 订单号={}, 商品数={}, 套餐子商品数={}, 加购商品数={}, 小料数={}",
                            taskKey, order.getOrderSn(), detail.getProductDOList().size(),
                            detail.getProductSonDOList().size(), detail.getPurchaseDOList().size(), condiments.size());
                    doSendSimulationPrinter(order, detail.getProductDOList(), detail.getProductSonDOList(),
                            detail.getPurchaseDOList(), Objects.equals(OrderTypeEnum.ERRAND.getCode(), order.getOrderType()),
                            printerSn, printerBrand, true, () -> isSimulationPrintStopped(task), condiments);
                    log.info("模拟打印订单发送完成：任务标识={}, 订单号={}", taskKey, order.getOrderSn());
                } catch (Exception e) {
                    log.error("模拟打印订单失败：任务标识={}, 订单号={}", taskKey, order.getOrderSn(), e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("模拟打印任务已停止：任务标识={}", taskKey);
        } finally {
            simulationPrintTasks.remove(taskKey, task);
            log.info("模拟打印任务结束并清理：任务标识={}, 是否停止={}", taskKey, task.isStopped());
        }
    }

    private boolean isSimulationPrintStopped(SimulationPrintTask task) {
        return task.isStopped() || Thread.currentThread().isInterrupted();
    }

    private void waitForSimulationPrintDelay(String taskKey, String orderSn, long delayMillis,
                                              SimulationPrintTask task) throws InterruptedException {
        long remainingMillis = delayMillis;
        long nextProgressLogMillis = Math.max(delayMillis - TimeUnit.MINUTES.toMillis(1), 0);
        while (remainingMillis > 0) {
            if (isSimulationPrintStopped(task)) {
                return;
            }
            long waitMillis = Math.min(remainingMillis, TimeUnit.SECONDS.toMillis(1));
            Thread.sleep(waitMillis);
            remainingMillis -= waitMillis;
            if (remainingMillis <= nextProgressLogMillis && remainingMillis > 0) {
                log.info("模拟打印仍在等待：任务标识={}, 订单号={}, 剩余毫秒={}", taskKey, orderSn, remainingMillis);
                nextProgressLogMillis = Math.max(nextProgressLogMillis - TimeUnit.MINUTES.toMillis(1), 0);
            }
        }
    }

    private String buildSimulationPrintTaskKey(Long storeId, LocalDate date, String printerSn) {
        return storeId + ":" + date + ":" + printerSn;
    }

    private int findSimulationStartIndex(List<BzOrderDO> orders, LocalTime currentTime) {
        int firstOrderIndex = 0;
        int latestPastOrderIndex = -1;
        for (int index = 0; index < orders.size(); index++) {
            LocalDateTime createTime = orders.get(index).getCreateTime();
            if (createTime == null) {
                continue;
            }
            if (!createTime.toLocalTime().isAfter(currentTime)) {
                latestPastOrderIndex = index;
            }
        }
        return latestPastOrderIndex >= 0 ? latestPastOrderIndex : firstOrderIndex;
    }

    private OrderDetailDTO getSimulationOrderDetail(String tableFix, String orderSn) {
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setProductDOList(bzOrderProductMapper.selectSimulationList(tableFix, orderSn));
        detail.setProductSonDOList(bzOrderProductSonMapper.selectSimulationList(tableFix, orderSn));
        detail.setPurchaseDOList(bzOrderPurchaseMapper.selectSimulationList(tableFix, orderSn));
        return detail;
    }

    private void printReceipt(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts, List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases) {
        // 打印小票的具体逻辑
        log.info("=================打印小票开始=====================" + bzOrder.getOrderSn() + ",    ---" + bzOrder.getPickUpNum());
        this.sendPrinter(bzOrder, orderProducts, sons, bzOrderPurchases);
    }

    /**
     * 1.根据订单变好组装打印数据/ 判断打印机进行打印
     *
     * @param bzOrder
     * @param orderProducts
     * @param sons
     * @return
     */
    @Override
    public void sendPrinter(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts, List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases) {
        if (bzOrder != null && Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrder.getOrderType())) {
            return;
        }
        doSendPrinter(bzOrder, orderProducts, sons, bzOrderPurchases, false);
    }

    @Override
    public void sendErrandPrinter(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts, List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases) {
        if (bzOrder == null || !Objects.equals(OrderTypeEnum.ERRAND.getCode(), bzOrder.getOrderType())) {
            return;
        }
        doSendPrinter(bzOrder, orderProducts, sons, bzOrderPurchases, true);
    }

    private void doSendPrinter(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts, List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases, boolean errandMode) {
        doSendSimulationPrinter(bzOrder, orderProducts, sons, bzOrderPurchases,
                errandMode, null, null, false, null, null);
    }

    private void doSendSimulationPrinter(BzOrderDO bzOrder, List<BzOrderProductDO> orderProducts,
                                         List<BzOrderProductSonDO> sons, List<BzOrderPurchaseDO> bzOrderPurchases,
                                         boolean errandMode, String targetPrinterSn, String targetPrinterBrand,
                                         boolean forceSingleCopy, BooleanSupplier shouldStop,
                                         List<BzOrderCondimentsDO> simulationCondiments) {
        log.info("进入打印发送处理：订单号={}, 商品数={}, 模拟打印机SN={}, 模拟打印机品牌={}",
                bzOrder == null ? null : bzOrder.getOrderSn(), orderProducts == null ? 0 : orderProducts.size(),
                targetPrinterSn, targetPrinterBrand);
        //查询订单
//        BzOrderDO bzOrder = bzOrderService.getBzOrderDO(orderSn);
        if (bzOrder != null) {
            Boolean isPD = false;
            if (StringUtils.isNotEmpty(bzOrder.getExpressCode()) && bzOrder.getPromotionDiscountAmount() != null && bzOrder.getPromotionDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                isPD = true;
            }
            log.info("开始查询门店打印联单配置：订单号={}, 门店={}", bzOrder.getOrderSn(), bzOrder.getStoreId());
            CommonResult<List<PrinterTableVO>> tableListResult =
                    printerApi.getPrinterByStoreIdAndPrintType(bzOrder.getStoreId(), 2);
            List<PrinterTableVO> printerTableVOList = tableListResult.getCheckedData();
            log.info("查询门店打印联单配置：订单号={}, 门店={}, 配置数={}, 模拟打印机SN={}",
                    bzOrder.getOrderSn(), bzOrder.getStoreId(),
                    printerTableVOList == null ? 0 : printerTableVOList.size(), targetPrinterSn);
            if (CollectionUtils.isEmpty(printerTableVOList)
                    && org.springframework.util.StringUtils.hasText(targetPrinterSn)) {
                PrinterTableVO simulationPrinter = new PrinterTableVO();
                simulationPrinter.setStoreId(bzOrder.getStoreId());
                simulationPrinter.setPrinterSerialNumber(targetPrinterSn);
                simulationPrinter.setPrinterBrand(targetPrinterBrand);
                simulationPrinter.setPrinterLocation("1");
                printerTableVOList = new ArrayList<>(Collections.singletonList(simulationPrinter));
                log.warn("门店未配置打印联单，模拟打印使用默认门店单：订单号={}, 打印机SN={}",
                        bzOrder.getOrderSn(), targetPrinterSn);
            }
            if (CollectionUtils.isEmpty(printerTableVOList)) {
                log.warn("门店未配置打印联单，跳过打印：订单号={}, 门店={}",
                        bzOrder.getOrderSn(), bzOrder.getStoreId());
            }
            if (CollectionUtils.isNotEmpty(printerTableVOList)) {
                log.info("门店打印联单配置加载完成：订单号={}, 配置数={}, 是否模拟打印={}",
                        bzOrder.getOrderSn(), printerTableVOList.size(), targetPrinterSn != null);
                //查询订单商品详情组装小票样式  区分打印机位置
                for (PrinterTableVO printerTable : printerTableVOList) {
                    if (shouldStop != null && shouldStop.getAsBoolean()) {
                        return;
                    }
                    String actualPrinterSn = org.springframework.util.StringUtils.hasText(targetPrinterSn)
                            ? targetPrinterSn : printerTable.getPrinterSerialNumber();
                    String actualPrinterBrand = org.springframework.util.StringUtils.hasText(targetPrinterBrand)
                            ? targetPrinterBrand : printerTable.getPrinterBrand();
                    printerTable.setPrinterSerialNumber(actualPrinterSn);
                    printerTable.setPrinterBrand(actualPrinterBrand);
                    if (printerTable.getPrinterLocation() == null) {
                        printerTable.setPrinterLocation("1");
                    }
                    List<PrinterSettingDTO> printerSettings = new ArrayList<>();
                    if (StringUtils.isNotBlank(printerTable.getPrinterSettingIds())) {
                        log.info("开始查询联单明细：订单号={}, 联单配置ID={}",
                                bzOrder.getOrderSn(), printerTable.getPrinterSettingIds());
                        List<Long> printerSettingIds = convertStringToList(printerTable.getPrinterSettingIds());
                        printerSettings = printerApi.selectPrinterSettingListByIds(printerSettingIds).getCheckedData();
                    }
                    if (CollectionUtils.isEmpty(printerSettings)) {
                        if (org.springframework.util.StringUtils.hasText(targetPrinterSn)) {
                            PrinterSettingDTO defaultSetting = new PrinterSettingDTO();
                            defaultSetting.setDocumentType(PrinterTomplateTypeEnum.STORE.getStatus());
                            defaultSetting.setPrintCopies(1);
                            printerSettings = Collections.singletonList(defaultSetting);
                            log.warn("联单明细为空，模拟打印使用默认门店单：订单号={}, 打印机SN={}",
                                    bzOrder.getOrderSn(), targetPrinterSn);
                        } else {
                            log.warn("联单明细为空，跳过该打印配置：订单号={}, 配置SN={}",
                                    bzOrder.getOrderSn(), printerTable.getPrinterSerialNumber());
                            continue;
                        }
                    }
                    log.info("开始查询门店打印模板：订单号={}, 门店={}", bzOrder.getOrderSn(), bzOrder.getStoreId());
                    CommonResult<StoreDTO> storeResult = storeApi.getStoreByStoreId(bzOrder.getStoreId());
                    StoreDTO storeDTO = storeResult.getCheckedData();

                    PrintConfigVO printConfigVO = JSON.parseObject(storeDTO.getPrinterTemplate(), PrintConfigVO.class);

//                    List<BzOrderProductDO> orderProducts = bzOrderProductService.list(
//                            new LambdaQueryWrapper<BzOrderProductDO>()
//                                    .eq(BzOrderProductDO::getOrderSn, bzOrder.getOrderSn())
//                    );

                    List<BzOrderProductVO> orderProductList = BeanCopyUtils.copyBeanList(orderProducts, BzOrderProductVO.class);
                    if (!orderProductList.isEmpty()) {
                        //活动优惠商品
                        List<BzOrderProductVO> activityGoods = activityGoods(orderProductList);
                        if (isPD) {
                            BzOrderProductVO bzOrderProductVO = new BzOrderProductVO();
                            bzOrderProductVO.setGoodsName("拼单优惠");
                            bzOrderProductVO.setGoodsShowPrice(bzOrder.getPromotionDiscountAmount());
                            if (CollectionUtils.isNotEmpty(activityGoods)) {
                                activityGoods.add(bzOrderProductVO);
                            } else {
                                activityGoods = new ArrayList<>();
                                activityGoods.add(bzOrderProductVO);
                            }
                        }
                        orderProductList = generateActivity(orderProductList);
                        for (BzOrderProductVO bzOrderProducts : orderProductList) {
                            //活动和订单商品的扩展关系对象  是否参加活动
                            //获取订单小料列表
                            if (bzOrderProducts.getActivityType() != null && bzOrderProducts.getActivityType() == 11){
                                bzOrderProducts.setGoodsName("[满赠]"+bzOrderProducts.getGoodsName());
                            }
                            List<BzOrderCondimentsDO> bzOrderCondimentList = iBzOrderCondimentsService.list(
                                    new LambdaQueryWrapper<BzOrderCondimentsDO>()
                                            .eq(BzOrderCondimentsDO::getOrderSn, bzOrder.getOrderSn())
                            );
                            Map<String, List<BzOrderCondimentsDO>> condimentMap = bzOrderCondimentList.stream().collect(Collectors.groupingBy(BzOrderCondimentsDO::getCommodityGoodsid));
                            //查询订单套餐货品明细列表
//                            List<BzOrderProductSonDO> sons = iBzOrderProductSonService.list(
//                                    new LambdaQueryWrapper<BzOrderProductSonDO>()
//                                            .eq(BzOrderProductSonDO::getOrderSn, bzOrder.getOrderSn())
//                            );
                            Map<String, List<BzOrderProductSonDO>> sonsMap = sons.stream().filter(c -> c.getGoodsNum() > 0).collect(Collectors.groupingBy(e -> e.getParentGoodsId().toString()));
                            //是否是单品
                            if (bzOrderProducts.getIsSingle() == 1) {
                                //保存小料信息  过滤0
                                List<BzOrderCondimentsDO> bzOrderCondimentGoodsList = condimentMap.get(String.valueOf(bzOrderProducts.getOrderProductId()));
                                if (ObjectUtil.isNotEmpty(bzOrderCondimentGoodsList)) {
                                    List<CommodityCondimentsVO> commodityCondiments = new ArrayList<>();
                                    List<String> describeList = new ArrayList<>();
                                    bzOrderCondimentGoodsList.stream().filter(e -> e.getCondimentNumber() > 0).forEach(e -> {
                                        CommodityCondimentsVO curr = new CommodityCondimentsVO();
                                        BigDecimal sumPrice = e.getCondimentPrice();
                                        curr.setPrice(sumPrice);
                                        curr.setCommodityId(e.getCondimentId());
                                        curr.setCondimentName(e.getCondimentName());
                                        curr.setImageUrl(e.getCondimentImage());
                                        curr.setDescription(e.getCondimentDescription());
                                        bzOrderProducts.getCondimentNameList().add(e.getCondimentName() + " x " + e.getCondimentNumber() * bzOrderProducts.getGoodsNum());
                                        //规格
                                        curr.setCommoditySkuname(Optional.ofNullable(e.getCommoditySkuname()).orElse(null) + " x " + Optional.ofNullable(e.getCommodityValue()).orElse(null));
                                        //加料
                                        curr.setCommodityFeedingname(e.getCondimentDescription());
                                        curr.setNumber(e.getCondimentNumber());
                                        describeList.add("" + e.getCondimentName() + "##" + e.getCondimentNumber());
                                        commodityCondiments.add(curr);
                                    });
                                    bzOrderProducts.setDescribeList(describeList);
                                    bzOrderProducts.setCommodityCondiments(commodityCondiments);
                                }
                            } else {
                                bzOrderProducts.setGroupBzOrderProductList(sonsMap.get(String.valueOf(bzOrderProducts.getOrderProductId())));
                                List<String> describeList = new ArrayList<>();
                                if (ObjectUtil.isNotEmpty(bzOrderProducts.getGroupBzOrderProductList())) {
                                    for (BzOrderProductSonDO e : bzOrderProducts.getGroupBzOrderProductList()) {
                                        String specStr = parseSpecValues(e.getSpecValues());
                                        if ("2".equals(printerTable.getPrinterLocation())) {
                                            describeList.add("-" + e.getGoodsName()+specStr + "*" + e.getGoodsNum());
                                        } else {
                                            if (e.getGoodsShowPrice() != null && e.getGoodsShowPrice().compareTo(new BigDecimal(0)) > 0) {
                                                describeList.add("-" + e.getGoodsName()+specStr + "*" + e.getGoodsNum());
                                            } else {
                                                describeList.add("-" + e.getGoodsName() +specStr+ "*" + e.getGoodsNum());
                                            }

                                        }
                                    }
                                    bzOrderProducts.setDescribeList(describeList);
                                }
                            }
                        }
                        try {
                            //获取加购商品
//                            List<BzOrderPurchaseDO> bzOrderPurchases = iBzOrderPurchaseService.list(
//                                    new LambdaQueryWrapper<BzOrderPurchaseDO>().eq(BzOrderPurchaseDO::getOrderSn, bzOrder.getOrderSn())
//                            );
                            int finalPrinterType;
                            if (!"飞鹅".equals(printerTable.getPrinterBrand())) {
                                finalPrinterType = 2;
                            } else {
                                finalPrinterType = 1;
                            }
                            List<BzOrderProductVO> finalOrderProductList = orderProductList;
                            List<BzOrderProductVO> finalActivityGoods = activityGoods;
                            printerSettings.forEach(e -> {
                                if (shouldStop != null && shouldStop.getAsBoolean()) {
                                    return;
                                }
                                if (errandMode) {
                                    if (e.getDocumentType() != PrinterTomplateTypeEnum.STORE.getStatus()
                                            && e.getDocumentType() != PrinterTomplateTypeEnum.DELIVERY.getStatus()) {
                                        return;
                                    }
                                } else {
                                    if (bzOrder.getOrderType() == OrderTypeEnum.TAKEAWAY.getCode() && e.getDocumentType() == PrinterTomplateTypeEnum.MEMBER.getStatus()) {
                                        return;
                                    }
                                    if (bzOrder.getOrderType() != OrderTypeEnum.TAKEAWAY.getCode() && e.getDocumentType() == PrinterTomplateTypeEnum.DELIVERY.getStatus()) {
                                        return;
                                    }
                                }
                                String context = MultiReceiptTemplate.getTemplateYu(e.getDocumentType(), finalPrinterType, printConfigVO, bzOrder, finalOrderProductList, bzOrder.getPickUpNum(), bzOrderPurchases, storeDTO, finalActivityGoods, bzOrder.getOrderType());
                                int printCopies = forceSingleCopy ? 1 : e.getPrintCopies();
                                log.info("发送打印小票：订单号={}, 联单类型={}, 打印机SN={}, 打印机品牌={}, 打印份数={}, 是否模拟打印={}",
                                        bzOrder.getOrderSn(), e.getDocumentType(), printerTable.getPrinterSerialNumber(),
                                        printerTable.getPrinterBrand(), printCopies, targetPrinterSn != null);
                                if ("飞鹅".equals(printerTable.getPrinterBrand())) {
                                    //  String context = assembleContextFe(bzOrder, orderProductList, printerTable, pickUpNum);
                                    log.info("飞鹅-----打印小票返回 {}", context);
                                    String result = FeiESendUtil.print(context, printerTable.getPrinterSerialNumber(), printCopies);
                                    log.info("飞鹅-----打印小票返回 {}", result);
                                    insertOrderLog(bzOrder, "飞鹅-----" + result);
                                } else {
                                    //     String context = assembleContextXp(bzOrder, orderProductList, printerTable, pickUpNum);
                                    log.info("芯烨云---打印小票返回 {}", "芯烨云-----" + context);
                                    String result = XpYunSendUtil.printComplexReceiptVoiceSupport(context, printerTable.getPrinterSerialNumber(), bzOrder.getGoodsAmount().doubleValue(), printCopies);
                                    log.info("芯烨云---打印小票返回 {}", "芯烨云-----" + result);
                                    insertOrderLog(bzOrder, result);
                                }
                            });
                        } catch (Exception e) {
                            log.error("打印小票失败`", e);
                            insertOrderLog(bzOrder, "打印小票失败" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    //插入日志
    private void insertOrderLog(BzOrderDO bzOrder, String remark) {
        BzOrderLogDO orderLog = new BzOrderLogDO();
        orderLog.setLogUserId(bzOrder.getMemberId());
        orderLog.setLogUserName(bzOrder.getMemberName());
        orderLog.setOrderStateLog(this.getOrderLogState(bzOrder));
        orderLog.setLogTime(DateUtils.getNowDate());
        orderLog.setOrderSn(bzOrder.getOrderSn());
        orderLog.setLogContent("小票云打印记录：  " + remark);
        orderLog.setBusinessId(bzOrder.getBusinessId());
        orderLog.setCreator(bzOrder.getCreator());
        iBzOrderLogService.save(orderLog);
    }

    private Integer getOrderLogState(BzOrderDO order) {
        return order.getOrderState();
    }

    /**
     * 校验订单
     *
     * @param orderSn
     * @return
     */
    private BzOrderDO getCheckBzOrderDO(String orderSn) {
        BzOrderDO bzOrderDO = bzOrderService.getBzOrderDO(orderSn);
        if (OrderStateEnum.CANCELED.getCode() == bzOrderDO.getOrderState()) {
            throw exception(ORDER_CANNEL_REORDER_AGAIN);
        }
        return bzOrderDO;
    }

    /**
     * 获取支付记录
     *
     * @param orderSn
     * @return
     */
    private BzOrderPayDO getBzOrderPayDO(String orderSn) {
        BzOrderPayDO bzOrderPayDO = this.getOne(
                new LambdaQueryWrapper<BzOrderPayDO>()
                        .eq(BzOrderPayDO::getOrderSn, orderSn)
        );
        if (ObjectUtils.isEmpty(bzOrderPayDO)) {
            throw exception(ORDER_PAY_RECORD_NOT_EXISTS);
        }
        return bzOrderPayDO;
    }

    /**
     * 校验支付记录
     *
     * @param orderSn
     * @return
     */
    private BzOrderPayDO getCheckBzOrderPayDO(String orderSn) {
        BzOrderPayDO bzOrderPayDO = this.getBzOrderPayDO(orderSn);
        if (!OrderConstants.FAIL.toString().equals(bzOrderPayDO.getApiPayState())) {
            throw exception(ORDER_HAS_PAYED);
        }
        return bzOrderPayDO;
    }

    private static List<Long> convertStringToList(String longListAsString) {
        List<Long> collect = Arrays.stream(longListAsString.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        collect.removeIf(Objects::isNull);
        // 使用逗号拆分字符串，并将每个部分转换为 Long 类型
        return collect;
    }

    /**
     * 优惠活动商品合并
     */
    private List<BzOrderProductVO> generateActivity(List<BzOrderProductVO> bzOrderProductList) {
        // 创建一个 Map 用于存储合并后的数据
        Map<Integer, BzOrderProductVO> mergedMap = new HashMap<>();
        // 遍历列表
        bzOrderProductList
                .sort(Comparator.comparingInt(BzOrderProductVO::getGoodsNum).reversed());
        for (BzOrderProductVO product : bzOrderProductList) {
            Integer sendIntegral = product.getSendIntegral();
            // 检查 sendIntegral 是否已经存在于 Map 中
            if (mergedMap.containsKey(sendIntegral)) {
                // 如果存在，获取对应的合并后对象
                BzOrderProductVO mergedProduct = mergedMap.get(sendIntegral);
                // 相加 goodsNum
                mergedProduct.setGoodsNum(mergedProduct.getGoodsNum() + product.getGoodsNum());
                // 相加 goodsShowPrice
                mergedProduct.setGoodsShowPrice(mergedProduct.getMoneyAmount().multiply(new BigDecimal(mergedProduct.getGoodsNum())));
            } else {
                // 如果不存在，将该元素添加到 Map 中
                mergedMap.put(sendIntegral, product);
            }
        }
        // 将 Map 中的值转换为一个新的列表并返回
        return new ArrayList<>(mergedMap.values());
    }

    /**
     * 优惠活动商品
     */
    private List<BzOrderProductVO> activityGoods(List<BzOrderProductVO> bzOrderProductList) {
        List<BzOrderProductVO> activityGoods = new ArrayList<>();
        String tagFormat = "第%s件%s折";
        List<Map.Entry<String, BzOrderProductVO>> tempEntryList = bzOrderProductList.stream()
                .filter(e -> e.getActivityId() != null)
                .map(e -> {
                    BzOrderProductVO vo = BeanUtils.toBean(e, BzOrderProductVO.class);
                    String groupKey = "OTHER";
                    if (!ObjectUtils.isEmpty(e.getActivityDiscountDetail())) {
                        StoreSkuInfoDTO.ActivityBaseDetailDTO activityInfo = JSON.parseObject(
                                e.getActivityDiscountDetail(),
                                StoreSkuInfoDTO.ActivityBaseDetailDTO.class
                        );

                        // 核心判定：activityType=5 是满减/满折大类型
                        if (activityInfo.getActivityType() != null && activityInfo.getActivityType() == 5) {
                            // 区分满减（discountOffer=1）和满折（discountOffer≠1），生成专属分组key
                            if (activityInfo.getDiscountOffer() == 1) {
                                vo.setGoodsName("满减活动");
                                groupKey = "MANJIAN";
                            } else {
                                vo.setGoodsName("满折活动");
                                groupKey = "MANZHE";
                            }
                            vo.setGoodsShowPrice(e.getPromotionDiscountAmount());
                        } else   if (activityInfo.getActivityType() != null && activityInfo.getActivityType() == 11) {
                            vo.setGoodsName("满赠活动");
                            groupKey = "MANZHENG";
                        }else {
                            // 非5类活动，保留原逻辑
                            if (activityInfo.getDiscountType() != null) {
                                NjnzDiscountTypeEnum discountType = NjnzDiscountTypeEnum.of(activityInfo.getDiscountType());
                                switch (discountType) {
                                    case SECOND_HALF_PRICE, BUY_ONE_GET_ONE:
                                        vo.setGoodsName(discountType.getDescription() + "(" + e.getGoodsName() + ")");
                                        vo.setGoodsShowPrice(e.getPromotionDiscountAmount());
                                        break;
                                    case CUSTOM:
                                        String tag = tagFormat.formatted(activityInfo.getDiscountItemNum(), activityInfo.getDiscountRate());
                                        vo.setGoodsName(tag + "(" + e.getGoodsName() + ")");
                                        break;
                                    case MJ:
                                        vo.setGoodsName(activityInfo.getActivityName());
                                        break;
                                }
                            }
                            groupKey = "ACT_" + activityInfo.getActivityType();
                        }
                    } else {
                        vo.setActivityInfo(new ActivityNjnzInfoDTO());
                    }

                    return new AbstractMap.SimpleEntry<>(groupKey, vo);
                })
                .collect(Collectors.toList());

        Map<String, List<Map.Entry<String, BzOrderProductVO>>> groupMap = tempEntryList.stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey));

        for (Map.Entry<String, List<Map.Entry<String, BzOrderProductVO>>> entry : groupMap.entrySet()) {
            String key = entry.getKey();
            List<Map.Entry<String, BzOrderProductVO>> entryList = entry.getValue();

            if ("MANJIAN".equals(key) || "MANZHE".equals(key)||"MANZHENG".equals(key)) {
                BzOrderProductVO mergedVO = entryList.get(0).getValue(); // 取第一条VO作为基础
                // 累加该分组下所有VO的优惠金额
                BigDecimal totalDiscount = entryList.stream()
                        .map(en -> en.getValue().getGoodsShowPrice())
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                mergedVO.setGoodsShowPrice(totalDiscount); // 设置合并后的总优惠金额
                activityGoods.add(mergedVO);
            } else {
                activityGoods.addAll(entryList.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
            }
        }
        return activityGoods;
    }
    private String parseSpecValues(String specValues) {
        if (specValues == null || specValues.trim().isEmpty()) {
            return "";
        }
        specValues = specValues.trim();

        List<String> specList = new ArrayList<>();

        String[] parts = specValues.split("@");
        if (parts.length > 0 && !parts[0].trim().isEmpty()) {
            specList.add(parts[0].trim());
        }

        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            String keyValueStr = parts[1].trim();
            String[] keyValuePairs = keyValueStr.split(",");
            for (String pair : keyValuePairs) {
                if (pair.trim().isEmpty()) {
                    continue;
                }
                String[] kv = pair.split(":");
                if (kv.length > 1 && !kv[1].trim().isEmpty()) {
                    specList.add(kv[1].trim());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String spec : specList) {
            sb.append("[").append(spec).append("]");
        }
        return sb.toString();
    }
    private static final class SimulationPrintTask {
        private final AtomicBoolean stopped = new AtomicBoolean(false);
        private volatile Future<?> future;

        private boolean stop() {
            if (!stopped.compareAndSet(false, true)) {
                return false;
            }
            Future<?> currentFuture = future;
            if (currentFuture != null) {
                currentFuture.cancel(true);
            }
            return true;
        }

        private boolean isStopped() {
            return stopped.get();
        }

        private void setFuture(Future<?> future) {
            this.future = future;
            if (isStopped()) {
                future.cancel(true);
            }
        }
    }
}
