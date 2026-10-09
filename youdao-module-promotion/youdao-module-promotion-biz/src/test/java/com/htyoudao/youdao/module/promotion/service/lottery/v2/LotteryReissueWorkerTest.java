package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryReissueWorkerTest {
    private LotteryLedger ledger;
    private WxMemberApi members;
    private LotteryCashGateway cash;
    private LotteryGrantWorker worker;
    private LotteryLedger.Draw draw;
    @BeforeEach void setup() {
        ledger=mock(LotteryLedger.class);members=mock(WxMemberApi.class);cash=mock(LotteryCashGateway.class);
        worker=new LotteryGrantWorker();
        ReflectionTestUtils.setField(worker,"ledger",ledger);ReflectionTestUtils.setField(worker,"members",members);
        ReflectionTestUtils.setField(worker,"cash",cash);ReflectionTestUtils.setField(worker,"storage",mock(LotteryGrantStorage.class));
        ReflectionTestUtils.setField(worker,"stockCache",mock(LotteryStockCache.class));
        var service=mock(LotteryV2Service.class);ReflectionTestUtils.setField(worker,"service",service);
        draw=new LotteryLedger.Draw();draw.setId(100);draw.setBusinessId(10);draw.setMemberId(40);draw.setPointsCost(50);
        draw.setOutBillNo("L100");draw.setDrawStatus("ACCEPTED");draw.setStockState("RESERVED");
        var snapshot=new LotteryDrawSnapshot();snapshot.setManualReissue(true);
        var prize=new LotteryPrizeDO();prize.setId(101L);prize.setPrizeType(2);prize.setPrizeValue(BigDecimal.valueOf(100));snapshot.setPrize(prize);draw.setSnapshot(snapshot);
        when(ledger.claim(eq(100L),anyString())).thenReturn(true);when(ledger.get(100)).thenReturn(draw);
        when(service.result(draw)).thenReturn(new com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO());
        doAnswer(call->{if(call.getArgument(3,Boolean.class)){draw.setDrawStatus(call.getArgument(4,Boolean.class)?"COMPLETED":"FAILED");draw.setStockState(call.getArgument(4,Boolean.class)?"ISSUED":"RELEASED");}return null;})
                .when(ledger).grantState(eq(100L),anyString(),any(),anyBoolean(),anyBoolean());
    }
    @Test void manualPointsAwardDoesNotDebitOriginalCost() {
        when(members.changeLotteryPoints(40L,"L100:AWARD",100)).thenReturn(true);
        ReflectionTestUtils.invokeMethod(worker,"run",100L);
        verify(members).changeLotteryPoints(40L,"L100:AWARD",100);
        verify(members,never()).changeLotteryPoints(anyLong(),eq("L100:DEBIT"),anyInt());
        verify(members,never()).refundLotteryPoints(anyLong(),anyString());
        verify(ledger).finishJob(eq(100L),anyString());
        assertEquals("COMPLETED",draw.getDrawStatus());
    }
    @Test void manualCashFailureDoesNotFallbackOrRefundAgain() {
        draw.getSnapshot().getPrize().setPrizeType(5);
        var response=new TransferToUser.TransferToUserResponse();response.state=TransferToUser.TransferBillStatus.FAIL;
        when(cash.reconcileOrSend(draw)).thenReturn(response);
        ReflectionTestUtils.invokeMethod(worker,"run",100L);
        verify(ledger).grantState(eq(100L),eq("FAILED"),any(),eq(true),eq(false));
        verify(ledger,never()).fallback(anyLong());verify(ledger,never()).rejectUnpaid(anyLong());
        verifyNoInteractions(members);
        assertEquals(101L,draw.getSnapshot().getPrize().getId());
        assertEquals("FAILED",draw.getDrawStatus());
    }
}
