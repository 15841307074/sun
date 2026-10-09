package com.htyoudao.youdao.module.promotion.service.appletnotice;

import com.htyoudao.youdao.module.promotion.controller.app.appletnotice.AppletNoticePushVO;

import java.util.List;

/**
 * @author dht
 */
public interface ICommonService  {

    void sendAppletNotice(List<String> valueList, AppletNoticePushVO appletNoticePush);

}