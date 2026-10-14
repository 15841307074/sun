package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.*;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryCounterConsolidationTest {
    private JdbcTemplate jdbc;
    private LotteryLedgerImpl ledger;
    private LotteryCounterCacheImpl cache;
    private StringRedisTemplate redis;
    private TransactionTemplate transaction;
    private LotteryDrawSnapshot snapshot;
    @BeforeEach void setup() {
        var source=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
        jdbc=new JdbcTemplate(source){
            @Override public List<Map<String,Object>> queryForList(String sql,Object... args){return super.queryForList(sql.replace("LOCK IN SHARE MODE","FOR UPDATE"),args);}
            @Override public int update(String sql,Object... args){return super.update(sql.replace("DATE_ADD(NOW(3),INTERVAL 10 SECOND)","DATEADD('SECOND',10,CURRENT_TIMESTAMP)"),args);}
        };
        transaction=new TransactionTemplate(new DataSourceTransactionManager(source));
        jdbc.execute("CREATE TABLE lottery_v2_counter(business_id BIGINT,activity_id BIGINT,member_id BIGINT,scope_key VARCHAR(128),source INT,gained BIGINT DEFAULT 0,consumed BIGINT DEFAULT 0,finished BIGINT DEFAULT 0,revision BIGINT DEFAULT 1,cache_dirty INT DEFAULT 0,cache_next_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(business_id,activity_id,member_id,scope_key,source))");
        jdbc.execute("CREATE INDEX idx_lottery_counter_publish ON lottery_v2_counter(scope_key,source,cache_dirty,cache_next_at)");
        jdbc.execute("CREATE TABLE lottery_settings(id BIGINT,business_id BIGINT,config_version BIGINT,stock_epoch BIGINT,runtime_version INT,state INT,deleted INT)");
        jdbc.execute("INSERT INTO lottery_settings VALUES(20,10,1,1,2,1,0)");
        jdbc.execute("CREATE TABLE lottery_v2_request(id BIGINT PRIMARY KEY,business_id BIGINT,activity_id BIGINT,settings_id BIGINT,member_id BIGINT,store_id BIGINT,request_id VARCHAR(64),config_version BIGINT,stock_epoch BIGINT,pool_store_id BIGINT,prize_code VARCHAR(64),prize_id BIGINT,draw_status VARCHAR(24) DEFAULT 'ACCEPTED',grant_status VARCHAR(24) DEFAULT 'PENDING',stock_state VARCHAR(16),chance_source INT,scope_key VARCHAR(128),points_cost INT,out_bill_no VARCHAR(64),snapshot_json LONGTEXT,result_json LONGTEXT,UNIQUE(business_id,activity_id,member_id,request_id))");
        jdbc.execute("CREATE TABLE lottery_v2_job(request_pk BIGINT PRIMARY KEY)");
        cache=new LotteryCounterCacheImpl();redis=mock(StringRedisTemplate.class);
        ReflectionTestUtils.setField(cache,"jdbc",jdbc);ReflectionTestUtils.setField(cache,"redis",redis);ReflectionTestUtils.setField(cache,"json",new ObjectMapper());
        var executor=mock(LotteryCacheExecutor.class);ReflectionTestUtils.setField(cache,"executor",executor);
        ledger=new LotteryLedgerImpl();ReflectionTestUtils.setField(ledger,"jdbc",jdbc);ReflectionTestUtils.setField(ledger,"json",new ObjectMapper());
        ReflectionTestUtils.setField(ledger,"events",mock(org.springframework.context.ApplicationEventPublisher.class));ReflectionTestUtils.setField(ledger,"quotaCache",cache);
        var cfg=new LotterySettingsCacheDataVO();cfg.setId(20L);cfg.setActivityId(21L);cfg.setConfigVersion(1L);cfg.setStockEpoch(1L);cfg.setLotteryLimit(0);cfg.setLotteryTotalNumber(0);
        var request=new LotteryVO();request.setLotteryId(20L);request.setMemberId(40L);request.setStoreId(30L);request.setRequestId("request_123");
        var prize=new LotteryPrizeDO();prize.setId(100L);prize.setCode("guarantee");prize.setIsGuarantees(1);prize.setPrizeType(4);
        snapshot=new LotteryDrawSnapshot();snapshot.setSettings(cfg);snapshot.setRequest(request);snapshot.setPrize(prize);snapshot.setGuarantee(prize);snapshot.setScopeKey("20261012");snapshot.setChances(List.of(new LotteryDrawSnapshot.Chance(1,"TOTAL",-1,0,false)));
    }
    @AfterEach void cleanup(){jdbc.execute("DROP ALL OBJECTS");}
    private LotteryLedger.Draw accept(){return transaction.execute(status->ledger.accept(10,snapshot));}
    private int dirty(){return jdbc.queryForObject("SELECT cache_dirty FROM lottery_v2_counter WHERE business_id=10 AND activity_id=21 AND member_id=40 AND scope_key='TOTAL' AND source=0",Integer.class);}
    @Test void acceptUsesOnlyExistingCounterTableAndRetriesDoNotChargeAgain(){
        var original=accept();assertTrue(original.isNewlyAccepted());assertEquals(1,ledger.activityDrawCount(10,21));
        assertEquals(1,ledger.consumed(10,21,40,"TOTAL",0));assertEquals(1,ledger.consumed(10,21,40,"20261012",0));assertEquals(1,dirty());
        var again=accept();assertFalse(again.isNewlyAccepted());assertEquals(original.getId(),again.getId());assertEquals(1,ledger.activityDrawCount(10,21));assertEquals(1,ledger.consumed(10,21,40,"TOTAL",0));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_request",Integer.class));
    }
    @Test void unpaidDrawRestoresCountersAndActivityTotalOnce(){
        var draw=accept();transaction.executeWithoutResult(status->ledger.rejectUnpaid(draw.getId()));
        assertEquals(0,ledger.activityDrawCount(10,21));assertEquals(0,ledger.consumed(10,21,40,"TOTAL",0));assertEquals(0,ledger.consumed(10,21,40,"20261012",0));
        transaction.executeWithoutResult(status->ledger.rejectUnpaid(draw.getId()));assertEquals(0,ledger.activityDrawCount(10,21));
    }
    @Test void failedAcceptRollsBackAllCountersAndQueueFlags(){
        snapshot.getSettings().setLotteryLimit(1);accept();snapshot.getRequest().setRequestId("request_456");
        assertThrows(RuntimeException.class,this::accept);assertEquals(1,ledger.activityDrawCount(10,21));assertEquals(1,ledger.consumed(10,21,40,"TOTAL",0));
    }
    @Test void quotaPublisherAcknowledgesTheCurrentRevision(){
        accept();cache.retry();assertEquals(0,dirty());
        verify(redis,atLeastOnce()).execute(any(RedisScript.class),anyList(),any(Object[].class));
    }
    @Test void delayedAcknowledgmentCannotLoseAConcurrentCounterChange(){
        accept();doAnswer(call->{jdbc.update("UPDATE lottery_v2_counter SET revision=revision+1,cache_dirty=1 WHERE scope_key='TOTAL' AND source=0");return 1L;}).when(redis).execute(any(RedisScript.class),anyList(),any(Object[].class));
        cache.retry();assertEquals(1,dirty());
    }
    @Test void redisFailureLeavesDurableQueueFlagAndDoesNotDelayNewRevision(){
        accept();doAnswer(call->{jdbc.update("UPDATE lottery_v2_counter SET revision=revision+1,cache_next_at=CURRENT_TIMESTAMP WHERE scope_key='TOTAL' AND source=0");throw new IllegalStateException("Redis unavailable");}).when(redis).execute(any(RedisScript.class),anyList(),any(Object[].class));
        cache.retry();assertEquals(1,dirty());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_counter WHERE scope_key='TOTAL' AND source=0 AND cache_next_at<=CURRENT_TIMESTAMP",Integer.class));
    }
    @Test void activitySummaryDoesNotAppearInMemberQuota(){
        accept();assertEquals(3,ledger.counters(10,21,40,"20261012").size());assertTrue(ledger.counters(10,21,0,"20261012").isEmpty());assertEquals(0,ledger.activityDrawCount(11,21));
    }
    @Test void redisOutageIsRetriedLaterFromCounterRow(){
        accept();when(redis.execute(any(RedisScript.class),anyList(),any(Object[].class))).thenThrow(new IllegalStateException("Redis unavailable")).thenReturn(1L);
        cache.retry();assertEquals(1,dirty());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_counter WHERE cache_dirty=1 AND cache_next_at>CURRENT_TIMESTAMP",Integer.class));
        // H2 TIMESTAMP 精度高于业务 NOW(3)，使用已到期的时间避免同毫秒精度竞态。
        jdbc.update("UPDATE lottery_v2_counter SET cache_next_at=DATEADD('SECOND',-1,CURRENT_TIMESTAMP) WHERE cache_dirty=1");cache.retry();assertEquals(0,dirty());
    }
    @Test void unlimitedPointsAndFreeOpportunitiesDoNotBlockTaskEarning() {
        var cfg=snapshot.getSettings();cfg.setLotteryTotalNumber(3);cfg.setPointsStatus(1);cfg.setPointsType(1);cfg.setPrice(1);cfg.setFreeStatus(1);cfg.setFreeCount(10);
        transaction.executeWithoutResult(status->ledger.earn(10,cfg,40,"20261012","20261012",4,2,false,1,100));
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,21,40,"20261012",4));
    }
    @Test void orderNotificationsAreIdempotentAndDistinctActivitiesEarnIndependently() {
        var cfg=snapshot.getSettings();
        transaction.executeWithoutResult(status->ledger.earnOrder(10,cfg,40,"20261012","20261012",2,false,"order-one"));
        transaction.executeWithoutResult(status->ledger.earnOrder(10,cfg,40,"20261012","20261012",2,false,"order-one"));
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,21,40,"20261012",3));
        cfg.setActivityId(22L);
        transaction.executeWithoutResult(status->ledger.earnOrder(10,cfg,40,"20261012","20261012",2,false,"order-one"));
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,22,40,"20261012",3));
    }
    @Test void failedOrderRewardRollsBackReceiptAndCanBeRetried() {
        var cfg=snapshot.getSettings();
        assertThrows(RuntimeException.class,()->transaction.executeWithoutResult(status->ledger.earnOrder(10,cfg,40,"20261012","20261012",0,false,"order-retry")));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_counter WHERE scope_key='ORDER:order-retry'",Integer.class));
        transaction.executeWithoutResult(status->ledger.earnOrder(10,cfg,40,"20261012","20261012",1,false,"order-retry"));
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,21,40,"20261012",3));
    }
    @Test void quotaHeadIsPublishedBeforeTaskReturnsAndRollbackDoesNotPublish() {
        var cfg=snapshot.getSettings();
        transaction.executeWithoutResult(status->ledger.earn(10,cfg,40,"20261012","20261012",4,2,false,1,0));
        verify(redis,times(1)).execute(any(RedisScript.class),anyList(),any(Object[].class));
        clearInvocations(redis);
        transaction.executeWithoutResult(status->{ledger.earn(10,cfg,40,"20261012","20261012",4,2,false,1,0);status.setRollbackOnly();});
        verifyNoInteractions(redis);
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,21,40,"20261012",4));
    }
}
