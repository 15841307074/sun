package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.Data;

@Data
public class AnalysisTopVO {

    @Schema(description = "排名字段ID")
    private String key;

    @Schema(description = "字段显示名")
    private String name;

    private List<Long> storeIds;

    @Schema(description = "指标值")
    private TreeMap<String, Double> values;
}
