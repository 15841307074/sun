package com.htyoudao.youdao.module.order.core.calc.v1.DTO;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.DTO.ActivityDiscountDTO;
import com.htyoudao.youdao.module.order.core.calc.context.AsyncData;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.OrderTypeEnum;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.htyoudao.youdao.module.system.enums.StoreCalculationTypeEnum;
import com.htyoudao.youdao.module.system.enums.StoreExpensesTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * <p>
 * 订单提交缓存信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CalculateCacheDataDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5878434441566980077L;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 支付单号
     */
    private String paySn;

    /**
     * 用户ID
     */
    private Long memberId;

    /**
     * 买家name
     */
    private String memberName;

    /**
     * 顾客手机号
     */
    private String takeAwayTel;

    /**
     * 拼单ID
     */
    private String mainId;

    /**
     * openId
     */
    private String openId;

    /**
     * 订单类型 2外卖
     */
    private Integer orderType;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店是否支持不付款下单 （0支持 1 不支持）
     */
    private Integer storeWithoutPayment;

    /**
     * 小程序门店状态（0 正常营业 1  闭店）
     */
    private Integer miniproStatus;

    /**
     * 门店营业时间
     */
    private String storeHours;

    /**
     * 外卖时间
     */
    private String deliveryTime;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区
     */
    private String area;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店电话
     */
    private String storePhone;

    /**
     * 营业状态  0 正常营业  1 休息
     */
    private Integer openStatus;

    /**
     * 门店纬度
     */
    private Double storeLatitude;

    /**
     * 门店经度
     */
    private Double storeLongitude;

    /**
     * 配送员
     */
    private String deliveryName;

    /**
     * 配送员电话
     */
    private String deliveryPhone;

    /**
     * 起送费 重门店获取
     */
    private BigDecimal minimumDeliveryFee;

    /**
     * 起送费是否满足
     */
    private Integer minimumDeliveryFeeIsOk;

    /**
     * 用户优惠券ID
     */
    private Long userCouponId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券CODE
     */
    private String couponCode;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 商品金额
     */
    private BigDecimal commodityAmount;

    /**
     * 加购金额
     */
    private BigDecimal afterAmount;

    /**
     * 优惠金额
     */
    private BigDecimal activityDiscountAmount;

    /**
     * 优惠活动名称
     */
    private String activityName;

    /**
     * 优惠活动信息
     */
    List<ActivityDiscountDTO> activityDiscounts;

    /**
     * 优惠金额
     */
    private BigDecimal promotionDiscountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 打包费
     */
    private BigDecimal packingFee;

    /**
     * 配送费
     */
    private BigDecimal deliveryFee;

    /**
     * 是否点餐机
     */
    private Boolean isDc;

    /**
     * 商品数量
     */
    private Integer goodsCount;

    /**
     * 防重令牌
     */
    private String antiRepeatToken;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 峰时信息
     */
    private Map<String, Integer> peakHours;

    /**
     * 秒杀信息
     */
    private SeckillInfo seckillInfo = new SeckillInfo();

    @Data
    public static class SeckillInfo {

        /**
         * 场次ID
         */
        private Integer sessionId;

        /**
         * 活动ID
         */
        private Long activityId;

        /**
         * 活动名称
         */
        private String activityName;

        /**
         * 渠道
         */
        private String channel;
    }

    /**
     * 配送费/打包费 规则
     */
    private List<StoreExpensesVO> expensesList;

    /**
     * 商品信息集合
     */
    private List<CommodityInfoVO> commodityInfos;

    @Data
    public static class CommodityInfoVO implements Cloneable {

        /**
         * 商品SKU ID
         */
        private Long skuId;

        /**
         * 连锁库SKU ID
         */
        private Long originalSkuId;

        /**
         * 商品SPU ID
         */
        private Long spuId;

        /**
         * 连锁库SPU ID
         */
        private Long commodityId;

        /**
         * 商品SPU名称
         */
        private String spuName;

        /**
         * 商品SPU图片
         */
        private String imageUrl;

        /**
         * 商品SKU金额
         */
        private BigDecimal skuPrice;

        /**
         * 商品结算金额
         */
        private BigDecimal goodsShowPrice;

        /**
         * 秒杀价格
         */
        private BigDecimal seckillPrice;

        /**
         * 营销活动ID
         */
        private Long activityId;

        /**
         * 营销活动名称
         */
        private String activityName;

        /**
         * 营销活动类型
         */
        private Integer activityType;

        /**
         * 优惠券ID
         */
        private Long couponId;

        /**
         * 用户优惠券ID
         */
        private Long userCouponId;

        /**
         * 优惠券名称
         */
        private String couponName;

        /**
         * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
         */
        private Integer discountType;

        /**
         * 优惠第几件
         */
        private Integer discountItemNum;

        /**
         * 优惠打几折
         */
        private Double discountRate;

        /**
         * 优惠标签
         */
        private String activityTag;

        /**
         * 活动信息 JSON
         */
        private String activityDiscountDetail;

        /**
         * 优惠叠加（0不叠加 1叠加）
         */
        private Integer discountStackable;

        /**
         * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
         */
        private List<Integer> stackableActivities = new ArrayList<>();

        /**
         * 是否享受活动商品
         */
        private Integer isGetActivity = 0;

        /**
         * 营销活动优惠金额
         */
        private BigDecimal promotionDiscountAmount = BigDecimal.ZERO;

        /**
         * 优惠券优惠金额
         */
        private BigDecimal activityDiscountAmount = BigDecimal.ZERO;

        /**
         * 商品SKU划线价
         */
        private BigDecimal strikeThroughPrice;

        /**
         * 打包费
         */
        private BigDecimal packageFee = BigDecimal.ZERO;

        /**
         * 商品SKU名称
         */
        private String skuName;

        /**
         * 门店商品分类ID
         */
        private Long categoryId;

        /**
         * 门店商品分类名称
         */
        private String categoryName;

        /**
         * 商品份数
         */
        private Integer copies;

        /**
         * 套餐类型 1.固定搭配套餐，2.分组可选套餐, 3单品
         */
        private Integer setmealType;

        /**
         * 是否为加购商品 1.是 0.否
         */
        private Integer isPurchase;

        /**
         * 加购ID
         */
        private Long afterId;

        /**
         * 加购配置价格
         */
        private BigDecimal afterPrice;

        /**
         * 标签
         */
        private Integer tag;

        /**
         * 限购数量
         */
        private Integer limitBuyNumber;

        /**
         * 套餐子项集合，有套餐时必传
         */
        private List<SingleInfoVO> singleList = new ArrayList<>();

        /**
         * 商品属性集合
         */
        private List<FlavorInfoVO> flavorList = new ArrayList<>();

        /**
         * 商品小料集合
         */
        private List<CondimentInfoVO> condimentsList = new ArrayList<>();

        @Override
        public CommodityInfoVO clone() {
            try {
                return (CommodityInfoVO) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    @Data
    public static class StoreExpensesVO {
        /**
         * 费用类型 0 堂食/外带打包费 1 外卖打包费  2 外卖配送费
         */
        private Integer storeExpensesType;

        /**
         * 起送费/打包费限额
         */
        private BigDecimal minimumDeliveryFee;

        /**
         * 配送费/打包费
         */
        private BigDecimal additionaaCosts;

        /**
         * 费用计算方式  0 按商品  1 按订单  （ 费用类型 2 外卖配送费不考虑该字段）
         */
        private Integer storeCalculationType;
    }

    @Data
    public static class SingleInfoVO {
        private Long singleId;
        private Integer number;
        private Long spuId;
        private Long commodityId;
        private Long skuId;
        private Long originalSkuId;
        private String spuName;
        private String imageUrl;
        private BigDecimal singlePrice;
    }

    @Data
    public static class CondimentInfoVO {
        private Long id;
        private String name;
        private String imageUrl;
        private Integer number;
        private BigDecimal price;
    }

    @Data
    public static class FlavorInfoVO {
        private String name;
        private String value;
    }

    public void convertToCommodityInfos(AsyncData data) {
        BigDecimal commodityAmount = BigDecimal.ZERO;
        BigDecimal afterAmount = BigDecimal.ZERO;
        BigDecimal packageFee = BigDecimal.ZERO;
        BigDecimal promotionDiscountAmount = BigDecimal.ZERO;
        int goodsCount = 0;

        List<StoreSkuInfoDTO> skus = data.skus();
        List<CalculateCacheDataDTO.CommodityInfoVO> commodityInfoVOS = new ArrayList<>(skus.size());

        AtomicInteger atomicInteger = new AtomicInteger(1);
        for (StoreSkuInfoDTO sku : skus) {
            if (sku == null) {
                continue;
            }

            // ===== sku处理 =====
            int tag = atomicInteger.getAndIncrement();
            CommodityInfoVO vo = getVo(sku, tag);
            commodityInfoVOS.add(vo);
            //拆分sku处理
            if (ObjectUtils.isNotEmpty(sku.getGiftSku())) {
                CommodityInfoVO spliteVo = getVo(sku.getGiftSku(), tag);
                commodityInfoVOS.add(spliteVo);
            }
        }

        // 聚合后的活动优惠（key -> dto）
        Map<String, ActivityDiscountDTO> activityAggMap = new LinkedHashMap<>();
        for (CalculateCacheDataDTO.CommodityInfoVO sku : commodityInfoVOS) {
            // ===== 原有金额累加 =====
            commodityAmount = commodityAmount.add(
                    sku.getSkuPrice().multiply(BigDecimal.valueOf(sku.getCopies()))
            );

            packageFee = packageFee.add(sku.getPackageFee());
            promotionDiscountAmount = promotionDiscountAmount.add(sku.getPromotionDiscountAmount());

            if (OrderConstants.YES.equals(sku.getIsPurchase())) {
                afterAmount = afterAmount.add(sku.getSkuPrice());
            }
            goodsCount += sku.getCopies();

            // ===== 新增：按规则聚合活动优惠 =====
            this.aggregateActivityDiscount(sku, activityAggMap);
        }

        //保留两位
        activityAggMap.forEach((key, value) -> value.setPromotionDiscountAmount(value.getPromotionDiscountAmount().setScale(2, RoundingMode.DOWN)));

        // 设置聚合后的活动优惠列表（按插入顺序）
        setActivityDiscounts(new ArrayList<>(activityAggMap.values()));
        setGoodsCount(goodsCount);
        setCommodityAmount(commodityAmount);
        setAfterAmount(afterAmount);
        setPromotionDiscountAmount(promotionDiscountAmount.setScale(2, RoundingMode.DOWN));

        putFee(this, data.store(), packageFee);

        setPayAmount(
                commodityAmount.add(getPackingFee()).add(getDeliveryFee()).subtract(getPromotionDiscountAmount()).setScale(2, RoundingMode.HALF_UP)
        );
        this.setCommodityInfos(commodityInfoVOS);
    }

    private void aggregateActivityDiscount(CommodityInfoVO sku,
                                           Map<String, ActivityDiscountDTO> activityAggMap) {

        String activityDiscountDetail = sku.getActivityDiscountDetail();
        if (ObjectUtils.isEmpty(activityDiscountDetail)) {
            return;
        }

        StoreSkuInfoDTO.ActivityBaseDetailDTO activityInfo =
                JSON.parseObject(activityDiscountDetail, StoreSkuInfoDTO.ActivityBaseDetailDTO.class);
        if (activityInfo == null) {
            return;
        }

        String activityName = activityInfo.getActivityName();
        Long activityId = activityInfo.getActivityId();
        Integer activityType = activityInfo.getActivityType();
        Integer discountType = activityInfo.getDiscountType();
        Integer discountOffer = activityInfo.getDiscountOffer();

        BigDecimal discount = sku.getPromotionDiscountAmount();

        // 没活动 / 无优惠的直接跳过
        if (ObjectUtils.isEmpty(activityName)
                || activityId == null
                || activityType == null
                || discount == null
                || discount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        // ===== 按 规则生成聚合 key =====
        String key = activityId.toString();
//        if (Integer.valueOf(1).equals(activityType)) {
//            key = activityType + "|" + ns(discountType) + "|" + ns(discountOffer);
//        } else if (Integer.valueOf(5).equals(activityType)) {
//            key = activityType + "|" + ns(discountType);
//        } else {
//            // 其他活动：如果你希望也聚合，可按需扩展；这里默认按“活动名”聚合避免重复爆炸
//            key = activityType + "|" + ns(discountType) + "|" + ns(discountOffer) + "|" + activityName;
//        }

        ActivityDiscountDTO dto = activityAggMap.get(key);
        if (dto == null) {
            dto = new ActivityDiscountDTO();
            dto.setActivityType(activityType);
            dto.setDiscountType(discountType);
            dto.setDiscountOffer(discountOffer);
            dto.setActivityName(activityName);
            dto.setActivityTag(ActivityDiscountDTO.buildActivityTag(activityType, discountType,
                    activityInfo.getDiscountItemNum(), activityInfo.getDiscountRate(),
                    activityInfo.getActivityTag()));
            dto.setPromotionDiscountAmount(BigDecimal.ZERO);
            activityAggMap.put(key, dto);
        }

        dto.setPromotionDiscountAmount(dto.getPromotionDiscountAmount().add(discount));
    }

    private static String ns(Integer v) {
        return v == null ? "null" : String.valueOf(v);
    }

    private CalculateCacheDataDTO.CommodityInfoVO getVo(StoreSkuInfoDTO sku, Integer tag) {
        CalculateCacheDataDTO.CommodityInfoVO vo = new CalculateCacheDataDTO.CommodityInfoVO();
        // ===== 基础标识 =====
        vo.setSkuId(sku.getSkuId());
        vo.setOriginalSkuId(sku.getOriginalSkuId());
        vo.setSpuId(sku.getSpuId());
        vo.setCommodityId(sku.getCommodityId());
        vo.setAfterId(sku.getAfterId());
        vo.setTag(tag);

        // ===== 名称 & 图片 =====
        vo.setSkuName(sku.getSkuName());
        vo.setSpuName(sku.getSpuName());
        vo.setImageUrl(sku.getImageUrl());

        // ===== 数量 & 套餐 =====
        vo.setCopies(sku.getCopies());
        vo.setSetmealType(sku.getSetmealType());
        vo.setIsPurchase(sku.getIsPurchase());

        // ===== 价格相关 =====
        vo.setSkuPrice(sku.getSkuPrice());
        // 结算展示价
        vo.setGoodsShowPrice(sku.getSkuPrice().multiply(new BigDecimal(sku.getCopies())));
        vo.setStrikeThroughPrice(sku.getStrikeThroughPrice().multiply(new BigDecimal(sku.getCopies())));
        vo.setSeckillPrice(sku.getSeckillPrice());
        vo.setPackageFee(sku.getPackageFee());

        // ===== 优惠信息 =====
        vo.setActivityId(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getActivityId());
        vo.setActivityName(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getActivityName());
        vo.setActivityType(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getActivityType());
        vo.setDiscountType(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getDiscountType());
        vo.setDiscountRate(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getDiscountRate());
        vo.setDiscountItemNum(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getDiscountItemNum());
        vo.setActivityTag(
                sku.getHitActivity() == null ? null : sku.getHitActivity().getActivityTag());
        vo.setDiscountStackable(sku.getDiscountStackable());
        vo.setActivityDiscountDetail(
                sku.getHitActivity() == null ? null : JSON.toJSONString(sku.getHitActivity())
        );
        vo.setIsGetActivity(sku.getIsGetActivity());
        vo.setPromotionDiscountAmount(sku.getPromotionDiscountAmount());
        vo.setStackableActivities(sku.getStackableActivitieList());

        // ===== 分类信息 =====
        vo.setCategoryId(sku.getCategoryId());
        vo.setCategoryName(sku.getCategoryName());

        // ===== 子结构 =====
        //套餐子项
        vo.setSingleList(convertSingles(sku));
        //小料
        vo.setCondimentsList(convertCondiments(sku));
        //属性
        vo.setFlavorList(convertFlavors(sku));

        return vo;
    }

    private List<CalculateCacheDataDTO.FlavorInfoVO> convertFlavors(StoreSkuInfoDTO sku) {
        if (sku.getFlavorList() == null || sku.getFlavorList().isEmpty()) {
            return List.of();
        }

        List<CalculateCacheDataDTO.FlavorInfoVO> list =
                new ArrayList<>(sku.getFlavorList().size());

        for (StoreSkuInfoDTO.FlavorInfoVO flavor : sku.getFlavorList()) {
            CalculateCacheDataDTO.FlavorInfoVO vo =
                    new CalculateCacheDataDTO.FlavorInfoVO();

            vo.setName(flavor.getName());
            vo.setValue(flavor.getValue());

            list.add(vo);
        }

        return list;
    }

    private List<CalculateCacheDataDTO.SingleInfoVO> convertSingles(StoreSkuInfoDTO sku) {
        if (sku.getSingles() == null || sku.getSingles().isEmpty()) {
            return List.of();
        }

        List<CalculateCacheDataDTO.SingleInfoVO> list =
                new ArrayList<>(sku.getSingles().size());

        for (StoreSingleInfoDTO single : sku.getSingles()) {
            if (single == null) {
                continue;
            }

            CalculateCacheDataDTO.SingleInfoVO vo =
                    new CalculateCacheDataDTO.SingleInfoVO();

            vo.setSingleId(single.getSingleId());
            vo.setSkuId(single.getOriginalSkuId());
            vo.setOriginalSkuId(single.getOriginalSkuId());
            vo.setSpuId(single.getSpuId());
            vo.setCommodityId(single.getCommodityId());
            vo.setSpuName(single.getSpuName());
            vo.setImageUrl(single.getImageUrl());
            vo.setNumber(single.getQty());
            vo.setSinglePrice(single.getSinglePrice());

            list.add(vo);
        }

        return list;
    }

    private List<CalculateCacheDataDTO.CondimentInfoVO> convertCondiments(StoreSkuInfoDTO sku) {
        if (sku.getCondimentsList() == null || sku.getCondimentsList().isEmpty()) {
            return List.of();
        }

        List<CalculateCacheDataDTO.CondimentInfoVO> list =
                new ArrayList<>(sku.getCondimentsList().size());

        for (StoreSkuInfoDTO.CondimentInfoVO condiment : sku.getCondimentsList()) {
            CalculateCacheDataDTO.CondimentInfoVO vo =
                    new CalculateCacheDataDTO.CondimentInfoVO();

            vo.setId(condiment.getId());
            vo.setName(condiment.getName());
            vo.setImageUrl(condiment.getImageUrl());
            vo.setNumber(condiment.getNumber());
            vo.setPrice(condiment.getPrice());

            list.add(vo);
        }

        return list;
    }

    /**
     * 处理打包费/配送费
     */
    private void putFee(CalculateCacheDataDTO cacheData, StoreDTO store, BigDecimal packageFee) {
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
            cacheData.setMinimumDeliveryFee(td.getMinimumDeliveryFee());
            //配送费
            cacheData.setDeliveryFee(ObjectUtils.isNotEmpty(td) ? td.getAdditionaaCosts() : BigDecimal.ZERO);

        } else {
            cacheData.setDeliveryFee(BigDecimal.ZERO);
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

        log.debug("==> [putFee] | orderType={} packFee={} deliveryFee={} minimumDeliveryFee={}",
                cacheData.getOrderType(), cacheData.getPackingFee(), cacheData.getDeliveryFee(), cacheData.getMinimumDeliveryFee());
    }
}
