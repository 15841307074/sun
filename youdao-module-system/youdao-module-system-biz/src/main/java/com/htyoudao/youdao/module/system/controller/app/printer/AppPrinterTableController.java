package com.htyoudao.youdao.module.system.controller.app.printer;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrintAllInfoVo;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrintConfigVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.PrinterTomplateReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.printer.PrinterTable;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterSettingMapper;
import com.htyoudao.youdao.module.system.dal.mysql.printer.PrinterTableMapper;
import com.htyoudao.youdao.module.system.service.printer.IPrinterTableService;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;


@RestController
@RequestMapping("/system/printer")
@Tag(name = "门店打印机", description = "门店打印机")
@Validated
public class AppPrinterTableController {
    @Resource
    private IPrinterTableService printerTableService;

    @Resource
    private PrinterTableMapper printerTableMapper;

    @Resource
    private PrinterSettingMapper printerSettingMapper;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;



    @Operation(summary = "查询门店打印机列表")
    @GetMapping("/list")
    public CommonResult<PageResult<PrinterTable>> list(@RequestParam("storeId")Long storeId) {

        PageResult<PrinterTable> pageResult = printerTableService.selectByPageList(storeId);

        return CommonResult.success(pageResult);
    }

    @Operation(summary = "查询门店打印机列表")
    @GetMapping("/listApp")
    public CommonResult<PageResult<PrinterTable>> listApp(@RequestParam("storeId")Long storeId) {

        PageResult<PrinterTable> pageResult = printerTableService.selectByPageListApp(storeId);

        return CommonResult.success(pageResult);
    }




    @PostMapping
    @Operation(summary = "新增门店打印机")
    public CommonResult<Boolean> add(@Valid @RequestBody PrinterTable printerTable) {
        return CommonResult.success(printerTableService.insertPrinterTable(printerTable));
    }


    @PostMapping("/removePrinter")
    @Operation(summary = "删除门店打印机")
    public CommonResult<Boolean> remove(@RequestBody PrinterTable printerTable) {
        return CommonResult.success(printerTableService.deletePrinterTableByPrinterTableId(printerTable.getPrinterTableId()));
    }


    @PostMapping("/delPrinterQueue")
    @Operation(summary = "删除打印队列")
    public CommonResult<Boolean> delPrinterQueue(@RequestBody PrinterTable printerTable) {

        return CommonResult.success(printerTableService.delPrinterQueue(printerTable));
    }


    @PostMapping("/edit")
    @Operation(summary = "修改门店打印机")
    public CommonResult<Boolean> edit(@Valid @RequestBody PrinterTable printerTable) {
        return CommonResult.success(printerTableService.updatePrinterTable(printerTable));
    }


    @PostMapping("/editStatus")
    @Operation(summary = "修改门店打印机状态")
    public CommonResult<Boolean> editStatus(@RequestBody PrinterTable printerTable) {

        UpdateWrapper<PrinterTable> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("printer_table_id",printerTable.getPrinterTableId());
        updateWrapper.set("status",printerTable.getStatus());
        int update = printerTableMapper.update(updateWrapper);


        return CommonResult.success(true);
    }



    @Operation(summary = "获取门店下的打印机信息")
    @GetMapping("/getPrinterByStoreIdAndPrintType")
    public CommonResult<List<PrinterTable>> getPrinterByStoreIdAndPrintType(@RequestParam("storeId") Long storeId, @RequestParam("printerType") Integer printerType) {
        PrinterTable printerTableParam = new PrinterTable();
        printerTableParam.setStoreId(storeId);
        printerTableParam.setPrinterType(printerType);
        return CommonResult.success(printerTableService.getPrinterTableList(printerTableParam));
    }

    @GetMapping(value = "/{printerTableId}")
    @Operation(summary = "获取门店打印机详细信息")
    public CommonResult<PrinterTable> getInfo(@PathVariable("printerTableId") Long printerTableId) {
        return CommonResult.success(printerTableService.selectPrinterTableByPrinterTableId(printerTableId));
    }

    /**
     * 根据小票类型获取小票模版信息-点餐机
     */
    @GetMapping("/selectByStorePrinterInfo")
    @Operation(summary = "根据小票类型获取小票模版信息-点餐机")
    public CommonResult<PrintAllInfoVo>  selectByStorePrinterInfo(PrinterTomplateReqVO printerTomplateReqVO){
        return success(systemStoreInfoService.selectByStorePrinterInfo(printerTomplateReqVO));
    }


    @PostMapping("/updatePrinterTemplate")
    @Operation(summary = "门店模板设置")
    public CommonResult<Boolean> updatePrinterTemplate(@RequestBody PrinterTomplateReqVO printerTomplateReqVO) {
        printerTableService.updatePrinterTemplate(printerTomplateReqVO);
        return CommonResult.success(true);
    }

    @GetMapping("/getPrinterTemplate")
    @Operation(summary = "门店模板查询")
    @PermitAll
    public CommonResult<PrintConfigVO> getPrinterTemplate(@RequestParam("storeId") Long storeId) {
        return CommonResult.success( printerTableService.getPrinterTemplate(storeId));
    }


    @PutMapping("/updatePrinter")
    @Operation(summary = "修改打印机打印内容")
    @PermitAll
    public CommonResult<Boolean> updatePrinter(@RequestBody PrinterTable printerTable) {
        printerTableService.updatePrinter(printerTable);
        return CommonResult.success(true);
    }


}
