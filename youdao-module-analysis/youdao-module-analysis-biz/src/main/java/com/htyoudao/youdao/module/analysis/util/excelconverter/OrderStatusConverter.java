package com.htyoudao.youdao.module.analysis.util.excelconverter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.htyoudao.youdao.module.analysis.enums.OrderStateEnum;

import java.util.Objects;


public class OrderStatusConverter implements Converter<Integer> {
    /**
     * easyExcel导出数据类型转换
     * @param cellData
     * @param contentProperty
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {

        String stringValue = cellData.getStringValue();
        if (ObjectUtil.isEmpty(stringValue)){
            return 2;
        }else if ("经营中".equals(stringValue)){
            return 1;
        }else {
            return 2;
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
        if(ObjectUtil.isEmpty( value)){
            return new WriteCellData<>("已取消");
        }
        return new WriteCellData<>(OrderStateEnum.getMessageByCode(value));
    }
}