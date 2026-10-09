package com.htyoudao.youdao.framework.websocket.core.message;

import com.htyoudao.youdao.framework.common.enums.TerminalEnum;
import lombok.Data;

import java.util.List;

@Data
public class PrivateMessage<T> {

    /**
     * 发送方
     */
    private UserInfo sender;

    /**
     * 接收者id
     */
    private Long receiverId;


    /**
     * 接收者终端类型,默认全部
     */
    private List<Integer> terminals = List.of(TerminalEnum.IM_ARRAYS);

    /**
     * 是否发送给自己的其他终端,默认true
     */
    private Boolean sendToSelf = true;

    /**
     * 是否需要回推发送结果,默认true
     */
    private Boolean sendResult = true;

    /**
     * 消息内容
     */
    private T data;


}
