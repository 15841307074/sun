package com.htyoudao.youdao.module.member.service.wxmember;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LotteryPointsReissueTest {
    private JdbcTemplate jdbc;private LotteryPointsService service;private PointsLogMapper logs;
    @BeforeEach void setup() {
        jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE","sa",""));
        jdbc.execute("CREATE TABLE member_lottery_points(business_id BIGINT,member_id BIGINT,sharding_value INT,business_no VARCHAR(64),delta INT,state VARCHAR(16),PRIMARY KEY(business_id,member_id,sharding_value,business_no))");
        jdbc.execute("CREATE TABLE wx_member(business_id BIGINT,member_id BIGINT,sharding_value INT,deleted INT,member_integral BIGINT,member_mobile VARCHAR(32),member_nick_name VARCHAR(64),update_time TIMESTAMP)");
        jdbc.update("INSERT INTO wx_member VALUES(10,40,0,0,0,'13800000000','会员',CURRENT_TIMESTAMP)");
        service=new LotteryPointsService();logs=mock(PointsLogMapper.class);
        ReflectionTestUtils.setField(service,"jdbc",jdbc);ReflectionTestUtils.setField(service,"pointsLogs",logs);
    }
    @Test void rejectedAwardMayBeReissuedOnceAfterFixingBalance() {
        Long previous=BusinessContextHolder.getBusinessId();
        try {
            BusinessContextHolder.setBusinessId(10L);
            jdbc.update("UPDATE wx_member SET member_integral=2147483647");
            assertFalse(service.change(40,"L100:AWARD",100));
            jdbc.update("UPDATE wx_member SET member_integral=0");
            assertTrue(service.change(40,"L100:AWARD",100));
            assertTrue(service.change(40,"L100:AWARD",100));
            assertEquals(100,jdbc.queryForObject("SELECT member_integral FROM wx_member",Integer.class));
            verify(logs,times(1)).insert(any(com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO.class));
        } finally {BusinessContextHolder.setBusinessId(previous);}
    }
    @Test void rejectedDebitIsNotChargedAgain() {
        Long previous=BusinessContextHolder.getBusinessId();
        try {
            BusinessContextHolder.setBusinessId(10L);
            assertFalse(service.change(40,"L100:DEBIT",-50));
            jdbc.update("UPDATE wx_member SET member_integral=100");
            assertFalse(service.change(40,"L100:DEBIT",-50));
            assertEquals(100,jdbc.queryForObject("SELECT member_integral FROM wx_member",Integer.class));
            verifyNoInteractions(logs);
        } finally {BusinessContextHolder.setBusinessId(previous);}
    }
}
