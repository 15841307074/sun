package com.htyoudao.youdao.module.member.service.appletnotice;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;


import java.util.List;

/**
 * @author dht
 */
public interface AppletNoticeService {

    void sendAppletNotice(List<String> valueList, AppletNoticePushVO appletNoticePush);

}