package com.htyoudao.youdao.module.promotion.controller.app.appletnotice;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class AppletNoticePush {

    private String access_token;

    private String template_id;

    private String touser;

    private JSONObject data;

    private String miniprogram_state;

    private String lang;
}
