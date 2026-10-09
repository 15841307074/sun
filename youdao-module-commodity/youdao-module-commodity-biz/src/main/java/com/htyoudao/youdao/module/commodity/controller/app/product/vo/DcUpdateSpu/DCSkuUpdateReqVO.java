package com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Schema(description = "规格修改 ReqVo")
@Data
public class DCSkuUpdateReqVO {

    @Schema(description = "规格 Id")
    @NotNull(message = "规格 Id不能为空")
    private Long commodityStoreSkuId;

    /**
     * 门店下当前规格的状态
     */
    @Schema(description = "规格状态")
    @NotNull(message = "门店下当前规格的状态不能为空")
    private Integer commodityStoreSkuStatus;

}
