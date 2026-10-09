package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商品更新名字 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityUNameReqVo {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;


    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "商品名称不能为空")
    private String commodityName;
}
