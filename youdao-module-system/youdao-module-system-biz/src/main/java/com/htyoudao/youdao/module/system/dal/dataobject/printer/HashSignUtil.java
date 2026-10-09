package com.htyoudao.youdao.module.system.dal.dataobject.printer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * 哈稀签名工具类
 *
 * @author RabyGao
 * @date Aug 9, 2019
 */
public class HashSignUtil {

    /**
     * 哈稀签名
     *
     * @param signSource - 源字符串
     * @return
     */
    public static String sign(String signSource) {
        String signature = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(signSource.getBytes(StandardCharsets.UTF_8));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return signature;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

}
