package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryRedPacketVo;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.TransferNotify;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.TransferNotifyData;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.LotteryWeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.SignatureDebugger;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.WeChatPayConfig;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.req.RedPacketRequest;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result.RedPacketResult;

import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result.TransferRedPacketResult;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.service.WeChatSignService;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferRecordMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.lottery.LotteryTransferSceneReportMapper;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryLogService;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryAdmission;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryCashGateway;
import com.htyoudao.youdao.module.promotion.service.lottery.v2.LotteryLedger;
import com.htyoudao.youdao.module.promotion.service.lotteryRedPacket.LotteryRedPacketService;
import com.htyoudao.youdao.module.promotion.util.WechatRedEnvelope.WXPayUtility;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.XmlUtil.xmlToMap;

@Tag(name = "中奖红包")
@RestController
@RequestMapping("/promotion/redPacket")
@Slf4j
@Validated
public class RedPacketController {
    @Resource
    private LotteryAdmission lotteryAdmission;
    @Autowired
    private LotteryCashGateway lotteryCashGateway;
    @Autowired
    private LotteryLedger lotteryLedger;

    @Autowired
    private LotteryRedPacketService lotteryRedPacketService;

    @Autowired
    private LotteryWeChatPayConfig weChatPayConfig;

    @Autowired
    private LotteryLogService lotteryLogService;


    /**
     * 发送红包接口
     */

//    @PermitAll
    @PostMapping("/send")
    public TransferToUser.TransferToUserResponse sendRedPacket(@Valid @RequestBody LotteryRedPacketVo lotteryRedPacketVo) {
        TransferToUser.TransferToUserResponse respon = lotteryRedPacketService.transferUser(lotteryRedPacketVo);
        return respon;

    }



    /**
     * 转账回调通知
     */
    @PermitAll
    @DataPermission(enable = false)
    @PostMapping("/notify")
    public ResponseEntity<String> handleTransferNotify(
            HttpServletRequest request,
            @RequestBody String notifyBody) {

        log.debug("收到转账回调通知");

        try (var permit = lotteryAdmission.enter("callback", 0, 0, null)) {
            if (!lotteryCashGateway.verify(request, notifyBody)) {
                return ResponseEntity.badRequest().body("{\"code\":\"FAIL\",\"message\":\"签名验证失败\"}");
            }

            // 2. 解析回调数据
            TransferNotify transferNotify = lotteryRedPacketService.parseNotifyData(notifyBody);

            // 3. 处理回调
            String outBillNo = transferNotify.getOut_bill_no();
            // 旧活动商户单号继续直接走原回调逻辑，只有新版单号才查询新版流水。
            boolean v2Bill = outBillNo != null && outBillNo.matches("L[0-9]{15,20}F?(?:R[1-9][0-9]*)?");
            boolean success = (v2Bill && lotteryLedger.wakeByBill(outBillNo))
                    || lotteryLogService.processTransferNotify(transferNotify);

            if (success) {
                return ResponseEntity.ok("{\"code\": \"SUCCESS\", \"message\": \"成功\"}");
            } else {
                return ResponseEntity.status(500).body("{\"code\": \"FAIL\", \"message\": \"处理失败\"}");
            }

        } catch (Exception e) {
            log.error("处理转账回调异常: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"code\": \"FAIL\", \"message\": \"处理异常\"}");
        }
    }

    /**
     * 验证回调签名
     */
    private boolean verifySignature(HttpServletRequest request, String body) {
        try {
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String signature = request.getHeader("Wechatpay-Signature");
            String serialNo = request.getHeader("Wechatpay-Serial");

            // 构建签名字符串
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";

            // 使用微信支付公钥验证签名
            PublicKey publicKey = WXPayUtility.loadPublicKeyFromPath(weChatPayConfig.getWechatPublicKeyPath());

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return verifier.verify(signatureBytes);

        } catch (Exception e) {
            log.error("验证签名异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 简化签名验证（用于快速调试）
     */
    private boolean verifySignatureSimple(HttpServletRequest request, String body) {
        try {
            // 获取签名头
            String timestamp = request.getHeader("Wechatpay-Timestamp");
            String nonce = request.getHeader("Wechatpay-Nonce");
            String signature = request.getHeader("Wechatpay-Signature");
            String serialNo = request.getHeader("Wechatpay-Serial");

            log.info("=== 签名调试信息 ===");
            log.info("timestamp: {}", timestamp);
            log.info("nonce: {}", nonce);
            log.info("serialNo: {}", serialNo);
            log.info("signature: {}", signature);
            log.info("body length: {}", body.length());

            // 构建签名字符串
            String message = timestamp + "\n" + nonce + "\n" + body + "\n";
            log.info("message: {}", message);

            // 加载公钥
            PublicKey publicKey = WXPayUtility.loadPublicKeyFromPath(weChatPayConfig.getWechatPublicKeyPath());
            log.info("公钥加载: {}", publicKey != null ? "成功" : "失败");

            // 验证签名
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            boolean isValid = verifier.verify(signatureBytes);

            log.info("签名验证结果: {}", isValid);
            return isValid;

        } catch (Exception e) {
            e.printStackTrace();
            log.error("签名验证详细异常:", e);
            return false;
        }
    }

    /**
     * 领取红包
     */
    @PostMapping("/claimRedEnvelope")
    public CommonResult<LotteryLogDO> claimRedEnvelope(@RequestParam("lotteryId") Long lotteryId) {
        LotteryLogDO lotteryLogDO = lotteryLogService.selectLotteryId(lotteryId);
        return CommonResult.success(lotteryLogDO);
    }

    /**
     * 转账回调通知
     */
    @PermitAll
    @DataPermission(enable = false)
    @PostMapping("/notifyTest")
    public ResponseEntity<String> handleTransferNotifyTest(
            HttpServletRequest request,
            @RequestParam("notifyBody") String notifyBody) {

        log.debug("收到转账回调通知");
        TransferNotify transferNotify = new TransferNotify();
        transferNotify.setOut_bill_no(notifyBody);
        // 3. 处理回调
        boolean success = lotteryLogService.processTransferNotify(transferNotify);

        if (success) {
            return ResponseEntity.ok("{\"code\": \"SUCCESS\", \"message\": \"成功\"}");
        } else {
            return ResponseEntity.ok("{\"code\": \"FAIL\", \"message\": \"处理失败\"}");
        }
    }




}
