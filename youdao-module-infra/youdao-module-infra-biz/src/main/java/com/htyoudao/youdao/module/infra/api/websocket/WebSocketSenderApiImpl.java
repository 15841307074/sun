package com.htyoudao.youdao.module.infra.api.websocket;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.websocket.core.sender.WebSocketMessageSender;
import com.htyoudao.youdao.module.infra.api.websocket.dto.WebSocketSendReqDTO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class WebSocketSenderApiImpl implements WebSocketSenderApi {

    @Resource
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public CommonResult<Boolean> send(WebSocketSendReqDTO message) {
        /* if (message.getUserId() != null) {
            webSocketMessageSender.sendPrivateMessage(message.getUserId(), message.getMessageType(), message.getMessageContent());
        } */
        return success(true);
    }

}
