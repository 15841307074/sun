package com.htyoudao.youdao.module.infra.controller.admin.groupmember;

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

import com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo.*;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmember.GroupMemberDO;
import com.htyoudao.youdao.module.infra.service.groupmember.GroupMemberService;

@Tag(name = "管理后台 - 群成员")
@RestController
@RequestMapping("/infra/group-member")
@Validated
public class GroupMemberController {

    @Resource
    private GroupMemberService groupMemberService;

    @PostMapping("/create")
    @Operation(summary = "创建群成员")
    @PreAuthorize("@ss.hasPermission('infra:group-member:create')")
    public CommonResult<Long> createGroupMember(@Valid @RequestBody GroupMemberSaveReqVO createReqVO) {
        return success(groupMemberService.createGroupMember(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新群成员")
    @PreAuthorize("@ss.hasPermission('infra:group-member:update')")
    public CommonResult<Boolean> updateGroupMember(@Valid @RequestBody GroupMemberSaveReqVO updateReqVO) {
        groupMemberService.updateGroupMember(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除群成员")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:group-member:delete')")
    public CommonResult<Boolean> deleteGroupMember(@RequestParam("id") Long id) {
        groupMemberService.deleteGroupMember(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得群成员")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('infra:group-member:query')")
    public CommonResult<GroupMemberRespVO> getGroupMember(@RequestParam("id") Long id) {
        GroupMemberDO groupMember = groupMemberService.getGroupMember(id);
        return success(BeanUtils.toBean(groupMember, GroupMemberRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得群成员分页")
    @PreAuthorize("@ss.hasPermission('infra:group-member:query')")
    public CommonResult<PageResult<GroupMemberRespVO>> getGroupMemberPage(@Valid GroupMemberPageReqVO pageReqVO) {
        PageResult<GroupMemberDO> pageResult = groupMemberService.getGroupMemberPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, GroupMemberRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出群成员 Excel")
    @PreAuthorize("@ss.hasPermission('infra:group-member:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupMemberExcel(@Valid GroupMemberPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GroupMemberDO> list = groupMemberService.getGroupMemberPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "群成员.xls", "数据", GroupMemberRespVO.class,
                        BeanUtils.toBean(list, GroupMemberRespVO.class));
    }

}