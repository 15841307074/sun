package com.htyoudao.youdao.module.analysis.util.excelconverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @description: 解决 EasyExcel 日期类型 Date 转换的问题
 * @author: dht
 * @create: 2024-06-29
 */
public class LocalDateConverter implements Converter<LocalDate> {

    private static  final String PATTERN_YYYY_MM_DD = "yyyy/MM/dd";

    @Override
    public Class<LocalDate> supportJavaTypeKey() {
        return LocalDate.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 导出
     * @param cellData
     * @param contentProperty
     * @param globalConfiguration
     * @return
     */
    @Override
    public LocalDate convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty,
                                       GlobalConfiguration globalConfiguration) {
        if(cellData.getType().equals(CellDataTypeEnum.NUMBER)){
            LocalDate localDate = LocalDate.of(1900, 1, 1);
            localDate = localDate.plusDays(cellData.getNumberValue().longValue()-2);
            return localDate;
        }else if(cellData.getType().equals(CellDataTypeEnum.STRING)) {
            return LocalDate.parse(cellData.getStringValue(), DateTimeFormatter.ofPattern(PATTERN_YYYY_MM_DD));
        }else{
            return null;
        }
    }

    /**
     * 导入
     * @param value
     * @param contentProperty
     * @param globalConfiguration
     * @return
     */
    @Override
    public WriteCellData<String> convertToExcelData(LocalDate value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        return new WriteCellData(value.format(DateTimeFormatter.ofPattern(PATTERN_YYYY_MM_DD)));
    }

}