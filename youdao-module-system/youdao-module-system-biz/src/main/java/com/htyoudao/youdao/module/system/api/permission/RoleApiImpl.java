package com.htyoudao.youdao.module.system.api.permission;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Collection;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class RoleApiImpl implements RoleApi {

    @Resource
    private RoleService roleService;

    @Override
    public CommonResult<Boolean> validRoleList(Collection<Long> ids) {
        roleService.validateRoleList(ids);
        return success(true);
    }
}
