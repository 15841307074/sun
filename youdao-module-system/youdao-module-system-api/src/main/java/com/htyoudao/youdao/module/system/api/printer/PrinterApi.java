package com.htyoudao.youdao.module.system.api.printer;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterSettingDTO;
import com.htyoudao.youdao.module.system.api.printer.dto.PrinterTableVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author dht
 */
@Tag(name = "RPC 服务 - 打印机")
public interface PrinterApi {


    @Operation(summary = "查询云打印机列表")
    CommonResult<List<PrinterTableVO>> getPrinterByStoreIdAndPrintType(Long storeId, Integer printerType);

    @Operation(summary = "查询打印机设置")
    CommonResult<List<PrinterSettingDTO>> selectPrinterSettingListByIds(List<Long> printerSettingIdsForArray);

}
