package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/** 微信调用不占用数据库事务，复用客户端和密钥。 */
@Service("lotteryCashGateway")
public class LotteryCashGatewayImpl implements LotteryCashGateway {
    @Resource
    private LotteryWeChatPayConfig config;
    private volatile TransferToUser client;
    private volatile List<String> clientConfig;
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
    public TransferToUser.TransferToUserResponse reconcileOrSend(LotteryLedger.Draw draw) {
        TransferToUser api=client();
        try{return api.query(draw.cashBillNo());}
        catch(WXPayUtility.ApiException e){if(e.getStatusCode()!=404||!"NOT_FOUND".equals(e.getErrorCode()))throw e;}
        var snapshot=draw.getSnapshot();
        TransferToUser.TransferToUserRequest req=new TransferToUser.TransferToUserRequest();
        req.appid=config.getAppId();req.outBillNo=draw.cashBillNo();req.openid=snapshot.getMember().getOpenid();
        req.transferSceneId=config.getTransferSceneId();req.notifyUrl=config.getNotifyUrl();
        req.transferAmount=snapshot.getPrize().getPrizeValue().movePointRight(2).setScale(0,RoundingMode.HALF_UP).intValueExact();
        req.transferRemark="活动抽奖奖励";
        TransferToUser.TransferSceneReportInfo activity=new TransferToUser.TransferSceneReportInfo();activity.infoType="活动名称";activity.infoContent=snapshot.getSettings().getLotteryTitle();
        TransferToUser.TransferSceneReportInfo reason=new TransferToUser.TransferSceneReportInfo();reason.infoType="奖励说明";reason.infoContent="参与活动抽奖获得奖励";
        req.transferSceneReportInfos=List.of(activity,reason);
        return api.run(req);
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
