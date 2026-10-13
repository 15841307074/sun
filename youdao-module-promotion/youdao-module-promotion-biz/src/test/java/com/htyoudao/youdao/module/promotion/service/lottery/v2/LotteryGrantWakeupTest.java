package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.LongSupplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

class LotteryGrantWakeupTest {
    @Test void immediateGrantRetainsTheCallingDataSource() {
        var observed=new AtomicReference<String>();
        when(ledger.claim(eq(19L),anyString())).thenAnswer(call->{observed.set(com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.peek());return false;});
        com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.push("uat-business");
        try {ready(19);}finally{com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.poll();}
        verify(ledger,timeout(1000)).claim(eq(19L),anyString());assertEquals("uat-business",observed.get());
    }
    @Test void delayedGrantRetainsTheCallingDataSource() {
        var observed=new AtomicReference<String>();
        when(ledger.claim(eq(20L),anyString())).thenAnswer(call->{observed.set(com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.peek());return false;});
        com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.push("uat-business");
        try {context.publishEvent(new LotteryLedger.JobReady(20,10000));}
        finally{com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder.poll();}
        now.addAndGet(10000);worker.poll();verify(ledger,timeout(1000)).claim(eq(20L),anyString());assertEquals("uat-business",observed.get());
    }
    private AnnotationConfigApplicationContext context;
    private LotteryLedger ledger;
    private LotteryGrantWorker worker,peer;
    private LotteryGrantStorage storage;
    private DataSource source;
    private AtomicLong now;
    private CountDownLatch release;

    @BeforeEach void setup() {
        ledger=mock(LotteryLedger.class); when(ledger.due(anyInt())).thenReturn(List.of());
        storage=mock(LotteryGrantStorage.class);
        source=new DriverManagerDataSource("jdbc:h2:mem:wakeup"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1", "sa", "");
        now=new AtomicLong(1000);
        context=new AnnotationConfigApplicationContext();
        context.registerBean("transactionalEventListenerFactory",TransactionalEventListenerFactory.class);
        context.getBeanFactory().registerSingleton("ledger",ledger);
        context.getBeanFactory().registerSingleton("storage",storage);
        context.getBeanFactory().registerSingleton("cash",mock(LotteryCashGateway.class));
        context.getBeanFactory().registerSingleton("stockCache",mock(LotteryStockCache.class));
        context.getBeanFactory().registerSingleton("service",mock(LotteryV2Service.class));
        context.getBeanFactory().registerSingleton("properties",new LotteryProperties());
        context.getBeanFactory().registerSingleton("dataSource",source);
        context.registerBean("worker",LotteryGrantWorker.class,LotteryGrantWorker::new);
        context.refresh(); worker=context.getBean(LotteryGrantWorker.class);
        ReflectionTestUtils.setField(worker,"clock",(LongSupplier)now::get);
        ReflectionTestUtils.setField(worker,"auditJitter",(LongSupplier)()->0);
    }
    @AfterEach void close() throws InterruptedException {
        if(release!=null)release.countDown();
        if(peer!=null)peer.stop();
        context.close();
        executor(worker).awaitTermination(3,TimeUnit.SECONDS);
        if(peer!=null)executor(peer).awaitTermination(3,TimeUnit.SECONDS);
    }
    private ThreadPoolExecutor executor(LotteryGrantWorker w){return (ThreadPoolExecutor)ReflectionTestUtils.getField(w,"executor");}
    private void ready(long id){context.publishEvent(new LotteryLedger.JobReady(id));}

    @Test void repeatedIdleTicksOnlyAuditOnceUntilFiveMinutes() {
        for(int i=0;i<100;i++)worker.poll();
        verify(ledger,times(1)).due(4);
        now.addAndGet(299999); worker.poll(); verify(ledger,times(1)).due(4);
        now.incrementAndGet(); worker.poll(); verify(ledger,times(2)).due(4);
    }
    @Test void readyNotificationDispatchesWithoutScanningTheTable() {
        ready(10); verify(ledger,timeout(1000)).claim(eq(10L),anyString());
        verify(ledger,never()).due(anyInt());
    }
    @Test void notificationDoesNotStartUntilTransactionCommits() {
        var tx=new TransactionTemplate(new DataSourceTransactionManager(source));
        tx.executeWithoutResult(status->{ready(11);verify(ledger,never()).claim(anyLong(),anyString());});
        verify(ledger,timeout(1000)).claim(eq(11L),anyString());
    }
    @Test void rolledBackNotificationNeverStarts() {
        new TransactionTemplate(new DataSourceTransactionManager(source)).executeWithoutResult(status->{ready(12);status.setRollbackOnly();});
        verify(ledger,never()).claim(anyLong(),anyString());
        assertEquals(0,executor(worker).getTaskCount());
    }
    @Test void duplicateNotificationsDoNotEnqueueTheSameRunningJob() throws Exception {
        release=new CountDownLatch(1); var entered=new CountDownLatch(1);
        when(ledger.claim(eq(13L),anyString())).thenAnswer(a->{entered.countDown();release.await(3,TimeUnit.SECONDS);return false;});
        ready(13); assertTrue(entered.await(1,TimeUnit.SECONDS));
        for(int i=0;i<20;i++)ready(13);
        verify(ledger,times(1)).claim(eq(13L),anyString());assertEquals(0,executor(worker).getQueue().size());
    }
    @Test void retryWaitsTenSecondsInMemoryWithoutRepeatedSql() {
        worker.poll(); context.publishEvent(new LotteryLedger.JobReady(14,10000));
        now.addAndGet(9999); for(int i=0;i<20;i++)worker.poll();
        verify(ledger,never()).claim(anyLong(),anyString()); verify(ledger,times(1)).due(4);
        now.incrementAndGet();worker.poll();verify(ledger,timeout(1000)).claim(eq(14L),anyString());
        verify(ledger,times(1)).due(4);
    }
    @Test void fullQueueLeavesOverflowForDurableRecoveryWithoutCallerRuns() throws Exception {
        release=new CountDownLatch(1); var entered=new CountDownLatch(4);
        when(ledger.claim(anyLong(),anyString())).thenAnswer(a->{if((long)a.getArgument(0)<4){entered.countDown();release.await(3,TimeUnit.SECONDS);}return false;});
        for(int i=0;i<4;i++)ready(i); assertTrue(entered.await(1,TimeUnit.SECONDS));
        for(int i=4;i<68;i++)ready(i);ready(68);
        assertEquals(64,executor(worker).getQueue().size());verify(ledger,never()).claim(eq(68L),anyString());
        worker.poll();verify(ledger,never()).due(anyInt());
        release.countDown();await().atMost(java.time.Duration.ofSeconds(3)).until(()->executor(worker).getActiveCount()==0&&executor(worker).getQueue().isEmpty());
        when(ledger.due(4)).thenReturn(List.of(68L));worker.poll();
        verify(ledger,timeout(1000)).claim(eq(68L),anyString());
    }
    @Test void twoInstancesStillRequireWinningTheSameDatabaseLease() {
        peer=new LotteryGrantWorker();
        for(String field:List.of("ledger","storage","cash","stockCache","service","properties","dataSource"))ReflectionTestUtils.setField(peer,field,ReflectionTestUtils.getField(worker,field));
        AtomicBoolean claimed=new AtomicBoolean();when(ledger.claim(eq(15L),anyString())).thenAnswer(a->claimed.compareAndSet(false,true));
        var draw=new LotteryLedger.Draw();draw.setId(15);draw.setBusinessId(10);draw.setDrawStatus("COMPLETED");draw.setStockState("ISSUED");
        when(ledger.get(15)).thenReturn(draw);
        ready(15);peer.onJobReady(new LotteryLedger.JobReady(15));
        verify(ledger,timeout(1000).times(2)).claim(eq(15L),anyString());
        verify(ledger,timeout(1000).times(1)).finishJob(eq(15L),anyString()); verify(storage,times(1)).log(draw);
    }
    @Test void idleAuditJitterAvoidsAllInstancesScanningAtTheSameDeadline() {
        ReflectionTestUtils.setField(worker,"auditJitter",(LongSupplier)()->27000);worker.poll();
        now.addAndGet(300000);worker.poll();verify(ledger,times(1)).due(4);
        now.addAndGet(27000);worker.poll();verify(ledger,times(2)).due(4);
    }
    @Test void busyPoolDefersBothClaimAndAuditWithoutBorrowingConnections() {
        HikariDataSource pool=mock(HikariDataSource.class);HikariPoolMXBean metrics=mock(HikariPoolMXBean.class);
        when(pool.getMaximumPoolSize()).thenReturn(16);when(pool.getHikariPoolMXBean()).thenReturn(metrics);when(metrics.getActiveConnections()).thenReturn(13);
        ReflectionTestUtils.setField(worker,"dataSource",pool);
        ready(16);await().atMost(java.time.Duration.ofSeconds(1)).until(()->executor(worker).getCompletedTaskCount()==1);
        for(int i=0;i<20;i++)worker.poll();verifyNoInteractions(ledger);
        when(metrics.getActiveConnections()).thenReturn(2);when(ledger.due(4)).thenReturn(List.of(16L));now.addAndGet(1000);worker.poll();
        verify(ledger,timeout(1000)).claim(eq(16L),anyString());
    }
    @Test void smallPoolCapsWorkersAtOneQuarterOfMaximumConnections() {
        HikariDataSource pool=mock(HikariDataSource.class);when(pool.getMaximumPoolSize()).thenReturn(8);
        ReflectionTestUtils.setField(worker,"dataSource",pool);worker.poll();
        assertEquals(2,executor(worker).getMaximumPoolSize());verify(ledger).due(2);
    }
    @Test void databaseFailureDoesNotCauseOneQueryEverySecond() {
        when(ledger.due(anyInt())).thenThrow(new IllegalStateException("database unavailable"));
        for(int i=0;i<100;i++)worker.poll();verify(ledger,times(1)).due(4);
        now.addAndGet(300000);worker.poll();verify(ledger,times(2)).due(4);
    }
    @Test void startupRecoversExistingDatabaseTasksWithoutWaitingFiveMinutes() {
        when(ledger.due(4)).thenReturn(List.of(17L));worker.poll();verify(ledger,timeout(1000)).claim(eq(17L),anyString());
    }
    @Test void lostLeaseDoesNotEmitAnExtraRetryNotification() {
        JdbcTemplate jdbc=mock(JdbcTemplate.class);ApplicationEventPublisher events=mock(ApplicationEventPublisher.class);
        var actual=new LotteryLedgerImpl();ReflectionTestUtils.setField(actual,"jdbc",jdbc);ReflectionTestUtils.setField(actual,"events",events);
        actual.retryJob(18,"old-lease","failure");verifyNoInteractions(events);
        when(jdbc.update(startsWith("UPDATE lottery_v2_job"),any(),any(),any(),any())).thenReturn(1);
        when(jdbc.queryForObject(startsWith("SELECT state FROM lottery_v2_job"),eq(String.class),eq(18L))).thenReturn("READY");
        actual.retryJob(18,"current-lease","failure");verify(events).publishEvent(new LotteryLedger.JobReady(18,10000));
    }
}
