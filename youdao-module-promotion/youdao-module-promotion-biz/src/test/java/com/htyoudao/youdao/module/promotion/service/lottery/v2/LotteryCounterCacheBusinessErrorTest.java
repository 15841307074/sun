package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryCounterCacheImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LotteryCounterCacheBusinessErrorTest {

    private LotteryCounterCacheImpl cache;
    private StringRedisTemplate redis;
    private ValueOperations<String, String> values;
    private JdbcTemplate jdbc;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        cache = new LotteryCounterCacheImpl();
        redis = mock(StringRedisTemplate.class);
        values = mock(ValueOperations.class);
        jdbc = mock(JdbcTemplate.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null);
        ReflectionTestUtils.setField(cache, "redis", redis);
        ReflectionTestUtils.setField(cache, "jdbc", jdbc);
        ReflectionTestUtils.setField(cache, "json", new ObjectMapper());
    }

    @Test
    void shouldReturnBusinessErrorWhenLocalRefillLimitIsReached() {
        AtomicInteger refills = (AtomicInteger) ReflectionTestUtils.getField(cache, "refills");
        refills.set(4);

        assertRetryBusinessError(() -> cache.read(10L, 20L, 30L, "20261016"));
        assertEquals(4, refills.get());
    }

    @Test
    void shouldReturnBusinessErrorWhenRefillLockIsBusy() {
        when(values.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);

        assertRetryBusinessError(() -> cache.read(10L, 20L, 30L, "20261016"));
    }

    @Test
    void shouldReturnBusinessErrorWhenCounterVersionChangesDuringRefill() {
        when(values.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.<Map<String, Object>>of());
        when(redis.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(0L);

        assertRetryBusinessError(() -> cache.read(10L, 20L, 30L, "20261016"));
    }

    private void assertRetryBusinessError(Runnable action) {
        ServiceException error = assertThrows(ServiceException.class, action::run);
        assertEquals(LOTTERY_SYSTEM_AGAIN.getCode(), error.getCode());
        assertEquals(LOTTERY_SYSTEM_AGAIN.getMsg(), error.getMessage());
    }
}
