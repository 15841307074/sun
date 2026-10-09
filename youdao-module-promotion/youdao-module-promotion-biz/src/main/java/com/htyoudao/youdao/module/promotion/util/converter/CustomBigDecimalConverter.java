package com.htyoudao.youdao.module.promotion.util.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author dht
 * 自定义 BigDecimal 2位转换器
 */
public class CustomBigDecimalConverter implements Converter<BigDecimal> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        // 在这里强制保留 2 位小数，并转为 String
        String formattedValue = value.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return new WriteCellData<>(formattedValue);
    }
}