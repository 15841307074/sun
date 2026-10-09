package com.htyoudao.youdao.module.system.controller.admin.orguser;

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

import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;

@Tag(name = "管理后台 - 组织和用户关联")
@RestController
@RequestMapping("/system/org-user")
@Validated
public class OrgUserController {

    @Resource
    private OrgUserService orgUserService;

    @PostMapping("/create")
    @Operation(summary = "创建组织和用户关联")
    @PreAuthorize("@ss.hasPermission('system:org-user:create')")
    public CommonResult<Long> createOrgUser(@Valid @RequestBody OrgUserSaveReqVO createReqVO) {
        return success(orgUserService.createOrgUser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新组织和用户关联")
    @PreAuthorize("@ss.hasPermission('system:org-user:update')")
    public CommonResult<Boolean> updateOrgUser(@Valid @RequestBody OrgUserSaveReqVO updateReqVO) {
        orgUserService.updateOrgUser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除组织和用户关联")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:org-user:delete')")
    public CommonResult<Boolean> deleteOrgUser(@RequestParam("id") Long id) {
        orgUserService.deleteOrgUser(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得组织和用户关联")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:org-user:query')")
    public CommonResult<OrgUserRespVO> getOrgUser(@RequestParam("id") Long id) {
        OrgUserDO orgUser = orgUserService.getOrgUser(id);
        return success(BeanUtils.toBean(orgUser, OrgUserRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得组织和用户关联分页")
    @PreAuthorize("@ss.hasPermission('system:org-user:query')")
    public CommonResult<PageResult<OrgUserRespVO>> getOrgUserPage(@Valid OrgUserPageReqVO pageReqVO) {
        PageResult<OrgUserDO> pageResult = orgUserService.getOrgUserPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, OrgUserRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出组织和用户关联 Excel")
    @PreAuthorize("@ss.hasPermission('system:org-user:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOrgUserExcel(@Valid OrgUserPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OrgUserDO> list = orgUserService.getOrgUserPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "组织和用户关联.xls", "数据", OrgUserRespVO.class,
                        BeanUtils.toBean(list, OrgUserRespVO.class));
    }

}