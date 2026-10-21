package com.htyoudao.youdao.module.promotion.service.lottery.v2;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryMobileServiceImpl;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryLedgerImpl;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryLogMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.*;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryLogVO;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class LotterySharedRecordAndReturnTest {
    @BeforeAll static void metadata(){TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(),""),LotteryLogDO.class);}
    @AfterEach void clear(){BusinessContextHolder.clear();}
    @Test void alipayAccountUpdatesTheWechatWinningRecordInItsOriginalShard() {
        BusinessContextHolder.setBusinessId(10L);
        var mapper=mock(LotteryLogMapper.class);var identities=mock(LotteryIdentityService.class);
        var owner=new LotteryLogDO();owner.setId(100L);owner.setMemberId(40L);owner.setMemberMobile("13800000000");owner.setPrizeType(3);owner.setPrizeState(1);
        when(identities.mobile(50L)).thenReturn("13800000000");when(identities.matchesMobile(eq("13800000000"),anyString())).thenAnswer(call->"13800000000".equals(call.getArgument(1)));when(identities.relatedMembers("13800000000",50L)).thenReturn(new LinkedHashSet<>(List.of(50L,40L)));
        when(mapper.selectOne(any(Wrapper.class))).thenAnswer(call->{var where=call.getArgument(0,AbstractWrapper.class);where.getSqlSegment();return where.getParamNameValuePairs().containsValue(40L)?owner:null;});
        when(mapper.update(any(Wrapper.class))).thenReturn(1);
        var storage=new LotteryAddressStorage();ReflectionTestUtils.setField(storage,"logs",mapper);
        var mobile=new LotteryMobileServiceImpl();ReflectionTestUtils.setField(mobile,"lotteryLogMapper",mapper);ReflectionTestUtils.setField(mobile,"lotteryIdentityService",identities);ReflectionTestUtils.setField(mobile,"lotteryAddressStorage",storage);
        var request=new LotteryLogVO();request.setId(100L);request.setMemberId(50L);request.setReceiveAddress("支付宝账号填写的地址");
        assertEquals(1,mobile.updateReceivingAddress(request));
        var captured=org.mockito.ArgumentCaptor.forClass(Wrapper.class);verify(mapper).update(captured.capture());
        var where=(AbstractWrapper<?,?,?>)captured.getValue();String sql=where.getSqlSegment();
        assertTrue(sql.contains("member_id"));assertTrue(sql.contains("business_id"));assertTrue(sql.contains("tracking_number"));
        assertTrue(where.getParamNameValuePairs().containsValue(40L));assertFalse(where.getParamNameValuePairs().containsValue(50L));
    }
    @Test void mappedMemberIdAloneCannotAuthorizeADifferentPhoneRecord() {
        BusinessContextHolder.setBusinessId(10L);
        var mapper=mock(LotteryLogMapper.class);var identities=mock(LotteryIdentityService.class);
        var other=new LotteryLogDO();other.setId(100L);other.setMemberId(40L);other.setMemberMobile("13900000000");
        when(identities.mobile(50L)).thenReturn("13800000000");when(identities.matchesMobile(eq("13800000000"),anyString())).thenAnswer(call->"13800000000".equals(call.getArgument(1)));when(identities.relatedMembers("13800000000",50L)).thenReturn(Set.of(40L));
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(other);when(mapper.selectLotteryRows(any())).thenReturn(List.of());
        var mobile=new LotteryMobileServiceImpl();ReflectionTestUtils.setField(mobile,"lotteryLogMapper",mapper);ReflectionTestUtils.setField(mobile,"lotteryIdentityService",identities);
        var request=new LotteryLogVO();request.setId(100L);request.setMemberId(50L);request.setReceiveAddress("无权限地址");
        assertEquals(0,mobile.updateReceivingAddress(request));verify(mapper,never()).update(any(Wrapper.class));
    }
    @Test void appCannotSupplyAnotherMemberId() {
        var identities=new LotteryIdentityService();
        try(var security=mockStatic(SecurityFrameworkUtils.class)) {
            security.when(SecurityFrameworkUtils::getLoginUserId).thenReturn(50L);
            identities.authorize(50L);
            assertThrows(com.htyoudao.youdao.framework.common.exception.ServiceException.class,()->identities.authorize(40L));
        }
    }
    @Test void physicalReturnRepairsMissingAndStoppedJobsButNeverStealsRunningGrant() {
        var jdbc=new JdbcTemplate(new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa",""));
        jdbc.execute("CREATE TABLE lottery_v2_job(request_pk BIGINT PRIMARY KEY,attempts INT DEFAULT 0,action VARCHAR(16) DEFAULT 'GRANT',state VARCHAR(16) DEFAULT 'READY',next_at TIMESTAMP,lease_token VARCHAR(64),lease_until TIMESTAMP)");
        var target=spy(new LotteryLedgerImpl());ReflectionTestUtils.setField(target,"jdbc",jdbc);var events=mock(org.springframework.context.ApplicationEventPublisher.class);ReflectionTestUtils.setField(target,"events",events);
        var draw=new LotteryLedger.Draw();draw.setId(100);draw.setBusinessId(10);draw.setMemberId(40);draw.setDrawStatus("COMPLETED");var snapshot=new LotteryDrawSnapshot();var prize=new LotteryPrizeDO();prize.setPrizeType(3);snapshot.setPrize(prize);draw.setSnapshot(snapshot);doReturn(draw).when(target).get(100);
        var row=new LotteryLogDO();row.setId(100L);row.setBusinessId(10L);row.setMemberId(40L);
        assertTrue(target.queuePhysicalReturn(row));assertEquals("RETURN",jdbc.queryForObject("SELECT action FROM lottery_v2_job",String.class));
        jdbc.update("UPDATE lottery_v2_job SET state='STOPPED',attempts=8");assertTrue(target.queuePhysicalReturn(row));assertEquals("READY",jdbc.queryForObject("SELECT state FROM lottery_v2_job",String.class));
        jdbc.update("UPDATE lottery_v2_job SET state='RUNNING',action='GRANT',lease_until=DATEADD('MINUTE',1,CURRENT_TIMESTAMP)");
        assertTrue(target.queuePhysicalReturn(row));assertEquals("RUNNING",jdbc.queryForObject("SELECT state FROM lottery_v2_job",String.class));verify(events,times(2)).publishEvent(new LotteryLedger.JobReady(100));
    }
    @Test void oneBadRecordDoesNotStopTheNextReturnAndBusinessContextIsRestored() {
        BusinessContextHolder.setBusinessId(99L);
        var rows=mock(LotteryLogMapper.class);var ledger=mock(LotteryLedger.class);var jdbc=mock(JdbcTemplate.class);
        var bad=new LotteryLogDO();bad.setId(100L);bad.setBusinessId(10L);var good=new LotteryLogDO();good.setId(101L);good.setBusinessId(11L);
        when(rows.selectLotteryRows(any())).thenReturn(List.of(bad,good));when(ledger.queuePhysicalReturn(bad)).thenThrow(new IllegalStateException("test record failed"));when(ledger.queuePhysicalReturn(good)).thenReturn(true);
        when(jdbc.queryForList(anyString())).thenReturn(List.of());
        var returns=new LotteryPhysicalReturnService();ReflectionTestUtils.setField(returns,"logs",rows);ReflectionTestUtils.setField(returns,"ledger",ledger);ReflectionTestUtils.setField(returns,"jdbc",jdbc);
        returns.sweep();verify(ledger).queuePhysicalReturn(good);verify(ledger).wakePhysicalReturns();assertEquals(99L,BusinessContextHolder.getBusinessId());
    }
    @Test void jdbcAndTransactionTimeoutsUseTheExistingBusyBusinessResponse() throws Exception {
        var type=com.htyoudao.youdao.module.promotion.controller.app.lottery.AppLotteryController.class;
        var resolver=new org.springframework.web.method.annotation.ExceptionHandlerMethodResolver(type);
        var controller=new com.htyoudao.youdao.module.promotion.controller.app.lottery.AppLotteryController();
        for(RuntimeException failure:List.of(new org.springframework.dao.CannotAcquireLockException("busy"),new org.springframework.transaction.TransactionTimedOutException("timeout"))) {
            var handler=resolver.resolveMethod(failure);assertNotNull(handler);
            var response=(com.htyoudao.youdao.framework.common.pojo.CommonResult<?>)handler.invoke(controller,failure);
            assertEquals(com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN.getCode(),response.getCode());
        }
        var identity=new LotteryIdentityService();
        assertTrue(identity.matchesMobile("13800000000","+8613800000000"));
        assertFalse(identity.matchesMobile("13800000000","13900000000"));
    }

}
