package com.htyoudao.youdao.module.promotion.api.ActivityChannelNameApi;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.activitychannelname.ActivityChannelNameApi;
import com.htyoudao.youdao.module.promotion.service.activityChannelName.ActivityChannelNameService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@DubboService
@Validated
@Slf4j
public class ActivityChannelNameApiImpl implements ActivityChannelNameApi {

    @Resource
    private ActivityChannelNameService activityChannelNameService;


    @Override
    public CommonResult<Map<Long, String>> selectChannelNameMap() {
        return CommonResult.success(activityChannelNameService.selectChannelNameMap());
    }
}
