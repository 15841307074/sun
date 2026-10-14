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
    @Test void fullLegacyVoIncludesActivityAndKeepsTheSameBillOnRetries() {
        var config=new LotteryWeChatPayConfig();var api=mock(TransferToUser.class);var redPackets=mock(LotteryRedPacketService.class);
        var gateway=new LotteryCashGatewayImpl();ReflectionTestUtils.setField(gateway,"config",config);
        ReflectionTestUtils.setField(gateway,"client",api);ReflectionTestUtils.setField(gateway,"clientConfig",Arrays.asList(null,null,null,null,null));
        ReflectionTestUtils.setField(gateway,"redPackets",redPackets);
        var draw=new LotteryLedger.Draw();draw.setActivityId(21);draw.setOutBillNo("L2100000000000000001");
        var snapshot=new LotteryDrawSnapshot();var settings=new LotterySettingsCacheDataVO();settings.setLotteryTitle("红包活动");snapshot.setSettings(settings);
        var member=new WxMemberVO();member.setOpenid("test-openid");snapshot.setMember(member);
        var prize=new LotteryPrizeDO();prize.setPrizeValue(new BigDecimal("12.345"));snapshot.setPrize(prize);draw.setSnapshot(snapshot);
        var response=new TransferToUser.TransferToUserResponse();response.setState(TransferToUser.TransferBillStatus.WAIT_USER_CONFIRM);response.setPackageInfo("test-package");
        when(redPackets.transferUser(any(LotteryRedPacketVo.class))).thenReturn(response);
        assertSame(response,gateway.reconcileOrSend(draw));assertSame(response,gateway.reconcileOrSend(draw));
        var captured=ArgumentCaptor.forClass(LotteryRedPacketVo.class);verify(redPackets,times(2)).transferUser(captured.capture());
        for(var vo:captured.getAllValues()) {
            assertEquals(21L,vo.getActivityId());assertEquals(draw.cashBillNo(),vo.getOutBillNo());assertEquals("test-openid",vo.getOpenId());
            assertEquals(1235,vo.getTransferAmount());assertEquals("抽奖活动红包",vo.getTransferRemark());
            assertEquals(2,vo.getSceneReports().size());assertEquals("红包活动",vo.getSceneReports().get(0).getInfoContent());
        }
        verifyNoInteractions(api);
    }
}
