package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryConfigurationCacheImpl;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryResultCacheImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryCacheLifetimeTest {
    private final Instant now = Instant.parse("2026-10-12T06:00:00Z");
    private LotterySettingsCacheDataVO settings(Instant end) {
        LotterySettingsCacheDataVO s = new LotterySettingsCacheDataVO();
        s.setId(11L); s.setActivityId(12L); s.setLotteryEndTime(Date.from(end));
        return s;
    }
    @Test void activeActivityRetainsUntilEndPlusSevenDays() {
        assertEquals(Duration.ofDays(10), LotteryCacheLifetime.remaining(settings(now.plus(Duration.ofDays(3))), now));
    }
    @Test void endedActivityUsesRemainingGracePeriod() {
        assertEquals(Duration.ofDays(5), LotteryCacheLifetime.remaining(settings(now.minus(Duration.ofDays(2))), now));
    }
    @Test void readsDoNotRenewTheRetentionDeadline() {
        var s = settings(now);
        assertEquals(Duration.ofDays(7), LotteryCacheLifetime.remaining(s, now));
        assertEquals(Duration.ofDays(6), LotteryCacheLifetime.remaining(s, now.plus(Duration.ofDays(1))));
    }
    @Test void expiredAndBoundaryActivitiesDoNotRecreateLongLivedCache() {
        assertEquals(Duration.ZERO, LotteryCacheLifetime.remaining(settings(now.minus(Duration.ofDays(7))), now));
        assertEquals(Duration.ZERO, LotteryCacheLifetime.remaining(settings(now.minus(Duration.ofDays(8))), now));
    }
    @Test void missingSnapshotDateUsesBoundedFallbackAndActivityDate() {
        assertEquals(Duration.ofMinutes(30), LotteryCacheLifetime.remaining(null, now));
        var s = new LotterySettingsCacheDataVO(); s.setEndDate(Date.from(now));
        assertEquals(Duration.ofDays(7), LotteryCacheLifetime.remaining(s, now));
    }
    @Test void terminalResultAlreadyPastRetentionIsNotCached() {
        var redis = mock(StringRedisTemplate.class);
        var cache = new LotteryResultCacheImpl();
        ReflectionTestUtils.setField(cache, "redis", redis);
        ReflectionTestUtils.setField(cache, "json", new ObjectMapper());
        var draw = new LotteryLedger.Draw(); draw.setDrawStatus("COMPLETED");
        var snapshot = new LotteryDrawSnapshot();
        snapshot.setSettings(settings(Instant.now().minus(Duration.ofDays(8)))); draw.setSnapshot(snapshot);
        cache.publish(draw, new LotteryUserLogVO());
        verifyNoInteractions(redis);
    }
    @SuppressWarnings("unchecked")
    @Test void newListVersionBypassesJustFilledOldListAndUsesFiveMinuteFallback() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        long business = 10;
        String versionKey = LotteryKeys.key(business, 0, "store-list-version", 0);
        String oldKey = LotteryKeys.key(business, 0, "store-list", "1:15");
        String newKey = LotteryKeys.key(business, 0, "store-list", "2:15");
        when(values.get(versionKey)).thenReturn("1", "2");
        when(values.get(oldKey)).thenReturn("[11]");
        when(values.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq(Long.class), eq(business), eq(business), eq(15L))).thenReturn(List.of(11L, 21L));
        var cache = new LotteryConfigurationCacheImpl();
        ReflectionTestUtils.setField(cache, "redis", redis); ReflectionTestUtils.setField(cache, "jdbc", jdbc);
        ReflectionTestUtils.setField(cache, "json", new ObjectMapper());
        BusinessContextHolder.setBusinessId(business);
        try {
            assertEquals(List.of(11L), cache.storeSettings(15));
            assertEquals(List.of(11L, 21L), cache.storeSettings(15));
            verify(values).get(oldKey); verify(values).get(newKey);
            verify(values).set(eq(newKey), eq("[11,21]"), argThat((Duration d) -> d.getSeconds() >= 300 && d.getSeconds() <= 330));
        } finally { BusinessContextHolder.clear(); }
    }
}
