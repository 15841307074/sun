package com.htyoudao.youdao.module.order.core.calc;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.*;
import com.htyoudao.youdao.module.order.core.calc.DTO.CostInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.context.AsyncData;
import com.htyoudao.youdao.module.order.core.calc.context.CalculatorContext;
import com.htyoudao.youdao.module.order.core.calc.context.IdCollections;
import com.htyoudao.youdao.module.order.core.calc.factory.PriceCalculatorFactory;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.dal.dataobject.collection.NotNullHashSet;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import com.htyoudao.youdao.module.order.enums.SetmealTypeEnum;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.UserCouponApi;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.CouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.GetReduceAmountReqVO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.api.sysconfig.SysConfigApi;
import com.htyoudao.youdao.module.system.enums.StoreCalculationTypeEnum;
import com.htyoudao.youdao.module.system.enums.StoreExpensesTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class SeckillCalculatorService extends CalculatorService {

    private static final long ASYNC_TIMEOUT = 3;

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

    /**
     * 计算
     */
    public CalculateCacheDataV2DTO calculate(SettlementReqV2VO reqVO) throws Exception {
        //校验
        this.validateRequest(reqVO);

        //初始化数据
        CalculateCacheDataV2DTO cacheData = this.initBaseData(reqVO);

        //处理数据
        this.processData(reqVO, cacheData);
        return cacheData;
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(SettlementReqV2VO reqVO) {
        if (CollectionUtils.isEmpty(reqVO.getCommodityInfos())) {
            throw exception(ORDER_GET_CAR_COMMODITY_FAIL);
        }
        //社群校验
        activityApi.checkCanJoin(reqVO.getSeckillInfo().getActivityId()).checkError();
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

        //订单类型 0堂食 1打包 2外卖
        cacheDataDTO.setOrderType(reqVO.getOrderType());
        cacheDataDTO.setIsDc(reqVO.getIsDc());

        //优惠券
        cacheDataDTO.setUserCouponId(reqVO.getUserCouponId());

        if (ObjectUtils.isEmpty(cacheDataDTO.getTakeAwayTel())) {
            //下单电话
            cacheDataDTO.setTakeAwayTel(reqVO.getTakeAwayTel());
        }

        //秒杀信息
        cacheDataDTO.getSeckillInfo().setActivityId(reqVO.getSeckillInfo().getActivityId());
        cacheDataDTO.getSeckillInfo().setSessionId(reqVO.getSeckillInfo().getSessionId());
        cacheDataDTO.getSeckillInfo().setChannel(reqVO.getSeckillInfo().getChannel());
        cacheDataDTO.getSeckillInfo().setActivityName(reqVO.getSeckillInfo().getActivityName());

        return cacheDataDTO;
    }

    /**
     * 处理数据
     */
    private void processData(SettlementReqV2VO reqVO, CalculateCacheDataV2DTO cacheData) {

        //收集所有相关ID集合
        IdCollections ids = this.collectIds(reqVO);

        //异步数据加载
        AsyncData data = this.loadAsyncData(ids);

        //构建门店缓存信息
        this.processStoreInfo(cacheData, data.store());

        //构建商品缓存信息
        this.processCommodities(cacheData, reqVO.getCommodityInfos(), data);

        //构建优惠券信息
        this.processCouponInfo(cacheData);
    }

    /**
     * 收集所有相关ID集合
     */
    private IdCollections collectIds(SettlementReqV2VO reqVO) {
        IdCollections ids = new IdCollections();
        ids.storeId = reqVO.getStoreId();

        //秒杀相关
        ids.seckillActivityId = reqVO.getSeckillInfo().getActivityId();
        ids.sessionId = reqVO.getSeckillInfo().getSessionId();

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
            if (commodity.getIsPurchase().equals(OrderConstants.YES)) {
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
     * 异步数据加载
     */
    private AsyncData loadAsyncData(IdCollections ids) {
        CompletableFuture<StoreDTO> storeFuture = this.loadStore(ids.storeId);
        CompletableFuture<Map<Long, AfterInfoDTO>> afterFuture = this.loadAfters(ids.afterIds, ids.storeId);
        CompletableFuture<List<SeckillSpuDTO>> commodityFuture = this.loadCommodity(ids.storeId, ids.seckillActivityId, ids.sessionId);

        StoreDTO storeDTO;
        Map<Long, AfterInfoDTO> longAfterInfoDTOMap;
        Map<Long, StoreSkuInfoDTO> longStoreSkuInfoDTOMap = new HashMap<>();
        Map<Long, StoreSingleInfoDTO> longStoreSingleInfoDTOMap = new HashMap<>();

        try {
            CompletableFuture.allOf(storeFuture, afterFuture, commodityFuture).get(ASYNC_TIMEOUT, TimeUnit.SECONDS);

            storeDTO = storeFuture.get();
            this.validateStoreIsOk(storeDTO);

            longAfterInfoDTOMap = afterFuture.get();
            List<SeckillSpuDTO> seckillSpuDTOList = commodityFuture.get();

            this.splitCommodity(seckillSpuDTOList, longStoreSkuInfoDTOMap, longStoreSingleInfoDTOMap, ids);

        } catch (InterruptedException e) {
            log.error("[InterruptedException][线程中断]", e);
            throw exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        } catch (ExecutionException e) {
            log.error("[ExecutionException][任务异常]", e);
            throw exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        } catch (TimeoutException e) {
            log.error("[TimeoutException][线程超时]", e);
            throw exception(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
        }

        return new AsyncData(storeDTO, longStoreSkuInfoDTOMap, null, longAfterInfoDTOMap, longStoreSingleInfoDTOMap, null, Map.of());
    }

    /**
     * 拆分商品数据
     */
    private void splitCommodity(List<SeckillSpuDTO> seckillSpuDTOList, Map<Long, StoreSkuInfoDTO> longStoreSkuInfoDTOMap, Map<Long, StoreSingleInfoDTO> longStoreSingleInfoDTOMap, IdCollections ids) {
        NotNullHashSet<Long> storeSingleIds = ids.storeSingleIds;
        NotNullHashSet<Long> commodityIds = ids.commodityIds;

        //处理spu&sku
        seckillSpuDTOList.forEach(spu -> {
            if (commodityIds.contains(spu.getCommodityId())) {
                //多规格拦截
                if (CollectionUtils.isEmpty(spu.getSkuList()) || (CollectionUtils.isNotEmpty(spu.getSkuList()) && spu.getSkuList().size() > 1)) {
                    throw exception(ORDER_SECKILL_SKU_ERROR);
                }

                SkuDto sku = spu.getSkuList().get(0);
                StoreSkuInfoDTO storeSkuInfoDTO = new StoreSkuInfoDTO();

                BeanUtils.copyProperties(sku, storeSkuInfoDTO);
                BeanUtils.copyProperties(spu, storeSkuInfoDTO);

                //特殊值处理
                storeSkuInfoDTO.setCommodityStoreSpuAppletStatus(spu.getCommodityStoreSpuAppletStatus());
                storeSkuInfoDTO.setCommodityStoreSpuMachineStatus(spu.getCommodityStoreSpuMachineStatus());
                storeSkuInfoDTO.setImageUrl(CollectionUtils.isNotEmpty(spu.getBannerList()) ? spu.getBannerList().get(0) : Strings.EMPTY);
                storeSkuInfoDTO.setTimeSharingTopping(OrderConstants.NO);
                storeSkuInfoDTO.setLimitBuyNumber(ObjectUtils.isEmpty(spu.getLimitPerItem()) ? 0 : spu.getLimitPerItem());

                //秒杀划线价格
                storeSkuInfoDTO.setStrikeThroughPrice(spu.getSpuUnderlinedPrice());
                //秒杀价格
                storeSkuInfoDTO.setSeckillPrice(spu.getSeckillPrice());
                //原价格 取spu价格
                storeSkuInfoDTO.setSkuPrice(spu.getSpuPrice());
                storeSkuInfoDTO.setActivityId(ids.seckillActivityId);

                List<CondimentInfoDTO> condimentInfoDTOS = new ArrayList<>();
                spu.getCommodityCondiments().forEach(i -> {
                    CondimentInfoDTO condimentInfoDTO = new CondimentInfoDTO();
                    BeanUtils.copyProperties(i, condimentInfoDTO);
                    condimentInfoDTO.setPrice(i.getCondimentPrice());
                    condimentInfoDTOS.add(condimentInfoDTO);
                });
                storeSkuInfoDTO.setCondiments(JSON.toJSONString(condimentInfoDTOS));

                longStoreSkuInfoDTOMap.put(sku.getSkuId(), storeSkuInfoDTO);
            }

            //处理分组商品
            if (CollectionUtils.isNotEmpty(spu.getGroupList())) {
                List<SingleDto> allSingles = spu.getGroupList().stream()
                        .filter(f -> f.getSingleList() != null)
                        .flatMap(f -> f.getSingleList().stream())
                        .toList();
                Map<Long, SingleDto> singleMap = allSingles.stream().collect(Collectors.toMap(SingleDto::getSingleId, r -> r, (k1, k2) -> k1));

                storeSingleIds.forEach(singleId -> {
                    if (!singleMap.containsKey(singleId)) {
                        return;
                    }
                    SingleDto singleDto = singleMap.get(singleId);
                    StoreSingleInfoDTO storeSingleInfoDTO = new StoreSingleInfoDTO();
                    BeanUtils.copyProperties(singleDto, storeSingleInfoDTO);

                    //特殊值处理
                    storeSingleInfoDTO.setSpuName(singleDto.getSingleName());
                    storeSingleInfoDTO.setSinglePrice(singleDto.getUpPrice());
                    storeSingleInfoDTO.setSpuId(singleDto.getCommodityStoreSpuId());

                    longStoreSingleInfoDTOMap.put(singleDto.getSingleId(), storeSingleInfoDTO);
                });
            }
        });
    }

    /**
     * 加载商品数据
     */
    private CompletableFuture<List<SeckillSpuDTO>> loadCommodity(Long storeId, Long activityId, Integer sessionId) {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            return commodityApi.seckillCommodityByActivityId(storeId, activityId, sessionId).getCheckedData();
        }, strongExecutor).exceptionally(ex -> {
            log.error("秒杀商品数据加载失败 | activityId:{}", activityId, ex);
            throw exception(ORDER_GET_SECKILL_ACTIVITY_FAIL);
        });
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
     * 加载门店数据
     */
    private CompletableFuture<StoreDTO> loadStore(Long storeId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            return storeApi.getStoreByStoreId(storeId).getCheckedData();
        }, strongExecutor).exceptionally(ex -> {
            log.error("门店数据加载失败 | storeId:{}", storeId, ex);
            throw exception(ORDER_GET_STORE_FAIL);
        });
    }

    /**
     * 加载加购数据
     */
    private CompletableFuture<Map<Long, AfterInfoDTO>> loadAfters(Set<Long> afterIds, Long storeId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            if (CollectionUtils.isEmpty(afterIds)) {
                return Collections.<Long, AfterInfoDTO>emptyMap();
            }

            CommonResult<List<AfterInfoDTO>> result = commodityApi.getAfterList(afterIds, storeId);

            List<AfterInfoDTO> afterList = Optional.ofNullable(result.getCheckedData()).orElseThrow(() -> exception(ORDER_GET_AFTER_COMMODITY_FAIL));

            return afterList.stream().collect(Collectors.toMap(AfterInfoDTO::getAfterId, Function.identity(), (existing, replacement) -> existing));
        }, strongExecutor).exceptionally(ex -> {
            log.error("加购数据加载失败 | afterIds:{}", afterIds, ex);
            throw exception(ORDER_GET_AFTER_COMMODITY_FAIL);
        });
    }

    /**
     * 处理门店信息
     */
    private void processStoreInfo(CalculateCacheDataV2DTO cacheData, StoreDTO store) {
        BeanUtils.copyProperties(store, cacheData);
        //0开启， 1关闭
        cacheData.setMiniproStatus(store.getOrderStoreType().equals(OrderConstants.NO) ? 0 : 1);
        cacheData.setExpensesList(BeanCopyUtils.copyBeanList(store.getExpensesList(), CalculateCacheDataV2DTO.StoreExpensesVO.class));
        cacheData.setPeakHours(this.buildPeakHourMap(store.getPeakHours(), store.getMealTime()));
    }

    /**
     * 处理商品信息
     */
    private void processCommodities(CalculateCacheDataV2DTO cacheData, List<SettlementReqV2VO.CommodityInfoVO> commodities, AsyncData data) {
        BigDecimal commodityAmount = BigDecimal.ZERO, packageFee = BigDecimal.ZERO, promotionDiscountAmount = BigDecimal.ZERO;
        Integer goodsCount = 0;
        List<CalculateCacheDataV2DTO.CommodityInfoVO> cacheCommodities = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger(1);

        Collection<StoreSkuInfoDTO> values = data.skuMap().values();
        boolean allNoDelivery = !values.isEmpty() &&
                values.stream().allMatch(sku -> sku.getNoDeliveryForSingleOrder() != null && sku.getNoDeliveryForSingleOrder().equals("1"));
        if (allNoDelivery && cacheData.getOrderType().equals(OrderTypeEnum.TAKEAWAY.getCode())) {
            throw exception(ORDER_COMMODITY_ALL_NOT_ALLOW_SEND);
        }

        for (SettlementReqV2VO.CommodityInfoVO commodity : commodities) {
            //执行计算
            CostInfoDTO costInfo = PriceCalculatorFactory.getCalculator(commodity).calculate(new CalculatorContext(commodity, data, cacheData.getIsDc(), cacheData.getSeckillInfo()));
            //商品金额
            commodityAmount = commodityAmount.add(costInfo.getCommodityAmount());
            //包装费累加
            packageFee = packageFee.add(costInfo.getPackingFee());
            //活动优惠金额累加
            promotionDiscountAmount = promotionDiscountAmount.add(costInfo.getPromotionDiscountAmount());
            //商品数量累加
            goodsCount += commodity.getCopies();
            //商品信息累加
            cacheCommodities.addAll(this.putTag(costInfo, atomicInteger));
        }

        cacheData.setGoodsCount(goodsCount);
        cacheData.setCommodityAmount(commodityAmount);
        cacheData.setPromotionDiscountAmount(promotionDiscountAmount.max(BigDecimal.ZERO));
        cacheData.setActivityDiscounts(new ArrayList<>());

        this.putFee(cacheData, data, packageFee);

        cacheData.setPayAmount(commodityAmount.add(cacheData.getPackingFee()).add(cacheData.getDeliveryFee()).subtract(cacheData.getPromotionDiscountAmount()));
        cacheData.setCommodityInfos(cacheCommodities);
    }

    /**
     * 打自增标签
     *
     * @param costInfo
     * @param atomicInteger
     * @return
     */
    private List<CalculateCacheDataV2DTO.CommodityInfoVO> putTag(CostInfoDTO costInfo, AtomicInteger atomicInteger) {
        List<CalculateCacheDataV2DTO.CommodityInfoVO> cacheVOList = costInfo.getCacheVOList();
        int andIncrement = atomicInteger.getAndIncrement();
        cacheVOList.forEach(i -> i.setTag(andIncrement));
        return cacheVOList;
    }

    /**
     * 处理打包费/配送费
     */
    private void putFee(CalculateCacheDataV2DTO cacheData, AsyncData data, BigDecimal packageFee) {
        StoreDTO store = data.store();
        List<StoreDTO.StoreExpensesVO> expensesList = store.getExpensesList();
        Map<Integer, StoreDTO.StoreExpensesVO> expensesVOMap = expensesList.stream().collect(Collectors.toMap(StoreDTO.StoreExpensesVO::getStoreExpensesType, r -> r, (existing, replacement) -> existing));

        if (cacheData.getOrderType().equals(OrderTypeEnum.TAKEAWAY.getCode())) {
            //外卖打包
            StoreDTO.StoreExpensesVO tp = expensesVOMap.get(StoreExpensesTypeEnum.TAKEAWAY_PACKAGE.getCode());
            //按商品
            if (ObjectUtils.isNotEmpty(tp) && tp.getStoreCalculationType() == StoreCalculationTypeEnum.COMMODITY.getCode()) {
                //打包费上限校验
                cacheData.setPackingFee(packageFee.compareTo(tp.getMinimumDeliveryFee()) > 0 ? tp.getMinimumDeliveryFee() : packageFee);
            }
            //按订单
            if (ObjectUtils.isNotEmpty(tp) && tp.getStoreCalculationType() == StoreCalculationTypeEnum.ORDER.getCode()) {
                cacheData.setPackingFee(tp.getAdditionaaCosts());
            }

            //外卖配送
            StoreDTO.StoreExpensesVO td = expensesVOMap.get(StoreExpensesTypeEnum.TAKEAWAY_DELIVERY.getCode());

            //起送费校验
            if (ObjectUtils.isNotEmpty(td.getMinimumDeliveryFee()) && td.getMinimumDeliveryFee().compareTo(cacheData.getCommodityAmount()) > 0) {
                throw new ServiceException(new ErrorCode(ORDER_AMOUNT_NOT_ENOUGH.getCode(), String.format(ORDER_AMOUNT_NOT_ENOUGH.getMsg(), td.getMinimumDeliveryFee())));
            }

            //配送费
            cacheData.setDeliveryFee(ObjectUtils.isNotEmpty(td) ? td.getAdditionaaCosts() : BigDecimal.ZERO);
        } else {
            //堂食打包
            StoreDTO.StoreExpensesVO tsp = expensesVOMap.get(StoreExpensesTypeEnum.CANTEEN_FOOD.getCode());
            //按商品
            if (tsp.getStoreCalculationType() == StoreCalculationTypeEnum.COMMODITY.getCode()) {
                //打包费上限校验
                cacheData.setPackingFee(ObjectUtils.isNotEmpty(tsp) ? packageFee.compareTo(tsp.getMinimumDeliveryFee()) > 0 ? tsp.getMinimumDeliveryFee() : packageFee : BigDecimal.ZERO);
            }
            //按订单
            if (tsp.getStoreCalculationType() == StoreCalculationTypeEnum.ORDER.getCode()) {
                cacheData.setPackingFee(tsp.getAdditionaaCosts());
            }
        }
    }

    /**
     * 处理优惠券信息
     */
    public void processCouponInfo(CalculateCacheDataV2DTO cacheData) {
        if (ObjectUtils.isEmpty(cacheData.getUserCouponId())) {
            return;
        }

        GetReduceAmountReqVO reduceAmountReqVO = this.getGetReduceAmountReqVO(cacheData);
        CouponCalculateRespVO reduceAmountRspVO;
        try {
            reduceAmountRspVO = userCouponApi.getReduceAmount(reduceAmountReqVO);
            if (ObjectUtils.isEmpty(reduceAmountRspVO)) {
                throw exception(ORDER_COUPON_ERROR);
            }
        } catch (Exception e) {
            log.warn("==> 获取优惠券信息异常 | orderSn={} userCouponId={}", cacheData.getOrderSn(), cacheData.getUserCouponId());
            return;
        }

        cacheData.setCouponId(reduceAmountRspVO.getId());
        cacheData.setCouponName(reduceAmountRspVO.getCouponName());
//        cacheData.setCouponCode(reduceAmountRspVO.getCouponCode());
        cacheData.setActivityDiscountAmount(reduceAmountRspVO.getReduceAmount());

        List<Long> commodityIds = reduceAmountRspVO.getCommodityIds();
        //分摊
        cacheData.getCommodityInfos().forEach(item -> {
            if (this.checkCanUseCoupon(item.getStackableActivities()) && commodityIds.contains(item.getCommodityId())) {
                item.setCouponId(reduceAmountRspVO.getCouponId());
                item.setUserCouponId(reduceAmountRspVO.getUserCouponId());
                item.setCouponName(reduceAmountRspVO.getCouponName());
                item.setActivityDiscountAmount(reduceAmountRspVO.getMoney().multiply(new BigDecimal(item.getCopies())));
            }
        });

        //实际支付金额 = 商品金额 - 优惠券金额
        cacheData.setPayAmount(cacheData.getPayAmount().subtract(cacheData.getActivityDiscountAmount()));
    }

    /**
     * 获取优惠券请求参数
     */
    private GetReduceAmountReqVO getGetReduceAmountReqVO(CalculateCacheDataV2DTO cacheData) {
        GetReduceAmountReqVO reduceAmountReqVO = new GetReduceAmountReqVO();
        List<GetReduceAmountReqVO.OrderGoods> goodsList = new ArrayList<>();

        cacheData.getCommodityInfos().forEach(commodityInfoVO -> {
            if (commodityInfoVO.getIsPurchase().equals(OrderConstants.YES)) {
                return;
            }

            if (!ObjectUtils.isEmpty(commodityInfoVO.getActivityId()) && !this.checkCanUseCoupon(commodityInfoVO.getStackableActivities())) {
                return;
            }

            for (int i = 0; i < commodityInfoVO.getCopies(); i++) {
                GetReduceAmountReqVO.OrderGoods orderGoods = new GetReduceAmountReqVO.OrderGoods();
                orderGoods.setCommodityId(commodityInfoVO.getCommodityId());
                orderGoods.setSkuId(commodityInfoVO.getSkuId());
                orderGoods.setTransactionAmount(commodityInfoVO.getSeckillPrice().divide(new BigDecimal(commodityInfoVO.getCopies())).setScale(2, RoundingMode.HALF_UP));
                goodsList.add(orderGoods);
            }
        });
        reduceAmountReqVO.setGoodsList(goodsList);
        reduceAmountReqVO.setStoreId(cacheData.getStoreId());
        reduceAmountReqVO.setUserId(cacheData.getMemberId());
        reduceAmountReqVO.setTransactionAmount(cacheData.getCommodityAmount());
        reduceAmountReqVO.setUserCouponId(cacheData.getUserCouponId());
        reduceAmountReqVO.setHabit(cacheData.getOrderType());
        return reduceAmountReqVO;
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
}
