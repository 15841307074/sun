package com.htyoudao.youdao.module.commodity.dal.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *  SKU信息，包含部分SPU信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-29
 */
@Data
public class StoreSkuDTO extends TimeBaseDTO {

    @Serial
    private static final long serialVersionUID = -953518147169052388L;

    /**
     * 连锁库ID
     */
    private Long commodityId;

    /**
     * 门店分类ID
     */
    private Long categoryId;

    /**
     * 菜单分类名称
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
     * 划线价
     */
    private BigDecimal strikeThroughPrice;

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
     * 打包费
     */
    private BigDecimal packageFee;

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
     * 不可配送单品 0否 1是
     */
    private String noDeliveryForSingleOrder;

    //以下是非商品详情信息

    /**
     * 是否为加购商品 1.是 0.否
     */
    private Integer isPurchase;
}
