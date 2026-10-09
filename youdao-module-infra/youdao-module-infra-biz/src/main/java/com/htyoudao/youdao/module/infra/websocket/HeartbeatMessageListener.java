package com.htyoudao.youdao.module.infra.websocket;

import com.htyoudao.youdao.framework.websocket.core.constants.IMConstants;
import com.htyoudao.youdao.framework.websocket.core.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.websocket.core.listener.WebSocketMessageListener;
import com.htyoudao.youdao.framework.websocket.core.util.WebSocketFrameworkUtils;
import com.htyoudao.youdao.module.infra.enums.MessageTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 心跳消息监听器
 *
 * @author liuzhaowang
 */
@Component
@Slf4j
public class HeartbeatMessageListener implements WebSocketMessageListener<String> {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void onMessage(WebSocketSession session, String message) {
        // 发送心跳消息
        try {
            session.sendMessage(new TextMessage("pong"));
        } catch (IOException e) {
            // 不抛出异常，避免关闭WebSocket连接
            log.error("发送心跳响应失败，sessionId: {}", session.getId(), e);
        }

        // 设置心跳属性
        Long heartBeatTimes = WebSocketFrameworkUtils.getHeartBeatTimes(session);
        // 避免空指针异常，如果heartBeatTimes为null，则初始化为0
        heartBeatTimes = heartBeatTimes != null ? heartBeatTimes + 1 : 1L;
        WebSocketFrameworkUtils.setHeartBeatTimes(heartBeatTimes, session.getAttributes());

        boolean refresh = heartBeatTimes % 10 == 0;
        Long loginUserId = WebSocketFrameworkUtils.getLoginUserId(session);
        if (refresh && loginUserId != null) {
            Integer terminalType = WebSocketFrameworkUtils.getTerminalType(session);
            // 避免空指针异常，如果terminalType为null，使用默认值
            if (terminalType != null) {
                String key = String.format("%s:%d:%d", RedisKeyConstants.IM_MAX_SERVER_ID, loginUserId, terminalType);
                try {
                    stringRedisTemplate.expire(key, IMConstants.ONLINE_TIMEOUT_SECOND, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.error("更新Redis过期时间失败，key: {}", key, e);
                }
            } else {
                log.warn("终端类型为null，无法更新Redis过期时间，userId: {}", loginUserId);
            }
        }
        log.info("heartbeat userId: {}, sessionId: {}", loginUserId, session.getId());
    }

    @Override
    public String getType() {
        return MessageTypeEnum.HEART_BEAT.getType();
    }

}
