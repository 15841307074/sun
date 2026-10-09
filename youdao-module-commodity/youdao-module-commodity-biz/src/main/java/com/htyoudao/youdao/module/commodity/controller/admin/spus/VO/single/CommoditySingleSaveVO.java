package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class CommoditySingleSaveVO {
    /**
     * 单品所指的商品 Id
     */
    @Schema(description = "单品所指的商品 Id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单品所指的商品 Id不能为空")
    private Long commodityId;

    /**
     * 排序
     */
    @Schema(description = "排序",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "排序不能为空")
    private int sort;

    /**
     * 加价
     */
    @Schema(description = "加价",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal upPrice;



    /**
     * 份数
     */
    @Schema(description = "份数",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "份数不能为空")
    private int copies;

    //商品名称
    @Schema(description = "商品名称",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品名称不能为空")
    private String commodityName;

    /**
     * 划线价格
     */
    @Schema(description = "划线价格",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "划线价格不能为空")
    private BigDecimal markingPrice;

    /**
     * 商品图片
     */
    @Schema(description = "商品图片",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品图片不能为空")
    private String commodityUrl;

    /**
     * 是否默认选中 1是 0否
     */
    @Schema(description = "是否默认选中 1是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer defaultChoose;

    /**
     * 是否必选 0否 1是
     */
    @Schema(description = "是否必选 0否 1是",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer  requiredChoose;

    /**
     * 此品的规格名称
     */
    @Schema(description = "此品的规格名称",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "此品的规格名称不能为空")
    private String singleSkuName;

    /**
     * 此品的规格 id
     */
    @Schema(description = "此品的规格 id",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "此品的规格 id不能为空")
    private Long singleSkuId;

    /** 小程序上下架状态 1 上架 0 下架*/
    @Schema(description = "小程序上下架状态 1 上架 0 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "小程序上下架状态 1 上架 0 下架不能为空")
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    @Schema(description = "门店上下架状态 1 上架 0 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "门店上下架状态 1 上架 0 下架不能为空")
    private Integer storeStatus;
}
