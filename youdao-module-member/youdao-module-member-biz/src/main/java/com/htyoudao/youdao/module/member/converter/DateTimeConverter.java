package com.htyoudao.youdao.module.member.converter;

import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.converters.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author dht
 */
public class DateTimeConverter implements Converter<Date> {
    private static  final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd HH:mm:ss";
    private static  final String PATTERN_YYYY_MM_DD_2 = "yyyy/MM/dd HH:mm:ss";

    @Override
    public Class<Date> supportJavaTypeKey() {
        return Date.class;
    }

    /**
     * easyExcel导出数据类型转换
     * @param cellData cellData
     * @param contentProperty contentProperty
     * @param globalConfiguration globalConfiguration
     */
    @Override
    public Date convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        String value = cellData.getStringValue();
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD_2);
        return sdf.parse(value);
    }

    /**
     * easyExcel导入Date数据类型转换
     * @param context context
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<Date> context) throws Exception {
        Date date = context.getValue();
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
        return new WriteCellData<>(sdf.format(date));
    }
}