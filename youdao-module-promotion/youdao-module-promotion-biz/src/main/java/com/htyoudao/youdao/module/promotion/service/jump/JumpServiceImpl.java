package com.htyoudao.youdao.module.promotion.service.jump;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.util.http.HttpUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.AppCodeReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpReqVO;
import com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo.WechatJumpRespVO;
import com.htyoudao.youdao.module.promotion.util.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dht
 */
@Service
@Slf4j
public class JumpServiceImpl implements JumpService{

    @Value("${md5.secret}")
    private String md5Secret;

//    @Value("${wechat.access.token}")
//    private String accessToken;

    @DubboReference
    private WxActionApi wxActionApi;

    /**
     * 最大重试次数
     */
    private static int maxTryNumber = 3;

    @Value("${wx.env_version}")
    private String envVersion;

    @Value("${wx.generate_url}")
    private String wxGenerateUrl;

    @Value("${wx.app_code_url}")
    private String wxGetAppCodeUrl;

    @Override
    public WechatJumpRespVO wechatJump(WechatJumpReqVO wechatJumpReqVO) {
        WechatJumpRespVO wechatJump = new WechatJumpRespVO();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> params = mapper.convertValue(wechatJumpReqVO, new TypeReference<Map<String, String>>() {});
        String sign = wechatJumpReqVO.getSign();
        params.remove("sign");
        String checkSign = Md5Utils.getMd5Sign(params, md5Secret);
        if (!sign.equals(checkSign)) {
            wechatJump.setPath("验签失败");
            return wechatJump;
        }

        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        String accessToken = wxActionApi.getWechatToken(businessId);

        int j = 0;
        JSONObject json = postWxGenerateUrl(wechatJumpReqVO, accessToken);
        String errcode = json.getString("errcode");
        while(j < maxTryNumber && errcode.equals("40001")){
            log.info("token失效重新获取：{}", json.toJSONString());
            j ++;
            json = postWxGenerateUrl(wechatJumpReqVO, accessToken)  ;
            errcode = json.getString("errcode");
        }
        log.info("generateUrlLink返回地址：{}", json.toJSONString());
        String openLink = json.getString("openlink");
        return wechatJump.setPath(openLink);
    }

    @Override
    public byte[] getUnlimitedMiniProgramCode(AppCodeReqVO appCodeReqVO) {
        try {
            Long businessId = BusinessContextHolder.getRequiredBusinessId();
            String accessToken = wxActionApi.getWechatToken(businessId);

            if (accessToken == null || accessToken.isEmpty()) {
                log.error("获取access_token失败");
                return null;
            }

            // 构建请求参数
            Map<String, Object> appCodeMap = new LinkedHashMap<>();

            // 1. 校验scene参数
            String scene = appCodeReqVO.getScene();
            appCodeMap.put("scene", scene);

            // 2. 校验page参数
            String page = appCodeReqVO.getPage();
            appCodeMap.put("page", page);

            // 3. 设置其他参数
            appCodeMap.put("check_path", true);

            // 4. 环境版本

            appCodeMap.put("env_version", envVersion);

            Map<String,String> headers = new HashMap<>(8);
            headers.put("Content-Type",  "application/json");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String jsonString = objectMapper.writeValueAsString(appCodeMap);
                // 4. 发送POST请求获取图片字节数组
                return HttpRequest.post(wxGetAppCodeUrl + accessToken)
                        .addHeaders(headers)
                        .body(jsonString)
                        .execute().bodyBytes();

            }catch (Exception e){
                return null;
            }


        } catch (Exception e) {
            log.error("获取小程序码异常", e);
            return null;
        }
    }


    public JSONObject postWxGenerateUrl(WechatJumpReqVO wechatJump, String token){
        Map<String, Object> params = new LinkedHashMap<>();

//        {
//            "jump_wxa":
//            {
//                "path": "/pages/publishHomework/publishHomework",
//                    "query": "",
//                    "env_version": "release"
//            },
//            "is_expire":true,
//                "expire_type":1,
//                "expire_interval":1
//        }
        Map<String, Object> jumpWxa = new LinkedHashMap<>();
        jumpWxa.put("path", wechatJump.getPath());
        jumpWxa.put("query", wechatJump.getQuery());
        jumpWxa.put("env_version", envVersion);
        params.put("jump_wxa", jumpWxa);
        params.put("expire_type", 0);
        // 固定参数
        params.put("expire_time", System.currentTimeMillis()/1000 + 15 * 60);
        Map<String,String> headers = new HashMap<>(8);
        headers.put("Content-Type",  "application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(params);
            String post = HttpUtils.post(wxGenerateUrl + token, headers, jsonString);
            return JSONObject.parseObject(post);
        }catch (Exception e){
            return null;
        }
    }

}
