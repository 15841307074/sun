package com.htyoudao.youdao.module.member.service.pointsLog;

import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.FrozenPointsReleaseMapper;
import com.htyoudao.youdao.module.member.service.job.JobServiceImpl;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import java.lang.reflect.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

class FrozenPointsReleaseTest {
    private JdbcTemplate jdbc;
    private JobServiceImpl job;
    private FrozenPointsReleaseService release;
    private FrozenPointsReleaseMapper mapper;
    private RLock lock;
    private CountingSource source;
    private Configuration configuration;
    static class CountingSource extends DelegatingDataSource {
        final AtomicInteger active=new AtomicInteger(), peak=new AtomicInteger();
        CountingSource(DriverManagerDataSource target){super(target);}
        @Override public Connection getConnection() throws SQLException {
            var real=super.getConnection(); peak.accumulateAndGet(active.incrementAndGet(),Math::max);
            var closed=new AtomicBoolean();
            return (Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,args)->{
                if(method.getName().equals("equals"))return proxy==args[0];
                if(method.getName().equals("hashCode"))return System.identityHashCode(proxy);
                if(method.getName().equals("close")&&closed.compareAndSet(false,true))active.decrementAndGet();
                try{return method.invoke(real,args);}catch(InvocationTargetException error){throw error.getCause();}
            });
        }
    }
    @BeforeEach void setup() throws Exception {
        source=new CountingSource(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000","sa",""));
        jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE wx_member(member_id BIGINT,business_id BIGINT,sharding_value INT,member_integral INT,freeze_points BIGINT,integral_frozen BIGINT,member_level INT DEFAULT 0,deleted INT DEFAULT 0,update_time TIMESTAMP,PRIMARY KEY(member_id,business_id))");
        jdbc.execute("CREATE TABLE points_log(points_log_id BIGINT PRIMARY KEY,member_id BIGINT,business_id BIGINT,sharding_value INT,points_change BIGINT,points_type INT,create_time TIMESTAMP,deleted INT DEFAULT 0,update_time TIMESTAMP)");
        jdbc.execute("CREATE TABLE wx_member_card(business_id BIGINT,member_level INT,card_status INT,min_points_threshold BIGINT,max_points_threshold BIGINT,deleted INT DEFAULT 0)");
        configuration=new Configuration();configuration.setMapUnderscoreToCamelCase(true);configuration.addMapper(FrozenPointsReleaseMapper.class);
        var factory=new SqlSessionFactoryBean();factory.setDataSource(source);factory.setConfiguration(configuration);
        var actual=new SqlSessionTemplate(factory.getObject()).getMapper(FrozenPointsReleaseMapper.class);
        mapper=mock(FrozenPointsReleaseMapper.class,delegatesTo(actual));
        var target=new FrozenPointsReleaseServiceImpl();ReflectionTestUtils.setField(target,"mapper",mapper);
        var transaction=new TransactionTemplate(new DataSourceTransactionManager(source));
        release=mock(FrozenPointsReleaseService.class,delegatesTo(target));
        doAnswer(call->transaction.execute(status->target.release(call.getArgument(0),call.getArgument(1),call.getArgument(2),call.getArgument(3),call.getArgument(4),call.getArgument(5))))
                .when(release).release(anyLong(),anyLong(),anyList(),any(),nullable(LocalDateTime.class),nullable(Integer.class));
        var redisson=mock(RedissonClient.class);lock=mock(RLock.class);
        when(redisson.getLock(anyString())).thenReturn(lock);when(lock.tryLock()).thenReturn(true);when(lock.isHeldByCurrentThread()).thenReturn(true);
        job=new JobServiceImpl();ReflectionTestUtils.setField(job,"frozenPointsReleaseService",release);ReflectionTestUtils.setField(job,"redissonClient",redisson);
    }
    private LocalDateTime now(){return LocalDateTime.now(ZoneId.of("Asia/Shanghai"));}
    private void member(long id,long frozen){jdbc.update("INSERT INTO wx_member(member_id,business_id,sharding_value,member_integral,freeze_points,integral_frozen) VALUES(?,10,?,100,?,0)",id,id%10,frozen);}
    private void log(long id,long member,Long points,int hours){jdbc.update("INSERT INTO points_log(points_log_id,member_id,business_id,sharding_value,points_change,points_type,create_time) VALUES(?,?,10,?,?,4,?)",id,member,member%10,points,now().minusHours(hours));}
    private int available(long member){return jdbc.queryForObject("SELECT member_integral FROM wx_member WHERE member_id=?",Integer.class,member);}
    private int type(long id){return jdbc.queryForObject("SELECT points_type FROM points_log WHERE points_log_id=?",Integer.class,id);}
    private int single(long id,long member){return release.release(member,10,List.of(id),now().minusHours(48),null,null);}

    @Test void missedRunIsCaughtUpAfter72HoursAndNeverCreditedTwice(){
        member(41,60);log(1,41,60L,240);job.updateMemberPointTask(0,0,0,10);
        assertEquals(160,available(41));assertEquals(1,type(1));job.updateMemberPointTask(0,0,0,10);assertEquals(160,available(41));
    }
    @Test void immaturePointsStayFrozenAndManualEndRangeIsPreserved(){
        member(41,3);log(1,41,1L,20);log(2,41,1L,50);log(3,41,1L,240);
        job.updateMemberPointTask(2,3,41,10);assertEquals(4,type(1));assertEquals(1,type(2));assertEquals(4,type(3));
        job.updateMemberPointTask(0,0,41,10);assertEquals(102,available(41));assertEquals(4,type(1));assertEquals(1,type(3));
    }
    @Test void shrinkingDatasetDoesNotSkipRecordsAndConnectionsStayBounded(){
        member(41,401);var timestamp=now().minusHours(50);
        for(int id=1;id<=401;id++)jdbc.update("INSERT INTO points_log(points_log_id,member_id,business_id,sharding_value,points_change,points_type,create_time) VALUES(?,41,10,1,1,4,?)",id,timestamp);
        source.peak.set(0);job.updateMemberPointTask(0,0,0,10);
        assertEquals(401,jdbc.queryForObject("SELECT COUNT(*) FROM points_log WHERE points_type=1",Integer.class));
        assertEquals(501,available(41));assertEquals(0,source.active.get());assertEquals(1,source.peak.get());
    }
    @Test void failedLogWriteRollsBackAccount(){
        member(41,60);log(1,41,60L,240);doReturn(0).when(mapper).updateDetails(anyLong(),anyLong(),anyInt(),anyList());
        assertDoesNotThrow(()->job.updateMemberPointTask(0,0,0,10));
        assertEquals(100,available(41));assertEquals(4,type(1));assertEquals(0,source.active.get());
    }
    @Test void oneBadRecordDoesNotBlockOtherRecordsAndCanRetryAfterCorrection(){
        member(41,1);log(1,41,null,240);log(2,41,1L,240);member(42,1);log(3,42,1L,240);
        assertDoesNotThrow(()->job.updateMemberPointTask(0,0,0,10));
        assertEquals(4,type(1));assertEquals(1,type(2));assertEquals(1,type(3));assertEquals(101,available(41));
        jdbc.update("UPDATE points_log SET points_change=0 WHERE points_log_id=1");job.updateMemberPointTask(0,0,0,10);assertEquals(1,type(1));assertEquals(101,available(41));
    }
    @Test void missingMemberAndBadBalanceDoNotBlockHealthyMembers(){
        log(1,41,1L,240);member(42,0);log(2,42,1L,240);member(43,1);log(3,43,1L,240);
        assertDoesNotThrow(()->job.updateMemberPointTask(0,0,0,10));
        assertEquals(4,type(1));assertEquals(4,type(2));assertEquals(1,type(3));
        member(41,1);jdbc.update("UPDATE wx_member SET freeze_points=1 WHERE member_id=42");job.updateMemberPointTask(0,0,0,10);assertEquals(1,type(1));assertEquals(1,type(2));
    }
    @Test void lockFailureIsNotRepeatedAndOtherMembersProceed(){
        member(41,2);log(1,41,1L,240);log(2,41,1L,240);member(51,1);log(3,51,1L,240);
        doThrow(new CannotAcquireLockException("locked")).when(mapper).lockMember(41,10,1);
        assertDoesNotThrow(()->job.updateMemberPointTask(0,0,0,10));verify(mapper,times(1)).lockMember(41,10,1);assertEquals(1,type(3));assertEquals(0,source.active.get());
    }
    @Test void concurrentReleasesOnlyCreditOnce() throws Exception {
        member(41,60);log(1,41,60L,240);var executor=Executors.newFixedThreadPool(2);
        try{var one=executor.submit(()->single(1,41));var two=executor.submit(()->single(1,41));assertEquals(1,one.get(15,TimeUnit.SECONDS)+two.get(15,TimeUnit.SECONDS));assertEquals(160,available(41));}
        finally{executor.shutdownNow();}assertEquals(0,source.active.get());
    }
    @Test void failedAccountWriteDoesNotMarkDetailsReleased(){
        member(41,1);log(1,41,1L,240);
        doReturn(0).when(mapper).updateAccount(anyLong(),anyLong(),anyInt(),anyInt(),anyLong(),anyLong(),nullable(Integer.class));
        assertDoesNotThrow(()->job.updateMemberPointTask(0,0,0,10));
        assertEquals(100,available(41));assertEquals(4,type(1));assertEquals(0,source.active.get());
    }
    @Test void otherProjectWrongShardAndDeletedRecordsAreNotCredited(){
        member(41,60);log(1,41,60L,240);jdbc.update("UPDATE points_log SET business_id=11");assertEquals(0,single(1,41));
        jdbc.update("UPDATE points_log SET business_id=10,sharding_value=2");assertEquals(0,single(1,41));
        jdbc.update("UPDATE points_log SET sharding_value=1,deleted=1");assertEquals(0,single(1,41));assertEquals(100,available(41));
    }
    @Test void statementsHaveTimeoutAndDuplicateJobDoesNotBorrowConnections(){
        for(var name:List.of("pending","pendingAmount","memberLevel","lockMember","lockDetails","updateAccount","updateDetails"))assertEquals(5,configuration.getMappedStatement(FrozenPointsReleaseMapper.class.getName()+"."+name).getTimeout());
        when(lock.tryLock()).thenReturn(false);job.updateMemberPointTask(0,0,0,10);verifyNoInteractions(release);
    }
    @Test void balanceMismatchProducesInfoWithoutStackAndDoesNotFailWholeJob(){
        member(41,0);log(1,41,60L,240);member(51,1);log(2,51,1L,240);
        var logger=(Logger)LoggerFactory.getLogger(JobServiceImpl.class);
        var previousLevel=logger.getLevel();logger.setLevel(Level.INFO);
        var appender=new ListAppender<ILoggingEvent>();appender.start();logger.addAppender(appender);
        try {
            assertDoesNotThrow(()->job.updateMemberPointTask(0,0,0,10));
            var skipped=appender.list.stream().filter(event->event.getFormattedMessage().contains("会员解冻暂不可执行")).toList();
            assertEquals(1,skipped.size());assertEquals(Level.INFO,skipped.get(0).getLevel());assertNull(skipped.get(0).getThrowableProxy());
            assertTrue(skipped.get(0).getFormattedMessage().contains("freezePoints=0"));assertTrue(skipped.get(0).getFormattedMessage().contains("releasePoints=60"));
            assertTrue(appender.list.stream().noneMatch(event->event.getLevel()==Level.ERROR));
            assertEquals(100,available(41));assertEquals(4,type(1));assertEquals(101,available(51));assertEquals(1,type(2));assertEquals(0,source.active.get());
        } finally {logger.detachAppender(appender);appender.stop();logger.setLevel(previousLevel);}
    }
    @Test void databaseOutageStillStopsScanningAndReleasesTaskLock(){
        doThrow(new DataAccessResourceFailureException("database unavailable")).when(mapper).pending(anyLong(),anyInt(),anyLong(),any(),nullable(LocalDateTime.class),nullable(LocalDateTime.class),nullable(Long.class),anyInt());
        assertThrows(DataAccessResourceFailureException.class,()->job.updateMemberPointTask(0,0,0,10));
        verify(mapper,times(1)).pending(anyLong(),anyInt(),anyLong(),any(),nullable(LocalDateTime.class),nullable(LocalDateTime.class),nullable(Long.class),anyInt());
        verify(lock).unlock();assertEquals(0,source.active.get());
    }
}
