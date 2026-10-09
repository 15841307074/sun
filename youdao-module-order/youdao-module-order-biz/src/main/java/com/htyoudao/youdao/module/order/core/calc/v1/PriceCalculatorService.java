package com.htyoudao.youdao.module.order.core.calc.v1;

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
import com.htyoudao.youdao.module.commodity.api.DTO.AfterInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.CondimentInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.commodity.enums.SaleRuleEnum;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.core.calc.CalculatorService;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.context.AsyncData;
import com.htyoudao.youdao.module.order.core.calc.context.IdCollections;
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
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
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
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_COMMODITY_ALL_NOT_ALLOW_SEND;

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
public class PriceCalculatorService extends CalculatorService {

    private static final long ASYNC_TIMEOUT_SECONDS = 20;
    //12个双活动品
    private static final long MAX_ACTIVITY_GROUPS = 4096;

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

    /**
     * 计算
     */
    public CalculateCacheDataDTO calculate(SettlementReqVO reqVO) throws Exception {
        //校验
        this.validateRequest(reqVO);
        //处理数据
        return this.processData(reqVO);
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(SettlementReqVO reqVO) {
        if (CollectionUtils.isEmpty(reqVO.getCommodityInfos())) {
            throw exception(ORDER_GET_CAR_COMMODITY_FAIL);
        }
    }

    /**
     * 处理数据
     */
    private CalculateCacheDataDTO processData(SettlementReqVO reqVO) {

        //初始化数据
        CalculateCacheDataDTO cacheData = this.initBaseData(reqVO);

        //收集所有相关ID集合
        IdCollections ids = this.collectIds(reqVO);

        //异步数据加载
        AsyncData data = this.loadAsyncData(ids, reqVO);

        //构建门店缓存信息
        this.processStoreInfo(cacheData, data.store());

        //合并活动信息
        this.mergeHitActivity(data.skus(), data.productResults());

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

        return cacheData;
    }

    /**
     * 初始化基本数据
     */
    private CalculateCacheDataDTO initBaseData(SettlementReqVO reqVO) {
        CalculateCacheDataDTO cacheDataDTO = new CalculateCacheDataDTO();
        cacheDataDTO.setOpenId(SecurityFrameworkUtils.getLoginOpenid());
        cacheDataDTO.setMemberName(SecurityFrameworkUtils.getLoginUsername());
        cacheDataDTO.setMemberId(SecurityFrameworkUtils.getLoginUserId());
        cacheDataDTO.setTakeAwayTel(SecurityFrameworkUtils.getLoginMobile());

        //初始化
        cacheDataDTO.setDeliveryFee(BigDecimal.ZERO);
        cacheDataDTO.setPayAmount(BigDecimal.ZERO);
        cacheDataDTO.setPackingFee(BigDecimal.ZERO);
        cacheDataDTO.setCommodityAmount(BigDecimal.ZERO);
        cacheDataDTO.setActivityDiscountAmount(BigDecimal.ZERO);
        cacheDataDTO.setMinimumDeliveryFeeIsOk(OrderConstants.YES);

        //拼单
        cacheDataDTO.setMainId(reqVO.getMainId());

        //订单类型 0堂食 1打包 2外卖
        cacheDataDTO.setOrderType(reqVO.getOrderType());
        cacheDataDTO.setIsDc(reqVO.getIsDc());

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
    private void checkMinimumDeliveryFee(CalculateCacheDataDTO cacheData) {
        //起送费校验
        if (OrderTypeEnum.TAKEAWAY.getCode() == cacheData.getOrderType() && ObjectUtils.isNotEmpty(cacheData.getMinimumDeliveryFee()) && cacheData.getMinimumDeliveryFee().compareTo(cacheData.getPayAmount().subtract(cacheData.getAfterAmount())) > 0) {
            cacheData.setMinimumDeliveryFeeIsOk(OrderConstants.NO);
        }
    }

    /**
     * 收集所有相关ID集合
     */
    private IdCollections collectIds(SettlementReqVO reqVO) {
        IdCollections ids = new IdCollections();
        ids.storeId = reqVO.getStoreId();

        reqVO.getCommodityInfos().forEach(commodity -> {
            if (ObjectUtils.isEmpty(commodity.getSkuId())) {
                throw exception(ORDER_COMMODITY_SKU_ID_ERROR);
            }

            //套餐
            if (commodity.getSetmealType() != SetmealTypeEnum.SINGLE.getCode() && CollectionUtils.isEmpty(commodity.getSingleList())) {
                throw exception(ORDER_PACKAGE_INFORMATION_INCOMPLETE);
            }

            //单品
            if (commodity.getSetmealType() == SetmealTypeEnum.SINGLE.getCode()) {
                commodity.setSingleList(Collections.EMPTY_LIST);
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

            for (Map<Long, Integer> m : commodity.getSingleList()) {
                if (m == null || m.isEmpty()) continue;
                ids.storeSingleIds.addAll(m.keySet());
            }

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
    private AsyncData loadAsyncData(IdCollections ids, SettlementReqVO reqVO) {

        // ===== 阶段1：并发拉取基础数据 =====
        CompletableFuture<StoreDTO> storeFuture =
                this.named("loadStore", this.loadStore(ids.storeId));

        // key: skuId
        CompletableFuture<List<StoreSkuInfoDTO>> skuFuture =
                this.named("loadSkus", this.loadSkus(ids.storeSkuIds));

        // key: singleId
        CompletableFuture<Map<Long, StoreSingleInfoDTO>> singleFuture =
                this.named("loadSingles", this.loadSingles(ids.storeSingleIds));

        // key: commodityId
        CompletableFuture<Map<Long, List<ActivityNjnzDTO>>> activityNJNZFuture =
                this.named("loadNJNZActivity", this.loadNJNZActivity(ids.commodityIds, ids.storeId));

        // key: commodityId
        CompletableFuture<Map<Long, List<ActivityMJDTO>>> activityMJFuture =
                this.named("loadMJActivity", this.loadMJActivity(ids.commodityIds, ids.storeId));

        // key: afterId
        CompletableFuture<Map<Long, AfterInfoDTO>> afterFuture =
                this.named("loadAfters", this.loadAfters(ids.afterIds, ids.storeId));

        // ===== 阶段2：sku + single 完成 => 整合 single, 小料, 属性 到 StoreSkuInfoDTO =====
        CompletableFuture<List<StoreSkuInfoDTO>> skuWithSinglesFuture =
                CompletableFuture.allOf(skuFuture, singleFuture)
                        .thenApply(v -> {
                            //合并sku及子集
                            return this.checkAndMergeSku(
                                    reqVO.getCommodityInfos(),
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
        return new AsyncData(storeFuture.join(), null, skuWithSinglesFuture.join(), afterFuture.join(), singleFuture.join(), discountFuture.join(), Map.of());
    }

    /**
     * 单点不送校验
     */
    private void noDeliveryCheck(SettlementReqVO reqVO, List<StoreSkuInfoDTO> skus) {
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
    private List<StoreSkuInfoDTO> checkAndMergeSku(List<SettlementReqVO.CommodityInfoVO> items,
                                                   List<StoreSkuInfoDTO> skus,
                                                   Map<Long, StoreSingleInfoDTO> singleMap,
                                                   SettlementReqVO reqVO) {
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
        for (SettlementReqVO.CommodityInfoVO item : items) {
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

    private void enrichSkuFromRequest(SettlementReqVO.CommodityInfoVO item,
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

    private void validateSku(SettlementReqVO.CommodityInfoVO item, StoreSkuInfoDTO sku, boolean isDc) {
        this.validateCommodityMatch(item, sku);
        this.validateSkuOnSale(sku, isDc);
        this.validateMinBuy(item, sku);
        this.validateSaleRule(sku);
    }

    private void validateCommodityMatch(SettlementReqVO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
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

    private void validateMinBuy(SettlementReqVO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
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
        if (!Objects.equals(saleRule, SaleRuleEnum.SALE_OK.getCode())) {
            throw new ServiceException(new ErrorCode(
                    ORDER_COMMODITY_SALE_RULE_ERROR.getCode(),
                    String.format(ORDER_COMMODITY_SALE_RULE_ERROR.getMsg(),
                            sku.getSpuName(), SaleRuleEnum.getMessageByCode(sku.getSaleRule()))
            ));
        }
    }

    private void applySkus(SettlementReqVO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
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

    private void applySingles(SettlementReqVO.CommodityInfoVO item,
                              StoreSkuInfoDTO sku,
                              Map<Long, StoreSingleInfoDTO> singleMap,
                              boolean isDc) {
        List<Map<Long, Integer>> singleList = item.getSingleList();
        if (CollectionUtils.isEmpty(singleList)) return;

        BigDecimal totalUpPrice = BigDecimal.ZERO;
        List<StoreSingleInfoDTO> singles = new ArrayList<>();

        for (Map<Long, Integer> map : singleList) {
            if (map == null || map.isEmpty()) continue;

            Map.Entry<Long, Integer> entry = map.entrySet().iterator().next();
            Long singleId = entry.getKey();
            Integer qty = entry.getValue();

            StoreSingleInfoDTO single = Optional.ofNullable(singleMap.get(singleId))
                    .orElseThrow(() -> exception(ORDER_COMMODITY_VALID_SINGLE));

            this.validateSingleUpStatus(single, isDc);

            StoreSingleInfoDTO copy = new StoreSingleInfoDTO();
            BeanUtils.copyProperties(single, copy);
            copy.setQty(qty);
            singles.add(copy);

            totalUpPrice = totalUpPrice.add(single.getSinglePrice().multiply(BigDecimal.valueOf(qty)));
        }

        sku.setSkuPrice(sku.getSkuPrice().add(totalUpPrice));
        sku.setSingles(singles);
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

    private void applyFlavors(SettlementReqVO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        List<SettlementReqVO.FlavorInfoVO> flavorList = item.getFlavorList();
        if (CollectionUtils.isEmpty(flavorList)) {
            return;
        }
        List<StoreSkuInfoDTO.FlavorInfoVO> flavorInfoVOS = BeanCopyUtils.copyBeanList(flavorList, StoreSkuInfoDTO.FlavorInfoVO.class);
        sku.setFlavorList(flavorInfoVOS);
    }

    private void applyCondiments(SettlementReqVO.CommodityInfoVO item, StoreSkuInfoDTO sku) {
        List<SettlementReqVO.CondimentInfoVO> selected = item.getCondimentsList();
        if (CollectionUtils.isEmpty(selected)) return;

        List<CondimentInfoDTO> condiments = this.parseCondimentsOrThrow(sku);

        this.validateCondimentSelectable(sku, selected.size());

        Map<Long, CondimentInfoDTO> condimentMap = condiments.stream()
                .collect(Collectors.toMap(CondimentInfoDTO::getCondimentId, Function.identity(), (a, b) -> a));

        int totalNumber = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<StoreSkuInfoDTO.CondimentInfoVO> condimentVOs = new ArrayList<>();

        for (SettlementReqVO.CondimentInfoVO req : selected) {
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

    private CondimentInfoDTO valitAndGetCondiment(SettlementReqVO.CondimentInfoVO req, Map<Long, CondimentInfoDTO> condimentMap) {
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
                    if(upperLimitActivityGroups.compareTo(new BigDecimal(MAX_ACTIVITY_GROUPS)) > 0){
                        throw exception(ORDER_ACTIVITY_TOO_MANY_COMMODITY);
                    }

                    log.info("==> [findOptimalAllocation start] | products:{}", JSON.toJSONString(products));

                    AllocationResult allocationResult = activityAllocationService.findOptimalAllocation(products).orElseThrow(
                            () -> exception(ORDER_ACTIVITY_CLAC_ERROR)
                    );
                    log.info("==> [findOptimalAllocation end] | allocationResult:{}", JSON.toJSONString(allocationResult));

                    List<ProductResult> productResults = activityAllocationService.calculateAllocation(allocationResult);
                    log.info("==> [calculateAllocation end] | productResults:{}", JSON.toJSONString(productResults));
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

    /**
     * 加载N件N折营销活动
     */
    private CompletableFuture<Map<Long, List<ActivityNjnzDTO>>> loadNJNZActivity(Set<Long> commodityIds, Long storeId) {
        return CompletableFuture.supplyAsync(() -> {
                    if (CollectionUtils.isEmpty(commodityIds)) {
                        return Collections.<Long, List<ActivityNjnzDTO>>emptyMap();
                    }

                    CommonResult<Map<Long, List<ActivityNjnzDTO>>> result = activityApi.selectNjnzActivity(storeId, commodityIds);
                    log.info("==> [selectNjnzActivity] | storeId={} commodityIdsSize={} result={}",
                            storeId, commodityIds.size(), JSON.toJSONString(result));
                    return Optional.ofNullable(result.getCheckedData())
                            .orElseThrow(() -> exception(ORDER_GET_ACTIVITY_FAIL));
                }, strongExecutor)
                .exceptionally(ex -> {
                    log.error("==> [loadNJNZActivity] | storeId={} commodityIdsSize={} cause={}",
                            storeId, commodityIds == null ? 0 : commodityIds.size(), ex.toString());
                    return Collections.emptyMap();
                });
    }

    /**
     * 加载满减满折营销活动
     */
    private CompletableFuture<Map<Long, List<ActivityMJDTO>>> loadMJActivity(Set<Long> commodityIds, Long storeId) {
        return CompletableFuture.supplyAsync(() -> {
                    if (CollectionUtils.isEmpty(commodityIds)) {
                        return Collections.<Long, List<ActivityMJDTO>>emptyMap();
                    }
                    CommonResult<Map<Long, List<ActivityMJDTO>>> result = activityApi.selectMJActivity(storeId, commodityIds);
                    log.info("==> [loadMJActivity] | storeId={} commodityIdsSize={} result={}",
                            storeId, commodityIds.size(), result);
                    return Optional.ofNullable(result.getCheckedData())
                            .orElseThrow(() -> exception(ORDER_GET_ACTIVITY_FAIL));
                }, strongExecutor)
                .exceptionally(ex -> {
                    log.error("==> [loadMJActivity] | storeId={} commodityIdsSize={} cause={}",
                            storeId, commodityIds == null ? 0 : commodityIds.size(), ex.toString());
                    return Collections.emptyMap();
                });
    }

    /**
     * 处理门店信息
     */
    private void processStoreInfo(CalculateCacheDataDTO cacheData, StoreDTO store) {
        //门店基本校验
        this.validateStoreIsOk(store);

        BeanUtils.copyProperties(store, cacheData);
        //0开启， 1关闭
        cacheData.setMiniproStatus(store.getOrderStoreType().equals(OrderConstants.NO) ? 0 : 1);
        cacheData.setExpensesList(BeanCopyUtils.copyBeanList(store.getExpensesList(), CalculateCacheDataDTO.StoreExpensesVO.class));
        cacheData.setPeakHours(this.buildPeakHourMap(store.getPeakHours(), store.getMealTime()));
    }

    private void convertToCommodityInfos(AsyncData data, CalculateCacheDataDTO cacheData) {
        cacheData.convertToCommodityInfos(data);
    }

    /**
     * 处理优惠券信息
     */
    public void processCouponInfo(CalculateCacheDataDTO cacheData) {
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
        //分摊
        cacheData.getCommodityInfos().forEach(item -> {
            Map<Long, Integer> countMap = commodityIds.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            e -> 1,
                            Integer::sum
                    ));
            if (commodityIds.contains(item.getCommodityId())) {
                item.setCouponId(reduceAmountRspVO.getCouponId());
                item.setUserCouponId(reduceAmountRspVO.getUserCouponId());
                item.setCouponName(reduceAmountRspVO.getCouponName());
                item.setActivityDiscountAmount(reduceAmountRspVO.getMoney().multiply(new BigDecimal(countMap.get(item.getCommodityId()))));
            }
        });

        //实际支付金额 = 商品金额 - 优惠券金额
        cacheData.setPayAmount(cacheData.getPayAmount().subtract(cacheData.getActivityDiscountAmount()));

        log.info("==> [processCouponInfo] | userCouponId={} couponId={} reduce={}",
                cacheData.getUserCouponId(), cacheData.getCouponId(), cacheData.getActivityDiscountAmount());
    }

    /**
     * 处理拼单优惠
     */
    private void processSplicing(CalculateCacheDataDTO cacheData) {
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
    private GetReduceAmountReqVO buildReduceAmountRequest(CalculateCacheDataDTO cacheData) {
        GetReduceAmountReqVO reduceAmountReqVO = new GetReduceAmountReqVO();
        List<CalculateCacheDataCopyDTO.CommodityInfoVO> goodsList = new ArrayList<>();

        cacheData.getCommodityInfos().forEach(commodityInfoVO -> {
            if (commodityInfoVO.getIsPurchase().equals(OrderConstants.YES)) {
                return;
            }

            if (!canCommodityUseCoupon(commodityInfoVO)) {
                return;
            }

            CalculateCacheDataCopyDTO.CommodityInfoVO commodityInfoCopyVO = new CalculateCacheDataCopyDTO.CommodityInfoVO();
            BeanUtils.copyProperties(commodityInfoVO, commodityInfoCopyVO);

            goodsList.add(commodityInfoCopyVO);
        });
        reduceAmountReqVO.setCommodityInfos(goodsList);
        reduceAmountReqVO.setStoreId(cacheData.getStoreId());
        reduceAmountReqVO.setUserId(cacheData.getMemberId());
        reduceAmountReqVO.setTransactionAmount(cacheData.getCommodityAmount());
        reduceAmountReqVO.setUserCouponId(cacheData.getUserCouponId());
        reduceAmountReqVO.setHabit(cacheData.getOrderType());
        return reduceAmountReqVO;
    }

    private boolean canCommodityUseCoupon(CalculateCacheDataDTO.CommodityInfoVO item) {
        if (item == null || OrderConstants.YES.equals(item.getIsPurchase())) {
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
