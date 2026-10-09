package com.htyoudao.youdao.module.commodity.controller.admin.materialLoss;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossRecordPageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialLossRecordPageVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialTypeVo;
import com.htyoudao.youdao.module.commodity.enums.LossType;
import com.htyoudao.youdao.module.commodity.enums.TransferStatus;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "管理后台 - 损耗记录")
@RestController
@RequestMapping("/commodity/loss")
public class RawMaterialLossRecordController {


    @Autowired
    private RawMaterialLossRecordService rawMaterialLossRecordService;

    @PostMapping("/list")
    @Operation(summary = "损耗记录列表")
    public CommonResult<PageResult<MaterialLossRecordPageVO>> lossRecordList(@RequestBody MaterialLossRecordPageReq pageReq){
        PageResult<MaterialLossRecordPageVO> pageResult = rawMaterialLossRecordService.lossRecordPageList(pageReq);
        return CommonResult.success(pageResult);
    }

    @GetMapping("/lossTypeList")
    @Operation(summary = "损耗类型列表")
    public CommonResult<List<MaterialTypeVo>> lossTypeList(){

        List<MaterialTypeVo> materialTypeVos = rawMaterialLossRecordService.lossTypeList();
        return CommonResult.success(materialTypeVos);
    }


    @GetMapping("/updateUninventoried")
    @Operation(summary = "更新损耗记录单")
    public CommonResult<BigDecimal> updateUninventoriedLossRecord(@RequestParam Long takeId, @RequestParam Long storeId){

        BigDecimal bigDecimal = rawMaterialLossRecordService.updateUninventoriedLossRecord(takeId, storeId);
        return CommonResult.success(bigDecimal);
    }
}
