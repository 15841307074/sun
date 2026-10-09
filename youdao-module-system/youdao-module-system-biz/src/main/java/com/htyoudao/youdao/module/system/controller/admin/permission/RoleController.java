package com.htyoudao.youdao.module.system.controller.admin.permission;

import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.*;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static java.util.Collections.singleton;

@Tag(name = "管理后台 - 角色")
@RestController
@RequestMapping("/system/role")
@Validated
public class RoleController {

    @Resource
    private RoleService roleService;


    @PostMapping("/create")
    @Operation(summary = "创建角色")
    @PreAuthorize("@ss.hasPermission('system:role:create')")
    public CommonResult<Long> createRole(@Valid @RequestBody RoleSaveReqVO createReqVO) {
        createReqVO.setId(null);
        if (createReqVO.getStatus() == null){
            createReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        return success(roleService.createRole(createReqVO, null));
    }

    @PutMapping("/update")
    @Operation(summary = "修改角色")
    @PreAuthorize("@ss.hasPermission('system:role:update')")
    public CommonResult<Boolean> updateRole(@Valid @RequestBody RoleSaveReqVO updateReqVO) {
        if (updateReqVO.getStatus() == null){
            updateReqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        roleService.updateRole(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除角色")
    @Parameter(name = "id", description = "角色编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:role:delete')")
    public CommonResult<Boolean> deleteRole(@RequestParam("id") Long id) {
        roleService.deleteRole(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得角色信息")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<RoleRespVO> getRole(@RequestParam("id") Long id) {
        RoleDO role = roleService.getRole(id);
        return success(BeanUtils.toBean(role, RoleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得角色分页")
    @PreAuthorize("@ss.hasPermission('system:role:query')")
    public CommonResult<PageResult<RoleRespVO>> getRolePage(RolePageReqVO pageReqVO) {
        PageResult<RoleRespVO> pageResult = roleService.getRolePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RoleRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取角色精简信息列表", description = "只包含被开启的角色，主要用于前端的下拉选项")
    public CommonResult<List<RoleRespVO>> getSimpleRoleList() {
        List<RoleDO> list = roleService.getRoleListByStatus(singleton(CommonStatusEnum.ENABLE.getStatus()));
        list.sort(Comparator.comparing(RoleDO::getSort));
        return success(BeanUtils.toBean(list, RoleRespVO.class));
    }


    @GetMapping("getUserListWithRole")
    @Operation(summary = "角色添加人员时候的查询窗口")
    public CommonResult<PageResult<OrgUserPageRespVO>> getUserListWithRole(@Valid RoleUserPageReqVO pageReqVO) {
        return success(roleService.getUserListWithRole(pageReqVO));
    }




    @Operation(summary = "角色拖动排序")
    @PostMapping("/sort/role")
    public CommonResult<Boolean> sortRole(@Valid @RequestBody List<RoleSortVO> sortReqVOS) {
        roleService.sortRole(sortReqVOS);
        return success(true);
    }


}
