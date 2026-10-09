package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 模板套餐单品 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCommoditySingleSaveVO {

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "加价")
    private BigDecimal upPrice;

    @Schema(description = "份数")
    private Integer copies;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "单品ID")
    private Long commodityId;

    @Schema(description = "划线价")
    private BigDecimal markingPrice;

    @Schema(description = "单品名称")
    private String commodityName;

    @Schema(description = "单品图片")
    private String commodityUrl;

    @Schema(description = "是否默认选中")
    private int defaultChoose;

    @Schema(description = "是否必选")
    private int requiredChoose;

    @Schema(description = "规格名称")
    private String singleSkuName;

    @Schema(description = "规格ID")
    private Long singleSkuId;

    @Schema(description = "小程序状态")
    private Integer wxStatus;

    @Schema(description = "门店状态")
    private Integer storeStatus;

    private Long commodityGroupSingleId;


    private Long templateId;

    private Long commodityTemplateId;

    private Long templateGroupId;


    /**
     * 是否在分组中显示 (1 是, 2 否)
     */
    private Integer display;


    /**
     * 单品所属的商品ID
     */
    private Long spuId;


    /** 口味*/
    private String flavor;
}
