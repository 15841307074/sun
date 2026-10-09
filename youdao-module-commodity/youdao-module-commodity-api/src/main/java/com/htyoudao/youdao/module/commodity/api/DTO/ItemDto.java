package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dht
 */
@Data
public class ItemDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -7758804630174256906L;

    private Long spuId;
    private Long storeId;
    private String imageUrl;
    private String spuName;
    private String spuDesc;
    /**
     * 是否展示按钮
     */
    private Integer showBtn;
    /**
     * 打包费每份多少元的每份 是每多少份
     */
    private Integer manyCopy;
    /**
     * 售卖规则
     */
    private BigDecimal packageFee;
    /**
     * 门店下商品的点餐机状态
     */
    private int commodityStoreSpuMachineStatus;

    /**
     * 门店下商品的小程序状态
     */
    private int commodityStoreSpuAppletStatus;

    /**
     * 最少购买多少份
     */
    private Integer limitBuyNumber;

    private BigDecimal spuPrice;
    /**
     * 划线价
     */
    private BigDecimal spuUnderlinedPrice;
    /**
     * 套餐类型（1.固定搭配套餐，2.分组可选套餐 3 单品）
     */
    private Integer setmealType;
    private Long categoryId;
    /**
     * 上下架状态 0下架 1上架
     */
    private String spuTag;
    private int listingStatus;

    private Long commodityId;
    private List<FlavorDto> flavorList = new ArrayList<>();
    private List<CondimentDto> condimentsList = new ArrayList<>();
    private List<SkuDto> skuList = new ArrayList<>();
    private List<String> bannerList = new ArrayList<>();
    private List<GroupDto> groupList = new ArrayList<>();
    //顺序
    Integer sort;
}