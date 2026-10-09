package com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw;

import com.google.gson.annotations.SerializedName;
import com.htyoudao.youdao.module.errand.framework.config.WechatPayConfig;
import com.htyoudao.youdao.module.errand.util.WXPayUtility;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 微信转账服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatTransferServiceImpl implements WechatTransferService {

    private final WechatPayConfig wechatPayConfig;

    @Override
    public TransferToUser.TransferToUserResponse transferToUser(String outBillNo, String openId, Integer amount, String remark) throws Exception {
        log.info("开始微信转账: outBillNo={}, openId={}, amount={}分", outBillNo, openId, amount);

        // 创建转账客户端
        TransferToUser client = new TransferToUser(
                wechatPayConfig.getMchId(),
                wechatPayConfig.getCertSerialNo(),
                wechatPayConfig.getPrivateKeyPath(),
                wechatPayConfig.getWechatPublicKeyId(),
                wechatPayConfig.getWechatPublicKeyPath()
        );

        // 构建请求
        TransferToUser.TransferToUserRequest request = new TransferToUser.TransferToUserRequest();
        request.appid = wechatPayConfig.getAppId();
        request.outBillNo = outBillNo;
        request.transferSceneId = wechatPayConfig.getTransferSceneId();
        request.openid = openId;

        // 用户姓名需要加密（如果传入）
//        if (userName != null && !userName.isEmpty()) {
//            request.userName = client.encrypt(userName);
//        }

        request.transferAmount = amount;
        request.transferRemark = remark;
        request.notifyUrl = wechatPayConfig.getNotifyUrl();
//        request.userRecvPerception = "提现奖励";

        // 设置场景报告信息（用于提现场景说明）
        request.transferSceneReportInfos = new ArrayList<>();
        {
            TransferToUser.TransferSceneReportInfo transferSceneReportInfosItem0 = new TransferToUser.TransferSceneReportInfo();
            transferSceneReportInfosItem0.infoType = "活动名称";
            transferSceneReportInfosItem0.infoContent = "新会员有礼";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem0);
            TransferToUser.TransferSceneReportInfo transferSceneReportInfosItem1 = new TransferToUser.TransferSceneReportInfo();
            transferSceneReportInfosItem1.infoType = "奖励说明";
            transferSceneReportInfosItem1.infoContent = "注册会员抽奖一等奖";
            request.transferSceneReportInfos.add(transferSceneReportInfosItem1);
        };

        // 发送转账请求
        TransferToUser.TransferToUserResponse response = client.run(request);
        log.info("微信转账响应: outBillNo={}, transferBillNo={}, state={}",
                response.outBillNo, response.transferBillNo, response.state);
        return response;


    }




}
