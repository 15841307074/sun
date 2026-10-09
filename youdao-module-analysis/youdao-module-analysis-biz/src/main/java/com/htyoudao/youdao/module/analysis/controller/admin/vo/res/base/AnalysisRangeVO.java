package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRangeVO{

    @Schema(description = "key")
    private String key;

    @Schema(description = "当前值")
    private Double currentValue;

    @Schema(description = "同比值")
    private Double beforeValue;

    @Schema(description = "占比")
    private Double percentage;
}
