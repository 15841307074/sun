package com.htyoudao.youdao.module.promotion.service.wechat;

import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlRequest;
import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.ShortUrlResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import jakarta.annotation.Resource;

/**
 * @author dht
 */
@Slf4j
@Service
public class ShortUrlService {

    @Value("${coupon.sort.server.host}")
    private String API_URL;

    @Value("${coupon.sort.apiKey}")
    private String API_KEY;

    private final RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    public ShortUrlService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Shlink 要求的时间格式：yyyy-MM-dd'T'HH:mm:ssXXX
     * 例如：2026-02-02T10:30:00+08:00
     */
    private static final DateTimeFormatter SHLINK_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

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
     * 将短链设置为“已过期”（把 validUntil 改为当前时间之前）。
     */
    public ShortUrlResponse expireShortUrl(String shortCode) {
        ZonedDateTime past = ZonedDateTime.now().minusSeconds(5);
        return updateValidUntil(shortCode, past);
    }

    /**
     * 将短链从“已过期”改回“不过期”（把 validUntil 置空）。
     */
    public ShortUrlResponse unexpireShortUrl(String shortCode) {
        return updateValidUntil(shortCode, null);
    }

    /**
     * 批量将短链设置为“已过期”。
     *
     * @param shortCodes Shlink 的 shortCode 列表
     * @return 每个 shortCode 的成功/失败明细（不中断整批）
     */
    public BatchUpdateResult batchExpireShortUrls(Collection<String> shortCodes) {
        ZonedDateTime past = ZonedDateTime.now().minusSeconds(5);
        return batchUpdateValidUntil(shortCodes, past);
    }

    /**
     * 批量将短链从“已过期”改回“不过期”。
     *
     * @param shortCodes Shlink 的 shortCode 列表
     * @return 每个 shortCode 的成功/失败明细（不中断整批）
     */
    public BatchUpdateResult batchUnexpireShortUrls(Collection<String> shortCodes) {
        return batchUpdateValidUntil(shortCodes, null);
    }

    /**
     * 更新短链的 validUntil。
     * - validUntil = null：表示取消过期（不过期）
     * - validUntil != null：表示设置过期时间
     *
     * 说明：这里使用 PATCH，是 Shlink 更新短链的常见方式。
     */
    public ShortUrlResponse updateValidUntil(String shortCode, ZonedDateTime validUntil) {
        if (shortCode == null || shortCode.isBlank()) {
            throw new IllegalArgumentException("shortCode 不能为空");
        }
        String url = buildUpdateUrl(shortCode);

        // 手工构造 JSON，确保 validUntil=null 时也会被传过去（用于“取消过期”）。
        String bodyJson = (validUntil == null)
                ? "{\"validUntil\":null}"
                : "{\"validUntil\":\"" + SHLINK_DATE_TIME_FORMATTER.format(validUntil) + "\"}";

        log.info(">>> 请求短链服务更新入参: shortCode={}, body={}", shortCode, bodyJson);
        HttpPatch patch = new HttpPatch(url);
        patch.setHeader("Content-Type", "application/json");
        patch.setHeader("x-api-key", API_KEY);
        patch.setEntity(new StringEntity(bodyJson, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = HTTP_CLIENT.execute(patch)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String respBody = response.getEntity() == null
                    ? ""
                    : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info(">>> 请求短链服务更新响应: status={}, body={}", statusCode, respBody);

            if (statusCode >= 200 && statusCode < 300) {
                try {
                    return objectMapper.readValue(respBody, ShortUrlResponse.class);
                } catch (Exception e) {
                    throw new RuntimeException("解析短链服务响应失败: " + respBody, e);
                }
            }
            throw new RuntimeException("API调用失败，状态码：" + statusCode + "，响应：" + respBody);
        } catch (Exception e) {
            throw new RuntimeException("调用短链服务更新失败: " + e.getMessage(), e);
        }
    }

    private String buildUpdateUrl(String shortCode) {
        String base = API_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + shortCode;
    }

    private BatchUpdateResult batchUpdateValidUntil(Collection<String> shortCodes, ZonedDateTime validUntil) {
        BatchUpdateResult result = new BatchUpdateResult();
        if (shortCodes == null || shortCodes.isEmpty()) {
            return result;
        }
        for (String shortCode : shortCodes) {
            if (shortCode == null || shortCode.isBlank()) {
                result.getFailed().put(String.valueOf(shortCode), "shortCode 不能为空");
                continue;
            }
            try {
                ShortUrlResponse response = updateValidUntil(shortCode, validUntil);
                result.getSuccess().put(shortCode, response);
            } catch (Exception e) {
                result.getFailed().put(shortCode, e.getMessage());
            }
        }
        return result;
    }

    @Data
    public static class BatchUpdateResult {
        /**
         * key: shortCode
         * value: 更新后的短链信息
         */
        private Map<String, ShortUrlResponse> success = new LinkedHashMap<>();
        /**
         * key: shortCode
         * value: 失败原因（message）
         */
        private Map<String, String> failed = new LinkedHashMap<>();
    }
}
