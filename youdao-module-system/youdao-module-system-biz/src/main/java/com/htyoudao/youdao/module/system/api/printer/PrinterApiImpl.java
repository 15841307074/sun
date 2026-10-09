package com.htyoudao.youdao.module.system.api.printer;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterSettingDTO;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterTableVO;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterSetting;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterTable;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterSettingMapper;
import com.htyoudao.youdao.module.system.service.printer.IPrinterTableService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class PrinterApiImpl implements PrinterApi {

    @Resource
    private IPrinterTableService printerTableService;
    @Resource
    private PrinterSettingMapper printerSettingMapper;

    @Override

    public CommonResult<List<PrinterTableVO>> getPrinterByStoreIdAndPrintType(Long storeId, Integer printerType) {
        PrinterTable printerTableParam = new PrinterTable();
        printerTableParam.setStoreId(storeId);
        printerTableParam.setPrinterType(printerType);
        printerTableParam.setStatus(0);
        List<PrinterTable> printerTableList = printerTableService.getPrinterTableList(printerTableParam);
        List<PrinterTableVO> printerTableVOList = BeanUtils.toBean(printerTableList, PrinterTableVO.class);
        return success(printerTableVOList);
    }

    @Override
    public CommonResult<List<PrinterSettingDTO>> selectPrinterSettingListByIds(List<Long> printerSettingIdsForArray) {
        List<PrinterSetting> list = printerSettingMapper.selectPrinterSettingListByIds(printerSettingIdsForArray);
        List<PrinterSettingDTO> printerSettingDTOList = BeanUtils.toBean(list, PrinterSettingDTO.class);
        return success(printerSettingDTOList);
    }
}
