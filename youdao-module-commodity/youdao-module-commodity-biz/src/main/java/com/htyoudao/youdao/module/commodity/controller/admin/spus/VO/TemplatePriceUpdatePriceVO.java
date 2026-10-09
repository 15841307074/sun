package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Schema(description = "管理后台 - 模版价格修改 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplatePriceUpdatePriceVO {
    @Schema(description = "模板价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板价格 不能为空")
    private List<CommodityTemplateSkus> commodityTemplateSkus;
}
