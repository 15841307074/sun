package com.htyoudao.youdao.module.analysis.controller.app.vo.resp;

import lombok.Data;

@Data
public class StoreStatsRow {
    private Long storeId;
    private String storeName;

    private Long warehouseId;
    private String warehouseName;

    private double orderTotalAmount;
    private double dailyAvgAmount;

    private Double mom;
    private Double yoy;
}
