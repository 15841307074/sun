package com.htyoudao.youdao.module.bpm.controller.app.storeInspection;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.bpm.controller.app.storeInspection.vo.InspectionTemplateDropDownRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeInspection.vo.StoreGroupRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionOverviewRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionReportReqVO;
import com.htyoudao.youdao.module.bpm.service.storeInspection.template.StoreInspectionTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "app - 点检项模板")
@RestController
@RequestMapping("/bpm/store-inspection-record-reoprt")
public class InspectionTemplateController {


    @Resource
    private StoreInspectionTemplateService storeInspectionTemplateService;


    @PostMapping("/getInspectionTemplate")
    @Operation(summary = "模板下拉")
    public CommonResult<List<InspectionTemplateDropDownRespVO>> getInspectionTemplate() {
        List<InspectionTemplateDropDownRespVO> respVOList = storeInspectionTemplateService.getInspectionTemplate();
        return CommonResult.success(respVOList);
    }

    @PostMapping("/getStoreList")
    @Operation(summary = "门店列表")
    public CommonResult<List<StoreGroupRespVO>> getStoreList() {
        List<StoreGroupRespVO> respVOList = new ArrayList<>();
        return CommonResult.success(respVOList);
    }
}
