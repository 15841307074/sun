package com.htyoudao.youdao.module.commodity.dal.dto;

import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "app - 商品 VO")
public class SpuDto extends TimeBaseDTO{

    @Schema(description = "spuID")
    private Long spuId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "spu 名称")
    private String spuName;

    @Schema(description = "spu 备注")
    private String spuDesc;

    @Schema(description = "打包费每份多少元的每份")
    private Integer manyCopy;

    @Schema(description = "打包费（单位元）")
    private BigDecimal packageFee;

    @Schema(description = "门店下商品的点餐机状态")
    private Integer commodityStoreSpuMachineStatus;

    @Schema(description = "门店下商品的小程序状态")
    private Integer commodityStoreSpuAppletStatus;

    @Schema(description = "最少购买多少份")
    private Integer limitBuyNumber;

    @Schema(description = "套餐类型（1.固定搭配套餐，2.分组可选套餐 3 单品）")
    private Integer setmealType;

    @Schema(description = "分类 id")
    private Long categoryId;

    @Schema(description = "标签")
    private List<CommodityTag> commodityTags = new ArrayList<>();

    @Schema(description = "商品原始 id")
    private Long commodityId;

    @Schema(description = "属性集合")
    private List<FlavorDto> commodityFlavors = new ArrayList<>();

    @Schema(description = "小料集合")
    private List<CondimentDto> commodityCondiments = new ArrayList<>();

    @Schema(description = "套餐下分组集合")
    private List<GroupDto> groupList = new ArrayList<>();

    @Schema(description = "规格信息")
    private List<SkuDto> skuList = new ArrayList<>();

    @Schema(description = "商品图片")
    private List<String> bannerList = new ArrayList<>();

    @Schema(description = "是否展示按钮")
    private Integer showBtn;

    @Schema(description = "价格")
    private BigDecimal spuPrice;

    @Schema(description = "划线价")
    private BigDecimal spuUnderlinedPrice;

    @Schema(description = "小料是否多选", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer condimentIsMore;

    @Schema(description = "最多可加小料数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCondimentNumber;

    @Schema(description = "顺序")
    private Integer sort;

    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）")
    private Integer saleRule;

    @Schema(description = "命中n件n折活动集合")
    private List<ActivityNjnzDTO> njnzActivityList = Collections.emptyList();

    @Schema(description = "命中mjmz活动集合")
    private List<ActivityMJDTO> mjActivityList = Collections.emptyList();

    @Schema(description = "命中满赠活动（最新一个）")
    private ActivityMzDTO mzActivityVO;

    @Schema(description = "单点不送 true单点配送 false或null 不限制")
    private Boolean singleNoDelivery = false;

    @Schema(description = "是否为加购商品 ture是 false或null 不是")
    private Boolean afterOrder = false;

    @Schema(description = "商品说明")
    private String goodsExplain;
}
