package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ActivityNjnzStorePageVO {

    @Schema(description = "聚合指标ID")
    private String key;

    @Schema(description = "聚合指标名称")
    private String name;

    private List<Long> storeIds;

    @Schema(description = "当前值")
    private ActivityNjnzStoreVO currentValue;

}
