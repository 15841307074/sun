package com.htyoudao.youdao.module.order.core.calc.v2;

import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.number.NumberUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.*;
import com.htyoudao.youdao.module.commodity.enums.SaleRuleEnum;
import com.htyoudao.youdao.module.order.config.ErrandOrderConfig;
import com.htyoudao.youdao.module.order.core.calc.CalculatorService;
import com.htyoudao.youdao.module.order.core.calc.context.AsyncData;
import com.htyoudao.youdao.module.order.core.calc.context.IdCollections;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.core.submit.DTO.DiscountResultDTO;
import com.htyoudao.youdao.module.order.core.submit.DTO.SplicingOrderConfigDTO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import com.htyoudao.youdao.module.order.enums.SetmealTypeEnum;
import com.htyoudao.youdao.module.order.service.activity.calc.ActivityAllocationService;
import com.htyoudao.youdao.module.order.service.activity.dto.*;
import com.htyoudao.youdao.module.order.util.AmountUtil;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryQuery;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftInventoryResult;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.SettlementActivitiesDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzDiscountTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzCategoryTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzPlaceOrderTypeEnum;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CalculateCacheDataCopyDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.CouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GetReduceAmountReqVO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.sysconfig.SysConfigApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 结算页面金额计算
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class PriceCalculatorV2Service extends CalculatorService {

    private static final String MZ_ERR_GIFT_SOLD_OUT = "活动赠品已抢光，下次早点来哦~";
    private static final String MZ_ERR_PARTICIPATION_LIMIT = "活动参与次数已达上限，本单暂不享受赠品优惠~";
    private static final String MZ_ERR_MARKETING_CHANGED = "活动营销信息已变更，本单暂不享受赠品优惠~";
    private static final String MZ_ERR_QUERY_EXCEPTION = "赠品查询异常";

    private static final long ASYNC_TIMEOUT_SECONDS = 20;
    //12个双活动品
    private static final long MAX_ACTIVITY_GROUPS = 4096;
    private static final LocalTime ERRAND_ORDER_DEADLINE = LocalTime.of(22, 0);
    private static final BigDecimal MIN_ERRAND_REWARD_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal DEFAULT_ERRAND_REWARD_AMOUNT = new BigDecimal("1.5");

    @Resource
    private ThreadPoolExecutor strongExecutor;
    @DubboReference(timeout = 5000)
    private CommodityApi commodityApi;
    @DubboReference(timeout = 5000)
    private StoreApi storeApi;
    @DubboReference(timeout = 5000)
    private UserCouponApi userCouponApi;
    @DubboReference(timeout = 5000)
    private ActivityApi activityApi;
    @DubboReference
    private SysConfigApi sysConfigApi;
    @Resource
    private ActivityAllocationService activityAllocationService;
    @Resource
    private ErrandOrderConfig errandOrderConfig;

    /**
     * 计算
     */
    public CalculateCacheDataV2DTO calculate(SettlementReqV2VO reqVO) throws Exception {
        //校验
        this.validateRequest(reqVO);
        //处理数据
        return this.processData(reqVO);
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(SettlementReqV2VO reqVO) {
        if (CollectionUtils.isEmpty(reqVO.getCommodityInfos())) {
            throw exception(ORDER_GET_CAR_COMMODITY_FAIL);
        }
        this.validateErrandInfo(reqVO);
    }

    /**
     * 校验代取订单参数。
     */
    private void validateErrandInfo(SettlementReqV2VO reqVO) {
        if (!Objects.equals(reqVO.getOrderType(), OrderTypeEnum.ERRAND.getCode())) {
            return;
        }
        validateErrandOrderTimeIfEnabled();
        if (reqVO.getErrandRewardAmount() != null && reqVO.getErrandRewardAmount().compareTo(MIN_ERRAND_REWARD_AMOUNT) < 0) {
            reqVO.setErrandRewardAmount(MIN_ERRAND_REWARD_AMOUNT);
        }
    }

    /**
     * 配置开启时，每天 22 点之后不允许下代取订单。
     */
    public void validateErrandOrderTimeIfEnabled() {
        if (errandOrderConfig.isDeadlineLimitEnabled()) {
            validateErrandOrderTime();
        }
    }

    /**
     * 每天 22 点之后不允许下代取订单。
     */
    public static void validateErrandOrderTime() {
        if (!LocalTime.now().isBefore(ERRAND_ORDER_DEADLINE)) {
            throw new ServiceException(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "22点之后不支持下代取订单");
        }
    }

    /**
     * 处理数据
     */
    private CalculateCacheDataV2DTO processData(SettlementReqV2VO reqVO) {

        //初始化数据
        CalculateCacheDataV2DTO cacheData = this.initBaseData(reqVO);

        //收集所有相关ID集合
        IdCollections ids = this.collectIds(reqVO);

        //异步数据加载
        AsyncData data = this.loadAsyncData(ids, reqVO);

        //构建门店缓存信息
        this.processStoreInfo(cacheData, data.store());

        //合并活动信息
        this.mergeHitActivity(data.skus(), data.productResults());

        //普通金额优惠计算完成后，再按满赠规则生成赠品行
        try {
            this.processMzActivity(data.skus(), data.mzActivities(), reqVO, cacheData);
        } catch (Exception ex) {
            // 满赠属于结算奖励层，代码异常时降级为不赠送，不能影响正品继续结算。
            log.warn("处理满赠活动异常，降级为不赠送 storeId={}", reqVO.getStoreId(), ex);
            setMzErrMsg(cacheData, MZ_ERR_QUERY_EXCEPTION);
        }

        //合并加购品
        this.mergeAfters(data.skus(), data.afters());

        //to CalculateCacheDataDTO
        this.convertToCommodityInfos(data, cacheData);

        //构建优惠券信息
        this.processCouponInfo(cacheData);

        //处理拼单
        this.processSplicing(cacheData);

        //起送费校验
        this.checkMinimumDeliveryFee(cacheData);

        //代取金额计入用户实付：用户赏金 - 门店补贴。
        this.processErrandAmount(cacheData);

        return cacheData;
    }

    /**
     * 处理代取金额。
     */
    private void processErrandAmount(CalculateCacheDataV2DTO cacheData) {
        if (!Objects.equals(cacheData.getOrderType(), OrderTypeEnum.ERRAND.getCode())) {
            return;
        }
        BigDecimal rewardAmount = cacheData.getErrandRewardAmount() == null ? BigDecimal.ZERO : cacheData.getErrandRewardAmount();
        BigDecimal subsidyAmount = cacheData.getErrandStoreSubsidyAmount() == null ? BigDecimal.ZERO : cacheData.getErrandStoreSubsidyAmount();
        subsidyAmount = subsidyAmount.min(rewardAmount);
        if (cacheData.getPayAmount().compareTo(subsidyAmount) < 0) {
            cacheData.setErrandStoreSubsidyIsOk(OrderConstants.NO);
            if (BigDecimal.ZERO.compareTo(cacheData.getMinimumDeliveryFeeLackAmount()) == 0) {
                cacheData.setMinimumDeliveryFeeLackAmount(subsidyAmount.subtract(cacheData.getPayAmount()).setScale(2, RoundingMode.HALF_UP));
            }
        }
        cacheData.setErrandStoreSubsidyAmount(subsidyAmount);
        cacheData.setPayAmount(cacheData.getPayAmount().add(rewardAmount).subtract(subsidyAmount).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 初始化基本数据
     */
    private CalculateCacheDataV2DTO initBaseData(SettlementReqV2VO reqVO) {
        CalculateCacheDataV2DTO cacheDataDTO = new CalculateCacheDataV2DTO();
        cacheDataDTO.setOpenId(SecurityFrameworkUtils.getLoginOpenid());
        cacheDataDTO.setMemberName(SecurityFrameworkUtils.getLoginUsername());
        cacheDataDTO.setMemberId(SecurityFrameworkUtils.getLoginUserId());
        cacheDataDTO.setTakeAwayTel(SecurityFrameworkUtils.getLoginMobile());
        cacheDataDTO.setMemberAvatar(reqVO.getMemberAvatar());

        //初始化
        cacheDataDTO.setDeliveryFee(BigDecimal.ZERO);
        cacheDataDTO.setPayAmount(BigDecimal.ZERO);
        cacheDataDTO.setPackingFee(BigDecimal.ZERO);
        cacheDataDTO.setCommodityAmount(BigDecimal.ZERO);
        cacheDataDTO.setActivityDiscountAmount(BigDecimal.ZERO);
        cacheDataDTO.setMinimumDeliveryFeeIsOk(OrderConstants.YES);
        cacheDataDTO.setMinimumDeliveryFeeLackAmount(BigDecimal.ZERO);
        cacheDataDTO.setErrandStoreSubsidyIsOk(OrderConstants.YES);

        //拼单
        cacheDataDTO.setMainId(reqVO.getMainId());

        //订单类型 0堂食 1打包 2外卖 3代取
        cacheDataDTO.setOrderType(reqVO.getOrderType());
        cacheDataDTO.setIsDc(reqVO.getIsDc());

        cacheDataDTO.setErrandRewardAmount(Objects.equals(reqVO.getOrderType(), OrderTypeEnum.ERRAND.getCode()) ? reqVO.getErrandRewardAmount() : BigDecimal.ZERO);
        cacheDataDTO.setErrandStoreSubsidyAmount(BigDecimal.ZERO);

        //优惠券
        cacheDataDTO.setUserCouponId(reqVO.getUserCouponId());

        if (ObjectUtils.isEmpty(cacheDataDTO.getTakeAwayTel())) {
            //下单电话
            cacheDataDTO.setTakeAwayTel(reqVO.getTakeAwayTel());
        }

        return cacheDataDTO;
    }

    /**
     * 起送费校验
     */
    private void checkMinimumDeliveryFee(CalculateCacheDataV2DTO cacheData) {
        if (OrderTypeEnum.TAKEAWAY.getCode() != cacheData.getOrderType()
                && OrderTypeEnum.ERRAND.getCode() != cacheData.getOrderType()) {
            return;
        }
        if (ObjectUtils.isEmpty(cacheData.getMinimumDeliveryFee())) {
            return;
        }

        BigDecimal payAmount = cacheData.getPayAmount() == null ? BigDecimal.ZERO : cacheData.getPayAmount();
        BigDecimal afterAmount = cacheData.getAfterAmount() == null ? BigDecimal.ZERO : cacheData.getAfterAmount();
        BigDecimal orderAmount = Objects.equals(cacheData.getOrderType(), OrderTypeEnum.ERRAND.getCode())
                ? payAmount
                : payAmount.subtract(afterAmount);
        BigDecimal lackAmount = cacheData.getMinimumDeliveryFee().subtract(orderAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        cacheData.setMinimumDeliveryFeeLackAmount(lackAmount);
        if (lackAmount.compareTo(BigDecimal.ZERO) > 0) {
            cacheData.setMinimumDeliveryFeeIsOk(OrderConstants.NO);
            cacheData.setErrandStoreSubsidyIsOk(OrderConstants.NO);
        }
    }

    /**
     * 收集所有相关ID集合
     */
    private IdCollections collectIds(SettlementReqV2VO reqVO) {
        IdCollections ids = new IdCollections();
        ids.storeId = reqVO.getStoreId();

        reqVO.getCommodityInfos().forEach(commodity -> {
            if (ObjectUtils.isEmpty(commodity.getSkuId())) {
                throw exception(ORDER_COMMODITY_SKU_ID_ERROR);
            }

            //套餐
            if (commodity.getSetmealType() != SetmealTypeEnum.SINGLE.getCode() && CollectionUtils.isEmpty(commodity.getSingleFlavorList())) {
                throw exception(ORDER_PACKAGE_INFORMATION_INCOMPLETE);
            }

            //单品
            if (commodity.getSetmealType() == SetmealTypeEnum.SINGLE.getCode()) {
                commodity.setSingleFlavorList(Collections.EMPTY_LIST);
            }

            //加购
            if (OrderConstants.YES.equals(commodity.getIsPurchase())) {
                if (ObjectUtils.isEmpty(commodity.getAfterId())) {
                    throw exception(ORDER_COMMODITY_AFTER_ID_ERROR);
                }
                ids.afterIds.add(commodity.getAfterId());
            } else {
                ids.storeSkuIds.add(commodity.getSkuId());
                ids.commodityIds.add(commodity.getCommodityId());
            }

            Optional.ofNullable(commodity.getSingleFlavorList())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(SettlementReqV2VO.SingleFlavorVO::getSingleId)
                    .filter(Objects::nonNull)
                    .forEach(ids.storeSingleIds::add);
        });
        return ids;
    }

    /**
     * 异步数据加载（四阶段编排）
     * - 阶段1：并发拉取基础数据
     * - 阶段2：sku + single 完成后，按入参把 single, 小料, 属性 整合进 StoreSkuInfoDTO
     * - 阶段3：skuWithSingles + njnz + mj 完成 => 立刻启动“优惠计算”异步
     * - 阶段4：等待所有任务收口（含优惠计算）
     */
    private AsyncData loadAsyncData(IdCollections ids, SettlementReqV2VO reqVO) {

        // ===== 阶段1：并发拉取基础数据 =====
        CompletableFuture<StoreDTO> storeFuture =
                this.named("loadStore", this.loadStore(ids.storeId));

        // key: skuId
        CompletableFuture<List<StoreSkuInfoDTO>> skuFuture =
                this.named("loadSkus", this.loadSkus(ids.storeSkuIds));

        // key: singleId
        CompletableFuture<Map<Long, StoreSingleInfoDTO>> singleFuture =
                this.named("loadSingles", this.loadSingles(ids.storeSingleIds));

        // 一次RPC加载三类结算活动，Promotion侧共用一次活动主表查询。
        CompletableFuture<SettlementActivitiesDTO> settlementActivitiesFuture =
                this.named("loadSettlementActivities",
                        this.loadSettlementActivities(ids.commodityIds, ids.storeId));
        CompletableFuture<Map<Long, List<ActivityNjnzDTO>>> activityNJNZFuture = settlementActivitiesFuture
                .thenApply(SettlementActivitiesDTO::getNjnzActivities);
        CompletableFuture<Map<Long, List<ActivityMJDTO>>> activityMJFuture = settlementActivitiesFuture
                .thenApply(SettlementActivitiesDTO::getMjActivities);
        CompletableFuture<Map<Long, List<ActivityMzDTO>>> activityMZFuture = settlementActivitiesFuture
                .thenApply(SettlementActivitiesDTO::getMzActivities);

        // key: afterId
        CompletableFuture<Map<Long, AfterInfoDTO>> afterFuture =
                this.named("loadAfters", this.loadAfters(ids.afterIds, ids.storeId));

        // ===== 阶段2：sku + single 完成 => 整合 single, 小料, 属性 到 StoreSkuInfoDTO =====
        CompletableFuture<List<StoreSkuInfoDTO>> skuWithSinglesFuture =
                CompletableFuture.allOf(skuFuture, singleFuture)
                        .thenApply(v -> {
                            //合并sku及子集
                            return this.checkAndMergeSku(
                                    skuFuture.join(),
                                    singleFuture.join(),
                                    reqVO
                            );
                        });

        // ===== 阶段3：skuWithSingles + njnz + mj 完成 => 立刻启动“优惠计算”异步 =====
        CompletableFuture<List<ProductResult>> discountFuture =
                CompletableFuture.allOf(skuWithSinglesFuture, activityNJNZFuture, activityMJFuture)
                        .thenCompose(v -> this.named("computeDiscount",
                                this.computeDiscountAsync(
                                        // 已整合 single,小料，属性 的 skus
                                        skuWithSinglesFuture.join(),
                                        activityNJNZFuture.join(),
                                        activityMJFuture.join()
                                )
                        ));

        // ===== 阶段4：最终收口，等待（阶段1所有 + 阶段2整合 + 阶段2优惠计算） =====
        CompletableFuture<Void> all = CompletableFuture.allOf(
                storeFuture,
                skuFuture,
                singleFuture,
                skuWithSinglesFuture,
                activityNJNZFuture,
                activityMJFuture,
                activityMZFuture,
                settlementActivitiesFuture,
                afterFuture,
                discountFuture
        );

        try {
            all.get(ASYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[InterruptedException][线程中断]", e);
            throw exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();

            // CompletableFuture 里抛出的异常，99% 会被包一层 CompletionException
            if (cause instanceof CompletionException && cause.getCause() != null) {
                cause = cause.getCause();
            }

            // 如果是你的业务异常，原样抛出
            if (cause instanceof ServiceException) {
                throw (ServiceException) cause;
            }

            log.error("[ExecutionException][异步任务异常]", cause);
            throw exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        } catch (TimeoutException e) {
            log.error("[TimeoutException][异步任务超时]", e);
            throw exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }

        // 注意：这里把 skus 返回为 “已整合 single 的 skus”
        return new AsyncData(storeFuture.join(), null, skuWithSinglesFuture.join(), afterFuture.join(),
                singleFuture.join(), discountFuture.join(), activityMZFuture.join());
    }

    /**
     * 处理结算中的满赠活动。
     *
     * <p>满赠位于普通营销活动之后执行，完整处理顺序如下：</p>
     * <ol>
     *     <li>每个商品只保留自己候选满赠活动中最后创建的一条，创建时间相同时用 activityId 兜底；</li>
     *     <li>商品已命中 N 件 N 折或满减满折时，校验普通活动与满赠活动是否双向允许叠加；</li>
     *     <li>按 activityId 将选中同一个满赠活动的商品合并，避免同一活动按商品重复计算和重复赠送；</li>
     *     <li>每个满赠活动使用自己组内的有效商品独立计算金额或件数门槛；</li>
     *     <li>小程序按活动校验用户参与次数，点餐机没有用户维度，因此跳过该校验；</li>
     *     <li>合并查询所有命中活动的赠品门店 SKU，再按活动分别读取实时库存并生成赠品行。</li>
     * </ol>
     *
     * <p>注意：</p>
     * <ul>
     *     <li>普通活动与满赠任意一方不允许叠加时，只排除当前商品的满赠资格，普通活动仍然有效；</li>
     *     <li>不同满赠活动可以在同一订单中分别赠送；</li>
     *     <li>不同活动赠送同一个商品时不合并赠品行，以保留活动和库存归属；</li>
     *     <li>结算阶段只展示可赠数量，不锁库存，最终数量以提交订单时的实际锁定结果为准。</li>
     * </ul>
     *
     * @param skus        已完成普通活动计算的商品列表；命中的满赠赠品会追加到该列表
     * @param activityMap 商品 ID -> 当前商品可参与的满赠活动列表
     * @param reqVO       结算请求，用于获取门店、终端类型和用户信息
     * @param cacheData   结算返回数据，用于写入满赠降级提示 errMsg
     */
    private void processMzActivity(List<StoreSkuInfoDTO> skus,
                                   Map<Long, List<ActivityMzDTO>> activityMap,
                                   SettlementReqV2VO reqVO,
                                   CalculateCacheDataV2DTO cacheData) {
        if (CollectionUtils.isEmpty(skus) || CollectionUtils.isEmpty(activityMap)) {
            return;
        }

        // 1. 定义“最新活动”的统一排序规则：创建时间越晚越新，时间相同时 activityId 越大越新。
        // null 排在前面，因此不会优先于具有正常创建时间或 activityId 的活动。
        Comparator<ActivityMzDTO> latestActivityComparator = Comparator
                .comparing(ActivityMzDTO::getCreateTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(ActivityMzDTO::getActivityId, Comparator.nullsFirst(Comparator.naturalOrder()));

        // 2. 按商品维度选择活动，不能把所有商品的活动打平后全局只选一条。
        // 没有赠品配置的活动无法产生奖励，在此直接排除。
        Map<Long, Integer> setmealTypeByCommodity = skus.stream()
                .filter(Objects::nonNull)
                .filter(sku -> sku.getCommodityId() != null)
                .collect(Collectors.toMap(StoreSkuInfoDTO::getCommodityId,
                        sku -> Optional.ofNullable(sku.getSetmealType()).orElse(SetmealTypeEnum.SINGLE.getCode()),
                        (first, ignored) -> first));
        Map<Long, ActivityMzDTO> latestActivityByCommodity = new HashMap<>();
        activityMap.forEach((commodityId, activities) -> Optional.ofNullable(activities)
                .orElse(List.of()).stream()
                .filter(Objects::nonNull)
                .filter(activity -> !CollectionUtils.isEmpty(activity.getGifts()))
                // 必须先按商品类型过滤，再选最新活动；否则不适用的最新活动会遮蔽可用的次新活动。
                .filter(activity -> isMzCategoryMatched(activity, setmealTypeByCommodity.get(commodityId)))
                .max(latestActivityComparator)
                .ifPresent(activity -> latestActivityByCommodity.put(commodityId, activity)));

        if (latestActivityByCommodity.isEmpty()) {
            // 已查询到满赠候选，但活动已没有有效赠品配置，视为营销配置发生变化。
            boolean hasActivityCandidate = activityMap.values().stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .anyMatch(Objects::nonNull);
            if (hasActivityCandidate) {
                log.warn("满赠候选活动无有效赠品或不适用当前商品类型，营销信息可能已变更 commodityIds={}",
                        activityMap.keySet());
                setMzErrMsg(cacheData, MZ_ERR_MARKETING_CHANGED);
            }
            return;
        }

        // 3. 过滤不能参与满赠的商品，并按 activityId 反向分组。
        // 例如商品A、商品B都选中活动X时，必须将A+B合并计算X一次，不能各计算一次而重复赠送。
        Map<Long, MzActivityGroup> activityGroups = new LinkedHashMap<>();
        skus.stream()
                // 加购商品和此前已经生成的赠品都不能再次参与满赠凑单。
                .filter(s -> !OrderConstants.YES.equals(s.getIsPurchase()))
                .filter(s -> !OrderConstants.YES.equals(s.getIsGift()))
                .forEach(sku -> {
                    ActivityMzDTO activity = latestActivityByCommodity.get(sku.getCommodityId());
                    // canJoinMzActivity 保留原有互斥规则：已命中普通活动时必须由双方同时允许叠加。
                    if (activity == null || !canJoinMzActivity(activity, sku)) {
                        return;
                    }
                    activityGroups.computeIfAbsent(activity.getActivityId(),
                                    ignored -> new MzActivityGroup(activity, new ArrayList<>()))
                            .skus().add(sku);
                });
        if (activityGroups.isEmpty()) {
            return;
        }

        // 4. 每个活动只使用本组有效商品独立计算门槛，并记录理论赠品及数量。
        List<MzActivityAwards> matchedActivities = new ArrayList<>();
        for (MzActivityGroup group : activityGroups.values()) {
            ActivityMzDTO activity = group.activity();

            // 满赠只统计实际支付的商品：原价品和折扣品参与，买一赠一产生的零元赠品不参与。
            // N件N折和买赠都会拆出内嵌 giftSku，因此必须依据折后实付金额区分，不能仅凭字段名称判断。
            BigDecimal amount = group.skus().stream().map(this::getMzEligibleAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int count = group.skus().stream().mapToInt(this::getMzEligibleCount).sum();
            List<MzGiftAward> awards = calculateMzAwards(activity, amount, count);
            if (awards.isEmpty()) {
                continue;
            }

            // 5. 只有已经达到门槛的活动才查询参与次数，避免未命中活动产生无效远程和数据库访问。
            // 小程序按活动分别校验；一个活动达到上限或校验异常，不影响同订单中的其他满赠活动。
            if (!Boolean.TRUE.equals(reqVO.getIsDc())) {
                try {
                    if (!Boolean.TRUE.equals(activityApi.checkMzCanParticipate(activity.getActivityId(),
                            SecurityFrameworkUtils.getLoginUserId()).getCheckedData())) {
                        setMzErrMsg(cacheData, MZ_ERR_PARTICIPATION_LIMIT);
                        continue;
                    }
                } catch (Exception ex) {
                    log.warn("满赠参与次数校验失败，降级为不赠送 activityId={}", activity.getActivityId(), ex);
                    setMzErrMsg(cacheData, MZ_ERR_QUERY_EXCEPTION);
                    continue;
                }
            }
            matchedActivities.add(new MzActivityAwards(activity, awards, group.skus()));
        }
        if (matchedActivities.isEmpty()) {
            return;
        }

        // 6. 汇总所有命中活动的赠品商品 ID，一次查询当前门店 SKU，避免按活动产生远程调用 N+1。
        Set<Long> giftCommodityIds = matchedActivities.stream()
                .flatMap(matched -> matched.awards().stream())
                .map(award -> award.gift().getGiftCommodityId())
                .collect(Collectors.toSet());
        List<StoreSkuInfoDTO> giftSkuCandidates;
        try {
            giftSkuCandidates = Optional.ofNullable(commodityApi
                            .getStoreSkuListByCommodityIds(reqVO.getStoreId(), giftCommodityIds).getCheckedData())
                    .orElse(List.of());
        } catch (Exception ex) {
            log.warn("查询满赠门店SKU失败，降级为不赠送 activityIds={}", activityGroups.keySet(), ex);
            setMzErrMsg(cacheData, MZ_ERR_QUERY_EXCEPTION);
            return;
        }
        // 同一赠品商品在查询结果中存在多条 SKU 时保留第一条；不可售或未上架 SKU 会被过滤。
        Map<Long, StoreSkuInfoDTO> giftSkuMap = giftSkuCandidates.stream()
                .filter(s -> isGiftSkuUp(s, reqVO.getIsDc()))
                .collect(Collectors.toMap(StoreSkuInfoDTO::getCommodityId, Function.identity(), (a, b) -> a));

        // 点餐机不参与库存；其他来源把所有活动赠品合并为一次RPC，Promotion再按Hash Key使用HMGET。
        Map<MzInventoryKey, Integer> inventoryMap = Collections.emptyMap();
        if (!Boolean.TRUE.equals(reqVO.getIsDc())) {
            Map<MzInventoryKey, MzGiftInventoryQuery> queryMap = new LinkedHashMap<>();
            for (MzActivityAwards matched : matchedActivities) {
                for (MzGiftAward award : matched.awards()) {
                    Long giftCommodityId = award.gift().getGiftCommodityId();
                    if (!giftSkuMap.containsKey(giftCommodityId)) continue;
                    MzInventoryKey key = new MzInventoryKey(matched.activity().getActivityId(), reqVO.getStoreId(), giftCommodityId);
                    MzGiftInventoryQuery query = new MzGiftInventoryQuery();
                    query.setActivityId(key.activityId());
                    query.setStoreId(key.storeId());
                    query.setGiftCommodityId(key.giftCommodityId());
                    queryMap.putIfAbsent(key, query);
                }
            }
            try {
                List<MzGiftInventoryResult> results = Optional.ofNullable(activityApi
                        .queryMzGiftInventories(new ArrayList<>(queryMap.values())).getCheckedData()).orElse(List.of());
                inventoryMap = new HashMap<>();
                for (MzGiftInventoryResult result : results) {
                    inventoryMap.put(new MzInventoryKey(result.getActivityId(), result.getStoreId(),
                            result.getGiftCommodityId()), result.getInventory());
                }
            } catch (Exception ex) {
                log.warn("批量查询满赠库存失败，降级为不赠送 activityIds={}", activityGroups.keySet(), ex);
                setMzErrMsg(cacheData, MZ_ERR_QUERY_EXCEPTION);
                return;
            }
        }

        // 7. 按活动、赠品读取批量库存结果并追加赠品行。
        // 库存属于活动维度，不能只按 giftCommodityId 查询或合并。
        for (MzActivityAwards matched : matchedActivities) {
            boolean giftAdded = false;
            for (MzGiftAward award : matched.awards()) {
                StoreSkuInfoDTO source = giftSkuMap.get(award.gift().getGiftCommodityId());
                // 当前门店不存在赠品 SKU，或 SKU 状态不可售时，本活动的该档赠品不下发。
                // 包含：门店下架、不支持当前点餐终端、分组可选套餐、有小料配置等情况。
                if (source == null) {
                    log.warn("满赠商品在当前门店无可用SKU，营销信息可能已变更 activityId={} storeId={} giftCommodityId={}",
                            matched.activity().getActivityId(), reqVO.getStoreId(), award.gift().getGiftCommodityId());
                    setMzErrMsg(cacheData, MZ_ERR_MARKETING_CHANGED);
                    continue;
                }
                int quantity;
                if (Boolean.TRUE.equals(reqVO.getIsDc())) {
                    // 点餐机订单不参与满赠库存维护，也不受活动库存限制，按规则计算出的理论数量赠送。
                    quantity = award.quantity();
                } else {
                    // 结算只读库存、不占库存：-1 为不限库存，0/null 不赠，有限库存按剩余量截断。
                    // 提交订单仍会重新锁库，因此最终赠送数量以提交时实际锁定数量为准。
                    Integer inventory = inventoryMap.get(new MzInventoryKey(matched.activity().getActivityId(),
                            reqVO.getStoreId(), award.gift().getGiftCommodityId()));
                    // null：缓存未初始化；0：无库存；负数：不限库存；正数：按实时库存截断。
                    if (inventory == null) {
                        // 查询成功但没有该活动赠品的库存元数据，一般是活动配置或缓存已经变化。
                        log.warn("满赠库存元数据不存在，营销信息可能已变更 activityId={} storeId={} giftCommodityId={}",
                                matched.activity().getActivityId(), reqVO.getStoreId(), award.gift().getGiftCommodityId());
                        setMzErrMsg(cacheData, MZ_ERR_MARKETING_CHANGED);
                        quantity = 0;
                    } else if (inventory == 0) {
                        setMzErrMsg(cacheData, MZ_ERR_GIFT_SOLD_OUT);
                        quantity = 0;
                    } else if (inventory < 0) {
                        quantity = award.quantity();
                    } else {
                        quantity = Math.min(award.quantity(), inventory);
                        if (inventory < award.quantity()) {
                            // 循环满赠库存不足时仍按剩余库存赠送，同时提示库存不足。
                            setMzErrMsg(cacheData, MZ_ERR_GIFT_SOLD_OUT);
                        }
                    }
                }
                if (quantity <= 0) continue;
                // 不合并不同活动赠送的相同商品，保留活动归属，便于分别锁库、取消和退款。
                skus.add(buildMzGiftSku(source, matched.activity(), quantity));
                giftAdded = true;
            }
            // 只有实际生成赠品后，参与凑门槛的商品才算命中满赠，并合并叠加范围。
            if (giftAdded) {
                matched.skus().forEach(sku -> mergeMzStackableActivities(sku, matched.activity()));
            }
        }
    }

    private record MzInventoryKey(Long activityId, Long storeId, Long giftCommodityId) {
    }

    /**
     * 设置满赠结算提示。一个订单可能同时命中多个满赠活动，提示按优先级保留：
     * 代码异常 &gt; 库存不足 &gt; 参与上限 &gt; 营销信息变化。
     */
    private static void setMzErrMsg(CalculateCacheDataV2DTO cacheData, String candidate) {
        if (cacheData == null || candidate == null) {
            return;
        }
        String current = cacheData.getErrMsg();
        if (current == null || mzErrPriority(candidate) > mzErrPriority(current)) {
            cacheData.setErrMsg(candidate);
        }
    }

    private static int mzErrPriority(String errMsg) {
        if (MZ_ERR_QUERY_EXCEPTION.equals(errMsg)) return 4;
        if (MZ_ERR_GIFT_SOLD_OUT.equals(errMsg)) return 3;
        if (MZ_ERR_PARTICIPATION_LIMIT.equals(errMsg)) return 2;
        if (MZ_ERR_MARKETING_CHANGED.equals(errMsg)) return 1;
        return 0;
    }

    private boolean canJoinMzActivity(ActivityMzDTO mz, StoreSkuInfoDTO sku) {
        if (sku.getHitActivity() == null) return true;

        if (!Integer.valueOf(1).equals(mz.getDiscountStackable())
                || !Integer.valueOf(1).equals(sku.getDiscountStackable())) {
            return false;
        }

        Integer type = sku.getHitActivity().getActivityType();
        // PC 配置的叠加标识不是活动类型：2=N件N折，3=满减满折，4=满赠。
        int normalActivityCode = Objects.equals(type, ActivityTypeEnum.NJ_NZ.getCode()) ? 2 : Objects.equals(type, ActivityTypeEnum.MJ.getCode()) ? 3 : -1;
        if (normalActivityCode < 0) return false;

        List<Integer> mzStackable = Optional.ofNullable(mz.getStackableActivities()).orElse(List.of());
        List<Integer> normalStackable = Optional.ofNullable(sku.getStackableActivitieList()).orElse(List.of());

        // 必须双向允许：普通活动允许满赠，并且满赠允许当前普通活动。
        return normalStackable.contains(4) && mzStackable.contains(normalActivityCode);
    }

    private void mergeMzStackableActivities(StoreSkuInfoDTO sku, ActivityMzDTO mz) {
        List<Integer> mzStackable = Optional.ofNullable(mz.getStackableActivities()).orElse(List.of());
        if (sku.getHitActivity() == null) {
            sku.setDiscountStackable(mz.getDiscountStackable());
            sku.setStackableActivitieList(Integer.valueOf(1).equals(mz.getDiscountStackable())
                    ? new ArrayList<>(mzStackable) : List.of());
            return;
        }

        boolean canStack = Integer.valueOf(1).equals(sku.getDiscountStackable())
                && Integer.valueOf(1).equals(mz.getDiscountStackable());
        sku.setDiscountStackable(canStack ? 1 : 0);
        if (!canStack) {
            sku.setStackableActivitieList(List.of());
            return;
        }
        Set<Integer> intersection = new LinkedHashSet<>(
                Optional.ofNullable(sku.getStackableActivitieList()).orElse(List.of()));
        intersection.retainAll(mzStackable);
        sku.setStackableActivitieList(new ArrayList<>(intersection));
    }

    private boolean isMzCategoryMatched(ActivityMzDTO activity, Integer setmealType) {
        // categoryType 仅在按品类限制时生效；按商品限制的活动已由 Promotion 完成商品范围筛选。
        if (!Objects.equals(activity.getPlaceOrderType(), MzPlaceOrderTypeEnum.BY_CATEGORY.getCode())) {
            return true;
        }
        Integer categoryType = activity.getCategoryType();
        if (categoryType == null || Objects.equals(categoryType, MzCategoryTypeEnum.ALL.getCode())) {
            return true;
        }
        boolean isCombo = Objects.equals(setmealType, SetmealTypeEnum.FIXED.getCode())
                || Objects.equals(setmealType, SetmealTypeEnum.GROUP.getCode());
        if (Objects.equals(categoryType, MzCategoryTypeEnum.SINGLE.getCode())) {
            return !isCombo;
        }
        if (Objects.equals(categoryType, MzCategoryTypeEnum.COMBO.getCode())) {
            return isCombo;
        }
        // 未知品类值按不限制兼容，避免异常配置直接导致活动静默失效。
        return true;
    }

    private BigDecimal getMzEligibleAmount(StoreSkuInfoDTO sku) {
        BigDecimal amount = getDiscountedRowAmount(sku);
        if (sku.getGiftSku() == null) {
            return amount;
        }

        // 第二件半价等折扣品按折后实付金额参与；买一赠一的免费品实付为0，不增加满赠金额。
        BigDecimal nestedPaidAmount = getDiscountedRowAmount(sku.getGiftSku());
        return nestedPaidAmount.compareTo(BigDecimal.ZERO) > 0
                ? amount.add(nestedPaidAmount) : amount;
    }

    private BigDecimal getDiscountedRowAmount(StoreSkuInfoDTO sku) {
        int copies = Optional.ofNullable(sku.getCopies()).orElse(0);
        BigDecimal original = sku.getSkuPrice().multiply(BigDecimal.valueOf(copies));
        return original.subtract(Optional.ofNullable(sku.getPromotionDiscountAmount()).orElse(BigDecimal.ZERO));
    }

    private int getMzEligibleCount(StoreSkuInfoDTO sku) {
        int count = Optional.ofNullable(sku.getCopies()).orElse(0);
        if (sku.getGiftSku() == null) {
            return count;
        }

        // 内嵌行仍有折后实付金额，说明是第二件半价等折扣购买品，应参与凑件；零元买赠品不参与。
        BigDecimal nestedPaidAmount = getDiscountedRowAmount(sku.getGiftSku());
        return nestedPaidAmount.compareTo(BigDecimal.ZERO) > 0
                ? count + Optional.ofNullable(sku.getGiftSku().getCopies()).orElse(0)
                : count;
    }

    private List<MzGiftAward> calculateMzAwards(ActivityMzDTO activity, BigDecimal amount, int count) {
        BigDecimal base = Objects.equals(activity.getDiscountType(), MzDiscountTypeEnum.BUY_N_ITEMS_GIFT.getCode()) ? BigDecimal.valueOf(count) : amount;
        List<MzGiftDTO> gifts = activity.getGifts().stream()
                .filter(g -> g.getThreshold() != null && g.getThreshold().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(MzGiftDTO::getThreshold))
                .toList();
        if (Objects.equals(activity.getDiscountRules(), 2)) {
            // 循环满赠理论数量不设单订单上限，最终只受实时库存限制。
            if (gifts.isEmpty()) return List.of();
            MzGiftDTO gift = gifts.get(0);
            int quantity = base.divideToIntegralValue(gift.getThreshold()).intValue();
            return quantity > 0 ? List.of(new MzGiftAward(gift, quantity)) : List.of();
        }
        // 阶梯满赠只命中已满足门槛中最高的一档，不累计低档赠品。
        return gifts.stream().filter(g -> base.compareTo(g.getThreshold()) >= 0)
                .max(Comparator.comparing(MzGiftDTO::getThreshold))
                .map(g -> List.of(new MzGiftAward(g, 1))).orElse(List.of());
    }

    private boolean isGiftSkuUp(StoreSkuInfoDTO sku, boolean isDc) {
        if (sku == null || (sku.getTimeSharingTopping() == 1 && !Boolean.TRUE.equals(sku.getIsUp()))) return false;
        return isDc ? Objects.equals(sku.getCommodityStoreSpuMachineStatus(), 1)
                : Objects.equals(sku.getCommodityStoreSpuAppletStatus(), 1);
    }

    private StoreSkuInfoDTO buildMzGiftSku(StoreSkuInfoDTO source, ActivityMzDTO activity, int quantity) {
        StoreSkuInfoDTO gift = new StoreSkuInfoDTO();
        BeanUtils.copyProperties(source, gift);
        gift.setCopies(quantity);
        gift.setIsPurchase(OrderConstants.NO);
        gift.setIsGift(OrderConstants.YES);
        // 满赠赠品按单品落单，避免继承赠品源商品的套餐类型。
        gift.setSetmealType(SetmealTypeEnum.SINGLE.getCode());
        gift.setPackageFee(BigDecimal.ZERO);
        // 赠品以正常售价计入商品金额，再记等额营销优惠，保证实付为0且财务优惠数据完整。
        gift.setPromotionDiscountAmount(source.getSkuPrice().multiply(BigDecimal.valueOf(quantity)));
        gift.setIsGetActivity(OrderConstants.YES);
        StoreSkuInfoDTO.ActivityBaseDetailDTO detail = new StoreSkuInfoDTO.ActivityBaseDetailDTO();
        detail.setActivityId(activity.getActivityId());
        detail.setActivityName(activity.getActivityName());
        detail.setActivityType(ActivityTypeEnum.MZ.getCode());
        detail.setDiscountStackable(activity.getDiscountStackable());
        detail.setDiscountType(activity.getDiscountType());
        detail.setActivityTag(activity.getTag());
        // 一条订单商品明细只命中一个活动：满赠只写在赠品行，参与凑门槛的正品不写满赠。
        gift.setHitActivity(detail);
        gift.setDiscountStackable(activity.getDiscountStackable());
        gift.setStackableActivitieList(Optional.ofNullable(activity.getStackableActivities()).orElse(List.of()));
        return gift;
    }

    private record MzGiftAward(MzGiftDTO gift, int quantity) {
    }

    private record MzActivityGroup(ActivityMzDTO activity, List<StoreSkuInfoDTO> skus) {
    }

    private record MzActivityAwards(ActivityMzDTO activity, List<MzGiftAward> awards, List<StoreSkuInfoDTO> skus) {
    }

    /**
     * 单点不送校验
     */
    private void noDeliveryCheck(SettlementReqV2VO reqVO, List<StoreSkuInfoDTO> skus) {
        boolean allNoDelivery = !skus.isEmpty() &&
                skus.stream().allMatch(sku -> sku.getNoDeliveryForSingleOrder() != null && sku.getNoDeliveryForSingleOrder().equals("1"));
        if (allNoDelivery && reqVO.getOrderType().equals(OrderTypeEnum.TAKEAWAY.getCode())) {
            throw exception(ORDER_COMMODITY_ALL_NOT_ALLOW_SEND);
        }
    }

    /**
     * 校验门店是否正常
     */
    private void validateStoreIsOk(StoreDTO storeDTO) {
        //门店不存在
        if (ObjectUtils.isEmpty(storeDTO)) {
            throw exception(ORDER_GET_STORE_FAIL);
        }

        //门店关店
        if (OrderConstants.YES.equals(storeDTO.getStoreStatus())) {
            throw exception(ORDER_STORE_NOT_OPEN);
        }
    }

    /**
     * 整合 + 校验
     */
    private List<StoreSkuInfoDTO> checkAndMergeSku(List<StoreSkuInfoDTO> skus,
                                                   Map<Long, StoreSingleInfoDTO> singleMap,
                                                   SettlementReqV2VO reqVO) {
        List<SettlementReqV2VO.CommodityInfoVO> items = reqVO.getCommodityInfos();
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        //单点不送校验
        this.noDeliveryCheck(reqVO, skus);

        Map<Long, StoreSkuInfoDTO> skuMap = Optional.of(skus).orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(StoreSkuInfoDTO::getSkuId, Function.identity(), (a, b) -> a));

        List<StoreSkuInfoDTO> mergedSkus = new ArrayList<>(items.size());

        AtomicInteger index = new AtomicInteger(1);
        for (SettlementReqV2VO.CommodityInfoVO item : items) {
            if (item == null) continue;

            Long skuId = item.getSkuId();
            if (skuId == null || OrderConstants.YES.equals(item.getIsPurchase())) {
                continue;
            }

            StoreSkuInfoDTO origin = Optional.ofNullable(skuMap.get(skuId))
                    .orElseThrow(() -> exception(ORDER_COMMODITY_NOT_EXISTS));

            //建新对象，否则相同skuId就错乱
            StoreSkuInfoDTO sku = new StoreSkuInfoDTO();
            BeanUtils.copyProperties(origin, sku);

            this.enrichSkuFromRequest(item, sku, singleMap, reqVO.getIsDc());

            try {
                String uId = index.getAndIncrement() + MD5Utils.md5Hex(sku.toString().getBytes(StandardCharsets.UTF_8));
                sku.setUId(uId);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            mergedSkus.add(sku);
        }

        return mergedSkus;
    }

    private void enrichSkuFromRequest(SettlementReqV2VO.CommodityInfoVO item,
                                      StoreSkuInfoDTO sku,
                                      Map<Long, StoreSingleInfoDTO> singleMap,
                                      boolean isDc) {
        this.validateSku(item, sku, isDc);
        this.applySkus(item, sku);
        this.applyCondiments(item, sku);
        this.applyFlavors(item, sku);
        this.applySingles(item, sku, singleMap, isDc);
    }

    private void mergeHitActivity(List<StoreSkuInfoDTO> skus, List<ProductResult> productResults) {
        if (CollectionUtils.isEmpty(skus)) return;

        Map<String, List<ProductResult>> resultsByUId =
                Optional.ofNullable(productResults).orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(ProductResult::getUId));

        for (StoreSkuInfoDTO sku : skus) {
            if (sku == null || sku.getSkuId() == null) continue;

            List<ProductResult> results = resultsByUId.getOrDefault(sku.getUId(), Collections.emptyList());
            HitActivityView view;
            try {
                view = this.buildHitActivityView(sku, results);
            } catch (Exception e) {
                // 降级为不命中，避免影响下单
                log.warn("==> [mergeHitActivity] buildHitActivityView 失败,降级为不命中, skuId={}, err={}", sku.getSkuId(), e.toString());
                this.clearHitActivity(sku);
                continue;
            }

            if (!view.matched) {
                this.clearHitActivity(sku);
                continue;
            }

            this.applyHitActivityView(sku, view);
        }
    }

    private HitActivityView buildHitActivityView(StoreSkuInfoDTO sku, List<ProductResult> results) {
        HitActivityView view = new HitActivityView();

        if (CollectionUtils.isEmpty(results)) {
            view.matched = false;
            return view;
        }

        //提取唯一 activityId（不含 null）
        Set<Long> activityIds = results.stream()
                .map(ProductResult::getActivityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(activityIds)) {
            view.matched = false;
            return view;
        }

        if (activityIds.size() > 1) {
            log.error("==> [mergeHitActivity] 单商品暂不支持命中多种优惠活动， skuId={} hitActivityIds={}", sku.getSkuId(), activityIds);
            throw exception(ORDER_ACTIVITY_CAN_NOT_MATCH_MORE);
        }

        Long activityId = activityIds.iterator().next();
        view.activityId = activityId;

        //活动详情：找不到则降级不命中
        StoreSkuInfoDTO.ActivityBaseDetailDTO detail = this.findActivityDetail(sku, activityId);
        if (detail == null) {
            log.warn("==> [mergeHitActivity] activityDetail 丢失，直接降级处理为不命中活动, skuId={}, activityId={}",
                    sku.getSkuId(), activityId);
            view.matched = false;
            return view;
        }
        view.activityDetail = detail;

        List<ProductResult> sorted = new ArrayList<>(results);
//        sorted.sort(Comparator.comparing(pr -> pr.getActivityId() == null ? 0 : 1));

        view.buy = sorted.stream().filter(pr -> pr.getActivityId() == null).findFirst().orElse(null);
        view.gift = sorted.stream().filter(pr -> pr.getActivityId() != null).findFirst().orElse(null);

        //同步是否叠加优惠券
        if (view.buy != null && view.gift != null) {
            view.buy.setStackableActivities(view.gift.getStackableActivities());
        }

        //普通：gift!=null 且 buy==null 也可能是“只有一条活动记录”（不需要 buy），允许
        //买赠拆分：buy!=null 且 gift!=null
        //只有 buy 没 gift：说明 activityId 从哪来的？不可能（因为 activityIds 非空），属于脏数据 -> 降级
        if (view.buy != null && view.gift == null) {
            log.warn("==> [mergeHitActivity] 只有买，没有增，但是有activityId，不合理, skuId={}, activityId={}",
                    sku.getSkuId(), activityId);
            view.matched = false;
            return view;
        }

        view.matched = true;
        return view;
    }

    private StoreSkuInfoDTO.ActivityBaseDetailDTO findActivityDetail(StoreSkuInfoDTO sku, Long activityId) {
        Map<Long, StoreSkuInfoDTO.ActivityBaseDetailDTO> nj =
                Optional.ofNullable(sku.getActivityNjMap()).orElse(Collections.emptyMap());
        StoreSkuInfoDTO.ActivityBaseDetailDTO d = nj.get(activityId);
        if (d != null) return d;

        Map<Long, StoreSkuInfoDTO.ActivityBaseDetailDTO> mj =
                Optional.ofNullable(sku.getActivityMjMap()).orElse(Collections.emptyMap());
        return mj.get(activityId);
    }

    private void clearHitActivity(StoreSkuInfoDTO sku) {
        sku.setPromotionDiscountAmount(BigDecimal.ZERO);
        sku.setHitActivity(null);
        sku.setDiscountStackable(null);
        sku.setStackableActivitieList(List.of());
        sku.setIsGetActivity(0);
        sku.setGiftSku(null);
    }

    /**
     * 把视图应用回 sku（单次写入，避免 forEach 覆盖状态）
     */
    private void applyHitActivityView(StoreSkuInfoDTO sku, HitActivityView view) {
        if (view.isGift()) {
            ProductResult buy = view.buy;
            ProductResult gift = view.gift;

            sku.setCopies(buy.getQuantity());
            sku.setPromotionDiscountAmount(BigDecimal.ZERO);
            sku.setHitActivity(view.activityDetail);
            sku.setDiscountStackable(view.activityDetail.getDiscountStackable());
            sku.setStackableActivitieList(buy.getStackableActivities());
            sku.setIsGetActivity(0);

            StoreSkuInfoDTO giftSku = this.buildGiftSku(sku, gift, view.activityDetail);
            sku.setGiftSku(giftSku);
            return;
        }

        ProductResult gift = view.gift;
        sku.setPromotionDiscountAmount(this.toMoneyBigDecimal(gift.getDiscountAmount()));
        sku.setHitActivity(view.activityDetail);
        sku.setDiscountStackable(view.activityDetail.getDiscountStackable());
        sku.setStackableActivitieList(gift.getStackableActivities());
        sku.setIsGetActivity(1);
    }

    private StoreSkuInfoDTO buildGiftSku(StoreSkuInfoDTO base, ProductResult gift, StoreSkuInfoDTO.ActivityBaseDetailDTO detail) {
        StoreSkuInfoDTO giftSku = new StoreSkuInfoDTO();
        BeanUtils.copyProperties(base, giftSku);

        giftSku.setCopies(gift.getQuantity());
        giftSku.setPackageFee(BigDecimal.ZERO);
        giftSku.setPromotionDiscountAmount(this.toMoneyBigDecimal(gift.getDiscountAmount()));
        giftSku.setHitActivity(detail);
        giftSku.setDiscountStackable(detail.getDiscountStackable());
        giftSku.setStackableActivitieList(gift.getStackableActivities());
        giftSku.setIsGetActivity(1);
        giftSku.setGiftSku(null);

        return giftSku;
    }

    private BigDecimal toMoneyBigDecimal(Double discountAmount) {
        if (discountAmount == null) return BigDecimal.ZERO;
        // Double -> BigDecimal：使用 valueOf 避免 new BigDecimal(double) 的二进制误差扩大
        return new BigDecimal(discountAmount.toString());
    }

    private void validateSku(SettlementReqV2VO.CommodityInfoVO item, StoreSkuInfoDTO sku, boolean isDc) {
        this.validateCommodityMatch(item, sku);
        this.validateSkuOnSale(sku, isDc);
        this.validateMinBuy(item, sku);
        this.validateSaleRule(sku);
    }

    private void validateCommodityMatch(SettlementReqV2VO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        if (ObjectUtils.isEmpty(item.getCommodityId()) || !item.getCommodityId().equals(sku.getCommodityId())) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_ID_NOT_MATCH.getCode(),
                    String.format(ORDER_COMMODITY_ID_NOT_MATCH.getMsg(), sku.getSpuName())
            ));
        }
    }

    private void validateSkuOnSale(StoreSkuInfoDTO sku, boolean isDc) {
        boolean timeSharingDown = sku.getTimeSharingTopping() == 1 && !sku.getIsUp();
        boolean dcDown = isDc && sku.getCommodityStoreSpuMachineStatus() == 0;
        boolean wxDown = !isDc && sku.getCommodityStoreSpuAppletStatus() == 0;

        if (timeSharingDown || dcDown || wxDown) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_IS_NOT_UP.getCode(),
                    String.format(ORDER_COMMODITY_IS_NOT_UP.getMsg(), sku.getSpuName())
            ));
        }
    }

    private void validateMinBuy(SettlementReqV2VO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        Integer limit = Optional.ofNullable(sku.getLimitBuyNumber()).orElse(1);
        Integer copies = Optional.ofNullable(item.getCopies()).orElse(1);
        if (copies < limit) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_BUY_NUMBER_ERROR.getCode(),
                    String.format(ORDER_COMMODITY_BUY_NUMBER_ERROR.getMsg(), sku.getSpuName(), limit)
            ));
        }
    }

    private void validateSaleRule(StoreSkuInfoDTO sku) {
        Integer saleRule = Optional.ofNullable(sku.getSaleRule()).orElse(SaleRuleEnum.SALE_OK.getCode());
        if (!Objects.equals(saleRule, SaleRuleEnum.SALE_OK.getCode()) && !Objects.equals(saleRule, SaleRuleEnum.EXCHANGE_SALE_OK.getCode())) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_SALE_RULE_ERROR.getCode(),
                    String.format(ORDER_COMMODITY_SALE_RULE_ERROR.getMsg(),
                            sku.getSpuName(), SaleRuleEnum.getMessageByCode(sku.getSaleRule()))
            ));
        }
    }

    private void applySkus(SettlementReqV2VO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        //份数
        int copies = item.getCopies() == null ? 1 : item.getCopies();
        sku.setCopies(copies);
        sku.setIsPurchase(OrderConstants.NO);
        //计算包装费
        BigDecimal packageFee = NumberUtils.calculatePackagingFee(sku.getCopies(), sku.getManyCopy(), sku.getPackageFee());
        sku.setPackageFee(packageFee);
        sku.setSetmealType(ObjectUtils.isEmpty(item.getSetmealType()) ? SetmealTypeEnum.SINGLE.getCode() : item.getSetmealType());

        //取第一张图片
        sku.setImageUrl(
                com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.getFirstCommaElement(sku.getImageUrl())
        );
    }

    private void applySingles(SettlementReqV2VO.CommodityInfoVO item,
                              StoreSkuInfoDTO sku,
                              Map<Long, StoreSingleInfoDTO> singleMap,
                              boolean isDc) {
        List<SettlementReqV2VO.SingleFlavorVO> singleList = item.getSingleFlavorList();
        if (CollectionUtils.isEmpty(singleList)) return;

        BigDecimal totalUpPrice = BigDecimal.ZERO;
        List<StoreSingleInfoDTO> singles = new ArrayList<>();

        for (SettlementReqV2VO.SingleFlavorVO singleFlavor : singleList) {
            if (singleFlavor == null) continue;

            StoreSingleInfoDTO single = Optional.ofNullable(singleMap.get(singleFlavor.getSingleId()))
                    .orElseThrow(() -> exception(ORDER_COMMODITY_VALID_SINGLE));

            this.validateSingleUpStatus(single, isDc);
            this.validateInputFlavors(singleFlavor.getFlavors(), single.getCommodityFlavors(), sku.getSpuName(), single.getSpuName());

            StoreSingleInfoDTO copy = new StoreSingleInfoDTO();
            BeanUtils.copyProperties(single, copy);
            copy.setQty(singleFlavor.getNum());
            copy.setFlavors(BeanCopyUtils.copyBeanList(singleFlavor.getFlavors(), StoreSingleInfoDTO.FlavorInfoVO.class));
            singles.add(copy);

            totalUpPrice = totalUpPrice.add(single.getSinglePrice().multiply(BigDecimal.valueOf(singleFlavor.getNum())));
        }

        sku.setSkuPrice(sku.getSkuPrice().add(totalUpPrice));
        sku.setSingles(singles);
    }

    private void validateInputFlavors(List<SettlementReqV2VO.FlavorInfoVO> inputFlavors,
                                      List<CommodityFlavorDTO> dbFlavors,
                                      String spuName, String singleName) {
        if (inputFlavors == null || inputFlavors.isEmpty()) {
            return; // 没传属性就不校验，按你们业务决定（也可以改成必须传）
        }

        Map<String, Map<String, Integer>> shelfIndex = buildShelfIndex(dbFlavors);
        if (shelfIndex.isEmpty()) {
            throw exception(ORDER_COMMODITY_NOT_SELECT_ATTRIBUTE);
        }

        List<String> errors = new ArrayList<>();

        for (SettlementReqV2VO.FlavorInfoVO vo : inputFlavors) {
            if (vo == null) {
                errors.add("存在空的属性项");
                continue;
            }

            String name = vo.getName() == null ? null : vo.getName().trim();
            String value = vo.getValue() == null ? null : vo.getValue().trim();

            if (name == null || name.isEmpty() || value == null || value.isEmpty()) {
                errors.add("属性名/属性值不能为空");
                continue;
            }

            Map<String, Integer> valueStatusMap = shelfIndex.get(name);
            if (valueStatusMap == null) {
                errors.add("属性【" + name + "】不存在或未配置");
                continue;
            }

            Integer status = valueStatusMap.get(value);
            if (status == null) {
                // 可选：给出可选列表，方便前端/排查
                String options = valueStatusMap.keySet().stream()
                        .sorted()
                        .collect(Collectors.joining("、"));
                errors.add("属性【" + name + "】不包含值【" + value + "】，可选值：" + options);
                continue;
            }

            if (!Objects.equals(status, 1)) {
                errors.add("【" + spuName + "】【" + singleName + "】【" + name + ":" + value + "】已下架");
            }
        }

        if (!errors.isEmpty()) {
            // 合并抛出，避免一次只报一个、前端要多次试错
            throw exception(ORDER_COMMODITY_NOT_SELECT_ATTRIBUTE.getCode(), String.join("；", errors));
        }
    }

    private Map<String, Map<String, Integer>> buildShelfIndex(List<CommodityFlavorDTO> dbFlavors) {
        if (dbFlavors == null || dbFlavors.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, Integer>> index = new HashMap<>(dbFlavors.size() * 2);

        for (CommodityFlavorDTO dto : dbFlavors) {
            if (dto == null) continue;

            String name = safeTrim(dto.getFlavorName());
            if (name == null) continue;

            Map<String, Integer> valueStatus = index.computeIfAbsent(name, k -> new HashMap<>());

            List<Map<String, Integer>> listMap = dto.getFlavorValueListMap();
            if (listMap == null) continue;

            for (Map<String, Integer> m : listMap) {
                if (m == null || m.isEmpty()) continue;
                for (Map.Entry<String, Integer> e : m.entrySet()) {
                    String value = safeTrim(e.getKey());
                    if (value == null) continue;
                    // 默认：后写覆盖前写（如果你们数据可能重复，这样更稳）
                    valueStatus.put(value, e.getValue());
                }
            }
        }

        return index;
    }

    private static String safeTrim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void validateSingleUpStatus(StoreSingleInfoDTO single, boolean isDc) {
        if (isDc) {
            if (ObjectUtils.isEmpty(single.getStoreStatus()) || single.getStoreStatus() == 0) {
                throw exception(ORDER_COMMODITY_VALID_SINGLE);
            }
        } else {
            if (ObjectUtils.isEmpty(single.getWxStatus()) || single.getWxStatus() == 0) {
                throw exception(ORDER_COMMODITY_VALID_SINGLE);
            }
        }
    }

    private void mergeAfters(List<StoreSkuInfoDTO> skus, Map<Long, AfterInfoDTO> afters) {
        afters.values().forEach(after -> {
            StoreSkuInfoDTO sku = new StoreSkuInfoDTO();
            sku.setCommodityId(after.getCommodityId());
            sku.setSkuId(after.getSkuId());
            sku.setOriginalSkuId(after.getOriginalSkuId());
            sku.setSkuName(after.getSkuName());
            sku.setSpuId(after.getSpuId());
            sku.setSpuName(after.getSkuName());
            sku.setCopies(OrderConstants.ONE);
            sku.setIsPurchase(OrderConstants.YES);
            sku.setPromotionDiscountAmount(BigDecimal.ZERO);
            sku.setStrikeThroughPrice(after.getStrikePrice());
            sku.setAfterId(after.getAfterId());
            sku.setSkuPrice(after.getSkuPrice().subtract(after.getAfterPrice()));
            sku.setImageUrl(
                    com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.getFirstCommaElement(after.getImageUrl())
            );

            //计算包装费
            BigDecimal packageFee = NumberUtils.calculatePackagingFee(sku.getCopies(), after.getManyCopy(), after.getPackageFee());
            sku.setPackageFee(packageFee);
            skus.add(sku);

            log.debug("[AfterApplied] afterId? skuId={} price={} packFee={}", sku.getSkuId(), sku.getSkuPrice(), sku.getPackageFee());
        });
    }

    private void applyFlavors(SettlementReqV2VO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        List<SettlementReqV2VO.FlavorInfoVO> flavorList = item.getFlavorList();
        if (CollectionUtils.isEmpty(flavorList)) {
            return;
        }
        List<StoreSkuInfoDTO.FlavorInfoVO> flavorInfoVOS = BeanCopyUtils.copyBeanList(flavorList, StoreSkuInfoDTO.FlavorInfoVO.class);
        sku.setFlavorList(flavorInfoVOS);
    }

    private void applyCondiments(SettlementReqV2VO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        List<SettlementReqV2VO.CondimentInfoVO> selected = item.getCondimentsList();
        if (CollectionUtils.isEmpty(selected)) return;

        List<CondimentInfoDTO> condiments = this.parseCondimentsOrThrow(sku);

        this.validateCondimentSelectable(sku, selected.size());

        Map<Long, CondimentInfoDTO> condimentMap = condiments.stream()
                .collect(Collectors.toMap(CondimentInfoDTO::getCondimentId, Function.identity(), (a, b) -> a));

        int totalNumber = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<StoreSkuInfoDTO.CondimentInfoVO> condimentVOs = new ArrayList<>();

        for (SettlementReqV2VO.CondimentInfoVO req : selected) {
            CondimentInfoDTO condiment = this.valitAndGetCondiment(req, condimentMap);

            StoreSkuInfoDTO.CondimentInfoVO vo = new StoreSkuInfoDTO.CondimentInfoVO();
            BeanUtils.copyProperties(condiment, vo);
            vo.setId(condiment.getCondimentId());
            vo.setName(condiment.getCondimentName());
            vo.setNumber(req.getNumber());
            condimentVOs.add(vo);

            totalPrice = totalPrice.add(condiment.getPrice().multiply(BigDecimal.valueOf(req.getNumber())));
            totalNumber += req.getNumber();
        }

        this.validateCondimentTotalNumber(sku, totalNumber);

        sku.setSkuPrice(sku.getSkuPrice().add(totalPrice));
        sku.setCondimentsList(condimentVOs);
    }

    private CondimentInfoDTO valitAndGetCondiment(SettlementReqV2VO.CondimentInfoVO req, Map<Long, CondimentInfoDTO> condimentMap) {
        CondimentInfoDTO condiment = Optional.ofNullable(condimentMap.get(req.getId()))
                .orElseThrow(() -> new ServiceException(new ErrorCode(
                        ORDER_COMMODITY_CONDIMENT_ID_ERROR.getCode(),
                        String.format(ORDER_COMMODITY_CONDIMENT_ID_ERROR.getMsg(), req.getId())
                )));

        if (OrderConstants.NO.equals(condiment.getStatus())) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_CONDIMENT_DOWN.getCode(),
                    String.format(ORDER_COMMODITY_CONDIMENT_DOWN.getMsg(), condiment.getCondimentName())
            ));
        }

        if (condiment.getNumber() > 0 && req.getNumber() > condiment.getNumber()) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_CONDIMENT_BUY_NUMBER.getCode(),
                    String.format(ORDER_COMMODITY_CONDIMENT_BUY_NUMBER.getMsg(), condiment.getCondimentName(), condiment.getNumber())
            ));
        }
        return condiment;
    }

    private List<CondimentInfoDTO> parseCondimentsOrThrow(StoreSkuInfoDTO sku) {
        try {
            return JSON.parseArray(sku.getCondiments(), CondimentInfoDTO.class);
        } catch (Exception e) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_CONDIMENT_PARSE_ERROR.getCode(),
                    String.format(ORDER_COMMODITY_CONDIMENT_PARSE_ERROR.getMsg(), sku.getSpuName())
            ));
        }
    }

    private void validateCondimentSelectable(StoreSkuInfoDTO sku, int selectedCount) {
        if (ObjectUtils.isNotEmpty(sku.getCondimentIsMore())
                && sku.getCondimentIsMore() == 0
                && selectedCount > 1) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_CONDIMENT_COPIES_ERROR.getCode(),
                    String.format(ORDER_COMMODITY_CONDIMENT_COPIES_ERROR.getMsg(), sku.getSpuName())
            ));
        }
    }

    private void validateCondimentTotalNumber(StoreSkuInfoDTO sku, int totalNumber) {
        if (ObjectUtils.isNotEmpty(sku.getMaxCondimentNumber())
                && sku.getMaxCondimentNumber() > 0
                && totalNumber > sku.getMaxCondimentNumber()) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_CONDIMENT_UNMBER_ERROR.getCode(),
                    String.format(ORDER_COMMODITY_CONDIMENT_UNMBER_ERROR.getMsg(), sku.getSpuName(), sku.getMaxCondimentNumber())
            ));
        }
    }

    private <T> CompletableFuture<T> named(String name, CompletableFuture<T> f) {
        return f.whenComplete((r, ex) -> {
            if (ex != null) {
                log.error("[OrderCalc Async-Fail][{}]", name, ex);
            }
        });
    }

    /**
     * 加载门店数据
     */
    private CompletableFuture<StoreDTO> loadStore(Long storeId) {
        return CompletableFuture.supplyAsync(() -> storeApi.getStoreByStoreId(storeId).getCheckedData()
                        , strongExecutor)
                .exceptionally(ex -> {
                    log.error("==> [loadStore] | storeId:{}", storeId);
                    throw exception(ORDER_GET_STORE_FAIL);
                });
    }

    /**
     * 计算最优
     */
    private CompletableFuture<List<ProductResult>> computeDiscountAsync(List<StoreSkuInfoDTO> skus, Map<Long, List<ActivityNjnzDTO>> njnz, Map<Long, List<ActivityMJDTO>> mj) {
        return CompletableFuture.supplyAsync(() -> {
                    List<ProductItem> products = new ArrayList<>();

                    //最多可以承受的活动组合数
                    BigDecimal upperLimitActivityGroups = BigDecimal.ONE;

                    for (StoreSkuInfoDTO sku : skus) {
                        ProductItem product = new ProductItem();
                        List<MjmzActivity> mjmzActivities = new ArrayList<>();
                        List<NjnzActivity> njnzActivities = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(njnz)) {
                            List<ActivityNjnzDTO> njList = njnz.getOrDefault(sku.getCommodityId(), List.of());
                            njnzActivities = njList.stream()
                                    .map(dto -> {
                                        NjnzActivity activity = new NjnzActivity();
                                        BeanUtils.copyProperties(dto, activity);
                                        return activity;
                                    }).toList();
                            njList.forEach(dto -> {
                                StoreSkuInfoDTO.ActivityBaseDetailDTO activityBaseDetailDTO = new StoreSkuInfoDTO.ActivityBaseDetailDTO();
                                activityBaseDetailDTO.setActivityId(dto.getId());
                                activityBaseDetailDTO.setActivityName(dto.getActivityName());
                                activityBaseDetailDTO.setActivityTag(dto.getTag());
                                activityBaseDetailDTO.setActivityType(dto.getActivityType());
                                activityBaseDetailDTO.setDiscountStackable(dto.getDiscountStackable());
                                activityBaseDetailDTO.setDiscountType(dto.getDiscountType());
                                activityBaseDetailDTO.setDiscountItemNum(dto.getDiscountItemNum());
                                activityBaseDetailDTO.setDiscountRate(dto.getDiscountRate());

                                sku.getActivityNjMap().put(dto.getId(), activityBaseDetailDTO);
                            });
                        }
                        if (CollectionUtils.isNotEmpty(mj)) {
                            List<ActivityMJDTO> mjList = mj.getOrDefault(sku.getCommodityId(), List.of());
                            mjmzActivities = mjList.stream()
                                    .map(dto -> {
                                        MjmzActivity activity = new MjmzActivity();
                                        BeanUtils.copyProperties(dto, activity);
                                        activity.parseDiscountSettings(dto.getDiscountSettings());
                                        return activity;
                                    }).toList();
                            mjList.forEach(dto -> {
                                StoreSkuInfoDTO.ActivityBaseDetailDTO activityBaseDetailDTO = new StoreSkuInfoDTO.ActivityBaseDetailDTO();
                                activityBaseDetailDTO.setActivityId(dto.getId());
                                activityBaseDetailDTO.setActivityName(dto.getActivityName());
                                activityBaseDetailDTO.setActivityTag(dto.getTag());
                                activityBaseDetailDTO.setActivityType(dto.getActivityType());
                                activityBaseDetailDTO.setDiscountStackable(dto.getDiscountStackable());
                                activityBaseDetailDTO.setDiscountType(dto.getDiscountType());
                                activityBaseDetailDTO.setDiscountOffer(dto.getDiscountOffer());

                                sku.getActivityMjMap().put(dto.getId(), activityBaseDetailDTO);
                            });
                        }
                        product.setCommodityId(sku.getCommodityId());
                        product.setSkuId(sku.getSkuId());
                        product.setPrice(sku.getSkuPrice().doubleValue());
                        product.setMjmzActivities(mjmzActivities);
                        product.setNjnzActivities(njnzActivities);
                        product.setProductName(sku.getSpuName());
                        product.setQuantity(sku.getCopies());
                        product.setUId(sku.getUId());

                        if (CollectionUtils.isNotEmpty(mjmzActivities) || CollectionUtils.isNotEmpty(njnzActivities)) {
                            upperLimitActivityGroups = upperLimitActivityGroups.multiply(new BigDecimal(mjmzActivities.size() + njnzActivities.size()));
                        }

                        products.add(product);
                    }

                    //上限校验
                    if (upperLimitActivityGroups.compareTo(new BigDecimal(MAX_ACTIVITY_GROUPS)) > 0) {
                        throw exception(ORDER_ACTIVITY_TOO_MANY_COMMODITY);
                    }

//                    log.info("==> [findOptimalAllocation start] | products:{}", JSON.toJSONString(products));

                    AllocationResult allocationResult = activityAllocationService.findOptimalAllocation(products).orElseThrow(
                            () -> exception(ORDER_ACTIVITY_CLAC_ERROR)
                    );
//                    log.info("==> [findOptimalAllocation end] | allocationResult:{}", JSON.toJSONString(allocationResult));

                    List<ProductResult> productResults = activityAllocationService.calculateAllocation(allocationResult);
//                    log.info("==> [calculateAllocation end] | productResults:{}", JSON.toJSONString(productResults));
                    return productResults;
                }, strongExecutor)
                .exceptionally(ex -> {
                    log.error(" ==> [computeDiscountAsync] error={}", ex.toString());
                    throw exception(1_005_008_002, ex.getCause().getMessage());
                });

    }

    /**
     * 加载商品数据
     */
    private CompletableFuture<List<StoreSkuInfoDTO>> loadSkus(Set<Long> storeSkuIds) {
        return CompletableFuture.supplyAsync(() -> {
                    // 空集合直接返回空映射
                    if (CollectionUtils.isEmpty(storeSkuIds)) {
                        return Collections.<StoreSkuInfoDTO>emptyList();
                    }

                    // 调用商品服务API
                    CommonResult<List<StoreSkuInfoDTO>> result = commodityApi.getStoreSkuList(storeSkuIds);

                    // 获取并校验数据
                    return Optional.ofNullable(result.getCheckedData())
                            .orElseThrow(() -> exception(ORDER_COMMODITY_GET_FAIL));
                }, strongExecutor)
                .exceptionally(ex -> {
                    log.error("==> [loadSkus] | storeSkuIds:{}", storeSkuIds);
                    throw exception(ORDER_COMMODITY_GET_FAIL);
                });
    }

    /**
     * 加载套餐子项数据
     */
    private CompletableFuture<Map<Long, StoreSingleInfoDTO>> loadSingles(Set<Long> singleIds) {
        return CompletableFuture.supplyAsync(() -> {
                    if (CollectionUtils.isEmpty(singleIds)) {
                        return Collections.<Long, StoreSingleInfoDTO>emptyMap();
                    }

                    CommonResult<List<StoreSingleInfoDTO>> result = commodityApi.getStoreSingleList(singleIds);
                    List<StoreSingleInfoDTO> singleList = Optional.ofNullable(result.getCheckedData())
                            .orElseThrow(() -> exception(ORDER_GET_SINGLE_COMMODITY_FAIL));

                    return singleList.stream()
                            .collect(Collectors.toMap(
                                    StoreSingleInfoDTO::getSingleId,
                                    Function.identity(),
                                    (existing, replacement) -> existing
                            ));
                }, strongExecutor)
                .exceptionally(ex -> {
                    log.error("==> [loadSingles] | singleIds:{}", singleIds);
                    throw exception(ORDER_GET_SINGLE_COMMODITY_FAIL);
                });
    }

    /**
     * 加载加购数据
     */
    private CompletableFuture<Map<Long, AfterInfoDTO>> loadAfters(Set<Long> afterIds, Long storeId) {
        return CompletableFuture.supplyAsync(() -> {
                    if (CollectionUtils.isEmpty(afterIds)) {
                        return Collections.<Long, AfterInfoDTO>emptyMap();
                    }

                    CommonResult<List<AfterInfoDTO>> result = commodityApi.getAfterList(afterIds, storeId);

                    List<AfterInfoDTO> afterList = Optional.ofNullable(result.getCheckedData())
                            .orElseThrow(() -> exception(ORDER_GET_AFTER_COMMODITY_FAIL));

                    return afterList.stream()
                            .collect(Collectors.toMap(
                                    AfterInfoDTO::getAfterId,
                                    Function.identity(),
                                    (existing, replacement) -> existing
                            ));
                }, strongExecutor)
                .exceptionally(ex -> {
                    log.error("==> [loadAfters] | afterIds:{}", afterIds);
                    throw exception(ORDER_GET_AFTER_COMMODITY_FAIL);
                });
    }

    private CompletableFuture<SettlementActivitiesDTO> loadSettlementActivities(Set<Long> commodityIds,
                                                                                 Long storeId) {
        return CompletableFuture.supplyAsync(() -> {
            if (CollectionUtils.isEmpty(commodityIds)) {
                return emptySettlementActivities();
            }
            SettlementActivitiesDTO result = activityApi.selectSettlementActivities(storeId, commodityIds)
                    .getCheckedData();
            if (result == null) {
                throw exception(ORDER_GET_ACTIVITY_FAIL);
            }
            result.setNjnzActivities(Optional.ofNullable(result.getNjnzActivities()).orElseGet(Collections::emptyMap));
            result.setMjActivities(Optional.ofNullable(result.getMjActivities()).orElseGet(Collections::emptyMap));
            result.setMzActivities(Optional.ofNullable(result.getMzActivities()).orElseGet(Collections::emptyMap));
//            log.info("==> [loadSettlementActivities] | storeId={} commodityIdsSize={} njnzCommodityCount={} "
//                            + "mjCommodityCount={} mzCommodityCount={}", storeId, commodityIds.size(),
//                    result.getNjnzActivities().size(), result.getMjActivities().size(), result.getMzActivities().size());
            return result;
        }, strongExecutor).exceptionally(ex -> {
            log.warn("加载结算营销活动失败，降级为无营销活动 storeId={} commodityIdsSize={}",
                    storeId, commodityIds == null ? 0 : commodityIds.size(), ex);
            return emptySettlementActivities();
        });
    }

    private SettlementActivitiesDTO emptySettlementActivities() {
        SettlementActivitiesDTO result = new SettlementActivitiesDTO();
        result.setNjnzActivities(Collections.emptyMap());
        result.setMjActivities(Collections.emptyMap());
        result.setMzActivities(Collections.emptyMap());
        return result;
    }

    /**
     * 处理门店信息
     */
    private void processStoreInfo(CalculateCacheDataV2DTO cacheData, StoreDTO store) {
        //门店基本校验
        this.validateStoreIsOk(store);

        BeanUtils.copyProperties(store, cacheData);
        if (Objects.equals(cacheData.getOrderType(), OrderTypeEnum.ERRAND.getCode())) {
            cacheData.setDeliveryName(null);
            cacheData.setDeliveryPhone(null);
            BigDecimal subsidyAmount = store.getCampusDeliverySubsidy() == null ? BigDecimal.ZERO : store.getCampusDeliverySubsidy();
            cacheData.setErrandStoreSubsidyAmount(subsidyAmount);
            if (cacheData.getErrandRewardAmount() == null) {
                cacheData.setErrandRewardAmount(subsidyAmount.compareTo(BigDecimal.ZERO) > 0 ? subsidyAmount : DEFAULT_ERRAND_REWARD_AMOUNT);
            }
        }
        //0开启， 1关闭
        cacheData.setMiniproStatus(store.getOrderStoreType().equals(OrderConstants.NO) ? 0 : 1);
        cacheData.setExpensesList(BeanCopyUtils.copyBeanList(store.getExpensesList(), CalculateCacheDataV2DTO.StoreExpensesVO.class));
        cacheData.setPeakHours(this.buildPeakHourMap(store.getPeakHours(), store.getMealTime()));
    }

    private void convertToCommodityInfos(AsyncData data, CalculateCacheDataV2DTO cacheData) {
        cacheData.convertToCommodityInfos(data);
    }

    /**
     * 处理优惠券信息
     */
    public void processCouponInfo(CalculateCacheDataV2DTO cacheData) {
        if (ObjectUtils.isEmpty(cacheData.getUserCouponId())) {
            return;
        }

        GetReduceAmountReqVO reduceAmountReqVO = this.buildReduceAmountRequest(cacheData);
        CouponCalculateRespVO reduceAmountRspVO;
        try {
            reduceAmountRspVO = userCouponApi.getReduceAmount(reduceAmountReqVO);
            if (ObjectUtils.isEmpty(reduceAmountRspVO)) {
                throw exception(ORDER_COUPON_ERROR);
            }
        } catch (Exception e) {
            log.warn("==> 获取优惠券信息异常 | orderSn={} userCouponId={}", cacheData.getOrderSn(), cacheData.getUserCouponId(), e);
            return;
        }

        cacheData.setCouponId(reduceAmountRspVO.getCouponId());
        cacheData.setCouponName(reduceAmountRspVO.getCouponName());
        cacheData.setActivityDiscountAmount(reduceAmountRspVO.getReduceAmount());

        List<Long> commodityIds = reduceAmountRspVO.getCommodityIds();
        Map<Long, Integer> countMap = commodityIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> 1,
                        Integer::sum
                ));
        Map<Long, Integer> remainingCountMap = new HashMap<>(countMap);
        List<Integer> discountIndexes = IntStream.range(0, cacheData.getCommodityInfos().size())
                .filter(index -> canCommodityUseCoupon(cacheData.getCommodityInfos().get(index)))
                .filter(index -> commodityIds.contains(cacheData.getCommodityInfos().get(index).getCommodityId()))
                .boxed()
                .toList();
        int lastDiscountIndex = discountIndexes.isEmpty() ? -1 : discountIndexes.get(discountIndexes.size() - 1);
        BigDecimal allocatedDiscountAmount = BigDecimal.ZERO;
        //分摊
        for (int i = 0; i < cacheData.getCommodityInfos().size(); i++) {
            CalculateCacheDataV2DTO.CommodityInfoVO item = cacheData.getCommodityInfos().get(i);
            if (!canCommodityUseCoupon(item)) {
                continue;
            }
            Integer remainingCount = remainingCountMap.get(item.getCommodityId());
            if (remainingCount != null && remainingCount > 0) {
                item.setCouponId(reduceAmountRspVO.getCouponId());
                item.setUserCouponId(reduceAmountRspVO.getUserCouponId());
                item.setCouponName(reduceAmountRspVO.getCouponName());

                int currentRowCount = Math.min(item.getCopies(), remainingCount);
                BigDecimal currentDiscountAmount = reduceAmountRspVO.getMoney().multiply(BigDecimal.valueOf(currentRowCount));
                if (i == lastDiscountIndex) {
                    currentDiscountAmount = reduceAmountRspVO.getReduceAmount().subtract(allocatedDiscountAmount);
                }
                item.setActivityDiscountAmount(currentDiscountAmount);
                allocatedDiscountAmount = allocatedDiscountAmount.add(currentDiscountAmount);
                remainingCountMap.put(item.getCommodityId(), remainingCount - currentRowCount);
            }
        }

        //实际支付金额 = 商品金额 - 优惠券金额
        cacheData.setPayAmount(cacheData.getPayAmount().subtract(cacheData.getActivityDiscountAmount()));

        log.info("==> [processCouponInfo] | userCouponId={} couponId={} reduce={}",
                cacheData.getUserCouponId(), cacheData.getCouponId(), cacheData.getActivityDiscountAmount());
    }

    /**
     * 处理拼单优惠
     */
    private void processSplicing(CalculateCacheDataV2DTO cacheData) {
        if (!ObjectUtils.isEmpty(cacheData.getMainId())) {
            DiscountResultDTO discountResult = AmountUtil.calculateDiscount(cacheData.getPayAmount(), this.getSplicingOrderDiscount());
            //满减存储到优惠活动中
            cacheData.setPromotionDiscountAmount(discountResult.getDiscountAmount());
            cacheData.setPayAmount(discountResult.getFinalAmount());
        }
    }

    /**
     * 获取拼单优惠规则
     *
     * @return
     */
    private String getSplicingOrderDiscount() {
        CommonResult<Map<String, String>> commonResult = sysConfigApi.getBykeys(Collections.singletonList(OrderConstants.SPLICING_ORDER_CONFIG));
        Map<String, String> configMap = commonResult.getData();
        String configValueJson = configMap.get(OrderConstants.SPLICING_ORDER_CONFIG);
        if (ObjectUtils.isEmpty(configValueJson)) {
            return "0#0";
        }
        return JSON.parseObject(configValueJson, SplicingOrderConfigDTO.class).getDiscount();
    }

    /**
     * 获取优惠券请求参数
     */
    private GetReduceAmountReqVO buildReduceAmountRequest(CalculateCacheDataV2DTO cacheData) {
        GetReduceAmountReqVO reduceAmountReqVO = new GetReduceAmountReqVO();
        List<CalculateCacheDataCopyDTO.CommodityInfoVO> goodsList = new ArrayList<>();

        cacheData.getCommodityInfos().forEach(commodityInfoVO -> {
            if (!canCommodityUseCoupon(commodityInfoVO)) return;

            CalculateCacheDataCopyDTO.CommodityInfoVO commodityInfoCopyVO = new CalculateCacheDataCopyDTO.CommodityInfoVO();
            BeanUtils.copyProperties(commodityInfoVO, commodityInfoCopyVO);

            goodsList.add(commodityInfoCopyVO);
        });
        reduceAmountReqVO.setCommodityInfos(goodsList);
        reduceAmountReqVO.setStoreId(cacheData.getStoreId());
        reduceAmountReqVO.setUserId(cacheData.getMemberId());
        BigDecimal couponTransactionAmount = cacheData.getCommodityInfos().stream()
                .filter(this::canCommodityUseCoupon)
                .map(item -> item.getSkuPrice().multiply(BigDecimal.valueOf(item.getCopies()))
                        .subtract(Optional.ofNullable(item.getPromotionDiscountAmount()).orElse(BigDecimal.ZERO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        reduceAmountReqVO.setTransactionAmount(couponTransactionAmount);
        reduceAmountReqVO.setUserCouponId(cacheData.getUserCouponId());
        reduceAmountReqVO.setHabit(cacheData.getOrderType());
        return reduceAmountReqVO;
    }

    /**
     * 优惠券商品统一口径：赠品、加购品不参与；命中普通活动时，只有活动明确配置可叠加优惠券才参与。
     * 未命中普通活动的商品正常参与优惠券。
     */
    private boolean canCommodityUseCoupon(CalculateCacheDataV2DTO.CommodityInfoVO item) {
        if (item == null || OrderConstants.YES.equals(item.getIsGift())
                || OrderConstants.YES.equals(item.getIsPurchase())) {
            return false;
        }
        if (item.getDiscountStackable() == null) {
            return true;
        }
        return Integer.valueOf(1).equals(item.getDiscountStackable())
                && checkCanUseCoupon(item.getStackableActivities());
    }

    /**
     * 检查活动是否可叠加优惠券
     *
     * @param stackableActivities
     * @return boolean
     */
    private boolean checkCanUseCoupon(List<Integer> stackableActivities) {
        if (!CollectionUtils.isEmpty(stackableActivities)) {
            return stackableActivities.contains(1);
        }
        return false;
    }

    private static class HitActivityView {
        boolean matched;
        Long activityId;
        StoreSkuInfoDTO.ActivityBaseDetailDTO activityDetail;

        ProductResult buy;
        ProductResult gift;

        boolean isGift() {
            return buy != null && gift != null;
        }
    }
}
