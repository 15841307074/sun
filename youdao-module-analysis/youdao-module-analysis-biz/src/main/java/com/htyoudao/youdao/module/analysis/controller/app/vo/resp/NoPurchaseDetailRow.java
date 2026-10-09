package com.htyoudao.youdao.module.analysis.controller.app.vo.resp;

import lombok.Data;

@Data
public class NoPurchaseDetailRow {
    private Long warehouseId;
    private String warehouseName;

    private Long storeId;
    private String storeName;

    /**
     * 最近一段未进货时间：yyyy-MM-dd 至 yyyy-MM-dd
     */
    private String noPurchaseStartDate;
    private String noPurchaseEndDate;

    /**
     * 该段连续未进货天数
     */
    private long noPurchaseDays;
}
