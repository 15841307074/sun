package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 模版价格 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplatePriceReqVO {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品模版不能为空")
    private Long commodityTemplateId;


    @Schema(description = "门店 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> storeId;


    @Schema(description = "商品 ids", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> commodityIds;

    @Schema(description = "商品模版id", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> commodityTemplateIds;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "模板价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> templatePriceIds;

    @Schema(description = "分类模版ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryTemplateId;

    //模板商品
    @Schema(description = "模板商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateCommoditySkuId;

    @Schema(description = "平台价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal illustratePrices;

    @Schema(description = "划线价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal strikeThroughPrice;

    @Schema(description = "商品模版", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityTemplateSkus> commodityTemplateSkus;
}
