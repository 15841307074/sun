package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "app - 巡店间隔分布 Response VO")
@Data
public class StoreInspectionIntervalRespVO {

    @Schema(description = "门店id", example = "沈阳航空航天大学店")
    private Long storeId;

    @Schema(description = "门店名称", example = "沈阳航空航天大学店")
    private String storeName;

    @Schema(description = "上次巡店人", example = "张三")
    private String lastInspectorName;

    @Schema(description = "上次巡店得分率 (%)", example = "95.00")
    private BigDecimal lastScoreRate;

    @Schema(description = "上次巡店得分", example = "95")
    private Integer lastActualScore;

    @Schema(description = "上次巡店距今 (天)", example = "12")
    private Integer daysSinceLastInspection;

}