package com.htyoudao.youdao.module.member.service.wxmember;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsLogMapper;
import org.junit.jupiter.api.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LotteryPointsServiceTest {
    private JdbcTemplate jdbc;
    private LotteryPointsService service;
    private PointsLogMapper logs;
    private Long previousBusiness;

    @BeforeEach void setup() {
        previousBusiness=BusinessContextHolder.getBusinessId();BusinessContextHolder.setBusinessId(10L);
        var source=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000","sa","");
        jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE wx_member(business_id BIGINT,member_id BIGINT,sharding_value INT,deleted INT DEFAULT 0,member_integral BIGINT,member_mobile VARCHAR(32),member_nick_name VARCHAR(64),update_time TIMESTAMP,PRIMARY KEY(business_id,member_id))");
        jdbc.execute("CREATE TABLE points_log(points_log_id BIGINT PRIMARY KEY,business_id BIGINT,member_id BIGINT,sharding_value INT,log_code VARCHAR(64),points_change BIGINT,points_type INT,points_log_status INT,is_points_product INT NOT NULL,deleted BOOLEAN,create_time TIMESTAMP,update_time TIMESTAMP)");
        jdbc.execute("CREATE INDEX idx_lottery_points_code ON points_log(business_id,member_id,log_code)");
        jdbc.update("INSERT INTO wx_member VALUES(10,40,0,0,100,'13800000000','会员',CURRENT_TIMESTAMP)");
        logs=mock(PointsLogMapper.class);
        when(logs.insert(any(PointsLogDO.class))).thenAnswer(call->{
            PointsLogDO row=call.getArgument(0);
            return jdbc.update("INSERT INTO points_log VALUES(?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",row.getPointsLogId(),row.getBusinessId(),row.getMemberId(),row.getShardingValue(),row.getLogCode(),row.getPointsChange(),row.getPointsType(),row.getPointsLogStatus(),row.getIsPointsProduct(),row.getDeleted());
        });
        var target=new LotteryPointsService();ReflectionTestUtils.setField(target,"jdbc",jdbc);ReflectionTestUtils.setField(target,"pointsLogs",logs);
        var proxy=new ProxyFactory(target);proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(source),new AnnotationTransactionAttributeSource()));
        service=(LotteryPointsService)proxy.getProxy();
    }
    @AfterEach void cleanup(){jdbc.execute("DROP ALL OBJECTS");BusinessContextHolder.setBusinessId(previousBusiness);}
    private int balance(){return jdbc.queryForObject("SELECT member_integral FROM wx_member WHERE business_id=10 AND member_id=40",Integer.class);}
    private int count(String condition){return jdbc.queryForObject("SELECT COUNT(*) FROM points_log WHERE "+condition,Integer.class);}

    @Test void successfulOperationsAndRefundAreIdempotentWithoutAnyNewTable(){
        assertTrue(service.change(40,"L100:DEBIT",-10));assertTrue(service.change(40,"L100:DEBIT",-10));
        assertTrue(service.change(40,"L100:AWARD",50));assertTrue(service.change(40,"L100:AWARD",50));
        service.refund(40,"L100");service.refund(40,"L100");
        assertEquals(150,balance());assertEquals(3,count("deleted=FALSE"));
    }
    @Test void rejectedDebitRemainsRejectedAfterBalanceIsReplenished(){
        assertFalse(service.change(40,"L100:DEBIT",-200));jdbc.update("UPDATE wx_member SET member_integral=300");
        assertFalse(service.change(40,"L100:DEBIT",-200));service.refund(40,"L100");
        assertEquals(300,balance());assertEquals(1,count("points_log_status=0 AND deleted=TRUE"));assertEquals(0,count("deleted=FALSE"));
    }
    @Test void rejectedAwardCanBeReissuedOnceAndUsesOriginalRow(){
        jdbc.update("UPDATE wx_member SET member_integral=2147483647");assertFalse(service.change(40,"L100:AWARD",100));
        long originalId=jdbc.queryForObject("SELECT points_log_id FROM points_log",Long.class);
        jdbc.update("UPDATE wx_member SET member_integral=0");assertTrue(service.change(40,"L100:AWARD",100));assertTrue(service.change(40,"L100:AWARD",100));
        assertEquals(100,balance());assertEquals(1,count("points_log_status=1 AND deleted=FALSE"));
        assertEquals(originalId,jdbc.queryForObject("SELECT points_log_id FROM points_log",Long.class));
    }
    @Test void sameBusinessNumberCannotChangeAmount(){
        assertTrue(service.change(40,"L100:DEBIT",-10));assertThrows(IllegalArgumentException.class,()->service.change(40,"L100:DEBIT",-20));assertEquals(90,balance());
    }
    @Test void failedLogInsertRollsBackBalanceAndRetryWorks(){
        doThrow(new IllegalStateException("storage unavailable")).when(logs).insert(any(PointsLogDO.class));
        assertThrows(IllegalStateException.class,()->service.change(40,"L100:DEBIT",-10));assertEquals(100,balance());assertEquals(0,count("1=1"));
    }
    @Test void zeroInsertedRowsAlsoRollBackBalance(){
        doReturn(0).when(logs).insert(any(PointsLogDO.class));assertThrows(IllegalStateException.class,()->service.change(40,"L100:AWARD",50));assertEquals(100,balance());
    }
    @Test void failedRefundCanBeRetriedAfterOverflowIsFixed(){
        assertTrue(service.change(40,"L100:DEBIT",-10));jdbc.update("UPDATE wx_member SET member_integral=2147483647");
        assertThrows(IllegalStateException.class,()->service.refund(40,"L100"));assertEquals(1,count("1=1"));
        jdbc.update("UPDATE wx_member SET member_integral=90");service.refund(40,"L100");assertEquals(100,balance());assertEquals(2,count("1=1"));
    }
    @Test void missingOrDeletedMemberDoesNotCreatePoints(){
        assertFalse(service.change(41,"L100:AWARD",50));jdbc.update("UPDATE wx_member SET deleted=1");assertFalse(service.change(40,"L100:AWARD",50));assertEquals(0,count("1=1"));
    }
    @Test void softDeletedSuccessfulLogStillPreventsDuplicateAward(){
        assertTrue(service.change(40,"L100:AWARD",50));jdbc.update("UPDATE points_log SET deleted=TRUE,points_log_status=2");
        assertTrue(service.change(40,"L100:AWARD",50));assertEquals(150,balance());assertEquals(1,count("1=1"));
    }
    @Test void migratedFailureMarkerPreventsChargingOldRejectedDebit(){
        jdbc.update("INSERT INTO points_log VALUES(1,10,40,0,'L100:DEBIT',-10,2,0,2,TRUE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        assertFalse(service.change(40,"L100:DEBIT",-10));assertEquals(100,balance());
    }
    @Test void duplicatesAreRejectedInsteadOfSilentlyChangingBalance(){
        jdbc.update("INSERT INTO points_log VALUES(1,10,40,0,'L100:AWARD',50,1,1,2,FALSE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP),(2,10,40,0,'L100:AWARD',50,1,1,2,FALSE,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
        assertThrows(IllegalStateException.class,()->service.change(40,"L100:AWARD",50));assertEquals(100,balance());
    }
    @Test void projectAndMemberScopeAreIsolated(){
        jdbc.update("INSERT INTO wx_member VALUES(11,40,0,0,100,NULL,NULL,CURRENT_TIMESTAMP),(10,41,1,0,100,NULL,NULL,CURRENT_TIMESTAMP)");
        assertTrue(service.change(40,"L100:AWARD",50));BusinessContextHolder.setBusinessId(11L);assertTrue(service.change(40,"L100:AWARD",50));
        BusinessContextHolder.setBusinessId(10L);assertTrue(service.change(41,"L100:AWARD",50));assertEquals(3,count("1=1"));
    }
    @Test void concurrentRetriesCreditExactlyOnce() throws Exception {
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(4);var futures=new ArrayList<Future<Boolean>>();
        try {
            for(int i=0;i<4;i++)futures.add(pool.submit(()->{BusinessContextHolder.setBusinessId(10L);try{start.await();return service.change(40,"L100:AWARD",50);}finally{BusinessContextHolder.clear();}}));
            start.countDown();for(var future:futures)assertTrue(future.get(10,TimeUnit.SECONDS));
            assertEquals(150,balance());assertEquals(1,count("1=1"));
        } finally {pool.shutdownNow();assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));}
    }
    @Test void invalidRequestsDoNotChangeBalance(){
        assertThrows(IllegalArgumentException.class,()->service.change(40,"invalid",10));assertThrows(IllegalArgumentException.class,()->service.refund(40,"L100:DEBIT"));assertEquals(100,balance());
    }
    @Test void publishedMigrationPreservesRejectedRowsWithoutChangingBalances() throws Exception {
        Path root=Path.of("").toAbsolutePath();
        while(root!=null&&!Files.exists(root.resolve("sql/lottery_release_all.sql")))root=root.getParent();
        assertNotNull(root,"Published SQL must be available");
        var script=Files.readAllLines(root.resolve("sql/lottery_release_all.sql"));
        assertTrue(script.stream().noneMatch(line->line.contains("CREATE TABLE IF NOT EXISTS member_lottery_points")));
        jdbc.execute("ALTER TABLE points_log ADD COLUMN product_name VARCHAR(64)");
        for(int shard=0;shard<10;shard++) {
            jdbc.execute("CREATE TABLE points_log_"+shard+" AS SELECT * FROM points_log WHERE 1=0");
            jdbc.execute("CREATE TABLE member_lottery_points_"+shard+"(business_id BIGINT,member_id BIGINT,sharding_value INT,business_no VARCHAR(64),delta INT,state VARCHAR(16),created_at TIMESTAMP)");
            jdbc.update("INSERT INTO member_lottery_points_"+shard+" VALUES(10,40,?,'L100:DEBIT',-200,'REJECTED',CURRENT_TIMESTAMP),(10,40,?,'L101:AWARD',50,'SUCCESS',CURRENT_TIMESTAMP)",shard,shard);
            jdbc.update("INSERT INTO points_log_"+shard+"(points_log_id,business_id,member_id,sharding_value,log_code,points_change,points_log_status,is_points_product,deleted) VALUES(1,10,40,?,'L101:AWARD',50,1,2,FALSE)",shard);
        }
        var migrations=script.stream().map(String::strip).filter(line->line.startsWith("'INSERT INTO `points_log_")).map(line->line.substring(1,line.length()-2).replace("''","'").replace("UUID_SHORT()","12345")).toList();
        assertEquals(10,migrations.size());
        for(String migration:migrations){assertEquals(1,jdbc.update(migration));assertEquals(0,jdbc.update(migration));}
        var checks=script.stream().map(String::strip).filter(line->line.startsWith("'SELECT ''member_lottery_points_")).map(line->line.substring(1,line.length()-2).replace("''","'")).toList();
        assertEquals(10,checks.size());
        for(String check:checks)assertEquals(0L,((Number)jdbc.queryForMap(check).get("missing_success_logs")).longValue());
        assertEquals(100,balance());
        assertEquals("抽奖消耗积分",jdbc.queryForObject("SELECT product_name FROM points_log_0 WHERE log_code='L100:DEBIT'",String.class));
    }
}
