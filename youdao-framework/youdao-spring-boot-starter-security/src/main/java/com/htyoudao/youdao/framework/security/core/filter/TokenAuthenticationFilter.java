package com.htyoudao.youdao.framework.security.core.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.security.config.SecurityProperties;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.handler.GlobalExceptionHandler;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.api.oauth2.OAuth2TokenApi;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Token 过滤器，验证 token 的有效性
 * 验证通过后，获得 {@link LoginUser} 信息，并加入到 Spring Security 上下文
 *
 * @author 0090
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties securityProperties;

    private final GlobalExceptionHandler globalExceptionHandler;

    private final OAuth2TokenApi oauth2TokenApi;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 检查是否是permitAll的URL
        if (isPermitAllUrl(request)) {
            chain.doFilter(request, response);
            return;
        }

        // 情况一，基于 header[login-user] 获得用户，例如说来自 Gateway 或者其它服务透传
        LoginUser loginUser = buildLoginUserByHeader(request);

        // 情况二，基于 Token 获得用户
        // 注意，这里主要满足直接使用 Nginx 直接转发到 Spring Cloud 服务的场景。
        if (loginUser == null) {
            String token = SecurityFrameworkUtils.obtainAuthorization(request,
                    securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
            if (StrUtil.isNotEmpty(token)) {
                Integer userType = WebFrameworkUtils.getLoginUserType(request);
                try {
                    // 基于 token 构建登录用户
                    loginUser = buildLoginUserByToken(token, userType);
                } catch (Throwable ex) {
                    CommonResult<?> result = globalExceptionHandler.allExceptionHandler(request, ex);
                    ServletUtils.writeJSON(response, result);
                    return;
                }
            }
        }

        // 设置当前用户
        if (loginUser != null) {
            SecurityFrameworkUtils.setLoginUser(loginUser, request);
        }
        // 继续过滤链
        chain.doFilter(request, response);
    }

    /**
     * 判断是否为免登录的URL
     *
     * @param request HTTP请求
     * @return 如果是免登录的URL，则返回true
     */
    private boolean isPermitAllUrl(HttpServletRequest request) {
        // 1. 获取当前请求的URI和方法
        String requestUri = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        // 2. 校验配置文件中的免登录URL
        for (String permitUrl : securityProperties.getPermitAllUrls()) {
            if (pathMatcher.match(permitUrl, requestUri)) {
                return true;
            }
        }

        // 3. 校验注解中的免登录URL
        if (securityProperties.getAnnotationPermitAllUrls() != null) {
            for (String permitUrl : securityProperties.getAnnotationPermitAllUrls().get(method)) {
                if (pathMatcher.match(permitUrl, requestUri)) {
                    return true;
                }
            }
        }

        return false;
    }

    private LoginUser buildLoginUserByToken(String token, Integer userType) {
        try {
            // 校验访问令牌
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token).getCheckedData();
            if (accessToken == null) {
                return null;
            }
            // 用户类型不匹配，无权限
            // 注意：只有 /admin-api/* 和 /app-api/* 有 userType，才需要比对用户类型
            // 类似 WebSocket 的 /ws/* 连接地址，是不需要比对用户类型的
           if (userType != null && ObjectUtil.notEqual(accessToken.getUserType(), userType)) {
               throw new AccessDeniedException("错误的用户类型");
           }
            // 构建登录用户
            return new LoginUser().setId(accessToken.getUserId()).setUserType(accessToken.getUserType())
                    .setInfo(accessToken.getUserInfo())
                    .setTenantId(accessToken.getTenantId()).setScopes(accessToken.getScopes())
                    .setExpiresTime(accessToken.getExpiresTime());
        } catch (ServiceException serviceException) {
            // 校验 Token 不通过时，考虑到一些接口是无需登录的，所以直接返回 null 即可
            return null;
        }
    }

    private LoginUser buildLoginUserByHeader(HttpServletRequest request) {
        String loginUserStr = request.getHeader(SecurityFrameworkUtils.LOGIN_USER_HEADER);
        if (StrUtil.isEmpty(loginUserStr)) {
            return null;
        }
        try {
            // 解码，解决中文乱码问题
            loginUserStr = URLDecoder.decode(loginUserStr, StandardCharsets.UTF_8);
            return JsonUtils.parseObject(loginUserStr, LoginUser.class);
        } catch (Exception ex) {
            log.error("[buildLoginUserByHeader][解析 LoginUser({}) 发生异常]", loginUserStr, ex);
            throw ex;
        }
    }

}
