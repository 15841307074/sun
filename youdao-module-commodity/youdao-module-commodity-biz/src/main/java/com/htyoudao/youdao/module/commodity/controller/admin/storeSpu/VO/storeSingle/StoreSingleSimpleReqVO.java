package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.storeSingle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class StoreSingleSimpleReqVO {

    @NotNull(message = "门店ID不能为空")
    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    /**
     * 门店下商品名称
     */
    @Schema(description = "门店下商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreSpuName;
}
