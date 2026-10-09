package com.htyoudao.youdao.module.analysis.util.excelconverter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeConverter implements Converter<Date> {
    private static  final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd HH:mm:ss";
    private static  final String PATTERN_YYYY_MM_DD_2 = "yyyy/MM/dd HH:mm:ss";

    @Override
    public Class<Date> supportJavaTypeKey() {
        return Date.class;
    }

    /**
     * easyExcel导出数据类型转换
     * @param cellData
     * @param contentProperty
     * @param globalConfiguration
     */
    @Override
    public Date convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        String value = cellData.getStringValue();
        if(ObjectUtil.isEmpty(value)){
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_YYYY_MM_DD_2);
        Date parse = sdf.parse(value);
        return parse;
    }

    /**
     * easyExcel导入Date数据类型转换
     * @param context
     * @return
     * @throws Exception
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
