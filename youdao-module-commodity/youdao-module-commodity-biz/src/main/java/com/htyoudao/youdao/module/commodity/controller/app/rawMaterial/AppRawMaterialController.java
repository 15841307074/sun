package com.htyoudao.youdao.module.commodity.controller.app.rawMaterial;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialListRespVo;
import com.htyoudao.youdao.module.commodity.controller.app.rawMaterial.VO.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.invertory.RawMaterial;
import com.htyoudao.youdao.module.commodity.dal.dto.inventory.StockChangeDTO;
import com.htyoudao.youdao.module.commodity.service.inventory.IRawMaterialService;
import com.htyoudao.youdao.module.commodity.service.inventory.RowMaterialStockFlowService;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "点餐机 - 门店原材料")
@RestController
@RequestMapping("/commodity/material/app")
public class AppRawMaterialController {

    @Resource
    private IRawMaterialService rawMaterialService;

    @Resource
    private RowMaterialStockFlowService stockFlowService;

    @Autowired
    private RawMaterialLossRecordService rawMaterialLossRecordService;

    @PostMapping("/add")
    @Operation(summary = "新增原材料")
    public CommonResult<Boolean> addMateria(@Valid @RequestBody List<SaveMaterialReqVO> saveMaterialReqVOs) {
        rawMaterialService.batchSaveOrUpdate(saveMaterialReqVOs);
        return CommonResult.success(true);
    }

    @PermitAll //TODO 临时放开
    @PostMapping("/gyl/add")
    @Operation(summary = "新增原材料")
    public CommonResult<Boolean> addMateriaForGyl(@Valid @RequestBody List<SaveMaterialReqVO> saveMaterialReqVOs) {
        rawMaterialService.batchSaveOrUpdate(saveMaterialReqVOs);
        return CommonResult.success(true);
    }

    @PermitAll //TODO 临时放开
    @PostMapping("/gyl/changeStock")
    @Operation(summary = "扣减/增加 库存接口")
    public CommonResult<Boolean> changeStockForGyl(@Valid @RequestBody StockChangeDTO stockChangeDTO) {
        return CommonResult.success(stockFlowService.changeStock(stockChangeDTO));
    }



    @PostMapping("/changeStock")
    @Operation(summary = "扣减/增加 库存接口")
    public CommonResult<Boolean> changeStock(@Valid @RequestBody StockChangeDTO stockChangeDTO) {
        return CommonResult.success(stockFlowService.changeStock(stockChangeDTO));
    }



    @PostMapping("/modifyCommodity")
    @Operation(summary = "把商品拆分成原材料")
    public CommonResult<List<MaterialListRespVo>> modifyCommodity(@Valid @RequestBody CommoditySplitMaterialReqVO commoditySplitMaterialReqVO) {
        List<MaterialListRespVo> list = rawMaterialLossRecordService.modifyCommodity(commoditySplitMaterialReqVO);
        return CommonResult.success(list);
    }


    @PostMapping("/list")
    @Operation(summary = "门店原材料查询")
    public CommonResult<RawMaterialRespVO> queryMaterialList(@Valid @RequestBody SelectMaterialListReqVO reqVO) {
        return CommonResult.success(rawMaterialService.queryMaterialList(reqVO));
    }

    @PostMapping("/modifyStatus")
    @Operation(summary = "修改门店原材料上下架状态")
    public CommonResult<Boolean> modifyStatus(@Valid @RequestBody ModifyStatusVO modifyStatusVO) {
        return CommonResult.success(rawMaterialService.modifyStatus(modifyStatusVO.getId(), modifyStatusVO.getIsEnable()));
    }

    @DeleteMapping("/remove")
    @Operation(summary = "删除门店原材料")
    public CommonResult<Boolean> deleteMaterial(@RequestParam("id") Long id) {
        return CommonResult.success(rawMaterialService.removeById(id));
    }







}
