package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryWinnerFeedImpl;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryWinnerFeedCacheTest {
    private LotteryWinnerFeedImpl feed;
    private StringRedisTemplate redis;
    private LotteryLogDO row;
    private Long previousBusiness;
    @BeforeEach void setup() {
        previousBusiness=BusinessContextHolder.getBusinessId();BusinessContextHolder.setBusinessId(10L);
        feed=new LotteryWinnerFeedImpl();redis=mock(StringRedisTemplate.class);
        ReflectionTestUtils.setField(feed,"redis",redis);ReflectionTestUtils.setField(feed,"json",new ObjectMapper());
        row=new LotteryLogDO();row.setId(100L);row.setBusinessId(10L);row.setIsGuarantees(0);row.setPrizeType(2);
        row.setCreateTime(LocalDateTime.now());row.setMemberMobile("13812345678");row.setPrizeName("100积分");
        row.setLotteryEndTime(Date.from(Instant.now().plus(Duration.ofDays(2))));
    }
    @AfterEach void cleanup() {
        if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);BusinessContextHolder.setBusinessId(previousBusiness);
    }
    @Test void publishesMaskedWinnerWithFixedActivityExpiryAndReadsItBack() {
        feed.afterLogSaved(30,row);
        var keys=ArgumentCaptor.forClass(List.class);var expiry=ArgumentCaptor.forClass(String.class);var entry=ArgumentCaptor.forClass(String.class);
        verify(redis).execute(any(RedisScript.class),keys.capture(),expiry.capture(),entry.capture());
        assertEquals(List.of(LotteryKeys.key(10,30,"winners","recent")),keys.getValue());
        assertEquals(row.getLotteryEndTime().getTime()+Duration.ofDays(7).toMillis(),Long.parseLong(expiry.getValue()));
        assertFalse(entry.getValue().contains("13812345678"));
        var zset=mock(ZSetOperations.class);when(redis.opsForZSet()).thenReturn(zset);
        when(zset.reverseRange(keys.getValue().get(0),0,19)).thenReturn(new LinkedHashSet<>(List.of(entry.getValue())));
        var result=feed.latest(30L);assertEquals(1,result.size());assertEquals("138****5678",result.get(0).getMemberMobile());assertEquals("100积分",result.get(0).getPrizeName());
        clearInvocations(redis);feed.afterLogSaved(30,row);
        verify(redis).execute(any(RedisScript.class),eq(keys.getValue()),eq(expiry.getValue()),anyString());
    }
    @Test void guaranteeAndNoPrizeDoNotEnterFeed() {
        row.setIsGuarantees(1);feed.afterLogSaved(30,row);
        row.setIsGuarantees(0);row.setPrizeType(4);feed.afterLogSaved(30,row);verifyNoInteractions(redis);
    }
    @Test void publishesOnlyAfterCommit() {
        TransactionSynchronizationManager.setActualTransactionActive(true);TransactionSynchronizationManager.initSynchronization();
        feed.afterLogSaved(30,row);verifyNoInteractions(redis);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(redis).execute(any(RedisScript.class),anyList(),anyString(),anyString());
    }
    @Test void rollbackDoesNotPublish() {
        TransactionSynchronizationManager.setActualTransactionActive(true);TransactionSynchronizationManager.initSynchronization();
        feed.afterLogSaved(30,row);
        TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verifyNoInteractions(redis);
    }
    @Test void expiredOrMissingActivityEndDoesNotCreateAnUnboundedCache() {
        row.setLotteryEndTime(Date.from(Instant.now().minus(Duration.ofDays(8))));feed.afterLogSaved(30,row);
        row.setLotteryEndTime(null);feed.afterLogSaved(30,row);verifyNoInteractions(redis);
    }
    @Test void cacheWriteFailureDoesNotFailAward() {
        when(redis.execute(any(RedisScript.class),anyList(),anyString(),anyString())).thenThrow(new IllegalStateException("redis unavailable"));
        assertDoesNotThrow(()->feed.afterLogSaved(30,row));
    }
    @Test void cacheReadFailureReturnsEmptyList() {
        var zset=mock(ZSetOperations.class);when(redis.opsForZSet()).thenReturn(zset);
        when(zset.reverseRange(anyString(),eq(0L),eq(19L))).thenThrow(new IllegalStateException("redis unavailable"));
        assertTrue(feed.latest(30L).isEmpty());
    }
    @Test void malformedCacheEntryIsIgnored() {
        var zset=mock(ZSetOperations.class);when(redis.opsForZSet()).thenReturn(zset);
        String valid="00000000000000000000000000000000|{\"memberMobile\":\"138****5678\",\"prizeName\":\"积分奖品\"}";
        when(zset.reverseRange(anyString(),eq(0L),eq(19L))).thenReturn(new LinkedHashSet<>(List.of("broken",valid)));
        var result=feed.latest(30L);
        assertEquals(1,result.size());assertEquals("积分奖品",result.get(0).getPrizeName());
    }
}
