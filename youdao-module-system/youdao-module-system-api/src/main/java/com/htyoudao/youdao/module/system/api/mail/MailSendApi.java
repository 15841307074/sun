package com.htyoudao.youdao.module.system.api.mail;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "RPC 服务 - 邮件发送")
public interface MailSendApi {

    @Operation(summary = "发送单条邮件给 Admin 用户", description = "在 mail 为空时，使用 userId 加载对应 Admin 的邮箱")
    CommonResult<Long> sendSingleMailToAdmin(@Valid @RequestBody MailSendSingleToUserReqDTO reqDTO);

    @Operation(summary = "发送单条邮件给 Member 用户", description = "在 mail 为空时，使用 userId 加载对应 Member 的邮箱")
    CommonResult<Long> sendSingleMailToMember(@Valid @RequestBody MailSendSingleToUserReqDTO reqDTO);

}
