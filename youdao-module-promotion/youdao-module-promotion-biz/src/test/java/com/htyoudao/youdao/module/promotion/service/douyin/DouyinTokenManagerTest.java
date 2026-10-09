package com.htyoudao.youdao.module.promotion.service.douyin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class DouyinTokenManagerTest {

    @InjectMocks
    private DouyinTokenManager tokenManager;

    @Mock
    private DouyinProperties properties;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setUp()  {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(properties.getBaseUrl()).thenReturn("https://open.douyin.com");
        when(properties.getClientKey()).thenReturn("CLOUD_SECRET_REQUIRED");
        when(properties.getClientSecret()).thenReturn("CLOUD_SECRET_REQUIRED");
    }

    @Test
    public void getAccessToken(){
        String accessToken = tokenManager.getAccessToken();
        System.out.println(accessToken);
    }



}
