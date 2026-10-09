package com.htyoudao.youdao.module.system.controller.app.user;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import com.htyoudao.youdao.module.system.controller.app.user.vo.UserBossUpdateFixedUserReqVO;
import com.htyoudao.youdao.module.system.controller.app.user.vo.SystemUserNavigationVO;
import com.htyoudao.youdao.module.system.controller.app.user.vo.SystemUserPermissionsVO;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.AUTH_REGISTER_FORBIDDEN;

@Tag(name = "管理后台 - 用户个人中心")
@RefreshScope
@RestController
@RequestMapping("/system/user/boss")
@Validated
@Slf4j
public class UserBossController {

    @Value("${appRegister.registerSwitch}")
    Integer registerSwitch;
    @Value("${appRegister.fixUserId}")
    Long fixUserId;

    @Resource
    private AdminUserService userService;
    @PutMapping("/update-password")
    @Operation(summary = "修改用户个人密码")
    public CommonResult<Boolean> updateUserProfilePassword(@Valid @RequestBody UserProfileUpdatePasswordReqVO reqVO) {
        userService.updateUserPassword(getLoginUserId(), reqVO);
        return success(true);
    }

    @PermitAll
    @PutMapping("/register-account")
    @Operation(summary = "注册固定用户账号")
    public CommonResult<Boolean> updateFixedUserAccount(@Valid @RequestBody UserBossUpdateFixedUserReqVO reqVO) {
        if (registerSwitch == 0) {
            throw exception(AUTH_REGISTER_FORBIDDEN);
        }
        userService.registerFixedUserAccount(fixUserId, reqVO.getMobile(), reqVO.getPassword());
        return success(true);
    }

    @GetMapping("/getUserPermissions")
    @Operation(summary = "获取用户菜单权限")
    public CommonResult<SystemUserPermissionsVO> getUserPermissions() {
        return success(userService.getUserPermissions(getLoginUserId()));
    }

    @GetMapping("/logOff")
    @Operation(summary = "注销")
    @DataPermission(enable = false)
    public CommonResult<String> userLogOff() {
        userService.deleteUser(getLoginUserId());
        return success("OK");
    }

    @GetMapping("/getNavigationByRole")
    @Operation(summary = "获取用户菜单权限(2.0)")
    public CommonResult<SystemUserNavigationVO> getNavigationByRole() {
        return success(userService.getNavigationByRole(getLoginUserId()));
    }
}
