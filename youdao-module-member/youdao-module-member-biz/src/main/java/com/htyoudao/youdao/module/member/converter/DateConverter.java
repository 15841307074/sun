package com.htyoudao.youdao.module.member.converter;

import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.converters.Converter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description: 解决 EasyExcel 日期类型 Date 转换的问题
 * @author: dht
 * @create: 2024-06-29
 */
public class DateConverter implements Converter<Date>{


    private static  final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    private static  final String PATTERN_YYYY_MM_DD_2 = "yyyy/MM/dd";

    @Override
    public Class<Date> supportJavaTypeKey() {
        return Date.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * easyExcel导出数据类型转换
     * @param cellData cellData
     * @param contentProperty contentProperty
     * @param globalConfiguration globalConfiguration
     * @return Date
     */
    @Override
    public Date convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                  GlobalConfiguration globalConfiguration) throws Exception {
        String value = cellData.getStringValue();
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD_2);
        return sdf.parse(value);
    }

    /**
     * 导入转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<Date> value) {
        Date date = value.getValue();
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD);
        return new WriteCellData<>(sdf.format(date));
    }
}
