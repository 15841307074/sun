package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 查询逻辑 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreAllReqVO {




    @Schema(description = "门店下商品分类", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品分类不能为空")
    private Long commodityStoreCategoryId;




}
