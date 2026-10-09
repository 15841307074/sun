package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.service.storeinspectionrecord.StoreInspectionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "app - 巡店记录")
@RestController
@RequestMapping("/bpm/store-inspection-record")
@Validated
public class StoreInspectionRecordController {

    @Resource
    private StoreInspectionRecordService storeInspectionRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建巡店记录主")
    public CommonResult<Long> createStoreInspectionRecord(@Valid @RequestBody StoreInspectionRecordSaveReqVO createReqVO) {
        return success(storeInspectionRecordService.createStoreInspectionRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新巡店记录主")
    public CommonResult<Boolean> updateStoreInspectionRecord(@Valid @RequestBody StoreInspectionRecordSaveReqVO updateReqVO) {
        storeInspectionRecordService.updateStoreInspectionRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除巡店记录主")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteStoreInspectionRecord(@RequestParam("id") Long id) {
        storeInspectionRecordService.deleteStoreInspectionRecord(id);
        return success(true);
    }

    @PostMapping("/get")
    @Operation(summary = "获得巡店记录")
    public CommonResult<StoreInspectionRecordDetailRespVO> getStoreInspectionRecord(@Valid @RequestBody StoreInspectionRecordDetailReqVO reqVO) {
        return success(storeInspectionRecordService.getStoreInspectionRecord(reqVO));
    }

    @PostMapping("/page")
    @Operation(summary = "获得巡店记录主分页")
    public CommonResult<PageResult<IndividualInspectionRecordRespVO>> getStoreInspectionRecordPage(@Valid @RequestBody StoreInspectionRecordPageReqVO pageReqVO) {
        PageResult<StoreInspectionRecordDO> pageResult = storeInspectionRecordService.getStoreInspectionRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IndividualInspectionRecordRespVO.class));
    }


    @PostMapping("/getRecordByStoreId")
    @Operation(summary = "通过门店查看巡店记录")
    public CommonResult<StoreInspectionRecordRespVO> getRecordByStoreId(@Valid @RequestBody StoreInspectionRecordReqVO reqVO) {
        return success(storeInspectionRecordService.getRecordByStoreId(reqVO));
    }

    @PutMapping("/overInspectionRecord")
    @Operation(summary = "完成巡检", description = "完成巡检")
    public CommonResult<Boolean> overInspectionRecord(@Valid @RequestBody OverInspectionRecordReqVO reqVO) {
        storeInspectionRecordService.overInspectionRecord(reqVO);
        return CommonResult.success(Boolean.TRUE);
    }

    @GetMapping("/getItemByRecordId")
    @Operation(summary = "通过recordId查看巡店记录")
    @Parameters({
            @Parameter(name = "recordId", description = "recordId", required = true, example = "1214737")
    })
    public CommonResult<StoreInspectionRecordDetailRespVO> getItemByRecordId(@RequestParam("recordId") Long recordId) {
        return success(storeInspectionRecordService.getItemByRecordId(recordId));
    }

    @PostMapping("/createAll")
    @Operation(summary = "所有门店插入一波 后端自用")
    public CommonResult<Long> createAll() {
        return success(storeInspectionRecordService.createAll());
    }
}