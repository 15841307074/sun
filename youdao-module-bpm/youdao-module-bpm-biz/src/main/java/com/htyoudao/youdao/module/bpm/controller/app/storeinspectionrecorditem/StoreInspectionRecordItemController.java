package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem;

import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordDetailRespVO;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.*;

import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.*;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecorditem.StoreInspectionRecordItemDO;
import com.htyoudao.youdao.module.bpm.service.storeinspectionrecorditem.StoreInspectionRecordItemService;

@Tag(name = "app - 巡店记录明细快照")
@RestController
@RequestMapping("/bpm/store-inspection-record-item")
@Validated
public class StoreInspectionRecordItemController {

    @Resource
    private StoreInspectionRecordItemService storeInspectionRecordItemService;

    @PutMapping("/update")
    @Operation(summary = "更新巡店项目")
    public CommonResult<StoreInspectionRecordDetailRespVO> updateStoreInspectionRecordItem(@Valid @RequestBody StoreInspectionRecordItemSaveReqVO updateReqVO) {
        return success(storeInspectionRecordItemService.updateStoreInspectionRecordItem(updateReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得巡店记录明细快照")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<StoreInspectionRecordItemRespVO> getStoreInspectionRecordItem(@RequestParam("id") Long id) {
        StoreInspectionRecordItemDO storeInspectionRecordItem = storeInspectionRecordItemService.getStoreInspectionRecordItem(id);
        return success(BeanUtils.toBean(storeInspectionRecordItem, StoreInspectionRecordItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得巡店记录明细快照分页")
    public CommonResult<PageResult<StoreInspectionRecordItemRespVO>> getStoreInspectionRecordItemPage(@Valid StoreInspectionRecordItemPageReqVO pageReqVO) {
        PageResult<StoreInspectionRecordItemDO> pageResult = storeInspectionRecordItemService.getStoreInspectionRecordItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StoreInspectionRecordItemRespVO.class));
    }
}