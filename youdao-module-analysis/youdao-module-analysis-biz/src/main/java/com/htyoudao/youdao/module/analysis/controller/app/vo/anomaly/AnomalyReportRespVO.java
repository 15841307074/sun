package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 经营异常报告列表返回 VO（App 接口 2）
 */
@Schema(description = "App - 经营异常报告")
@Data
public class AnomalyReportRespVO {

    @Schema(description = "报告ID（字符串）", example = "2038870253207044099")
    private String id;

    @Schema(description = "异常类型：1=周度 2=月度销量 3=月度0订单", example = "1")
    private Integer anomalyType;

    @Schema(description = "周期开始日", example = "2026-09-14")
    private String periodStart;

    @Schema(description = "统计门店数", example = "12")
    private Integer storeCount;

    @Schema(description = "异常指标数（仅周度 type=1 有值，月度为 null）", example = "3")
    private Integer anomalyMetricCount;

    @Schema(description = "异常门店数（仅月度 type=2/3 有值，周度为 null）", example = "5")
    private Integer anomalyStoreCount;
}
