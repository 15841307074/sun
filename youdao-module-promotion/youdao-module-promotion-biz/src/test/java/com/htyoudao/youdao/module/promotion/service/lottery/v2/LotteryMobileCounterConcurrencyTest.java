package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.test.util.ReflectionTestUtils;
import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 真实事务和唯一键验证同手机号并发，而不是只核对SQL字符串。 */
class LotteryMobileCounterConcurrencyTest {
    JdbcTemplate jdbc;LotteryLedger ledger;
    @BeforeEach void setup() {
        DataSource source=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=1500","sa","");
        jdbc=new JdbcTemplate(source){@Override public List<Map<String,Object>> queryForList(String sql,Object... args){
            // H2不支持MySQL共享读锁语法；配置固定，不参与本测试的竞争。
            if(sql.startsWith("SELECT config_version"))return List.of(Map.of("config_version",1L,"stock_epoch",1L,"runtime_version",2,"state",1));
            return super.queryForList(sql,args);
        }};
        jdbc.execute("CREATE TABLE lottery_mobile_counter(business_id BIGINT,activity_id BIGINT,member_mobile VARCHAR(32),scope_key VARCHAR(128),source INT,gained BIGINT DEFAULT 0,consumed BIGINT DEFAULT 0,finished BIGINT DEFAULT 0,revision BIGINT DEFAULT 1,PRIMARY KEY(business_id,activity_id,member_mobile,scope_key,source))");
        jdbc.execute("CREATE TABLE lottery_v2_counter(business_id BIGINT,activity_id BIGINT,member_id BIGINT,scope_key VARCHAR(128),source INT,gained BIGINT DEFAULT 0,PRIMARY KEY(business_id,activity_id,member_id,scope_key,source))");
        jdbc.execute("CREATE TABLE lottery_v2_request(id BIGINT PRIMARY KEY,business_id BIGINT,activity_id BIGINT,settings_id BIGINT,member_id BIGINT,store_id BIGINT,request_id VARCHAR(64),config_version BIGINT,stock_epoch BIGINT,pool_store_id BIGINT,prize_code VARCHAR(64),prize_id BIGINT,stock_state VARCHAR(32),chance_source INT,scope_key VARCHAR(128),points_cost INT,out_bill_no VARCHAR(64),snapshot_json CLOB,draw_status VARCHAR(32) DEFAULT 'ACCEPTED',grant_status VARCHAR(32) DEFAULT 'PENDING',result_json CLOB,UNIQUE(business_id,activity_id,member_id,request_id))");
        jdbc.execute("CREATE TABLE lottery_v2_job(request_pk BIGINT PRIMARY KEY)");
        var target=new LotteryLedgerImpl();ReflectionTestUtils.setField(target,"jdbc",jdbc);
        ReflectionTestUtils.setField(target,"json",new ObjectMapper());ReflectionTestUtils.setField(target,"quotaCache",mock(LotteryCounterCache.class));
        ReflectionTestUtils.setField(target,"events",mock(org.springframework.context.ApplicationEventPublisher.class));
        var proxy=new ProxyFactory(target);proxy.addAdvice(new TransactionInterceptor(new DataSourceTransactionManager(source),new AnnotationTransactionAttributeSource()));ledger=(LotteryLedger)proxy.getProxy();
    }
    LotteryDrawSnapshot snapshot(long member,String phone,String request) {
        var cfg=new LotterySettingsCacheDataVO();cfg.setId(20L);cfg.setActivityId(21L);cfg.setStockEpoch(1L);cfg.setConfigVersion(1L);cfg.setLotteryLimit(1);cfg.setLotteryTotalNumber(1);
        var req=new LotteryVO();req.setLotteryId(20L);req.setMemberId(member);req.setStoreId(30L);req.setRequestId(request);
        var prize=new LotteryPrizeDO();prize.setId(100L);prize.setCode("guarantee");prize.setIsGuarantees(1);prize.setPrizeType(4);
        var s=new LotteryDrawSnapshot();s.setRequest(req);s.setSettings(cfg);s.setPrize(prize);s.setMemberMobile(phone);s.setScopeKey("20261021");
        s.setChances(List.of(new LotteryDrawSnapshot.Chance(1,"20261021",1,0,false)));return s;
    }
    @Test void twoAccountsWithSamePhoneCannotConsumeTheLastChanceTwice() throws Exception {
        var pool=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
        try {
            List<Future<Boolean>> pending=new ArrayList<>();
            for(long actor:List.of(40L,50L))pending.add(pool.submit(()->{start.await();try{ledger.accept(10,snapshot(actor,"13800000000","request_"+actor));return true;}catch(com.htyoudao.youdao.framework.common.exception.ServiceException limited){return false;}}));
            start.countDown();int won=0;for(var result:pending)if(result.get(8,TimeUnit.SECONDS))won++;
            assertEquals(1,won);assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_request",Integer.class));
            assertEquals(1L,ledger.consumed(10,21,"13800000000","TOTAL",0));
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(DISTINCT member_mobile) FROM lottery_mobile_counter",Integer.class));
        } finally{pool.shutdownNow();}
    }
    @Test void projectAndPhoneAreIsolatedAndIdempotentReplayDoesNotConsumeAgain() {
        var first=snapshot(40,"13800000000","request_40");var accepted=ledger.accept(10,first);
        assertEquals(accepted.getId(),ledger.accept(10,first).getId());
        ledger.accept(10,snapshot(50,"13900000000","request_50"));ledger.accept(11,snapshot(60,"13800000000","request_60"));
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_request",Integer.class));
        assertEquals(1L,ledger.consumed(10,21,"13800000000","TOTAL",0));
    }
    @Test void orderTaskReceiptAndFinishedCountAreSharedByPhone() {
        var cfg=snapshot(40,"13800000000","request_40").getSettings();cfg.setLotteryTotalNumber(0);
        ledger.earnOrder(10,cfg,"13800000000","20261021","20261021",3,false,"order_same");
        ledger.earnOrder(10,cfg,"13800000000","20261021","20261021",3,false,"order_same");
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,21,"13800000000","20261021",3));
        ledger.earn(10,cfg,"13800000000","20261021","20261021",4,1,false,1,0);
        assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class,()->ledger.earn(10,cfg,"13800000000","20261021","20261021",4,1,false,1,0));
        assertArrayEquals(new long[]{1,0,1},ledger.counter(10,21,"13800000000","20261021",4));
    }
}
