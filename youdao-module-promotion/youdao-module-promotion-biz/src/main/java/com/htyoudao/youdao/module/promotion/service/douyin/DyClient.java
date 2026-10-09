package com.htyoudao.youdao.module.promotion.service.douyin;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.CancelVerifyRequest;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.CancelVerifyResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.DouYinApiResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.DouYinApiResponse.ResponseExtra;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareRequest;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.PrepareResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.QueryResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.VerifyRequest;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.VerifyResponse;
import com.htyoudao.youdao.module.promotion.service.douyin.dto.VerifyResponse.VerifyResult;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class DyClient {

    @Resource
    private DouyinTokenManager tokenManager;
    @Resource
    private DouyinProperties properties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final Integer SUCCESS = 0;

    /**
     * 验券准备
     */
    public DouYinApiResponse<PrepareResponse> prepare(@Validated PrepareRequest request) {

        String url = properties.getBaseUrl() + "/goodlife/v1/fulfilment/certificate/prepare/";

        String encryptedData = getEncryptedDataFromShortLink(request.getShortLink());

        url = UriComponentsBuilder.fromUriString(url)
            .queryParamIfPresent("encrypted_data", Optional.ofNullable(encryptedData))
            .queryParamIfPresent("code", Optional.ofNullable(request.getCode()))
            .queryParam("poi_id", request.getPoiId())
            .queryParam("account_id", properties.getAccountId())
            .toUriString();

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        String body = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        DouYinApiResponse<PrepareResponse> response = parseApiResponse(body, PrepareResponse.class);
        checkPrepareResponse(response.getData());
        return response;
    }

    /**
     * 通过短链获取 encrypted_data（即 object_id 参数）
     */
    private String getEncryptedDataFromShortLink(String shortLink) {
        if (StringUtils.isBlank(shortLink)) {
            return null;
        }

        // 建立连接
        int responseCode = 0;
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(shortLink).openConnection();
            connection.setInstanceFollowRedirects(false); // 不自动跳转，手动获取 Location
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            connection.connect();
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            log.error("", e);
            throw new ServiceException(ErrorCodeConstants.QR_CODE_INFO_ERROR);
        }

        if (responseCode != 301 && responseCode != 302) {
            log.error("不是跳转链接，响应码: {}", responseCode);
            throw new ServiceException(ErrorCodeConstants.QR_CODE_INFO_ERROR);
        }

        String location = connection.getHeaderField("Location");
        if (location == null || location.isEmpty()) {
            log.error("未获取到跳转地址");
            throw new ServiceException(ErrorCodeConstants.QR_CODE_INFO_ERROR);
        }

        // 提取 object_id
        Matcher matcher = Pattern.compile("object_id=([A-Za-z0-9%]+)").matcher(location);
        if (matcher.find()) {
            return matcher.group(1); // object_id 就是 encrypted_data
        } else {
            log.error("未从跳转链接中提取到 object_id");
            throw new ServiceException(ErrorCodeConstants.QR_CODE_INFO_ERROR);
        }
    }

    /**
     * 核销券
     */
    public DouYinApiResponse<VerifyResponse> verify(@Validated VerifyRequest verifyRequest) {
        String url = properties.getBaseUrl() + "/goodlife/v1/fulfilment/certificate/verify/";

        Map<String, Object> body = Map.of(
            "encrypted_codes", verifyRequest.getEncryptedCodes(),
            "verify_token", verifyRequest.getVerifyToken(),
            "poi_id", verifyRequest.getPoiId()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(body, buildHeaders());
        String json = restTemplate.postForObject(url, entity, String.class);
        DouYinApiResponse<VerifyResponse> response = parseApiResponse(json, VerifyResponse.class);
        checkVerifyResponse(response.getData());

        return response;
    }


    /**
     * 撤销核销
     */
    public DouYinApiResponse<CancelVerifyResponse> cancelVerify(@Validated CancelVerifyRequest request) {
        String url = properties.getBaseUrl() + "/goodlife/v1/fulfilment/certificate/cancel/";
        Map<String, String> body = new HashMap<>();
        body.put("shop_order_id", request.getShopOrderId());
        body.put("account_id", properties.getAccountId());
        body.put("verify_id", request.getVerifyId());
        body.put("certificate_id", request.getCertificateId());

        if (request.getCancelToken() != null) {
            body.put("cancel_token", request.getCancelToken());
        }

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, buildHeaders());
        String json = restTemplate.postForObject(url, entity, String.class);
        DouYinApiResponse<CancelVerifyResponse> response = parseApiResponse(json, CancelVerifyResponse.class);
        checkCancelResponse(response.getData());

        return response;
    }


    /**
     * 核销券
     */
    public DouYinApiResponse<QueryResponse> query(@Validated String orderId) {
        String url = properties.getBaseUrl() + "/goodlife/v1/fulfilment/certificate/query/";

        url = UriComponentsBuilder.fromUriString(url)
            .queryParam("order_id", orderId)
            .queryParam("account_id", properties.getAccountId())
            .toUriString();


        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        String body = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        DouYinApiResponse<QueryResponse> response = parseApiResponse(body, QueryResponse.class);
        return response;

    }



    private void checkPrepareResponse(PrepareResponse data) {
        if (!Objects.equals(data.getErrorCode(), SUCCESS)){
            log.warn("douyin prepare failed：{}", JSON.toJSONString(data));
            throw new ServiceException(ErrorCodeConstants.DOUYIN_COUPON_PREPARE_FAILED);
        }

        if (CollectionUtils.isEmpty(data.getCertificates())){
            log.warn("douyin prepare failed：{}", JSON.toJSONString(data));
            throw new ServiceException(ErrorCodeConstants.DOUYIN_COUPON_PREPARE_FAILED);
        }
    }
    private void checkVerifyResponse(VerifyResponse data) {
        if (!Objects.equals(data.getErrorCode(), SUCCESS)) {
            log.warn("douyin verify failed：{}", JSON.toJSONString(data));
            throw new ServiceException(ErrorCodeConstants.DOUYIN_COUPON_VERIFY_FAILED);
        }

        for (VerifyResult verifyResult : data.getVerifyResults()) {
            if (!Objects.equals(verifyResult.getResult(), SUCCESS)) {
                log.warn("douyin verify failed：{}", JSON.toJSONString(verifyResult));
                Integer errorCode = ErrorCodeConstants.DOUYIN_COUPON_VERIFY_FAILED.getCode();
                throw new ServiceException(errorCode, verifyResult.getMessage());
            }
        }
    }
    private void checkCancelResponse(CancelVerifyResponse data) {
        if (!Objects.equals(data.getErrorCode(), SUCCESS)){
            log.warn("douyin cancel failed：{}", JSON.toJSONString(data));
            throw new ServiceException(ErrorCodeConstants.DOUYIN_COUPON_CANCEL_FAILED);
        }
    }


    private HttpHeaders buildHeaders() {
        String token = tokenManager.getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access-token", token);
        if (StringUtils.isNotEmpty(properties.getTransitAccount())) {
            headers.set("Rpc-Transit-Life-Account", properties.getTransitAccount());
        }
        return headers;
    }

    private <T> DouYinApiResponse<T> parseApiResponse(String json, Class<T> dataType) {

        log.info("douyin api result:{}", json);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            // 创建类型工厂
            JavaType type = objectMapper.getTypeFactory().constructParametricType(DouYinApiResponse.class, dataType);

            // 解析JSON
            DouYinApiResponse<T> response = objectMapper.readValue(json, type);

            ResponseExtra extra = response.getExtra();
            if (extra.getErrorCode() != 0) {
                log.error("douyin api error:{}", json);
                throw new ServiceException(extra.getErrorCode(), extra.getDescription());
            }
            return response;
        } catch (JsonProcessingException e) {
            log.error("", e);
            throw new ServiceException(ErrorCodeConstants.DOUYIN_JSON_PROCESSING);
        }
    }
}