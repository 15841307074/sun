package com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw;

/**
 * 微信转账服务接口
 */
public interface WechatTransferService {

    /**
     * 转账到用户零钱
     *
     * @param outBillNo 商户单号
     * @param openId 用户OpenID
     * @param amount 转账金额（分）
     * @param remark 转账备注
     * @throws Exception 转账异常
     */
    TransferToUser.TransferToUserResponse transferToUser(String outBillNo, String openId, Integer amount, String remark) throws Exception;
}
