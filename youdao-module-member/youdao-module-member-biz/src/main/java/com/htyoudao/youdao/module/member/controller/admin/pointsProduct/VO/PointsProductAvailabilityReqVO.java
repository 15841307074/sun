package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 积分商品上下架请求参数。
 */
@Data
@Schema(description = "管理后台 - 积分商品上下架 Request VO")
public class PointsProductAvailabilityReqVO {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull(message = "商品上下架状态不能为空")
    @Min(value = 1, message = "商品上下架状态只能为1或2")
    @Max(value = 2, message = "商品上下架状态只能为1或2")
    @Schema(description = "商品上下架状态：1已上架，2已下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAvailable;
}
