package com.htyoudao.youdao.module.analysis.util.excelconverter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.util.Objects;

/**
 * @author dht
 */
public class PayCodeConvert implements Converter<String> {
    /**
     * easyExcel导出数据类型转换
     * @param cellData
     * @param contentProperty
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {

        return cellData.getStringValue();
    }

    /**
     * 导入转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(String value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if(ObjectUtil.isEmpty(value)){
            return new WriteCellData<>("否");
        }
        if(Objects.equals(value, "0")){
            return new WriteCellData<>("是");
        }else {
            return new WriteCellData<>("否");
        }
    }
}
