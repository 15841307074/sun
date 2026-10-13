package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryGrantBackpressureTest {

    @Test void slowDatabaseRefreshNeverWaitsOnTheDrawRequest() throws Exception {
        var redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn("0:0:1");
        when(values.setIfAbsent(anyString(), anyString(), any(java.time.Duration.class))).thenReturn(true);
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        DataSource source = mock(DataSource.class);
        var route=new java.util.concurrent.atomic.AtomicReference<String>();
        when(source.getConnection()).thenAnswer(call -> {route.set(com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.peek());entered.countDown(); release.await(2, TimeUnit.SECONDS); throw new java.sql.SQLException("database unavailable");});
        var pressure = new LotteryGrantBackpressure();
        ReflectionTestUtils.setField(pressure, "redis", redis);
        ReflectionTestUtils.setField(pressure, "jdbc", new JdbcTemplate(source));
        try {
            long began = System.nanoTime();
            com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.push("uat-business");
            try {assertDoesNotThrow(pressure::checkDrawAllowed);}
            finally{com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.poll();}
            assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - began) < 1000, "draw waited on the database refresh");
            assertTrue(entered.await(1, TimeUnit.SECONDS));
            assertEquals("uat-business",route.get());
        } finally {
            release.countDown();
            pressure.stop();
        }
    }

    @Test void missingSnapshotAllowsOnlyOneBootstrapDrawAcrossInstances() {
        var redis=mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String,String> values=mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        java.util.concurrent.atomic.AtomicBoolean granted=new java.util.concurrent.atomic.AtomicBoolean();
        when(values.setIfAbsent(contains("bootstrap"),anyString(),any(java.time.Duration.class))).thenAnswer(call->granted.compareAndSet(false,true));
        var first=new LotteryGrantBackpressure();var peer=new LotteryGrantBackpressure();
        ReflectionTestUtils.setField(first,"redis",redis);ReflectionTestUtils.setField(peer,"redis",redis);
        try {assertDoesNotThrow(first::checkDrawAllowed);assertThrows(ServiceException.class,peer::checkDrawAllowed);}
        finally {first.stop();peer.stop();}
    }

    @Test void pausedSnapshotStillRejectsEvenWhenItIsOld() {
        var redis=mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked") ValueOperations<String,String> values=mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);when(values.get(anyString())).thenReturn("100:1:1");
        var pressure=new LotteryGrantBackpressure();ReflectionTestUtils.setField(pressure,"redis",redis);
        try {assertThrows(ServiceException.class,pressure::checkDrawAllowed);}
        finally {pressure.stop();}
    }

    @Test void stoppedAwardsDoNotConsumeTheActiveBacklogLimit() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:grantPressure" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE lottery_v2_request(id BIGINT PRIMARY KEY,draw_status VARCHAR(24),grant_status VARCHAR(24))");
        jdbc.execute("CREATE TABLE lottery_v2_job(request_pk BIGINT PRIMARY KEY,state VARCHAR(16))");
        for (int id = 1; id <= 130; id++) add(jdbc, id, "STOPPED", "ACCEPTED", "PENDING");
        for (int id = 131; id <= 135; id++) add(jdbc, id, "READY", "ACCEPTED", "PENDING");
        add(jdbc, 136, "RUNNING", "ACCEPTED", "PROCESSING");
        add(jdbc, 137, "READY", "ACCEPTED", "WAIT_USER_CONFIRM");
        add(jdbc, 138, "READY", "COMPLETED", "SUCCESS");
        var pressure = new LotteryGrantBackpressure();
        ReflectionTestUtils.setField(pressure, "jdbc", jdbc);
        assertEquals(6, (int) ReflectionTestUtils.invokeMethod(pressure, "pendingCount"));
        for (int id = 139; id <= 232; id++) add(jdbc, id, "READY", "ACCEPTED", "PENDING");
        assertEquals(100, (int) ReflectionTestUtils.invokeMethod(pressure, "pendingCount"));
    }

    @Test void stoppedRetryReleasesLeaseAndDoesNotScheduleAnotherAttempt() {
        var jdbc = mock(JdbcTemplate.class);
        var events = mock(ApplicationEventPublisher.class);
        var ledger = new LotteryLedgerImpl();
        ReflectionTestUtils.setField(ledger, "jdbc", jdbc);
        ReflectionTestUtils.setField(ledger, "events", events);
        when(jdbc.update(startsWith("UPDATE lottery_v2_job"), any(), any(), any(), any())).thenReturn(1);
        when(jdbc.queryForObject(startsWith("SELECT state FROM lottery_v2_job"), eq(String.class), eq(1L))).thenReturn("STOPPED");
        ledger.retryJob(1L, "lease", "timeout");
        verify(jdbc).update(argThat(sql -> sql.contains("'STOPPED'") && sql.contains("lease_token=NULL") && sql.contains("lease_until=NULL")), eq(8), eq("timeout"), eq(1L), eq("lease"));
        verifyNoInteractions(events);
    }

    private static void add(JdbcTemplate jdbc, long id, String state, String draw, String grant) {
        jdbc.update("INSERT INTO lottery_v2_request(id,draw_status,grant_status) VALUES(?,?,?)", id, draw, grant);
        jdbc.update("INSERT INTO lottery_v2_job(request_pk,state) VALUES(?,?)", id, state);
    }
}
