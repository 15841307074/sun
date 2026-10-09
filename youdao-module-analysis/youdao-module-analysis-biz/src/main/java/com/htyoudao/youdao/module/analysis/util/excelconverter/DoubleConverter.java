package com.htyoudao.youdao.module.analysis.util.excelconverter;

import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import java.text.DecimalFormat;
import com.alibaba.excel.converters.Converter;


/**
 * Double转两位小数字符串的转换器（不带百分号）
 */
public class DoubleConverter implements Converter<Double> {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    /**
     * 导出转换，将Double格式化为保留两位小数的字符串
     *
     * @param value             单元格值
     * @param contentProperty   单元格属性
     * @param globalConfiguration 全局配置
     * @return WriteCellData<String>
     */
    @Override
    public WriteCellData<String> convertToExcelData(Double value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("0.00");
        }
        String formatted = DECIMAL_FORMAT.format(value);
        return new WriteCellData<>(formatted);
    }

    /**
     * 导入时（如果有需要转换回Double），可以实现该方法
     */
    @Override
    public Double convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        try {
            return Double.parseDouble(cellData.getStringValue());
        } catch (Exception e) {
            return 0.0;
        }
    }
}