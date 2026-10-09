package com.htyoudao.youdao.module.member.api.notice;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;
import com.htyoudao.youdao.module.member.service.appletnotice.AppletNoticeService;
import com.htyoudao.youdao.module.member.util.WechatAccessTokenUtil;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;


@DubboService
public class WxActionApiImpl implements WxActionApi {

    @Resource
    private AppletNoticeService appletNoticeService;

    @Resource
    private WechatAccessTokenUtil wechatAccessTokenUtil;

    @Override
    public void sendAppletNotice(List<String> valueList, AppletNoticePushVO appletNoticePush) {
         appletNoticeService.sendAppletNotice(valueList, appletNoticePush);
    }

    @Override
    public String getWechatToken(Long businessId) {
        return wechatAccessTokenUtil.getAccessToken(businessId);
    }
}
