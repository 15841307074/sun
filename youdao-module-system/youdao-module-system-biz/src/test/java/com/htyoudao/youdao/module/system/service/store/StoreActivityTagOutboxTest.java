package com.htyoudao.youdao.module.system.service.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityStoreTagUpdateDTO;
import com.htyoudao.youdao.module.promotion.api.enums.coupon.CouponStoreTagSqlTypeEnum;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.LongSupplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StoreActivityTagOutboxTest {
    private StoreActivityTagOutbox outbox;
    private JdbcTemplate jdbc;
    private ActivityApi activities;
    private StoreActivityTagCache cache;
    private AtomicLong now;
    private CountDownLatch release;

    @BeforeEach void setup() {
        outbox=new StoreActivityTagOutbox();jdbc=mock(JdbcTemplate.class);
        activities=mock(ActivityApi.class);cache=mock(StoreActivityTagCache.class);now=new AtomicLong(1000);
        ReflectionTestUtils.setField(outbox,"jdbc",jdbc);
        ReflectionTestUtils.setField(outbox,"json",new ObjectMapper());
        ReflectionTestUtils.setField(outbox,"cache",cache);
        ReflectionTestUtils.setField(outbox,"activities",activities);
        ReflectionTestUtils.setField(outbox,"clock",(LongSupplier)now::get);
        when(jdbc.queryForList(startsWith("SELECT id FROM"),eq(32))).thenReturn(List.of());
        when(jdbc.update(anyString(),any(Object[].class))).thenReturn(1);
    }

    @AfterEach void close() throws InterruptedException {
        if(release!=null)release.countDown();outbox.stop();
        ((ThreadPoolExecutor)ReflectionTestUtils.getField(outbox,"immediate")).awaitTermination(3,TimeUnit.SECONDS);
        BusinessContextHolder.clear();
    }

    @Test void emptyDatabaseStopsAllIdleSqlUntilTagChangesWakeIt() {
        outbox.dispatch();
        for(int i=0;i<1000;i++){now.addAndGet(1000);outbox.dispatch();}
        verify(jdbc,times(1)).queryForList(startsWith("SELECT id FROM"),eq(32));
        verify(jdbc,times(1)).queryForObject(startsWith("SELECT MIN(next_at)"),eq(Timestamp.class));
        BusinessContextHolder.setBusinessId(10L);
        var change=new ActivityStoreTagUpdateDTO();change.setStoreId(20L);change.setTagIds(List.of(30L));
        outbox.enqueue(List.of(change),CouponStoreTagSqlTypeEnum.UPDATE);
        now.addAndGet(30000);outbox.dispatch();
        verify(jdbc,times(2)).queryForList(startsWith("SELECT id FROM"),eq(32));
    }

    @Test void schemaErrorsBackOffInsteadOfHittingTheDatabaseEveryTwoSeconds() {
        when(jdbc.queryForList(startsWith("SELECT id FROM"),eq(32))).thenThrow(new BadSqlGrammarException("scan","SELECT id FROM system_activity_tag_outbox",new SQLException("Table does not exist","42S02",1146)));
        outbox.dispatch();
        for(int i=0;i<100;i++){now.addAndGet(2000);outbox.dispatch();}
        verify(jdbc,times(1)).queryForList(startsWith("SELECT id FROM"),eq(32));
        now.set(301000);outbox.dispatch();
        verify(jdbc,times(2)).queryForList(startsWith("SELECT id FROM"),eq(32));
        verifyNoInteractions(cache,activities);
    }

    @Test void futureRetryIsRecoveredInsteadOfMistakenForAnEmptyOutbox() {
        when(jdbc.queryForObject(startsWith("SELECT MIN(next_at)"),eq(Timestamp.class))).thenReturn(new Timestamp(System.currentTimeMillis()+30000));
        outbox.dispatch();now.addAndGet(1000);outbox.dispatch();
        verify(jdbc,times(1)).queryForList(startsWith("SELECT id FROM"),eq(32));
        now.addAndGet(30000);outbox.dispatch();
        verify(jdbc,times(2)).queryForList(startsWith("SELECT id FROM"),eq(32));
    }

    @Test void slowMarketingCallRunsOnTheWorkerInsteadOfBlockingTheScheduler() throws Exception {
        when(jdbc.queryForList(startsWith("SELECT id FROM"),eq(32))).thenReturn(List.of(Map.of("id",42L)));
        when(jdbc.queryForList(startsWith("SELECT business_id,store_id"),eq(42L))).thenReturn(List.of(Map.of("business_id",10L,"store_id",20L,"operation","UPDATE","payload","{\"storeId\":20,\"tagIds\":[30]}")));
        CountDownLatch entered=new CountDownLatch(1);release=new CountDownLatch(1);AtomicReference<String> thread=new AtomicReference<>();
        doAnswer(call->{thread.set(Thread.currentThread().getName());assertEquals(10L,BusinessContextHolder.getBusinessId());entered.countDown();release.await(2,TimeUnit.SECONDS);return null;}).when(activities).updateActivityStoreByTagIdAndStoreId(anyList(),eq(CouponStoreTagSqlTypeEnum.UPDATE));
        long began=System.nanoTime();outbox.dispatch();
        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-began)<1000);
        assertTrue(entered.await(1,TimeUnit.SECONDS));assertEquals("store-activity-tag-sync",thread.get());
        verify(cache).refresh(10L,20L);
    }

    @Test void rolledBackTagChangesNeverNotifyMarketing() {
        var source=new DriverManagerDataSource("jdbc:h2:mem:storeTag"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1","sa","");
        var tx=new TransactionTemplate(new DataSourceTransactionManager(source));
        BusinessContextHolder.setBusinessId(10L);
        var change=new ActivityStoreTagUpdateDTO();change.setStoreId(20L);change.setTagIds(List.of(30L));
        tx.executeWithoutResult(status->{outbox.enqueue(List.of(change),CouponStoreTagSqlTypeEnum.UPDATE);status.setRollbackOnly();});
        verify(jdbc,never()).queryForList(startsWith("SELECT business_id,store_id"),anyLong());
        verifyNoInteractions(cache,activities);
    }
}
