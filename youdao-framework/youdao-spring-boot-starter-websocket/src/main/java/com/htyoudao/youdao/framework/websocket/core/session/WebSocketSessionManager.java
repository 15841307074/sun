package com.htyoudao.youdao.framework.websocket.core.session;

import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;

/**
 * {@link WebSocketSession} 管理器的接口
 *
 * @author 0090
 */
public interface WebSocketSessionManager {

    /**
     * 添加 Session
     *
     * @param session Session
     */
    void addSession(WebSocketSession session);

    /**
     * 移除 Session
     *
     * @param session Session
     */
    void removeSession(WebSocketSession session);

    /**
     * 获取指定用户终端的会话
     *
     * @param userId   用户ID
     * @param terminal 终端
     * @return {@link WebSocketSession }
     */
    WebSocketSession getSession(Long userId, Integer terminal);

    /**
     * 获得指定用户编号的 Session 列表
     *
     * @param userId 用户编号
     * @return Session 列表
     */
    Collection<WebSocketSession> getSessionList(Long userId);

}
