package com.htyoudao.youdao.module.commodity.api.DTO;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * SKU信息，包含部分SPU信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-29
 */
@Data
public class StoreSkuInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -953518147169052388L;

    private String uId;

    /**
     * 商品连锁库ID
     */
    private Long commodityId;

    /**
     * 门店分类ID
     */
    private Long categoryId;

    /**
     * 门店分类名称
     */
    private String categoryName;

    /**
     * 门店SPU ID
     */
    private Long spuId;

    /**
     * 门店SPU名称
     */
    private String spuName;

    /**
     * 门店SPU描述
     */
    private String spuDesc;

    /**
     * 门店SKU ID
     */
    private Long skuId;

    /**
     * 连锁库SKU ID
     */
    private Long originalSkuId;

    /**
     * 门店SKU名称
     */
    private String skuName;

    /**
     * 门店SKU价格
     */
    private BigDecimal skuPrice;

    /**
     * 门店SKU数量
     */
    private Integer copies;

    /**
     * 包装费
     */
    private BigDecimal packageFee;

    /**
     * 划线价
     */
    private BigDecimal strikeThroughPrice;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 套餐类型 1.固定搭配套餐，2.分组可选套餐, 3单品
     */
    private Integer setmealType;

    /**
     * 商品图片
     */
    private String imageUrl;

    /**
     * 打包份每份多少钱
     */
    private Integer manyCopy;

    /**
     * 小料
     */
    private String condiments;

    /**
     * 售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）
     */
    private Integer saleRule;

    /**
     * 限购数量
     */
    private Integer limitBuyNumber;

    /**
     * 小料是否多选 0否 1是
     */
    private Integer condimentIsMore;

    /**
     * 最多可加小料数量
     */
    private Integer maxCondimentNumber;

    /**
     * 门店下商品的点餐机状态
     */
    private Integer commodityStoreSpuMachineStatus;

    /**
     * 门店下商品的小程序状态
     */
    private Integer commodityStoreSpuAppletStatus;

    /**
     * 是否开启分时置顶 1是 0否
     */
    private Integer timeSharingTopping;

    //以下是非商品详情信息

    /**
     * 是否为加购商品 1.是 0.否
     */
    private Integer isPurchase;

    /** 是否满赠赠品：0否，1是。 */
    private Integer isGift = 0;

    /**
     * 架构ID
     */
    private Long afterId;

    /**
     * 是否满足分时条件
     */
    private Boolean isUp;

    /**
     * 活动ID 秒杀用
     */
    private Long activityId;

    /**
     * 是否享受活动商品
     */
    private Integer isGetActivity = 0;

    /**
     * 是否优惠叠加（0-否，1-是）
     */
    private Integer discountStackable;

    /**
     * 不可配送单品 0否 1是
     */
    private String noDeliveryForSingleOrder;

    /**
     * 优惠金额
     */
    private BigDecimal promotionDiscountAmount;

    /**
     * 命中活动信息
     */
    private ActivityBaseDetailDTO hitActivity;

    /**
     * 拆分SKU信息 (必定是带上活动的记录)
     */
    private StoreSkuInfoDTO giftSku;

    /**
     * 套餐子品信息
     */
    private List<StoreSingleInfoDTO> singles = new ArrayList<>();

    /**
     * 商品小料集合
     */
    private List<CondimentInfoVO> condimentsList = new ArrayList<>();

    /**
     * 商品属性集合
     */
    private List<FlavorInfoVO> flavorList = new ArrayList<>();

    /**
     * N件N折活动信息
     */
    private Map<Long, ActivityBaseDetailDTO> activityNjMap = new HashMap<>();

    /**
     * 满减满折活动信息
     */
    private Map<Long, ActivityBaseDetailDTO> activityMjMap = new HashMap<>();

    private List<Integer> stackableActivitieList = new ArrayList<>();

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

    @Data
    public static class ActivityBaseDetailDTO {
        private Long activityId;
        private Integer activityType;
        private Integer discountStackable;
        private String activityName;
        private String activityTag;
        private Integer discountType;
        private Integer discountOffer;
        private Integer discountItemNum;
        private Double discountRate;
        private List<Integer> stackableActivities = new ArrayList<>();
    }

    private ErrorCode errorCode;

}
