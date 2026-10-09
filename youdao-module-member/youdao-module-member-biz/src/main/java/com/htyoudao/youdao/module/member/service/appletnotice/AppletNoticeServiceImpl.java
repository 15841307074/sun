package com.htyoudao.youdao.module.member.service.appletnotice;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.htyoudao.youdao.framework.common.util.http.HttpUtils;
import com.htyoudao.youdao.module.member.controller.app.appletnotice.AppletNoticePush;
import com.htyoudao.youdao.module.member.api.wx.VO.AppletNoticePushVO;
import com.htyoudao.youdao.module.member.dal.dataobject.appletNoticePushTemplate.AppletNoticePushTemplate;
import com.htyoudao.youdao.module.member.util.WechatAccessTokenUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AppletNoticeServiceImpl implements AppletNoticeService {

    private Map<String, AppletNoticePushTemplate> appletNoticePushTemplateMap = new HashMap<>();

    @Resource
    private AppletNoticePushTemplateService appletNoticePushTemplateService;

    @Resource
    private WechatAccessTokenUtil wechatAccessTokenUtil;

    @Override
    @Async
    public void sendAppletNotice(List<String> valueList, AppletNoticePushVO appletNoticePush) {

        try {
            AppletNoticePushTemplate appletNoticePushTemplate =
                    appletNoticePushTemplateMap.get(String.valueOf(appletNoticePush.getTemplateType()));

            if (ObjectUtil.isEmpty(appletNoticePushTemplate)) {
                List<AppletNoticePushTemplate> list =
                        appletNoticePushTemplateService.getListByTemplateType(appletNoticePush.getTemplateType());
                if (CollectionUtil.isEmpty(list)) {
                    log.warn("==> 【sendAppletNotice】模板不存在 templateType={}, businessId={}",
                            appletNoticePush.getTemplateType(), appletNoticePush.getBusinessId());
                    return;
                }
                appletNoticePushTemplate = list.get(0);
                appletNoticePushTemplateMap.put(
                        String.valueOf(appletNoticePush.getTemplateType()),
                        appletNoticePushTemplate
                );
            }

            String accessToken = wechatAccessTokenUtil.getAccessToken(appletNoticePush.getBusinessId());

            // 组装模板内容
            String content = appletNoticePushTemplate.getContent();
            for (int i = 0; i < valueList.size(); i++) {
                content = content.replace("value" + i, valueList.get(i));
            }

            JSONObject data = JSONObject.parseObject(content);

            AppletNoticePush noticePush = new AppletNoticePush();
            noticePush.setLang("zh_CN");
            noticePush.setMiniprogram_state(appletNoticePushTemplate.getMiniProgramState());
            noticePush.setTouser(appletNoticePush.getOpenId());
            noticePush.setData(data);
            noticePush.setAccess_token(accessToken);
            noticePush.setTemplate_id(appletNoticePushTemplate.getTemplateId());

            String appletUrl = appletNoticePushTemplate.getAppletUrl() + accessToken;

            Map<String, String> headers = new HashMap<>(8);
            headers.put("Content-Type", "application/json");

            JSONObject payload = JSONObject.parseObject(JSONObject.toJSONString(noticePush));
            log.info("==> 【sendAppletNotice】微信通知调用 openId={}, templateType={}",
                    appletNoticePush.getOpenId(), appletNoticePush.getTemplateType());

            String result = HttpUtils.post(appletUrl, headers, payload.toJSONString());
            JSONObject resp = JSONObject.parseObject(result);

            int errCode = resp.getIntValue("errcode");
            if (errCode != 0) {
                log.warn("==> 【sendAppletNotice】微信通知失败 openId={}, templateType={}, errcode={}, errmsg={}, resp={}",
                        appletNoticePush.getOpenId(),
                        appletNoticePush.getTemplateType(),
                        errCode,
                        resp.getString("errmsg"),
                        resp.toJSONString());
                return;
            }

            log.info("==> 【sendAppletNotice】微信通知成功 openId={}, templateType={}",
                    appletNoticePush.getOpenId(), appletNoticePush.getTemplateType());

        } catch (Exception e) {
            // 异步方法里：任何异常只打日志，不向上抛
            log.warn("==> 【sendAppletNotice】异步发送异常 openId={}, templateType={}, businessId={}",
                    appletNoticePush.getOpenId(),
                    appletNoticePush.getTemplateType(),
                    appletNoticePush.getBusinessId(),
                    e);
        }
    }

}
