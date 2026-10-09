package com.htyoudao.youdao.module.system.dal.mysql.printer;


import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterBrandTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrinterBrandTableMapper {



    /**
     * 查询打印机品牌，存储打印机品牌信息列表
     *
     * @param printerBrandTable 打印机品牌，存储打印机品牌信息
     * @return 打印机品牌，存储打印机品牌信息集合
     */
    public List<PrinterBrandTable> selectPrinterBrandTableList(@Param("printerBrandTable") PrinterBrandTable printerBrandTable);


}
