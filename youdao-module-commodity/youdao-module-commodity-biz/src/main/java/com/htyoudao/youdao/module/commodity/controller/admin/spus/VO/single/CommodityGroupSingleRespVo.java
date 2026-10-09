package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 套餐里分组与单品关系表对象  Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityGroupSingleRespVo {



    @Schema(description = "套餐分组与单品关系表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "套餐分组与单品关系ID不能为空")
    private Long commodityGroupSingleId;

    @Schema(description = "分组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;


    @Schema(description = "单品所属ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long spuId;


    @Schema(description = "单品所指的商品Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;


    @Schema(description = "单品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long singleId;


    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private int sort;


    @Schema(description = "加价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal upPrice;


    @Schema(description = "是否在分组中显示(1 是, 2 否)", requiredMode = Schema.RequiredMode.REQUIRED)
    private int display;


    @Schema(description = "份数", requiredMode = Schema.RequiredMode.REQUIRED)
    private int copies;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;

    @Schema(description = "商品缩略图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String thumbnailUrl;


    @Schema(description = "划线价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal markingPrice;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityUrl;

    @Schema(description = "是否默认选中 1是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer defaultChoose;


    @Schema(description = "是否必选 0否 1是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer requiredChoose;

    @Schema(description = "门店单品上下架状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private int commodityStoreSingleStatus;


    @Schema(description = "此品的规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String singleSkuName;


    @Schema(description = "此品的规格 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long singleSkuId;

    @Schema(description = "小程序上下架状态 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;

    @Schema(description = "门店上下架状态 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;
}
