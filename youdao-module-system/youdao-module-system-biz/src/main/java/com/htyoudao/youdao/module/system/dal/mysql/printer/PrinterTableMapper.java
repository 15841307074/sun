package com.htyoudao.youdao.module.system.dal.mysql.printer;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrinterTableMapper extends BaseMapper<PrinterTable> {


    /**
     * 查询门店打印机列表
     *
     * @param printerTable 门店打印机
     * @return 门店打印机集合
     */
    Page<PrinterTable> selectPrinterTableListPage(@Param("page") Page page,@Param("printerTable") PrinterTable printerTable);


    List<PrinterTable> selectPrinterTableList(@Param("printerTable") PrinterTable printerTable);

    /**
     * 新增门店打印机
     *
     * @param printerTable 门店打印机
     * @return 结果
     */
    Long insertPrinterTable(@Param("printerTable") PrinterTable printerTable);



    /**
     * 删除门店打印机
     *
     * @param printerTableId 门店打印机主键
     * @return 结果
     */
    int deletePrinterTableByPrinterTableId(@Param("printerTableId") Long printerTableId);

    /**
     * 修改门店打印机
     *
     * @param printerTable 门店打印机
     * @return 结果
     */
    int updatePrinterTable(@Param("printerTable") PrinterTable printerTable);


    PrinterTable selectPrinterTableByPrinterTableId(Long printerTableId);
}
