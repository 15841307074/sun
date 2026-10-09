package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.StoreInspectionRecordItemSaveReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionItemStatRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionOverviewRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.InspectionReportReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo.StoreInspectionIntervalRespVO;
import com.htyoudao.youdao.module.bpm.service.storeinspectionrecord.StoreInspectionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "app - 巡店记录报表")
@RestController
@RequestMapping("/bpm/store-inspection-record-reoprt")
public class StoreInspectionRecordReportController {

    @Resource
    private StoreInspectionRecordService storeInspectionRecordService;


    @PostMapping("/getInspectionOverview")
    @Operation(summary = "巡店概览")
    public CommonResult<PageResult<InspectionOverviewRespVO>> getInspectionOverview(@Valid @RequestBody InspectionReportReqVO reqVO) {
        return CommonResult.success(storeInspectionRecordService.getInspectionOverview(reqVO));
    }

    @PostMapping("/getInspectionItemStat")
    @Operation(summary = "不合格点检项")
    public CommonResult<PageResult<InspectionItemStatRespVO>> getInspectionItemStat(@Valid @RequestBody InspectionReportReqVO reqVO) {
        return CommonResult.success(storeInspectionRecordService.getInspectionItemStat(reqVO));
    }

    @PostMapping("/getInspectionGroupStat")
    @Operation(summary = "不合格点检大项")
    public CommonResult<PageResult<InspectionItemStatRespVO>> getInspectionGroupStat(@Valid @RequestBody InspectionReportReqVO reqVO) {
        return CommonResult.success(storeInspectionRecordService.getInspectionGroupStat(reqVO));
    }

    @PostMapping("/getStoreInspectionInterval")
    @Operation(summary = "巡店间隔分布")
    public CommonResult<PageResult<StoreInspectionIntervalRespVO>> getStoreInspectionInterval(@Valid @RequestBody InspectionReportReqVO reqVO) {
        return CommonResult.success(storeInspectionRecordService.getStoreInspectionInterval(reqVO));
    }

}
