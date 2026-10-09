package com.htyoudao.youdao.module.analysis.util.excelconverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import de.danielbechler.diff.category.CategoryConfigurer.Of;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class RateConverter implements Converter<Double> {

    /**
     * 导入转换
     *
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(Double value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (value == null || value == 0.0){
            return new WriteCellData<>("0%");
        }

        NumberFormat percentFormat = NumberFormat.getPercentInstance(Locale.CHINA);
        percentFormat.setMinimumFractionDigits(2); // 最少两位小数
        percentFormat.setMaximumFractionDigits(2); // 最多两位小数
        String formatted = percentFormat.format(value);
        return new WriteCellData<>(formatted);
    }


}
