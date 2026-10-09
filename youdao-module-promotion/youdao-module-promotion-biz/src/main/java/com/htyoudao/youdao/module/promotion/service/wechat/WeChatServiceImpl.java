package com.htyoudao.youdao.module.promotion.service.wechat;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dht
 */
@Service
public class WeChatServiceImpl implements WeChatService{

    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";


    //@Resource
    //private WechatAppConfig wechatAppConfig;


    @Override
    public String getWechatToken(boolean b, String businessId) {
        if(ObjectUtil.isEmpty(businessId)){
            businessId = String.valueOf(BusinessContextHolder.getBusinessId());
        }
        if(ObjectUtil.isEmpty(businessId)){
            return "token获取失败";
        }


        Map<String, Object> params = new HashMap<>();
        // 固定参数
//        params.put("appid", wechatAppConfig.getAppId(projectOwnerShip));
//        params.put("secret", wechatAppConfig.getAppSecret(projectOwnerShip));
        params.put("grant_type", "client_credential");
        String resultJson = HttpUtil.get(TOKEN_URL, params);
        JSONObject json = JSONObject.parseObject(resultJson);
        String accessToken = json.getString("access_token");
        return accessToken;
    }
}
