package com.htyoudao.youdao.framework.websocket.core.message;

import lombok.Data;

import java.util.List;

@Data
public class ReceiverInfo {

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 发送方
     */
    private UserInfo sender;

    /**
     * 接收方用户列表
     */
    List<UserInfo> receivers;

    /**
     * 是否需要回调发送结果
     */
    private Boolean sendResult;

    /**
     * 当前服务名（回调发送结果使用）
     */
    private String serviceName;
    /**
     * 推送消息体
     */
    private Object data;
}


