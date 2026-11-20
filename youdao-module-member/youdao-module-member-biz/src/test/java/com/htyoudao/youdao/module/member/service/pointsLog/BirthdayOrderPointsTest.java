package com.htyoudao.youdao.module.member.service.pointsLog;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.htyoudao.youdao.module.member.api.point.VO.ClientAddMemberPointReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.pointsLog.PointsLogDO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.dal.mysql.pointsLog.PointsLogMapper;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BirthdayOrderPointsTest {

    private final PointsLogServiceImpl service = new PointsLogServiceImpl();
    private final PointsLogMapper pointsLogMapper = mock(PointsLogMapper.class);
    private final WxMemberDO member = new WxMemberDO();

    @BeforeEach
    void setUp() {
        member.setMemberId(11L);
        member.setShardingValue(1);
        member.setUserIdentity(1);
        member.setFreezePoints(0L);
        member.setMemberBirthday(birthday(LocalDate.of(1998, 11, 20)));

        WxMemberMapper memberMapper = mock(WxMemberMapper.class);
        when(memberMapper.selectOne(any())).thenReturn(member);
        IdentifierGenerator identifierGenerator = mock(IdentifierGenerator.class);
        when(identifierGenerator.nextId(any())).thenReturn(123456L);
        ReflectionTestUtils.setField(service, "wxMemberMapper", memberMapper);
        ReflectionTestUtils.setField(service, "pointsLogMapper", pointsLogMapper);
        ReflectionTestUtils.setField(service, "identifierGenerator", identifierGenerator);
    }

    @Test
    void birthYearDoesNotPreventDoublePointsOnPaymentDate() {
        PointsLogDO record = awardOn(LocalDate.of(2024, 11, 20));

        assertEquals(40L, record.getPointsChange());
        assertEquals(4, record.getPointsType());
        assertEquals(40L, member.getFreezePoints());
    }

    @Test
    void paymentOnTheNextDayGetsOrdinaryPoints() {
        assertEquals(20L, awardOn(LocalDate.of(2024, 11, 21)).getPointsChange());
    }

    @Test
    void oldRequestUsesTheProcessingDate() {
        member.setMemberBirthday(birthday(LocalDate.now().minusYears(4)));

        assertEquals(40L, awardOn(null).getPointsChange());
    }

    @Test
    void birthdayBonusStillRespectsTheDailyLimit() {
        PointsLogDO earlier = new PointsLogDO();
        earlier.setPointsChange(68L);
        when(pointsLogMapper.selectList(any())).thenReturn(List.of(earlier));

        assertEquals(32L, awardOn(LocalDate.of(2024, 11, 20)).getPointsChange());
    }

    private PointsLogDO awardOn(LocalDate payDate) {
        BigDecimal payAmount = new BigDecimal("20.75");
        ClientAddMemberPointReqVO request = new ClientAddMemberPointReqVO()
                .setMemberId(11L)
                .setOrderSn("test-order")
                .setBusinessId(10L)
                .setOrderAmount(payAmount)
                .setPayAmount(payAmount)
                .setPayDate(payDate);
        assertTrue(service.addMemberPoint(request));

        ArgumentCaptor<PointsLogDO> record = ArgumentCaptor.forClass(PointsLogDO.class);
        verify(pointsLogMapper).insert(record.capture());
        return record.getValue();
    }

    private static Date birthday(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
    }
}
