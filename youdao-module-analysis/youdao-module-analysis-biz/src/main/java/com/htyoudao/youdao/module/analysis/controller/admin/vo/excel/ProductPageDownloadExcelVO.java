package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.analysis.util.excelconverter.DoubleConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.ProductTypeConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.RateConverter;
import lombok.Data;

@Data
public class ProductPageDownloadExcelVO {

    @ExcelProperty("商品")
    private String goodsName;

    @ExcelProperty("商品分组")
    private String categoryName;

    @ExcelProperty(value = "商品类型", converter = ProductTypeConverter.class)
    private Integer isSingle;

    @ExcelProperty("所属门店")
    private String storeName;

    @ExcelProperty("时间")
    private String time;

    @ExcelProperty(value = "单独售卖销量")
    private Integer salesVolume;

    @ExcelProperty(value = "套餐内单独商品销量")
    private Integer singleSalesVolume;

    @ExcelProperty(value = "总销量")
    private Integer allSalesVolume;

    @ExcelProperty(value = "销售额", converter = DoubleConverter.class)
    private Double salesAmount;

    @ExcelProperty(value = "带来订单数")
    private Integer orderCount;

    @ExcelProperty(value = "下单人数")
    private Long customerCount;

    @ExcelProperty(value = "复购人数")
    private Long repurchaseUserCount;

    @ExcelProperty(value = "复购率", converter = RateConverter.class)
    private Double repurchaseRate;

    @ExcelProperty(value = "新客人数")
    private Long newCustomerCount;

    @ExcelProperty(value = "新客占比", converter = RateConverter.class)
    private Double newCustomerRate;

    @ExcelProperty(value = "点击人数")
    private Long clickUserCount;

    @ExcelProperty(value = "加购人数")
    private Long addCartUserCount;

    @ExcelProperty(value = "在售门店数")
    private Long storeCount;
}
