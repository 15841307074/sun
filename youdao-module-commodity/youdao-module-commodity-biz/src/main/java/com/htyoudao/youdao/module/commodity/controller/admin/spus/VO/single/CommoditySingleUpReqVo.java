package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 单品上架 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySingleUpReqVo {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;


    @Schema(description = "1wx 2dcj", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "不能为空")
    private Integer chooseView;
}
