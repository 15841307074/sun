package com.htyoudao.youdao.module.order.util;

import java.util.UUID;

/**
 * <p>
 * 支付相关工具类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-11
 */
public class PaymentUtil {

    public static String buildOrderNo(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    public static String maskSensitive(String content) {
        return content.replaceAll("(\"\\w*[Kk]ey\"\\s*:\\s*\")(\\w+)(\")", "$1****$3")
                .replaceAll("(\"cardNo\"\\s*:\\s*\")(\\d+)(\")", "$1****$3");
    }
}


