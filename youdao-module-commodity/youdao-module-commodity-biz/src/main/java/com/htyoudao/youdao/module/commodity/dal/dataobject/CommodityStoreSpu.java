package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.module.commodity.dal.dto.TimeBaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 门店下商品表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Data
@TableName("commodity_store_spu")
@EqualsAndHashCode(callSuper = true)
public class CommodityStoreSpu extends TimeBase implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 门店下商品的唯一ID
     */
    @TableId(value = "commodity_store_spu_id", type = IdType.ASSIGN_ID)
    private Long commodityStoreSpuId;
    /**
     * 门店ID
     */
    private Long storeId;

    private Long commodityId;

    /**
     * 门店下商品分类的ID
     */
    private Long commodityStoreCategoryId;
    /**
     * 商品原始 ID
     */
    private Long commodityStorePrimitiveSpuId;


    /**
     * 门店下商品名称
     */
    private String commodityStoreSpuName;



    /**
     * 门店下商品的详情
     */
    private String commodityStoreSpuDescription;

    /**
     * 门店下商品的排序顺序
     */
    private Integer commodityStoreSpuSort;

    /**
     * 门店下商品的单位
     */
    private String commodityStoreSpuUnit;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 门店下商品是否是单品（TRUE是 FALSE否）
     */
    private Integer commodityStoreSpuIsSingle;

    /**
     * 单点不送（0关闭  1开启）
     */
    private String commodityStoreSpuDetailUrl;

    /**
     * 门店下商品的点餐机状态
     */
    private Integer commodityStoreSpuMachineStatus;

    /**
     * 门店下商品的小程序状态
     */
    private Integer commodityStoreSpuAppletStatus;

    /**
     * 门店下商品是否锁
     */
    private Boolean commodityStoreSpuLock;




    private int storeForceStatus;

    private int wxForceStatus;

    @Schema(description = "商品说明")
    private String goodsExplain;



    private Integer salesVolumes;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String condiments;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String flavor;

    private String categoryName;

    private String spusTag;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String tagIds;

    private BigDecimal packageFee;

    private Integer saleRule;

    private Integer limitBuyNumber;

    private Integer limitDayBuyNumber;

    private Integer limitOrderBuyNumber;

    private Integer setmealType;

    private Integer applicableNumber;

    private Integer templateFlavor;

    private Integer choosePrice;
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String imageUrl;

    private Integer manyCopy;

    private Integer condimentIsMore;

    private Integer maxCondimentNumber;

    /**
     * 门店下商品的规格列表
     */
    @TableField(exist = false)
    List<CommodityStoreSku> commodityStoreSkuList;

    /**
     * 最低价
     */
    @TableField(exist = false)
    private BigDecimal lowPrice;

    /**
     * 最高价
     */
    @TableField(exist = false)
    private BigDecimal highPrice;

    /**
     * 最低划线价
     */
    @TableField(exist = false)
    private BigDecimal lowStrikePrice;

    /**
     * 最高划线价
     */
    @TableField(exist = false)
    private BigDecimal highStrikePrice;

    /**
     * 是否多规格
     */
    @TableField(exist = false)
    private Boolean isMoreSku;

    /**
     * 分类顺序
     */
    @TableField(exist = false)
    Integer categorySort;

    /**
     * 商品标签 list
     */
    @TableField(exist = false)
    private List<Integer> spusTagList = new ArrayList<>();
    /** 图片地址 */
    @TableField(exist = false)
    private List<String> imageUrlVO = new ArrayList<>();

    /** 商品里 sku 列表*/
    @TableField(exist = false)
    private List<CommodityStoreSku> commoditySkusList = new ArrayList<>();
    /**商品里的属性集合 */
    @TableField(exist = false)
    private List<CommodityFlavor> flavorList = new ArrayList<>();
    /** 商品列表里所有添加的小料 */
    @TableField(exist = false)
    private List<CommodityCondiments> condimentsList  =  new ArrayList<>();

    /** 套餐里分组添加的单品列表 */
    @TableField(exist = false)
    private List<CommodityStoreGroup> commoditySetmealGroupList = new ArrayList<>();

    @TableField(exist = false)
    List<CommodityStoreGroup> commodityStoreGroups = new ArrayList<>();
}
