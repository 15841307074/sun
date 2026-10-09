package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 套餐上下架 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetMealUpAndDownReqVo {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;


    @Schema(description = "小程序上下架状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;


    @Schema(description = "门店上下架状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;
}
