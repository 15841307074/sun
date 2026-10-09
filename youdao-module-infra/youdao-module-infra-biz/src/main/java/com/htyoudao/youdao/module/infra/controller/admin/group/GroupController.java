package com.htyoudao.youdao.module.infra.controller.admin.group;

import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.excel.core.util.ExcelUtils;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.GroupPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.GroupRespVO;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.GroupSaveReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.group.GroupDO;
import com.htyoudao.youdao.module.infra.service.group.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 群")
@RestController
@RequestMapping("/infra/group")
@Validated
public class GroupController {

    @Resource
    private GroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建群")
    public CommonResult<Long> createGroup(@Valid @RequestBody GroupSaveReqVO createReqVO) {
        return success(groupService.createGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群")
    @PreAuthorize("@ss.hasPermission('infra:group:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody GroupSaveReqVO updateReqVO) {
        groupService.updateGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除群")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        groupService.deleteGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:group:query')")
    public CommonResult<GroupRespVO> getGroup(@RequestParam("id") Long id) {
        GroupDO group = groupService.getGroup(id);
        return success(BeanUtils.toBean(group, GroupRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得群分页")
    @PreAuthorize("@ss.hasPermission('infra:group:query')")
    public CommonResult<PageResult<GroupRespVO>> getGroupPage(@Valid GroupPageReqVO pageReqVO) {
        PageResult<GroupDO> pageResult = groupService.getGroupPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, GroupRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出群 Excel")
    @PreAuthorize("@ss.hasPermission('infra:group:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupExcel(@Valid GroupPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GroupDO> list = groupService.getGroupPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "群.xls", "数据", GroupRespVO.class,
                        BeanUtils.toBean(list, GroupRespVO.class));
    }

}
