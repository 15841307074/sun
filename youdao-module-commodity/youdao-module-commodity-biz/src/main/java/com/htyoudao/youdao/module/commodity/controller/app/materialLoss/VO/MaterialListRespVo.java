package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.CommodityConversion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拆分原材料数据
 */
@Data
public class MaterialListRespVo {


    @Schema(description = "原材料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rawMaterialId;

    @Schema(description = "原料编号")
    private String materialsId;

    @Schema(description = "原料名称")
    private String materialsName;

    @Schema(description = "使用单位")
    private String usedUnit;

    @Schema(description = "单位列表")
    private List<String> unitList;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "单位换算列表")
    private List<CommodityConversion> calUnitList;


    @Schema(description = "二级目录名称")
    private String stasticsName;






}
