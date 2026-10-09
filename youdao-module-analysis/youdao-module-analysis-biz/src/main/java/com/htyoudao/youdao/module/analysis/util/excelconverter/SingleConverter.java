package com.htyoudao.youdao.module.analysis.util.excelconverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import org.springframework.util.ObjectUtils;

import java.text.DecimalFormat;
import java.util.Objects;


/**
 * 套餐转换器
 */
public class SingleConverter implements Converter<Integer> {


    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        String result = ObjectUtils.isEmpty(value) || Objects.equals(value, 0) || Objects.equals(value, 1) ? "否" : "是";
        return new WriteCellData<>(result);
    }

}
