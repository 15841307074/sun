package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.*;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryCachePublicationTest {
    private LotteryCachePublisherImpl publisher;
    private JdbcTemplate jdbc;
    private StringRedisTemplate redis;
    private LotteryCacheExecutor executor;
    @BeforeEach void setup() {
        BusinessContextHolder.setBusinessId(10L);publisher=new LotteryCachePublisherImpl();jdbc=mock(JdbcTemplate.class);redis=mock(StringRedisTemplate.class);executor=mock(LotteryCacheExecutor.class);
        ReflectionTestUtils.setField(publisher,"jdbc",jdbc);ReflectionTestUtils.setField(publisher,"redis",redis);ReflectionTestUtils.setField(publisher,"executor",executor);
        when(redis.opsForValue()).thenReturn(mock(ValueOperations.class));
    }
    @AfterEach void clear(){BusinessContextHolder.clear();TransactionSynchronizationManager.clear();}
    @Test void successfulCommitPublishesImmediatelyAndClearsDirtyWithoutIdleScan() {
        var cfg=new LotterySettingsDO();cfg.setId(20L);cfg.setActivityId(21L);cfg.setConfigVersion(3L);
        var pending=new AtomicReference<Runnable>();doAnswer(call->{pending.set(call.getArgument(0));return null;}).when(executor).submit(any());
        TransactionSynchronizationManager.setActualTransactionActive(true);TransactionSynchronizationManager.initSynchronization();
        publisher.enqueue(cfg);verifyNoInteractions(redis);
        TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCommit());
        verify(redis).execute(any(RedisScript.class),anyList(),any(Object[].class));assertNotNull(pending.get());
        TransactionSynchronizationManager.clear();pending.get().run();
        verify(jdbc).update(eq("UPDATE lottery_v2_cache_task SET dirty=0,attempts=0 WHERE business_id=? AND activity_id=? AND config_version=?"),eq(10L),eq(21L),eq(3L));
    }
    @Test void publicationFailureRetainsPendingRecordAndDoesNotScheduleRetry() {
        var cfg=new LotterySettingsDO();cfg.setId(20L);cfg.setActivityId(21L);cfg.setConfigVersion(3L);
        when(redis.execute(any(RedisScript.class),anyList(),any(Object[].class))).thenThrow(new IllegalStateException("Redis unavailable"));
        assertDoesNotThrow(()->publisher.enqueue(cfg));verifyNoInteractions(executor);
        verify(jdbc,never()).queryForList(anyString(),any(Object[].class));
        verify(jdbc,never()).update(startsWith("UPDATE lottery_v2_cache_task SET dirty=0"),any(Object[].class));
    }
    @Test void cachePublishersHaveNoPeriodicDatabaseScanning() {
        for(var type:List.of(LotteryCachePublisherImpl.class,LotteryCounterCacheImpl.class))
            for(var method:type.getDeclaredMethods())assertNull(method.getAnnotation(Scheduled.class));
    }
}
