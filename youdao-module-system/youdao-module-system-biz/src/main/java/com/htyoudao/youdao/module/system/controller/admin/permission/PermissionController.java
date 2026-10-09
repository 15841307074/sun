package com.htyoudao.youdao.module.system.controller.admin.permission;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionAssignRoleDataScopeReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionAssignRoleMenuReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionAssignUserRoleReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionCopyRoleReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionMoveUserRoleReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionRemoveUserRoleReqVO;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 权限 Controller，提供赋予用户、角色的权限的 API 接口
 *
 * @author 0090
 */
@Tag(name = "管理后台 - 权限")
@RestController
@RequestMapping("/system/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    @Resource
    private RoleService roleService;


    @Operation(summary = "获得角色拥有的菜单编号")
    @Parameter(name = "roleId", description = "角色编号", required = true)
    @GetMapping("/list-role-menus")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Set<Long>> getRoleMenuList(Long roleId) {
        return success(permissionService.getRoleMenuListByRoleId(roleId));
    }

    @PostMapping("/assign-role-menu")
    @Operation(summary = "赋予角色菜单")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-menu')")
    public CommonResult<Boolean> assignRoleMenu(@Validated @RequestBody PermissionAssignRoleMenuReqVO reqVO) {
        //不允许 修改项目管理员的菜单权限
        roleService.validateRoleForUpdate(reqVO.getRoleId());
        // 执行菜单的分配
        permissionService.assignRoleMenu(reqVO.getRoleId(), reqVO.getMenuIds());
        return success(true);
    }

    @PostMapping("/assign-role-data-scope")
    @Operation(summary = "赋予角色数据权限")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-role-data-scope')")
    public CommonResult<Boolean> assignRoleDataScope(@Valid @RequestBody PermissionAssignRoleDataScopeReqVO reqVO) {
        permissionService.assignRoleDataScope(reqVO.getRoleId(), reqVO.getDataScope(), reqVO.getDataScopeDeptIds());
        return success(true);
    }

    @Operation(summary = "赋予用户角色")
    @PostMapping("/assign-user-role")
    @PreAuthorize("@ss.hasPermission('system:permission:assign-user-role')")
    public CommonResult<Boolean> assignUserRole(@Validated @RequestBody PermissionAssignUserRoleReqVO reqVO) {
        for (Long userId : reqVO.getUserIds()) {
            permissionService.incrAssignUserRole(userId, reqVO.getRoleIds(), BusinessContextHolder.getBusinessId());
        }
        return success(true);
    }


    @PutMapping("/move")
    @Operation(summary = "移动用户角色")
    @PreAuthorize("@ss.hasPermission('system:role:move-user-role')")
    public CommonResult<Boolean> moveUserRole(@Valid @RequestBody PermissionMoveUserRoleReqVO updateReqVO) {
        permissionService.moveUserRole(updateReqVO);
        return success(true);
    }


    @Operation(summary = "移除用户角色")
    @PostMapping("/remove-user-role")
    @PreAuthorize("@ss.hasPermission('system:permission:remove-user-role')")
    public CommonResult<Boolean> removeUserRole(@Validated @RequestBody PermissionRemoveUserRoleReqVO reqVO) {
        permissionService.removeUserRole(reqVO);
        return success(true);
    }

    @Operation(summary = "复制角色")
    @PostMapping("/copy/role")
    @PreAuthorize("@ss.hasPermission('system:permission:copy-role')")
    public CommonResult<Boolean> copyRole(@Validated @RequestBody PermissionCopyRoleReqVO reqVO) {
        permissionService.copyRole(reqVO);
        return success(true);
    }


}
