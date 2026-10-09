package com.htyoudao.youdao.module.member.converter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * @author dht
 */
public class GenderConverter implements Converter<Integer> {
    /**
     * easyExcel导出数据类型转换
     * @param cellData cellData
     * @param contentProperty contentProperty
     * @param globalConfiguration globalConfiguration
     * @return Integer
     */
    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {

        String stringValue = cellData.getStringValue();
        if (ObjectUtil.isEmpty(stringValue)){
            return 0;
        }else if ("男".equals(stringValue)){
            return 1;
        }else if("女".equals(stringValue)){
            return 2;
        }else {
            return 0;
        }
    }

    /**
     * 导入转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if (value == null){
            return new WriteCellData<>("保密");
        } else if (value == 1) {
            return new WriteCellData<>("男");
        }else if(value == 2){
            return new WriteCellData<>("女");
        }else {
            return new WriteCellData<>("保密");
        }
    }
}