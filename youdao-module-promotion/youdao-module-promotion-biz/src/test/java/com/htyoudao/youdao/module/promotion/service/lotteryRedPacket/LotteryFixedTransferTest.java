package com.htyoudao.youdao.module.promotion.service.lotteryRedPacket;

import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import com.htyoudao.youdao.module.promotion.enums.LotteryTransferStatus;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import okhttp3.Headers;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryFixedTransferTest {
    private LotteryRedPacketService service,self;
    private TransferToUser api;
    private LotteryRedPacketVo vo;
    private LotteryTransferRecordDO record;
    @BeforeEach void setup() {
        service=new LotteryRedPacketService();self=mock(LotteryRedPacketService.class);api=mock(TransferToUser.class);
        var config=new LotteryWeChatPayConfig();config.setAppId("test-app");config.setTransferSceneId("1000");config.setNotifyUrl("https://example.test/notify");
        ReflectionTestUtils.setField(service,"weChatPayConfig",config);ReflectionTestUtils.setField(service,"self",self);
        vo=new LotteryRedPacketVo();vo.setActivityId(21L);vo.setOutBillNo("L2100000000000000001");vo.setOpenId("test-openid");vo.setTransferAmount(100);vo.setTransferRemark("抽奖活动红包");
        var info=new LotteryTransferSceneReportDO();info.setInfoType("活动名称");info.setInfoContent("红包活动");vo.setSceneReports(List.of(info));
        record=new LotteryTransferRecordDO();record.setOutBillNo(vo.getOutBillNo());record.setStatus(LotteryTransferStatus.PROCESSING);
        when(self.prepareAndClaimFixedTransfer(vo)).thenReturn(new LotteryRedPacketService.FixedTransferPreparation(record,false));
    }
    @Test void notFoundSendsFullRequestAfterSavingTheRecord() {
        when(api.query(vo.getOutBillNo())).thenThrow(error(404,"NOT_FOUND"));
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);response.setPackageInfo("test-package");
        when(api.run(any())).thenReturn(response);
        assertSame(response,service.transferUser(vo,api));assertEquals(vo.getOutBillNo(),response.getOutBillNo());assertEquals(200,response.getCode());
        var order=inOrder(self,api);order.verify(self).prepareAndClaimFixedTransfer(vo);order.verify(api).query(vo.getOutBillNo());
        var captured=ArgumentCaptor.forClass(TransferToUser.TransferToUserRequest.class);order.verify(api).run(captured.capture());order.verify(self).saveFixedTransferResult(record,response);
        var request=captured.getValue();assertEquals("test-app",request.appid);assertEquals("1000",request.transferSceneId);assertEquals("https://example.test/notify",request.notifyUrl);
        assertEquals(vo.getOutBillNo(),request.outBillNo);assertEquals(vo.getOpenId(),request.openid);assertEquals(100,request.transferAmount);assertEquals("红包活动",request.transferSceneReportInfos.get(0).infoContent);
    }
    @Test void firstRequestPostsWithoutWaitingForWechatQuery() {
        record.setStatus(LotteryTransferStatus.INIT);
        when(self.prepareAndClaimFixedTransfer(vo)).thenReturn(new LotteryRedPacketService.FixedTransferPreparation(record,true));
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);
        response.setPackageInfo("claim-package");when(api.run(any())).thenReturn(response);
        assertSame(response,service.transferUser(vo,api));
        verify(api,never()).query(anyString());verify(api).run(any());
        verify(self).saveFixedTransferResult(record,response);
    }
    @Test void anotherRequestCannotPostWhileFirstRequestIsInFlight() {
        record.setStatus(LotteryTransferStatus.INIT);
        assertEquals(TransferToUser.TransferBillStatus.PROCESSING,service.transferUser(vo,api).getState());
        verify(self).prepareAndClaimFixedTransfer(vo);verifyNoInteractions(api);
    }
    @Test void preparedClaimParametersAreReusedWithoutWechatCall() {
        record.setStatus(LotteryTransferStatus.WAIT_USER_CONFIRM);record.setPackageInfo("claim-package");
        var response=service.transferUser(vo,api);
        assertEquals("claim-package",response.getPackageInfo());
        assertEquals(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM,response.getState());
        verifyNoInteractions(api);
    }
    @Test void notFoundDuringRecentInFlightPostDoesNotSendAgain() {
        record.setUpdateTime(LocalDateTime.now());
        when(api.query(vo.getOutBillNo())).thenThrow(error(404,"NOT_FOUND"));
        assertEquals(TransferToUser.TransferBillStatus.PROCESSING,service.transferUser(vo,api).getState());
        verify(api,never()).run(any());
    }
    @Test void expiredInFlightPostCanRecoverWithTheSameBill() {
        record.setUpdateTime(LocalDateTime.now().minusMinutes(1));
        when(api.query(vo.getOutBillNo())).thenThrow(error(404,"NOT_FOUND"));
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);
        response.setPackageInfo("claim-package");when(api.run(any())).thenReturn(response);
        assertSame(response,service.transferUser(vo,api));
        var request=ArgumentCaptor.forClass(TransferToUser.TransferToUserRequest.class);
        verify(api).run(request.capture());assertEquals(vo.getOutBillNo(),request.getValue().outBillNo);
    }
    @Test void existingWechatBillIsQueriedWithoutResending() {
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.SUCCESS);when(api.query(vo.getOutBillNo())).thenReturn(response);
        service.transferUser(vo,api);verify(api,never()).run(any());verify(self).saveFixedTransferResult(record,response);
    }
    @Test void locallyConfirmedSuccessNeverQueriesOrCreatesAnotherPayment() {
        record.setStatus(com.htyoudao.youdao.module.promotion.enums.LotteryTransferStatus.SUCCESS);
        var response=service.transferUser(vo,api);assertEquals(TransferToUser.TransferBillStatus.SUCCESS,response.getState());
        assertEquals(vo.getOutBillNo(),response.getOutBillNo());verifyNoInteractions(api);
    }
    @Test void unknownApiFailureNeverCreatesAnotherPayment() {
        var failure=error(500,"SYSTEM_ERROR");when(api.query(vo.getOutBillNo())).thenThrow(failure);
        assertSame(failure,assertThrows(WXPayUtility.ApiException.class,()->service.transferUser(vo,api)));
        verify(api,never()).run(any());verify(self).saveFixedTransferFailure(record,"test-error");verify(self,never()).saveFixedTransferResult(any(),any());
        assertEquals("L2100000000000000001",vo.getOutBillNo());
    }
    @Test void recordAndFirstSendClaimCommitTogetherBeforeCallingWechat() {
        record.setStatus(LotteryTransferStatus.INIT);
        var inner=spy(new LotteryRedPacketService());
        doAnswer(call->{assertTrue(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());return record;}).when(inner).prepareFixedTransferRecord(vo);
        doAnswer(call->{assertTrue(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());return true;}).when(inner).claimFirstSend(record);
        doNothing().when(inner).saveFixedTransferResult(eq(record),any());
        var source=new org.springframework.jdbc.datasource.DriverManagerDataSource("jdbc:h2:mem:transfer"+java.util.UUID.randomUUID(),"sa","");
        var manager=spy(new org.springframework.jdbc.datasource.DataSourceTransactionManager(source));
        var factory=new org.springframework.aop.framework.ProxyFactory(inner);
        var advice=new org.springframework.transaction.interceptor.TransactionInterceptor();
        advice.setTransactionManager(manager);advice.setTransactionAttributeSource(new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource());
        factory.addAdvice(advice);
        ReflectionTestUtils.setField(service,"self",factory.getProxy());
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);response.setPackageInfo("test-package");
        when(api.run(any())).thenAnswer(call->{
            assertFalse(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
            verify(manager,times(1)).commit(any());return response;
        });
        assertSame(response,service.transferUser(vo,api));verify(api,never()).query(anyString());verify(manager,times(2)).commit(any());
    }
    private WXPayUtility.ApiException error(int status,String code) {return new WXPayUtility.ApiException(status,"{\"code\":\""+code+"\",\"message\":\"test-error\"}",new Headers.Builder().build());}
}
