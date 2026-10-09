package com.htyoudao.youdao.module.promotion.service.appletnotice;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.http.HttpUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.promotion.controller.app.appletnotice.AppletNoticePush;
import com.htyoudao.youdao.module.promotion.controller.app.appletnotice.AppletNoticePushVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.appletNoticePushTemplate.AppletNoticePushTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CommonServiceImpl implements ICommonService{

    private Map<String, AppletNoticePushTemplate> appletNoticePushTemplateMap = new HashMap<>();

    @Autowired
    private AppletNoticePushTemplateService appletNoticePushTemplateService;

    @DubboReference
    private WxActionApi wxActionApi;

    /**
     * 异步推送小程序通知：不抛异常、不关心失败（失败只打日志）
     * 说明：
     * 1) 修复 valueList.indexOf(value) 可能替换错位的问题（value 重复时 indexOf 永远取第一个）
     * 2) 模板获取做空校验，避免 get(0) NPE/越界
     * 3) accessToken 获取失败、HTTP 返回空、JSON 解析失败等都只打日志
     * 4) 日志避免全量打印敏感信息（可按需调整）
     */
    @Override
    @Async
    public void sendAppletNotice(List<String> valueList, AppletNoticePushVO appletNoticePush) {
        if (appletNoticePush == null) {
            log.warn("==> 【sendAppletNotice】入参为空 appletNoticePush=null");
            return;
        }
        if (CollectionUtil.isEmpty(valueList)) {
            log.warn("==> 【sendAppletNotice】valueList 为空 templateType={}, openId={}",
                    appletNoticePush.getTemplateType(), appletNoticePush.getOpenId());
            return;
        }

        Integer templateType = appletNoticePush.getTemplateType();
        String openId = appletNoticePush.getOpenId();

        try {
            // 1) 获取模板（带缓存）
            String key = String.valueOf(templateType);
            AppletNoticePushTemplate tpl = appletNoticePushTemplateMap.get(key);
            if (ObjectUtil.isEmpty(tpl)) {
                List<AppletNoticePushTemplate> list = appletNoticePushTemplateService.getListByTemplateType(templateType);
                if (CollectionUtil.isEmpty(list)) {
                    log.warn("==> 【sendAppletNotice】模板不存在 templateType={}, openId={}", templateType, openId);
                    return;
                }
                tpl = list.get(0);
                appletNoticePushTemplateMap.put(key, tpl);
            }

            // 2) businessId / token
            Long businessId = null;
            try {
                businessId = BusinessContextHolder.getRequiredBusinessId();
            } catch (Exception ignore) {
                // 兜底
                businessId = appletNoticePush.getBusinessId();
            }
            if (businessId == null) {
                log.warn("==> 【sendAppletNotice】businessId 为空 templateType={}, openId={}", templateType, openId);
                return;
            }

            String accessToken = wxActionApi.getWechatToken(businessId);
            if (StrUtil.isBlank(accessToken)) {
                log.warn("==> 【sendAppletNotice】accessToken 获取失败 businessId={}, templateType={}, openId={}",
                        businessId, templateType, openId);
                return;
            }

            // 3) 填充模板内容（按下标替换，避免重复值导致 indexOf 错位）
            String content = tpl.getContent();
            for (int i = 0; i < valueList.size(); i++) {
                content = content.replace("value" + i, valueList.get(i));
            }

            JSONObject data;
            try {
                data = JSONObject.parseObject(content);
            } catch (Exception e) {
                log.warn("==> 【sendAppletNotice】模板内容不是合法 JSON templateType={}, openId={}, content={}",
                        templateType, openId, content);
                return;
            }

            // 4) 组装请求体
            AppletNoticePush noticePush = new AppletNoticePush();
            noticePush.setLang("zh_CN");
            noticePush.setMiniprogram_state(tpl.getMiniProgramState());
            noticePush.setTouser(openId);
            noticePush.setData(data);
            noticePush.setAccess_token(accessToken);
            noticePush.setTemplate_id(tpl.getTemplateId());

            String appletUrl = tpl.getAppletUrl() + accessToken;

            Map<String, String> headers = new HashMap<>(4);
            headers.put("Content-Type", "application/json");

            // 注意：from 这层 JSONObject 转来转去没必要，直接发字符串即可
            String payload = JSONObject.toJSONString(noticePush);

            log.info("==> 【sendAppletNotice】微信通知调用 businessId={}, templateType={}, openId={}",
                    businessId, templateType, openId);

            String result = HttpUtils.post(appletUrl, headers, payload);
            if (StrUtil.isBlank(result)) {
                log.warn("==> 【sendAppletNotice】微信通知返回为空 businessId={}, templateType={}, openId={}",
                        businessId, templateType, openId);
                return;
            }

            JSONObject resp;
            try {
                resp = JSONObject.parseObject(result);
            } catch (Exception e) {
                log.warn("==> 【sendAppletNotice】微信通知返回非 JSON businessId={}, templateType={}, openId={}, result={}",
                        businessId, templateType, openId, result);
                return;
            }

            int errCode = resp.getIntValue("errcode");
            if (errCode != 0) {
                log.warn("==> 【sendAppletNotice】微信通知失败 businessId={}, templateType={}, openId={}, errcode={}, errmsg={}, resp={}",
                        businessId, templateType, openId, errCode, resp.getString("errmsg"), resp.toJSONString());
                return;
            }

            log.info("==> 【sendAppletNotice】微信通知成功 businessId={}, templateType={}, openId={}",
                    businessId, templateType, openId);

        } catch (Exception e) {
            // 异步场景：任何异常只打日志，不影响主流程
            log.warn("==> 【sendAppletNotice】异常 templateType={}, openId={}, businessId={}",
                    templateType, openId, appletNoticePush.getBusinessId(), e);
        }
    }

}
