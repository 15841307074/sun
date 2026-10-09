package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config;

import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.service.WeChatSignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SignatureDebugger {

    @Autowired
    private WeChatPayConfig weChatPayConfig;

    @Autowired
    private WeChatSignService weChatSignService;

    /**
     * 调试签名生成过程
     */
    public void debugSignatureGeneration(Map<String, String> params) {
        try {
            log.info("=== 签名调试信息 ===");

            // 1. 显示原始参数
            log.info("原始参数:");
            params.forEach((key, value) -> log.info("  {}: {}", key, value));

            // 2. 过滤后的参数
            Map<String, String> filteredParams = params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
                    .filter(entry -> !"sign".equals(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            log.info("过滤后参数:");
            filteredParams.forEach((key, value) -> log.info("  {}: {}", key, value));

            // 3. 排序后的参数
            List<String> keys = new ArrayList<>(filteredParams.keySet());
            Collections.sort(keys);

            log.info("排序后参数顺序:");
            keys.forEach(key -> log.info("  {}: {}", key, filteredParams.get(key)));

            // 4. 拼接字符串
            StringBuilder stringA = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = filteredParams.get(key);
                stringA.append(key).append("=").append(value);
                if (i < keys.size() - 1) {
                    stringA.append("&");
                }
            }

            String stringSignTemp = stringA.toString() + "&key=" + weChatPayConfig.getMchKey();
            log.info("待签名字符串: {}", stringSignTemp);

            // 5. 生成签名
            String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
            log.info("生成签名: {}", sign);

        } catch (Exception e) {
            log.error("签名调试失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 验证商户密钥格式
     */
    public void validateMchKey() {
        String mchKey = weChatPayConfig.getMchKey();
        if (mchKey == null) {
            log.error("商户密钥未配置");
            return;
        }

        log.info("商户密钥长度: {} 字符", mchKey.length());
        log.info("商户密钥前10字符: {}", mchKey.substring(0, Math.min(10, mchKey.length())) + "...");

        if (mchKey.length() != 32) {
            log.error("商户密钥长度不正确，应为32位，当前为{}位", mchKey.length());
        }
    }
}