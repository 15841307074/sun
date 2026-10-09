package com.htyoudao.youdao.module.infra.websocket.message;

import lombok.Data;

import java.util.Set;

/**
 * 示例：client -> server 发送消息
 *
 * @author 0090
 */
@Data
public class WebsocketSendMessage {

    /**
     * 发送给谁
     * 如果为空，说明发送给所有人，单个说明是私聊，多个是群聊
     */
    private Set<Long> receiverIds;
    /**
     * 内容
     */
    private String text;

}
