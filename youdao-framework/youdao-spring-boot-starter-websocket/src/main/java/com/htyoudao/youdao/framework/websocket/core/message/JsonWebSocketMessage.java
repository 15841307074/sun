package com.htyoudao.youdao.framework.websocket.core.message;

import com.htyoudao.youdao.framework.websocket.core.listener.WebSocketMessageListener;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * JSON 格式的 WebSocket 消息帧
 *
 * @author 0090
 */
@Data
public class JsonWebSocketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 5590060831173314109L;

    /**
     * 消息类型
     * 目的：用于分发到对应的 {@link WebSocketMessageListener} 实现类
     */
    private String type;
    /**
     * 消息内容
     * 要求 JSON 对象
     */
    private String content;

}
