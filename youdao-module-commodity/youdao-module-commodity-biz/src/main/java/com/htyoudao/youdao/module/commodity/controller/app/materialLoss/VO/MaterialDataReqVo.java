package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaterialDataReqVo {

    /**
     * 关联的原材料ID
     */
    @Schema(description = "关联的原材料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rawMaterialId;

    @Schema(description = "原料编号")
    private String materialsId;


    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "数量必须大于0")
    private BigDecimal quantity;

    /**
     * 最小单位单价
     */
    @Schema(description = "单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "单价必须大于0")
    private BigDecimal price;

    /**
     * 最小单位
     */
    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "单位不能为空")
    private String unit;


    /**
     * 总价
     */
    @Schema(description = "总价", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalPrice;


    @Schema(description = "二级目录名称")
    private String stasticsName;

}
