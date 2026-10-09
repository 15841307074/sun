package com.htyoudao.youdao.module.order.core.submit.strategy;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.util.TextFilterUtil;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.enums.SaleRuleEnum;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitReqVO;
import com.htyoudao.youdao.module.order.controller.app.order.VO.SubmitResVO;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.submit.DTO.SubmitCacheDTO;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.collection.NotNullHashSet;
import com.htyoudao.youdao.module.order.dal.dataobject.order.*;
import com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.order.enums.*;
import com.htyoudao.youdao.module.order.service.order.*;
import com.htyoudao.youdao.module.order.service.pay.BzOrderPayService;
import com.htyoudao.youdao.module.order.service.pay.CallbackOrderDetailCache;
import com.htyoudao.youdao.module.order.util.DateUtils;
import com.htyoudao.youdao.module.order.util.SerialNumberGenerator;
import com.htyoudao.youdao.module.promotion.api.enums.ChannelEnum;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UsedCouponReqVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 约束了提交订单的固定流程
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
public abstract class AbstractIOrderSubmitStrategy<T extends SubmitReqVO> implements IOrderSubmitStrategy<T> {

    private static final int MZ_LOCK_MAX_ATTEMPTS = 2;

    @DubboReference
    protected UserCouponApi userCouponApi;
    @DubboReference
    protected ActivityApi activityApi;
    @Resource
    protected BzOrderService bzOrderService;
    @Resource
    protected BzOrderProductService bzOrderProductService;
    @Resource
    protected BzOrderProductSonService bzOrderProductSonService;
    @Resource
    protected BzOrderPurchaseService bzOrderPurchaseService;
    @Resource
    protected BzOrderCondimentsService bzOrderCondimentsService;
    @Resource
    protected BzOrderLogService bzOrderLogService;
    @Resource
    protected BzOrderPayService bzOrderPayService;
    @Resource
    protected CallbackOrderDetailCache callbackOrderDetailCache;
    @Resource
    protected StringRedisTemplate stringRedisTemplate;
    @Resource
    protected RabbitMQService rabbitMQService;
    @Resource
    protected ThreadPoolExecutor strongExecutor;
    @Resource
    protected ThreadPoolExecutor weakExecutor;

    /**
     * 模版方法定义标准流程
     *
     * @param reqVO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitResVO submit(T reqVO) throws Exception {
        //1.基础校验
        this.validateCommon(reqVO);

        //2.策略特有校验 前置
        this.validateSpecificBefore(reqVO);

        //3.获取缓存数据
        CalculateCacheDataV2DTO cacheData = this.getCacheData(reqVO);

        //4.策略特有校验 后置
        this.validateSpecificAfter(reqVO, cacheData);

        //5.保存订单主体
        BzOrderDO bzOrderDO = this.buildBzOrderDO(reqVO, cacheData);

        boolean mzGiftLocked = this.lockMzGifts(reqVO, cacheData, bzOrderDO);
        if (mzGiftLocked) {
            this.registerMzLockRollback(bzOrderDO.getOrderSn());
        }
        bzOrderService.save(bzOrderDO);

        //6.保存订单附表信息
        this.saveOtherTables(reqVO, bzOrderDO, cacheData);

        // 现金订单提交即视为支付成功，但赠品库存必须等订单本地事务提交成功后再确认。
        if (mzGiftLocked && Integer.parseInt(bzOrderDO.getPaymentCode()) == PaymentMethodEnum.CASH.getCode()) {
            registerMzCashConfirmAfterCommit(bzOrderDO.getOrderSn());
        }

        //7.时间轮
        this.addTask(bzOrderDO, cacheData);

        //8.后续处理
        this.postProcess(reqVO, cacheData, bzOrderDO);

        //9.响应
        return SubmitResVO.builder()
                .orderSn(bzOrderDO.getOrderSn())
                .pickUpNum(bzOrderDO.getPickUpNum())
                .createTime(DateUtils.localDateTimeToString(bzOrderDO.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS))
                .build();
    }

    private void registerMzLockRollback(String orderSn) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_ROLLED_BACK) return;
                // Redis/MySQL 满赠锁库不属于订单库本地事务，订单事务失败后必须显式补偿释放。
                try {
                    activityApi.releaseMzGiftInventory(orderSn);
                } catch (Exception ex) {
                    log.error("订单事务回滚后释放满赠库存失败 orderSn={}", orderSn, ex);
                }
            }
        });
    }

    private void registerMzCashConfirmAfterCommit(String orderSn) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            activityApi.confirmMzGiftInventory(orderSn);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    activityApi.confirmMzGiftInventory(orderSn);
                } catch (Exception ex) {
                    // 订单已经提交，不能因远程确认失败回滚；过期锁定补偿任务会再次确认。
                    log.error("现金订单提交后确认满赠库存失败，等待补偿任务重试 orderSn={}", orderSn, ex);
                }
            }
        });
    }

    /**
     * 提交订单时锁定满赠赠品库存，并按实际锁定结果修正结算生成的赠品行和订单金额。
     *
     * <p>处理口径：</p>
     * <ol>
     *     <li>点餐机订单只计算、保存赠品，不读取或维护活动库存，因此直接跳过；</li>
     *     <li>只处理 {@code isGift=1} 的满赠赠品行，正品和加购品不进入锁库流程；</li>
     *     <li>结算库存仅用于展示，提交时重新调用 Promotion 原子锁库，以提交时的实际库存为准；</li>
     *     <li>实际锁定为0时移除整条赠品，部分锁定时缩减赠品数量及对应展示金额；</li>
     *     <li>赠品减少只同步减少商品原价和等额营销优惠，用户实付 {@code payAmount} 保持不变；</li>
     *     <li>至少成功锁定一个赠品时返回true，调用方才注册回滚释放及现金订单确认回调。</li>
     * </ol>
     *
     * <p>库存不足或参与次数达到上限只影响赠品，不阻断正品下单；远程调用结果未知则由
     * {@link #lockMzGiftWithRetry(CalculateCacheDataV2DTO, BzOrderDO,
     * CalculateCacheDataV2DTO.CommodityInfoVO, int, boolean)} 重试，仍无法确认时终止订单提交，
     * 避免订单赠品与Promotion库存流水不一致。</p>
     *
     * @param reqVO 提交请求；当前方法不直接读取，仅保留在统一提交锁库方法签名中
     * @param cacheData 结算缓存，包含结算生成的正品、加购品及满赠赠品行
     * @param order 已构建但尚未保存的订单主体，提供订单号、来源等锁库上下文
     * @return true-至少一个赠品实际锁定成功；false-点餐机订单或没有任何赠品锁定成功
     */
    private boolean lockMzGifts(T reqVO, CalculateCacheDataV2DTO cacheData, BzOrderDO order) {
        // 1. 点餐机满赠只计算和落订单赠品行：不查询库存、不锁库、不产生库存/参与流水。
        if (Objects.equals(order.getOrderFrom(), OrderFromEnum.POINT_SINGLE_MACHINE.getCode())) {
            return false;
        }

        // 使用迭代器遍历，因为锁库为0时需要安全地从结算商品列表中移除当前赠品行。
        Iterator<CalculateCacheDataV2DTO.CommodityInfoVO> iterator = cacheData.getCommodityInfos().iterator();
        // 记录因无库存或部分库存而移除的赠品原价；赠品原价与等额营销优惠需要同时回调。
        BigDecimal removedAmount = BigDecimal.ZERO;
        // 控制后续事务回滚释放和现金订单提交后确认，避免无满赠订单产生空RPC查询。
        boolean giftLocked = false;

        // 2. 非点餐机订单需要校验用户参与上限；同一订单、同一活动只占用一次，与赠品件数无关。
        boolean checkUserLimit = true;
        while (iterator.hasNext()) {
            CalculateCacheDataV2DTO.CommodityInfoVO item = iterator.next();
            // 正品、加购品等普通订单行不参与赠品库存锁定。
            if (!OrderConstants.YES.equals(item.getIsGift())) continue;

            // 3. requested是结算阶段的理论赠送数量；null按0处理，最终数量以Promotion返回值为准。
            int requested = Optional.ofNullable(item.getCopies()).orElse(0);
            int actual = lockMzGiftWithRetry(cacheData, order, item, requested, checkUserLimit);
            if (actual <= 0) {
                // 4. 没有成功锁到库存：累计整条赠品原价并从订单商品列表移除，不影响正品继续下单。
                removedAmount = removedAmount.add(item.getSkuPrice().multiply(BigDecimal.valueOf(requested)));
                iterator.remove();
            } else {
                // 至少存在一条真实库存流水，后续必须注册订单回滚释放；现金单还需提交后确认。
                giftLocked = true;
                if (actual < requested) {
                    // 5. 库存只能满足部分赠送：保留实际数量，累计未锁到部分的赠品原价。
                    int removed = requested - actual;
                    removedAmount = removedAmount.add(item.getSkuPrice().multiply(BigDecimal.valueOf(removed)));
                    item.setCopies(actual);
                    // 赠品展示金额、等额营销优惠和划线价都必须按照实际赠送数量重新计算。
                    item.setGoodsShowPrice(item.getSkuPrice().multiply(BigDecimal.valueOf(actual)));
                    item.setPromotionDiscountAmount(item.getGoodsShowPrice());
                    item.setStrikeThroughPrice(item.getSkuPrice().multiply(BigDecimal.valueOf(actual)));
                }
            }
        }

        // 6. 只有赠品被删除或缩减时才重算订单汇总，正常全量锁定不产生额外金额操作。
        if (removedAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 赠品原价和等额优惠同步减少，所以用户实付payAmount不变。
            cacheData.setCommodityAmount(cacheData.getCommodityAmount().subtract(removedAmount));
            cacheData.setPromotionDiscountAmount(cacheData.getPromotionDiscountAmount().subtract(removedAmount));
            // buildBzOrderDO已在锁库前执行，需要把修正后的结算汇总同步回尚未落库的订单主体。
            order.setGoodsAmount(cacheData.getCommodityAmount());
            order.setPromotionDiscountAmount(cacheData.getPromotionDiscountAmount());
            order.setAllDiscountAmount(cacheData.getActivityDiscountAmount().add(cacheData.getPromotionDiscountAmount()));
            // orderAmount采用“实付 + 全部优惠”口径；实付不变，随优惠汇总同步修正。
            order.setOrderAmount(cacheData.getPayAmount().add(order.getAllDiscountAmount()));
        }
        return giftLocked;
    }

    /**
     * 调用Promotion锁定单个赠品库存，并处理远程调用结果未知的场景。
     *
     * <p>{@code orderSn + activityId + giftCommodityId} 在库存流水表中唯一，因此相同参数重试时：</p>
     * <ul>
     *     <li>首次请求未到达Promotion：重试执行正常锁库；</li>
     *     <li>首次请求已成功但响应丢失：重试读取并返回首次实际锁定数量，不会重复扣减；</li>
     *     <li>连续重试仍失败：尽力按订单号释放可能已产生的全部锁定，并终止本次订单提交；</li>
     *     <li>即时释放也失败：保留LOCKED流水，由过期库存补偿任务再次对账处理。</li>
     * </ul>
     *
     * @param cacheData 结算缓存，提供门店ID和会员ID
     * @param order 订单主体，提供全局唯一订单号
     * @param item 当前满赠赠品行，提供活动、赠品商品及门店SKU信息
     * @param requested 结算阶段计算出的理论赠送数量
     * @param checkUserLimit 是否校验并占用用户参与次数
     * @return Promotion实际成功锁定的赠品数量；库存不足时可能小于requested
     * @throws IllegalStateException 连续重试后仍无法确认锁库结果
     */
    private int lockMzGiftWithRetry(CalculateCacheDataV2DTO cacheData, BzOrderDO order,
                                    CalculateCacheDataV2DTO.CommodityInfoVO item,
                                    int requested, boolean checkUserLimit) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MZ_LOCK_MAX_ATTEMPTS; attempt++) {
            try {
                // Promotion根据唯一库存流水保证幂等，并在有限库存不足时返回实际可锁定数量。
                return Optional.ofNullable(activityApi.lockMzGiftInventory(item.getActivityId(), cacheData.getStoreId(),
                        cacheData.getMemberId(), order.getOrderSn(), item.getCommodityId(), item.getSkuId(), requested,
                        checkUserLimit).getCheckedData()).orElse(0);
            } catch (Exception ex) {
                // RPC异常不能等同于锁库失败：Provider可能已经提交，只是响应在返回途中丢失。
                lastException = ex;
                log.warn("满赠锁库存结果未知，准备重试 orderSn={}, activityId={}, giftCommodityId={}, attempt={}",
                        order.getOrderSn(), item.getActivityId(), item.getCommodityId(), attempt, ex);
            }
        }

        // 可能已有部分赠品或本次超时请求在 Promotion 成功落库，先尽力释放整单；失败则由过期任务兜底。
        try {
            activityApi.releaseMzGiftInventory(order.getOrderSn());
        } catch (Exception releaseException) {
            log.error("满赠锁库存结果未知且即时释放失败，等待过期任务补偿 orderSn={}", order.getOrderSn(), releaseException);
        }
        throw new IllegalStateException("满赠库存状态确认失败，请重新提交订单", lastException);
    }

    /**
     * 公共校验，所有类型订单都必须通过的
     *
     * @param reqVO
     */
    private void validateCommon(T reqVO) {
        //备注过滤掉emoji
        if (ObjectUtils.isNotEmpty(reqVO.getRemark())) {
            if (reqVO.getRemark().length() > 160) {
                throw exception(ORDER_REMARK_LENGTH_ERROR);
            }
            reqVO.setRemark(TextFilterUtil.keepNormalChars(reqVO.getRemark()));
        }
        //收货地址过滤emoji
        if (ObjectUtils.isNotEmpty(reqVO.getReceiverInfo()) && ObjectUtils.isNotEmpty(reqVO.getReceiverInfo().getReceiverAddress())) {
            reqVO.getReceiverInfo().setReceiverAddress(TextFilterUtil.keepNormalChars(reqVO.getReceiverInfo().getReceiverAddress()));
        }
        //收货人名过滤emoji
        if (ObjectUtils.isNotEmpty(reqVO.getReceiverInfo()) && ObjectUtils.isNotEmpty(reqVO.getReceiverInfo().getReceiverName())) {
            reqVO.getReceiverInfo().setReceiverName(TextFilterUtil.keepNormalChars(reqVO.getReceiverInfo().getReceiverName()));
        }
    }

    /**
     * 策略特有前置校验
     *
     * @param reqVO
     */
    protected abstract void validateSpecificBefore(T reqVO);

    /**
     * 策略特有后置校验
     *
     * @param cacheData
     */
    protected void validateSpecificAfter(T reqVO, CalculateCacheDataV2DTO cacheData) {
        //是否合规信息
        if (cacheData.getMemberId() > 0L && !cacheData.getMemberId().equals(WebFrameworkUtils.getLoginUserId())) {
            throw exception(ORDER_CALCULATION_INFO_ERROR);
        }

        //门店未营业
        if (OrderConstants.YES.equals(cacheData.getOpenStatus())) {
            throw exception(ORDER_STORE_NOT_WORK);
        }
    }

    protected void validateExchangeCommodityCoupon(CalculateCacheDataV2DTO cacheData) {
        if (cacheData == null || CollectionUtils.isEmpty(cacheData.getCommodityInfos())) {
            return;
        }

        long exchangeCommodityCount = cacheData.getCommodityInfos().stream()
                .filter(commodityInfo -> !OrderConstants.YES.equals(commodityInfo.getIsPurchase()))
                .filter(commodityInfo -> Objects.equals(commodityInfo.getSaleRule(), SaleRuleEnum.EXCHANGE_SALE_OK.getCode()))
                .map(this::getCommodityUniqueId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        boolean usedCoupon = ObjectUtils.isNotEmpty(cacheData.getUserCouponId());

        if (exchangeCommodityCount > 0 && (!usedCoupon || exchangeCommodityCount > 1)) {
            throw exception(ORDER_EXCHANGE_COMMODITY_ONLY_COUPON);
        }
    }

    private Long getCommodityUniqueId(CalculateCacheDataV2DTO.CommodityInfoVO commodityInfo) {
        if (commodityInfo.getCommodityId() != null) {
            return commodityInfo.getCommodityId();
        }
        if (commodityInfo.getSpuId() != null) {
            return commodityInfo.getSpuId();
        }
        return commodityInfo.getSkuId();
    }

    /**
     * 生成取餐码
     *
     * @param storeId
     */
    protected String getPickUpCode(Long storeId) {
        return bzOrderService.getPickupCode(storeId);
    }

    /**
     * 获取缓存数据
     *
     * @param reqVO
     */
    protected CalculateCacheDataV2DTO getCacheData(T reqVO) throws Exception {
        Long memberId = WebFrameworkUtils.getLoginUserId();
        reqVO.setCacheKey(RedisKeyConstants.ORDER_CALC_CACHE + memberId);

        String calcInfoJSON = stringRedisTemplate.opsForValue().get(reqVO.getCacheKey());
        if (ObjectUtils.isEmpty(calcInfoJSON)) {
            throw exception(ORDER_SETTLEMENT_INFO_EXPIRE);
        }
        return JSON.parseObject(calcInfoJSON, CalculateCacheDataV2DTO.class);
    }

    /**
     * 获取订单实体
     *
     * @param reqVO
     * @param cacheData
     */
    protected BzOrderDO buildBzOrderDO(T reqVO, CalculateCacheDataV2DTO cacheData) {
        String orderSn = SerialNumberGenerator.generateSerialNumber("ORD", reqVO.getCreateTime());
        String paySn = SerialNumberGenerator.generateSerialNumber("PAY", reqVO.getCreateTime());

        BzOrderDO bzOrderDO = new BzOrderDO();
        BeanUtils.copyProperties(reqVO, bzOrderDO);
        bzOrderDO.setOrderSn(orderSn);
        bzOrderDO.setPaySn(paySn);
        bzOrderDO.setPaymentName(PaymentMethodEnum.getMsgByCode(reqVO.getPaymentCode()));
        bzOrderDO.setPaymentCode(reqVO.getPaymentCode().toString());
        bzOrderDO.setOrderState(OrderStateEnum.UNPAID.getCode());
        bzOrderDO.setExpressId(OrderConstants.IS_OLD.longValue());
        bzOrderDO.setOrderRemark(reqVO.getRemark());
        bzOrderDO.setCreateTime(reqVO.getCreateTime());
        bzOrderDO.setStar(Integer.valueOf(reqVO.getCreateTime().getHour()).longValue());
        bzOrderDO.setRefuseReason(reqVO.getLongitude());
        bzOrderDO.setRefuseRemark(reqVO.getLatitude());
        cacheData.setOrderSn(orderSn);
        cacheData.setPaySn(paySn);
        cacheData.setCreateTime(reqVO.getCreateTime());

        //以下取值缓存
        BeanUtils.copyProperties(cacheData, bzOrderDO);
        bzOrderDO.setExpressName(cacheData.getStoreLongitude().toString());
        bzOrderDO.setStoreRemark(cacheData.getStoreLatitude().toString());
        bzOrderDO.setGoodsAmount(cacheData.getCommodityAmount());
        bzOrderDO.setActivityDiscountAmount(cacheData.getActivityDiscountAmount());
        bzOrderDO.setPromotionDiscountAmount(cacheData.getPromotionDiscountAmount());
        bzOrderDO.setAllDiscountAmount(cacheData.getActivityDiscountAmount().add(cacheData.getPromotionDiscountAmount()));
        bzOrderDO.setOrderAmount(
                cacheData.getPayAmount()
                        .add(cacheData.getActivityDiscountAmount())
                        .add(cacheData.getPromotionDiscountAmount())
        );
        bzOrderDO.setPayAmount(cacheData.getPayAmount());
        bzOrderDO.setVoucherCode(cacheData.getCouponCode());
        bzOrderDO.setTakeAwayAddress(cacheData.getCouponName());
        bzOrderDO.setVoucherPrice(cacheData.getActivityDiscountAmount());
        bzOrderDO.setPackingCharge(cacheData.getPackingFee());
        bzOrderDO.setExpressFee(cacheData.getDeliveryFee());
        bzOrderDO.setMinimumDeliveryFee(cacheData.getMinimumDeliveryFee());
        bzOrderDO.setMemberName(OrderFromEnum.getMemberNameByCode(bzOrderDO.getOrderFrom()));
        bzOrderDO.setTableWareNum(this.sumGoodsNum(cacheData));
        //过滤表情
        bzOrderDO.setOrderRemark(StringUtils.filterEmoji(bzOrderDO.getOrderRemark()));
        //放到最后，覆盖缓存里的订单类型
        bzOrderDO.setOrderType(reqVO.getOrderType());

        bzOrderDO.setEvaluateState(reqVO.getChannel());
        //渠道处理
        if (ObjectUtils.isEmpty(reqVO.getChannel())) {
            //点餐机走点餐机渠道
            if (bzOrderDO.getOrderFrom() == OrderFromEnum.POINT_SINGLE_MACHINE.getCode()) {
                bzOrderDO.setEvaluateState(ChannelEnum.ORDERING_MACHINE.getId().intValue());
            }
            //其他走小程序渠道
            else {
                bzOrderDO.setEvaluateState(ChannelEnum.USER_MINI_PROGRAM.getId().intValue());
            }
        }

        return bzOrderDO;
    }

    private Integer sumGoodsNum(CalculateCacheDataV2DTO cacheData) {
        if (cacheData == null || cacheData.getCommodityInfos() == null) {
            return 0;
        }
        return cacheData.getCommodityInfos().stream()
                .map(CalculateCacheDataV2DTO.CommodityInfoVO::getCopies)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * 保存表信息
     *
     * @param reqVO
     * @param bzOrderDO
     * @param cacheData
     * @return 返回订单号
     */
    private void saveOtherTables(T reqVO, BzOrderDO bzOrderDO, CalculateCacheDataV2DTO cacheData) {
        if (cacheData == null || cacheData.getCommodityInfos() == null) {
            throw exception(ORDER_CACHE_NOT_EXISTS);
        }

        List<CalculateCacheDataV2DTO.CommodityInfoVO> commodityInfos = cacheData.getCommodityInfos();
        boolean hasOrderProduct = commodityInfos.stream()
                .anyMatch(item -> !Objects.equals(item.getIsPurchase(), OrderConstants.YES));
        if (!hasOrderProduct) {
            throw exception(ORDER_COMMODITY_PRODUCT_EMPTY);
        }

        NotNullHashSet<Long> activityIdSet = new NotNullHashSet<>();
        OrderDetailDTO callbackDetail = new OrderDetailDTO();
        callbackDetail.setProductDOList(new ArrayList<>());
        callbackDetail.setProductSonDOList(new ArrayList<>());
        callbackDetail.setPurchaseDOList(new ArrayList<>());
        commodityInfos.forEach(item -> {
            try {
                if (Objects.equals(item.getIsPurchase(), OrderConstants.YES)) {
                    //处理加购单品
                    BzOrderPurchaseDO bzOrderPurchaseDO = this.buildBzOrderPurchaseDO(bzOrderDO, item);
                    bzOrderPurchaseService.save(bzOrderPurchaseDO);
                    callbackDetail.getPurchaseDOList().add(bzOrderPurchaseDO);
                } else {
                    //处理单品
                    BzOrderProductDO bzOrderProductDO = this.buildBzOrderProductDO(bzOrderDO, item);
                    activityIdSet.add(bzOrderProductDO.getActivityId());
                    bzOrderProductService.save(bzOrderProductDO);
                    callbackDetail.getProductDOList().add(bzOrderProductDO);

                    //处理套餐
                    List<BzOrderProductSonDO> bzOrderProductSonDOS = this.buildBzOrderProductSonDOList(bzOrderDO, bzOrderProductDO, item);
                    bzOrderProductSonService.saveBatch(bzOrderProductSonDOS);
                    callbackDetail.getProductSonDOList().addAll(bzOrderProductSonDOS);

                    //处理单品小料
                    this.handleCondiments(bzOrderDO, bzOrderProductDO, item);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("处理商品信息失败，orderSn: {}, 商品ID: {}", bzOrderDO.getOrderSn(), item.getCommodityId(), e);
                throw exception(ORDER_COMMODITY_ERROR);
            }
        });

        //订单记录
        this.handleBzOrderLog(bzOrderDO);

        //初始化支付记录表
        this.handleBzOrderPay(bzOrderDO);
        // 付款单、秒杀单、外卖单、拼单和代取单需要缓存支付回调明细。
        OrderSourceEnum source = getSource();
        if (source == OrderSourceEnum.K_PAYMENT_ORDER
                || source == OrderSourceEnum.X_PAYMENT_ORDER
                || source == OrderSourceEnum.SECKILL_ORDER
                || source == OrderSourceEnum.TAKE_OUT_ORDER
                || source == OrderSourceEnum.SPLICING_ORDER
                || source == OrderSourceEnum.ERRAND_ORDER) {
            callbackOrderDetailCache.putAfterCommit(bzOrderDO.getBusinessId(), bzOrderDO.getOrderSn(), callbackDetail);
        }

        //活动ID缓存
//        activityIdSet.forEach(activityId -> stringRedisTemplate.opsForValue().set(RedisKeyConstants.ACTIVITY_EXIST_KEY + activityId, "OK"));
    }

    /**
     * 后置处理
     */
    protected void postProcess(SubmitReqVO reqVO, CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        SubmitCacheDTO submitCacheDTO = new SubmitCacheDTO();
        submitCacheDTO.setOrderSn(cacheDataDTO.getOrderSn());
        submitCacheDTO.setPaySn(cacheDataDTO.getPaySn());
        submitCacheDTO.setBusinessId(BusinessContextHolder.getBusinessId());
        submitCacheDTO.setPayAmount(cacheDataDTO.getPayAmount());
        submitCacheDTO.setCreateTime(cacheDataDTO.getCreateTime());
        submitCacheDTO.setStoreId(cacheDataDTO.getStoreId());
        submitCacheDTO.setPaymentCode(reqVO.getPaymentCode().toString());
        submitCacheDTO.setOrderType(cacheDataDTO.getOrderType());

        String submitKey = RedisKeyConstants.ORDER_SUBMIT_CACHE + cacheDataDTO.getOrderSn();
        stringRedisTemplate.opsForValue().set(submitKey, JSON.toJSONString(submitCacheDTO), 5, TimeUnit.MINUTES);
    }

    /**
     * 添加时间轮
     */
    protected void addTask(BzOrderDO bzOrderDO, CalculateCacheDataV2DTO cacheData) {
        bzOrderService.addOrderTimerTask(bzOrderDO, this.getValueByNow(cacheData.getPeakHours()));
        bzOrderService.cancelCouponTimeTask(bzOrderDO, 15);
    }

    public Integer getValueByNow(Map<String, Integer> map) {
        LocalTime now = LocalTime.now();
        return getValueByTime(map, now);
    }

    public Integer getValueByTime(Map<String, Integer> map, LocalTime time) {
        log.info("==> 门店匹配的时段 | map {}", map);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();

            String[] parts = key.split("-");
            if (parts.length != 2) {
                continue; // 忽略不合法 key
            }

            LocalTime start = LocalTime.parse(parts[0]);
            LocalTime end = LocalTime.parse(parts[1]);

            if (!time.isBefore(start) && !time.isAfter(end)) {
                return entry.getValue();
            }
        }

        // 理论上永远走不到，因为 map 中有 "00:00-23:59"
        log.info("==> 未匹配到时段，走默认");
        return 5;
    }

    /**
     * 构建 BzOrderPurchaseDO 对象
     *
     * @param bzOrderDO
     * @param item
     * @return
     */
    public BzOrderPurchaseDO buildBzOrderPurchaseDO(BzOrderDO bzOrderDO, CalculateCacheDataV2DTO.CommodityInfoVO item) {
        BzOrderPurchaseDO bzOrderPurchaseDO = new BzOrderPurchaseDO();
        bzOrderPurchaseDO.setOrderSn(bzOrderDO.getOrderSn());
        bzOrderPurchaseDO.setPurchasePrice(item.getSkuPrice());
        bzOrderPurchaseDO.setStrikePrice(item.getStrikeThroughPrice());
        bzOrderPurchaseDO.setPurchaseId(item.getCommodityId().toString());
        bzOrderPurchaseDO.setPurchaseName(item.getSpuName());
        bzOrderPurchaseDO.setPurchaseImage(item.getImageUrl());

        bzOrderPurchaseDO.setGoodsId(item.getSpuId());
        bzOrderPurchaseDO.setStoreSkuId(item.getSkuId());
        bzOrderPurchaseDO.setOriginalSkuId(item.getOriginalSkuId());
        bzOrderPurchaseDO.setCommodityId(item.getCommodityId());

        bzOrderPurchaseDO.setCreateTime(bzOrderDO.getCreateTime());
        return bzOrderPurchaseDO;
    }

    /**
     * 构建 BzOrderProductDO 对象
     *
     * @param bzOrderDO
     * @param item
     * @return
     */
    public BzOrderProductDO buildBzOrderProductDO(BzOrderDO bzOrderDO, CalculateCacheDataV2DTO.CommodityInfoVO item) {
        BzOrderProductDO bzOrderProductDO = new BzOrderProductDO();
        bzOrderProductDO.setOrderSn(bzOrderDO.getOrderSn());
        bzOrderProductDO.setStoreId(bzOrderDO.getStoreId());
        bzOrderProductDO.setStoreName(bzOrderDO.getStoreName());
        bzOrderProductDO.setCategoryId(item.getCategoryId());
        bzOrderProductDO.setCategoryName(item.getCategoryName());
        bzOrderProductDO.setMemberId(bzOrderDO.getMemberId());
        bzOrderProductDO.setIsGift(item.getIsGift());

        bzOrderProductDO.setCommodityId(item.getCommodityId());
        bzOrderProductDO.setGoodsId(item.getSpuId());
        bzOrderProductDO.setStoreSkuId(item.getSkuId());
        bzOrderProductDO.setOriginalSkuId(item.getOriginalSkuId());

        bzOrderProductDO.setGoodsName(item.getSpuName());
        bzOrderProductDO.setGoodsImage(item.getImageUrl().contains(",") ? item.getImageUrl().split(",")[0] : item.getImageUrl());
        bzOrderProductDO.setGoodsNum(item.getCopies());
        bzOrderProductDO.setIsSingle(
                item.getSetmealType().equals(SetmealTypeEnum.SINGLE.getCode()) ? SetmealTypeEnum.FIXED.getCode() : SetmealTypeEnum.GROUP.getCode()
        );
        bzOrderProductDO.setSpecValues(item.getSkuName());
        bzOrderProductDO.setSendIntegral(item.getTag());

        //优惠卷
        bzOrderProductDO.setCouponId(item.getCouponId());
        bzOrderProductDO.setUserCouponId(item.getUserCouponId());
        bzOrderProductDO.setCouponName(item.getCouponName());


        //活动相关
        if (item.getIsGetActivity().equals(OrderConstants.YES)) {
            bzOrderProductDO.setActivityId(item.getActivityId());
            bzOrderProductDO.setActivityName(item.getActivityName());
            bzOrderProductDO.setActivityType(item.getActivityType());
        }
        bzOrderProductDO.setActivityDiscountDetail(item.getActivityDiscountDetail());

        //金额相关
        bzOrderProductDO.setActivityDiscountAmount(item.getActivityDiscountAmount());
        bzOrderProductDO.setPromotionDiscountAmount(item.getPromotionDiscountAmount());
        bzOrderProductDO.setGoodsShowPrice(item.getGoodsShowPrice());
        bzOrderProductDO.setSkuStrikePrice(item.getStrikeThroughPrice());
        bzOrderProductDO.setMoneyAmount(item.getSkuPrice());
        bzOrderProductDO.setCreateTime(bzOrderDO.getCreateTime());

        // 商品属性信息
        this.setFlavorAttributes(bzOrderProductDO, item.getFlavorList());
        return bzOrderProductDO;
    }

    /**
     * 设置商品属性信息
     *
     * @param bzOrderProductDO
     * @param flavorList
     */
    public void setFlavorAttributes(BzOrderProductDO bzOrderProductDO, List<CalculateCacheDataV2DTO.FlavorInfoVO> flavorList) {
        if (CollectionUtils.isNotEmpty(flavorList)) {
            StringBuilder flavorName = new StringBuilder();
            StringBuilder flavorValue = new StringBuilder();
            for (CalculateCacheDataV2DTO.FlavorInfoVO flavor : flavorList) {
                if (flavor != null && flavor.getName() != null && flavor.getValue() != null) {
                    flavorName.append(flavor.getName()).append(",");
                    flavorValue.append(flavor.getValue()).append(",");
                }
            }
            if (!flavorName.isEmpty()) {
                bzOrderProductDO.setFlavorName(flavorName.substring(0, flavorName.length() - 1));
            }
            if (!flavorValue.isEmpty()) {
                bzOrderProductDO.setFlavorValue(flavorValue.substring(0, flavorValue.length() - 1));
            }
        }
    }

    /**
     * 处理套餐
     *
     * @param bzOrderDO
     * @param bzOrderProductDO
     * @param item
     */
    public List<BzOrderProductSonDO> buildBzOrderProductSonDOList(BzOrderDO bzOrderDO, BzOrderProductDO bzOrderProductDO, CalculateCacheDataV2DTO.CommodityInfoVO item) {
        List<BzOrderProductSonDO> sonBatchInsertList = new ArrayList<>();

        if (!item.getSetmealType().equals(SetmealTypeEnum.SINGLE.getCode())) {
            List<CalculateCacheDataV2DTO.SingleInfoVO> singleList = item.getSingleList();
            if (CollectionUtils.isEmpty(singleList)) {
                throw new ServiceException(1, "套餐信息不完整");
            }

            singleList.forEach(single -> {
                BzOrderProductSonDO bzOrderProductSonDO = new BzOrderProductSonDO();
                bzOrderProductSonDO.setOrderSn(bzOrderDO.getOrderSn());
                bzOrderProductSonDO.setGoodsShowPrice(single.getSinglePrice().multiply(new BigDecimal(single.getNumber())));

                bzOrderProductSonDO.setGoodsId(single.getSpuId());
                bzOrderProductSonDO.setCommodityId(single.getCommodityId());
                bzOrderProductSonDO.setOriginalSkuId(single.getOriginalSkuId());

                // 规格名@甜度:微甜，辣度:微辣
                String specValues = Optional.ofNullable(single.getSkuName()).orElse("");

                List<StoreSingleInfoDTO.FlavorInfoVO> flavors =
                        Optional.ofNullable(single.getFlavors()).orElse(Collections.emptyList());
                if (!flavors.isEmpty()) {
                    String flavorStr = flavors.stream()
                            .filter(Objects::nonNull)
                            .filter(f -> StringUtils.hasText(f.getName()) && StringUtils.hasText(f.getValue()))
                            .map(f -> f.getName() + ":" + f.getValue())
                            .collect(Collectors.joining(","));

                    if (StringUtils.hasText(flavorStr)) {
                        specValues = specValues + "@" + flavorStr;
                    }
                }

                bzOrderProductSonDO.setSpecValues(specValues);
                bzOrderProductSonDO.setGoodsName(single.getSpuName());
                bzOrderProductSonDO.setGoodsImage(single.getImageUrl());
                bzOrderProductSonDO.setGoodsNum(single.getNumber() * item.getCopies());
                bzOrderProductSonDO.setParentGoodsId(bzOrderProductDO.getOrderProductId());
                bzOrderProductSonDO.setCreateTime(bzOrderDO.getCreateTime());

                sonBatchInsertList.add(bzOrderProductSonDO);
            });
        }

        return sonBatchInsertList;
    }

    /**
     * 处理单品小料
     *
     * @param bzOrderDO
     * @param bzOrderProductDO
     * @param item
     */
    private void handleCondiments(BzOrderDO bzOrderDO, BzOrderProductDO bzOrderProductDO, CalculateCacheDataV2DTO.CommodityInfoVO item) {
        if (item.getSetmealType().equals(SetmealTypeEnum.SINGLE.getCode())) {
            List<CalculateCacheDataV2DTO.CondimentInfoVO> condimentsList = item.getCondimentsList();
            if (CollectionUtils.isNotEmpty(condimentsList)) {
                List<BzOrderCondimentsDO> condimentBatchInsertList = new ArrayList<>();
                condimentsList.forEach(condiment -> {
                    BzOrderCondimentsDO bzOrderCondimentsDO = new BzOrderCondimentsDO();
                    bzOrderCondimentsDO.setOrderSn(bzOrderDO.getOrderSn());
                    bzOrderCondimentsDO.setCondimentId(condiment.getId());
                    bzOrderCondimentsDO.setCondimentImage(condiment.getImageUrl());
                    bzOrderCondimentsDO.setCondimentName(condiment.getName());
                    bzOrderCondimentsDO.setCondimentPrice(condiment.getPrice().multiply(new BigDecimal(item.getCopies() * condiment.getNumber())));
                    bzOrderCondimentsDO.setCondimentNumber(condiment.getNumber());
                    bzOrderCondimentsDO.setCommodityGoodsid(bzOrderProductDO.getOrderProductId().toString());
                    bzOrderCondimentsDO.setCreateTime(bzOrderDO.getCreateTime());

                    condimentBatchInsertList.add(bzOrderCondimentsDO);
                });
                bzOrderCondimentsService.saveBatch(condimentBatchInsertList);
            }
        }
    }

    /**
     * 处理订单日志
     *
     * @param bzOrderDO
     */
    private void handleBzOrderLog(BzOrderDO bzOrderDO) {
        BzOrderLogDO bzOrderLogDO = new BzOrderLogDO();
        bzOrderLogDO.setOrderSn(bzOrderDO.getOrderSn());
        bzOrderLogDO.setOrderStateLog(this.getOrderLogState(bzOrderDO));
        bzOrderLogDO.setLogUserId(bzOrderDO.getMemberId());
        bzOrderLogDO.setLogUserName(bzOrderDO.getMemberName());
        bzOrderLogDO.setLogContent(this.getOrderLogContent(bzOrderDO));
        bzOrderLogDO.setLogTime(new Date());
        bzOrderLogService.save(bzOrderLogDO);
    }

    private Integer getOrderLogState(BzOrderDO order) {
        return order.getOrderState();
    }

    private String getOrderLogContent(BzOrderDO order) {
        return OrderStateEnum.getMessageByCode(order.getOrderState());
    }

    /**
     * 处理订单支付
     *
     * @param bzOrderDO
     */
    private void handleBzOrderPay(BzOrderDO bzOrderDO) {
        BzOrderPayDO bzOrderPayDO = new BzOrderPayDO();
        bzOrderPayDO.setOrderSn(bzOrderDO.getOrderSn());
        bzOrderPayDO.setPaySn(bzOrderDO.getPaySn());
        bzOrderPayDO.setApiPayState(OrderConstants.FAIL.toString());
        //现金结算直接置为支付成功
        if (Integer.parseInt(bzOrderDO.getPaymentCode()) == PaymentMethodEnum.CASH.getCode()) {
            bzOrderPayDO.setApiPayState(OrderConstants.SUCCESS.toString());
            bzOrderPayDO.setPayAmount(bzOrderDO.getPayAmount());
        }
        bzOrderPayDO.setPaySn(bzOrderPayDO.getPaySn());
        bzOrderPayDO.setMemberId(bzOrderDO.getMemberId());
        bzOrderPayDO.setPaymentCode(bzOrderDO.getPaymentCode());
        bzOrderPayDO.setCreateTime(bzOrderDO.getCreateTime());

        bzOrderPayService.save(bzOrderPayDO);
    }

    /**
     * 唯一token校验
     *
     * @param reqVO
     */
    @Deprecated
    protected void checkRepeatToken(T reqVO) {
        Long memberId = reqVO.getMemberId();

        //检查并删除
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(RedisKeyConstants.ORDER_SUBMIT_TOKEN + memberId), reqVO.getAntiRepeatToken());
        if (ObjectUtils.isEmpty(result) || result == 0) {
            throw exception(ORDER_SUBMIT_REPEAT);
        }
    }

    /**
     * 使用优惠券
     *
     * @param cacheDataDTO
     */
    protected void usedCoupon(CalculateCacheDataV2DTO cacheDataDTO) {
        userCouponApi.usedCoupon(
                UsedCouponReqVO.builder()
                        .userCouponId(cacheDataDTO.getUserCouponId())
                        .memberId(cacheDataDTO.getMemberId())
                        .isUsed(OrderConstants.YES)
                        .storeId(cacheDataDTO.getStoreId())
                        .useTime(com.htyoudao.youdao.framework.common.util.date.DateUtils.getNowDate())
                        .payAmount(cacheDataDTO.getPayAmount())
                        .couponPrice(cacheDataDTO.getActivityDiscountAmount())
                        .count(cacheDataDTO.getGoodsCount())
                        .isRefundAction(OrderConstants.NO)
                        .build()
        );
    }

    /**
     * 获取订单详情
     *
     * @param cacheDataDTO
     * @param bzOrderDO
     * @return
     */
    protected OrderDetailDTO getOrderDetail(CalculateCacheDataV2DTO cacheDataDTO, BzOrderDO bzOrderDO) {
        OrderDetailDTO orderDetailDTO = new OrderDetailDTO();

        List<BzOrderPurchaseDO> bzOrderPurchaseDOList = new ArrayList<>();
        List<BzOrderProductSonDO> bzOrderProductSonDOList = new ArrayList<>();
        List<BzOrderProductDO> bzOrderProductDOList = new ArrayList<>();

        cacheDataDTO.getCommodityInfos().forEach(item -> {
            if (Objects.equals(item.getIsPurchase(), OrderConstants.YES)) {
                //处理加购单品
                BzOrderPurchaseDO bzOrderPurchaseDO = this.buildBzOrderPurchaseDO(bzOrderDO, item);
                bzOrderPurchaseDOList.add(bzOrderPurchaseDO);
            } else {
                //处理单品
                BzOrderProductDO bzOrderProductDO = this.buildBzOrderProductDO(bzOrderDO, item);
                bzOrderProductDOList.add(bzOrderProductDO);
                //处理套餐
                List<BzOrderProductSonDO> bzOrderProductSonDOS = this.buildBzOrderProductSonDOList(bzOrderDO, bzOrderProductDO, item);
                bzOrderProductSonDOList.addAll(bzOrderProductSonDOS);
            }
        });

        orderDetailDTO.setProductDOList(bzOrderProductDOList);
        orderDetailDTO.setPurchaseDOList(bzOrderPurchaseDOList);
        orderDetailDTO.setProductSonDOList(bzOrderProductSonDOList);

        return orderDetailDTO;
    }

}
