package com.htyoudao.youdao.module.member.util;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.framework.app.core.WechatAppConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;

/**
 * @author dht
 */
@Component
public class WechatAccessTokenUtil {

    @Resource
    private WechatAppConfig wechatAppConfig;


    /**
     * 获取access_token
     */
    public String getAccessToken(Long businessId) {
        // 使用前端code获取手机号码（accessToken一定要以get的方式请求）其他参数为json格式
        HashMap<String, Object> tokenParam = new HashMap<>(16);
        businessId = ObjectUtils.isEmpty(businessId) ? BusinessContextHolder.getBusinessId() : businessId;
        tokenParam.put("businessId", businessId);
        tokenParam.put("sourceType", "0090hbgc");
        String accessTokenBody = HttpRequest.get(wechatAppConfig.getTokenUrl())
                .header(Header.AUTHORIZATION, wechatAppConfig.getTokenAuth())
                .form(tokenParam)
                .execute().body();
        @SuppressWarnings("unchecked")
        CommonResult<String> commonResult = JsonUtils.parseObject(accessTokenBody, CommonResult.class);
        assert commonResult != null;
        return commonResult.getCheckedData();
    }
}
