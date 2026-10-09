package com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw.VO.TransferNotify;
import com.htyoudao.youdao.module.errand.framework.config.WechatPayConfig;
import com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw.ErrandRunnerWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 微信转账回调控制器
 */
@Slf4j
@RestController
@RequestMapping("/errand/runner/withdraw")
@RequiredArgsConstructor
public class WithdrawNotifyController {

    @Autowired
    private ErrandRunnerWithdrawService withdrawService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WechatPayConfig weChatPayConfig;

    /**
     * 微信转账回调通知
     */
    @PermitAll
    @DataPermission(enable = false)
    @PostMapping("/notify")
    @Operation(summary = "微信转账回调通知")
    public ResponseEntity<String> handleTransferNotify(
            HttpServletRequest request,
            @RequestBody String notifyBody) {

        log.info("=== 收到微信转账回调通知 ===");
        log.info("回调请求体: {}", notifyBody);

        try {
            // 解析回调数据
            log.info("开始解析回调数据");
            TransferNotify transferNotify = parseNotifyData(notifyBody);
            log.info("回调数据解析成功: transferNotify={}", transferNotify);

            // 处理回调
            String outBillNo = transferNotify.getOut_bill_no();
            String eventType = transferNotify.getEvent_type();
            String state = transferNotify.getState();
            String failReason = transferNotify.getFail_reason();

            log.info("回调参数: outBillNo={}, eventType={}, state={}, failReason={}",
                    outBillNo, eventType, state, failReason);

            boolean success = withdrawService.processWithdrawCallback(outBillNo, eventType, state, failReason);
            log.info("回调处理结果: success={}", success);

            if (success) {
                log.info("回调处理成功，返回SUCCESS");
                return ResponseEntity.ok("{\"code\": \"SUCCESS\", \"message\": \"成功\"}");
            } else {
                log.warn("回调处理失败，返回FAIL");
                return ResponseEntity.ok("{\"code\": \"FAIL\", \"message\": \"处理失败\"}");
            }

        } catch (Exception e) {
            log.error("处理提现回调异常: error={}", e.getMessage(), e);
            return ResponseEntity.ok("{\"code\": \"FAIL\", \"message\": \"处理异常\"}");
        }
    }

    /**
     * 解析回调数据
     */
    private TransferNotify parseNotifyData(String notifyBody) throws Exception {
        Map<String, Object> notifyMap = objectMapper.readValue(notifyBody, Map.class);
        String eventType = (String) notifyMap.get("event_type");

        // 解密resource数据
        Map<String, Object> resource = (Map<String, Object>) notifyMap.get("resource");
        String ciphertext = (String) resource.get("ciphertext");
        String associatedData = (String) resource.get("associated_data");
        String nonce = (String) resource.get("nonce");

        String decryptedData = decryptAes256Gcm(ciphertext,
                weChatPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8),
                associatedData != null ? associatedData.getBytes(StandardCharsets.UTF_8) : null,
                nonce.getBytes(StandardCharsets.UTF_8));

        TransferNotify transferNotify = objectMapper.readValue(decryptedData, TransferNotify.class);
        transferNotify.setEvent_type(eventType);

        return transferNotify;
    }

    // AES-GCM解密方法
    public String decryptAes256Gcm(String ciphertext, byte[] key, byte[] associatedData, byte[] nonce)
            throws Exception {
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonce);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        if (associatedData != null) {
            cipher.updateAAD(associatedData);
        }

        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    @PermitAll
    @DataPermission(enable = false)
    @PostMapping("/notifyTest")
    @Operation(summary = "微信转账回调通知")
    public ResponseEntity<String> handleTransferNotifyTest(@RequestParam("notifyBody") String outBillNo) {

        log.info("收到提现回调通知: {}", outBillNo);

        boolean success = withdrawService.processWithdrawCallbackTest(outBillNo);

        if (success) {
            return ResponseEntity.ok("{\"code\": \"SUCCESS\", \"message\": \"成功\"}");
        } else {
            return ResponseEntity.ok("{\"code\": \"FAIL\", \"message\": \"处理失败\"}");
        }
    }
}
