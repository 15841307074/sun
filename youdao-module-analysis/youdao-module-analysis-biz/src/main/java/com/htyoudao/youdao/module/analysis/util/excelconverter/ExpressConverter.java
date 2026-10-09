package com.htyoudao.youdao.module.analysis.util.excelconverter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import jakarta.validation.constraints.NotNull;

public class ExpressConverter implements Converter<Long> {
    /**
     * easyExcel导出数据类型转换
     * @param cellData
     * @param contentProperty
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {

        String stringValue = cellData.getStringValue();
        if (ObjectUtil.isEmpty(stringValue)){
            return 2L;
        }else if ("经营中".equals(stringValue)){
            return 1L;
        }else {
            return 2L;
        }
    }

    /**
     * 导入转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<Long> convertToExcelData(Long value, ExcelContentProperty contentProperty,
                                                  GlobalConfiguration globalConfiguration) {
        int intValue = value != null ? value.intValue() : 0;
        return switch (intValue) {
            case 0 -> new WriteCellData<>("老客");
            default -> new WriteCellData<>("新客");
        };
    }
}