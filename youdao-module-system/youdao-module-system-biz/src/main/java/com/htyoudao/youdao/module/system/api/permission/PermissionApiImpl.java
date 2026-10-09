package com.htyoudao.youdao.module.system.api.permission;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class PermissionApiImpl implements PermissionApi {

    @Resource
    private PermissionService permissionService;

    @Override
    public CommonResult<Set<Long>> getUserRoleIdListByRoleIds(Collection<Long> roleIds) {
        return success(permissionService.getUserRoleIdListByRoleId(roleIds));
    }

    @Override
    public CommonResult<Boolean> hasAnyPermissions(Long userId, String... permissions) {
        return success(permissionService.hasAnyPermissions(userId, permissions));
    }

    @Override
    public CommonResult<Boolean> hasAnyRoles(Long userId, String... roles) {
        return success(permissionService.hasAnyRoles(userId, roles));
    }

    @Override
    public CommonResult<DeptDataPermissionRespDTO> getDeptDataPermission(Long userId) {
        return success(permissionService.getDeptDataPermission(userId, BusinessContextHolder.getBusinessId()));
    }

}
