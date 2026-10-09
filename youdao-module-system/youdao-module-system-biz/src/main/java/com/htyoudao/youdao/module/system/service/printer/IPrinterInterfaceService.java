package com.htyoudao.youdao.module.system.service.printer;




import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterInterface;

import java.util.List;

/**
 * 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等Service接口
 * 
 * @author ruoyi
 * @date 2024-03-28
 */
public interface IPrinterInterfaceService 
{

    /**
     * 查询打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等列表
     * 
     * @param printerInterface 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等
     * @return 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等集合
     */
    public List<PrinterInterface> selectPrinterInterfaceList(PrinterInterface printerInterface);


}
