package com.htyoudao.youdao.module.analysis.util.excelconverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 商品类型转换器
 */
public class ProductTypeConverter implements Converter<Integer> {

    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        String text = switch (value == null ? 1 : value) {
            case 1 -> "单品";
            case 2 -> "套餐";
            case 3 -> "加购";
            default -> String.valueOf(value);
        };
        return new WriteCellData<>(text);
    }
}
