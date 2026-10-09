package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class LotteryReissueLedgerTest {
    private JdbcTemplate jdbc;
    private LotteryLedger ledger;
    private TransactionTemplate transaction;
    @BeforeEach void setup() {
        var source=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE","sa","");
        jdbc=new JdbcTemplate(source); transaction=new TransactionTemplate(new DataSourceTransactionManager(source));
        jdbc.execute("CREATE TABLE lottery_v2_request(id BIGINT PRIMARY KEY,business_id BIGINT,activity_id BIGINT,settings_id BIGINT,member_id BIGINT,store_id BIGINT,stock_epoch BIGINT,pool_store_id BIGINT,request_id VARCHAR(64),out_bill_no VARCHAR(64),prize_code VARCHAR(64),draw_status VARCHAR(24),grant_status VARCHAR(24),stock_state VARCHAR(16),scope_key VARCHAR(128),chance_source INT,points_cost INT,snapshot_json LONGTEXT,result_json LONGTEXT)");
        jdbc.execute("CREATE TABLE lottery_v2_job(request_pk BIGINT PRIMARY KEY,state VARCHAR(16),action VARCHAR(16),next_at DATETIME(3),lease_token VARCHAR(64),lease_until DATETIME(3),last_error VARCHAR(512))");
        jdbc.execute("CREATE TABLE lottery_v2_stock(business_id BIGINT,activity_id BIGINT,stock_epoch BIGINT,pool_store_id BIGINT,prize_code VARCHAR(64),total BIGINT,reserved BIGINT,issued BIGINT,revision BIGINT)");
        ledger=new LotteryLedgerImpl();ReflectionTestUtils.setField(ledger,"jdbc",jdbc);ReflectionTestUtils.setField(ledger,"json",new ObjectMapper());
        var snapshot=new LotteryDrawSnapshot();var prize=new LotteryPrizeDO();prize.setId(101L);prize.setCode("prize");prize.setIsGuarantees(0);prize.setPrizeType(2);
        snapshot.setPrize(prize);var cfg=new LotterySettingsCacheDataVO();cfg.setId(21L);snapshot.setSettings(cfg);
        jdbc.update("INSERT INTO lottery_v2_request VALUES(100,10,20,21,40,30,1,0,'request_123','L100','prize','FAILED','FAILED','RELEASED','TOTAL',2,50,?,NULL)",ledger.encode(snapshot));
        jdbc.update("INSERT INTO lottery_v2_job(request_pk,state,action) VALUES(100,'DONE','GRANT')");
        jdbc.update("INSERT INTO lottery_v2_stock VALUES(10,20,1,0,'prize',2,0,0,1)");
        // 故意不建次数表。补发不能尝试再次扣次或修改次数计数。
    }
    private LotteryLedger.Reissue reissue(long b,long m,long store) {
        return transaction.execute(status->ledger.reissue(b,100,m,store,"request_123","L100",0,false));
    }
    @Test void failedRequestReservesOriginalStockWithoutChargingCounters() {
        var result=reissue(10,40,30);
        assertTrue(result.queued());assertTrue(result.draw().getSnapshot().isManualReissue());
        assertEquals(1,result.draw().getSnapshot().getReissueSequence());
        assertEquals("ACCEPTED",result.draw().getDrawStatus());
        assertEquals(1,jdbc.queryForObject("SELECT reserved FROM lottery_v2_stock",Integer.class));
        assertEquals("READY",jdbc.queryForObject("SELECT state FROM lottery_v2_job",String.class));
        assertEquals(50,result.draw().getPointsCost()); // 原流水记录保留，worker 不重复扣它。
    }
    @Test void repeatedRequestDoesNotReserveTwice() {
        reissue(10,40,30);
        var again=reissue(10,40,30);
        assertFalse(again.queued());assertEquals(1,jdbc.queryForObject("SELECT reserved FROM lottery_v2_stock",Integer.class));
        assertEquals(1,again.draw().getSnapshot().getReissueSequence());
    }
    @Test void noOriginalStockRollsBack() {
        jdbc.update("UPDATE lottery_v2_stock SET total=0");
        assertThrows(ServiceException.class,()->reissue(10,40,30));
        assertEquals("FAILED",ledger.get(100).getDrawStatus());
        assertEquals("DONE",jdbc.queryForObject("SELECT state FROM lottery_v2_job",String.class));
    }
    @Test void rejectsOtherProjectMemberOrStore() {
        assertThrows(ServiceException.class,()->reissue(11,40,30));
        assertThrows(ServiceException.class,()->reissue(10,41,30));
        assertThrows(ServiceException.class,()->reissue(10,40,31));
    }
    @Test void successfulAndRunningRequestsAreNotRescheduled() {
        jdbc.update("UPDATE lottery_v2_request SET draw_status='COMPLETED',grant_status='SUCCESS',stock_state='ISSUED'");
        assertFalse(reissue(10,40,30).queued());
        jdbc.update("UPDATE lottery_v2_request SET draw_status='ACCEPTED',grant_status='PROCESSING',stock_state='RESERVED'");
        jdbc.update("UPDATE lottery_v2_job SET state='RUNNING'");
        assertFalse(reissue(10,40,30).queued());
    }
    @Test void returnedRequestCannotBeReissued() {
        jdbc.update("UPDATE lottery_v2_request SET draw_status='RETURNED',stock_state='RETURNED'");
        assertThrows(ServiceException.class,()->reissue(10,40,30));
    }
    @Test void unknownCashKeepsOriginalBillAndKnownFailureRotatesOnlyOnce() {
        var draw=ledger.get(100);draw.getSnapshot().getPrize().setPrizeType(5);
        jdbc.update("UPDATE lottery_v2_request SET snapshot_json=?",ledger.encode(draw.getSnapshot()));
        var scheduled=transaction.execute(status->ledger.reissue(10,100,40,30,"request_123","L100",0,true));
        assertEquals("L100R1",scheduled.draw().cashBillNo());
        assertFalse(transaction.execute(status->ledger.reissue(10,100,40,30,"request_123","L100R1",1,true)).queued());
    }
    @Test void unknownOrMissingCashDoesNotChangeBill() {
        var draw=ledger.get(100);draw.getSnapshot().getPrize().setPrizeType(5);
        jdbc.update("UPDATE lottery_v2_request SET snapshot_json=?",ledger.encode(draw.getSnapshot()));
        assertEquals("L100",reissue(10,40,30).draw().cashBillNo());
    }
    @Test void callbacksOnlyFindCurrentReissueBill() {
        String base="L2104868000000000001";
        var draw=ledger.get(100);draw.getSnapshot().getPrize().setPrizeType(5);
        jdbc.update("UPDATE lottery_v2_request SET out_bill_no=?,snapshot_json=?",base,ledger.encode(draw.getSnapshot()));
        transaction.execute(status->ledger.reissue(10,100,40,30,"request_123",base,0,true));
        assertNotNull(ledger.byBill(base+"R1"));
        var current=ledger.get(100);current.getSnapshot().setReissueCashBillNo(base+"R2");
        current.getSnapshot().setReissueSequence(2);
        jdbc.update("UPDATE lottery_v2_request SET snapshot_json=?",ledger.encode(current.getSnapshot()));
        assertNull(ledger.byBill(base+"R1"));
        assertNotNull(ledger.byBill(base+"R2"));
        jdbc.update("UPDATE lottery_v2_job SET next_at=TIMESTAMP '2099-01-01 00:00:00'");
        assertTrue(ledger.wakeByBill(base+"R1"));
        assertEquals(2099,jdbc.queryForObject("SELECT YEAR(next_at) FROM lottery_v2_job",Integer.class));
    }
    @Test void concurrentManualCallsOnlyScheduleAndReserveOnce() throws Exception {
        var pool=java.util.concurrent.Executors.newFixedThreadPool(4);
        var start=new java.util.concurrent.CountDownLatch(1);
        try {
            var futures=new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for(int i=0;i<4;i++)futures.add(pool.submit(()->{start.await();return reissue(10,40,30).queued();}));
            start.countDown();int queued=0;
            for(var future:futures)if(future.get(10,java.util.concurrent.TimeUnit.SECONDS))queued++;
            assertEquals(1,queued);
            assertEquals(1,jdbc.queryForObject("SELECT reserved FROM lottery_v2_stock",Integer.class));
        } finally {pool.shutdownNow();}
    }
}
