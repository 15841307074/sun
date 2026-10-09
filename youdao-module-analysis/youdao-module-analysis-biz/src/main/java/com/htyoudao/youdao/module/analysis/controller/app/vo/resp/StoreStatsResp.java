package com.htyoudao.youdao.module.analysis.controller.app.vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class StoreStatsResp {
    private String startDate;
    private String endDate;
    private long days;
    private long total;
    private List<StoreStatsRow> rows;
}