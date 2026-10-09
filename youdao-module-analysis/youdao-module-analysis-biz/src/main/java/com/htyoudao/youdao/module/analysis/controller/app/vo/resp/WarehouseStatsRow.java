package com.htyoudao.youdao.module.analysis.controller.app.vo.resp;


import lombok.Data;

@Data
public class WarehouseStatsRow {
    private Long warehouseId;
    private String warehouseName;

    private long storeCount;
    private long newStoreCount;
    private long closedStoreCount;

    private double orderTotalAmount; // 订货总值
    private double storeDailyAvgAmount; // 门店日均货值

    private Double mom; // 环比（百分比，例：14.8588表示14.8588%）
    private Double yoy; // 同比

    private long noPurchase10dStoreCount; // 按月时才有意义
}
