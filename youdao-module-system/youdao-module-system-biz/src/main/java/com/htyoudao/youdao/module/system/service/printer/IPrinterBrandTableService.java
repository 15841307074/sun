package com.htyoudao.youdao.module.system.service.printer;

import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterBrandTable;

import java.util.List;

/**
 * 打印机品牌，存储打印机品牌信息Service接口
 * 
 * @author Qizhongnan
 * @date 2024-03-28
 */
public interface IPrinterBrandTableService 
{

    /**
     * 查询打印机品牌，存储打印机品牌信息列表
     * 
     * @param printerBrandTable 打印机品牌，存储打印机品牌信息
     * @return 打印机品牌，存储打印机品牌信息集合
     */
    public List<PrinterBrandTable> selectPrinterBrandTableList(PrinterBrandTable printerBrandTable);



}
