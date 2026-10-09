package com.htyoudao.youdao.module.analysis.util.excelconverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;

public class BigDecimalNumberConverter implements Converter<BigDecimal> {

 
    public Class<BigDecimal> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    /**
     * easyExcel导出数据类型转换
     * @param cellData
     * @param contentProperty
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public BigDecimal convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                        GlobalConfiguration globalConfiguration) {
        return cellData.getNumberValue();
    }

    /**
     * 导入转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        return new WriteCellData(value.toPlainString());
    }
}