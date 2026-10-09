package com.htyoudao.youdao.module.promotion.service.douyin;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class DouyinTokenManager {

    private static final String TOKEN_URL = "https://open.douyin.com/oauth/client_token/";
    private static final String REDIS_KEY_PREFIX = "douyin:access_token:";

    @Resource
    private DouyinProperties properties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReentrantLock lock = new ReentrantLock();


    public String getAccessToken(){
        String redisKey = REDIS_KEY_PREFIX + properties.getClientKey();

        // 先从 Redis 获取
        String token = stringRedisTemplate.opsForValue().get(redisKey);
        if (token != null && !token.isBlank()) {
            return token;
        }

        // 加锁防止并发刷新
        lock.lock();
        try {
            // 双重检查
            token = stringRedisTemplate.opsForValue().get(redisKey);
            if (token != null && !token.isBlank()) {
                return token;
            }

            // 请求抖音接口
            String requestBody = String.format(
                "{\"grant_type\":\"client_credential\",\"client_key\":\"%s\",\"client_secret\":\"%s\"}",
                properties.getClientKey(), properties.getClientSecret()
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = null;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                log.error("", e);
                throw new ServiceException(ErrorCodeConstants.DOUYIN_API_REQUEST_FAILED);
            }

            if (response.statusCode() != 200) {
                log.error("抖音Token请求失败:{}", response.body());
                throw new ServiceException(ErrorCodeConstants.DOUYIN_TOKEN_REQUEST_FAILED);
            }

            JsonNode data = null;
            try {
                data = objectMapper.readTree(response.body()).path("data");
            } catch (JsonProcessingException e) {
                log.error("json format error：{}", response.body());
                throw new RuntimeException(e);
            }
            int errorCode = data.path("error_code").asInt();
            if (errorCode != 0) {
                log.error("抖音接口错误: {}", data.path("description").asText());
                throw new ServiceException(ErrorCodeConstants.DOUYIN_API_ERROR);
            }

            token = data.path("access_token").asText();

            // 写入 Redis
            stringRedisTemplate.opsForValue().set(redisKey, token, 2, TimeUnit.HOURS);

            return token;
        } finally {
            lock.unlock();
        }
    }
}