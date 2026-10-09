package com.htyoudao.youdao.module.promotion.util;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * @author 33483
 */
public class CouponSourceConvert implements Converter<Integer> {


    /**
     * 导出转换
     * @param value time
     * @return WriteCellData
     */
    @Override
    public WriteCellData<String> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                                    GlobalConfiguration globalConfiguration) {
        return switch (value) {
            case 0 -> new WriteCellData<>("小程序链接领券");
            case 1 -> new WriteCellData<>("H5链接领券");
            case 2 -> new WriteCellData<>("短信链接领券");
            case 3 -> new WriteCellData<>("小程序内部领券(banner)");
            case 4 -> new WriteCellData<>("小程序内部领券(弹窗)");
            case 5 -> new WriteCellData<>("小程序内部领券(分享)");
            case 6 -> new WriteCellData<>("推广");
            case 7 -> new WriteCellData<>("积分商城");
            case 8 -> new WriteCellData<>("抽奖");
            case 9 -> new WriteCellData<>("抖音");
            case 10 -> new WriteCellData<>("自动发放");
            case 11 -> new WriteCellData<>("秒杀");
            default -> new WriteCellData<>("小程序链接领券");
        };
    }
}
