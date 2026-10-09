package com.htyoudao.youdao.module.system.api.sms;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "RPC 服务 - 短信验证码")
public interface SmsCodeApi {

    @Operation(summary = "创建短信验证码，并进行发送")
    CommonResult<Boolean> sendSmsCode(SmsCodeSendReqDTO reqDTO);

    @Operation(summary = "验证短信验证码，并进行使用")
    CommonResult<Boolean> useSmsCode(SmsCodeUseReqDTO reqDTO);

    @Operation(summary = "检查验证码是否有效")
    CommonResult<Boolean> validateSmsCode(SmsCodeValidateReqDTO reqDTO);

}
