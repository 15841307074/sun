package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 批量排序 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySpusSortReqVo {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;


    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序字段不能为空")
    private Integer sort;
}
