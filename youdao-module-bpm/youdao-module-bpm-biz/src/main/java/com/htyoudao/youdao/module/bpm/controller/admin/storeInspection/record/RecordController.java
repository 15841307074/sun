package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.record;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.record.vo.StoreInspectionRecordQueryReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.IndividualInspectionRecordRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordDetailReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordDetailRespVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo.StoreInspectionRecordPageReqVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionrecord.StoreInspectionRecordDO;
import com.htyoudao.youdao.module.bpm.service.storeInspection.record.PCStoreInspectionRecordService;
import com.htyoudao.youdao.module.bpm.service.storeinspectionrecord.StoreInspectionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 点检项目") // 流程实例，通过流程定义创建的一次“申请”
@RestController
@RequestMapping("/bpm/inspectionRecord")
@Validated
public class RecordController {

    @Resource
    private StoreInspectionRecordService storeInspectionRecordService;

    @Resource
    private PCStoreInspectionRecordService pcStoreInspectionRecordService;

    @PostMapping("/queryRecord")
    @Operation(summary = "查询巡店记录")
    public CommonResult<PageResult<IndividualInspectionRecordRespVO>> queryRecord(@Valid @RequestBody StoreInspectionRecordQueryReqVO pageReqVO) {
        PageResult<StoreInspectionRecordDO> pageResult = pcStoreInspectionRecordService.getStoreInspectionRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IndividualInspectionRecordRespVO.class));
    }

    @PostMapping("/getStoreInspectionRecord")
    @Operation(summary = "获得巡店记录详情")
    public CommonResult<StoreInspectionRecordDetailRespVO> getStoreInspectionRecord(@Valid @RequestBody StoreInspectionRecordDetailReqVO reqVO) {
        return success(storeInspectionRecordService.getStoreInspectionRecord(reqVO));
    }
}
