package com.htyoudao.youdao.module.analysis.controller.app.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class WarehouseStatsResp {
    private String startDate;
    private String endDate;
    private long days; // 统计天数（不含当天）
    private List<WarehouseStatsRow> rows;
}