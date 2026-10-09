package com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.bigdecimal.BigDecimalStringConverter;
import com.alibaba.excel.converters.integer.IntegerStringConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponDataSumExportExcel {

    @Schema(description = "店铺名称")
    @ExcelProperty(value = "门店名称",index = 0)
    private String storeName;

    @Schema(description = "门店领取次数")
    @ExcelProperty(value = "门店领取次数",index = 1)
    private Long receiveNum;


    @Schema(description = "用券总次数")
    @ExcelProperty(value = "使用数量",index = 2)
    private Integer usage;

    @Schema(description = "用券总成交额")
    @ExcelProperty(value = "用券总成交额",index = 3,converter = BigDecimalStringConverter.class)
    private BigDecimal turnover;

    @Schema(description = "优惠总金额")
    @ExcelProperty(value = "优惠券优惠",index = 4,converter = BigDecimalStringConverter.class)
    private BigDecimal offerTotal;

    @Schema(description = "费效比")
    @ExcelProperty(value = "费效比",index = 5,converter = BigDecimalStringConverter.class)
    private BigDecimal cost;

    @Schema(description = "付款单数")
    @ExcelProperty(value = "付款单数",index = 6,converter = IntegerStringConverter.class)
    private Integer orderNum;

    @Schema(description = "用券笔单价")
    @ExcelProperty(value = "用券笔单价",index = 7,converter = BigDecimalStringConverter.class)
    private BigDecimal singlePrice;

    @Schema(description = "商品数量")
    @ExcelProperty(value = "购买商品件数",index = 8,converter = IntegerStringConverter.class)
    private Integer itemNum;

}
