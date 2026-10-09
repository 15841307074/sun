package com.htyoudao.youdao.module.commodity.controller.admin.category.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商品ID查询 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryIdReqVo {

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Long id;
}
