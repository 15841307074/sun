package com.htyoudao.youdao.module.system.service.printer;


import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterSetting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 打印机设置Service接口
 * 
 * @author Qizhongnan
 * @date 2024-03-26
 */
public interface IPrinterSettingService 
{

    /**
     * 新增打印机设置
     * 
     * @param printerSetting 打印机设置
     * @return 结果
     */
    public Long insertPrinterSetting(PrinterSetting printerSetting);
    /**
     * 删除打印机设置
     *
     */
    public void delPrinterSetting(List<Long> printerSettingIds  );

    /**
     * 修改打印机设置
     *
     * @param printerSetting 打印机设置
     * @return 结果
     */
    public int updatePrinterSetting(PrinterSetting printerSetting);


    List<PrinterSetting> selectPrinterSettingListByIds( List<Long> printerSettingIdsForArray);


}
