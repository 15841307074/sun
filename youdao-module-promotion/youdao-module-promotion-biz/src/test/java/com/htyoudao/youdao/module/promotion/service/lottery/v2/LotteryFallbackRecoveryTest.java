package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryV2ServiceImpl;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryMobileServiceImpl;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.junit.jupiter.api.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryFallbackRecoveryTest {
    private LotteryV2ServiceImpl service;
    private LotteryLedger ledger;
    private LotteryDrawSnapshot snapshot;
    private LotteryVO request;
    private LotterySettingsCacheDataVO cfg;
    private LotteryLedger.Draw accepted;
    private Long previousBusiness;
    @BeforeEach void setup(){
        previousBusiness=BusinessContextHolder.getBusinessId();BusinessContextHolder.setBusinessId(10L);
        service=new LotteryV2ServiceImpl();ledger=mock(LotteryLedger.class);var admission=mock(LotteryAdmission.class);var legacy=mock(LotteryMobileServiceImpl.class);var cache=mock(LotteryConfigurationCache.class);var stock=mock(LotteryStockCache.class);var results=mock(LotteryResultCache.class);
        ReflectionTestUtils.setField(service,"ledger",ledger);ReflectionTestUtils.setField(service,"admission",admission);ReflectionTestUtils.setField(service,"legacy",legacy);ReflectionTestUtils.setField(service,"cache",cache);ReflectionTestUtils.setField(service,"stock",stock);ReflectionTestUtils.setField(service,"results",results);ReflectionTestUtils.setField(service,"properties",new LotteryProperties());
        request=new LotteryVO();request.setLotteryId(20L);request.setMemberId(40L);request.setStoreId(30L);request.setRequestId("request_123");
        cfg=new LotterySettingsCacheDataVO();cfg.setId(20L);cfg.setActivityId(21L);cfg.setRuntimeVersion(2);
        snapshot=new LotteryDrawSnapshot();var prize=new LotteryPrizeDO();prize.setId(100L);prize.setIsGuarantees(0);var guarantee=new LotteryPrizeDO();guarantee.setId(101L);guarantee.setPrizeType(4);guarantee.setIsGuarantees(1);snapshot.setPrize(prize);snapshot.setGuarantee(guarantee);
        when(admission.enter("draw",10L,21L,40L)).thenReturn(mock(LotteryAdmission.Permit.class));when(legacy.prepareV2Draw(eq(request),eq(cfg),any())).thenReturn(snapshot);
        accepted=new LotteryLedger.Draw();accepted.setStoreId(30L);accepted.setRequestId("request_123");accepted.setSnapshot(snapshot);accepted.setGrantStatus("PENDING");
    }
    @AfterEach void cleanup(){BusinessContextHolder.setBusinessId(previousBusiness);}
    @Test void fallbackDuplicateReturnsAlreadyAcceptedResult(){
        when(ledger.accept(10L,snapshot)).thenThrow(new LotteryLedger.UnavailablePrize()).thenThrow(new DuplicateKeyException("same request"));when(ledger.find(10L,21L,40L,"request_123")).thenReturn(accepted);
        assertEquals(101L,service.drawAfterResultMiss(request,cfg).getLotteryPrizeId());verify(ledger,times(2)).accept(10L,snapshot);
    }
    @Test void uncertainFallbackCommitRecoversAcceptedResult(){
        when(ledger.accept(10L,snapshot)).thenThrow(new LotteryLedger.UnavailablePrize()).thenThrow(new IllegalStateException("connection dropped after commit"));when(ledger.find(10L,21L,40L,"request_123")).thenReturn(accepted);
        assertEquals("request_123",service.drawAfterResultMiss(request,cfg).getRequestId());
    }
    @Test void failedFallbackWithoutPersistedRequestStillFails(){
        var error=new IllegalStateException("database unavailable");when(ledger.accept(10L,snapshot)).thenThrow(new LotteryLedger.UnavailablePrize()).thenThrow(error);
        assertSame(error,assertThrows(IllegalStateException.class,()->service.drawAfterResultMiss(request,cfg)));
    }
}
