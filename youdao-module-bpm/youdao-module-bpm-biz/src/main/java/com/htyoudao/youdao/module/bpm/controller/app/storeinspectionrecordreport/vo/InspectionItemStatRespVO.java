package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "app - 点检项分析统计 Response VO")
@Data
public class InspectionItemStatRespVO {

    @Schema(description = "点检项名称", example = "产品腌制比例是否正确")
    private String checklistTitle;

    @Schema(description = "检查总次数", example = "150")
    private Integer checkCount;

    @Schema(description = "合格次数", example = "120")
    private Integer qualifiedCount;

    @Schema(description = "不合格次数", example = "30")
    private Integer unqualifiedCount;

    @Schema(description = "不合格率 (%)", example = "20.00")
    private BigDecimal unqualifiedRate;

}