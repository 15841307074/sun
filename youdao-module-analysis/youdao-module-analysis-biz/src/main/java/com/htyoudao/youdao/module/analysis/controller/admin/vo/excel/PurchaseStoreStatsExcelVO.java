package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PurchaseStoreStatsExcelVO {

    @ExcelIgnore
    private Long storeId;

    @ExcelProperty("门店名称")
    private String storeName;

    @ExcelIgnore
    private Long warehouseId;

    @ExcelProperty("仓库名称")
    private String warehouseName;

    @ExcelProperty("订货总值")
    private double orderTotalAmount;

    @ExcelProperty("日均货值")
    private double dailyAvgAmount;

    @ExcelProperty("环比")
    private Double mom;

    @ExcelProperty("同比")
    private Double yoy;
}
