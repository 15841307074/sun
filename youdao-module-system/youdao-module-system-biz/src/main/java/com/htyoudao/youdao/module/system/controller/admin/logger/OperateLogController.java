package com.htyoudao.youdao.module.system.controller.admin.logger;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;
import com.htyoudao.youdao.framework.translate.core.TranslateUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.logger.vo.operatelog.OperateLogRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.logger.OperateLogDO;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.logger.OperateLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 操作日志")
@RestController
@RequestMapping("/system/operate-log")
@Validated
public class OperateLogController {

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private BusinessService businessService;

    @GetMapping("/page")
    @Operation(summary = "查看操作日志分页列表")
    // @PreAuthorize("@ss.hasPermission('system:operate-log:query')")
    public CommonResult<PageResult<OperateLogRespVO>> pageOperateLog(@Valid OperateLogPageReqVO pageReqVO) {
        PageResult<OperateLogRespVO> pageRespVO;
        if (WebFrameworkUtils.isAdminBusiness()) {
            pageRespVO = DataPermissionUtils.executeIgnore(() -> operateLogService.getOperateLogPageByJoin(pageReqVO));
        } else {
            pageRespVO = operateLogService.getOperateLogPageByJoin(pageReqVO);
        }
        List<OperateLogRespVO> logs = pageRespVO.getList();
        if (WebFrameworkUtils.isAdminBusiness()) {
            logs.forEach(log -> {
                // 如果是总部项目，补充项目名
                if (Objects.nonNull(log.getBusinessId())) {
                    log.setBusinessName(businessService.getBusinessName(log.getBusinessId()));
                }
            });
        }
        return success(pageRespVO);
    }

    @Operation(summary = "导出操作日志")
    @GetMapping("/export")
    @PreAuthorize("@ss.hasPermission('system:operate-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOperateLog(HttpServletResponse response, @Valid OperateLogPageReqVO exportReqVO) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OperateLogDO> list = operateLogService.getOperateLogPage(exportReqVO).getList();
        ExcelUtils.write(response, "操作日志.xls", "数据列表", OperateLogRespVO.class,
                TranslateUtils.translate(BeanUtils.toBean(list, OperateLogRespVO.class)));
    }

}
