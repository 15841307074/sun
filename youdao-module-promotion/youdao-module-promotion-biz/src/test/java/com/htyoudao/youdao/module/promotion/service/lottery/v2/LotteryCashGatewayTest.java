package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.LotteryCashGatewayImpl;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LotteryCashGatewayTest {
    @Test void legacyServiceGeneratesTheFirstBillAndRetriesUseItsReturnedBill() {
        var config=new LotteryWeChatPayConfig();var api=mock(TransferToUser.class);var redPackets=mock(LotteryRedPacketService.class);
        var ledger=mock(LotteryLedger.class);
        var gateway=new LotteryCashGatewayImpl();ReflectionTestUtils.setField(gateway,"config",config);
        ReflectionTestUtils.setField(gateway,"client",api);ReflectionTestUtils.setField(gateway,"clientConfig",Arrays.asList(null,null,null,null,null));
        ReflectionTestUtils.setField(gateway,"redPackets",redPackets);
        ReflectionTestUtils.setField(gateway,"ledger",ledger);
        var draw=new LotteryLedger.Draw();draw.setId(7);draw.setActivityId(21);draw.setOutBillNo("L7");
        when(ledger.firstCashAttempt(7)).thenReturn(true);
        var snapshot=new LotteryDrawSnapshot();var settings=new LotterySettingsCacheDataVO();settings.setLotteryTitle("红包活动");snapshot.setSettings(settings);
        var member=new WxMemberVO();member.setOpenid("test-openid");snapshot.setMember(member);
        var prize=new LotteryPrizeDO();prize.setPrizeValue(new BigDecimal("12.345"));snapshot.setPrize(prize);draw.setSnapshot(snapshot);
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);response.setPackageInfo("test-package");response.setOutBillNo("activity21Awechatbill");
        when(redPackets.transferUser(any(LotteryRedPacketVo.class))).thenReturn(response);
        assertSame(response,gateway.reconcileOrSend(draw));
        draw.setOutBillNo(response.getOutBillNo());
        assertSame(response,gateway.reconcileOrSend(draw));
        var captured=ArgumentCaptor.forClass(LotteryRedPacketVo.class);verify(redPackets,times(2)).transferUser(captured.capture());
        assertNull(captured.getAllValues().get(0).getOutBillNo());
        assertEquals(response.getOutBillNo(),captured.getAllValues().get(1).getOutBillNo());
        for(var vo:captured.getAllValues()) {
            assertEquals(21L,vo.getActivityId());assertEquals("test-openid",vo.getOpenId());
            assertEquals(1235,vo.getTransferAmount());assertEquals("抽奖活动红包",vo.getTransferRemark());
            assertTrue(vo.getSceneReports().isEmpty());
        }
        verifyNoInteractions(api);
    }
}
