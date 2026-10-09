package com.htyoudao.youdao.module.system.dal.mysql.printer;


import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrinterSettingMapper extends BaseMapperX<PrinterSetting> {




    /**
     * 新增打印机设置
     *
     * @param printerSetting 打印机设置
     * @return 结果
     */
    public Long insertPrinterSetting(@Param("printerSetting") PrinterSetting printerSetting);


    int updatePrinterSetting(@Param("printerSetting") PrinterSetting printerSetting);

    List<PrinterSetting> selectPrinterSettingListByIds(@Param("printerSettingIdsForArray") List<Long> printerSettingIdsForArray);
}
