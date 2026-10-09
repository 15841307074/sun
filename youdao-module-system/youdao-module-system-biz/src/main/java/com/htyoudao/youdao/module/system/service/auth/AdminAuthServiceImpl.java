package com.htyoudao.youdao.module.system.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.enums.UserTypeEnum;
import com.htyoudao.youdao.framework.common.util.monitor.TracerUtils;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.common.util.validation.ValidationUtils;
import com.htyoudao.youdao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.htyoudao.youdao.module.system.api.sms.SmsCodeApi;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.htyoudao.youdao.module.system.api.social.dto.SocialUserBindReqDTO;
import com.htyoudao.youdao.module.system.api.social.dto.SocialUserRespDTO;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.*;
import com.htyoudao.youdao.module.system.convert.auth.AuthConvert;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.enums.logger.LoginLogTypeEnum;
import com.htyoudao.youdao.module.system.enums.logger.LoginResultEnum;
import com.htyoudao.youdao.module.system.enums.oauth2.OAuth2ClientConstants;
import com.htyoudao.youdao.module.system.enums.sms.SmsSceneEnum;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.logger.LoginLogService;
import com.htyoudao.youdao.module.system.service.member.MemberService;
import com.htyoudao.youdao.module.system.service.oauth2.OAuth2TokenService;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.service.social.SocialUserService;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import com.xingyuv.captcha.model.common.ResponseModel;
import com.xingyuv.captcha.model.vo.CaptchaVO;
import com.xingyuv.captcha.service.CaptchaService;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

/**
 * Auth Service 实现类
 *
 * @author 0090
 */
@Service
@Slf4j
public class AdminAuthServiceImpl implements AdminAuthService {

    @Resource
    private AdminUserService userService;
    @Resource
    private LoginLogService loginLogService;
    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private SocialUserService socialUserService;
    @Resource
    private Validator validator;
    @Resource
    private CaptchaService captchaService;
    @Resource
    private SmsCodeApi smsCodeApi;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;
    @Resource
    private BusinessService businessService;
    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    /**
     * 验证码的开关，默认为 true
     */
    @Value("${youdao.captcha.enable:true}")
    @Setter // 为了单测：开启或者关闭验证码
    private Boolean captchaEnable;

    @Override
    public AdminUserDO authenticate(String username, String password) {
        AdminUserDO user;
        LoginLogTypeEnum logTypeEnum;
        // 先校验是否是手机号，如果是手机号使用手机号登录
        if (ValidationUtils.isMobile(username)) {
            logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
            user = userService.getUserByUsername(username);

            // 如果使用手机号没有查到，尝试使用账号名查询，防止有些账号名是手机号的无法登录
            if (user == null) {
                logTypeEnum = LoginLogTypeEnum.LOGIN_MOBILE;
                user = userService.getUserByMobile(username);
            }
        } else {
            logTypeEnum = LoginLogTypeEnum.LOGIN_USERNAME;
            user = userService.getUserByUsername(username);
        }

        if (user == null) {
            createLoginLog(null, username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (!userService.isPasswordMatch(password, user.getPassword())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(AUTH_LOGIN_BAD_CREDENTIALS);
        }
        // 校验是否禁用
        if (CommonStatusEnum.isDisable(user.getStatus())) {
            createLoginLog(user.getId(), username, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }

        //新增外部客户权限校验
        this.validateGylUserAuth(user);

        return user;
    }

    private void validateGylUserAuth(AdminUserDO user) {
        //只处理外部客户
        if(!user.getUserType().equals("01")){
            return;
        }

        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoService.getStoreDOListByUserId(user.getId());
        if(storeInfoDOS.isEmpty()){
            throw exception(AUTH_BUY_NOT_PERMISSION);
        }
        storeInfoDOS.stream().filter(storeInfoDO -> storeInfoDO.getUseStatus() == 1)
                .findAny().orElseThrow(() -> exception(AUTH_BUY_NOT_PERMISSION));
    }

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO) {
        // 校验验证码
        validateCaptcha(reqVO);

        // 使用账号密码，进行登录
        AdminUserDO user = authenticate(reqVO.getUsername(), reqVO.getPassword());

        // 验证是否绑定项目
        validateBusinessBind(user.getId());

        // 如果 socialType 非空，说明需要绑定社交用户
        if (reqVO.getSocialType() != null) {
            socialUserService.bindSocialUser(new SocialUserBindReqDTO(user.getId(), getUserType().getValue(),
                    reqVO.getSocialType(), reqVO.getSocialCode(), reqVO.getSocialState()));
        }
        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, null);
    }

    @Override
    public void sendSmsCode(AuthSmsSendReqVO reqVO) {
        // 如果是重置密码场景，需要校验图形验证码是否正确
        if (Objects.equals(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene(), reqVO.getScene())) {
            ResponseModel response = doValidateCaptcha(reqVO);
            if (!response.isSuccess()) {
                throw exception(AUTH_REGISTER_CAPTCHA_CODE_ERROR, response.getRepMsg());
            }
        }

        // 登录场景，验证是否存在
        if (userService.getUserByMobile(reqVO.getMobile()) == null) {
            throw exception(AUTH_MOBILE_NOT_EXISTS);
        }
        // 发送验证码
        smsCodeApi.sendSmsCode(AuthConvert.INSTANCE.convert(reqVO).setCreateIp(getClientIP()));
    }

    @Override
    public AuthLoginRespVO smsLogin(AuthSmsLoginReqVO reqVO) {
        // 校验验证码
        smsCodeApi.useSmsCode(AuthConvert.INSTANCE.convert(reqVO, SmsSceneEnum.ADMIN_MEMBER_LOGIN.getScene(), getClientIP())).checkError();

        // 获得用户信息
        AdminUserDO user = userService.getUserByMobile(reqVO.getMobile());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 验证是否绑定项目
        validateBusinessBind(user.getId());

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), reqVO.getMobile(), LoginLogTypeEnum.LOGIN_MOBILE, null);
    }

    @Override
    public AuthLoginRespVO machineLogin(AuthLoginReqVO reqVO) {
        // 校验验证码
        validateCaptcha(reqVO);

        // 使用账号密码，进行登录
        AdminUserDO user = authenticate(reqVO.getUsername(), reqVO.getPassword());

        // 创建 Token 令牌，记录登录日志
        return createMachineTokenAfterLoginSuccess(user.getId(), reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME,
                UserTypeEnum.MEMBER.getValue(), MapUtil.builder("username", user.getUsername()).put("nickname", user.getNickname()).build());
    }

    /**
     * 验证项目绑定
     *
     * @param userId 用户id
     */
    private void validateBusinessBind(Long userId) {
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(userId, null);
        if (CollUtil.isEmpty(roleIds)) {
            throw exception(AUTH_MOBILE_NOT_BIND);
        }
        List<RoleDO> roleList = roleService.getRoleList(roleIds);
        // 获取所拥有的businessId
        Set<Long> businessIds = roleList.stream().map(RoleDO::getBusinessId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(businessIds)) {
            throw exception(AUTH_MOBILE_NOT_BIND);
        }
        // 判断项目是否存在
        List<BusinessDO> businessList = businessService.getBusinessList(businessIds);
        if (CollUtil.isEmpty(businessList)) {
            throw exception(AUTH_MOBILE_NOT_BIND);
        }

    }

    @Override
    public void createLoginLog(Long userId, String username,
                               LoginLogTypeEnum logTypeEnum, LoginResultEnum loginResult) {
        // 插入登录日志
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logTypeEnum.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(getUserType().getValue());
        reqDTO.setUsername(username);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
        // 更新最后登录时间
        if (userId != null && Objects.equals(LoginResultEnum.SUCCESS.getResult(), loginResult.getResult())) {
            userService.updateUserLogin(userId, ServletUtils.getClientIP());
        }
    }

    @Override
    public AuthLoginRespVO socialLogin(AuthSocialLoginReqVO reqVO) {
        // 使用 code 授权码，进行登录。然后，获得到绑定的用户编号
        SocialUserRespDTO socialUser = socialUserService.getSocialUserByCode(UserTypeEnum.ADMIN.getValue(), reqVO.getType(),
                reqVO.getCode(), reqVO.getState());
        if (socialUser == null || socialUser.getUserId() == null) {
            throw exception(AUTH_THIRD_LOGIN_NOT_BIND);
        }

        // 获得用户
        AdminUserDO user = userService.getUser(socialUser.getUserId());
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(user.getId(), user.getUsername(), LoginLogTypeEnum.LOGIN_SOCIAL, null);
    }

    @VisibleForTesting
    void validateCaptcha(AuthLoginReqVO reqVO) {
        ResponseModel response = doValidateCaptcha(reqVO);
        // 校验验证码
        if (!response.isSuccess()) {
            // 创建登录失败日志（验证码不正确)
            createLoginLog(null, reqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, LoginResultEnum.CAPTCHA_CODE_ERROR);
            throw exception(AUTH_LOGIN_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }

    private ResponseModel doValidateCaptcha(CaptchaVerificationReqVO reqVO) {
        // 如果验证码关闭，则不进行校验
        final String code = "ignoreCaptchaCode";
        if (!captchaEnable || StrUtil.equals(code, reqVO.getCaptchaVerification())) {
            return ResponseModel.success();
        }
        ValidationUtils.validate(validator, reqVO, CaptchaVerificationReqVO.CodeEnableGroup.class);
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(reqVO.getCaptchaVerification());
        return captchaService.verification(captchaVO);
    }

    private AuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum logType, Integer userType) {
        // 如果不传入用户类型，需要获取一下
        if (Objects.isNull(userType)) {
            userType = getUserType().getValue();
        }
        // 插入登陆日志
        createLoginLog(userId, username, logType, LoginResultEnum.SUCCESS);
        // 创建访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(userId, userType,
                OAuth2ClientConstants.CLIENT_ID_DEFAULT, null, null);
        // 构建返回结果
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    private AuthLoginRespVO createMachineTokenAfterLoginSuccess(Long userId, String username, LoginLogTypeEnum logType,
                                                                Integer userType, Map<String, String> info) {
        // 如果不传入用户类型，需要获取一下
        if (Objects.isNull(userType)) {
            userType = getUserType().getValue();
        }
        // 插入登陆日志
        createLoginLog(userId, username, logType, LoginResultEnum.SUCCESS);
        // 创建访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(userId, userType, OAuth2ClientConstants.CLIENT_ID_DEFAULT, null, info);
        // 构建返回结果
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    @Override
    public AuthLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, OAuth2ClientConstants.CLIENT_ID_DEFAULT);
        return AuthConvert.INSTANCE.convert(accessTokenDO);
    }

    @Override
    public void logout(String token, Integer logType) {
        // 删除访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(token);
        if (accessTokenDO == null) {
            return;
        }
        // 删除成功，则记录登出日志
        createLogoutLog(accessTokenDO.getUserId(), accessTokenDO.getUserType(), logType);
    }

    private void createLogoutLog(Long userId, Integer userType, Integer logType) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logType);
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(userType);
        reqDTO.setUsername(getUsername(userId));
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(ServletUtils.getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    private String getUsername(Long userId) {
        if (userId == null) {
            return null;
        }
        AdminUserDO user = userService.getUser(userId);
        return user != null ? user.getUsername() : null;
    }

    private UserTypeEnum getUserType() {
        return UserTypeEnum.ADMIN;
    }

    @Override
    public AuthLoginRespVO register(AuthRegisterReqVO registerReqVO) {
        // 1. 校验验证码
        validateCaptcha(registerReqVO);

        // 2. 校验用户名是否已存在
        Long userId = userService.registerUser(registerReqVO);

        // 3. 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(userId, registerReqVO.getUsername(), LoginLogTypeEnum.LOGIN_USERNAME, null);
    }

    @VisibleForTesting
    void validateCaptcha(AuthRegisterReqVO reqVO) {
        ResponseModel response = doValidateCaptcha(reqVO);
        // 验证不通过
        if (!response.isSuccess()) {
            throw exception(AUTH_REGISTER_CAPTCHA_CODE_ERROR, response.getRepMsg());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(AuthResetPasswordReqVO reqVO) {
        AdminUserDO userByMobile = userService.getUserByMobile(reqVO.getMobile());
        if (userByMobile == null) {
            throw exception(USER_MOBILE_NOT_EXISTS);
        }

        smsCodeApi.useSmsCode(new SmsCodeUseReqDTO()
                .setCode(reqVO.getCode())
                .setMobile(reqVO.getMobile())
                .setScene(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene())
                .setUsedIp(getClientIP())
        ).checkError();

        userService.updateUserPassword(userByMobile.getId(), reqVO.getPassword());
    }

    @Override
    public void sendSms(AuthSmsSendReqVO reqVO) {
        // 发送验证码
        smsCodeApi.sendSmsCode(AuthConvert.INSTANCE.convert(reqVO).setCreateIp(getClientIP()));
    }
}
