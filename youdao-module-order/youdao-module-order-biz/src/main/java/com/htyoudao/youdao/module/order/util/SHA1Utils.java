package com.htyoudao.youdao.module.order.util;

import jakarta.xml.bind.DatatypeConverter;
import org.apache.commons.lang3.ArrayUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class SHA1Utils {

    public static String sha1(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(hash).toLowerCase();
    }

    /**
     * 将参数按ASCII码大小升序排列进行拼接
     *
     * @param map
     * @param keys
     * @return
     */
    public static String ASCII(Map<String, String> map, String keys) {
        StringBuilder result = new StringBuilder();
        try {
            List<Map.Entry<String, String>> infoIds = new ArrayList<>(map.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            infoIds.sort(new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> map1, Map.Entry<String, String> map2) {
                    return (map1.getKey()).compareTo(map2.getKey());
                }
            });

            // 构造签名键值对的格式
            for (Map.Entry<String, String> item : infoIds) {
                String key = item.getKey();
                String val = item.getValue();
                if (key == null || key.equals("")) {
                    continue;// 跳过这些不签名
                }
                if (!result.toString().equals("")) {
                    result.append('&'); // 第一个字符串签名不加& 其他加&连接起来参数
                }
                result.append(key).append("=").append(val);
//                result.append(key).append("=").append(URLDecoder.decode(val, "utf-8"));
            }
            //result.append("&" + "secret" + "=");
            if (!StringUtils.isEmpty(keys)) {
                result.append(keys);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * 利用java原生的类实现SHA256加密
     *
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
            encodestr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 使用公钥对明文进行签名
     *
     * @param publicKey 公钥
     * @param plainText 明文
     * @return
     */
    public static String encrypt(String publicKey, String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
            byte[] bytes = plainText.getBytes();
            ByteArrayInputStream read = new ByteArrayInputStream(bytes);
            ByteArrayOutputStream write = new ByteArrayOutputStream();
            byte[] buf = new byte[117];
            int len = 0;
            while ((len = read.read(buf)) != -1) {
                byte[] buf1 = null;
                if (buf.length == len) {
                    buf1 = buf;
                } else {
                    buf1 = new byte[len];
                    for (int i = 0; i < len; i++) {
                        buf1[i] = buf[i];
                    }
                }
                byte[] bytes1 = cipher.doFinal(buf1);
                write.write(bytes1);
            }
            return Base64Util.encode(write.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到公钥
     *
     * @param key 密钥字符串（经过base64编码�?
     * @throws Exception
     */
//    public static PublicKey getPublicKey(String key) throws Exception {
//        byte[] keyBytes;
//        keyBytes = new BASE64Decoder().decodeBuffer(key);
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        return keyFactory.generatePublic(keySpec);
//    }
    public static PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 公钥解密过程
     *
     * @param key  公钥
     * @param data 密文数据
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String publicKeyDecrypt2(String key, String data) throws Exception {
        if (key == null) {
            throw new Exception("解密公钥为空, 请设置");
        }
        PublicKey publicKey = getPublicKey(key);
        byte[] cipherData = Base64Util.decode(data);
        Cipher cipher = null;
        try {
            // 使用默认RSA
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] buf = (byte[]) null;
            for (int i = 0; i < cipherData.length; i += 256) {
                byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(cipherData, i, i + 256));
                if (buf == null) {
                    buf = doFinal;
                } else {
                    buf = addBytes(buf, doFinal);
                }
            }
            return new String(buf);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("无此解密算法");
        } catch (NoSuchPaddingException e) {
            throw new Exception("可能需要更换填充方案或检查填充方案名称");
        } catch (InvalidKeyException e) {
            throw new Exception("解密公钥非法,请检查");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("密文长度非法");
        } catch (BadPaddingException e) {
            throw new Exception("密文数据已损坏");
        }
    }

    /**
     * 数组拼接
     *
     * @param data1 数组1
     * @param data2 数组2
     * @return
     */
    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

}
