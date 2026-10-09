package com.htyoudao.youdao.module.commodity.controller.admin.schoolProgram;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.api.VO.CommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.SchoolProgramCommoditySplitMaterialDataVO;
import com.htyoudao.youdao.module.commodity.controller.admin.materialLoss.VO.SchoolProgramCommoditySplitMaterialReqVO;
import com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO.MaterialListRespVo;
import com.htyoudao.youdao.module.commodity.service.materialLoss.RawMaterialLossRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理后台 - 学校小程序")
@RestController
@RequestMapping("/commodity/schoolProgram")
public class SchoolProgramController {

    @Autowired
    private RawMaterialLossRecordService rawMaterialLossRecordService;

    @PostMapping("/modifyCommodity")
    @Operation(summary = "学校小程序把商品拆分成原材料")
    public CommonResult<Boolean> modifyCommodity(@Valid @RequestBody SchoolProgramCommoditySplitMaterialReqVO schoolProgramCommoditySplitMaterialReqVO) {
        Boolean flag = rawMaterialLossRecordService.schoolProgramModifyCommodity(schoolProgramCommoditySplitMaterialReqVO);
        return CommonResult.success(flag);
    }




    @GetMapping("/selectInfo")
    @Operation(summary = "查看详情")
    public CommonResult<SchoolProgramCommoditySplitMaterialDataVO> selectInfo(@RequestParam("id") Long id) {
        SchoolProgramCommoditySplitMaterialDataVO dataVO = rawMaterialLossRecordService.selectInfo(id);
        return CommonResult.success(dataVO);
    }







}
