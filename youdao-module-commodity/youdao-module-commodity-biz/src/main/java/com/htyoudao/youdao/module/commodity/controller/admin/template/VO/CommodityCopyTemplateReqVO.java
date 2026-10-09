package com.htyoudao.youdao.module.commodity.controller.admin.template.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommodityCopyTemplateReqVO {
    @NotNull(message = "模板不能为空")
    @Schema(description = "模板 id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long commodityTemplateId;
}
