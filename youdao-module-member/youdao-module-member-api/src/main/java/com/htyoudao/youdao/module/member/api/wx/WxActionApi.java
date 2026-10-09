package com.htyoudao.youdao.module.member.api.wx;

import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * <p>
 * 微信操作相关API
 * </p>
 *
 * @author zhangjihe
 * @since 2025-06-18
 */
@Tag(name = "微信通知")
public interface WxActionApi {

    /**
     * 发送小程序通知
     *
     * @param valueList
     * @param appletNoticePush 异步调用需要传入businessId
     * @return
     */
    void sendAppletNotice(List<String> valueList, AppletNoticePushVO appletNoticePush);

    /**
     * 获取微信token
     *
     * @param businessId 异步调用需要传入businessId
     * @return
     */
    String getWechatToken(Long businessId);
}
