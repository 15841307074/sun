package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.bigdecimal.BigDecimalStringConverter;
import com.alibaba.excel.converters.integer.IntegerStringConverter;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * @author dht
 */
@Schema(description = "优惠券使用的数据 Request VO")
@Data
@ToString(callSuper = true)
public class GoodCouponDateRespVO {

    @Schema(description = "店铺id")
    @ExcelIgnore
    private Long storeId;

    @Schema(description = "时间")
    @ExcelProperty(value = "时间")
    private String createDate;


    @Schema(description = "店铺名称")
    @ExcelProperty(value = "门店名称")
    private String storeName;

    @Schema(description = "门店领取次数")
    @ExcelProperty(value = "门店领取次数",converter = LongStringConverter.class)
    private Long receiveNum;

    @Schema(description = "付款单数")
    @ExcelProperty(value = "使用数量",converter = IntegerStringConverter.class)
    private Integer orderNum;

    @Schema(description = "付款单数")
    @ExcelProperty(value = "使用数量",converter = IntegerStringConverter.class)
    private Integer useNum;

    @Schema(description = "用券总成交额")
    @ExcelProperty(value = "用券总成交额",converter = BigDecimalStringConverter.class)
    private BigDecimal turnover;

    @Schema(description = "优惠总金额")
    @ExcelProperty(value = "优惠券优惠",converter = BigDecimalStringConverter.class)
    private BigDecimal offerTotal;

    @Schema(description = "费效比")
    @ExcelProperty(value = "费效比",converter = BigDecimalStringConverter.class)
    private BigDecimal cost;

    @Schema(description = "用券笔单价")
    @ExcelProperty(value = "用券笔单价",converter = BigDecimalStringConverter.class)
    private BigDecimal singlePrice;

    @Schema(description = "商品数量")
    @ExcelProperty(value = "商品数量",converter = IntegerStringConverter.class)
    private Integer itemNum;



    @Schema(description = "老客户数量")
    @ExcelIgnore
    private Integer oldCustom;

    @Schema(description = "新客户数量")
    @ExcelIgnore
    private Integer newCustom;

    @Schema(description = "使用率")
    @ExcelIgnore
    private BigDecimal usedRate;

    @ExcelIgnore
    private Integer couponSource;
}
