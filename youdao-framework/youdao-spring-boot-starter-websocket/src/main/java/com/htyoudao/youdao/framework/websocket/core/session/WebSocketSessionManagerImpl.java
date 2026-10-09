package com.htyoudao.youdao.framework.websocket.core.session;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认的 {@link WebSocketSessionManager} 实现类
 *
 * @author 0090
 */
public class WebSocketSessionManagerImpl implements WebSocketSessionManager {

    /**
     * user 与 WebSocketSession 映射
     * key：用户编号
     */
    private final ConcurrentMap<Long, CopyOnWriteArrayList<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(WebSocketSession session) {
        // 添加到 userSessions 中
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(user.getId());
        if (sessions == null) {
            sessions = new CopyOnWriteArrayList<>();
            if (userSessions.putIfAbsent(user.getId(), sessions) != null) {
                sessions = userSessions.get(user.getId());
            }
        }
        sessions.add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        // 移除从 idSessions 中
        LoginUser user = WebSocketFrameworkUtils.getLoginUser(session);
        if (user == null) {
            return;
        }
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(user.getId());
        sessions.removeIf(session0 -> session0.getId().equals(session.getId()));
        if (CollUtil.isEmpty(sessions)) {
            userSessions.remove(user.getId(), sessions);
        }
    }

    @Override
    public WebSocketSession getSession(Long userId, Integer terminal) {
        return getSessionList(userId).stream()
                .filter(session -> Objects.equals(terminal, WebSocketFrameworkUtils.getTerminalType(session)))
                .findAny()
                .orElse(null);
    }

    @Override
    public Collection<WebSocketSession> getSessionList(Long userId) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        return CollUtil.isNotEmpty(sessions) ? new ArrayList<>(sessions) : new ArrayList<>();
    }

}
