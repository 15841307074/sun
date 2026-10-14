package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import lombok.extern.slf4j.Slf4j;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/** 微信调用不占用数据库事务，复用客户端和密钥。 */
@Service("lotteryCashGateway")
@Slf4j
public class LotteryCashGatewayImpl implements LotteryCashGateway {
    @Resource
    private LotteryWeChatPayConfig config;
    @Resource private LotteryRedPacketService redPackets;
    private volatile TransferToUser client;
    private volatile List<String> clientConfig;
    /** 服务就绪时加载密钥和客户端，避免首个红包承担本地初始化耗时；不调用微信、不发款。 */
    @EventListener(ApplicationReadyEvent.class)
    public void warmClient() {
        try {client();}
        catch(RuntimeException e){log.info("Lottery cash client initialization deferred: {}",e.getClass().getSimpleName());}
    }
    private TransferToUser client() {
        List<String> current=Arrays.asList(config.getMchId(),config.getCertSerialNo(),config.getPrivateKeyPath(),config.getWechatPublicKeyId(),config.getWechatPublicKeyPath());
        if(client==null||!current.equals(clientConfig)) synchronized(this) {
            if(client==null||!current.equals(clientConfig)) {
                client=new TransferToUser(current.get(0),current.get(1),current.get(2),current.get(3),current.get(4));clientConfig=current;
            }
        }
        return client;
    }
    @Override
    @DS(DsNameConstants.SHARDING)
    public TransferToUser.TransferToUserResponse reconcileOrSend(LotteryLedger.Draw draw) {
        // 沿用原抽奖红包发送方法；传入原商户单号以保证重试仍指向同一笔转账。
        return redPackets.transferUser(buildRedPacketVo(draw));
    }
    private LotteryRedPacketVo buildRedPacketVo(LotteryLedger.Draw draw) {
        var snapshot=draw.getSnapshot();
        LotteryRedPacketVo vo=new LotteryRedPacketVo();
        vo.setActivityId(draw.getActivityId());vo.setOutBillNo(draw.cashBillNo());vo.setOpenId(snapshot.getMember().getOpenid());
        vo.setTransferAmount(snapshot.getPrize().getPrizeValue().movePointRight(2).setScale(0,RoundingMode.HALF_UP).intValueExact());
        vo.setTransferRemark("抽奖活动红包");
        LotteryTransferSceneReportDO activity=new LotteryTransferSceneReportDO();activity.setInfoType("活动名称");
        String title=snapshot.getSettings().getLotteryTitle();activity.setInfoContent(title==null||title.isBlank()?"抽奖活动":title);
        LotteryTransferSceneReportDO reason=new LotteryTransferSceneReportDO();reason.setInfoType("奖励说明");reason.setInfoContent("参与活动抽奖获得奖励");
        vo.setSceneReports(List.of(activity,reason));
        return vo;
    }
    /** 人工补发前只查单，不直接发起支付；未知状态不能换新支付单号。 */
    @Override
    public TransferToUser.TransferToUserResponse queryForReissue(LotteryLedger.Draw draw) {
        try { return client().query(draw.cashBillNo()); }
        catch(WXPayUtility.ApiException e) {
            if(e.getStatusCode()==404&&"NOT_FOUND".equals(e.getErrorCode()))return null;
            throw e;
        }
    }
    @Override
    public boolean verify(HttpServletRequest req,String body) {
        return client().verifyCallback(req.getHeader("Wechatpay-Timestamp"),req.getHeader("Wechatpay-Nonce"),req.getHeader("Wechatpay-Signature"),req.getHeader("Wechatpay-Serial"),body);
    }
}
