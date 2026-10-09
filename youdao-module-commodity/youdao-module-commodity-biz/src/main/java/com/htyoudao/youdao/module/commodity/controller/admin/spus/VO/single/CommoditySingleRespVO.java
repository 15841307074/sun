package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Schema(description = "管理后台 Single Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySingleRespVO {


    /**
     * 套餐分组与单品关系表ID
     */

    @Schema(description = "主键 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityGroupSingleId;

    @Schema(description = " 分组 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;

    /**
     * 单品所属ID
     */
    @Schema(description = "单品所属ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long spuId;

    /**
     * 单品所指的商品 Id
     */
    @Schema(description = "单品所指的商品 Id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;


    /**
     * 排序
     */
    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private int sort;

    /**
     * 加价
     */
    @Schema(description = "加价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal upPrice;



    /**
     * 份数
     */
    @Schema(description = "份数", requiredMode = Schema.RequiredMode.REQUIRED)
    private int copies;

    //商品名称
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;



    /**
     * 划线价格
     */
    @Schema(description = "划线价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal markingPrice;

    /**
     * 商品图片
     */
    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityUrl;

    /**
     * 是否默认选中 1是 0否
     */
    @Schema(description = "是否默认选中 1是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer defaultChoose;

    /**
     * 是否必选 0否 1是
     */
    @Schema(description = "是否必选 0否 1是", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer  requiredChoose;


    /**
     * 此品的规格名称
     */
    @Schema(description = "此品的规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String singleSkuName;

    /**
     * 此品的规格 id
     */
    @Schema(description = "此品的规格 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long singleSkuId;

    /** 小程序上下架状态 1 上架 0 下架*/
    @Schema(description = "小程序上下架状态 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    @Schema(description = "门店上下架状态 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;
}
