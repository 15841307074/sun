package com.htyoudao.youdao.framework.websocket.core.session;

import com.htyoudao.youdao.framework.websocket.core.constants.IMConstants;
import com.htyoudao.youdao.framework.websocket.core.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.util.concurrent.TimeUnit;

/**
 * {@link WebSocketHandler} 的装饰类，实现了以下功能：
 * 1. {@link WebSocketSession} 连接或关闭时，使用 {@link #sessionManager} 进行管理
 * 2. 封装 {@link WebSocketSession} 支持并发操作
 *
 * @author 0090
 */
@Slf4j
public class WebSocketSessionHandlerDecorator extends WebSocketHandlerDecorator {

    private final StringRedisTemplate stringRedisTemplate;

    private final Long serverId;

    /**
     * 发送时间的限制，单位：毫秒
     */
    private static final Integer SEND_TIME_LIMIT = 1000 * 5;
    /**
     * 发送消息缓冲上线，单位：bytes
     */
    private static final Integer BUFFER_SIZE_LIMIT = 1024 * 100;

    private final WebSocketSessionManager sessionManager;

    public WebSocketSessionHandlerDecorator(WebSocketHandler delegate,
                                            WebSocketSessionManager sessionManager,
                                            StringRedisTemplate stringRedisTemplate, Long serverId) {
        super(delegate);
        this.sessionManager = sessionManager;
        this.stringRedisTemplate = stringRedisTemplate;
        this.serverId = serverId;
    }

    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        // 实现 session 支持并发，可参考 https://blog.csdn.net/abu935009066/article/details/131218149
        session = new ConcurrentWebSocketSessionDecorator(session, SEND_TIME_LIMIT, BUFFER_SIZE_LIMIT);
        // 添加到 WebSocketSessionManager 中
        sessionManager.addSession(session);

        // 存储用户在线状态到Redis，添加异常处理
        Long loginUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        Integer terminalType = WebSocketFrameworkUtils.getTerminalType(session);
        try {
            String key = String.format("%s:%d:%d", RedisKeyConstants.IM_MAX_SERVER_ID, loginUserId, terminalType);
            stringRedisTemplate.opsForValue().set(key, serverId.toString(), IMConstants.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 记录错误，但不中断WebSocket连接
            log.error("存储用户在线状态到Redis失败，userId: {}, terminalType: {}, serverId: {}",
                    loginUserId, terminalType, serverId, e);
        }

    }

    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus closeStatus) {
        sessionManager.removeSession(session);

        // 移除缓存
        Long loginUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        Integer terminalType = WebSocketFrameworkUtils.getTerminalType(session);
        String key = String.format("%s:%d:%d", RedisKeyConstants.IM_MAX_SERVER_ID, loginUserId, terminalType);
        stringRedisTemplate.delete(key);
    }

}
