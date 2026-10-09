package com.htyoudao.youdao.module.system.service.printer;


import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterSetting;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterSettingMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.util.string.ConvertUtil.convertListToString;


@Service
public class PrinterSettingServiceImpl implements IPrinterSettingService {

    @Resource
    private PrinterSettingMapper printerSettingMapper;




    /**
     * 新增打印机设置
     *
     * @param printerSetting 打印机设置
     * @return 结果
     */
    @Override
    public Long insertPrinterSetting(PrinterSetting printerSetting)
    {
        if (printerSetting.getPrintableChannelS() != null){
            List<Integer> printableChannelS = printerSetting.getPrintableChannelS();
            String printableChannelString = convertListToString(printableChannelS);
            printerSetting.setPrintableChannel(printableChannelString);
        }


        return printerSettingMapper.insertPrinterSetting(printerSetting);
    }
    /**
     * 删除打印机设置
     *
     * @return 结果
     */
    @Override
    public void delPrinterSetting(List<Long> printerSettingIds ) {
        printerSettingMapper.deleteByIds(printerSettingIds);
    }
    @Override
    public int updatePrinterSetting(PrinterSetting printerSetting)
    {

        if (printerSetting.getPrintableChannelS() != null){
            List<Integer> printableChannelS = printerSetting.getPrintableChannelS();
            String printableChannelString = convertListToString(printableChannelS);
            printerSetting.setPrintableChannel(printableChannelString);
        }
        return printerSettingMapper.updatePrinterSetting(printerSetting);
    }

    @Override
    public List<PrinterSetting> selectPrinterSettingListByIds(List<Long> printerSettingIdsForArray) {
        return printerSettingMapper.selectPrinterSettingListByIds(printerSettingIdsForArray);
    }


    // 将 List<Integer> 转成 string
    private static String convertListToString(List<Integer> integerList) {
        // 使用逗号连接 List<Integer> 中的元素
        return integerList.stream().map(String::valueOf).collect(Collectors.joining(","));
    }


}
