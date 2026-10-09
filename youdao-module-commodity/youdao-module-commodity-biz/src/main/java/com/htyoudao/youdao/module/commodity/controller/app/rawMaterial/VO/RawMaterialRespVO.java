package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "app - 原材料商品累计返回 VO")
public class RawMaterialRespVO {

    @Schema(description = "详情列表")
    private List<RawMaterialDetailVO> details = new ArrayList<>();

    @Schema(description = "总计")
    private RawMaterialTotalRespVO total;
}
