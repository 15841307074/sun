package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActivityNjnzStoreExcelVO {

    @ExcelProperty(value = "门店名称")
    @Schema(description = "聚合指标名称")
    private String name;

    /**
     * 下单顾客数
     */
    @ExcelProperty(value = "付款用户数")
    @Schema(description = "付款用户数")
    private Long customerCount;

    @ExcelProperty(value = "订单数")
    @Schema(description = "订单数")
    private Long orderNumber;




    /**
     * 支付总金额
     */
    @ExcelProperty(value = "支付总金额")
    @Schema(description = "支付总金额")
    private Double payAmount;


    @ExcelProperty(value = "购买商品件数")
    @Schema(description = "购买商品件数")
    private Long commodityCount;

    /**
     * 优惠总金额
     */
    @ExcelProperty(value = "活动优惠")
    @Schema(description = "活动优惠")
    private Double offerAmount = 0.0;


//    /**
//     * 客单价
//     */
//    @ExcelProperty(value = "客单价")
//    @Schema(description = "客单价")
//    private Double averagePayment;











}
