package com.htyoudao.youdao.module.order.controller.app.sq.utils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.Base64;

public class SignUtils {

    /**
     * 按 key ASCII 排序，拼接成：k1=v1&k2=v2...
     */
    public static String asciiKvJoin(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        keys.sort(Comparator.naturalOrder());

        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            String v = params.get(k);
            if (v == null) continue;
            if (sb.length() > 0) sb.append("&");
            sb.append(k).append("=").append(v);
        }
        return sb.toString();
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(dig);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }

    /**
     * RSA 公钥加密（PKCS1Padding），输出 Base64 字符串
     */
    public static String rsaEncryptBase64(String publicKeyBase64, String plainText) {
        try {
            PublicKey publicKey = loadPublicKey(publicKeyBase64);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] enc = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(enc);
        } catch (Exception e) {
            throw new RuntimeException("RSA encrypt failed", e);
        }
    }

    private static PublicKey loadPublicKey(String base64Key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Load public key failed", e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
