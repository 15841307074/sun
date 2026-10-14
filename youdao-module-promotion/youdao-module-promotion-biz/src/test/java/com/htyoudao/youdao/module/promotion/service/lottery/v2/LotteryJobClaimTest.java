package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LotteryJobClaimTest {
    private JdbcTemplate jdbc;
    private LotteryLedgerImpl ledger;
    private AtomicInteger updates;
    @BeforeEach void setup() {
        updates=new AtomicInteger();
        var source=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1","sa","");
        jdbc=new JdbcTemplate(source){
            @Override public int update(String sql,Object... args) {
                if(sql.startsWith("UPDATE lottery_v2_job"))updates.incrementAndGet();
                return super.update(sql.replace("DATE_ADD(NOW(3),INTERVAL 60 SECOND)","DATEADD('SECOND',60,CURRENT_TIMESTAMP)"),args);
            }
        };
        jdbc.execute("CREATE TABLE lottery_v2_job(request_pk BIGINT PRIMARY KEY,state VARCHAR(16),attempts INT,next_at TIMESTAMP,lease_token VARCHAR(64),lease_until TIMESTAMP,last_error VARCHAR(512))");
        ledger=new LotteryLedgerImpl();ReflectionTestUtils.setField(ledger,"jdbc",jdbc);
    }
    @AfterEach void cleanup(){jdbc.execute("DROP ALL OBJECTS");}
    private void job(int attempts,String error) {
        jdbc.update("INSERT INTO lottery_v2_job VALUES(1,'READY',?,DATEADD('SECOND',-1,CURRENT_TIMESTAMP),NULL,NULL,?)",attempts,error);
    }
    @Test void normalClaimNeedsOnlyOneUpdateAndCannotBeClaimedAgain() {
        job(0,null);assertTrue(ledger.claim(1,"first"));assertEquals(1,updates.get());
        assertFalse(ledger.claim(1,"second"));assertEquals("first",jdbc.queryForObject("SELECT lease_token FROM lottery_v2_job",String.class));
        assertEquals(1,jdbc.queryForObject("SELECT attempts FROM lottery_v2_job",Integer.class));
    }
    @Test void failedAwardsStopAtTheOriginalLimitAndReleaseTheLease() {
        job(8,"timeout");assertFalse(ledger.claim(1,"new"));
        assertEquals("STOPPED",jdbc.queryForObject("SELECT state FROM lottery_v2_job",String.class));
        assertNull(jdbc.queryForObject("SELECT lease_token FROM lottery_v2_job",String.class));
        assertNull(jdbc.queryForObject("SELECT lease_until FROM lottery_v2_job",java.sql.Timestamp.class));
        assertEquals(8,jdbc.queryForObject("SELECT attempts FROM lottery_v2_job",Integer.class));
    }
    @Test void waitingWechatKeepsItsSeparateThirtyAttemptLimit() {
        job(29,"微信红包处理中");assertTrue(ledger.claim(1,"last"));
        assertEquals(30,jdbc.queryForObject("SELECT attempts FROM lottery_v2_job",Integer.class));
        jdbc.update("UPDATE lottery_v2_job SET state='READY',next_at=DATEADD('SECOND',-1,CURRENT_TIMESTAMP) WHERE request_pk=1");
        assertFalse(ledger.claim(1,"again"));assertEquals("STOPPED",jdbc.queryForObject("SELECT state FROM lottery_v2_job",String.class));
    }
    @Test void expiredLeaseCanBeTakenOverWithoutDoubleIncrement() {
        job(2,"timeout");jdbc.update("UPDATE lottery_v2_job SET state='RUNNING',lease_token='old',lease_until=DATEADD('SECOND',-1,CURRENT_TIMESTAMP)");
        assertTrue(ledger.claim(1,"recovered"));assertEquals(3,jdbc.queryForObject("SELECT attempts FROM lottery_v2_job",Integer.class));
        assertFalse(ledger.claim(1,"other"));assertEquals("recovered",jdbc.queryForObject("SELECT lease_token FROM lottery_v2_job",String.class));
    }
}
