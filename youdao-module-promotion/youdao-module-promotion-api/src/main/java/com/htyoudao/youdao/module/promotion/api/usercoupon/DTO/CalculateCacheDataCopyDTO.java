package com.htyoudao.youdao.module.promotion.api.usercoupon.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;


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
public class CalculateCacheDataCopyDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1628636095019275801L;

//    /**
//     * 订单号
//     */
//    private String orderSn;
//
//    /**
//     * 支付单号
//     */
//    private String paySn;
//
//    /**
//     * 用户ID
//     */
//    private Long memberId;
//
//    /**
//     * 买家name
//     */
//    private String memberName;
//
//    /**
//     * 顾客手机号
//     */
//    private String takeAwayTel;
//
//    /**
//     * 拼单ID
//     */
//    private String mainId;
//
//    /**
//     * openId
//     */
//    private String openId;
//
//    /**
//     * 订单类型 2外卖   0堂食 1打包
//     */
//    private Integer orderType;
//
//    /**
//     * 门店ID
//     */
//    private Long storeId;
//
//    /**
//     * 门店是否支持不付款下单 （0支持 1 不支持）
//     */
//    private Integer storeWithoutPayment;
//
//    /**
//     * 小程序门店状态（0 正常营业 1  闭店）
//     */
//    private Integer miniproStatus;
//
//    /**
//     * 门店营业时间
//     */
//    private String storeHours;
//
//    /**
//     * 外卖时间
//     */
//    private String deliveryTime;
//
//    /**
//     * 省份
//     */
//    private String province;
//
//    /**
//     * 城市
//     */
//    private String city;
//
//    /**
//     * 区
//     */
//    private String area;
//
//    /**
//     * 门店名称
//     */
//    private String storeName;
//
//    /**
//     * 门店电话
//     */
//    private String storePhone;
//
//    /**
//     * 营业状态  0 正常营业  1 休息
//     */
//    private Integer openStatus;
//
//    /**
//     * 门店纬度
//     */
//    private Double storeLatitude;
//
//    /**
//     * 门店经度
//     */
//    private Double storeLongitude;
//
//    /**
//     * 配送员
//     */
//    private String deliveryName;
//
//    /**
//     * 配送员电话
//     */
//    private String deliveryPhone;
//
//    /**
//     * 起送费 重门店获取
//     */
//    private BigDecimal minimumDeliveryFee;
//
//    /**
//     * 起送费是否满足
//     */
//    private Integer minimumDeliveryFeeIsOk;
//
//    /**
//     * 用户优惠券ID
//     */
//    private Long userCouponId;
//
//    /**
//     * 优惠券ID
//     */
//    private Long couponId;
//
//    /**
//     * 优惠券CODE
//     */
//    private String couponCode;
//
//    /**
//     * 优惠券名称
//     */
//    private String couponName;
//
//    /**
//     * 商品金额
//     */
//    private BigDecimal commodityAmount;
//
//    /**
//     * 加购金额
//     */
//    private BigDecimal afterAmount;
//
//    /**
//     * 优惠金额
//     */
//    private BigDecimal activityDiscountAmount;
//
//    /**
//     * 优惠活动名称
//     */
//    private String activityName;
//
//    /**
//     * 优惠金额
//     */
//    private BigDecimal promotionDiscountAmount;
//
//    /**
//     * 实付金额
//     */
//    private BigDecimal payAmount;
//
//    /**
//     * 打包费
//     */
//    private BigDecimal packingFee;
//
//    /**
//     * 配送费
//     */
//    private BigDecimal deliveryFee;
//
//    /**
//     * 是否点餐机
//     */
//    private Boolean isDc;
//
//    /**
//     * 商品数量
//     */
//    private Integer goodsCount;
//
//    /**
//     * 防重令牌
//     */
//    private String antiRepeatToken;
//
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 峰时信息
//     */
//    private Map<String, Integer> peakHours;

    /**
     * 商品信息集合
     */
    private List<CommodityInfoVO> commodityInfos;

    @Data
    public static class CommodityInfoVO implements Cloneable, Serializable {

        @Serial
        private static final long serialVersionUID = -8620930391487154476L;
        /**
         * 商品份数
         */
        private Integer copies;

        /**
         * 商品SKU金额
         */
        private BigDecimal skuPrice;

        /**
         * 营销活动优惠金额
         */
        private BigDecimal promotionDiscountAmount = BigDecimal.ZERO;

        /**
         * 连锁库SPU ID
         */
        private Long commodityId;

        /**
         * 营销活动ID
         */
        private Long activityId;

        /**
         * 商品SPU ID
         */
        private Long spuId;

        /**
         * 是否为加购商品 1.是 0.否
         */
        private Integer isPurchase;

        /**
         * 是否赠品：1-是，0-否
         */
        private Integer isGift;

        /**
         * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
         */
        private List<Integer> stackableActivities = new ArrayList<>();

        /** 是否允许叠加其他优惠：null-未命中活动，0-否，1-是。 */
        private Integer discountStackable;

        // 下面的暂时没用

//        /**
//         * 商品SKU ID
//         */
//        private Long skuId;
//
//        /**
//         * 连锁库SKU ID
//         */
//        private Long originalSkuId;

//
//        /**
//         * 商品SPU名称
//         */
//        private String spuName;
//
//        /**
//         * 商品SPU图片
//         */
//        private String imageUrl;
//
//
//
//        /**
//         * 商品结算金额
//         */
//        private BigDecimal goodsShowPrice;
//
//        /**
//         * 秒杀价格
//         */
//        private BigDecimal seckillPrice;
//
//
//
//        /**
//         * 营销活动名称
//         */
//        private String activityName;
//
//        /**
//         * 营销活动类型
//         */
//        private Integer activityType;
//
//        /**
//         * 优惠券ID
//         */
//        private Long couponId;
//
//        /**
//         * 用户优惠券ID
//         */
//        private Long userCouponId;
//
//        /**
//         * 优惠券名称
//         */
//        private String couponName;
//
//        /**
//         * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
//         */
//        private Integer discountType;
//
//        /**
//         * 优惠第几件
//         */
//        private Integer discountItemNum;
//
//        /**
//         * 优惠打几折
//         */
//        private Double discountRate;
//
//        /**
//         * 优惠标签
//         */
//        private String activityTag;
//
//        /**
//         * 活动信息 JSON
//         */
//        private String activityDiscountDetail;
//
//        /**
//         * 优惠叠加（0不叠加 1叠加）
//         */
//        private Integer discountStackable = 1;
//
//
//
//        /**
//         * 是否享受活动商品
//         */
//        private Integer isGetActivity = 0;
//
//
//
//        /**
//         * 优惠券优惠金额
//         */
//        private BigDecimal activityDiscountAmount = BigDecimal.ZERO;
//
//        /**
//         * 商品SKU划线价
//         */
//        private BigDecimal strikeThroughPrice;
//
//        /**
//         * 打包费
//         */
//        private BigDecimal packageFee = BigDecimal.ZERO;
//
//        /**
//         * 商品SKU名称
//         */
//        private String skuName;
//
//        /**
//         * 门店商品分类ID
//         */
//        private Long categoryId;
//
//        /**
//         * 门店商品分类名称
//         */
//        private String categoryName;
//
//
//
//        /**
//         * 套餐类型 1.固定搭配套餐，2.分组可选套餐, 3单品
//         */
//        private Integer setmealType;
//
//
//
//        /**
//         * 加购ID
//         */
//        private Long afterId;
//
//        /**
//         * 加购配置价格
//         */
//        private BigDecimal afterPrice;
//
//        /**
//         * 标签
//         */
//        private Integer tag;
//
//        /**
//         * 限购数量
//         */
//        private Integer limitBuyNumber;
    }

}
