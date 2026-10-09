package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商品分类删除 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDelReqVo {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Long id;
}
