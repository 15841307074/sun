package com.htyoudao.youdao.module.promotion.util.wechatLink;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wx.WxActionApi;
import com.htyoudao.youdao.module.promotion.constant.WechatJumpConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlRequest;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlResponse;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.WechatJumpParam;
import com.htyoudao.youdao.module.promotion.service.wechat.ShortUrlService;
import com.htyoudao.youdao.module.promotion.service.wechat.WeChatService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RefreshScope
@Component
public class GenerateUrlLink {

    @DubboReference
    private WxActionApi wxActionApi;

    @Value("${wechat.schemeInfo.envVersion}")
    private String envVersion;

    @Resource
    private WeChatService weChatService;

    @Resource
    private  RestTemplate restTemplate;

    @Resource
    private ShortUrlService shortUrlService;

    @Value("${coupon.sort.server.host}")
    private String API_URL;

    @Value("${coupon.sort.apiKey}")
    private String API_KEY;

    private static final String GENERATE_URL_LINK = "https://api.weixin.qq.com/wxa/generatescheme?access_token=";

    public String getSortUrl(String longUrl) {
        ShortUrlRequest request = ShortUrlRequest.builder()
                .longUrl(longUrl)
                .tags(new ArrayList<>())
                .forwardQuery(true)
                .build();

        ShortUrlResponse response = createShortUrl(request);
        return response.getShortUrl();
    }

    public JSONObject postWxGenerateUrl(WechatJumpParam wechatJump, String token) {
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, Object> jumpWxa = new LinkedHashMap<>();
        jumpWxa.put("path", wechatJump.getPath());
        jumpWxa.put("query", wechatJump.getQuery());
        jumpWxa.put("env_version", envVersion);
        params.put("jump_wxa", jumpWxa);
        params.put("expire_type", 0);
        // 固定参数
        params.put("expire_time", System.currentTimeMillis() / 1000 + 30 * 24 * 60 * 60);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(params);
            String post = HttpUtil.post(GENERATE_URL_LINK + token, json);
            //ResponseEntity<String> responseEntity = restTemplate.postForEntity(GENERATE_URL_LINK + token, httpEntity, String.class);
            return JSONObject.parseObject(post);
        }catch (Exception e){
            return null;
        }

    }
    public String generateUrlLink(WechatJumpParam wechatJump) {
        int j = 0;
        //String wechatToken = weChatService.getWechatToken(true, wechatJump.getBusinessId().toString());
        //String token = "CLOUD_SECRET_REQUIRED";
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        //String accessToken = weChatService.getWechatToken(true, String.valueOf(businessId));
        String wechatToken = wxActionApi.getWechatToken(businessId);
        JSONObject json = postWxGenerateUrl(wechatJump,wechatToken);
        String code = json.getString(WechatJumpConstants.WECHAT_JUMP_ERRCODE);
        while (j < WechatJumpConstants.WECHAT_JUMP_TIMES && code.equals(WechatJumpConstants.WECHAT_JUMP_ERROR_CODE_40001)) {
            log.info("token失效重新获取：{}", json.toJSONString());
            j++;
            json = postWxGenerateUrl(wechatJump, weChatService.getWechatToken(false, wechatJump.getBusinessId().toString()));
            code = json.getString(WechatJumpConstants.WECHAT_JUMP_ERRCODE);
        }
        log.info("generateUrlLink返回地址：{}", json.toJSONString());
        return json.getString(WechatJumpConstants.WECHAT_JUMP_OPEN_LINK);
    }

    public ShortUrlResponse createShortUrl(ShortUrlRequest request) {
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", API_KEY);

        // 创建请求实体
        HttpEntity<ShortUrlRequest> entity = new HttpEntity<>(request, headers);
        log.info(">>> 请求短连服务入参: {}", request);

        // 发送POST请求
        ResponseEntity<ShortUrlResponse> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                ShortUrlResponse.class
        );

        log.info(">>> 请求短连服务响应: {}", response);
        // 验证响应状态
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("API调用失败，状态码：" + response.getStatusCode());
        }
    }

    /**
     * 将 Shlink 短链设置为“已过期”。
     *
     * @param shortCode Shlink 的 shortCode
     */
    public ShortUrlResponse expireShortUrl(String shortCode) {
        return shortUrlService.expireShortUrl(shortCode);
    }

    /**
     * 将 Shlink 短链从“已过期”改回“不过期”。
     *
     * @param shortCode Shlink 的 shortCode
     */
    public ShortUrlResponse unexpireShortUrl(String shortCode) {
        return shortUrlService.unexpireShortUrl(shortCode);
    }

    /**
     * 批量将 Shlink 短链设置为“已过期”。
     */
    public ShortUrlService.BatchUpdateResult batchExpireShortUrls(Collection<String> shortCodes) {
        return shortUrlService.batchExpireShortUrls(shortCodes);
    }

    /**
     * 批量将 Shlink 短链从“已过期”改回“不过期”。
     */
    public ShortUrlService.BatchUpdateResult batchUnexpireShortUrls(Collection<String> shortCodes) {
        return shortUrlService.batchUnexpireShortUrls(shortCodes);
    }
}

