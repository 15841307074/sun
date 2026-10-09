package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 点餐机套餐内子品上下架 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreSingleStatusReqVO {

    @Schema(description = "门店下套餐分组里的单品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStoreSingleId;


    @Schema(description = "门店下套餐里单品状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSingleStatus;

    @Schema(description = "小程序状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;

    @Schema(description = "门店状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;


    @Schema(description = "1 wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseView;
}
