package com.htyoudao.youdao.module.order.util;

import com.alibaba.fastjson2.JSON;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 日志工具类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-11
 */
public class LogUtil {
    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(pwd|password|secret|key|cardNo|idCard|mobile|email)",
            Pattern.CASE_INSENSITIVE);

    public static String maskSensitiveInfo(Object obj) {
        if (obj == null) return null;
        String json = JSON.toJSONString(obj);
        return maskSensitiveJson(json);
    }

    private static String maskSensitiveJson(String json) {
        return SENSITIVE_KEY_PATTERN.matcher(json)
                .replaceAll(matchResult -> {
                    String group = matchResult.group(1);
                    return group + "\":\"******";
                });
    }

    public static Map<String, String> maskSensitiveParams(Map<String, String> params) {
        return params.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> isSensitiveKey(e.getKey()) ? "******" : e.getValue()
                ));
    }

    private static boolean isSensitiveKey(String key) {
        return key.toLowerCase().matches(".*(pwd|password|secret|key|card).*");
    }
}

