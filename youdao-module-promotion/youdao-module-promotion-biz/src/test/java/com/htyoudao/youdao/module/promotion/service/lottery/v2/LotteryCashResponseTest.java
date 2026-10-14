package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryVO;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.promotion.service.lottery.impl.LotteryMobileServiceImpl;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryV2ServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryCashResponseTest {
    private LotteryV2ServiceImpl service;
    private LotteryLedger ledger;
    private LotteryGrantWorker grants;
    private LotteryStockCache.Reservation reservation;
    private LotteryVO req;
    private LotterySettingsCacheDataVO cfg;
    private LotteryLedger.Draw draw;
    @BeforeEach void setup() {
        BusinessContextHolder.setBusinessId(10L);service=new LotteryV2ServiceImpl();ledger=mock(LotteryLedger.class);grants=mock(LotteryGrantWorker.class);
        var admission=mock(LotteryAdmission.class);when(admission.enter(anyString(),anyLong(),anyLong(),any())).thenReturn(mock(LotteryAdmission.Permit.class));
        var cache=mock(LotteryConfigurationCache.class);var legacy=mock(LotteryMobileServiceImpl.class);var stock=mock(LotteryStockCache.class);reservation=mock(LotteryStockCache.Reservation.class);
        ReflectionTestUtils.setField(service,"ledger",ledger);ReflectionTestUtils.setField(service,"grants",grants);ReflectionTestUtils.setField(service,"admission",admission);
        ReflectionTestUtils.setField(service,"cache",cache);ReflectionTestUtils.setField(service,"legacy",legacy);ReflectionTestUtils.setField(service,"stock",stock);
        ReflectionTestUtils.setField(service,"properties",new LotteryProperties());ReflectionTestUtils.setField(service,"results",mock(LotteryResultCache.class));
        req=new LotteryVO();req.setLotteryId(20L);req.setMemberId(40L);req.setStoreId(30L);req.setRequestId("request_123");
        cfg=new LotterySettingsCacheDataVO();cfg.setId(20L);cfg.setActivityId(21L);cfg.setRuntimeVersion(2);
        var snapshot=new LotteryDrawSnapshot();snapshot.setSettings(cfg);snapshot.setRequest(req);var prize=new LotteryPrizeDO();prize.setId(100L);prize.setPrizeType(5);prize.setIsGuarantees(0);snapshot.setPrize(prize);
        draw=new LotteryLedger.Draw();draw.setId(7);draw.setBusinessId(10);draw.setActivityId(21);draw.setMemberId(40);draw.setStoreId(30);draw.setRequestId(req.getRequestId());draw.setOutBillNo("L7");
        draw.setSnapshot(snapshot);draw.setDrawStatus("ACCEPTED");draw.setGrantStatus("PENDING");draw.setNewlyAccepted(true);
        when(cache.prizes(cfg,30L)).thenReturn(List.of(prize));when(legacy.prepareV2Draw(eq(req),eq(cfg),anyList())).thenReturn(snapshot);
        when(stock.reserve(10L,snapshot)).thenReturn(reservation);when(ledger.accept(10L,snapshot)).thenReturn(draw);when(ledger.get(7)).thenReturn(draw);
        when(ledger.findByReference(10L,20L,40L,"request_123")).thenReturn(draw);
    }
    @AfterEach void clear(){BusinessContextHolder.clear();}
    private void ready() {
        doAnswer(call->{draw.setGrantStatus("WAIT_USER_CONFIRM");draw.setOutBillNo("activity21Awechatbill");var result=new LotteryUserLogVO();result.setPrizeType(5);result.setLotteryPrizeId(100L);result.setPackageInfo("test-package");result.setOutBillNo(draw.getOutBillNo());draw.setResult(result);return draw;}).when(grants).prepareCash(7);
    }
    @Test void lotteryReturnsReadyCashParametersAfterReleasingTheInventoryLock() {
        ready();var response=service.drawAfterResultMiss(req,cfg);
        assertEquals("test-package",response.getPackageInfo());assertEquals("activity21Awechatbill",response.getOutBillNo());assertEquals("WAIT_USER_CONFIRM",response.getGrantStatus());
        var order=inOrder(reservation,grants);order.verify(reservation).commit();order.verify(reservation).close();order.verify(grants).prepareCash(7);
        verify(ledger,times(1)).accept(anyLong(),any());
        verify(ledger,never()).get(anyLong());
    }
    @Test void retryOnlyPreparesTheOriginalBillWithoutAcceptingAnotherDraw() {
        ready();var response=service.prepareCashResult(req);assertEquals("test-package",response.getPackageInfo());assertEquals("activity21Awechatbill",response.getOutBillNo());
        verify(ledger,never()).accept(anyLong(),any());verify(grants).prepareCash(7);
    }
    @Test void unavailableCashParametersAreNotReturnedAsClaimableSuccess() {
        var error=assertThrows(ServiceException.class,()->service.drawAfterResultMiss(req,cfg));assertEquals(ErrorCodeConstants.LOTTERY_CASH_PREPARING.getCode(),error.getCode());
        verify(ledger,times(1)).accept(anyLong(),any());verify(ledger,never()).find(anyLong(),anyLong(),anyLong(),anyString());verify(reservation).close();
    }
    @Test void resultRemainsReadOnlyAndNeverTriggersCashSending() {
        var response=service.query(req);assertEquals("PENDING",response.getGrantStatus());assertNull(response.getPackageInfo());assertNull(response.getOutBillNo());verifyNoInteractions(grants);verify(ledger,never()).accept(anyLong(),any());
    }
    @Test void aDefinitivelyFailedCashRequestIsNotSentAgain() {
        draw.setDrawStatus("FAILED");draw.setGrantStatus("FAILED");var error=assertThrows(ServiceException.class,()->service.prepareCashResult(req));
        assertEquals(ErrorCodeConstants.LOTTERY_CASH_FAILED.getCode(),error.getCode());verifyNoInteractions(grants);verify(ledger,never()).accept(anyLong(),any());
    }
}
