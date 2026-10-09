package com.htyoudao.youdao.module.system.api.notify;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "RPC 服务 - 站内信发送")
public interface NotifyMessageSendApi {

    @Operation(summary = "发送单条站内信给 Admin 用户")
    CommonResult<Long> sendSingleMessageToAdmin(@Valid @RequestBody NotifySendSingleToUserReqDTO reqDTO);

    @Operation(summary = "发送单条站内信给 Member 用户")
    CommonResult<Long> sendSingleMessageToMember(@Valid @RequestBody NotifySendSingleToUserReqDTO reqDTO);

}
