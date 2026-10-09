package com.htyoudao.youdao.framework.websocket.core.security;

import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.enums.TerminalEnum;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.filter.TokenAuthenticationFilter;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;

/**
 * 登录用户的 {@link HandshakeInterceptor} 实现类
 * 流程如下：
 * 1. 前端连接 websocket 时，会通过拼接 ?token={token} 到 ws:// 连接后，这样它可以被 {@link TokenAuthenticationFilter} 所认证通过
 * 2. {@link LoginUserHandshakeInterceptor} 负责把 {@link LoginUser} 添加到 {@link WebSocketSession} 中
 *
 * @author 0090
 */
@Slf4j
public class LoginUserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(@Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response,
                                   @Nonnull WebSocketHandler wsHandler, @Nonnull Map<String, Object> attributes) {
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (loginUser == null) {
            log.error("用户认证失败！");
            return false;
        }

        // 设置登录用户信息
        WebSocketFrameworkUtils.setLoginUser(loginUser, attributes);

        // 解析并设置终端类型，提供默认值避免null
        String terminalTypeStr = request.getHeaders().getFirst(WebSocketFrameworkUtils.HEADER_TERMINAL_TYPE);
        if (StrUtil.isBlank(terminalTypeStr)) {
            log.error("无终端类型！");
            return false;
        }
        int terminalType;
        try {
            terminalType = Integer.parseInt(terminalTypeStr);
            if (!Arrays.asList(TerminalEnum.IM_ARRAYS).contains(terminalType)) {
                log.error("不支持的终端类型：{}！", terminalTypeStr);
                return false;
            }
            WebSocketFrameworkUtils.setTerminalType(terminalType, attributes);
        } catch (NumberFormatException e) {
            log.error("终端类型格式错误：{}！", terminalTypeStr);
            return false;
        }

        // 初始化心跳次数
        WebSocketFrameworkUtils.setHeartBeatTimes(0L, attributes);
        return true;
    }

    @Override
    public void afterHandshake(@Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response,
                               @Nonnull WebSocketHandler wsHandler, Exception exception) {
        // do nothing
    }

}
