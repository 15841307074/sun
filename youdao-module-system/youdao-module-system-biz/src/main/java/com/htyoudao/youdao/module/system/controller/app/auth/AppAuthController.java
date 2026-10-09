package com.htyoudao.youdao.module.system.controller.app.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.config.SecurityProperties;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO.MenuVO;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.AuthSmsSendReqVO;
import com.htyoudao.youdao.module.system.convert.auth.AuthConvert;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.MenuDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.enums.logger.LoginLogTypeEnum;
import com.htyoudao.youdao.module.system.service.auth.AdminAuthService;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.permission.MenuService;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.service.social.SocialClientService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.model.vo.CaptchaVO;
import com.xingyuv.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.htyoudao.youdao.module.system.controller.admin.captcha.CaptchaController.getRemoteId;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.BUSINESS_BOSS_USER_MENU_NOT_FOUND;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.BUSINESS_BOSS_USER_ROLE_NOT_FOUND;

/**
 * @author lqman
 */
@Tag(name = "点餐机 - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
@Slf4j
public class AppAuthController {

    @Resource
    private AdminAuthService authService;
    @Resource
    private SecurityProperties securityProperties;
    @Resource
    private AdminUserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private SocialClientService socialClientService;
    @Resource
    private BusinessService businessService;

    @Resource
    private CaptchaService captchaService;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用账号密码登录")
    public CommonResult<AuthLoginRespVO> machineLogin(@RequestBody @Valid AuthLoginReqVO reqVO) {
        return success(authService.machineLogin(reqVO));
    }

    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "登出系统")
    public CommonResult<Boolean> machineLogout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    public CommonResult<AuthLoginRespVO> machineRefreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(authService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "获取登录用户的权限信息")
    public CommonResult<AuthPermissionInfoRespVO> allGetPermissionInfo() {
        // 1.1 获得用户信息
        AdminUserDO user = userService.getUser(getLoginUserId());
        if (user == null) {
            return success(null);
        }

        // 1.2 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(getLoginUserId(), BusinessContextHolder.getBusinessId());
        if (CollUtil.isEmpty(roleIds)) {
            throw new ServiceException(BUSINESS_BOSS_USER_ROLE_NOT_FOUND);
        }
        List<RoleDO> roles = roleService.getRoleList(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())); // 移除禁用的角色

        // 1.3 获得菜单列表
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(convertSet(roles, RoleDO::getId));
        List<MenuDO> menuList = menuService.getMenuList(menuIds);
        menuList = menuService.filterDisableMenus(menuList);


        if (CollectionUtils.isEmpty(roles)){
            throw new ServiceException(BUSINESS_BOSS_USER_ROLE_NOT_FOUND);
        }

        if (CollectionUtils.isEmpty(menuIds) || !menuIds.contains(1967835995479326722L)){
            throw new ServiceException(BUSINESS_BOSS_USER_MENU_NOT_FOUND);
        }

        // 2. 拼接结果返回
        AuthPermissionInfoRespVO convert = AuthConvert.INSTANCE.convert(user, roles, menuList);
        convert.getMenus().removeIf(m -> !Objects.equals(m.getId(), 1967835995479326722L));

        return success(convert);
    }

    @PostMapping("/send-sms-code")
    @PermitAll
    @Operation(summary = "发送手机验证码")
    public CommonResult<Boolean> sendLoginSmsCode(@RequestBody @Valid AuthSmsSendReqVO reqVO) {
        authService.sendSms(reqVO);
        return success(true);
    }



}
