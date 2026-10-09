package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.template;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 模板商品详情 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCommodityDetailReqVO {

    @Schema(description = "模板商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板商品ID不能为空")
    private Long commodityTemplateId;
}
