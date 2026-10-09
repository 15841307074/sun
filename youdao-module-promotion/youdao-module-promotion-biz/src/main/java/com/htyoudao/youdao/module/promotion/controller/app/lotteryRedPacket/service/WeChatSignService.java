package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.service;

import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.config.WeChatPayConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */

@Service
@Slf4j
public class WeChatSignService {

    @Autowired
    private WeChatPayConfig weChatPayConfig;

    /**
     * 生成微信支付签名
     */
    public String generateSignature(Map<String, String> params) {
        try {
            // 1. 过滤空值和sign参数
            Map<String, String> filteredParams = params.entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
                    .filter(entry -> !"sign".equals(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 2. 按字典序排序参数名
            List<String> keys = new ArrayList<>(filteredParams.keySet());
            Collections.sort(keys);

            // 3. 拼接参数
            StringBuilder stringA = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = filteredParams.get(key);
                stringA.append(key).append("=").append(value);
                if (i < keys.size() - 1) {
                    stringA.append("&");
                }
            }

            // 4. 拼接API密钥
            String stringSignTemp = stringA.toString() + "&key=" + weChatPayConfig.getMchKey();
            log.debug("待签名字符串: {}", stringSignTemp);

            // 5. MD5加密并转为大写
            String sign = DigestUtils.md5Hex(stringSignTemp).toUpperCase();
            log.debug("生成签名: {}", sign);

            return sign;

        } catch (Exception e) {
            log.error("生成签名失败: {}", e.getMessage(), e);
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * 验证微信支付签名
     */
    public boolean verifySignature(Map<String, String> params) {
        try {
            String receivedSign = params.get("sign");
            if (receivedSign == null) {
                return false;
            }

            String generatedSign = generateSignature(params);
            boolean isValid = receivedSign.equals(generatedSign);

            if (!isValid) {
                log.warn("签名验证失败");
                log.warn("接收到的签名: {}", receivedSign);
                log.warn("生成的签名: {}", generatedSign);
            }

            return isValid;
        } catch (Exception e) {
            log.error("验证签名异常: {}", e.getMessage());
            return false;
        }
    }
}
