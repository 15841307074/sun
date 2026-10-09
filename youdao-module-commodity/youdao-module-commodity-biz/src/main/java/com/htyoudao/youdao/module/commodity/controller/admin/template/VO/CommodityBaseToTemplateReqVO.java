package com.htyoudao.youdao.module.commodity.controller.admin.template.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CommodityBaseToTemplateReqVO {

    @NotNull(message = "商品不能为空")
    @Schema(description = "商品id集合", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Long> commodityIds;

    @NotNull(message = "模板不能为空")
    @Schema(description = "模板 id", requiredMode = Schema.RequiredMode.REQUIRED)
    Long commodityTemplateId;
}
