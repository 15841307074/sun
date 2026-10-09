package com.htyoudao.youdao.module.system.service.printer;


import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterBrandTable;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterBrandTableMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrinterBrandTableServiceImpl implements IPrinterBrandTableService {

    @Resource
    private PrinterBrandTableMapper printerBrandTableMapper;


    /**
     * 查询打印机品牌，存储打印机品牌信息列表
     *
     * @param printerBrandTable 打印机品牌，存储打印机品牌信息
     * @return 打印机品牌，存储打印机品牌信息
     */
    @Override
    public List<PrinterBrandTable> selectPrinterBrandTableList(PrinterBrandTable printerBrandTable)
    {
        return printerBrandTableMapper.selectPrinterBrandTableList(printerBrandTable);
    }


}
