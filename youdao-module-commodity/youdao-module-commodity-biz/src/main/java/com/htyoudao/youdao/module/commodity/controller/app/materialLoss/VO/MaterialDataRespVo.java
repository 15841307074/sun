package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 调拨记录返回值
 */
@Data
public class MaterialDataRespVo {

    /**
     * 关联的原材料ID
     */
    @Schema(description = "关联的原材料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rawMaterialId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;

    @Schema(description = "商品编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityCode;

    @Schema(description = "品牌名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String brandName;


    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal quantity;

    /**
     * 最小单位单价
     */
    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;
    /**
     * 最小单位
     */
    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;

    @Schema(description = "总价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalPrice;

}
