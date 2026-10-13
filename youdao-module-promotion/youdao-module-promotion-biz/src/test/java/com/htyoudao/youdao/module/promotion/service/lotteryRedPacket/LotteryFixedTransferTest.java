package com.htyoudao.youdao.module.promotion.service.lotteryRedPacket;

import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferRecordDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import okhttp3.Headers;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
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
        record=new LotteryTransferRecordDO();record.setOutBillNo(vo.getOutBillNo());when(self.prepareFixedTransferRecord(vo)).thenReturn(record);
    }
    @Test void notFoundSendsFullRequestAfterSavingTheRecord() {
        when(api.query(vo.getOutBillNo())).thenThrow(error(404,"NOT_FOUND"));
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);response.setPackageInfo("test-package");
        when(api.run(any())).thenReturn(response);
        assertSame(response,service.transferUser(vo,api));assertEquals(vo.getOutBillNo(),response.getOutBillNo());assertEquals(200,response.getCode());
        var order=inOrder(self,api);order.verify(self).prepareFixedTransferRecord(vo);order.verify(api).query(vo.getOutBillNo());
        var captured=ArgumentCaptor.forClass(TransferToUser.TransferToUserRequest.class);order.verify(api).run(captured.capture());order.verify(self).saveFixedTransferResult(record,response);
        var request=captured.getValue();assertEquals("test-app",request.appid);assertEquals("1000",request.transferSceneId);assertEquals("https://example.test/notify",request.notifyUrl);
        assertEquals(vo.getOutBillNo(),request.outBillNo);assertEquals(vo.getOpenId(),request.openid);assertEquals(100,request.transferAmount);assertEquals("红包活动",request.transferSceneReportInfos.get(0).infoContent);
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
    private WXPayUtility.ApiException error(int status,String code) {return new WXPayUtility.ApiException(status,"{\"code\":\""+code+"\",\"message\":\"test-error\"}",new Headers.Builder().build());}
}
