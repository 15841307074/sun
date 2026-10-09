package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ActivitySeckillStorePageVO {

    @Schema(description = "聚合指标ID")
    private String key;

    @Schema(description = "聚合指标名称")
    private String name;

    private List<Long> storeIds;

    private List<Long> channels;
    @Schema(description = "当前值")
    private ActivitySeckillStoreVO currentValue;

}
