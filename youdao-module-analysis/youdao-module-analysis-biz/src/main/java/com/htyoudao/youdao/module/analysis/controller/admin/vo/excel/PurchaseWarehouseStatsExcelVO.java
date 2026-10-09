package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class PurchaseWarehouseStatsExcelVO {

    @ExcelIgnore
    private Long warehouseId;

    @ExcelProperty("仓库名称")
    private String warehouseName;

    @ExcelProperty("门店数")
    private long storeCount;

    @ExcelProperty("新增门店数")
    private long newStoreCount;

    @ExcelProperty("闭店门店数")
    private long closedStoreCount;

    @ExcelProperty("订货总值")
    private double orderTotalAmount;

    @ExcelProperty("门店日均货值")
    private double storeDailyAvgAmount;

    @ExcelProperty("环比")
    private Double mom;

    @ExcelProperty("同比")
    private Double yoy;

    @ExcelProperty("十天未进货门店数")
    private long noPurchase10dStoreCount;
}
