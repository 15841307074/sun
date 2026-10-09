package com.htyoudao.youdao.module.order.core.ylb;

/**
 * <p>
 * 签名工具，简陋版
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SignUtils {

    private static final Gson gson = new Gson();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成请求签名
     * @param request 请求对象
     * @param secret 签名密钥
     * @return 大写MD5签名
     */
    public static String generateSign(ApiRequest<? extends Object> request, String secret) {
        TreeMap treeMap = new TreeMap<>(objectMapper.convertValue(request.getBody(), Map.class));
        // 1. 构建有序参数Map
        Map<String, Object> signMap = new TreeMap<>();
        signMap.put("body", treeMap);
        signMap.put("cmd", request.getCmd());
        signMap.put("source", request.getSource());
        signMap.put("timestamp", request.getTimestamp());
        signMap.put("version", request.getVersion());

        // 2. 转换为键值对字符串
        String paramString = mapToQueryString(signMap);

        // 3. 拼接密钥并生成MD5
        String stringToSign = paramString + secret;
        return md5(stringToSign).toUpperCase();
    }

    /**
     * 将Map转换为查询字符串
     */
    private static String mapToQueryString(Map<String, Object> map) {
        List<String> paramList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // Body特殊处理：转换为JSON字符串
                String jsonBody = gson.toJson(value);
                paramList.add(key + "=" + jsonBody);
            } else {
                paramList.add(key + "=" + value);
            }
        }
        return String.join("&", paramList);
    }

    /**
     * 生成MD5
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }
}
