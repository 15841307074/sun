package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeInspection.checklist.StoreInspectionChecklistDO;
import com.htyoudao.youdao.module.bpm.service.storeInspection.checklist.StoreInspectionChecklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 点检项目") // 流程实例，通过流程定义创建的一次“申请”
@RestController
@RequestMapping("/bpm/checklist")
@Validated
public class ChecklistController {

    @Resource
    private StoreInspectionChecklistService checklistService;

    @PostMapping("/create")
    @Operation(summary = "创建点检项")
    public CommonResult<Integer> create(@Valid @RequestBody CheckListReqVO checkListReqVO) {
        return success(checklistService.create(checkListReqVO));
    }

    @PostMapping("/update")
    @Operation(summary = "更新点检项")
    public CommonResult<Integer> update(@Valid @RequestBody CheckListReqVO checkListReqVO) {
        return success(checklistService.update(checkListReqVO));
    }

    @GetMapping("/deleteChecklistById")
    @Operation(summary = "删除点检项")
    public CommonResult<Integer> deleteChecklistById(@Valid @NotNull Long checklistId) {
        return success(checklistService.deleteChecklistById(checklistId));
    }

    @GetMapping("/getAllType")
    @Operation(summary = "获取所有点检项大类")
    public CommonResult<List<StoreInspectionTypeRespVO>> getAllType(String typeName) {
        return success(checklistService.getAllType(typeName));
    }

    @GetMapping("/deleteTypeById")
    @Operation(summary = "删除点检项大类")
    public CommonResult<Integer> deleteTypeById(@Valid @NotNull Long typeId) {
        return success(checklistService.deleteTypeById(typeId));
    }

    @PostMapping("/createType")
    @Operation(summary = "创建大类")
    public CommonResult<Integer> createType(@Valid @RequestBody StoreInspectionTypeReqVO storeInspectionTypeReqVO) {
        return success(checklistService.createType(storeInspectionTypeReqVO));
    }

    @PostMapping("/updateType")
    @Operation(summary = "更新大类")
    public CommonResult<Integer> updateType(@Valid @RequestBody StoreInspectionTypeReqVO storeInspectionTypeReqVO) {
        return success(checklistService.updateType(storeInspectionTypeReqVO));
    }

    @GetMapping("/getChecklistsByTypeId")
    @Operation(summary = "获取大类下所有点检项")
    public CommonResult<List<CheckListRespVO>> getChecklistsByTypeId(@Valid @NotNull Long typeId) {
        return success(checklistService.getChecklistsByTypeId(typeId));
    }

    @PostMapping("/getAllChecklists")
    @Operation(summary = "获取所有点检项")
    public CommonResult<PageResult<CheckListRespVO>> getAllChecklists(@Valid @RequestBody CheckListsQueryReqVO checkListsQueryReqVO) {
        return success(checklistService.getAllChecklists(checkListsQueryReqVO));
    }

    @PostMapping("/sortChecklists")
    @Operation(summary = "大类下点检项排序")
    public CommonResult<Integer> sortChecklists(@Valid @RequestBody List<CheckListsSortReqVO> list) {
        return success(checklistService.sortChecklists(list));
    }

}
