package com.htyoudao.youdao.module.commodity.dal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "app - 套餐下单品 VO")
public class SingleDto implements Serializable {

    @Schema(description = "单品 id")
    private Long singleId;

    @Schema(description = "商品原始 id")
    private Long commodityId;

    @Schema(description = "单品名称")
    private String singleName;

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "门店下套餐里单品的加价")
    private BigDecimal upPrice;

    @Schema(description = "划线价")
    private BigDecimal markingPrice;

    @Schema(description = "是否默认选中 0否 1是")
    private Integer defaultChoose;

    @Schema(description = "是否必选 0否 1是")
    private Integer requiredChoose;

    @Schema(description = "此品的规格名称")
    private String skuName;

    @Schema(description = "小程序上下架状态 1 上架 0 下架")
    private Integer wxStatus;

    @Schema(description = "门店上下架状态 1 上架 0 下架")
    private Integer storeStatus;

    @Schema(description = "门店下套餐里单品的份数")
    private Integer commodityStoreSingleCopies;

    @Schema(description = "所在套餐的spuID")
    private Long commodityStoreSpuId;

    @Schema(description = "当前Single的连锁库skuId")
    private Long singleSkuId;

    @Schema(description = "属性集合")
    private List<FlavorDto> commodityFlavors = new ArrayList<>();
}
