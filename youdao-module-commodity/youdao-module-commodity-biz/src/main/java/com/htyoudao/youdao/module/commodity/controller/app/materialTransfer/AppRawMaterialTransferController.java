package com.htyoudao.youdao.module.commodity.controller.app.materialTransfer;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossDeleteReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossRecordPageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossRecordPageVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossSaveReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO.*;
import com.htyoudao.youdao.module.commodity.service.materialTransfer.RawMaterialTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "点餐机 - 调拨记录")
@RestController
@RequestMapping("/commodity/app/transfer")
public class AppRawMaterialTransferController {


    @Autowired
    private RawMaterialTransferService materialTransferService;


    @PostMapping("/listPage")
    @Operation(summary = "调拨记录列表")
    public CommonResult<PageResult<MaterialTransferRecordPageVO>> transferRecordPageList(@RequestBody MaterialTransferRecordPageReq pageReq){
        PageResult<MaterialTransferRecordPageVO> pageResult = materialTransferService.transferRecordList(pageReq);
        return CommonResult.success(pageResult);
    }


    @PostMapping("/delete")
    @Operation(summary = "删除调拨记录")
    public CommonResult<Boolean> deleteTransferRecord(@RequestBody MaterialTransferDeleteReq transferDeleteReq){
        Boolean flag = materialTransferService.deleteTransferRecord(transferDeleteReq);
        return CommonResult.success(flag);
    }


    @PostMapping("/save")
    @Operation(summary = "新增调拨记录")
    public CommonResult<Boolean> saveTransferRecord(@Valid @RequestBody MaterialTransferSaveReq saveReq){
        Boolean flag = materialTransferService.saveTransferRecord(saveReq);
        return CommonResult.success(flag);
    }

    @PostMapping("/isReceive")
    @Operation(summary = "是否接收，调拨完成")
    public CommonResult<Boolean> isReceive(@RequestBody MaterialTransferReceiveReq receiveReq){
        Boolean flag = materialTransferService.isReceive(receiveReq);
        return CommonResult.success(flag);
    }
}
