package com.htyoudao.youdao.module.promotion.util.converter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.htyoudao.youdao.module.promotion.enums.ActivityExchangeAwardType;


/**
 * @author dht
 * @description: 集点兑换物的转换
 */
public class AwardTypeConverter implements Converter<Integer> {

    /**
     * 导入转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<Integer> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        if(ObjectUtil.isEmpty(value)){
            return new WriteCellData<>(ActivityExchangeAwardType.getByCode(0).getDescription());
        }
        return new WriteCellData<>(ActivityExchangeAwardType.getByCode(value).getDescription());
    }
}