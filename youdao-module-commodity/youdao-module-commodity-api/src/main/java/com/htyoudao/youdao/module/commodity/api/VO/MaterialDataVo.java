package com.htyoudao.youdao.module.commodity.api.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
//商品列表信息
@Data
public class MaterialDataVo implements Serializable {


    @Serial
    private static final long serialVersionUID = -988955062859099349L;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long skuId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    @Schema(description = "商品类型 1 商品 2 原材料", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityType;

    @Schema(description = "原材料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String materialId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;




}
