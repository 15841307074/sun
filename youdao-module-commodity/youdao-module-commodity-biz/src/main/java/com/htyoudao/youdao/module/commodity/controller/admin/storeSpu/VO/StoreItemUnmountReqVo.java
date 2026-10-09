package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 门店下单品下架 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreItemUnmountReqVo {

    @Schema(description = "门店下的商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品ID不能为空")
    private Long commodityStoreSpuId;


    @Schema(description = "门店下商品的点餐机状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSpuMachineStatus;


    @Schema(description = "门店下商品的小程序状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreSpuAppletStatus;

    @Schema(description = "1 wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseView;


    //true 点餐机 false pc
    private Boolean isApp;




}
