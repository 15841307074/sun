package com.htyoudao.youdao.module.system.service.printer;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrintConfigVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrinterTomplateReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterTable;
import jakarta.validation.Valid;

import java.util.List;

public interface IPrinterTableService extends IService<PrinterTable> {



    /**
     * 查询门店打印机列表
     *
     * @param printerTable 门店打印机
     * @return 门店打印机集合
     */
    Page<PrinterTable> selectPrinterTableList(PrinterTable printerTable);


    /**
     * 新增门店打印机
     *
     * @param printerTable 门店打印机
     * @return 结果
     */
    boolean insertPrinterTable(@Valid PrinterTable printerTable);


    /**
     * 删除门店打印机信息
     *
     * @param printerTableId 门店打印机主键
     * @return 结果
     */
    boolean deletePrinterTableByPrinterTableId(Long printerTableId);

    boolean delPrinterQueue(PrinterTable printerTable);
    /**
     * 查询门店打印机列表
     *
     * @param printerTable 门店打印机
     * @return 门店打印机集合
     */
    List<PrinterTable> getPrinterTableList(PrinterTable printerTable);

    /**
     * 修改打印机打印内容
     * @param printerTable
     */
    void updatePrinter(PrinterTable printerTable);
    /**
     * 修改门店小票模板设置
     */
    void updatePrinterTemplate(PrinterTomplateReqVO printerTomplateReqVO);
    /**
     * 获取模板
     */
    PrintConfigVO getPrinterTemplate(Long  storeId);

    Boolean updatePrinterTable(@Valid PrinterTable printerTable);

    /**
     * 查询门店打印机
     *
     * @param printerTableId 门店打印机主键
     * @return 门店打印机
     */
    PrinterTable selectPrinterTableByPrinterTableId(Long printerTableId);

    PageResult<PrinterTable> selectByPageList(Long storeId);

    PageResult<PrinterTable> selectByPageListApp(Long storeId);
}
