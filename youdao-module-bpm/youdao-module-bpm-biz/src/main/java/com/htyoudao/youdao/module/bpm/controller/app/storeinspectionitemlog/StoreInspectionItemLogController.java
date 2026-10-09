package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo.StoreInspectionItemLogPageReqVO;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionitemlog.vo.StoreInspectionItemLogRespVO;
import com.htyoudao.youdao.module.bpm.dal.dataobject.storeinspectionitemlog.StoreInspectionItemLogDO;
import com.htyoudao.youdao.module.bpm.service.storeinspectionitemlog.StoreInspectionItemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "app - 巡店项操作变更日志")
@RestController
@RequestMapping("/bpm/store-inspection-item-log")
@Validated
public class StoreInspectionItemLogController {

    @Resource
    private StoreInspectionItemLogService storeInspectionItemLogService;

    @GetMapping("/page")
    @Operation(summary = "获得巡店项操作变更日志分页")
    public CommonResult<PageResult<StoreInspectionItemLogRespVO>> getStoreInspectionItemLogPage(@Valid StoreInspectionItemLogPageReqVO pageReqVO) {
        PageResult<StoreInspectionItemLogDO> pageResult = storeInspectionItemLogService.getStoreInspectionItemLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, StoreInspectionItemLogRespVO.class));
    }

}