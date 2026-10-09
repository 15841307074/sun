package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品上面商品数量统计
 */

@Schema(description = "管理后台 商品上面商品数量统计 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityCountRespVO {
    @Schema(description = "类型 1全部 2售卖 3已下架 4已隐藏", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer count;
}
