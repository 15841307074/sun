package com.htyoudao.youdao.module.analysis.util.excelconverter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class OrderFromConverter implements Converter<Integer> {
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
        return switch (value) {
            case 0 -> new WriteCellData<>("点餐机");
            case 1 -> new WriteCellData<>("微信小程序");
            case 2 -> new WriteCellData<>("支付宝");
            default -> new WriteCellData<>("微信小程序");
        };
    }
}
