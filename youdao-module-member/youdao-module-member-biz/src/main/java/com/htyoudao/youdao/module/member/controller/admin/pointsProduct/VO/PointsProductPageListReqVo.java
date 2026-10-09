package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 积分商品列表参数 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsProductPageListReqVo {



    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;



    @Schema(description = "商品类型 1 优惠劵 2 实体积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;

    @Schema(description = "商品上下架状态：1已上架，2已下架")
    @Min(value = 1, message = "商品上下架状态只能为1或2")
    @Max(value = 2, message = "商品上下架状态只能为1或2")
    private Integer isAvailable;

}
