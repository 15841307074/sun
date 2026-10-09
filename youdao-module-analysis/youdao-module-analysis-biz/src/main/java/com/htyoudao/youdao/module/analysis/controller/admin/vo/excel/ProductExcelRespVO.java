package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.DoubleConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.RateConverter;

import com.htyoudao.youdao.module.analysis.util.excelconverter.SingleConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductExcelRespVO extends TimeRangeRow{

    @ExcelProperty("门店名称")
    private String storeName;

    @ExcelProperty(value = "门店编号",converter = LongStringConverter.class)
    private Long storeId;

    @ExcelProperty("城市")
    private String storeCity;

    @ExcelProperty("上级组织")
    private String orgName;

    @ExcelProperty("商品分组")
    private String categoryName;

    @ExcelProperty("商品名称")
    private String goodsName;

    @ExcelProperty(value = "是否套餐",converter = SingleConverter.class)
    private Integer isSingle;

    @ExcelProperty("是否为加购商品")
    private String isPurchase;

    @ExcelProperty(value = "销售额", converter = DoubleConverter.class)
    private Double salesAmount;

    @ExcelProperty(value = "单独售卖销量")
    private Integer salesVolume;

    @ExcelProperty(value = "套餐内单品销量")
    private Integer singleSalesVolume;

    @ExcelProperty(value = "总销量")
    private Integer allSalesVolume;

    @ExcelProperty(value = "加购销量")
    private Integer purchaseSalesVolume;

    @ExcelProperty(value = "加购销售额", converter = DoubleConverter.class)
    private Double purchaseSalesAmount;
    
    @ExcelProperty(value = "下单人数")
    private Long customerCount;

    @ExcelProperty(value = "带来订单数")
    private Integer orderCount;

    @ExcelProperty(value = "复购人数")
    private Long repurchaseUserCount;

    @ExcelProperty(value = "复购率", converter = RateConverter.class)
    private Double repurchaseRate = 0.0;

    @ExcelProperty(value = "新客人数")
    private Long newCustomerCount;

    @ExcelProperty(value = "新客占比", converter = RateConverter.class)
    private Double newCustomerRate = 0.0;

    @ExcelProperty(value = "点击人数")
    private Long clickUserCount;

    @ExcelProperty(value = "加购人数")
    private Long addCartUserCount;

}
