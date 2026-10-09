package com.htyoudao.youdao.module.system.service.printer;


import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterInterface;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterInterfaceMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrinterInterfaceServiceImpl implements IPrinterInterfaceService {

    @Resource
    private PrinterInterfaceMapper printerInterfaceMapper;



    /**
     * 查询打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等列表
     *
     * @param printerInterface 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等
     * @return 打印机接口列，存储打印机接口信息，包括功能、请求方式和接口值等
     */
    @Override
    public List<PrinterInterface> selectPrinterInterfaceList(PrinterInterface printerInterface)
    {
        return printerInterfaceMapper.selectPrinterInterfaceList(printerInterface);
    }


}
