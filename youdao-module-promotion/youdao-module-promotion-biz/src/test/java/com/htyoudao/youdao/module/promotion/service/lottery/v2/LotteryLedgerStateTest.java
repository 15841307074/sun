package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryLedgerStateTest {
    private JdbcTemplate jdbc;
    private LotteryLedger ledger;
    private LotteryDrawSnapshot snapshot;

    @BeforeEach
    void setup() {
        jdbc = mock(JdbcTemplate.class);
        ledger = new LotteryLedgerImpl();
        ReflectionTestUtils.setField(ledger, "jdbc", jdbc);
        ReflectionTestUtils.setField(ledger, "json", new ObjectMapper());
        ReflectionTestUtils.setField(ledger, "quotaCache", mock(LotteryCounterCache.class));
        var settings = new LotterySettingsCacheDataVO();
        settings.setId(20L); settings.setActivityId(21L);
        settings.setConfigVersion(1L); settings.setStockEpoch(1L);
        var request = new LotteryVO();
        request.setLotteryId(20L); request.setMemberId(40L);
        request.setStoreId(30L); request.setRequestId("request_123");
        var prize = new LotteryPrizeDO();
        prize.setId(100L); prize.setCode("guarantee"); prize.setIsGuarantees(1);
        snapshot = new LotteryDrawSnapshot();
        snapshot.setSettings(settings); snapshot.setRequest(request); snapshot.setPrize(prize);
        snapshot.setScopeKey("TOTAL");
        snapshot.setChances(List.of(new LotteryDrawSnapshot.Chance(1, "TOTAL", 1, 0, false)));
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
    }

    static Stream<Object> enabledStates() { return Stream.of(true, 1, 1L, BigInteger.ONE); }
    static Stream<Object> disabledStates() { return Stream.of(false, 0, 2, null, "1"); }

    private void databaseState(Object state) {
        Map<String, Object> row = new HashMap<>(Map.of("config_version", 1L, "stock_epoch", 1L, "runtime_version", 2));
        row.put("state", state);
        when(jdbc.queryForList(startsWith("SELECT config_version,stock_epoch,runtime_version,state"), eq(20L), eq(10L)))
                .thenReturn(List.of(row));
    }

    @ParameterizedTest
    @MethodSource("enabledStates")
    void acceptsEnabledBooleanOrNumericState(Object state) {
        databaseState(state);
        var draw = ledger.accept(10L, snapshot);
        assertTrue(draw.isNewlyAccepted());
        assertEquals("ACCEPTED", draw.getDrawStatus());
        assertEquals(40L, draw.getMemberId());
    }

    @ParameterizedTest
    @MethodSource("disabledStates")
    void rejectsDisabledOrInvalidStateBeforeConsumingAnything(Object state) {
        databaseState(state);
        assertThrows(ServiceException.class, () -> ledger.accept(10L, snapshot));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    void suppliedJsonBindsLongIdsAndUuidWithoutPrecisionLoss() throws Exception {
        var request = new ObjectMapper().readValue("""
                {"requestId":"5E957271-11F4-4F8F-9E79-41CD1D5360CE",
                 "lotteryId":"2064548866544381954","memberId":1100041,"isFree":0,
                 "cityName":"沈阳市","storeId":"1241338966192422912"}
                """, LotteryVO.class);
        assertEquals(2064548866544381954L, request.getLotteryId());
        assertEquals(1241338966192422912L, request.getStoreId());
        assertEquals(1100041L, request.getMemberId());
        assertTrue(request.getRequestId().matches("[A-Za-z0-9_-]{8,64}"));
        assertEquals(0, request.getIsFree());
    }

    private LotteryLedger transactionalProxy(PlatformTransactionManager manager) {
        var advice = new TransactionInterceptor();
        advice.setTransactionManager(manager);
        advice.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        var factory = new ProxyFactory(ledger);
        factory.setInterfaces(LotteryLedger.class);
        factory.addAdvice(advice);
        return (LotteryLedger) factory.getProxy();
    }

    @Test
    void interfaceProxyRetainsTransactionOnAcceptedDraw() {
        databaseState(true);
        var manager = mock(PlatformTransactionManager.class);
        var status = mock(TransactionStatus.class);
        when(manager.getTransaction(any())).thenReturn(status);
        assertTrue(transactionalProxy(manager).accept(10L, snapshot).isNewlyAccepted());
        verify(manager).commit(status);
        verify(manager, never()).rollback(any());
    }

    @Test
    void interfaceProxyRollsBackRejectedDraw() {
        databaseState(false);
        var manager = mock(PlatformTransactionManager.class);
        var status = mock(TransactionStatus.class);
        when(manager.getTransaction(any())).thenReturn(status);
        assertThrows(ServiceException.class, () -> transactionalProxy(manager).accept(10L, snapshot));
        verify(manager).rollback(status);
        verify(manager, never()).commit(any());
    }
}
