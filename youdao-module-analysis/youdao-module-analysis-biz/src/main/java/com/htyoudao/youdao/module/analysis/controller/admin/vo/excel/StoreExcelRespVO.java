package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.DoubleConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.RateConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.YesNoConverter;
import lombok.Data;

@Data
public class StoreExcelRespVO extends TimeRangeRow {

    @ExcelProperty("门店名称")
    private String storeName;

    @ExcelProperty(value = "门店编号", converter = LongStringConverter.class)
    private Long storeId;

    @ExcelProperty("城市")
    private String storeCity;

    @ExcelProperty("上级组织")
    private String orgName;

    @ExcelProperty(value = "是否营业", converter = YesNoConverter.class)
    private Integer storeStatus;

    @ExcelProperty(value = "是否闭店", converter = YesNoConverter.class)
    private Integer openStatus;

    @ExcelProperty("营业时段")
    private String storeHours;

    @ExcelProperty(value = "是否不付款下单", converter = YesNoConverter.class)
    private Integer storeWithoutPayment;

    @ExcelProperty("有效订单")
    private Integer validOrders;

    @ExcelProperty(value = "优惠前总额", converter = DoubleConverter.class)
    private Double orderAmount;

    @ExcelProperty(value = "实收额", converter = DoubleConverter.class)
    private Double payAmount;

    @ExcelProperty("无效订单")
    private Integer invalidOrders;

    @ExcelProperty("堂食订单数")
    private Integer canteenFoodOrders;

    @ExcelProperty(value = "堂食订单优惠前总额", converter = DoubleConverter.class)
    private Double canteenFoodOrderAmount;

    @ExcelProperty("外带订单数")
    private Integer packOrders;

    @ExcelProperty(value = "外带订单优惠前总额", converter = DoubleConverter.class)
    private Double packOrderAmount;

    @ExcelProperty("外卖订单数")
    private Integer takeawayOrders;

    @ExcelProperty(value = "外卖订单优惠前总额", converter = DoubleConverter.class)
    private Double takeawayOrderAmount;

    @ExcelProperty("点餐机订单数")
    private Integer dcValidOrders;

    @ExcelProperty(value = "点餐机优惠前总额", converter = DoubleConverter.class)
    private Double dcOrderAmount;

    @ExcelProperty(value = "现金收款", converter = DoubleConverter.class)
    private Double cashPayAmount;

    @ExcelProperty(value = "优惠总额", converter = DoubleConverter.class)
    private Double discountAmount;

    @ExcelProperty(value = "客单价", converter = DoubleConverter.class)
    private Double averagePayment;

    @ExcelProperty(value = "下单人数")
    private Integer customerCount;

    @ExcelProperty("复购人数")
    private Integer repeatBuyers;

    @ExcelProperty(value = "复购率", converter = RateConverter.class)
    private Double repeatBuyersRate = 0.0;

    /**
     * 校园配送补贴
     */
    @ExcelProperty(value = "校园配送补贴",converter = DoubleConverter.class)
    private Double errandStoreSubsidyAmount;
}
