package com.htyoudao.youdao.module.system.api.sms;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.htyoudao.youdao.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import com.htyoudao.youdao.module.system.service.sms.SmsCodeService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
public class SmsCodeApiImpl implements SmsCodeApi {

    @Resource
    private SmsCodeService smsCodeService;

    @Override
    public CommonResult<Boolean> sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        smsCodeService.sendSmsCode(reqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> useSmsCode(SmsCodeUseReqDTO reqDTO) {
        smsCodeService.useSmsCode(reqDTO);
        return success(true);
    }

    @Override
    public CommonResult<Boolean> validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        smsCodeService.validateSmsCode(reqDTO);
        return success(true);
    }

}
