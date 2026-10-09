package com.htyoudao.youdao.module.system.dal.mysql.printer;

import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterInterface;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrinterInterfaceMapper {


    /**
     * 查询打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等列表
     *
     * @param printerInterface 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等
     * @return 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等集合
     */
    public List<PrinterInterface> selectPrinterInterfaceList(@Param("printerInterface") PrinterInterface printerInterface);



}
