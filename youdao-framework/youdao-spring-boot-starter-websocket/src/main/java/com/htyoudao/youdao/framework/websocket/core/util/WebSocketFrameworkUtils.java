package com.htyoudao.youdao.framework.websocket.core.util;

import com.htyoudao.youdao.framework.security.core.LoginUser;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

/**
 * 专属于 web 包的工具类
 *
 * @author 0090
 */
public class WebSocketFrameworkUtils {

    /**
     * 终端类型请求头
     */
    public static final String HEADER_TERMINAL_TYPE = "terminal";
    /**
     * 登录用户
     */
    public static final String ATTRIBUTE_LOGIN_USER = "LOGIN_USER";
    /**
     * 终端类型
     */
    public static final String ATTRIBUTE_TERMINAL_TYPE = "TERMINAL_TYPE";
    /**
     * 心跳次数
     */
    public static final String ATTRIBUTE_HEARTBEAT_TIMES = "HEARTBEAT_TIMES";

    /**
     * 设置当前用户
     *
     * @param loginUser 登录用户
     * @param attributes Session
     */
    public static void setLoginUser(LoginUser loginUser, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_LOGIN_USER, loginUser);
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户
     */
    public static LoginUser getLoginUser(WebSocketSession session) {
        return (LoginUser) session.getAttributes().get(ATTRIBUTE_LOGIN_USER);
    }

    /**
     * 获得当前用户的编号
     *
     * @return 用户编号
     */
    public static Long getLoginUserId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 获得当前用户的类型
     *
     * @return 用户编号
     */
    public static Integer getLoginUserType(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getUserType() : null;
    }

    /**
     * 设置终端类型
     *
     * @param terminalType 终端类型
     * @param attributes   Session
     */
    public static void setTerminalType(Integer terminalType, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_TERMINAL_TYPE, terminalType);
    }

    /**
     * 获取终端类型
     *
     * @param session 会话
     * @return {@link Integer }
     */
    public static Integer getTerminalType(WebSocketSession session) {
        return (Integer) session.getAttributes().get(ATTRIBUTE_TERMINAL_TYPE);
    }

    /**
     * 设定心跳次数
     *
     * @param heartBeatTimes 心跳次数
     * @param attributes     Session
     */
    public static void setHeartBeatTimes(Long heartBeatTimes, Map<String, Object> attributes) {
        attributes.put(ATTRIBUTE_HEARTBEAT_TIMES, heartBeatTimes);
    }

    /**
     * 获取心跳次数
     *
     * @param session 会话
     * @return {@link Long }
     */
    public static Long getHeartBeatTimes(WebSocketSession session) {
        return (Long) session.getAttributes().get(ATTRIBUTE_HEARTBEAT_TIMES);
    }

    /**
     * 获得当前用户的租户编号
     *
     * @param session Session
     * @return 租户编号
     */
    public static Long getTenantId(WebSocketSession session) {
        LoginUser loginUser = getLoginUser(session);
        return loginUser != null ? loginUser.getTenantId() : null;
    }

}

