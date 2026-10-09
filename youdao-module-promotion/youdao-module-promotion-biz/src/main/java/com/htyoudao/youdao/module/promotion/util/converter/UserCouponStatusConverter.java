package com.htyoudao.youdao.module.promotion.util.converter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.htyoudao.youdao.module.promotion.enums.UserCouponStatusEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.UserRestrictionsEnum;

/**
 * @author dht
 */
public class UserCouponStatusConverter implements Converter<Integer> {

    /**
     * 导出转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<Integer> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                     GlobalConfiguration globalConfiguration) {
        if(ObjectUtil.isEmpty(value)){
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(UserCouponStatusEnum.getByCode(value));
    }
}
