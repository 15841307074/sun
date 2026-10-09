package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "app - 巡店概览 Response VO")
@Data
public class InspectionOverviewRespVO {

    @Schema(description = "门店id", example = "沈阳航空航天大学店")
    private Long storeId;

    @Schema(description = "门店名称", example = "沈阳航空航天大学店")
    private String storeName;

    @Schema(description = "巡店总次数", example = "100")
    private Integer inspectionCount;

    @Schema(description = "合格次数", example = "85")
    private Integer qualifiedCount;

    @Schema(description = "不合格次数", example = "15")
    private Integer unqualifiedCount;

    @Schema(description = "平均得分", example = "92.5")
    private BigDecimal averageScore;

    @Schema(description = "得分率", example = "88.50")
    private BigDecimal scoreRate;
}
