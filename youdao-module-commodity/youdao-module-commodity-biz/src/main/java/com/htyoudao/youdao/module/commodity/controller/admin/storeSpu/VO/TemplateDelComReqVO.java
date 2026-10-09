package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "管理后台 - 删除模版商品 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateDelComReqVO {




    @Schema(description = "商品 ids", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品 ids 不能为空")
    private List<Long> commodityTemplateIds;


}
