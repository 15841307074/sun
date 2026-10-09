package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.DoubleConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.RateConverter;
import lombok.Data;

@Data
public class MarketingShopExcelRespVO extends TimeRangeRow {

    @ExcelProperty("门店名称")
    private String storeName;

    @ExcelProperty(value = "优惠券领取数量")
    private Long couponReceiveCount;

    @ExcelProperty(value = "优惠券使用数量")
    private Integer couponUseCount;

    @ExcelProperty(value = "优惠券使用率")
    private String couponUseRate;

    @ExcelProperty(value = "下单人数")
    private Integer customerCount;

    @ExcelProperty(value = "活动订单数")
    private Integer orderCount;

    @ExcelProperty(value = "购买商品件数")
    private Integer salesVolume;

    @ExcelProperty(value = "客单价", converter = DoubleConverter.class)
    private Double customerUnitPrice;

    @ExcelProperty(value = "优惠券优惠金额", converter = DoubleConverter.class)
    private Double activityDiscountAmount;

    @ExcelProperty(value = "活动优惠金额", converter = DoubleConverter.class)
    private Double promotionDiscountAmount;

    @ExcelProperty(value = "总优惠金额", converter = DoubleConverter.class)
    private Double allDiscountAmount;

    @ExcelProperty(value = "活动销售额", converter = DoubleConverter.class)
    private Double salesAmount;

}
