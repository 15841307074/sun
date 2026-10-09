package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryReissueServiceImpl;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.wxmember.WxMemberApi;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryReissueReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.LotteryController;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryReissueServiceTest {
    @ParameterizedTest @ValueSource(strings={"PROCESSING","SUCCESS","FAIL","CANCELLED"})
    void onlyDefinitelyFailedCashMayUseANewBill(String state) {
        var ledger=mock(LotteryLedger.class);var cash=mock(LotteryCashGateway.class);var members=mock(WxMemberApi.class);
        var service=new LotteryReissueServiceImpl();var resultService=mock(LotteryV2Service.class);var cache=mock(LotteryResultCache.class);
        ReflectionTestUtils.setField(service,"ledger",ledger);ReflectionTestUtils.setField(service,"cash",cash);
        ReflectionTestUtils.setField(service,"members",members);ReflectionTestUtils.setField(service,"results",resultService);ReflectionTestUtils.setField(service,"cache",cache);
        var draw=new LotteryLedger.Draw();draw.setId(100);draw.setStoreId(30);draw.setMemberId(40);draw.setDrawStatus("FAILED");draw.setPointsCost(50);draw.setOutBillNo("L100");
        var snapshot=new LotteryDrawSnapshot();var prize=new LotteryPrizeDO();prize.setPrizeType(5);snapshot.setPrize(prize);draw.setSnapshot(snapshot);
        var req=new LotteryReissueReqVO();req.setLotteryId(20L);req.setMemberId(40L);req.setStoreId(30L);req.setRequestId("request_123");req.setReason("人工核对后补发");
        when(ledger.findByReference(10,20,40,"request_123")).thenReturn(draw);
        var remote=new TransferToUser.TransferToUserResponse();remote.state=TransferToUser.TransferBillStatus.valueOf(state);when(cash.queryForReissue(draw)).thenReturn(remote);
        boolean failed=state.equals("FAIL")||state.equals("CANCELLED");
        when(ledger.reissue(10,100,40,30,"request_123","L100",0,failed)).thenReturn(new LotteryLedger.Reissue(draw,true,"已安排"));
        Long previous=BusinessContextHolder.getBusinessId();
        try {
            BusinessContextHolder.setBusinessId(10L);
            assertTrue(service.reissue(req).isQueued());
            verify(ledger).reissue(10,100,40,30,"request_123","L100",0,failed);
            verify(members).refundLotteryPoints(40L,"L100"); // 补齐原失败退款，不是重新扣费。
            verify(members,never()).changeLotteryPoints(anyLong(),anyString(),anyInt());
            verify(cash,never()).reconcileOrSend(any()); // 人工接口只查单，支付仍在任务中执行。
        } finally {BusinessContextHolder.setBusinessId(previous);}
    }
    @Test void manualEndpointRequiresDedicatedPermission() throws Exception {
        var method=LotteryController.class.getMethod("reissue",LotteryReissueReqVO.class);
        assertEquals("@ss.hasPermission('promotion:lottery:reissue')",method.getAnnotation(PreAuthorize.class).value());
        assertNull(method.getAnnotation(jakarta.annotation.security.PermitAll.class));
    }
    @Test void unavailableResultCachePreventsScheduling() {
        var ledger=mock(LotteryLedger.class);var cache=mock(LotteryResultCache.class);
        var service=new LotteryReissueServiceImpl();ReflectionTestUtils.setField(service,"ledger",ledger);ReflectionTestUtils.setField(service,"cache",cache);
        var draw=new LotteryLedger.Draw();draw.setId(100);draw.setStoreId(30);draw.setDrawStatus("ACCEPTED");draw.setOutBillNo("L100");
        var snapshot=new LotteryDrawSnapshot();var prize=new LotteryPrizeDO();prize.setPrizeType(2);snapshot.setPrize(prize);draw.setSnapshot(snapshot);
        var req=new LotteryReissueReqVO();req.setLotteryId(20L);req.setMemberId(40L);req.setStoreId(30L);req.setRequestId("request_123");
        when(ledger.findByReference(10,20,40,"request_123")).thenReturn(draw);
        doThrow(new org.springframework.data.redis.RedisConnectionFailureException("unavailable"))
                .when(cache).invalidateForReissue(draw,1);
        Long previous=BusinessContextHolder.getBusinessId();
        try {
            BusinessContextHolder.setBusinessId(10L);
            assertThrows(org.springframework.data.redis.RedisConnectionFailureException.class,()->service.reissue(req));
            verify(ledger,never()).reissue(anyLong(),anyLong(),anyLong(),anyLong(),anyString(),anyString(),anyLong(),anyBoolean());
        } finally {BusinessContextHolder.setBusinessId(previous);}
    }
}
