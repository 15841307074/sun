package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 门店套餐上架信息 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreSingleRespVO {



    @Schema(description = "门店下套餐分组里的单品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSingleId;


    @Schema(description = "门店下套餐分组 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreGroupId;
    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSpuId;


    @Schema(description = "门店下套餐里单品的顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSingleSort;


    @Schema(description = "门店下套餐里单品的加价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal commodityStoreSinglePrice;


    @Schema(description = "门店下套餐里单品的份数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSingleCopies;


    @Schema(description = "门店下套餐里单品状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSingleStatus;


    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;


    @Schema(description = "商品的ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;


    @Schema(description = "划线价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal markingPrice;


    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;


    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityUrl;


    @Schema(description = "是否默认选中 0否 1是", requiredMode = Schema.RequiredMode.REQUIRED)
    private int defaultChoose;


    @Schema(description = "是否必选 0否 1是", requiredMode = Schema.RequiredMode.REQUIRED)
    private int requiredChoose;


    @Schema(description = "商品spuId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long spuId;


    @Schema(description = "此品的规格名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String singleSkuName;

    @Schema(description = "此品的规格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long singleSkuId;

    @Schema(description = "小程序上下架状态 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;

    @Schema(description = "门店上下架状态 1 上架 0 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;
}
