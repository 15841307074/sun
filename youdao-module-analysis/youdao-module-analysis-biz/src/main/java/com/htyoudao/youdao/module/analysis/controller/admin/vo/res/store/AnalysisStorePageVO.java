package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class AnalysisStorePageVO {

    @Schema(description = "聚合指标ID")
    private String key;

    @Schema(description = "聚合指标名称")
    private String name;

    private List<Long> storeIds;

    @Schema(description = "当前值")
    private AnalysisStoreVO currentValue;

    @Schema(description = "同比值")
    private AnalysisStoreVO beforeValues;
}
