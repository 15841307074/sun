package com.htyoudao.youdao.module.commodity.controller.admin.materialTransfer;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialTypeVo;
import com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO.MaterialTransferRecordPageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialTransfer.VO.MaterialTransferRecordPageVO;
import com.htyoudao.youdao.module.commodity.enums.TransferStatus;
import com.htyoudao.youdao.module.commodity.service.materialTransfer.RawMaterialTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理后台 - 调拨记录")
@RestController
@RequestMapping("/commodity/transfer")
public class RawMaterialTransferController {


    @Autowired
    private RawMaterialTransferService materialTransferService;
    @PostMapping("/list")
    @Operation(summary = "调拨记录列表")
    public CommonResult<PageResult<MaterialTransferRecordPageVO>> transferRecordList(@RequestBody MaterialTransferRecordPageReq pageReq){

        PageResult<MaterialTransferRecordPageVO> pageResult = materialTransferService.transferRecordList(pageReq);
        return CommonResult.success(pageResult);
    }


    @GetMapping("/transferStatusList")
    @Operation(summary = "调拨状态列表")
    public CommonResult<List<MaterialTypeVo>> transferStatusList(){

        List<MaterialTypeVo> materialTypeVos = materialTransferService.transferStatusList();
        return CommonResult.success(materialTypeVos);
    }


    @GetMapping("/selectInfo")
    @Operation(summary = "调拨记录列表")
    public CommonResult<MaterialTransferRecordPageVO> selectInfo(@RequestParam Long id){

        MaterialTransferRecordPageVO materialTransferRecordPageVO = materialTransferService.selectInfo(id);
        return CommonResult.success(materialTransferRecordPageVO);
    }
}
