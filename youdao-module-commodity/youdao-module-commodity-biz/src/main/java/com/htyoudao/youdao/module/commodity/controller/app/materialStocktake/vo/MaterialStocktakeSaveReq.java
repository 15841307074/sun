package com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class MaterialStocktakeSaveReq {

    @Schema(description = "门店ID")
    @NotNull
    private Long storeId;

    @Schema(description = "填写的盘点信息")
    private List<MaterialInfo> materialInfos;

    @Schema(description = "库存金额差值")
    private BigDecimal inventoryDifferenceAmount = BigDecimal.ZERO;

    @Schema(description = "备注")
    private String remark;

    @Data
    public static class MaterialInfo {

        @Schema(description = "盘点选择的单位", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        private String chooseUnit;

        @Schema(description = "原材料ID")
        @NotNull
        private Long materialId;

        @Schema(description = "盘点数量(最小单位数量)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "盘点数量不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "盘点数量必须大于0")
        private BigDecimal takeCount;
    }

}
