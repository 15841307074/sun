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
public class MemberStatusConverter implements Converter<Integer> {


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
        // 会员状态 0正常 1冻结 2待注销 3已注销
        String stringValue = cellData.getStringValue();
        if (ObjectUtil.isEmpty(stringValue)){
            return 0;
        }else if ("正常".equals(stringValue)){
            return 0;
        }else if("冻结".equals(stringValue)){
            return 1;
        }else if("待注销".equals(stringValue)){
            return 2;
        }else if("已注销".equals(stringValue)){
            return 3;
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
            return new WriteCellData<>("正常");
        } else if (value == 0) {
            return new WriteCellData<>("正常");
        } else if(value == 1){
            return new WriteCellData<>("冻结");
        } else if(value == 2){
            return new WriteCellData<>("待注销");
        } else if(value == 3){
            return new WriteCellData<>("已注销");
        }else {
            return new WriteCellData<>("正常");
        }
    }
}
