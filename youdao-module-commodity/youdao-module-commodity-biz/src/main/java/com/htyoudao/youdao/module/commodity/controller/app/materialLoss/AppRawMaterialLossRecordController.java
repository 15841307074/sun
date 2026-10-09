package com.htyoudao.youdao.module.commodity.controller.app.materialLoss;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.*;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageReq;
import com.htyoudao.youdao.module.commodity.controller.app.materialStocktake.vo.MaterialStocktakePageVO;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialInventoryService;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "点餐机 - 损耗记录")
@RestController
@RequestMapping("/commodity/app/loss")
public class AppRawMaterialLossRecordController {

    @Autowired
    private RawMaterialLossRecordService rawMaterialLossRecordService;


    @Autowired
    private RawMaterialInventoryService rawMaterialInventoryService;

    @PostMapping("/listPage")
    @Operation(summary = "损耗记录列表")
    public CommonResult<PageResult<MaterialLossRecordPageVO>> lossRecordPageList(@Valid @RequestBody MaterialLossRecordPageReq pageReq){
        PageResult<MaterialLossRecordPageVO> pageResult  = rawMaterialLossRecordService.lossRecordPageList(pageReq);
        return CommonResult.success(pageResult);
    }


    @PostMapping("/delete")
    @Operation(summary = "删除损耗记录")
    public CommonResult<Boolean> deleteLossRecord(@RequestBody MaterialLossDeleteReq lossDeleteReq){
        Boolean flag = rawMaterialLossRecordService.deleteLossRecord(lossDeleteReq);
        return CommonResult.success(flag);
    }


    @PostMapping("/save")
    @Operation(summary = "新增损耗记录")
    public CommonResult<Boolean> saveLossRecord(@Valid @RequestBody MaterialLossSaveReq saveReq){
        Boolean flag = rawMaterialLossRecordService.saveLossRecord(saveReq);
        return CommonResult.success(flag);
    }


    @PostMapping("/splitCommodity")
    @Operation(summary = "把商品拆分成原材料")
    public CommonResult<MaterialOwnDataRespVo> splitCommodity(@Valid @RequestBody CommoditySplitMaterialReqVO commoditySplitMaterialReqVO) {
        MaterialOwnDataRespVo materialOwnDataRespVo = rawMaterialInventoryService.splitCommodity(commoditySplitMaterialReqVO);
        return CommonResult.success(materialOwnDataRespVo);
    }

}
