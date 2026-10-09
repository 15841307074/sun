package com.htyoudao.youdao.module.system.api.notify;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.htyoudao.youdao.module.system.service.notify.NotifySendService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class NotifyMessageSendApiImpl implements NotifyMessageSendApi {

    @Resource
    private NotifySendService notifySendService;

    @Override
    public CommonResult<Long> sendSingleMessageToAdmin(NotifySendSingleToUserReqDTO reqDTO) {
        return success(notifySendService.sendSingleNotifyToAdmin(reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams()));
    }

    @Override
    public CommonResult<Long> sendSingleMessageToMember(NotifySendSingleToUserReqDTO reqDTO) {
        return success(notifySendService.sendSingleNotifyToMember(reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams()));
    }

}
